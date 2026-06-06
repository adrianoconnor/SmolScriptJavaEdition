package org.smolscript.internals.ast.statements;



public class ContinueStatement extends Statement {

    public final Number keywordTokenIndex;


    public ContinueStatement(Number keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}