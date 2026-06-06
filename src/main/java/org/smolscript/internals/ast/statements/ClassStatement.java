package org.smolscript.internals.ast.statements;

import org.smolscript.internals.Token;
import org.smolscript.internals.ast.statements.FunctionStatement;


public class ClassStatement extends Statement {

    public final Token className;
    public final Token superclassName;
    public final FunctionStatement[] functions;


    public ClassStatement(Token className, Token superclassName, FunctionStatement[] functions) {
        this.className = className;
        this.superclassName = superclassName;
        this.functions = functions;
    }

    public Object accept(IStatementVisitor visitor) {
        return visitor.visit(this);
    }
}