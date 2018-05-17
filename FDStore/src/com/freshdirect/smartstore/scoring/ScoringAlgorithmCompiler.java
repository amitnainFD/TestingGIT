package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import com.freshdirect.smartstore.dsl.BlockExpression;
import com.freshdirect.smartstore.dsl.ClassCompileException;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.dsl.Context;
import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.dsl.Operation;
import com.freshdirect.smartstore.dsl.Parser;
import com.freshdirect.smartstore.dsl.VariableCollector;
import com.freshdirect.smartstore.dsl.VariableExpression;
import com.freshdirect.smartstore.dsl.VisitException;

/**
 * This class creates ScoringAlgorithm implementations, based on the custom expression language.
 * It has two separate mode, quirks and strict. In strict mode it is allowed to specify:
 * <p>
 *  <i>Scoring Algorithm</i> := <i>Expression</i>[;<i>Expression</i>[;<i>Expression...]]</i>
 * <br/>
 *  <i>Expression := Variable|Function|(Number * Variable) + (Number * Variable) +... </i>
 * <br/>
 *  <i>Function := 'functionName'(ParameterList)</i>
 * <br/>
 *  <i>ParameterList := Parameter[,Parameter[,Parameter...]]</i>
 * <br/>
 *  <i>Parameter := Number|Variable</i> 
 * </p>
 * 
 * Where 'functionName' can be 'between', 'atLeast', and 'atMost'.
 * 
 * <table>
 *  <tr>
 *   <td>functionName</td>
 *   <td>parameters</td>
 *   <td>explanation</td>
 *  </tr>
 *  <tr>
 *   <td>between</td>
 *   <td>(variable, min, max)</td>
 *   <td>returns 1, when min<=variable<=max, 0 otherwise</td>
 *  </tr>
 *  <tr>
 *   <td>atLeast</td>
 *   <td>(variable, max)</td>
 *   <td>returns 1, when variable<=max, 0 otherwise</td>
 *  </tr>
 *  <tr>
 *   <td>atMost</td>
 *   <td>(variable, min)</td>
 *   <td>returns 1, when variable>=min, 0 otherwise</td>
 *  </tr>
 * 
 * In quirks mode, the expressions are not limited.
 * 
 * 
 * @author zsombor
 * 
 */
public class ScoringAlgorithmCompiler extends CompilerBase {

    static class TopLimit {
        final int maximum;
        final int position;
        
        public TopLimit(int maximum, int position) {
            this.maximum = maximum;
            this.position = position;
        }
        
        
        
    }
    
    public ScoringAlgorithmCompiler() {
    }

    public ScoringAlgorithmCompiler(CompilerBase cb) {
        super(cb);
    }

    protected  void setupParser(Parser parser) {
        super.setupParser(parser);
        parser.getContext().addVariable("top", Expression.RET_SYMBOL);
        for (int i = 1; i <= 5; i++) {
            parser.getContext().addVariable("top" + i, Expression.RET_SYMBOL);
        }
    }

    /**
     * Validates the type safetyness, and returns a list of variables.
     * 
     * @param expr
     * @return
     * @throws CompileException
     */
    protected Set<String> validate(BlockExpression expr) throws CompileException {
        expr.validate();
        VariableCollector collector = new VariableCollector();
        try {
            expr.visit(null, collector);
            Context context = getParser().getContext();
            Set<String> result = new HashSet<String>();
            for (String varName : collector.getVariables()) {
                if (!context.isVariable(varName)) {
                    throw new ClassCompileException("Unknown variable '" + varName + ", available variables:" + context.getVariables());
                }
                if (context.getVariableType(varName)!=Expression.RET_SYMBOL) {
                    result.add(varName);
                }
            }
            return result;
        } catch (VisitException e) {
            throw new ClassCompileException("VisitException :" + e.getMessage(), e);
        }
    }

