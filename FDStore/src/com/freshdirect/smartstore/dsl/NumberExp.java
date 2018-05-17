/**
 * 
 */
package com.freshdirect.smartstore.dsl;


public class NumberExp extends Expression {
    Number number;

    public NumberExp(String number) {
        if (number.indexOf('.') != -1) {
            this.type = RET_FLOAT;
            this.number = Double.valueOf(number);
        } else {
            this.type = RET_INT;
            this.number = Integer.valueOf(number);
        }
    }

    public NumberExp(double value) {
        if (Math.abs(value - Math.round(value)) < 0.00001) {
            this.type = RET_INT;
            this.number = new Integer((int) Math.round(value));
        } else {
            this.type = RET_FLOAT;
            this.number = new Double(value);
        }
    }

    public String toCode() {
        return number.toString();
    }
    
    public String toString() {
        return "Number["+number+"]";
    }
    
    public String toJavaCode() throws CompileException {
        return toCode();
    }
    
    public Number evaluateExpression() {
        return number;
    }
    
    @Override
    public String getStringValue() {
        return number.toString();
    }

    @Override
    protected boolean equalExpression(Expression obj) {
        if (obj instanceof NumberExp) {
            NumberExp n = (NumberExp) obj;
            return n.number.equals(number);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return number.hashCode();
    }
}