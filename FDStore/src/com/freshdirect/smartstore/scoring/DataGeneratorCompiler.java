package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.dsl.BinaryExpression;
import com.freshdirect.smartstore.dsl.BlockExpression;
import com.freshdirect.smartstore.dsl.ClassCompileException;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.dsl.Context;
import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.dsl.FunctionCall;
import com.freshdirect.smartstore.dsl.Operation;
import com.freshdirect.smartstore.dsl.Parser;
import com.freshdirect.smartstore.dsl.VariableAliasingVisitor;
import com.freshdirect.smartstore.dsl.VariableCollector;
import com.freshdirect.smartstore.dsl.VariableExpression;
import com.freshdirect.smartstore.dsl.VisitException;
import com.freshdirect.smartstore.external.ExternalRecommenderRegistry;
import com.freshdirect.smartstore.external.ExternalRecommenderType;

/**
 * This class is used to generate DataGenerator classes based on simple expressions.
 * 
 * @see DataGenerator
 * @author zsombor
 *
 */
public class DataGeneratorCompiler extends CompilerBase {

    static final String FN_RECURSIVE_NODES_EXCEPT = "RecursiveNodesExcept";
    static final String FN_RECURSIVE_NODES = "RecursiveNodes";
    static final String FN_PERSONALIZED_ITEMS_PREFIX = "PersonalizedItems_";
    static final String FN_RELATED_ITEMS_PREFIX = "RelatedItems_";
    static final String FN_TO_LIST = "toList";
	@Deprecated
    private static final String FN_PRODUCT_RECOMMENDATION = "ProductRecommendation";
	@Deprecated
    private static final String FN_USER_RECOMMENDATION = "PersonalRecommendation";
    // [APPDEV-3776]
    private static final String FN_SMART_CATEGORY = "SmartCategory";
    
    private static final String EXPLICIT_LIST = "explicitList";
    private static final String CURRENT_PRODUCT = "currentProduct";
    private static final String CURRENT_NODE = "currentNode"; // alias to currentProduct (which is not necessarily a product)
    private static final String CART_CONTENTS = "cartContents";
    private static final String RECENT_ITEMS = "recentItems";
    
    private static final String FN_SMART_YMAL = "SmartYMAL";

    private final static Collection<String> GLOBAL_VARIABLES = new HashSet<String>(); 
    {
        GLOBAL_VARIABLES.add(CURRENT_NODE);
        GLOBAL_VARIABLES.add(CURRENT_PRODUCT);
        GLOBAL_VARIABLES.add(EXPLICIT_LIST);
    }
    
    private static final String ITERATION_VARIABLE = "obj";
    
    final static String NODE_TYPE=ContentNodeModel.class.getCanonicalName();
    final static String SET_TYPE="List";
    
    boolean optimize = false;
    boolean caching = true;
    final static boolean CACHE_BY_CURRENT_NODE_ALSO = false;

    private static boolean checkIdentifier(String providerName) {
        for (int i = 0; i < providerName.length(); i++) {
            char c = providerName.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }
        return true;
    }

    private final String[] zoneDependentFactors;
    
    Set<String> globalVariables = Collections.emptySet();
    
    private class NodeFunction extends Context.FunctionDef {

        @SuppressWarnings( "unused" )
		public NodeFunction() {
            super();
        }

        public NodeFunction(int min, int max, int retType) {
            super(min, max, retType);
        }

        @SuppressWarnings( "unused" )
		public NodeFunction(int min, int max) {
            super(min, max);
        }

        @SuppressWarnings( "unused" )
		public NodeFunction(int params) {
            super(params);
        }
        
        @Override
        protected void validate(String name, String paramTypes) throws CompileException {
            validateParamCount(name, paramTypes);
            for (int i = 0; i < paramTypes.length(); i++) {
                int type = paramTypes.charAt(i);
                if (type != Expression.RET_NODE && type != Expression.RET_STRING && type != Expression.RET_SET) {
				    throw new CompileException(CompileException.TYPE_ERROR, "The "+""+(i+1)+". parameter"+" type is " + Expression.getTypeName(type)
				            + " instead of the expected node/set/string!");
				}
            }
        }
        
    }
    
    private static class PersonalizedExternalRecommenderFunction extends Context.FunctionDef {
    	private String providerName;
    	
		public PersonalizedExternalRecommenderFunction(String providerName) {
			super(0, 0, Expression.RET_SET);
			this.providerName = providerName;
		}
		
		@Override
		protected void validate(String name, String paramTypes) throws CompileException {
			validateParamCount(name, paramTypes);
		}
		
		@Override
		public String toJavaCode(FunctionCall call, List<Expression> parameters) throws CompileException {
			return "  HelperFunctions.getPersonalizedExternalRecommendations(\"" + providerName + "\" , sessionInput)";
		}
    }
    
    private static class RelatedExternalRecommenderFunction extends Context.FunctionDef {
    	private String providerName;
    	
		public RelatedExternalRecommenderFunction(String providerName) {
			super(1, 1, Expression.RET_SET);
			this.providerName = providerName;
		}
		
