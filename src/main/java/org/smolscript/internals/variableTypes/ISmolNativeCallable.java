package org.smolscript.internals.variableTypes;

public interface ISmolNativeCallable {

    public SmolVariableType GetProp(String propName);

    public void SetProp(String propName, SmolVariableType value);

    public SmolVariableType NativeCall(String funcName, SmolVariableType[] parameters);

    public static SmolVariableType StaticCall(String funcName, SmolVariableType[] parameters) {
        return null;
    }
}
