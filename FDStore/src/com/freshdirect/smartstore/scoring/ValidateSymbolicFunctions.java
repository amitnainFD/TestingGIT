package com.freshdirect.smartstore.scoring;

import java.util.Collection;

import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.dsl.ExpressionVisitor;
import com.freshdirect.smartstore.dsl.FunctionCall;
import com.freshdirect.smartstore.dsl.NumberExp;
import com.freshdirect.smartstore.dsl.VariableExpression;
import com.freshdirect.smartstore.dsl.VisitException;

public class ValidateSymbolicFunctions implements ExpressionVisitor {

    String     name;
    Collection symbolicVariables;

    public ValidateSymbolicFunctions(String name, Collection symbolicVariables) {
        this.name = name;
        this.symbolicVariables = symbolicVariables;
    }

    @Override
    public void visit(Expression parent, Expression expression) throws VisitException {
        if (expression instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expression;
            if (name.equals(f.getName())) {
                if (!(f.getParam(0) instanceof VariableExpression)) {
                    throw new VisitException("First parameter is not a variable :" + f.toCode());
                }
                VariableExpression var = (VariableExpression) f.getParam(0);
                if (!symbolicVariables.contains(var.getVariableName())) {
                    throw new VisitException("Variable " + var.getVariableName() + " is not a valid factor, in " + f.toCode());
                }
                Expression p1 = f.getParam(1);
                if (!(p1 instanceof NumberExp)) {
                    throw new VisitException("Second parameter is not a number:" + p1.toCode() + " in " + f.toCode());
                }

            }
        }
    }

}
