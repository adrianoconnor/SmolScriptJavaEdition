package org.smolscript;

public interface ISmolRuntime
{
    /// <summary>
    /// Static method that takes a smol source string
    /// and compiles the program but does not execute
    /// any code
    /// </summary>
    /// <param name="sourceCode"></param>
    /// <returns>A SmolVM instance</returns>
    public static ISmolRuntime compile(String sourceCode) {
        return null;
    }

    /// <summary>
    /// Static method that takes a smol source string,
    /// compiles it and then immediately executes the
    /// top level statements, preparing the global
    /// environment and making it ready to call from .net
    /// </summary>
    /// <param name="sourceCode"></param>
    /// <returns>A SmolVM instance</returns>
    public static ISmolRuntime init(String sourceCode) {
        return null;
    }


    /// <summary>
    /// Set a limit on the maximum stack size for the smol vm,
    /// constraining the amount of resouces a smol program can
    /// consume
    /// </summary>
    public void setMaxStackSize(int maxStackSize);

    /// <summary>
    /// Sets a limit on the number of cycles in the VM that a single program execution
    /// is allowed to consume. Prevents infinite loops etc.
    /// </summary>
    public void setMaxCycleCount(int maxCycleCount);


    /// <summary>
    /// Retrieve the value of a global variable from the VM after execution
    /// </summary>
    /// <typeparam name="T">The generic type to cast to</typeparam>
    /// <param name="variableName">The name of the variable to get</param>
    /// <returns>The value of the variable. If the variable is not defined returns null for nullable types, and throws if not nullable</returns>
    public Object getGlobalVar(String variableName);
    //List GetGlobalVarAsArray<T>(string variableName);

    Object call(String functionName, Object[] ... args);

    //void RegisterMethod(string methodName, object lambda);

    void run();
    void reset();
    void step();

    //Action<string> OnDebugLog { set; }

    String decompile();
}
