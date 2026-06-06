package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;
import org.smolscript.internals.ast.statements.BlockStatement;


public class FunctionExpression extends Expression {

    public final Token[] parameters;
    public final BlockStatement functionBody;


    public FunctionExpression(Token[] parameters, BlockStatement functionBody) {
        this.parameters = parameters;
        this.functionBody = functionBody;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}