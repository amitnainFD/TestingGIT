package com.freshdirect.smartstore.dsl;

public interface ExpressionVisitor {
    public void visit(Expression parent, Expression expression) throws VisitException;

}
