package org.smolscript.internals.variableTypes;

public class SmolBool extends SmolVariableType implements ISmolNativeCallable {

    private boolean boolValue;

    public SmolBool(boolean boolValue) {
        this.boolValue = boolValue;
    }

    @Override
    public SmolVariableType GetProp(String propName) {
        return null;
    }

    @Override
    public void SetProp(String propName, SmolVariableType value) {

    }

    @Override
    public SmolVariableType NativeCall(String funcName, SmolVariableType[] parameters) {
        return null;
    }

    @Override
    public Object GetValue() {
        return null;
    }
}
