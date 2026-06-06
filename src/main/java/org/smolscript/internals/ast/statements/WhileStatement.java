package org.smolscript.internals.ast.statements;

import org.smolscript.internals.ast.expressions.Expression;


public class WhileStatement extends Statement {

    public final Expression whileCondition;
    public final Statement executeStatement;
    public final Number keywordTokenIndex;
    public final Number expressionFirstTokenIndex;
    public final Number expressionLastTokenIndex;
    public final Number statementFirstTokenIndex;
    public final Number statementLastTokenIndex;


    public WhileStatement(Expression whileCondition, Statement executeStatement, Number keywordTokenIndex, Number expressionFirstTokenIndex, Number expressionLastTokenIndex, Number statementFirstTokenIndex, Number statementLastTokenIndex) {
        this.whileCondition = whileCondition;
        this.executeStatement = executeStatement;
        this.keywordTokenIndex = keywordTokenIndex;
        this.expressionFirstTokenIndex = expressionFirstTokenIndex;
        this.expressionLastTokenIndex = expressionLastTokenIndex;
        this.statementFirstTokenIndex = statementFirstTokenIndex;
        this.statementLastTokenIndex = statementLastTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}