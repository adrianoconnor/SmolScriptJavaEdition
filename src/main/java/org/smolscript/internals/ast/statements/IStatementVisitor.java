package org.smolscript.internals.ast.statements;

public interface IStatementVisitor {
    Object visit(BlockStatement statement);
    Object visit(BreakStatement statement);
    Object visit(ClassStatement statement);
    Object visit(ContinueStatement statement);
    Object visit(DebuggerStatement statement);
    Object visit(ExpressionStatement statement);
    Object visit(FunctionStatement statement);
    Object visit(IfStatement statement);
    Object visit(PrintStatement statement);
    Object visit(ReturnStatement statement);
    Object visit(ThrowStatement statement);
    Object visit(TryStatement statement);
    Object visit(VarStatement statement);
    Object visit(WhileStatement statement);
}
