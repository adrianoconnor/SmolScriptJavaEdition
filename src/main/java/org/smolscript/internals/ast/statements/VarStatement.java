package org.smolscript.internals.ast.statements;

import org.smolscript.internals.Token;
import org.smolscript.internals.ast.expressions.Expression;


public class VarStatement extends Statement {

    public final Token name;
    public final Expression initializerExpression;
    public final Number firstTokenIndex;
    public final Number lastTokenIndex;


    public VarStatement(Token name, Expression initializerExpression, Number firstTokenIndex, Number lastTokenIndex) {
        this.name = name;
        this.initializerExpression = initializerExpression;
        this.firstTokenIndex = firstTokenIndex;
        this.lastTokenIndex = lastTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}