/**
 * 
 */
package com.freshdirect.smartstore.dsl;

import java.util.ArrayList;
import java.util.List;

public class FunctionCall extends Expression {

    String name;
    /**
     * List<Expression>
     */
    List<Expression> params = new ArrayList<Expression>();

    @Override
    public int getReturnType() {
        return type;
    }

    public FunctionCall(String name) {
        this.name = name;
        this.type = RET_UNKNOWN;
    }

    public FunctionCall(String name, int type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean add(Expression exp) {
        exp.context = context;
        params.add(exp);
        return true;
    }

    @Override
    public Expression lastExpression() {
        return (params.size() > 0 ? params.get(params.size() - 1) : null);
    }

    @Override
    public void removeLastExpression() {
        if (params.size() > 0) {
            params.remove(params.size() - 1);
        }
    }

    @Override
    public String toCode() {
        final StringBuffer buf = new StringBuffer();
        buf.append(name).append('(');
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(params.get(i).toCode());
        }
        buf.append(')');
        return buf.toString();
    }

    @Override
    public String toJavaCode() throws CompileException {
        final String tempVariable = context.getJavaTempVariableId(this);
        if (tempVariable != null) {
            return tempVariable;
        } else {
            return context.getJavaCode(this, new ArrayList<Expression>(params));
        }
    }

    @Override
    public String getJavaInitializationCode() throws CompileException {
        return context.getPreparingCode(this, new ArrayList<Expression>(params));
    }

    @Override
    public void validate() throws CompileException {
        final StringBuffer paramTypes = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            final Expression exp = params.get(i);
            exp.validate();
            final int rtype = exp.getReturnType();
            if (rtype == RET_FLOAT || rtype == RET_INT || rtype == RET_STRING || rtype == RET_NODE || rtype == RET_SET) {
                paramTypes.append((char) rtype);
            } else {
                throw new CompileException(CompileException.TYPE_ERROR, "Return type of '" + exp.toCode() + "' is neither float, nor int, nor string ! ("
                        + rtype + ")");
            }
        }
        this.type = context.getFunctionReturnType(name, paramTypes.toString());
        if (this.type == RET_UNKNOWN) {
            throw new CompileException(CompileException.UNKNOWN_FUNCTION, "Not a valid function : " + name + "(" + paramTypes + ")!");
        }
    }

    @Override
    public boolean replace(Expression from, Expression to) {
        return replace(params, from, to);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression getParam(int index) {
        return params.get(index);
    }

    /**
     * The raw parameter list.
     * 
     * @return
     */
    public List<Expression> getParams() {
        return params;
    }

    @Override
    public void visit(Expression parent, ExpressionVisitor visitor) throws VisitException {
        visitor.visit(parent, this);
        for (final Expression exp : params) {
            exp.visit(this, visitor);
        }
    }

    @Override
    public String toString() {
        return "Function[" + name + ',' + params + "]";
    }

    @Override
    protected boolean equalExpression(Expression obj) {
        if (obj instanceof FunctionCall) {
            final FunctionCall f = (FunctionCall) obj;
            if (f.name.equals(name) && f.params.size() == params.size()) {
                for (int i = 0; i < params.size(); i++) {
                    final Expression e1 = params.get(i);
                    final Expression e2 = f.params.get(i);
                    if (!e1.equalExpression(e2)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() ^ hashCode(params);
    }

}