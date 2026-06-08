package org.smolscript.internals.variableTypes;

import java.util.ArrayList;
import java.util.List;

public class SmolFunction extends SmolVariableType {

    public String globalFunctionName;
    public int codeSection;
    public int arity;
    public List<String> parameterVariableNames;

    public SmolFunction(String globalFunctionName, int codeSection, int arity, List<String> parameterVariableNames)
    {
        this.globalFunctionName = globalFunctionName;
        this.codeSection = codeSection;
        this.arity = arity;
        this.parameterVariableNames = parameterVariableNames;
    }

    @Override
    public Object GetValue() {
        return this;
    }
}
