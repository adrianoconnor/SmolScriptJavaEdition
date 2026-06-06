package org.smolscript.internals.ast.statements;

import org.smolscript.internals.ast.expressions.Expression;


public class IfStatement extends Statement {

    public final Expression expression;
    public final Statement thenStatement;
    public final Statement elseStatement;
    public final Number expressionFirstTokenIndex;
    public final Number expressionLastTokenIndex;
    public final Number thenStatementFirstTokenIndex;
    public final Number thenStatementLastTokenIndex;
    public final Number elseStatementFirstTokenIndex;
    public final Number elseStatementLastTokenIndex;


    public IfStatement(Expression expression, Statement thenStatement, Statement elseStatement, Number expressionFirstTokenIndex, Number expressionLastTokenIndex, Number thenStatementFirstTokenIndex, Number thenStatementLastTokenIndex, Number elseStatementFirstTokenIndex, Number elseStatementLastTokenIndex) {
        this.expression = expression;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
        this.expressionFirstTokenIndex = expressionFirstTokenIndex;
        this.expressionLastTokenIndex = expressionLastTokenIndex;
        this.thenStatementFirstTokenIndex = thenStatementFirstTokenIndex;
        this.thenStatementLastTokenIndex = thenStatementLastTokenIndex;
        this.elseStatementFirstTokenIndex = elseStatementFirstTokenIndex;
        this.elseStatementLastTokenIndex = elseStatementLastTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}