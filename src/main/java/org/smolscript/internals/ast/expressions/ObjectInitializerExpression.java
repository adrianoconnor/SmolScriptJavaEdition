package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class ObjectInitializerExpression extends Expression {

    public final Token name;
    public final Expression value;


    public ObjectInitializerExpression(Token name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}