    public Class compileAlgorithm(String name, BlockExpression expr, String toStringValue) throws CompileException {

        Set<String> variables = validate(expr);

        try {
            CtClass class1 = pool.makeClass(packageName + name);
            CtClass parent;
            parent = pool.get(ScoringAlgorithm.class.getName());
            class1.setSuperclass(parent);

            List<TopLimit> topParam = removeSymbolicOperations(expr);
            if (topParam.size()>0) {
                StringBuilder s = new StringBuilder("public com.freshdirect.smartstore.scoring.OrderingFunction createOrderingFunction() { \n\t com.freshdirect.smartstore.scoring.TopNLimitFunction t = new com.freshdirect.smartstore.scoring.TopNLimitFunction();\n");
                for (TopLimit t : topParam) {
                    s.append("\t t.addTopN(").append(t.position).append(",").append(t.maximum).append(");\n");
                }
                s.append(" return t;\n }");
                CtMethod method5 = CtNewMethod.make(s.toString(), class1);
                class1.addMethod(method5);
            }

            CtClass stringArray = pool.get("java.lang.String[]");
            
            pool.importPackage("com.freshdirect.smartstore.scoring");

            CtField field = new CtField(stringArray, "variables", class1);
            field.setModifiers(Modifier.STATIC);
            class1.addField(field, generateFieldInitializers(expr, variables));
            
            CtMethod method = CtNewMethod.make(generateGetScoresMethodBody(expr, variables), class1);
            class1.addMethod(method);

            CtMethod method2 = CtNewMethod.make(generateGetVariablesMethodBody(variables), class1);
            class1.addMethod(method2);
            
            CtMethod method3 = CtNewMethod.make(generateNewGetScoresMethodBody(expr, variables), class1);
            class1.addMethod(method3);

            CtMethod method4 = CtNewMethod.make("public int getReturnSize() { return "+expr.size()+"; } ", class1);
            class1.addMethod(method4);
            
            CtMethod method7 = CtNewMethod.make(generateGetExpressionsMethodBody(expr), class1);
            class1.addMethod(method7);
            
            method = createToStringMethod(class1, toStringValue);
            class1.addMethod(method);
            

            return class1.toClass();
        } catch (NotFoundException e) {
            throw new ClassCompileException("NotFound:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new ClassCompileException("CannotCompile:" + e.getMessage(), e);
        }
    }

    private List<TopLimit> removeSymbolicOperations(BlockExpression expr) {
        List<TopLimit> limits = new ArrayList<TopLimit>();
        for (int i=0;i<expr.size();i++) {
            if (expr.get(i) instanceof Operation) {
                int top = filterOperation((Operation) expr.get(i));
                if (top>=1) {
                    limits.add(new TopLimit(top,i));
                }
            }
        }
        return limits;
    }

    private int filterOperation(Operation operation) {
        int paramCount = operation.size();
        Expression last = operation.get(paramCount-1);
        if (last.getReturnType()==Expression.RET_SYMBOL && last instanceof VariableExpression) {
            int top = 1;
            String name = ((VariableExpression)last).getVariableName();
            if (name.startsWith("top") && name.length() > 3) {
                top = Integer.parseInt(name.substring(3));
            }
            operation.removeOperator(paramCount-1);
            return top;
        }
        return -1;
    }

    private String generateFieldInitializers(BlockExpression expr, Set<String> variables) {
        if (variables.isEmpty()) {
            return " variables = new String[0];\n";
        }
        StringBuffer buf = new StringBuffer();
        buf.append("  variables =  new String[] {");
        
        boolean first = true;
        for (String name : variables) {
            if (getParser().getContext().getVariableType(name)!=Expression.RET_SYMBOL) {
                if (first) {
                    first = false;
                } else {
                    buf.append(',');
                }
                buf.append('"').append(name).append('"');
            }
        }
        buf.append("};");
        return buf.toString();
    }

    private String generateGetScoresMethodBody(BlockExpression expr, Set<String> variables) throws CompileException {
        StringBuffer buf = new StringBuffer();
        buf.append("public Score getScores(java.util.Map variables) {\n");
        buf.append("  Score sc = new Score(").append(expr.size()).append(");\n");
        Context context = getParser().getContext();
        for (String name : variables) {
            buf.append(declareVariable(context, name));
        }
        for (int i = 0; i < expr.size(); i++) {
            String statement = "  sc.set(" + i + "," + expr.get(i).toJavaCode() + ");\n";
            buf.append(statement);
        }
        buf.append("  return sc;\n");
        buf.append(" }");
        return buf.toString();
    }
    
    private String generateNewGetScoresMethodBody(BlockExpression expr, Set<String> variables) throws CompileException {
        StringBuffer buf = new StringBuffer();
        buf.append("public double[] getScores(double[] factors) {\n");
        buf.append("  double[] result = new double[").append(expr.size()).append("];\n");
        Context context = getParser().getContext();
        int j = 0;
        for (String name : variables) {
            buf.append(declareVariable(context, name, "factors[" + j + ']'));
            j++;
        }
        for (int i = 0; i < expr.size(); i++) {
            String statement = "  result[" + i + "] = " + expr.get(i).toJavaCode() + ";\n";
            buf.append(statement);
        }
        buf.append("  return result;\n");
        buf.append(" }");
        return buf.toString();
    }

    private String generateGetVariablesMethodBody(Set variables) {
        return "public String[] getVariableNames() {\n"+
        "  return variables; \n}";
    }
    
    private String generateGetExpressionsMethodBody(BlockExpression xp) {
        String s = "public String[] getExpressions() {\n";
        int size = xp.size();
        if (size == 0)
        	return s + "  return new String[0];\n}";
        s += "  String[] result = new String[] { \n";
        for (int i=0;i<size;i++) {
            if (i>0) {
                s += ",";
            }
            s += "  \"" + xp.get(i).toCode() + "\"\n";
        }
        return s + "  };\n  return result;\n}";
    }

    public ScoringAlgorithm createScoringAlgorithm(String name, String expression) throws CompileException {
        Class generated = generateClass(name, expression);
        try {
            return (ScoringAlgorithm) generated.newInstance();
        } catch (InstantiationException e) {
            throw new ClassCompileException("InstantiationException:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ClassCompileException("IllegalAccessException:" + e.getMessage(), e);
        }
    }

}
