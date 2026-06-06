package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class SetExpression extends Expression {

    public final Expression targetObject;
    public final Token attributeName;
    public final Expression valueExpression;


    public SetExpression(Expression targetObject, Token attributeName, Expression valueExpression) {
        this.targetObject = targetObject;
        this.attributeName = attributeName;
        this.valueExpression = valueExpression;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}