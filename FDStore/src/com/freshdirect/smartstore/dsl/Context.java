package com.freshdirect.smartstore.dsl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class encapsulates the compiling context, it contains the available
 * variables, and function definitions.
 * 
 * @author zsombor
 * 
 */
public class Context {
    Map<String, Integer> variableTypes    = new HashMap<String, Integer>();
    Map variableValues    = new HashMap();
    Map<String, FunctionDef> functionDefs = new HashMap<String, FunctionDef>();
    Map<String, String> javaVariableIds    = new HashMap<String, String>();
    Map<Expression, String> javaTempVariableIds = new HashMap<Expression, String>();

    Map<Object, String> generatedVariables = new HashMap<Object, String>();
    
    int lastTempVariable = 0;

    public int getFunctionReturnType(String name, String paramTypes) throws CompileException {
        FunctionDef def = functionDefs.get(name);
        if (def != null) {
            return def.getReturnType(name, paramTypes);
        }
        return Expression.RET_UNKNOWN;
    }
    
    public boolean isFunction(String name) {
        return functionDefs.get(name)!=null;
    }

    public String getJavaCode(FunctionCall call, List<Expression> params) throws CompileException {
        FunctionDef def = functionDefs.get(call.getName());
        if (def != null) {
            return def.toJavaCode(call, params);
        }
        throw new CompileException(CompileException.UNKNOWN_FUNCTION, "Function '" + call.getName() + "' not declared!");
    }

    public String getPreparingCode(FunctionCall call, List<Expression> params) throws CompileException {
        FunctionDef def = functionDefs.get(call.getName());
        if (def != null) {
            return def.getPreparingCode(call, params);
        }
        throw new CompileException(CompileException.UNKNOWN_FUNCTION, "Function '" + call.getName()+ "' not declared!");
    }
    
    public void addFunctionDef(String name, FunctionDef def) {
        functionDefs.put(name, def);
    }

    public boolean isVariable(String name) {
        return variableTypes.get(name) != null;
    }

    public int getVariableType(String name) {
        Integer type = (Integer) variableTypes.get(name);
        if (type != null) {
            return type.intValue();
        }
        return Expression.RET_UNKNOWN;
    }

    public void addVariable(String name, int type) {
        this.variableTypes.put(name, new Integer(type));
    }

    public String formatVariable(String name) {
        return "$" + name;
    }
    
    public void setVariableValue(String name, Object value) {
        this.variableValues.put(name, value);
    }

    public Object getVariableValue(String name) {
        return this.variableValues.get(name);
    }
    
    
    public static class FunctionDef {
        int minParams;
        int maxParams;
        int retType;

        public FunctionDef() {
            this(-1);
        }

        public FunctionDef(int params) {
            this.maxParams = params;
            this.minParams = params;
        }

        public FunctionDef(int min, int max) {
            this.maxParams = max;
            this.minParams = min;
        }

        public FunctionDef(int min, int max, int retType) {
            this.maxParams = max;
            this.minParams = min;
            this.retType = retType;
        }

        public FunctionDef setReturnType(int retType) {
            this.retType = retType;
            return this;
        }

        protected final void validateParamCount(String name, String paramTypes) throws CompileException {
            int paramCount = paramTypes.length();
            if (minParams != -1 && paramCount < minParams) {
                throw new CompileException(CompileException.PARAMETER_ERROR, "Function '" + name + "' expect at least " + minParams
                        + " parameters, but found only " + paramCount);
            }
            if (maxParams != -1 && paramCount > maxParams) {
                throw new CompileException(CompileException.PARAMETER_ERROR, "Function '" + name + "' expect at most " + maxParams + " parameters, but found "
                        + paramCount);
            }
        }
        
        protected void validate(String name, String paramTypes) throws CompileException {
            validateParamCount(name, paramTypes);
        }

        public int getReturnType(String name, String paramTypes) throws CompileException {
            validate(name, paramTypes);
            return retType;
        }

        public String toJavaCode(FunctionCall call, List<Expression> parameters) throws CompileException {
            return toJavaCode(call.getName(), parameters);
        }
        
        public String toJavaCode(String name, List<Expression> parameters) throws CompileException {
            StringBuffer buf = new StringBuffer();
            buf.append(name);
            buf.append('(');
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(((Expression) parameters.get(i)).toJavaCode());
            }
            buf.append(')');
            return buf.toString();
        }
        

        
        public String getPreparingCode(FunctionCall call, List<Expression> parameters) throws CompileException {
            return "";
        }
    }

    public static class MultiReturnTypeFunctionDef extends FunctionDef {

        public MultiReturnTypeFunctionDef() {
            super();
        }

        public MultiReturnTypeFunctionDef(int min, int max) {
            super(min, max);
        }

        public MultiReturnTypeFunctionDef(int params) {
            super(params);
        }

        @Override
        public int getReturnType(String name, String paramTypes) throws CompileException {
            validateParamCount(name, paramTypes);
            if (paramTypes.indexOf(Expression.RET_FLOAT) != -1) {
                return Expression.RET_FLOAT;
            }
            return Expression.RET_INT;
        }

    }

    public Set<String> getVariables() {
        return variableTypes.keySet();
    }

    public String getJavaVariableId(String variableName) {
        if (javaVariableIds.containsKey(variableName)) {
            return javaVariableIds.get(variableName);
        } else {
            return variableName;
        }
    }

    public String putJavaVariableId(String variableName, String variableId) {
        return javaVariableIds.put(variableName, variableId);
    }

    public String getJavaTempVariableId(Expression expression) {
        return javaTempVariableIds.get(expression);
    }

    public String putJavaTempVariableId(Expression expression, String variableId) {
        return javaTempVariableIds.put(expression, variableId);
    }

    public String createTempVariable(Object key) {
        String var = generatedVariables.get(key);
        if (var == null) {
            var = "var" + (lastTempVariable++);
            generatedVariables.put(key, var);
        }
        return var;
    }
    
    public void cleanupTemporary() {
        javaTempVariableIds.clear();
        javaVariableIds.clear();
    }

}
