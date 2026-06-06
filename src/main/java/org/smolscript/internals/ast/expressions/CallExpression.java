package org.smolscript.internals.ast.expressions;



public class CallExpression extends Expression {

    public final Expression callee;
    public final Expression[] args;
    public final Boolean useObjectRef;


    public CallExpression(Expression callee, Expression[] args, Boolean useObjectRef) {
        this.callee = callee;
        this.args = args;
        this.useObjectRef = useObjectRef;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}