package org.smolscript.internals.ast.statements;

import org.smolscript.internals.ast.expressions.Expression;


public class ExpressionStatement extends Statement {

    public final Expression expression;
    public final Number expressionFirstTokenIndex;
    public final Number expressionLastTokenIndex;


    public ExpressionStatement(Expression expression, Number expressionFirstTokenIndex, Number expressionLastTokenIndex) {
        this.expression = expression;
        this.expressionFirstTokenIndex = expressionFirstTokenIndex;
        this.expressionLastTokenIndex = expressionLastTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}