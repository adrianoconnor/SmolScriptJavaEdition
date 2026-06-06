package org.smolscript.internals.ast.statements;

public abstract class Statement {
    public abstract Object accept(IStatementVisitor visitor);
}
