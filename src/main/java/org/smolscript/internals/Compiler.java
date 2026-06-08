package org.smolscript.internals;

import org.smolscript.internals.ast.expressions.*;
import org.smolscript.internals.ast.statements.*;
import org.smolscript.internals.variableTypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Compiler implements IStatementVisitor, IExpressionVisitor {

    private int _nextLabel = 1;

    private int reserveLabelId()
    {
        return _nextLabel++;
    }

    private List<SmolFunction> _functionTable = new ArrayList<SmolFunction>();
    private List<Chunk> _functionBodies = new ArrayList<Chunk>();

    private List<SmolVariableType> _constants = new ArrayList<>(List.of(
            new SmolUndefined()
    ));

    private int constantIndexForValue(Object constantLiteralValue) {

        var constantAsSmolType = SmolVariableType.create(constantLiteralValue);

        for (var i = 0; i < _constants.size(); i++) {
            var c = _constants.get(i);

            //if (c instanceof SmolVariableType x) {
                // TODO: ?? if (x.GetValue().Equals(constantAsSmolType.GetValue())) {
                if (c.GetValue() == constantAsSmolType.GetValue()) {
                    return i;
                }
            //}
        };

        _constants.add(constantAsSmolType);

        return _constants.size() - 1;
    }

    private int constantIndexForUndefined() {
        return 0;
    }


    public static SmolProgram Compile(String source) throws Exception {
        var compiler = new Compiler();

        return compiler._Compile(source);
    }

    private SmolProgram _Compile(String source) throws Exception {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens.toArray(new Token[0])); // TODO: Decide whether to just use List<> everywhere...
        var statements = parser.parse();

        // Creating the main chunk will populate the constants and build the function bodies too

        var mainChunk = new Chunk();
        mainChunk.appendInstruction(OpCode.PRG_START);

        for (var stmt : statements)
        {
            var stmtChunk = new Chunk();
            stmtChunk.appendChunk((Chunk) stmt.accept(this));

            stmtChunk.instructions.get(0).isStatementStartpoint = true;

            mainChunk.appendChunk(stmtChunk);
        }

        mainChunk.appendInstruction(OpCode.EOF);

        mainChunk.getLast().isStatementStartpoint = true;

        List<Chunk> codeSections = new ArrayList<>();

        codeSections.add(mainChunk);
        codeSections.addAll(_functionBodies);

        var prog = new SmolProgram(_constants, codeSections, _functionTable, tokens, source);

        prog.buildJumpTable();

        return prog;
    }

    @Override
    public Chunk visit(BinaryExpression expr) {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.left.accept(this));
        chunk.appendChunk((Chunk) expr.right.accept(this));

        switch (expr.op.type)
        {
            case MINUS:
                chunk.appendInstruction(OpCode.SUB);
                break;

            case DIVIDE:
                chunk.appendInstruction(OpCode.DIV);
                break;

            case MULTIPLY:
                chunk.appendInstruction(OpCode.MUL);
                break;

            case PLUS:
                chunk.appendInstruction(OpCode.ADD);
                break;

            case POW:
                chunk.appendInstruction(OpCode.POW);
                break;

            case REMAINDER:
                chunk.appendInstruction(OpCode.REM);
                break;

            case EQUAL_EQUAL:
                chunk.appendInstruction(OpCode.EQL);
                break;

            case NOT_EQUAL:
                chunk.appendInstruction(OpCode.NEQ);
                break;

            case GREATER:
                chunk.appendInstruction(OpCode.GT);
                break;

            case GREATER_EQUAL:
                chunk.appendInstruction(OpCode.GTE);
                break;

            case LESS:
                chunk.appendInstruction(OpCode.LT);
                break;

            case LESS_EQUAL:
                chunk.appendInstruction(OpCode.LTE);
                break;

            case BITWISE_AND:
                chunk.appendInstruction(OpCode.BITWISE_AND);
                break;

            case BITWISE_OR:
                chunk.appendInstruction(OpCode.BITWISE_OR);
                break;

            //default:
                // TODO: Anything needed here ??? Probably not...
        }

        return chunk;
    }

    @Override
    public Chunk visit(LogicalExpression expr)
    {
        var chunk = new Chunk();

        var shortcutLabel = reserveLabelId();
        var testCompleteLabel = reserveLabelId();

        switch (expr.op.type)
        {
            case LOGICAL_AND:

                chunk.appendChunk((Chunk) expr.left.accept(this));
                chunk.appendInstruction(OpCode.JMPFALSE, shortcutLabel);
                chunk.appendChunk((Chunk) expr.right.accept(this));
                chunk.appendInstruction(OpCode.JMP, testCompleteLabel);
                chunk.appendInstruction(OpCode.LABEL, shortcutLabel);

                // We arrived at this point from the shortcut, which had to be FALSE, and that Jump-not-true
                // instruction popped the false result from the stack, so we need to put it back. I think a
                // specific test instruction would make this nicer, but for now we can live with a few extra steps...

                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(false));
                chunk.appendInstruction(OpCode.LABEL, testCompleteLabel);

                break;

            case LOGICAL_OR:

                chunk.appendChunk((Chunk) expr.left.accept(this));
                chunk.appendInstruction(OpCode.JMPTRUE, shortcutLabel);
                chunk.appendChunk((Chunk) expr.right.accept(this));
                chunk.appendInstruction(OpCode.JMP, testCompleteLabel);
                chunk.appendInstruction(OpCode.LABEL, shortcutLabel);
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(true));
                chunk.appendInstruction(OpCode.LABEL, testCompleteLabel);

                break;
        }

        return chunk;
    }

    @Override
    public Chunk visit(GroupingExpression expr)
    {
        // TODO: Evaluate whether this was really needed
        /*
        if (expr.castToStringForEmbeddedStringExpression)
        {
            // We use a group with this special flag to force a cast of a varible like `${a}` to String, where a is a number (or whatever).
            // This is important because interpolated Strings are basically separate expressions joined with a +,
            // so `${a}${b}` is really a+b internally -- if we force both a and b to toString'd, then
            // you'll get a String concatenation instead of numbers being added...

            var chunk = new Chunk();

            chunk.appendChunk((Chunk) expr.expression.accept(this));
            chunk.appendInstruction(OpCode.FETCH, "toString", true);
            chunk.appendInstruction(OpCode.CALL, 0, true);

            return chunk;
        }
        else
        {
            return (Chunk) expr.expression.accept(this);
        }
        */

        return (Chunk) expr.expression.accept(this);
    }

    @Override
    public Chunk visit(LiteralExpression expr)
    {
        // Literal is always a constant value. The helper method looks to see if we already
        // have that constant (and if not adds it) and returns the index.

        return new Chunk(OpCode.CONST, constantIndexForValue(expr.value));
    }

    @Override
    public Chunk visit(UnaryExpression expr)
    {
        var chunk = new Chunk();

        switch (expr.op.type)
        {
            case NOT:
            {
                chunk.appendChunk((Chunk) expr.right.accept(this));

                int isTrueLabel = reserveLabelId();
                int endLabel = reserveLabelId();

                chunk.appendInstruction(OpCode.JMPTRUE, isTrueLabel);

                // If we're here it was false, so now it's true
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(true));
                chunk.appendInstruction(OpCode.JMP, endLabel);
                chunk.appendInstruction(OpCode.LABEL, isTrueLabel);

                // If we're here it was true, so now it's false
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(false));
                chunk.appendInstruction(OpCode.LABEL, endLabel);

                break;
            }

            case MINUS:

                // This block looks to see if the minus sign is followed by a literal number. If it is,
                // we can create a constant for the negative number and load that instead of the more
                // generalised unary operator behaviour, which negates whatever expression might come
                // after it in normal cirumstances.
                if (expr.right instanceof LiteralExpression l)
                {
                    if (l.value instanceof SmolNumber n)
                    {
                        chunk.appendInstruction(OpCode.CONST, constantIndexForValue(0 - n.numberValue));

                        break;
                    }
                }

                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(0.0));
                chunk.appendChunk((Chunk) expr.right.accept(this));
                chunk.appendInstruction(OpCode.SUB);

                break;

        }

        return chunk;
    }

    @Override
    public Chunk visit(VariableExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);

        if (expr.unaryOperator != null)
        {
            if (expr.unaryOperator == TokenType.POSTFIX_INCREMENT)
            {
                chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(1.0));
                chunk.appendInstruction(OpCode.ADD);
                chunk.appendInstruction(OpCode.STORE, expr.name.lexeme);
            }

            if (expr.unaryOperator == TokenType.POSTFIX_DECREMENT)
            {
                chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(1.0));
                chunk.appendInstruction(OpCode.SUB);
                chunk.appendInstruction(OpCode.STORE, expr.name.lexeme);
            }

            if (expr.unaryOperator == TokenType.PREFIX_INCREMENT)
            {
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(1.0));
                chunk.appendInstruction(OpCode.ADD);
                chunk.appendInstruction(OpCode.STORE, expr.name.lexeme);
                chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);
            }

            if (expr.unaryOperator == TokenType.PREFIX_DECREMENT)
            {
                chunk.appendInstruction(OpCode.CONST, constantIndexForValue(1.0));
                chunk.appendInstruction(OpCode.SUB);
                chunk.appendInstruction(OpCode.STORE, expr.name.lexeme);
                chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);
            }
        }

        return chunk;
    }

    @Override
    public Chunk visit(AssignExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.value.accept(this));

        chunk.appendInstruction(OpCode.STORE, expr.name.lexeme);

        // This is so inefficient

        chunk.appendInstruction(OpCode.FETCH, expr.name.lexeme);

        return chunk;
    }

    @Override
    public Chunk visit(CallExpression expr)
    {
        var chunk = new Chunk();

        // Evalulate the arguments from left to right and pop them on the stack.

        for (var i = expr.args.length - 1; i >= 0; i--) {
            var arg = expr.args[i];
            chunk.appendChunk((Chunk) ((Expression)arg).accept(this));
        }

        chunk.appendChunk((Chunk) expr.callee.accept(this)); // Load the function name onto the stack
        chunk.appendInstruction(OpCode.CALL, expr.args.length, expr.useObjectRef);

        return chunk;
    }

    @Override
    public Chunk visit(VarStatement stmt)
    {
        var chunk = new Chunk();

        chunk.appendInstruction(OpCode.DECLARE, stmt.name.lexeme);

        if (stmt.initializerExpression != null)
        {
            chunk.appendChunk((Chunk) stmt.initializerExpression.accept(this));
            chunk.appendInstruction(OpCode.STORE, stmt.name.lexeme);
        }

        chunk.mapTokens(stmt.firstTokenIndex, stmt.lastTokenIndex);

        return chunk;
    }

    @Override
    public Chunk visit(ExpressionStatement stmt)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) stmt.expression.accept(this));
        chunk.appendInstruction(OpCode.POP_AND_DISCARD);

        return chunk;
    }

    @Override
    public Chunk visit(BlockStatement stmt)
    {
        var chunk = new Chunk();

        chunk.appendInstruction(OpCode.ENTER_SCOPE);

        for (var blockStmt : stmt.statements)
        {
            Chunk c = (Chunk) blockStmt.accept(this);
            chunk.instructions.get(0).isStatementStartpoint = true;
            chunk.appendChunk(c);
        }

        chunk.appendInstruction(OpCode.LEAVE_SCOPE);

        return chunk;
    }

    @Override
    public Chunk visit(ReturnStatement stmt)
    {
        var chunk = new Chunk();

        if (stmt.expression != null)
        {
            chunk.appendChunk((Chunk) stmt.expression.accept(this));
        }
        else
        {
            chunk.appendInstruction(OpCode.CONST, constantIndexForUndefined());
        }

        chunk.appendInstruction(OpCode.RETURN);

        return chunk;
    }

    @Override
    public Chunk visit(IfStatement stmt)
    {
        var chunk = new Chunk();

        int notTrueLabel = reserveLabelId();

        chunk.appendChunk((Chunk) stmt.expression.accept(this));
        chunk.appendInstruction(OpCode.JMPFALSE, notTrueLabel);
        chunk.appendChunk((Chunk) stmt.thenStatement.accept(this));

        if (stmt.elseStatement == null)
        {
            chunk.appendInstruction(OpCode.LABEL, notTrueLabel);
        }
        else
        {
            int skipElseLabel = reserveLabelId();

            chunk.appendInstruction(OpCode.JMP, skipElseLabel);
            chunk.appendInstruction(OpCode.LABEL, notTrueLabel);
            chunk.appendChunk((Chunk) stmt.elseStatement.accept(this));
            chunk.appendInstruction(OpCode.LABEL, skipElseLabel);
        }

        return chunk;
    }

    @Override
    public Chunk visit(PrintStatement statement) {
        var chunk = new Chunk();

        chunk.appendInstruction(OpCode.PRINT);
        chunk.appendChunk((Chunk) statement.expression.accept(this));

        return chunk;
    }

    @Override
    public Chunk visit(TernaryExpression expr)
    {
        var chunk = new Chunk();

        var notTrueLabel = reserveLabelId();
        var endLabel = reserveLabelId();

        chunk.appendChunk((Chunk) expr.evaluationExpression.accept(this));
        chunk.appendInstruction(OpCode.JMPFALSE, notTrueLabel);
        chunk.appendChunk((Chunk) expr.expresisonIfTrue.accept(this));
        chunk.appendInstruction(OpCode.JMP, endLabel);
        chunk.appendInstruction(OpCode.LABEL, notTrueLabel);
        chunk.appendChunk((Chunk) expr.expresisonIfFalse.accept(this));
        chunk.appendInstruction(OpCode.LABEL, endLabel);

        return chunk;
    }


    private record WhileLoop(int startOfLoop, int endOfLoop) {
    }

    private Stack<WhileLoop> _loopStack = new Stack<>();

    @Override
    public Chunk visit(WhileStatement stmt)
    {
        var chunk = new Chunk();

        var startOfLoop = reserveLabelId();
        var endOfLoop = reserveLabelId();

        _loopStack.push(new WhileLoop(startOfLoop, endOfLoop));

        chunk.appendInstruction(OpCode.LOOP_START);
        chunk.appendInstruction(OpCode.LABEL, startOfLoop);
        chunk.appendChunk((Chunk) stmt.whileCondition.accept(this));
        chunk.appendInstruction(OpCode.JMPFALSE, endOfLoop);
        chunk.appendChunk((Chunk) stmt.executeStatement.accept(this));
        chunk.appendInstruction(OpCode.JMP, startOfLoop);
        chunk.appendInstruction(OpCode.LABEL, endOfLoop);
        chunk.appendInstruction(OpCode.LOOP_END);

        _loopStack.pop();

        return chunk;
    }

    @Override
    public Chunk visit(BreakStatement stmt)
    {
        return new Chunk(OpCode.LOOP_EXIT, _loopStack.peek().endOfLoop);
    }

    @Override
    public Chunk visit(ContinueStatement stmt)
    {
        return new Chunk(OpCode.LOOP_EXIT, _loopStack.peek().startOfLoop);
    }

    @Override
    public Chunk visit(ThrowStatement stmt)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) stmt.expression.accept(this));
        chunk.appendInstruction(OpCode.THROW);

        return chunk;
    }

    @Override
    public Chunk visit(TryStatement stmt)
    {
        var chunk = new Chunk();

        var exceptionLabel = reserveLabelId();
        var finallyLabel = reserveLabelId();
        var finallyWithExceptionLabel = reserveLabelId();

        // This will create a try 'checkpoint' in the vm. If we hit an exception the
        // vm will rewind the stack back to this instruction and jump to the catch/finally.
        chunk.appendInstruction(OpCode.TRY, exceptionLabel, false);

        // If an exception happens inside the body, it will rewind the stack to the try that just went on
        // and that tells us where to jump to

        chunk.appendChunk(this.visit(stmt.tryBody));

        // If there was no exception, we need to get rid of that try checkpoint that's on the stack, we aren't
        // going back there even if there's an exception in the finally

        chunk.appendInstruction(OpCode.POP_AND_DISCARD);

        // Now execute the finally

        chunk.appendInstruction(OpCode.JMP, finallyLabel);
        chunk.appendInstruction(OpCode.LABEL, exceptionLabel);

        // We're now at the catch part -- even if the user didn't specify one, we'll have a default (of { throw })
        // We now should have the thrown exception on the stack, so if a throw happens inside the catch that will
        // be the thing that's thrown

        chunk.appendInstruction(OpCode.TRY, finallyWithExceptionLabel, true); // True means keep the exception at the top of the stack

        if (stmt.catchBody != null)
        {
            if (stmt.exceptionVariableName != null)
            {
                chunk.appendInstruction(OpCode.ENTER_SCOPE);

                // Top of stack will be exception so store it in variable name

                chunk.appendInstruction(OpCode.DECLARE, stmt.exceptionVariableName.lexeme);
                chunk.appendInstruction(OpCode.STORE, stmt.exceptionVariableName.lexeme);
            }
            else
            {
                // Top of stack is exception, but no variable defined to hold it so get rid of it
                chunk.appendInstruction(OpCode.POP_AND_DISCARD);
            }

            chunk.appendChunk((Chunk) this.visit(stmt.catchBody)); // Might be a throw inside here...

            if (stmt.exceptionVariableName != null)
            {
                chunk.appendInstruction(OpCode.LEAVE_SCOPE);
            }
        }
        else
        {
            // No catch body is replaced by single instruction to rethrow the exception, which is already on the top of the stack

            chunk.appendInstruction(OpCode.THROW);
        }

        // If we made it here we got through the catch block without a throw, so we're free to execute the regular
        // finally and carry on with execution, exception is fully handled.

        // Top of stack has to the try checkpoint, so get rid of it because we aren't going back there
        chunk.appendInstruction(OpCode.POP_AND_DISCARD);
        chunk.appendInstruction(OpCode.JMP, finallyLabel);
        chunk.appendInstruction(OpCode.LABEL, finallyWithExceptionLabel);

        // If we're here then we had a throw inside the catch, so execute the finally and then throw it again.
        // When we throw this time the try checkpoint has been removed so we'll bubble down the stack to the next
        // try checkpoint (if there is one -- and panic if not)

        if (stmt.finallyBody != null)
        {
            chunk.appendChunk((Chunk) this.visit(stmt.finallyBody));

            // Instruction to check for unthrown exception and throw it
        }

        chunk.appendInstruction(OpCode.THROW);
        chunk.appendInstruction(OpCode.LABEL, finallyLabel);

        if (stmt.finallyBody != null)
        {
            chunk.appendChunk((Chunk) this.visit(stmt.finallyBody));

            // Instruction to check for unthrown exception and throw it
        }

        // Hopefully that all works. It's mega dependent on the instructions leaving the stack in a pristine state -- no
        // half finished evaluations or anything. That's definitely going to be a problem.

        return chunk;
    }

    @Override
    public Chunk visit(FunctionStatement stmt)
    {
        var functionIndex = _functionBodies.size() + 1;
        var functionName = stmt.name != null ? stmt.name.lexeme : "$_anon_" + functionIndex;

        _functionTable.add(new SmolFunction(functionName, functionIndex,
            stmt.parameters.length,
                Arrays.stream(stmt.parameters).map(p -> p.lexeme).toList()
        ));

        // Reserve the function body so if we're
        // TODO: I can't remember but I think this is in case we create an anonymous function
        // in the evaluation of the body
        _functionBodies.add(new Chunk());

        Chunk body = (Chunk)stmt.functionBody.accept(this);

        if (body.instructions.isEmpty() || body.getLast().opCode != OpCode.RETURN)
        {
            body.appendInstruction(OpCode.CONST, constantIndexForUndefined());
            body.appendInstruction(OpCode.RETURN);
        }

        _functionBodies.set(functionIndex - 1, body);

        // We are declaring a function, we don't add anything to the byte stream at the current loc.
        // When we allow functions as expressions and assignments we'll need to do something
        // here, I guess something more like load constant but for functions
        return new Chunk(OpCode.NOP);
    }

    @Override
    public Chunk visit(DebuggerStatement stmt)
    {
        return new Chunk(OpCode.DEBUGGER);
    }

    @Override
    public ByteCodeInstruction visit(ClassStatement stmt)
    {
        //this.class_table.Add(stmt.className.lexeme, stmt.superclassName?.lexeme);

        for (var fn : stmt.functions)
        {
            var functionIndex = _functionBodies.size() + 1;
            var functionName = stmt.className.lexeme + "." + fn.name.lexeme;

            _functionTable.add(new SmolFunction(functionName,
                functionIndex,
                fn.parameters.length,
                Arrays.stream(fn.parameters).map(p -> p.lexeme).toList()
            ));

            var body = (Chunk)fn.functionBody.accept(this);

            if (body.instructions.isEmpty() || body.getLast().opCode != OpCode.RETURN)
            {
                body.appendInstruction(OpCode.CONST, constantIndexForUndefined());
                body.appendInstruction(OpCode.RETURN);
            }

            _functionBodies.add(body);
        }

        // We are declaring a function, we don't add anything to the byte stream at the current loc.
        // When we allow functions as expressions and assignments we'll need to do something
        // here, I guess something more like load constant but for functions
        return new ByteCodeInstruction(OpCode.NOP);
    }

    @Override
    public Chunk visit(NewInstanceExpression expr)
    {
        var chunk = new Chunk();

        var className = expr.className.lexeme;

        // We need to tell the VM that we want to create an instance of a class.
        // It will need its own environment, and the instance info needs to be on the stack
        // so we can call the ctor, which needs to leave it on the stack afterwards
        // ready for whatever was wanting it in the first place
        chunk.appendInstruction(OpCode.CREATE_OBJECT, className);

        if (className != "Object")
        {
            for (Expression arg : Arrays.stream(expr.ctorArgs).toList()) // TODO: should be .reversed())
            {
                chunk.appendChunk((Chunk) arg.accept(this));
            }

            chunk.appendInstruction(OpCode.DUPLICATE_VALUE, expr.ctorArgs.length); // We need two copies of that ref
        }
        else
        {
            chunk.appendInstruction(OpCode.DUPLICATE_VALUE, 0); // We need two copies of that ref
        }


        // Stack now has class instance value

        var constructorFnName = expr.className.lexeme + ".constructor";

        chunk.appendInstruction(OpCode.FETCH, constructorFnName, true);

        if (className == "Object") { // TODO: Doubt this will work like this in Java version
            for (Expression arg : Arrays.stream(expr.ctorArgs).toList()) // TODO: Should be .reversed())
            {
                chunk.appendChunk((Chunk) arg.accept(this));
            }
        }

        chunk.appendInstruction(OpCode.CALL, expr.ctorArgs.length,
            true // this operand means use the instance that's on stack -- might not use this in the end because I think we should be able to tell from class name it's an instance call...
        );

        chunk.appendInstruction(OpCode.POP_AND_DISCARD); // We don't care about the ctor's return value

        return chunk;
    }

    @Override
    public Chunk visit(GetExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));
        chunk.appendInstruction(OpCode.FETCH, expr.attributeName.lexeme, true);

        // Who knows if this will work... :)

        return chunk;
    }

    @Override
    public Chunk visit(IndexerGetExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));
        chunk.appendChunk((Chunk) expr.indexerExpression.accept(this));

        // Now on the stack we have an expression result which is
        // the index value we want to get, so we have a special
        // way to call fetch that knows to get that value and use
        // it as a property

        chunk.appendInstruction(OpCode.FETCH, "@IndexerGet", true);

        // Who knows if this will work... :)

        return chunk;
    }

    @Override
    public Chunk visit(SetExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));

        chunk.appendChunk((Chunk) expr.valueExpression.accept(this));

        chunk.appendInstruction(OpCode.STORE, expr.attributeName.lexeme, true); // true means object reference on stack

        // This is so inefficient, but we need to read the saved value back onto the stack

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));

        chunk.appendInstruction(OpCode.FETCH, expr.attributeName.lexeme, true);

        return chunk;
    }

    @Override
    public Chunk visit(ObjectInitializerExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendInstruction(OpCode.DUPLICATE_VALUE, 2);
        chunk.appendChunk((Chunk) expr.value.accept(this));
        chunk.appendInstruction(OpCode.STORE, expr.name.lexeme, true); // true means object reference on stack

        // We don't reload the value onto the stack for these...

        return chunk;
    }

    @Override
    public Chunk visit(IndexerSetExpression expr)
    {
        var chunk = new Chunk();

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));
        chunk.appendChunk((Chunk) expr.valueExpression.accept(this));
        chunk.appendChunk((Chunk) expr.indexerExpression.accept(this));
        chunk.appendInstruction(OpCode.STORE, "@IndexerSet", true); // true means object reference on stack

        // This is so inefficient, but we need to read the saved value back onto the stack

        chunk.appendChunk((Chunk) expr.targetObject.accept(this));

        // TODO: This won't even work for indexer++ etc.
        chunk.appendChunk((Chunk) expr.indexerExpression.accept(this));
        chunk.appendInstruction(OpCode.FETCH, "@IndexerGet", true);

        return chunk;
    }

    @Override
    public ByteCodeInstruction visit(FunctionExpression expr)
    {
        var functionIndex = _functionBodies.size() + 1;
        var functionName = "$_anon_" + functionIndex;

        _functionTable.add(new SmolFunction(functionName, functionIndex,
            expr.parameters.length,
            Arrays.stream(expr.parameters).map(p -> p.lexeme).toList()
        ));

        var body = (Chunk)expr.functionBody.accept(this);

        if (body.instructions.isEmpty() || body.getLast().opCode != OpCode.RETURN)
        {
            body.appendInstruction(OpCode.CONST, constantIndexForUndefined());
            body.appendInstruction(OpCode.RETURN);
        }

        _functionBodies.add(body);

        // We are declaring a function expression, so the reference to the function needs
        // to go on the stack so some other code can grab and use it
        return new ByteCodeInstruction(OpCode.FETCH, functionName);
    }
}
