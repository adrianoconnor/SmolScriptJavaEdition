package org.smolscript.internals.variableTypes;

public class SmolUndefined extends SmolVariableType {
    @Override
    public Object GetValue() {
        return null;
    }

    @Override
    public String toString() {
        return "Undefined";
    }
}
