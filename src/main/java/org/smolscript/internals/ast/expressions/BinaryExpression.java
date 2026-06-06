package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class BinaryExpression extends Expression {

    public final Expression left;
    public final Token op;
    public final Expression right;


    public BinaryExpression(Expression left, Token op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}