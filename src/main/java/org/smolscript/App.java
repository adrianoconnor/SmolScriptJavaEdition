package org.smolscript;

import org.smolscript.internals.Parser;
import org.smolscript.internals.Scanner;
import org.smolscript.internals.Token;
import org.smolscript.internals.ast.AstDump;

public class App {
    public static void main(String[] args) throws Exception {
        var sx = new Scanner("""
                    function test(name) {
                        return `hi ${name}`
                    }
                    var x = 10;
                    x = y + z;
                    x++;
                    test("monkey");
                """);

        var s = new Scanner("""
                    var x = 10;
                    x = x + y;
                    test(x,y);;;;
                    goopy.baby = true;
                    b = goopy.age;
                """);
        var tokens = s.scanTokens();

        for (var t : tokens) {
            System.out.println(t);
        }

        var p = new Parser(tokens.toArray(new Token[0]));

        var ast = p.parse();

        var dump = new AstDump();

        System.out.println(dump.print(ast));
    }
}
