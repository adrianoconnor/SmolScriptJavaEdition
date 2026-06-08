package org.smolscript.internals;

public class ByteCodeInstruction {

    public OpCode opCode;

    public Object operand1;
    public Object operand2;

    // This flag tells us where the debugger should stop if we're stepping through a program
    public Boolean isStatementStartpoint = false;

    // These attributes are used for mapping back to the original source code
    public Number tokenMapStartIndex = null;
    public Number tokenMapEndIndex = null;

    public ByteCodeInstruction(OpCode opCode) {
        this.opCode = opCode;
    }

    public ByteCodeInstruction(OpCode opCode, Object op1) {
        this.opCode = opCode;
        this.operand1 = op1;
    }

    public ByteCodeInstruction(OpCode opCode, Object op1, Object op2) {
        this.opCode = opCode;
        this.operand1 = op1;
        this.operand2 = op2;
    }

    @Override
    public String toString()
    {
        var str = this.opCode.toString(); // In .net we had PadRight 13 but not sure it added anything

        if (this.operand1 != null)
        {
            str += " [op1: " + this.operand1 + "]";
        }

        if (this.operand2 != null)
        {
            str += " [op2: " + this.operand2 + "]";
        }

        return str;
    }
}
