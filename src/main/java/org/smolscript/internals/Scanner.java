package org.smolscript.internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Scanner {

    private final String source;
    private final char[] sourceChars;
    private final List<Token> tokens = new ArrayList<>();

    private int startOfToken = 0;
    private int currentPosition = 0;
    private int currentLine = 1;
    private int currentLineStartIndex = 0;
    private int previous = 0;

    private final HashMap<String, TokenType> keywords = new HashMap<>() {{
        put("break", TokenType.BREAK);
        put("class", TokenType.CLASS);
        put("case", TokenType.CASE);
        put("const", TokenType.CONST);
        put("continue", TokenType.CONTINUE);
        put("debugger", TokenType.DEBUGGER);
        put("do", TokenType.DO);
        put("else", TokenType.ELSE);
        put("false", TokenType.FALSE);
        put("for", TokenType.FOR);
        put("function", TokenType.FUNC);
        put("if", TokenType.IF);
        put("null", TokenType.NULL);
        put("new", TokenType.NEW);
        put("return", TokenType.RETURN);
        put("super", TokenType.SUPER);
        put("switch", TokenType.SWITCH);
        put("true", TokenType.TRUE);
        put("var", TokenType.VAR);
        put("let", TokenType.VAR);
        put("while", TokenType.WHILE);
        put("undefined", TokenType.UNDEFINED);
        put("try", TokenType.TRY);
        put("catch", TokenType.CATCH);
        put("finally", TokenType.FINALLY);
        put("throw", TokenType.THROW);
    }};

    public Scanner(String source) {
        this.source = source;
        this.sourceChars = source.toCharArray();
    }

    public List<Token> scanTokens() throws Exception {
        while (!isAtEnd()) {
            startOfToken = currentPosition;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, currentLine, currentPosition - currentLineStartIndex, source.length(), source.length()));
        return tokens;
    }

    private void scanToken() throws Exception {

        char c = nextChar();

        switch (c) {
            case '(':
                addToken(TokenType.LEFT_BRACKET);
                break;
            case ')':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '[':
                addToken(TokenType.LEFT_SQUARE_BRACKET);
                break;
            case ']':
                addToken(TokenType.RIGHT_SQUARE_BRACKET);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '?':
                addToken(TokenType.QUESTION_MARK);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;

            case '=':
                if (matchNext('=')) {
                    addToken(TokenType.EQUAL_EQUAL);
                } else if (matchNext('>')) {
                    addToken(TokenType.FAT_ARROW);
                } else {
                    addToken(TokenType.EQUAL);
                }
                break;
            case '!':
                if (matchNext('=')) {
                    addToken(TokenType.NOT_EQUAL);
                } else {
                    addToken(TokenType.NOT);
                }
                break;

            case '+':
                if (matchNext('+')) {
                    var previousToken = tokens.get(tokens.size() - 1);
                    if (previousToken.type == TokenType.IDENTIFIER && !previousToken.isFollowedByLineBreak) {
                        addToken(TokenType.POSTFIX_INCREMENT);
                    } else {
                        addToken(TokenType.PREFIX_INCREMENT);
                    }
                } else if (matchNext('=')) {
                    addToken(TokenType.PLUS_EQUALS);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;
            case '-':
                if (matchNext('-')) {
                    var previousToken = tokens.get(tokens.size() - 1);
                    if (previousToken.type == TokenType.IDENTIFIER && !previousToken.isFollowedByLineBreak) {
                        addToken(TokenType.POSTFIX_DECREMENT);
                    } else {
                        addToken(TokenType.PREFIX_DECREMENT);
                    }
                } else if (matchNext('=')) {
                    addToken(TokenType.MINUS_EQUALS);
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '*':
                if (matchNext('*')) {
                    if (matchNext('=')) {
                        addToken(TokenType.POW_EQUALS);
                    } else {
                        addToken(TokenType.POW);
                    }
                } else if (matchNext('=')) {
                    addToken(TokenType.MULTIPLY_EQUALS);
                } else {
                    addToken(TokenType.MULTIPLY);
                }
                break;
            case '/':
                if (matchNext('/')) {
                    while (peek() != '\n' && !isAtEnd()) nextChar();
                } else if (matchNext('=')) {
                    addToken(TokenType.DIVIDE_EQUALS);
                } else if (matchNext('*')) {
                    while (peek() != '*' || peek(1) != '/') {
                        if (isAtEnd()) {
                            throw new Exception("Expected end of a comment block but reached the end of the file (line " + currentLine + "})");
                        } else {
                            c = nextChar();

                            if (c == '\n') {
                                currentLine++;
                                currentLineStartIndex = currentPosition;
                            }
                        }
                    }

                    matchNext('*');
                    matchNext('/');
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;

            case '<':
                if (matchNext('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    addToken(TokenType.LESS);
                }
                break;
            case '>':
                if (matchNext('=')) {
                    addToken(TokenType.GREATER_EQUAL);
                } else {
                    addToken(TokenType.GREATER);
                }
                break;

            case '%':
                if (matchNext('=')) {
                    addToken(TokenType.REMAINDER_EQUALS);
                } else {
                    addToken(TokenType.REMAINDER);
                }
                break;
            case '&':
                if (matchNext('&')) {
                    addToken(TokenType.LOGICAL_AND);
                } else {
                    addToken(TokenType.BITWISE_AND);
                }
                break;
            case '|':
                if (matchNext('|')) {
                    addToken(TokenType.LOGICAL_OR);
                } else {
                    addToken(TokenType.BITWISE_OR);
                }
                break;

            // Handle whitespace
            case ' ':
                previous = currentPosition;
                break;
            case '\r':
            case '\t':
                // Ignore whitespace
                break;

            case '\n':
                currentLine++;
                currentLineStartIndex = currentPosition;

                if (!tokens.isEmpty()) {
                    tokens.get(tokens.size() - 1).isFollowedByLineBreak = true;
                }
                break;

            case '\'':
                processString('\'');
                break;

            case '"':
                processString('"');
                break;

            case '`':
                processString('`');
                break;

            default:
                if (charIsDigit(c)) {
                    processNumber();
                } else if (charIsAlpha(c)) {
                    processIdentifier();
                } else {
                    System.err.println("Unexpected character: " + c + " at line " + currentLine);
                }
        }

    }

    private Boolean isAtEnd() {
        return currentPosition >= source.length();
    }

    private char nextChar() {
        return sourceChars[currentPosition++];
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int peekAhead) {
        if (isAtEnd()) return '\0';
        return sourceChars[currentPosition + peekAhead];
    }

    private Boolean matchNext(char expected) {
        if (peek() == expected) {
            nextChar();
            return true;
        } else {
            return false;
        }
    }

    private Boolean charIsDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Boolean charIsAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private Boolean charIsAlphaNumeric(char c) {
        return charIsAlpha(c) || charIsDigit(c);
    }

    private void processNumber() {
        while (charIsDigit(peek())) nextChar();

        if (peek() == '.' && charIsDigit(peek(1))) {
            nextChar();

            while (charIsDigit(peek())) nextChar();
        }

        String numberString = source.substring(startOfToken, currentPosition);
        addToken(TokenType.NUMBER, Double.parseDouble(numberString));
    }

    private void processIdentifier() {
        while (charIsAlphaNumeric(peek())) nextChar();

        String identifier = source.substring(startOfToken, currentPosition);
        TokenType type = keywords.getOrDefault(identifier, TokenType.IDENTIFIER);
        addToken(type);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(startOfToken, currentPosition);
        tokens.add(new Token(type, lexeme, literal, currentLine, startOfToken - currentLineStartIndex + 1, previous, currentPosition));
        previous = currentPosition;
    }


    private void processString(char quoteChar) throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean hasProducedAtLeastOneToken = false; // We use this to know whether we need to inject a + before each new token in a string literal that might contain embedded ${x} expressions

        while (peek() != quoteChar && !isAtEnd()) {
            if (quoteChar == '`' && peek() == '\n') // `backtick` strings allow line breaks
            {
                currentLine++;
            } else if (matchNext('\n')) {
                throw new Exception("Unexpected Line break in string (line " + currentLine + ")");
            }

            if (peek() == '\\') {
                var next = peek(1);

                if (next == '\'' || next == '"' || next == '\\' || next == '{') {
                    nextChar();
                    sb.append(nextChar());
                } else if (next == 't') {
                    nextChar();
                    nextChar();
                    sb.append('\t');
                } else if (next == 'r') {
                    nextChar();
                    nextChar();
                    sb.append('\r');
                } else if (next == 'n') {
                    nextChar();
                    nextChar();
                    sb.append('\n');
                } else {
                    sb.append(nextChar());
                }
            } else {
                if (quoteChar == '`' & peek() == '$' && peek(1) == '{') {
                    // We've just entered a ${...} section, so whatever we've got so far, create
                    // a string token and add it to the stream, and start a new string part before
                    // extracting the actual expression and making it look like it was concatenated
                    // (e.g., `a${b}` becomes "a" + (b).toString()

                    if (!sb.isEmpty()) {
                        if (hasProducedAtLeastOneToken) {
                            addToken(TokenType.PLUS); // Concatenate the accumulated string with previously extracted string or expression values
                        }

                        addToken(TokenType.STRING, sb.toString());
                        sb = new StringBuilder();
                        hasProducedAtLeastOneToken = true;
                    }

                    // Now we'll loop through collecting whatever is inside the ${}

                    nextChar();
                    nextChar();

                    StringBuilder embeddedExpr = new StringBuilder();

                    boolean inEmbeddedString = false;
                    Character embeddedStringChar = null;

                    while ((peek() != '}' || inEmbeddedString) && !isAtEnd()) {
                        if ((embeddedStringChar == null && (peek() == '\'' || peek() == '"'))
                                || embeddedStringChar != null && peek() == embeddedStringChar) // Also `
                        {
                            embeddedStringChar = peek();
                            inEmbeddedString = !inEmbeddedString;
                        }

                        embeddedExpr.append(nextChar());
                    }

                    nextChar();

                    if (!embeddedExpr.isEmpty()) {
                        // We've just extracted the contents inside the ${}.
                        // Now we create a new scanner and pass it that string and
                        // get back tokens. We will wrap those in parens and, if we've already
                        // generated at least one token so far, we insert a + to concat them.

                        if (hasProducedAtLeastOneToken) {
                            addToken(TokenType.PLUS); // Concatenate this expression with previously extracted string or expression values
                        }

                        Scanner embeddedScanner = new Scanner(embeddedExpr.toString());

                        var embeddedTokens = embeddedScanner.scanTokens();

                        // Special grouping tokens used to ensure that the embedded expression is cast to string at runtime.
                        // Without this `${a}${b}` becomes a+b, and if a and b are numbers they get added as numbers. Using
                        // these tags we actually produce (a).toString() + (b).toString() when parsing.

                        addToken(TokenType.START_OF_EMBEDDED_STRING_EXPRESSION);

                        for (var t : embeddedTokens) {
                            if (t.type == TokenType.EOF) {
                                break;
                            }

                            this.tokens.add(t);
                        }

                        addToken(TokenType.END_OF_EMBEDDED_STRING_EXPRESSION);

                        hasProducedAtLeastOneToken = true;
                    }

                } else {
                    sb.append(nextChar());
                }


            }
        }

        if (isAtEnd())
        {
            throw new Exception("Unterminated string (line "+currentLine+")");
        }

        // Consume the closing quote
        nextChar();

        if (!sb.isEmpty() || !hasProducedAtLeastOneToken) // If we haven't produced a token yet, even if it's an empty string, we still need that string token
        {
            if (hasProducedAtLeastOneToken)
            {
                addToken(TokenType.PLUS);
            }

            addToken(TokenType.STRING, sb.toString());
        }
    }
}