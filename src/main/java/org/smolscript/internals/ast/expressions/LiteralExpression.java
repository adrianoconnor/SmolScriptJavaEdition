package org.smolscript.internals.ast.expressions;



public class LiteralExpression extends Expression {

    public final Object value;


    public LiteralExpression(Object value) {
        this.value = value;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}