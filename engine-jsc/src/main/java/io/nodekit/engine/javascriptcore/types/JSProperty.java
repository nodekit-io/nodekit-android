package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.Nullable;


public final class JSProperty<T> extends JSValue
{
    private final Class type;

    public JSProperty(JSContext context, Class type)
    {
        super(context);
        this.type = type;
    }

    public JSProperty(JSContext context, T value)
    {
        super(context, value);
        type = value.getClass();
    }

    public void set(T value)
    {
        setValue(value);
    }

    /**
     * @return the current property value. Or null, if:
     * 1. the generic type is incompatible with the type of this property in JS
     * 2. property in JS is either null, or undefined
     */
    @Nullable
    public T get()
    {
        if (jsvalueIsNull() || jsvalueIsUndefined())
            return null;

        Object value = null;
        if (jsvalueIsNumber())
        {
            if (type == Integer.class)
                value = jsvalueToNumber().intValue();
            else if (type == Float.class)
                value = jsvalueToNumber().floatValue();
            else if (type == Double.class)
                value = jsvalueToNumber();
            else if (type == Long.class)
                value = jsvalueToNumber().longValue();
            else if (type == Short.class)
                value = jsvalueToNumber().shortValue();
        }
        else if (type == Boolean.class && jsvalueIsBoolean())
            value = jsvalueToBoolean();
        else if (type == String.class && jsvalueIsString())
            value = toString();
        else if (type == JSObject.class && jsvalueIsObject())
            value = jsvalueToObject();
        else if (type == JSFunction.class && jsvalueOrObjectIsFunction())
            value = jsvalueToFunction();
        else if (type == JSArray.class && isArray())
            value = jsvalueToJSArray();

        return (T) value;
    }
}
