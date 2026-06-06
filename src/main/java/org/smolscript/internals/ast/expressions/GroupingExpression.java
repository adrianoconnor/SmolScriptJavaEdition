package org.smolscript.internals.ast.expressions;



public class GroupingExpression extends Expression {

    public final Expression expression;


    public GroupingExpression(Expression expression) {
        this.expression = expression;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}