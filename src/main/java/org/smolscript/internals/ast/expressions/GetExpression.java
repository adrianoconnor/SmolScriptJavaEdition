package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class GetExpression extends Expression {

    public final Expression targetObject;
    public final Token attributeName;


    public GetExpression(Expression targetObject, Token attributeName) {
        this.targetObject = targetObject;
        this.attributeName = attributeName;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}