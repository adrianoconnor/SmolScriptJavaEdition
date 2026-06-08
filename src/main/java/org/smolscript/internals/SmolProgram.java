package org.smolscript.internals;

import org.smolscript.internals.variableTypes.*;

import java.util.HashMap;
import java.util.List;

public class SmolProgram {

    public final List<SmolVariableType> constants;
    public final List<Chunk> codeSections;
    public final List<SmolFunction> functionTable;

    public final List<Token> tokens;
    public final String sourceCode;

    public final HashMap<Integer, Integer> jumpTable = new HashMap<>();

    public SmolProgram(List<SmolVariableType> constants, List<Chunk> codeSections, List<SmolFunction> functionTable, List<Token> tokens, String sourceCode) {
        this.constants = constants;
        this.codeSections = codeSections;
        this.functionTable = functionTable;
        this.tokens = tokens;
        this.sourceCode = sourceCode;
    }

    public void buildJumpTable() {
        // Loop through all labels in all code sections, capturing
        // the label number (always unique) and the location/index
        // in the instructions for that section so we can jump

        for (Chunk codeSection : this.codeSections) {
            // Not sure if this will hold up, might be too simplistic

            for (int i = 0; i < codeSection.instructions.size(); i++) {
                var instr = codeSection.instructions.get(i);

                // We're not storing anything about the section
                // number but this should be ok becuase we should
                // only ever jump inside the current section...
                // Jumps to other sections are handled in a different
                // way using the CALL instruction
                if (instr.opCode == OpCode.LABEL) {
                    jumpTable.put((Integer) instr.operand1, i);
                }
            }
        }
    }
}
