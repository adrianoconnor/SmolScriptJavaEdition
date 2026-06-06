package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class UnaryExpression extends Expression {

    public final Token op;
    public final Expression right;


    public UnaryExpression(Token op, Expression right) {
        this.op = op;
        this.right = right;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}