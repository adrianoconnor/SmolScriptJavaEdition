package org.smolscript.internals.ast.expressions;

public interface IExpressionVisitor {
    Object visit(AssignExpression expression);
    Object visit(BinaryExpression expression);
    Object visit(CallExpression expression);
    Object visit(FunctionExpression expression);
    Object visit(GetExpression expression);
    Object visit(GroupingExpression expression);
    Object visit(IndexerGetExpression expression);
    Object visit(IndexerSetExpression expression);
    Object visit(LiteralExpression expression);
    Object visit(LogicalExpression expression);
    Object visit(NewInstanceExpression expression);
    Object visit(ObjectInitializerExpression expression);
    Object visit(SetExpression expression);
    Object visit(TernaryExpression expression);
    Object visit(UnaryExpression expression);
    Object visit(VariableExpression expression);
}
