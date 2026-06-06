package org.smolscript.internals.ast.expressions;

import org.smolscript.internals.Token;
import org.smolscript.internals.TokenType;


public class VariableExpression extends Expression {

    public final Token name;
    public final TokenType unaryOperator;


    public VariableExpression(Token name, TokenType unaryOperator) {
        this.name = name;
        this.unaryOperator = unaryOperator;
    }

    public Object accept(IExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}