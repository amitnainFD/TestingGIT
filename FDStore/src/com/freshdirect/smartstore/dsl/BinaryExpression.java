package com.freshdirect.smartstore.dsl;

public class BinaryExpression extends Expression {

    Expression left;
    Expression right;
    char       operator;

    public BinaryExpression(Expression left, char operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public char getOperator() {
        return operator;
    }

    @Override
    public String toCode() {
        return "(" + left.toCode() + ' ' + operator + ' ' + right.toCode() + ')';
    }

    @Override
    public String toJavaCode() throws CompileException {
        return "(" + left.toJavaCode() + ' ' + operator + ' ' + right.toJavaCode() + ')';
    }
    
    @Override
    public void validate() throws CompileException {
        left.validate();
        right.validate();
        this.type = Operation.calculateType(left.getReturnType(), operator, right.getReturnType());
    }

    @Override
    public void visit(Expression parent, ExpressionVisitor visitor) throws VisitException {
        super.visit(parent, visitor);
        left.visit(this, visitor);
        right.visit(this, visitor);
    }
    
    @Override
    public Number evaluateExpression() {
        Number lv = left.evaluateExpression();
        Number rv = right.evaluateExpression();
        switch (operator) {
            case '+' : return new Double(lv.doubleValue() + rv.doubleValue());
            case '-' : return new Double(lv.doubleValue() - rv.doubleValue());
            case '*' : return new Double(lv.doubleValue() * rv.doubleValue());
            case '/' : return new Double(lv.doubleValue() / rv.doubleValue());
        }
        return super.evaluateExpression();
    }
    
    @Override
    public String getStringValue() {
        String s1 = left.getStringValue();
        String s2 = right.getStringValue();
        if (operator == '+') {
            return s1 + s2;
        }
        throw new RuntimeException("Operator " + operator + " not supported in string manipulations, between : '" + s1 + "' and '" + s2 + "' !");
    }
    
    @Override
    public boolean replace(Expression from,Expression to) {
        boolean result = false;
        if (right == from) {
            right = to;
            result = true;
        }
        if (left == from) {
            left = to;
            result = true;
        }
        return result;
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Binary[").append(left).append(',').append(operator).append(',').append(right).append("]");
        return b.toString();
    }
    
    @Override
    public String getJavaInitializationCode() throws CompileException {
        return right.getJavaInitializationCode() + left.getJavaInitializationCode();
    }
    
    @Override
    protected boolean equalExpression(Expression obj) {
        if (obj instanceof BinaryExpression) {
            BinaryExpression b = (BinaryExpression) obj;
            if (b.operator == operator) {
                if (b.left.equalExpression(left) && b.right.equalExpression(right)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode() ^ operator;
    }

    
}
