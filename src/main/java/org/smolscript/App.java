package org.smolscript;

import org.smolscript.internals.Compiler;
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

        var source = """
                    var x = 10;
                    x = x + 1;
                    var z = 'moo';
                    print(x);
                    function test(i) {
                        return;
                    }
                    a = test(9);
                """;

        var s = new Scanner(source);
        var tokens = s.scanTokens();

        for (var t : tokens) {
            System.out.println(t);
        }

        var p = new Parser(tokens.toArray(new Token[0]));

        var ast = p.parse();

        var dump = new AstDump();

        System.out.println(dump.print(ast));

        var prg = Compiler.Compile(source);

        System.out.println("Constants:");
        for (var c : prg.constants) {
            System.out.println(c);
        }
        System.out.println("(End of constants)");

        for(int i = 0; i < prg.codeSections.size(); i++) {
            System.out.println("Code section " + i + ":");
            for (var instr : prg.codeSections.get(i).instructions) {
                System.out.println(instr);
            }
            System.out.println("(End of code section " + i + ")");
        }
    }
}
