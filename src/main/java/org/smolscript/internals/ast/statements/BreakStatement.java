package org.smolscript.internals.ast.statements;



public class BreakStatement extends Statement {

    public final Number keywordTokenIndex;


    public BreakStatement(Number keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}