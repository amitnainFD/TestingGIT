package com.freshdirect.smartstore.dsl;

import java.util.HashSet;
import java.util.Set;

public class DuplicationEliminator implements ExpressionVisitor {

    Set<Expression> alreadyVisited = new HashSet<Expression>();
    
    public DuplicationEliminator() {
    }

    @Override
    public void visit(Expression parent, Expression expression) throws VisitException {
        if (alreadyVisited.contains(expression)) {
//            parent.replace(from, to)
        }

    }

}
