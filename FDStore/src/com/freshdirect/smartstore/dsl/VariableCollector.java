package com.freshdirect.smartstore.dsl;

import java.util.HashSet;
import java.util.Set;

public class VariableCollector implements ExpressionVisitor {

    Set<String> names = new HashSet<String>();
    
    Set<String> functions = new HashSet<String>();
    
    public void visit(Expression parent, Expression expression) {
        if (expression instanceof VariableExpression) {
            names.add( ((VariableExpression)expression).name);
        }
        if (expression instanceof FunctionCall) {
            functions.add(((FunctionCall)expression).getName());
        }
    }

    public Set<String> getVariables() {
        return names;
    }
    
    public Set<String> getFunctions() {
        return functions;
    }
    
    @Override
    public String toString() {
        return "VariableCollector[" + names + ']';
    }
}
