package org.smolscript.internals.variableTypes;

public class SmolString extends SmolVariableType implements ISmolNativeCallable {

    private String stringValue;

    public SmolString(String stringValue) {
        this.stringValue = stringValue;
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
