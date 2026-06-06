package org.smolscript.internals.variableTypes;

import org.smolscript.internals.stackTypes.SmolStackType;

public abstract class SmolVariableType extends SmolStackType {

    public abstract Object GetValue();

    protected String getTypeName()
    {
        return this.getClass().getName().replace("Smol", "");
    }

    protected static SmolVariableType Create(Object value) throws Exception {

        if (value == null)  {
            return new SmolNull();
        }
        else if (value instanceof SmolVariableType castValue) {
            return castValue;
        }
        else if (value instanceof String castValue)
        {
            return new SmolString(castValue);
        }
        else if (value instanceof Number castValue)
        {
            return new SmolNumber(castValue.doubleValue());
        }
        else if (value instanceof Boolean castValue)
        {
            return new SmolBool(castValue);
        }

        throw new Exception("Could not create a valid SmolVariable object from {value.GetType()}");
    }
/*
    public static SmolVariableType operator +(SmolVariableType a, SmolVariableType b)
    {
        // Currently only handles really simple case of both values
        // being numeric -- anything else will raise an exception. Next
        // to implement is string handling...

        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = ((SmolNumber)a).NumberValue;
            var right = ((SmolNumber)b).NumberValue;

            return new SmolNumber(left + right);
        }
        else if (a.GetType() == typeof(SmolString) || b.GetType() == typeof(SmolString))
        {
            // TODO: Need a Stringify helper method.

            var aString = a.GetValue()!.ToString()!;
            var bString = b.GetValue()!.ToString()!;

            return new SmolString(aString + bString);
        }

        throw new Exception($"Unable to add {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator -(SmolVariableType a, SmolVariableType b)
    {
        // Currently only handles really simple case of both values
        // being numeric -- anything else will raise an exception. Next
        // to implement is string handling...

        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left - right);
        }

        throw new Exception($"Unable to subtract {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator *(SmolVariableType a, SmolVariableType b)
    {
        // Currently only handles really simple case of both values
        // being numeric -- anything else will raise an exception. Next
        // to implement is string handling...

        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left * right);
        }

        throw new Exception($"Unable to multiply {a.GetTypeName()} and {b.GetTypeName()}");
    }

    public static SmolVariableType operator /(SmolVariableType a, SmolVariableType b)
    {
        // Currently only handles really simple case of both values
        // being numeric -- anything else will raise an exception. Next
        // to implement is string handling...

        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left / right);
        }

        throw new Exception($"Unable to divide {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator %(SmolVariableType a, SmolVariableType b)
    {
        // Currently only handles really simple case of both values
        // being numeric -- anything else will raise an exception. Next
        // to implement is string handling...

        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left % right);
        }

        throw new Exception($"Unable to modulo {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator >(SmolVariableType a, SmolVariableType b)
    {
        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left > right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator <(SmolVariableType a, SmolVariableType b)
    {
        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left < right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator >=(SmolVariableType a, SmolVariableType b)
    {
        if (a instanceof SmolNumber && b instanceof SmolNumber)
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left >= right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator <=(SmolVariableType a, SmolVariableType b)
    {
        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = (double)a.GetValue()!;
            var right = (double)b.GetValue()!;

            return SmolVariableType.Create(left <= right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator |(SmolVariableType a, SmolVariableType b)
    {
        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = Convert.ToInt64((double)a.GetValue()!);
            var right = Convert.ToInt64((double)b.GetValue()!);

            return SmolVariableType.Create(left | right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public static SmolVariableType operator &(SmolVariableType a, SmolVariableType b)
    {
        if (a.GetType() == typeof(SmolNumber) && b.GetType() == typeof(SmolNumber))
        {
            var left = Convert.ToInt64((double)a.GetValue()!);
            var right = Convert.ToInt64((double)b.GetValue()!);

            return SmolVariableType.Create(left & right);
        }

        throw new Exception($"Unable to compare {a.GetType()} and {b.GetType()}");
    }

    public SmolVariableType Power(SmolVariableType power)
    {
        if (this.GetType() == typeof(SmolNumber) && power.GetType() == typeof(SmolNumber))
        {
            var left = (double)((SmolNumber)this).GetValue()!;
            var right = (double)((SmolNumber)power).GetValue()!;

            return SmolVariableType.Create(Math.Pow(left, right));
        }

        throw new Exception($"Unable to calculate power for");// {this.type} and {power.type}");
    }

    public override bool Equals([NotNullWhen(true)] object? obj)
    {
        if (this.GetType() == typeof(SmolNumber) && obj?.GetType() == typeof(SmolNumber))
        {
            return ((SmolNumber)this).NumberValue.Equals(((SmolNumber)obj!).NumberValue);
        }
            else if (this.GetType() == typeof(SmolString))
    {
        return ((SmolString)this).StringValue == ((SmolString)obj!).StringValue;
    }
    else if (this.GetType() == typeof(SmolUndefined))
    {
        return obj?.GetType() == typeof(SmolUndefined);
    }
    else
    {
        return base.Equals(obj);
    }
    }

    public override int GetHashCode()
    {
        if (this.GetType() == typeof(SmolNumber))
        {
            return ((SmolNumber)this).NumberValue.GetHashCode();
        }
        if (this.GetType() == typeof(SmolString))
        {
            return ((SmolString)this).StringValue.GetHashCode();
        }
        else
        {
            return base.GetHashCode();
        }
    }
    */

    public boolean IsTruthy()
    {
        return (boolean)this.GetValue();
    }

    public boolean IsFalsey()
    {
        return !IsTruthy();
    }

}