		@Override
		protected void validate(String name, String paramTypes) throws CompileException {
			validateParamCount(name, paramTypes);
            int type = paramTypes.charAt(0);
            if (type != Expression.RET_NODE && type != Expression.RET_STRING && type != Expression.RET_SET) {
			    throw new CompileException(CompileException.TYPE_ERROR, "The first parameter"+" type is " + Expression.getTypeName(type)
			            + " instead of the expected node/set/string!");
			}
		}
		
		@Override
		public String toJavaCode(FunctionCall call, List<Expression> parameters) throws CompileException {
			return "  HelperFunctions.getRelatedExternalRecommendations(" + createScriptConvertToSet(parameters.get(0)) 
					+ ", \"" + providerName + "\" , sessionInput)";
		}
    }

    private class RecursiveNodesFunction extends NodeFunction {
        public RecursiveNodesFunction() {
            super(1, Integer.MAX_VALUE, Expression.RET_SET);
        }

        @Override
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            if (parameters.size() == 1) {
                Expression param = parameters.get(0);
                return handleOneParameteredFunction(param);
            }
            
			StringBuffer buffer = new StringBuffer();
			buffer.append("  HelperFunctions.trace(sessionInput, \"Recursive Nodes\", HelperFunctions.recursiveNodes(new Object[] { ");
			for (int i = 0; i < parameters.size(); i++) {
			    if (i > 0) {
			        buffer.append(',');
			    }
			    buffer.append(parameters.get(i).toJavaCode());
			}
			buffer.append("}))");
			return buffer.toString();
        }


