package org.smolscript.internals.ast.expressions;



public class IndexerSetExpression extends Expression {

    public final Expression targetObject;
    public final Expression indexerExpression;
    public final Expression valueExpression;


    public IndexerSetExpression(Expression targetObject, Expression indexerExpression, Expression valueExpression) {
        this.targetObject = targetObject;
        this.indexerExpression = indexerExpression;
        this.valueExpression = valueExpression;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}