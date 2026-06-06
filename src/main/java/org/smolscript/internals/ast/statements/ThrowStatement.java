package org.smolscript.internals.ast.statements;

import org.smolscript.internals.ast.expressions.Expression;


public class ThrowStatement extends Statement {

    public final Expression expression;
    public final Number keywordTokenIndex;
    public final Number expressionFirstTokenIndex;
    public final Number expressionLastTokenIndex;


    public ThrowStatement(Expression expression, Number keywordTokenIndex, Number expressionFirstTokenIndex, Number expressionLastTokenIndex) {
        this.expression = expression;
        this.keywordTokenIndex = keywordTokenIndex;
        this.expressionFirstTokenIndex = expressionFirstTokenIndex;
        this.expressionLastTokenIndex = expressionLastTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}