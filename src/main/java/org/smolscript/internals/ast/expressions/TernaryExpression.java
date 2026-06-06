package org.smolscript.internals.ast.expressions;



public class TernaryExpression extends Expression {

    public final Expression evaluationExpression;
    public final Expression expresisonIfTrue;
    public final Expression expresisonIfFalse;


    public TernaryExpression(Expression evaluationExpression, Expression expresisonIfTrue, Expression expresisonIfFalse) {
        this.evaluationExpression = evaluationExpression;
        this.expresisonIfTrue = expresisonIfTrue;
        this.expresisonIfFalse = expresisonIfFalse;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}