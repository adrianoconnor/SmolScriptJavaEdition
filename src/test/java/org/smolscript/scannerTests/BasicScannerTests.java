package org.smolscript.scannerTests;

import org.junit.jupiter.api.Test;
import org.smolscript.internals.Scanner;
import org.smolscript.internals.TokenType;

import static org.junit.jupiter.api.Assertions.*;

public class BasicScannerTests {

    @Test
    public void scannerProducesExpectedTokens_SimpleVarStatement() {

        var source = "var a"; // This is a whole program :)

        try {
            var tokens = Scanner.scan(source);

            assertEquals(3, tokens.size());
            assertEquals(TokenType.VAR, tokens.get(0).type);
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
            assertEquals(TokenType.EOF, tokens.get(2).type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void scanSourceWithError() {

        var source = "var a = '"; // Strings must be closed on the same line

        Exception thrown = assertThrows(
                Exception.class, // TODO: Add a proper ScannerError (or CompilerError, tbc)
                () -> Scanner.scan(source),
                "Expected: Unterminated string (line 1)" // This doesn't seem to do what I expect...
        );

        assertTrue(thrown.getMessage().contains("Unterminated string (line 1)"));
    }

    @Test
    public void scannerProcessesSource_LotsOfTokenTypes() {

        var source = """
/* Comment */
// Comment
var number = 213; // Comment
var string_1 = 'Test 1';
var string_2 = "Test 1";
var string_3 = `Test ${number}`;

function x(p1) {
    return;
}

function y(p2) {
    return -1;
}

function z() {
}

class Test {
    constructor() {
        x = 1;
    }
}
        """;

        try {
            var tokens = Scanner.scan(source);

            assertEquals(64, tokens.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
