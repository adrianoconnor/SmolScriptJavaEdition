package org.smolscript.internals;

public class Token {

    public final TokenType type;
    public final String lexeme;
    public final Object literal;

    public final int line;
    public final int col;
    public final int startPosition;
    public final int endPosition;

    public Boolean isFollowedByLineBreak = false;

    public Token(TokenType type, String lexeme, Object literal, int line, int col, int startPosition, int endPosition)
    {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;

        this.line = line;
        this.col = col;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return "Token: " + this.type + ", " + this.lexeme + ", " + this.literal;
    }
}
