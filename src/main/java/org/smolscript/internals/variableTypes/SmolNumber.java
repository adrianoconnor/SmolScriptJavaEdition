package org.smolscript.internals.variableTypes;

public class SmolNumber extends SmolVariableType implements ISmolNativeCallable {

    private double numberValue;

    public SmolNumber(double numberValue) {
        this.numberValue = numberValue;
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
