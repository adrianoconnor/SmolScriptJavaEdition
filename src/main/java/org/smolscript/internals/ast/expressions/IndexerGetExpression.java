package org.smolscript.internals.ast.expressions;



public class IndexerGetExpression extends Expression {

    public final Expression targetObject;
    public final Expression indexerExpression;


    public IndexerGetExpression(Expression targetObject, Expression indexerExpression) {
        this.targetObject = targetObject;
        this.indexerExpression = indexerExpression;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}