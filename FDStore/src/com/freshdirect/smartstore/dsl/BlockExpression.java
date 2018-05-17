/**
 * 
 */
package com.freshdirect.smartstore.dsl;

import java.util.ArrayList;
import java.util.List;

public class BlockExpression extends Expression {
    List<Expression> expressions = new ArrayList<Expression>();

    public BlockExpression() {
        this.type = Expression.RET_VOID;
    }

    @Override
    public String toCode() {
        final StringBuffer b = new StringBuffer();
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                b.append(';');
            }
            b.append(expressions.get(i).toCode());
        }
        return b.toString();
    }

    @Override
    public void validate() throws CompileException {
        for (final Expression exp : expressions) {
            exp.validate();
        }
    }

    public int size() {
        return expressions.size();
    }

    public Expression get(int i) {
        return expressions.get(i);
    }

    @Override
    public boolean add(Expression arg0) {
        expressions.add(arg0);
        return true;
    }

    @Override
    public Expression lastExpression() {
        return (expressions.size() > 0 ? expressions.get(expressions.size() - 1) : null);
    }

    @Override
    public void removeLastExpression() {
        if (expressions.size() > 0) {
            expressions.remove(expressions.size() - 1);
        }
    }

    @Override
    public boolean replace(Expression from, Expression to) {
        return replace(expressions, from, to);
    }
    
    @Override
    public void visit(Expression parent,ExpressionVisitor visitor) throws VisitException {
        visitor.visit(parent, this);
        for (final Expression exp : expressions) {
            exp.visit(this, visitor);
        }
    }

    @Override
    public String toJavaCode() throws CompileException {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                b.append(';');
            }
            b.append(expressions.get(i).toJavaCode());
        }
        return b.toString();
    }

    @Override
    public String getJavaInitializationCode() throws CompileException {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < expressions.size(); i++) {
            b.append(expressions.get(i).toJavaCode());
        }
        return b.toString();
    }

    @Override
    protected boolean equalExpression(Expression obj) {
        if (obj instanceof BlockExpression) {
            final BlockExpression f = (BlockExpression) obj;
            if (f.expressions.size() == expressions.size()) {
                for (int i = 0; i < expressions.size(); i++) {
                    final Expression e1 = expressions.get(i);
                    final Expression e2 = f.expressions.get(i);
                    if (!e1.equalExpression(e2)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}