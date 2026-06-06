package org.smolscript.internals.ast;

import org.smolscript.internals.ast.expressions.*;
import org.smolscript.internals.ast.statements.*;

public class AstDump implements IExpressionVisitor, IStatementVisitor {

    private final String newline = String.valueOf('\n');

    public String print(Statement[] stmts)
    {
        StringBuilder sb = new StringBuilder();

        for (var stmt : stmts)
        {
            sb.append((String) stmt.accept(this)).append(newline);
        }

        return sb.toString();
    }

    @Override
    public Object visit(BinaryExpression expr)
    {
        return "(" + expr.op.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this) + ")";
    }

    @Override
    public Object visit(LogicalExpression expr)
    {
        return "(" + expr.op.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this) + ")";
    }

    @Override
    public Object visit(GroupingExpression expr)
    {
        return "(group " + expr.expression.accept(this) + ")";
    }

    @Override
    public Object visit(LiteralExpression expr)
    {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public Object visit(UnaryExpression expr)
    {
        return "(" + expr.op.lexeme + " " + expr.right.accept(this) + ")";
    }

    @Override
    public Object visit(VariableExpression expr)
    {
        return "(var " + expr.name + ")";
    }

    @Override
    public Object visit(AssignExpression expr)
    {
        return "(assign " + expr.name.lexeme + " " + expr.value.accept(this) + ")";
    }

    @Override
    public Object visit(CallExpression expr)
    {
        return "(call " + expr.callee.accept(this) + " args[" + expr.args.length + "])";
    }

    @Override
    public Object visit(VarStatement stmt)
    {
        if (stmt.initializerExpression != null)
        {
            var output = "[declare var " + stmt.name.lexeme + " with initial value]";
            output += newline;
            output += "initializer: " + stmt.initializerExpression.accept(this);
            output += newline;
            output += "[/declare var]";


            return output;
        }
        else
        {
            return "[declare var " + stmt.name.lexeme + " (undefined) /]";
        }
    }

    @Override
    public Object visit(ExpressionStatement stmt)
    {
        return "[expr " + stmt.expression.accept(this) + "]";
    }

    @Override
    public Object visit(BlockStatement stmt)
    {
        var s = new StringBuilder();

        s.append("[block begin]").append(newline);

        for (var blockStmt : stmt.statements)
        {
            s.append((String)blockStmt.accept(this)).append(newline);
        }

        s.append("[block end]").append(newline);

        return s.toString();
    }

    @Override
    public Object visit(DebuggerStatement stmt)
    {
        return "[debugger]";
    }

    @Override
    public Object visit(ReturnStatement stmt)
    {
        if (stmt.expression != null) {
            return "[return " + stmt.expression.accept(this) + "]";
        }
        else {
            return "[return (null)]";
        }
    }

    @Override
    public Object visit(BreakStatement stmt)
    {
        return "[break]";
    }

    @Override
    public Object visit(ContinueStatement stmt)
    {
        return "[continue]";
    }

    @Override
    public Object visit(IfStatement stmt)
    {
        var s = new StringBuilder();

        s.append("[if ").append(stmt.expression.accept(this)).append("]").append(newline);;

        s.append("[then]").append(newline);;
        s.append(stmt.thenStatement.accept(this));
        s.append("[/then]").append(newline);;

        if (stmt.elseStatement != null)
        {
            s.append("[else]").append(newline);;
            s.append(stmt.elseStatement.accept(this));
            s.append("[/else]").append(newline);;
        }

        s.append("[end if]").append(newline);;

        return s.toString();
    }

    @Override
    public Object visit(PrintStatement stmt) {
        return "[print " + stmt.expression.accept(this) + "]";
    }

    @Override
    public Object visit(ThrowStatement stmt)
    {
        if (stmt.expression != null)
        {
            return "[throw " + stmt.expression.accept(this) + "]";
        }
        else
        {
            return "[throw]";
        }
    }

    @Override
    public Object visit(TryStatement stmt)
    {
        var s = new StringBuilder();
        /*
        s.AppendLine($"[if {stmt.testExpression.accept(this)}]");

        s.AppendLine($"[then]");
        s.Append($"{stmt.thenStatement.accept(this)}");
        s.AppendLine($"[/then]");

        if (stmt.elseStatement != null)
        {
            s.AppendLine($"[else]");
            s.Append($"{stmt.elseStatement!.accept(this)}");
            s.AppendLine($"[/else]");
        }

        s.AppendLine("[end if]");
        */
        return s.toString();
    }

    @Override
    public Object visit(TernaryExpression expr)
    {
        var s = new StringBuilder();

        s.append("[ternary ").append(expr.evaluationExpression.accept(this)).append("]").append(newline);

        s.append("[true]").append(newline);
        s.append(expr.expresisonIfTrue.accept(this));
        s.append("[/true]").append(newline);

        s.append("[false]").append(newline);
        s.append(expr.expresisonIfFalse.accept(this));
        s.append("[/false]").append(newline);

        s.append("[end ternary]").append(newline);

        return s.toString();
    }

    @Override
    public Object visit(WhileStatement stmt)
    {
        var s = new StringBuilder();

        s.append("[while ").append(stmt.whileCondition.accept(this)).append("]").append(newline);
        s.append(stmt.executeStatement.accept(this));
        s.append("[end while]").append(newline);

        return s.toString();
    }

    @Override
    public Object visit(FunctionStatement stmt)
    {
        var s = new StringBuilder();

        s.append("[declare function " + stmt.name.lexeme + "()]").append(newline);
        s.append(stmt.functionBody.accept(this));
        s.append("[end function declaration]").append(newline);

        return s.toString();
    }

    @Override
    public Object visit(ClassStatement stmt)
    {
        var s = new StringBuilder();

        s.append("[declare class " + stmt.className.lexeme + "()]").append(newline);

        //s.Append($"{stmt.constructor?.accept(this) ?? "no ctor"}");

        for (var function : stmt.functions)
        {
            s.append(function.accept(this));
        }

        return s.toString();
    }

    @Override
    public Object visit(NewInstanceExpression expr)
    {
        return "[new instance of " + expr.className.lexeme + "()]" + newline;
    }

    @Override
    public Object visit(GetExpression expr)
    {
        return "[getter obj=" + expr.targetObject.accept(this) + ", property name=" + expr.attributeName + "]" + newline;
    }

    @Override
    public Object visit(SetExpression expr)
    {
        return "[setter obj={expr.TargetObject.accept(this)}, property name={expr.AttributeName} value={expr.Value.accept(this)}]" + newline;
    }

    @Override
    public Object visit(ObjectInitializerExpression expr)
    {
        return "[initializer property name={expr.ObjectName} value={expr.Value.accept(this)}]" + newline;
    }

    @Override
    public Object visit(IndexerGetExpression expr)
    {
        return "[indexGetter obj={expr.TargetObject.accept(this)}, indexer Expr={expr.IndexerExpression.accept(this)}]" + newline;
    }

    @Override
    public Object visit(IndexerSetExpression expr)
    {
        return "[indexSetter obj={expr.TargetObject.accept(this)}, indexer Expr={expr.IndexerExpression.accept(this)} value={expr.Value.accept(this)}]" + newline;
    }

    @Override
    public Object visit(FunctionExpression expr)
    {
        return null;
    }
}
