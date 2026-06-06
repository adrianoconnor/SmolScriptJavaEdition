package org.smolscript.internals.ast.expressions;

public abstract class Expression {

    public abstract Object accept(IExpressionVisitor visitor);
}
