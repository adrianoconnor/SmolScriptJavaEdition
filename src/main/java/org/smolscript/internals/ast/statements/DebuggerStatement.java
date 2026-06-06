package org.smolscript.internals.ast.statements;



public class DebuggerStatement extends Statement {

    public final Number keywordTokenIndex;


    public DebuggerStatement(Number keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}