package org.smolscript.internals;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public final List<ByteCodeInstruction> instructions = new ArrayList<ByteCodeInstruction>();

    public Chunk() {

    }

    public Chunk(OpCode opcode) {
        instructions.add(new ByteCodeInstruction(opcode));
    }

    public Chunk(OpCode opcode, Object operand1) {
        instructions.add(new ByteCodeInstruction(opcode, operand1));
    }

    public void mapTokens(Number firstTokenIndex, Number lastTokenIndex) {
        for(var instr : instructions)
        {
            if (instr.tokenMapStartIndex == null)
            {
                instr.tokenMapStartIndex = firstTokenIndex;
                instr.tokenMapEndIndex = lastTokenIndex != null ? lastTokenIndex : firstTokenIndex;
            }
        }
    }

    public ByteCodeInstruction getLast() {
        return instructions.get(instructions.size() - 1);
    }

    public void appendInstruction(OpCode opcode) {
        instructions.add(new ByteCodeInstruction(opcode, null, null));
    }

    public void appendInstruction(OpCode opcode, Object operand1) {
        instructions.add(new ByteCodeInstruction(opcode, operand1, null));
    }

    public void appendInstruction(OpCode opcode, Object operand1, Object operand2) {
        instructions.add(new ByteCodeInstruction(opcode, operand1, operand2));
    }

    public void appendChunk(Chunk chunk) {
        instructions.addAll(chunk.instructions);
    }
}
