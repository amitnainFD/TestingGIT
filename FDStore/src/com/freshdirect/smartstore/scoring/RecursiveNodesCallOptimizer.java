package com.freshdirect.smartstore.scoring;

import com.freshdirect.smartstore.dsl.Expression;
import com.freshdirect.smartstore.dsl.ExpressionVisitor;
import com.freshdirect.smartstore.dsl.FunctionCall;
import com.freshdirect.smartstore.dsl.Operation;
import com.freshdirect.smartstore.dsl.VisitException;

public class RecursiveNodesCallOptimizer implements ExpressionVisitor {

    boolean optimizationOccured = false;
    public RecursiveNodesCallOptimizer() {
    }

    @Override
    public void visit(Expression parent, Expression expression) throws VisitException {
        if (expression instanceof Operation) {
            Operation oper = (Operation) expression;
            Expression statement = oper.get(0);
            if (statement instanceof FunctionCall) {
                if (DataGeneratorCompiler.FN_RECURSIVE_NODES.equals(((FunctionCall) statement).getName())
                        || DataGeneratorCompiler.FN_RECURSIVE_NODES_EXCEPT.equals(((FunctionCall) statement).getName())) {
                    boolean ok = true;
                    do {
                        char operator = oper.getOperator(0);
                        if (operator == '-') {
                            FunctionCall exceptCall = convertRecursiveNodesExceptFunction((FunctionCall) statement);
                            mergeMinusIntoExceptCall(exceptCall, oper.get(1));
                            oper.removeOperator(1);
                            ok = oper.size()>1;
                            optimizationOccured = true;
                            
//                        } else if (operator == '+') {
                            // it's incorrect, unfortunately, the order of - and + are relevant
//                            FunctionCall exceptCall = convertRecursiveNodesExceptFunction((FunctionCall) statement);
//                            mergePlusIntoExceptCall(exceptCall, oper.get(1));
//                            oper.removeOperator(1);
//                            ok = oper.size()>1;
                        } else {
                            ok = false;
                        }
                    } while(ok);
                }
            }
        }
    }

    private void mergeMinusIntoExceptCall(FunctionCall exceptCall, Expression expression) {
        if (expression instanceof FunctionCall && DataGeneratorCompiler.FN_TO_LIST.equals(((FunctionCall) expression).getName())) {
            FunctionCall fc = (FunctionCall) expression;
            for (int i=0;i<fc.getParams().size();i++) {
                exceptCall.add(fc.getParam(i));
            }
        } else {
            exceptCall.add(expression);
        }
    }

    private void mergePlusIntoExceptCall(FunctionCall exceptCall, Expression expression) {
        exceptCall.add(expression);
    }
    
    
    private FunctionCall convertRecursiveNodesExceptFunction(FunctionCall statement) {
        if (DataGeneratorCompiler.FN_RECURSIVE_NODES.equals(statement.getName())) {
            statement.setName(DataGeneratorCompiler.FN_RECURSIVE_NODES_EXCEPT);
            if (statement.getParams().size() > 1) {
                // convert RecursiveNodes(a,b,c) to
                // RecursiveNodesExcept(toList(a,b,c))
                FunctionCall call = new FunctionCall(DataGeneratorCompiler.FN_TO_LIST);
                call.getParams().addAll(statement.getParams());
                statement.getParams().clear();
                statement.add(call);
            }
        } else if (DataGeneratorCompiler.FN_RECURSIVE_NODES_EXCEPT.equals(statement.getName())) {

        }
        return statement;
    }

    public boolean isOptimizationOccured() {
        return optimizationOccured;
    }
}
