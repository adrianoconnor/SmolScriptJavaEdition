package org.smolscript.internals;

public class ParseError extends Exception {
    Token token;

    public ParseError(Token token, String message) {
        super(message);

        this.token = token;
    }
}
