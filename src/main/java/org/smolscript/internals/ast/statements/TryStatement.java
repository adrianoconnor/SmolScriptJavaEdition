package org.smolscript.internals.ast.statements;

import org.smolscript.internals.Token;
import org.smolscript.internals.ast.statements.BlockStatement;


public class TryStatement extends Statement {

    public final BlockStatement tryBody;
    public final Token exceptionVariableName;
    public final BlockStatement catchBody;
    public final BlockStatement finallyBody;


    public TryStatement(BlockStatement tryBody, Token exceptionVariableName, BlockStatement catchBody, BlockStatement finallyBody) {
        this.tryBody = tryBody;
        this.exceptionVariableName = exceptionVariableName;
        this.catchBody = catchBody;
        this.finallyBody = finallyBody;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}