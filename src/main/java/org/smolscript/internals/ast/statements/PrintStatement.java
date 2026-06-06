package org.smolscript.internals.ast.statements;

import org.smolscript.internals.ast.expressions.Expression;


public class PrintStatement extends Statement {

    public final Expression expression;
    public final Number keywordTokenIndex;


    public PrintStatement(Expression expression, Number keywordTokenIndex) {
        this.expression = expression;
        this.keywordTokenIndex = keywordTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}