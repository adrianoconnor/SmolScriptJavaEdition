package org.smolscript.internals;

import org.smolscript.internals.ast.expressions.*;
import org.smolscript.internals.ast.statements.*;
import org.smolscript.internals.variableTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class Parser {

    private final Token[] tokens;
    private int currentTokenIndex = 0;

    private final Stack<String> statementCallStack = new Stack<>();

    public Parser(Token[] tokens) {
        this.tokens = tokens;
    }

    private Boolean match(TokenType... tokenTypes) {
        for(var tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Boolean match(TokenType tokenType) {
        if (check(tokenType)) {
            advance();
            return true;
        }

        return false;
    }

    private Boolean check(TokenType tokenType) {
        return check(tokenType, 0);
    }

    private Boolean check(TokenType tokenType, int skip) {

        if (!reachedEnd()) {
            return peek(skip).type == tokenType;
        }

        return false;
    }

    private Token peek() {
        return peek(0);
    }

    private Token peek(int skip) {
        return tokens[currentTokenIndex + skip];
    }

    private Token advance() {
        if (!reachedEnd()) currentTokenIndex++;

        return previous();
    }

    public Token previous() {
        return previous(0);
    }

    public Token previous(int skip) {
        return tokens[currentTokenIndex - 1 - skip];
    }

    private Boolean reachedEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token consume(TokenType tokenType, String errorIfNotFound) throws ParseError {
        if (check(tokenType)) return advance();

        // If we expected a ; but got a newline, we just wave it through
        if (tokenType == TokenType.SEMICOLON && tokens[currentTokenIndex - 1].isFollowedByLineBreak)
        {
            // We need to return a token, so we'll make a fake semicolon
            return new Token(TokenType.SEMICOLON, "", "", -1, -1, -1, -1);
        }

        // If we expected a ; but got a }, we also wave that through
        if (tokenType == TokenType.SEMICOLON && (check(TokenType.RIGHT_BRACE) || peek().type == TokenType.EOF))
        {
            return new Token(TokenType.SEMICOLON, "", "", -1, -1, -1, -1);
        }

        raiseParseError(peek(), errorIfNotFound);

        return null;
    }

    private void raiseParseError(Token token, String errorMessage) throws ParseError {
        throw new ParseError(token, errorMessage + " (Line " + token.line + ", Col " + token.col + ")");
    }

    private void synchronize()
    {
        advance();

        while (!reachedEnd())
        {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type)
            {
                case CLASS:
                case FUNC:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                case TRY:
                    return;
            }

            advance();
        }

        statementCallStack.clear();
    }


    public Statement[] parse() throws Exception {

        List<Statement> statements = new ArrayList<Statement>();
        List<ParseError> errors = new ArrayList<ParseError>();

        while (!reachedEnd())
        {
            try
            {
                if (peek().type == TokenType.SEMICOLON)
                {
                    consume(TokenType.SEMICOLON, "");
                }
                else
                {
                    statements.add(declaration());
                }
            }
            catch (ParseError e)
            {
                errors.add(e);
            }
        }

        if (!errors.isEmpty())
        {
            throw new Exception("Encountered one or more errors while parsing (first error: " + errors.get(0).getMessage()+")");
            //throw new SmolCompilerError(errors, $"Encountered one or more errors while parsing (first error: {errors.First().Message})");
        }

        return statements.toArray(new Statement[0]);
    }

    private Statement declaration() throws ParseError {
        try
        {
            if (match(TokenType.VAR)) return varDeclaration();
            if (match(TokenType.FUNC)) return functionDeclaration();
            if (match(TokenType.CLASS)) return classDeclaration();

            return statement();
        }
        catch (ParseError e)
        {
            synchronize();
            throw e;
        }
    }

    private Statement varDeclaration() throws ParseError {
        var firstTokenIndex = currentTokenIndex - 1;

        var name = consume(TokenType.IDENTIFIER, "Expected variable name");

        Expression initializer = null;

        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        var closingSemiColon = consume(TokenType.SEMICOLON, "Expected either a value to be assigned or the end of the statement");
        var skip = Objects.requireNonNull(closingSemiColon).startPosition == -1 ? 1 : 2;
        var lastTokenIndex = currentTokenIndex - skip;

        return new VarStatement(name, initializer, firstTokenIndex, lastTokenIndex);
    }

    private Statement functionDeclaration() throws ParseError {
        // If this function is nested we're going to turn it into a var fnName = fnExpression() so that
        // the function variable becomes a regular variable in the enclosing functions environment.
        var firstTokenIndex = currentTokenIndex - 1;
        var isNestedFunction = statementCallStack.contains("FUNCTION");

        // Regular function statement code...

        statementCallStack.push("FUNCTION");

        var functionName = consume(TokenType.IDENTIFIER, "Expected function name");

        List<Token> functionParams = new ArrayList<Token>();

        consume(TokenType.LEFT_BRACKET, "Expected (");

        if (!check(TokenType.RIGHT_BRACKET))
        {
            do
            {
                if (functionParams.size() >= 32_766)
                {
                    raiseParseError(peek(), "Can't define a function with more than 32,766 parameters.");
                }

                functionParams.add(consume(TokenType.IDENTIFIER, "Expected parameter name"));
            } while (match(TokenType.COMMA));
        }

        var functionParamsAsArray = functionParams.toArray(new Token[0]);

        consume(TokenType.RIGHT_BRACKET, "Expected )");
        consume(TokenType.LEFT_BRACE, "Expected {");

        var functionBody = block();

        statementCallStack.pop();

        if (isNestedFunction) // Switch out the function statement for a var declaration if it's nested
        {
            var skip = Objects.requireNonNull(consume(TokenType.SEMICOLON, "Expected either a value to be assigned or the end of the statement")).startPosition == -1 ? 1 : 2;
            var lastTokenIndex = currentTokenIndex - skip;

            return new VarStatement(functionName, new FunctionExpression(functionParamsAsArray, functionBody), firstTokenIndex, lastTokenIndex);
        }
        else
        {
            return new FunctionStatement(functionName, functionParamsAsArray, functionBody);
        }
    }

    private Statement classDeclaration() throws ParseError {
        var className = consume(TokenType.IDENTIFIER, "Expected class name");
        Token superclassName = null;
        var functions = new ArrayList<FunctionStatement>();

        if (match(TokenType.COLON))
        {
            superclassName = consume(TokenType.IDENTIFIER, "Expected superclass name");
        }

        consume(TokenType.LEFT_BRACE, "Expected {");

        while (!check(TokenType.RIGHT_BRACE) && !reachedEnd())
        {
            if (check(TokenType.IDENTIFIER) && check(TokenType.LEFT_BRACKET, 1))
            {
                functions.add((FunctionStatement)functionDeclaration());
            }
            else
            {
                throw new ParseError(peek(), "Didn't expect to find "+peek()+" in the class body");
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expected }");

        return new ClassStatement(className, superclassName, functions.toArray(new FunctionStatement[0]));
    }


    private Statement statement() throws ParseError {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.TRY)) return tryStatement();
        if (match(TokenType.THROW)) return throwStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.BREAK)) return breakStatement();
        if (match(TokenType.CONTINUE)) return continueStatement();
        if (match(TokenType.LEFT_BRACE)) return block();
        if (match(TokenType.DEBUGGER)) return debuggerStatement();

        return expressionStatement();
    }

    private Statement throwStatement() throws ParseError {

        var throwTokenIndex = currentTokenIndex - 1;
        var exprFirstTokenIndex = currentTokenIndex;

        var expr = expression();

        var exprLastTokenIndex = currentTokenIndex;

        consume(TokenType.SEMICOLON, "Expected ;");

        return new ThrowStatement(expr, throwTokenIndex, exprFirstTokenIndex, exprLastTokenIndex);
    }

    private Statement returnStatement() throws ParseError {
        if (!statementCallStack.contains("FUNCTION"))
        {
            raiseParseError(previous(), "Return not in function.");
        }

        var returnTokenIndex = currentTokenIndex - 1;

        if (peek().type == TokenType.SEMICOLON
                || peek().type == TokenType.RIGHT_BRACE
                || previous().isFollowedByLineBreak) {

            // No expression for this return statement

            consume(TokenType.SEMICOLON, "Expected ;");

            return new ReturnStatement(null, returnTokenIndex, null, null);
        }
        else {
            var exprFirstTokenIndex = currentTokenIndex;
            var expr = this.expression();
            var exprLastTokenIndex = currentTokenIndex - 1;

            consume(TokenType.SEMICOLON, "Expected ;");

            return new ReturnStatement(expr, returnTokenIndex, exprFirstTokenIndex, exprLastTokenIndex);
        }
    }

    private Statement breakStatement() throws ParseError {
        if (!statementCallStack.contains("WHILE"))
        {
            raiseParseError(previous(), "Break should be inside a while or for loop");
        }

        var breakTokenIndex = currentTokenIndex - 1;

        consume(TokenType.SEMICOLON, "Expected ;");

        return new BreakStatement(breakTokenIndex);
    }

    private Statement continueStatement() throws ParseError {
        if (!statementCallStack.contains("WHILE"))
        {
            raiseParseError(previous(), "Continue should be inside a while or for loop");
        }

        var continueTokenIndex = currentTokenIndex - 1;

        consume(TokenType.SEMICOLON, "Expected ;");

        return new ContinueStatement(continueTokenIndex);
    }

    private Statement debuggerStatement() throws ParseError {

        var debuggerTokenIndex = currentTokenIndex - 1;

        consume(TokenType.SEMICOLON, "Expected ;");

        return new DebuggerStatement(debuggerTokenIndex);
    }

    private BlockStatement block() throws ParseError {
        statementCallStack.push("BLOCK");

        var blockStartTokenIndex = currentTokenIndex - 1;

        List<Statement> statements = new ArrayList<Statement>();

        while (!check(TokenType.RIGHT_BRACE) && !reachedEnd())
        {
            if (peek().type == TokenType.SEMICOLON)
            {
                consume(TokenType.SEMICOLON, "");
            }
            else
            {
                statements.add(declaration());
            }
        }

        statementCallStack.pop();

        var blockEndTokenIndex = currentTokenIndex;

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");

        return new BlockStatement(statements.toArray(new Statement[0]), false, blockStartTokenIndex, blockEndTokenIndex);
    }

    private Statement ifStatement() throws ParseError {
        statementCallStack.push("IF");

        consume(TokenType.LEFT_BRACKET, "Expected (");
        var condition = expression();
        consume(TokenType.RIGHT_BRACKET, "Expected )");
        var thenStatement = statement();

        Statement elseStatement = null;

        if (match(TokenType.ELSE))
        {
            elseStatement = statement();
        }

        statementCallStack.pop();

        return new IfStatement(condition, thenStatement, elseStatement, null, null, null, null, null, null); // TODO: Capture correct token indexes
    }

    private Statement whileStatement() throws ParseError {
        statementCallStack.push("WHILE");

        consume(TokenType.LEFT_BRACKET, "Expected (");
        var whileCondition = expression();
        consume(TokenType.RIGHT_BRACKET, "Expected )");
        var whileStatement = statement();

        statementCallStack.pop();

        return new WhileStatement(whileCondition, whileStatement, null, null, null, null, null); // TODO: Capture correct token indexes
    }

    private Statement tryStatement() throws ParseError {
        statementCallStack.push("TRY");

        consume(TokenType.LEFT_BRACE, "Expected {");
        BlockStatement tryBody = block();
        BlockStatement catchBody = null;
        BlockStatement finallyBody = null;

        Token exceptionVarName = null;

        if (match(TokenType.CATCH))
        {
            if (match(TokenType.LEFT_BRACKET))
            {
                exceptionVarName = consume(TokenType.IDENTIFIER, "Expected a single variable name for exception variable");

                consume(TokenType.RIGHT_BRACKET, "Expected )");
            }

            consume(TokenType.LEFT_BRACE, "Expected {");
            catchBody = block();
        }

        if (match(TokenType.FINALLY))
        {
            consume(TokenType.LEFT_BRACE, "Expected {");
            finallyBody = block();
        }

        if (catchBody == null && finallyBody == null)
        {
            consume(TokenType.CATCH, "Expected catch or finally");
        }

        statementCallStack.pop();

        return new TryStatement(tryBody, exceptionVarName, catchBody, finallyBody);
    }

    private Statement forStatement() throws ParseError {
        statementCallStack.push("WHILE");

        consume(TokenType.LEFT_BRACKET, "Expected (");

        Statement initialiser = null;

        if (match(TokenType.SEMICOLON))
        {
            initialiser = null;
        }
        else if (match(TokenType.VAR))
        {
            initialiser = varDeclaration();
        }
        else
        {
            initialiser = expressionStatement();
        }

        Expression condition = null;

        if (!check(TokenType.SEMICOLON))
        {
            condition = expression();
        }
        else
        {
            condition = new LiteralExpression(new SmolBool(true));
        }

        consume(TokenType.SEMICOLON, "Expected ;");

        Expression increment = null;

        if (!check(TokenType.RIGHT_BRACKET))
        {
            increment = expression();
        }

        consume(TokenType.RIGHT_BRACKET, "Expected )");

        var body = statement();

        if (increment != null)
        {
            body = new BlockStatement(new Statement[] {
                body, new ExpressionStatement(increment, null, null)
            }, true, null,null); // TODO: Validate these additional params
        }

        body = new WhileStatement(condition, body, null, null, null, null, null); // TODO: Add token indexes for debug symbols

        if (initialiser != null)
        {
            body = new BlockStatement(new Statement[] {
                initialiser,
                body
            }, true, null, null); // TODO: Validate these additional params
        }

        statementCallStack.pop();

        return body;
    }

    private Statement expressionStatement() throws ParseError {

        var expressionStartTokenIndex = currentTokenIndex - 1;
        var expr = expression();
        var expressionEndTokenIndex = currentTokenIndex;

        consume(TokenType.SEMICOLON, "Expected ;");

        return new ExpressionStatement(expr, expressionStartTokenIndex, expressionEndTokenIndex);
    }

    private Expression expression() throws ParseError {
        var expr = assignment();

        if (match(TokenType.QUESTION_MARK))
        {
            var thenExpression = expression(); // This isn't right, need to work out correct order
            consume(TokenType.COLON, "Expected :");
            var elseExpression = expression();

            return new TernaryExpression(expr, thenExpression, elseExpression);
        }

        return expr;
    }

    private Expression assignment() throws ParseError {
        var expr = functionExpression();

        if (match(TokenType.EQUAL))
        {
            var equals = previous();
            var value = assignment();

            if (expr instanceof VariableExpression varExpr)
            {
                return new AssignExpression(varExpr.name, value);
            }
            else if (expr instanceof GetExpression getExpr)
            {
                return new SetExpression(getExpr.targetObject, getExpr.attributeName, value);
            }
            else if (expr instanceof IndexerGetExpression getExpr)
            {
                return new IndexerSetExpression(getExpr.targetObject, getExpr.indexerExpression, value);
            }

            raiseParseError(equals, "Invalid assignment target.");
        }


        if (match(TokenType.PLUS_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.PLUS, "+=", expr);
        }

        if (match(TokenType.MINUS_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.MINUS, "-=", expr);
        }

        if (match(TokenType.DIVIDE_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.DIVIDE, "/=", expr);
        }

        if (match(TokenType.REMAINDER_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.REMAINDER, "%=", expr);
        }

        if (match(TokenType.MULTIPLY_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.MULTIPLY, "*=", expr);
        }

        if (match(TokenType.POW_EQUALS))
        {
            return compoundAssignmentExpressionHelper(TokenType.POW, "**=", expr);
        }

        return expr;
    }

    private Expression compoundAssignmentExpressionHelper(TokenType tokenForExpression, String literalForExpression, Expression expr) throws ParseError {

        var originalToken = previous();
        var value = assignment();
        var binExpr = new BinaryExpression(expr, new Token(tokenForExpression, literalForExpression, null, originalToken.line, originalToken.col, originalToken.startPosition, originalToken.endPosition), value);

        if (expr instanceof VariableExpression varExpr) {
            return new AssignExpression(varExpr.name, binExpr);
        }
        else if (expr instanceof GetExpression getExpr) {
            return new SetExpression(getExpr.targetObject, getExpr.attributeName, binExpr);
        }
        else if (expr instanceof IndexerGetExpression getIndexerExpr) {
            return new IndexerSetExpression(getIndexerExpr.targetObject, getIndexerExpr.indexerExpression, binExpr);
        }

        throw new Error("Invalid assignment target");

    }

    private Expression functionExpression() throws ParseError {
        if ((peek().type == TokenType.LEFT_BRACKET || peek().type == TokenType.IDENTIFIER) && isInFatArrow(false))
        {
            return fatArrowFunctionExpression(false);
        }
        else if (match(TokenType.FUNC))
        {
            statementCallStack.push("FUNCTION");

            List<Token> functionParams = new ArrayList<Token>();

            consume(TokenType.LEFT_BRACKET, "Expected (");

            if (!check(TokenType.RIGHT_BRACKET))
            {
                do
                {
                    if (functionParams.size() >= 127)
                    {
                        raiseParseError(peek(), "Can't define a function with more than 127 parameters.");
                    }

                    functionParams.add(consume(TokenType.IDENTIFIER, "Expected parameter name"));
                } while (match(TokenType.COMMA));
            }

            consume(TokenType.RIGHT_BRACKET, "Expected )");
            consume(TokenType.LEFT_BRACE, "Expected {");

            var functionBody = block();

            statementCallStack.pop();

            return new FunctionExpression(functionParams.toArray(new Token[0]), functionBody);
        }

        return logicalOr();
    }

    private Expression logicalOr() throws ParseError {
        var expr = logicalAnd();

        while (match(TokenType.LOGICAL_OR))
        {
            var op = previous();
            var right = logicalAnd();
            expr = new LogicalExpression(expr, op, right);
        }

        return expr;
    }

    private Expression logicalAnd() throws ParseError {
        var expr = equality();

        while (match(TokenType.LOGICAL_AND))
        {
            var op = previous();
            var right = equality();
            expr = new LogicalExpression(expr, op, right);
        }

        return expr;
    }

    private Expression equality() throws ParseError {
        var expr = comparison();

        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL_EQUAL))
        {
            var op = previous();
            var right = comparison();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression comparison() throws ParseError {
        var expr = bitwiseOperation(); // Was term

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL))
        {
            var op = previous();
            var right = bitwiseOperation(); // Was term
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression bitwiseOperation() throws ParseError {
        var expr = term();

        while (match(TokenType.BITWISE_AND, TokenType.BITWISE_OR, TokenType.REMAINDER))
        {
            var op = previous();
            var right = term();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression term() throws ParseError {
        var expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS))
        {
            var op = previous();
            var right = factor();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression factor() throws ParseError {
        var expr = pow();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE))
        {
            var op = previous();
            var right = pow();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression pow() throws ParseError {
        var expr = unary();

        while (match(TokenType.POW))
        {
            var op = previous();
            var right = unary();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression unary() throws ParseError {
        if (match(TokenType.NOT, TokenType.MINUS))
        {
            var op = previous();
            var right = unary();
            return new UnaryExpression(op, right);
        }

        return call();
    }

    private Expression call() throws ParseError {
        var expr = primary();

        while (true)
        {
            if (match(TokenType.LEFT_BRACKET))
            {
                expr = finishCall(expr, expr instanceof GetExpression);
            }
            else if (match(TokenType.LEFT_SQUARE_BRACKET))
            {
                var indexerExpression = expression();

                var closingParen = consume(TokenType.RIGHT_SQUARE_BRACKET, "Expected ]");

                expr = new IndexerGetExpression(expr, indexerExpression);
            }
            else if (match(TokenType.DOT))
            {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new GetExpression(expr, name);
            }
            else
            {
                break;
            }
        }

        return expr;
    }

    private Expression finishCall(Expression callee) throws ParseError {
        return finishCall(callee, false);
    }

    private Expression finishCall(Expression callee, Boolean isFollowingGetter) throws ParseError {
        var args = new ArrayList<Expression>();

        if (!check(TokenType.RIGHT_BRACKET))
        {
            do { args.add(expression()); } while (match(TokenType.COMMA));
        }

        var closingParen = consume(TokenType.RIGHT_BRACKET, "Expected )");

        return new CallExpression(callee, args.toArray(new Expression[0]), isFollowingGetter);
    }

    private Expression primary() throws ParseError {
        if (match(TokenType.FALSE)) return new LiteralExpression(new SmolBool(false));
        if (match(TokenType.TRUE)) return new LiteralExpression(new SmolBool(true));
        if (match(TokenType.NULL)) return new LiteralExpression(new SmolNull());
        if (match(TokenType.UNDEFINED)) return new LiteralExpression(new SmolUndefined());

        if (match(TokenType.NUMBER))
        {
            return new LiteralExpression(new SmolNumber((Double)previous().literal));
        }

        if (match(TokenType.STRING))
        {
            return new LiteralExpression(new SmolString((String) previous().literal));
        }

        if (match(TokenType.PREFIX_INCREMENT))
        {
            if (match(TokenType.IDENTIFIER))
            {
                return new VariableExpression(previous(), TokenType.PREFIX_INCREMENT);
            }
        }

        if (match(TokenType.PREFIX_DECREMENT))
        {
            if (match(TokenType.IDENTIFIER))
            {
                return new VariableExpression(previous(), TokenType.PREFIX_DECREMENT);
            }
        }

        if (match(TokenType.IDENTIFIER))
        {
            if (match(TokenType.POSTFIX_INCREMENT))
            {
                return new VariableExpression(previous(1), TokenType.POSTFIX_INCREMENT);
            }
            else if (match(TokenType.POSTFIX_DECREMENT))
            {
                return new VariableExpression(previous(1), TokenType.POSTFIX_DECREMENT);
            }
            else
            {
                return new VariableExpression(previous(), null);
            }
        }

        if (match(TokenType.NEW))
        {
            var className = consume(TokenType.IDENTIFIER, "Expected identifier after new");

            consume(TokenType.LEFT_BRACKET, "Expect ')' after expression.");

            List<Expression> args = new ArrayList<Expression>();

            if (!check(TokenType.RIGHT_BRACKET))
            {
                do { args.add(expression()); } while (match(TokenType.COMMA));
            }

            var closingParen = consume(TokenType.RIGHT_BRACKET, "Expected )");

            return new NewInstanceExpression(className, args.toArray(new Expression[0]));
        }

        if (match(TokenType.LEFT_SQUARE_BRACKET))
        {
            var originalToken = previous();
            var className = new Token(TokenType.IDENTIFIER, "Array", null, originalToken.line, originalToken.col, originalToken.startPosition, originalToken.endPosition);
            var args = new ArrayList<Expression>();

            if (!check(TokenType.RIGHT_SQUARE_BRACKET))
            {
                do { args.add(expression()); } while (match(TokenType.COMMA));
            }

            var closingParen = consume(TokenType.RIGHT_SQUARE_BRACKET, "Expected ]");

            return new NewInstanceExpression(className, args.toArray(new Expression[0]));
        }

        if (match(TokenType.LEFT_BRACE))
        {
            var originalToken = previous();
            var className = new Token(TokenType.IDENTIFIER, "Object", null, originalToken.line, originalToken.col, originalToken.startPosition, originalToken.endPosition);

            var args = new ArrayList<Expression>();

            if (!check(TokenType.RIGHT_BRACE))
            {
                do
                {
                    var name = consume(TokenType.IDENTIFIER, "Expected idetifier");
                    consume(TokenType.COLON, "Exepcted :");
                    var value = expression();

                    args.add(new ObjectInitializerExpression(name, value));

                } while (match(TokenType.COMMA));
            }

            var closingParen = consume(TokenType.RIGHT_BRACE, "Expected }");

            return new NewInstanceExpression(className, args.toArray(new Expression[0]));
        }

        if (match(TokenType.LEFT_BRACKET))
        {
            var expr = expression();

            consume(TokenType.RIGHT_BRACKET, "Expect ')' after expression.");

            return new GroupingExpression(expr);
        }

        if (match(TokenType.START_OF_EMBEDDED_STRING_EXPRESSION))
        {
            var expr = expression();

            consume(TokenType.END_OF_EMBEDDED_STRING_EXPRESSION, "Expect internal special token 'END_OF_EMBEDDED_STRING_EXPRESSION' after expression.");

            return new GroupingExpression(expr);
        }

        raiseParseError(peek(), "Parser did not expect to see '"+peek().lexeme+"' here");

        return null; // TODO: Maybe refactor this away with a throwable parseError function
    }

    private FunctionExpression fatArrowFunctionExpression() throws ParseError {
        return fatArrowFunctionExpression(false);
    }

    private FunctionExpression fatArrowFunctionExpression(Boolean openBracketconsumed) throws ParseError {

        statementCallStack.push("FUNCTION");

        if (!openBracketconsumed && check(TokenType.LEFT_BRACKET))
        {
            consume(TokenType.LEFT_BRACKET, "Expected (");

            openBracketconsumed = true;
        }

        var functionParams = new ArrayList<Token>();

        if (!check(TokenType.RIGHT_BRACKET))
        {
            do
            {
                if (functionParams.size() >= 127)
                {
                    raiseParseError(peek(), "Can't define a function with more than 127 parameters.");
                }

                functionParams.add(consume(TokenType.IDENTIFIER, "Expected parameter name"));

            } while (match(TokenType.COMMA));
        }

        if (openBracketconsumed)
        {
            consume(TokenType.RIGHT_BRACKET, "Expected )");
        }

        consume(TokenType.FAT_ARROW, "Expected =>");

        if (check(TokenType.LEFT_BRACE))
        {
            consume(TokenType.LEFT_BRACE, "Expected {");

            var functionBody = block();

            statementCallStack.pop();

            return new FunctionExpression(functionParams.toArray(new Token[0]), functionBody);
        }
        else
        {
            var funcExpr = expression();

            // We need to remove this next check to allow function parameter style fat arrows to work...
            // e.g., my_func((x) => x + 1, param2)
            // In this case there's no ;, there's just an expression, but we know it's just one single
            // expression so in theory no need to check for any terminator...?

            // consume(TokenType.SEMICOLON, "Expected ;");

            statementCallStack.pop();

            var functionBody = new BlockStatement(new Statement[] {
                    new ReturnStatement(funcExpr, null, null, null)
            }, true, null, null);

            return new FunctionExpression(functionParams.toArray(new Token[0]), functionBody);
        }
    }

    private Boolean isInFatArrow() {
        return isInFatArrow(true);
    }

    private Boolean isInFatArrow(Boolean openBracketconsumed)
    {
        // If we've jsut consumed an opening bracket we need to look ahead for
        //  (x) =>
        // or
        //  (x, y, z) =>

        var index = currentTokenIndex;

        // If we're looking at an expression, the current token could be an identifier and we just need to check if the next token is =>

        if (!openBracketconsumed)
        {
            if (!tokens[currentTokenIndex].isFollowedByLineBreak && tokens[currentTokenIndex + 1].type == TokenType.FAT_ARROW)
            {
                return true;
            }
            else if (tokens[currentTokenIndex].type == TokenType.LEFT_BRACKET)
            {
                index++; // pretend we consumed the left brack and next section can serve both needs
            }
            else
            {
                return false;
            }
        }

        // The logic for brackets is a bit more involved...

        var previous = TokenType.LEFT_BRACKET;


        while (true)
        {
            if (tokens[index].isFollowedByLineBreak && tokens[index].type != TokenType.FAT_ARROW) // => has to be on same line as (...), but newline can come after =>
            {
                break;
            }

            var next = tokens[index];

            if (previous == TokenType.LEFT_BRACKET && next.type == TokenType.RIGHT_BRACKET)
            {
                // Valid, move on to the next token
                index++;
            }
            else if (previous == TokenType.LEFT_BRACKET && next.type == TokenType.IDENTIFIER)
            {
                // Valid, move on to the next token
                index++;
            }
            else if (previous == TokenType.IDENTIFIER && (next.type == TokenType.COMMA || next.type == TokenType.RIGHT_BRACKET))
            {
                // Valid, move on to the next token
                index++;
            }
            else if (previous == TokenType.COMMA && next.type == TokenType.IDENTIFIER)
            {
                // Valid, move on to the next token
                index++;
            }
            else if (previous == TokenType.RIGHT_BRACKET && next.type == TokenType.FAT_ARROW)
            {
                // Valid, we're definitely dealing with a fat arrow
                return true;
            }
            else
            {
                break;
            }

            previous = next.type;
        }

        return false;
    }

}
