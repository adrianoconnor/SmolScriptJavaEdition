package org.smolscript.internals.ast.statements;

import org.smolscript.internals.Token;
import org.smolscript.internals.ast.statements.BlockStatement;


public class FunctionStatement extends Statement {

    public final Token name;
    public final Token[] parameters;
    public final BlockStatement functionBody;


    public FunctionStatement(Token name, Token[] parameters, BlockStatement functionBody) {
        this.name = name;
        this.parameters = parameters;
        this.functionBody = functionBody;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}