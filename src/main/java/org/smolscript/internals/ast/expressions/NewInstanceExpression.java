package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;


public class NewInstanceExpression extends Expression {

    public final Token className;
    public final Expression[] ctorArgs;


    public NewInstanceExpression(Token className, Expression[] ctorArgs) {
        this.className = className;
        this.ctorArgs = ctorArgs;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}