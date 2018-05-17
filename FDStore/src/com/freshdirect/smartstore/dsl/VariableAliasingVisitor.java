package com.freshdirect.smartstore.dsl;

public class VariableAliasingVisitor implements ExpressionVisitor {
	String from;
	String to;

	public VariableAliasingVisitor(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}

	public void visit(Expression parent, Expression expression) {
		if (expression instanceof VariableExpression) {
			VariableExpression variableExpression = (VariableExpression) expression;
			if (variableExpression.name.equals(from))
				variableExpression.name = to;
		}
	}
}