        String handleOneParameteredFunction(Expression param) throws CompileException {
            switch (param.getReturnType()) {
                case Expression.RET_NODE:
                case Expression.RET_STRING:
                case Expression.RET_SET:
                    return "  HelperFunctions.trace(sessionInput, \"Recursive Nodes\", HelperFunctions.recursiveNodes(" + createScriptConvertToNodeOrSet(param) + "))";
                default:
					throw new CompileException(CompileException.TYPE_ERROR, "The "+"first parameter"+" type is "
							+ Expression.getTypeName(param.getReturnType())
				            + " instead of the expected node/set/string!");
            }
        }
    }

    private class ManuallyOverridenSlotsFunction extends NodeFunction {
        public ManuallyOverridenSlotsFunction() {
            super(1, 1, Expression.RET_SET);
        }

        @Override
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            if (parameters.size() == 1) {
                Expression param = parameters.get(0);
                switch (param.getReturnType()) {
                    case Expression.RET_NODE:
                    case Expression.RET_STRING:
                        return "  HelperFunctions.getManuallyOverriddenSlots(" + createScriptConvertToNodeOrSet(param) + ", sessionInput, input)";
                    default:
                        throw new CompileException(CompileException.PARAMETER_ERROR, "first parameter has unexpected type of "
                                + Expression.getTypeName(param.getReturnType()) + ", but expected node or string");
                }
            }
			throw new CompileException(CompileException.PARAMETER_ERROR, "too many parameters");
        }
    }

    private class ManuallyOverridenSlotsPFunction extends NodeFunction {
        public ManuallyOverridenSlotsPFunction() {
            super(1, 1, Expression.RET_SET);
        }


        @Override
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            if (parameters.size() == 1) {
                Expression param = parameters.get(0);
                switch (param.getReturnType()) {
                    case Expression.RET_NODE:
                    case Expression.RET_STRING:
                        return "  HelperFunctions.getManuallyOverriddenSlotsP(" + createScriptConvertToNodeOrSet(param) + ", sessionInput, input)";
                    default:
                    throw new CompileException(CompileException.PARAMETER_ERROR, "first parameter has unexpected type of "
                            + Expression.getTypeName(param.getReturnType()) + ", but expected node or string");
                }
            }
			throw new CompileException(CompileException.PARAMETER_ERROR, "too many parameters");
        }
    }

    private class TopFunction extends NodeFunction {
        public TopFunction() {
            super(3, 3, Expression.RET_SET);
        }

        @Override
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            if (parameters.size() == 3) {
                Expression param0 = parameters.get(0);
                if (param0.getReturnType() != Expression.RET_SET) {
			        throw new CompileException(CompileException.TYPE_ERROR, "The "+"first parameter"+" type is " + Expression.getTypeName(param0.getReturnType())
				            + " instead of the expected node/set/string!");
				}
                Expression param1 = parameters.get(1);
                if (!(param1 instanceof VariableExpression)
                		|| !(param1.getReturnType() == Expression.RET_FLOAT ||
                				param1.getReturnType() == Expression.RET_INT)) {
                	throw new CompileException(CompileException.PARAMETER_ERROR, "The second parameter must be a factor variable!");
                }
                String factorName = ((VariableExpression) param1).getVariableName();
                Expression param2 = parameters.get(2);
                if (param2.getReturnType() != Expression.RET_INT) {
                    throw new CompileException(CompileException.TYPE_ERROR, "The third parameter must have type INT!");
                }

                return "  HelperFunctions.getTopN(" + param0.toJavaCode() + ", \"" + factorName + "\", " + param2.toJavaCode() +", sessionInput, input)";
            }
			throw new CompileException(CompileException.PARAMETER_ERROR, "3 parameters expected");
        }

        @Override
        protected void validate(String name, String paramTypes) throws CompileException {
        }
    }
    
    private class SkuPrefixFilter extends NodeFunction {
        
        public SkuPrefixFilter() {
            super(1, -1, Expression.RET_INT);
        }
        
        @Override
        public String toJavaCode(FunctionCall call, List<Expression> parameters) throws CompileException {
            String tempVariable = call.getContext().createTempVariable(call);
            return "HelperFunctions.matchSkuPrefix("+ITERATION_VARIABLE+","+tempVariable+")";
        }
        
        @Override
        public String getPreparingCode(FunctionCall call, List<Expression> parameters) {
            String tempVariable = call.getContext().createTempVariable(call);

            List<String> prefixes = new ArrayList<String>();
            for (Expression xp : parameters) {
                String val = xp.getStringValue();
                String[] arr = val.split(",");
                for (String s : arr) {
                    prefixes.add(s);
                }
            }
            StringBuilder result = new StringBuilder("   String[] ").append(tempVariable).append(" = new String[] { \n");
            for (int i = 0; i < prefixes.size(); i++) {
                if (i>0) {
                    result.append(",\n");
                }
                result.append("    \"").append(prefixes.get(i).replaceAll("\"", "\\\"")).append("\"");
            }
            result.append(" };\n");
            return result.toString();
        }
        
        @Override
        protected void validate(String name, String paramTypes) throws CompileException {
            for (int i = 0; i < paramTypes.length(); i++) {
                int type = paramTypes.charAt(i);
                if (type != Expression.RET_STRING) {
                    throw new CompileException(CompileException.TYPE_ERROR, "The " + "" + (i + 1) + ". parameter" + " type is " + Expression.getTypeName(type)
                            + " instead of the expected string!");
                }
            }
        }
    }
    
    private class MatchBrandFilter extends NodeFunction {
        public MatchBrandFilter () {
            super(1, 1, Expression.RET_INT);
        }

        @Override
        public String toJavaCode(FunctionCall call, List<Expression> parameters) throws CompileException {
            String tempVariable = call.getContext().createTempVariable(call);
            return "HelperFunctions.matchBrand("+ITERATION_VARIABLE+","+tempVariable+")";
        }

        @Override
        public String getPreparingCode(FunctionCall call, List<Expression> parameters) throws CompileException {
            String tempVariable = call.getContext().createTempVariable(call);
            return "  "+ NODE_TYPE + ' '+ tempVariable + " = HelperFunctions.findBrand("+createScriptConvertToSet( parameters.get(0))+");\n"; 
        }
    }
    
    /**
     * Create a java fragment which converts the given expression to an expression which returns a content node.
     * 
     * @param param
     * @return
     * @throws CompileException if the given expression expression type is not a string or a node. 
     */
    static String createScriptConvertToNode(Expression param) throws CompileException {
        if (param.getReturnType() == Expression.RET_NODE) {
            return "(" + NODE_TYPE + ")" + param.toJavaCode();
        }
        if (param.getReturnType() == Expression.RET_STRING) {
            return "HelperFunctions.lookup((String)" + param.toJavaCode() + ")";
        }
        throw new CompileException(CompileException.TYPE_ERROR, "Node or string parameter expected (" + param + ")!");
    }

    /**
     * Create a java fragment which converts the given expression to an expression which returns a set of content nodes or an unique node.
     * 
     * @param param
     * @return
     * @throws CompileException if the given expression expression type is not a string or a node or a set of content nodes. 
     */
    static String createScriptConvertToNodeOrSet(Expression param) throws CompileException {
        if (param.getReturnType() == Expression.RET_NODE) {
            return "(" + NODE_TYPE + ")" + param.toJavaCode();
        }
        if (param.getReturnType() == Expression.RET_STRING) {
            return "HelperFunctions.lookup((String)" + param.toJavaCode() + ")";
        }
        if (param.getReturnType() == Expression.RET_SET) {
            return "(" + SET_TYPE + ")" + param.toJavaCode();
        }
        throw new CompileException(CompileException.TYPE_ERROR, "Node or string parameter expected (" + param + ")!");
    }
    
    /**
     * Create a java fragment which converts the given expression to an expression which returns a set of content nodes.
     * 
     * @param param
     * @return
     * @throws CompileException if the given expression expression type is not a string or a node or a set of content nodes. 
     */
    static String createScriptConvertToSet(Expression param) throws CompileException {
        if (param.getReturnType() == Expression.RET_NODE) {
            return "HelperFunctions.toList((" + NODE_TYPE + ")" + param.toJavaCode() + ")";
        }
        if (param.getReturnType() == Expression.RET_STRING) {
            return "HelperFunctions.toList(HelperFunctions.lookup((String)" + param.toJavaCode() + "))";
        }
        if (param.getReturnType() == Expression.RET_SET) {
            return "(" + SET_TYPE + ")" + param.toJavaCode();
        }
        throw new CompileException(CompileException.TYPE_ERROR, "Node or string parameter expected (" + param + ")!");
    }
    
    class RecursiveNodesExceptFunction extends RecursiveNodesFunction {
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            if (parameters.size() == 1) {
                Expression param = parameters.get(0);
                return handleOneParameteredFunction(param);
            }
			Expression param = parameters.get(0);
			if (parameters.size() == 2) {
			    Expression exp = parameters.get(1);
			    return "  HelperFunctions.recursiveNodesExcept(" + createScriptConvertToNodeOrSet(param) + ",(Object) " + exp.toJavaCode() + ")";
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append("  HelperFunctions.recursiveNodesExcept("+ createScriptConvertToNodeOrSet(param)+ ",new Object[] { ");
			for (int i = 1; i < parameters.size(); i++) {
			    if (i > 1) {
			        buffer.append(',');
			    }
			    buffer.append(parameters.get(i).toJavaCode());
			}
			buffer.append("})");
			return buffer.toString();
        }        
    }

    static class OperationCompileResult {
        String tempVariableName;
        String codeFragment;

        public OperationCompileResult() {
            tempVariableName = "null";
            codeFragment = "";
        }

        public OperationCompileResult(String variableName, String codeFragment) {
            this.tempVariableName = variableName;
            this.codeFragment = codeFragment;
        }
        
        @Override
        public String toString() {
            return codeFragment;
        }
    }

    static class CompileState {
        int lastTempVariable = 0;
        Set<String> declaredVariables;

        public CompileState() {
        	declaredVariables = new HashSet<String>();
        }
    }
    
    public DataGeneratorCompiler(String[] zoneDependentFactors) {
    	this.zoneDependentFactors = zoneDependentFactors;
    }
    
    protected  void setupParser(Parser parser) {
        super.setupParser(parser);
        parser.getContext().addFunctionDef(FN_RECURSIVE_NODES, new RecursiveNodesFunction());
        parser.getContext().addFunctionDef(FN_RECURSIVE_NODES_EXCEPT, new RecursiveNodesExceptFunction());
        parser.getContext().addFunctionDef(FN_TO_LIST, new NodeFunction(1, Integer.MAX_VALUE, Expression.RET_SET) {

            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                if (parameters.size()==1) {
                    Expression param = parameters.get(0);
                    switch (param.getReturnType()) {
                        case Expression.RET_NODE:
                            return "  HelperFunctions.toList((" + NODE_TYPE + ")" + param.toJavaCode() + ")";
                        case Expression.RET_STRING:
                            return "  HelperFunctions.toList((String)" + param.toJavaCode() + ")";
                        case Expression.RET_SET:
                            return "  " + param.toJavaCode() + "";
                    }
                    throw new CompileException(CompileException.TYPE_ERROR, "The "+"first parameter"+" type is " + Expression.getTypeName(param.getReturnType())
                            + " instead of the expected node/set/string!");
                }
				StringBuffer buffer = new StringBuffer();
				buffer.append("  HelperFunctions.toList(new Object[] { ");
				for (int i = 0; i < parameters.size(); i++) {
				    if (i > 0) {
				        buffer.append(',');
				    }
				    buffer.append(parameters.get(i).toJavaCode());
				}
				buffer.append("})");
				return buffer.toString();
            }
        });
        parser.getContext().addFunctionDef("ParentCategory", new Context.FunctionDef(1, 1, Expression.RET_NODE) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression param = parameters.get(0);
                return "  HelperFunctions.getParentCategory(" + createScriptConvertToNode(param) + ")";
            }
        });
        parser.getContext().addFunctionDef("TopLevelCategory", new Context.FunctionDef(1, 1, Expression.RET_NODE) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression param = parameters.get(0);
                return "  HelperFunctions.getToplevelCategory(" + createScriptConvertToNode(param) + ")";
            }
        });

        parser.getContext().addFunctionDef("Department", new Context.FunctionDef(1, 1, Expression.RET_NODE) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression param = parameters.get(0);
                return "  HelperFunctions.getParentDepartment(" + createScriptConvertToNode(param) + ")";
            }
        });
        parser.getContext().addFunctionDef("ManuallyOverriddenSlots", new ManuallyOverridenSlotsFunction());
        parser.getContext().addFunctionDef("ManuallyOverriddenSlotsP", new ManuallyOverridenSlotsPFunction());
        parser.getContext().addFunctionDef("prioritize", new Context.FunctionDef(0, 0, Expression.RET_INT) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                return "HelperFunctions.addPrioritizedNode(input," + ITERATION_VARIABLE + ")";
            }
        });
        parser.getContext().addFunctionDef("deprioritize", new Context.FunctionDef(0, 0, Expression.RET_INT) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                return "HelperFunctions.addDeprioritizedNode(input," + ITERATION_VARIABLE + ")";
            }
        });
        parser.getContext().addFunctionDef("Top", new TopFunction());

        parser.getContext().addFunctionDef(FN_PRODUCT_RECOMMENDATION, new Context.FunctionDef(2, 2, Expression.RET_SET) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression param0 = parameters.get(0);
                Expression param1 = parameters.get(1);
                if (param0.getReturnType() != Expression.RET_STRING) {
                    throw new CompileException(CompileException.TYPE_ERROR, "First parameter should be a string for " + name + "!");
                }
                return "  HelperFunctions.getProductRecommendationFromVendor(" + param0.toJavaCode() + ',' + createScriptConvertToNode(param1) + ")";
            }
        });

        parser.getContext().addFunctionDef(FN_USER_RECOMMENDATION, new Context.FunctionDef(1, 1, Expression.RET_SET) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression param0 = parameters.get(0);
                if (param0.getReturnType() != Expression.RET_STRING) {
                    throw new CompileException(CompileException.TYPE_ERROR, "First parameter should be a string for " + name + "!");
                }
                return "  HelperFunctions.getUserRecommendationFromVendor(" + param0.toJavaCode() + ", sessionInput.getCustomerId())";
            }
        });        
        parser.getContext().addFunctionDef("matchSkuPrefix", new SkuPrefixFilter());
        parser.getContext().addFunctionDef("matchBrand", new MatchBrandFilter());
        
        Set<String> personalizedRecommenders = ExternalRecommenderRegistry.getRegisteredRecommenders(ExternalRecommenderType.PERSONALIZED);
        for (String providerName : personalizedRecommenders) {
        	if (checkIdentifier(providerName)) {
        		parser.getContext().addFunctionDef(FN_PERSONALIZED_ITEMS_PREFIX + providerName, new PersonalizedExternalRecommenderFunction(providerName));
        	}
        }
        Set<String> relatedRecommenders = ExternalRecommenderRegistry.getRegisteredRecommenders(ExternalRecommenderType.RELATED);
        for (String providerName : relatedRecommenders) {
        	if (checkIdentifier(providerName)) {
        		parser.getContext().addFunctionDef(FN_RELATED_ITEMS_PREFIX + providerName, new RelatedExternalRecommenderFunction(providerName));
        	}
        }
        
        parser.getContext().addVariable(CURRENT_PRODUCT, Expression.RET_NODE);
        parser.getContext().addVariable(EXPLICIT_LIST, Expression.RET_SET);
        parser.getContext().addVariable(CART_CONTENTS, Expression.RET_SET);
        parser.getContext().addVariable(RECENT_ITEMS, Expression.RET_SET);

        // you have to add alias variables here
        parser.getContext().addVariable(CURRENT_NODE, Expression.RET_NODE);


        parser.getContext().addFunctionDef(FN_SMART_YMAL, new Context.FunctionDef(0, 0, Expression.RET_SET) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                return "HelperFunctions.getSmartYMALRecommendation(sessionInput)";
            }
        });

        // NOTE: actually, getSmartCategoryRecommendation() uses sessionInput instead of
        // explicit parameters
        parser.getContext().addFunctionDef(FN_SMART_CATEGORY, new Context.FunctionDef(0, Integer.MAX_VALUE, Expression.RET_SET) {
        	@Override
        	public String toJavaCode(String name, List<Expression> parameters)
        			throws CompileException {
        		return "HelperFunctions.getSmartCategoryRecommendation(sessionInput)";
        	}
        });
    }

    public synchronized DataGenerator createDataGenerator(String name, String expression) throws CompileException {
        getParser().getContext().cleanupTemporary();
        BlockExpression expr = parse(expression);
        try {
            if (expr.size() > 1) {
                throw new CompileException("One expression expected instead of " + expr.size() + " in '" + expr.toCode() + "'!");
            }

            // resolve aliases here
            expr.visit(null, new VariableAliasingVisitor(CURRENT_NODE, CURRENT_PRODUCT));

            if (optimize) {
                RecursiveNodesCallOptimizer optimizer = new RecursiveNodesCallOptimizer();
                expr.visit(null, optimizer);
                if (optimizer.isOptimizationOccured()) {
                    expression += " -> optimized to : "+expr.toCode();
                }
            }
            VariableCollector vc = new VariableCollector();
            expr.visit(null, vc);

            Class<? extends DataGenerator> generated = compileAlgorithm(name, expr, expression, vc);
            
            DataGenerator dg = generated.newInstance();
            

            Context context = getParser().getContext();
            
            Set<String> vars = vc.getVariables();
            for (Iterator<String> iter = vars.iterator(); iter.hasNext();) {
                String varName = iter.next();
                int type = context.getVariableType(varName);
                if (type != Expression.RET_FLOAT && type != Expression.RET_INT) {
                    iter.remove();
                }
            }
            dg.setFactors(vars);
            context.cleanupTemporary();
            return dg;
        } catch (InstantiationException e) {
            throw new ClassCompileException("InstantiationException:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ClassCompileException("IllegalAccessException:" + e.getMessage(), e);
        } catch (VisitException e) {
            throw new ClassCompileException("VisitException:" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends DataGenerator> compileAlgorithm(String name, BlockExpression ast, String toStringValue, VariableCollector vc) throws CompileException {

        CtClass class1 = pool.makeClass(packageName + name);
        CtClass parent;

        boolean doCaching = caching && isCacheable(vc.getVariables()) && isCacheableByFunctions(vc.getFunctions());

        try {
            parent = doCaching ? pool.get(CachingDataGenerator.class.getName()) : pool.get(DataGenerator.class.getName());
            class1.setSuperclass(parent);
            pool.importPackage("java.util");
            pool.importPackage("com.freshdirect.smartstore.scoring");
            pool.importPackage("com.freshdirect.smartstore");

            CtMethod method = createGenerateMethod(class1, ast, doCaching, vc);
            if (method != null) {
                class1.addMethod(method);
            }
            if (doCaching) {
                method = createKeyCreatorMethod(class1, toStringValue, vc);
                class1.addMethod(method);
            }
            
            method = createToStringMethod(class1, toStringValue);
            class1.addMethod(method);

            return class1.toClass();
        } catch (NotFoundException e) {
            throw new ClassCompileException("NotFound:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new ClassCompileException("CannotCompile:" + e.getMessage(), e);
        } 
    }


    /**
     * return true if all the functions returns a deterministic values, so the script result 
     * can be cached. 
     * @param functions
     * @return
     */
    private boolean isCacheableByFunctions(Set<String> functions) {
    	for (String function : functions)
    		if (function.startsWith(FN_PERSONALIZED_ITEMS_PREFIX)
    				|| function.startsWith(FN_RELATED_ITEMS_PREFIX)
    				|| function.equals(FN_USER_RECOMMENDATION)
    				|| function.equals(FN_SMART_YMAL))
    			// FIXME APPDEV-3776 is smart cat recommender cacheable?
    			return false;
        return true;
    }

    private CtMethod createKeyCreatorMethod(CtClass class1, String toStringValue, VariableCollector vc) throws CannotCompileException {
        Set<String> keys = this.getCachingKeys(vc.getVariables());
        StringBuffer b = new StringBuffer("public String getKey(SessionInput input) {\n");
        b.append("   return \""+toStringValue.replace('"', '\'')+"\"");
        if (containsZoneDependentFactor(vc.getVariables())) {
            b.append("+ \"%zone=\" + input.getPricingContext().getZoneInfo().getPricingZoneId()");
        }
        if (keys.contains(CURRENT_PRODUCT)) {
            b.append("+ '$' + HelperFunctions.getCurrentNodeCacheKey(input)");
        }
        if (keys.contains(EXPLICIT_LIST)) {
            b.append("+ '$' + HelperFunctions.getExplicitListCacheKey(input)");
        }
        b.append(";\n");
        b.append(" } ");
        return CtNewMethod.make(b.toString(), class1);
    }

    private CtMethod createGenerateMethod(CtClass class1, BlockExpression ast, boolean doCaching,VariableCollector vc) throws CompileException {
        CompileState c = new CompileState();

        Expression expression = ast.get(0);
        expression.validate();
        
        String initCode = expression.getJavaInitializationCode();
        
        OperationCompileResult oc = compile(c, expression);
        StringBuilder buffer = new StringBuilder("public List ")
            .append(doCaching? "generateImpl" : "generate" )
            .append("(com.freshdirect.smartstore.SessionInput sessionInput, DataAccess input) {\n");
        
        buffer.append(" String userId = sessionInput.getCustomerId();\n");
        buffer.append(" com.freshdirect.common.pricing.PricingContext pricingCtx = sessionInput.getPricingContext();\n");
        if (vc.getVariables().contains(CURRENT_PRODUCT)) {
            buffer.append(" "+NODE_TYPE+ " currentProduct = sessionInput.getCurrentNode();\n");
        }
        if (vc.getVariables().contains(EXPLICIT_LIST)) {
            buffer.append(" "+SET_TYPE+ ' ' + EXPLICIT_LIST+ "  = sessionInput.getExplicitList();\n");
        }
        if (vc.getVariables().contains(CART_CONTENTS)) {
			buffer.append(" " + SET_TYPE + ' ' + CART_CONTENTS + "  = HelperFunctions.toList(sessionInput.getCartContents());\n");
		}
        if (vc.getVariables().contains(RECENT_ITEMS)) {
			buffer.append(" " + SET_TYPE + ' ' + RECENT_ITEMS + "  = HelperFunctions.toList(sessionInput.getRecentItems());\n");
		}
        buffer.append(initCode);
        
        buffer.append(oc.codeFragment);
        buffer.append("\n  return ").append(oc.tempVariableName).append(";\n");
        buffer.append("}");
        try {
            CtMethod method = createReturningStringMethod(class1, "getGeneratedCode", buffer.toString());
            class1.addMethod(method);
            
            return CtNewMethod.make(buffer.toString(), class1);
        } catch (CannotCompileException e) {
            throw new CompileException("Compiling "+ast.toCode()+", generated code:"+buffer.toString()+", error:"+e.getMessage(), e);
        }
    }

    private OperationCompileResult compile(CompileState c, Expression expression) throws CompileException {
        OperationCompileResult oc = new OperationCompileResult();
        if (expression instanceof VariableExpression) {
            oc = compileVariable(c, (VariableExpression) expression);
        } else if (expression instanceof Operation) {
            //oc = compileOperation(c, (Operation) expression);
            BinaryExpression bexp = ((Operation) expression).fixPrecedence();
            if (bexp != null) {
                oc = compileBinaryOperation(c, bexp);
            } else {
                oc = compile(c, ((Operation) expression).getUniqueExpression());
            }
        } else if (expression instanceof BinaryExpression) {
            oc = compileBinaryOperation(c, (BinaryExpression) expression);
            
        } else if (expression instanceof FunctionCall) {
            oc = compileFunctionCall(c, (FunctionCall) expression);
        }
        return oc;
    }
    
    private OperationCompileResult compileFunctionCall(CompileState c, FunctionCall expression) throws CompileException {
        String varName = "tmp" + c.lastTempVariable++;
        int retType = expression.getReturnType();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < expression.getParams().size(); i++) {
        	buffer.append(compile(c, expression.getParam(i)).codeFragment);
        }
        buffer.append("  ").append(getType(retType)).append(' ').append(varName).append(" = ").append(expression.toJavaCode()).append(";\n");
        expression.getContext().putJavaTempVariableId(expression, varName);
        return new OperationCompileResult(varName, buffer.toString());
    }

    private OperationCompileResult compileVariable(CompileState c, VariableExpression expression) {
        if (expression.getReturnType() != Expression.RET_SET) {
            return new OperationCompileResult();
        }
        if (c.declaredVariables.contains(expression.getVariableName())) {
            return new OperationCompileResult(expression.getContext().getJavaVariableId(expression.getVariableName()), "");
        }
        
        c.declaredVariables.add(expression.getVariableName());
        String varName = "tmp" + c.lastTempVariable++;
        expression.getContext().putJavaVariableId(expression.getVariableName(), varName);
        if (isVariableFromDatasource(expression.getVariableName())) {
            OperationCompileResult oc = new OperationCompileResult(varName, "  List " + varName + " = input.fetchContentNodes(sessionInput, \"" + expression.getVariableName()
                    + "\");\n");
            return oc;
        }
		OperationCompileResult oc = new OperationCompileResult(varName, "  List " + varName + " = " + expression.getVariableName() + ";\n");
		return oc;            
    }
    
    protected boolean isVariableFromDatasource(String name) {
        return !EXPLICIT_LIST.equals(name) && !CART_CONTENTS.equals(name) && !RECENT_ITEMS.equals(name);
    }
    
    private OperationCompileResult compileBinaryOperation(CompileState c, BinaryExpression expression) throws CompileException {
        OperationCompileResult left = compile(c, expression.getLeft());
        StringBuilder buffer = new StringBuilder();

        switch (expression.getOperator()) {
            case '+' : {
                OperationCompileResult right = compile(c, expression.getRight());
                buffer.append(left.codeFragment);
                buffer.append(right.codeFragment);
                
                buffer.append("\n  ").append(left.tempVariableName).append(".addAll(").append(right.tempVariableName).append(");\n");
                return new OperationCompileResult(left.tempVariableName, buffer.toString());
            }
            case '*' : {
                OperationCompileResult right = compile(c, expression.getRight());
                buffer.append(left.codeFragment);
                buffer.append(right.codeFragment);
                
                buffer.append("\n  ").append(left.tempVariableName).append(".retainAll(").append(right.tempVariableName).append(");\n");
                return new OperationCompileResult(left.tempVariableName, buffer.toString());
            }
            case '-' : {
                OperationCompileResult right = compile(c, expression.getRight());
                buffer.append(left.codeFragment);
                buffer.append(right.codeFragment);
                
                buffer.append("\n  ").append(left.tempVariableName).append(".removeAll(").append(right.tempVariableName).append(");\n");
                return new OperationCompileResult(left.tempVariableName, buffer.toString());
            }
            case ':' : {
                String arrayName = "arr"+c.lastTempVariable;
                String varName = "tmp" + c.lastTempVariable++;
                buffer.append("  List ").append(varName).append(" = new ArrayList();\n");
                buffer.append(left.codeFragment);
                
                buffer.append("  ").append(varName).append(".addAll(").append(left.tempVariableName).append(");\n");
                buffer.append("  String[] ").append(arrayName).append(";\n");

                Expression xpr = expression.getRight();

                try {
                    generateFilteringFunction(buffer, arrayName, varName, xpr);
                    return new OperationCompileResult(varName, buffer.toString());
                    
                } catch (VisitException e) {
                    e.printStackTrace();
                }
                
            }
            default : 
                throw new CompileException(CompileException.UNKNOWN_OPERATION, "Error, unknown operation in "+expression);
        }
    }

    /**
     * 
     * The expected function is something like this for filtering:
     * 
     * for (Iterator iter=tmp0.iterator();iter.hasNext();) { 
     *          TypeX obj = (TypeX) iter.next(); 
     *          Map variables = input.getVariables(obj); 
     *          double popularity = variables.get("popularity").doubleValue(); 
     *          int weight = variables.get("weight").doubleValue();
     * 
     *          if (someFunc(popularity, weight)==0) { 
     *                  iter.remove(); 
     *          } 
     * }
     * 
     * 
     * @param buffer
     * @param arrayName
     * @param varName
     * @param xpr
     * @throws VisitException
     * @throws ClassCompileException
     * @throws CompileException
     */
    private void generateFilteringFunction(StringBuilder buffer, String arrayName, String varName, Expression xpr) throws VisitException,
            CompileException {
        //String functionName = xpr instanceof FunctionCall ? ((FunctionCall) xpr).getName() : null;
        
        VariableCollector vc = new VariableCollector();
        xpr.visit(null, vc);
        vc.getVariables().removeAll(GLOBAL_VARIABLES);
        boolean constantExpression = vc.getVariables().isEmpty();
        if (!constantExpression) {
            buffer.append("  ").append(arrayName).append(" = new String[] {\n");
            boolean first = true;
            for (Iterator<String> iter=vc.getVariables().iterator();iter.hasNext();) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(",\n");
                }
                buffer.append("      \"").append(iter.next()).append('"');
            }
            buffer.append("   };\n");
        }
        buffer.append("  for (Iterator iter=").append(varName).append(".iterator();iter.hasNext();) {\n     " 
                + NODE_TYPE + ' ' + ITERATION_VARIABLE + " = (" + NODE_TYPE + ") iter.next();\n");
        Context context = getParser().getContext();
        if (!constantExpression) {
            buffer.append("     double[] vars = input.getVariables(userId, pricingCtx, " + ITERATION_VARIABLE + "," + arrayName + ");\n");
            int index = 0;
            for (Iterator<String> iter=vc.getVariables().iterator();iter.hasNext();) {
                buffer.append("   ").append(declareVariable(context, iter.next(), "vars["+index+']'));
            }
        }
//        if ("prioritize".equals(functionName)) {
//            buffer.append("     input.addPrioritizedNode(obj);\n");
//        }
        buffer.append("     if ((").append(xpr.toJavaCode()).append(") == 0) {\n"
                + "        iter.remove();\n"
                + "     }\n"
                + "   }");
    }

    @SuppressWarnings("unused")
	private OperationCompileResult compileOperation(CompileState c, Operation expression) throws CompileException {
        String arrayName = "arr"+c.lastTempVariable;
        String varName = "tmp" + c.lastTempVariable++;
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("  List ").append(varName).append(" = new ArrayList();\n");
        buffer.append("  String[] ").append(arrayName).append(";\n");
        char operation = '+';
        
        for (int i = 0; i < expression.size(); i++) {
            Expression xpr = expression.get(i);
            switch (operation) {
                case '+' : {
                    OperationCompileResult cc = compile(c, xpr);
                    buffer.append(cc.codeFragment);
                    buffer.append("  ").append(varName).append(".addAll(").append(cc.tempVariableName).append(");\n");
                    //buffer.append("  HelperFunctions.addAll(").append(varName).append(",").append(cc.tempVariableName).append(");\n");
                    break;
                }
                case '*' : {
                    OperationCompileResult cc = compile(c, xpr);
                    buffer.append(cc.codeFragment);
                    buffer.append("  ").append(varName).append(".retainAll(").append(cc.tempVariableName).append(");\n");
                    break;
                }
                case '-' : {
                    OperationCompileResult cc = compile(c, xpr);
                    buffer.append(cc.codeFragment);
                    buffer.append("  ").append(varName).append(".removeAll(").append(cc.tempVariableName).append(");\n");
                    break;
                }
                case ':' : {
                    try {
                        generateFilteringFunction(buffer, arrayName, varName, xpr);
                        break;
                    } catch (VisitException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
            operation = i<expression.size()-1 ? expression.getOperator(i) : ' ';
        }
        
        return new OperationCompileResult(varName, buffer.toString());
    }

    String getType(int type) throws CompileException {
        switch (type) {
            case Expression.RET_NODE : return NODE_TYPE;
            case Expression.RET_SET : return "List";
            case Expression.RET_STRING : return "String";
            case Expression.RET_FLOAT : return "double";
            case Expression.RET_INT : return "int";
            default : throw new CompileException(CompileException.TYPE_ERROR, "Java type not found for:"+Expression.getTypeName(type));
        }
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }
    
    public boolean isOptimize() {
        return optimize;
    }
    
    public void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    public boolean isCaching() {
        return caching;
    }

    /**
     * 
     * @param globalVariables
     */
    public void setGlobalVariables(Set<String> globalVariables) {
        this.globalVariables = globalVariables;
    }
    
    /**
     * return the cache key for the given variable, or null, if it's not possible to cache. For example 'PurchaseHistory' is specific to user, 
     * so it shouldn't be cached. 'explicitList', 'currentProduct', 'FeaturedItems' and 'CandidateList' is global, so it can be cached.
     *   
     * @param variable
     * @return
     */
    private String getCacheKeyForVariable(String variable) {
        if (EXPLICIT_LIST.equals(variable)) {
            return EXPLICIT_LIST;
        }
        if (CACHE_BY_CURRENT_NODE_ALSO) {
            if (CURRENT_PRODUCT.equals(variable)) {
                return CURRENT_PRODUCT;
            }
            if ("FeaturedItems".equals(variable)) {
                return CURRENT_PRODUCT;
            }
            if ("CandidateLists".equals(variable)) {
                return CURRENT_PRODUCT;
            }
        }
        return null;
    }
    
    private boolean isCacheable(Collection<String> usedVariables) {
        for (String varName  : usedVariables) {
            if (!globalVariables.contains(varName)) {
                if (getCacheKeyForVariable(varName)==null) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private Set<String> getCachingKeys(Collection<String> usedVariables) {
        Set<String> result = new HashSet<String>();
        for (String varName : usedVariables) {
            if (!globalVariables.contains(varName)) {
                String key = getCacheKeyForVariable(varName);
                if (key==null) {
                    return null;
                }
				result.add(key);
            }
        }
        return result;
    }

    public boolean containsZoneDependentFactor(Set<String> variables) {
    	for (String factor : zoneDependentFactors) {
    		if (variables.contains(factor))
    			return true;
    	}
    	return false;
    }
}
