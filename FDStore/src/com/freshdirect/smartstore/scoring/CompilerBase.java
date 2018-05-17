package com.freshdirect.smartstore.scoring;

import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import com.freshdirect.smartstore.dsl.BlockExpression;
import com.freshdirect.smartstore.dsl.ClassCompileException;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.dsl.Context;
import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.dsl.Parser;

/**
 * Abstract base class which contains common code for various compiler implementations.
 * 
 * @author zsombor
 *
 */
public abstract class CompilerBase {

    protected ClassPool pool;

    protected String    packageName = "com.freshdirect.smartstore.scoring.tmp.";

    private Parser      parser;

    public CompilerBase() {
        ClassPool parent = ClassPool.getDefault();
        this.pool = new ClassPool(parent);
        this.pool.insertClassPath(new ClassClassPath(CompilerBase.class));
    }
    
    public CompilerBase(CompilerBase cb) {
        this.pool = cb.pool;
    }

    protected  void setupParser(Parser parser) {
        parser.getContext().addFunctionDef("between", new Context.FunctionDef(3, 3, Expression.RET_INT) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                boolean hasDouble = false;
                // check that every parameter is int ?
                for (int i = 0; i < parameters.size(); i++) {
                    hasDouble |= (parameters.get(i)).getReturnType() != Expression.RET_INT;
                }

                StringBuffer buf = new StringBuffer();
                buf.append("HelperFunctions.");
                buf.append(name);
                buf.append('(');
                for (int i = 0; i < parameters.size(); i++) {
                    if (i > 0) {
                        buf.append(',');
                    }
                    Expression expression = parameters.get(i);
                    String statement = expression.toJavaCode();
                    // we have to cast to double, if there is one double
                    // parameter
                    if (hasDouble && expression.getReturnType() == Expression.RET_INT) {
                        statement = "(double) (" + statement + ")";
                    }
                    buf.append(statement);
                }
                buf.append(')');
                return buf.toString();

            }
        });
        parser.getContext().addFunctionDef("atLeast", new Context.FunctionDef(2, 2, Expression.RET_INT) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression p0 = parameters.get(0);
                Expression p1 = parameters.get(1);
                return "(" + p0.toJavaCode() + " >= " + p1.toJavaCode() + " ? 1 : 0)";
            }
        });
        parser.getContext().addFunctionDef("atMost", new Context.FunctionDef(2, 2, Expression.RET_INT) {
            public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
                Expression p0 = parameters.get(0);
                Expression p1 = parameters.get(1);
                return "(" + p0.toJavaCode() + " <= " + p1.toJavaCode() + " ? 1 : 0)";
            }
        });
    }

    protected Class<?> compileAlgorithm(String name, BlockExpression ast, String toStringMethod) throws CompileException {
        throw new CompileException("Not implemented!");
    }
    
    public Parser getParser() {
        if (parser == null) {
            parser = new Parser();
            setupParser(parser);
        }
        return parser;
    }
    
    /**
     * Add new variable declaration
     * @param name
     * @param type
     */
    public void addVariable(String name, int type) {
        getParser().getContext().addVariable(name, type);
    }
    
    protected String declareVariable(Context context, String name) throws ClassCompileException {
        int type = context.getVariableType(name);
        switch (type) {
            case Expression.RET_FLOAT:
                return "  double " + name + " = ((Number) variables.get(\"" + name + "\")).doubleValue();\n";
            case Expression.RET_INT:
                return "  int " + name + " = ((Number) variables.get(\"" + name + "\")).intValue();\n";
            default:
                throw new ClassCompileException("Unknown variable type for " + name + " -> " + type);
        }
    }

    protected String declareVariable(Context context, String name, String doubleExpression) throws ClassCompileException {
        int type = context.getVariableType(name);
        switch (type) {
            case Expression.RET_FLOAT:
                return "  double " + name + " = "+doubleExpression+ ";\n";
            case Expression.RET_INT:
                return "  int " + name + " = (int) "+ doubleExpression + ";\n";
            default:
                throw new ClassCompileException("Unknown variable type for " + name + " -> " + type);
        }
    }

    protected CtMethod createToStringMethod(CtClass class1, String toStringValue) throws CannotCompileException {
        return createReturningStringMethod(class1, "toString", toStringValue);
    }

    protected CtMethod createReturningStringMethod(CtClass class1, String methodName, String toStringValue) throws CannotCompileException {
        return CtNewMethod.make("public String "+methodName+"() { \n" +
                "  return \""+toStringValue.replace('"', '\'').replaceAll("\n", "\\\\n")+"\";\n" +
                                "}", class1);
    }
    
    public synchronized Class<?> generateClass(String name, String expression) throws CompileException {
        BlockExpression ast = parse(expression);
        return compileAlgorithm(name, ast, expression);
    }

    protected BlockExpression parse(String expression) throws CompileException {
        return getParser().parse(expression);
    }
}
