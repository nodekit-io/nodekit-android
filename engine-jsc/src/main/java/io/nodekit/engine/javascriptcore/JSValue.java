package io.nodekit.engine.javascriptcore;

import io.nodekit.engine.javascriptcore.types.*;
import io.nodekit.engine.javascriptcore.util.JSException;
import io.nodekit.nkscripting.NKScriptValue;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import android.support.annotation.Nullable;
import android.webkit.ValueCallback;

/**
 * A JavaScript value
 *
 */
public class JSValue {

    protected Long valueRef = 0L;
    protected JSContext context = null;
    protected Boolean isDefunct = false;

    private boolean isProtected = true;

    /* Constructors */

    /**
     * Creates an empty JSValue. This can only be used by subclasses, and those
     * subclasses must define 'context' and 'valueRef' themselves
     */
    public JSValue() {
    }

    /**
     * Creates a new undefined JavaScript value
     *
     * @param ctx The context in which to create the value
     */
    public JSValue(final JSContext ctx) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = NJSmakeUndefined(context.contextRef());
            }
        });
    }

    /**
     * Creates a new JavaScript value from a Java value. Classes supported are:
     * Boolean, Double, Integer, Long, String, and JSString. Any other object will
     * generate an undefined JavaScript value.
     *
     * @param ctx The context in which to create the value
     * @param val The Java value
     *
     */
    @SuppressWarnings("unchecked")
    public JSValue(JSContext ctx, final Object val) {
        context = ctx;
        setValue(val);
    }

    protected void setValue(final Object val) {
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (val == null) {
                    valueRef = NJSmakeNull(context.contextRef());
                } else if (val instanceof JSValue) {
                    valueRef = ((JSValue) val).valueRef();
                    NJSprotect(context.contextRef(), valueRef);
                } else if (val instanceof Map) {
                    valueRef = new JSObjectPropertiesMap(context, (Map) val, Object.class).getJSObject().valueRef();
                    NJSprotect(context.contextRef(), valueRef);
                } else if (val instanceof List) {
                    valueRef = new JSArray<>(context, (List) val, JSValue.class).valueRef();
                    NJSprotect(context.contextRef(), valueRef);
                } else if (val.getClass().isArray()) {
                    valueRef = new JSArray<>(context, (Object[]) val, JSValue.class).valueRef();
                    NJSprotect(context.contextRef(), valueRef);
                } else if (val instanceof Boolean) {
                    valueRef = NJSmakeBoolean(context.contextRef(), (Boolean) val);
                } else if (val instanceof Double) {
                    valueRef = NJSmakeNumber(context.contextRef(), (Double) val);
                } else if (val instanceof Float) {
                    valueRef = NJSmakeNumber(context.contextRef(), Double.valueOf(val.toString()));
                } else if (val instanceof Integer) {
                    valueRef = NJSmakeNumber(context.contextRef(), ((Integer) val).doubleValue());
                } else if (val instanceof Long) {
                    valueRef = NJSmakeNumber(context.contextRef(), ((Long) val).doubleValue());
                } else if (val instanceof Byte) {
                    valueRef = NJSmakeNumber(context.contextRef(), ((Byte) val).doubleValue());
                } else if (val instanceof Short) {
                    valueRef = NJSmakeNumber(context.contextRef(), ((Short) val).doubleValue());
                } else if (val instanceof String) {
                    JSString s = new JSString((String) val);
                    valueRef = NJSmakeString(context.contextRef(), s.stringRef);
                } else {
                    valueRef = NJSmakeUndefined(context.contextRef());
                }
            }
        });
    }

    /**
     * Wraps an existing JavaScript value
     *
     * @param valueRef The JavaScriptCore reference to the value
     * @param ctx      The context in which the value exists
     *
     */
    public JSValue(final long valueRef, JSContext ctx) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (valueRef == 0) {
                    JSValue.this.valueRef = NJSmakeUndefined(context.contextRef());
                } else {
                    JSValue.this.valueRef = valueRef;
                    NJSprotect(context.contextRef(), valueRef);
                }
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        jsvalueUnprotect();
    }

    /* Testers */

    /**
     * Tests whether the value is undefined
     *
     * @return true if undefined, false otherwise
     *
     */
    public Boolean jsvalueIsUndefined() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisUndefined(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is null
     *
     * @return true if null, false otherwise
     *
     */
    public Boolean jsvalueIsNull() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisNull(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is boolean
     *
     * @return true if boolean, false otherwise
     *
     */
    public Boolean jsvalueIsBoolean() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisBoolean(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is a number
     *
     * @return true if a number, false otherwise
     *
     */
    public Boolean jsvalueIsNumber() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisNumber(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is a string
     *
     * @return true if a string, false otherwise
     *
     */
    public Boolean jsvalueIsString() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisString(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is an array
     *
     * @return true if an array, false otherwise
     *
     */
    public Boolean isArray() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisArray(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is a date object
     *
     * @return true if a date object, false otherwise
     *
     */
    public Boolean jsvalueIsDate() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisDate(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether the value is an object
     *
     * @return true if an object, false otherwise
     *
     */
    public Boolean jsvalueIsObject() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisObject(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Tests whether a value in an instance of a constructor object
     *
     * @param constructor The constructor object to test
     * @return true if the value is an instance of the given constructor object,
     *         false otherwise
     *
     */
    public Boolean jsvalueIsInstanceOfConstructor(final JSObject constructor) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJSisInstanceOfConstructor(context.contextRef(), valueRef, constructor.valueRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            runnable.jni.bool = false;
        }
        return runnable.jni.bool;
    }

    /* Comparators */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object other) {
        return jsvalueIsEqual(other);
    }

    /**
     * JavaScript definition of equality (==). JSValue.equals() and
     * JSValue.jsvalueIsEqual() represent the Java and JavaScript definitions,
     * respectively. Normally they will return the same value, however some classes
     * may override and offer different results. Example, in JavaScript, new
     * Float32Array([1,2,3]) == new Float32Array([1,2,3]) will be false (as the
     * equality is only true if they are the same physical object), but from a Java
     * util.java.List perspective, these two are equal.
     *
     * @param other the value to compare for equality
     * @return true if == from JavaScript perspective, false otherwise
     *
     */
    public boolean jsvalueIsEqual(Object other) {
        if (other == this)
            return true;
        JSValue otherJSValue;
        if (other instanceof JSValue) {
            otherJSValue = (JSValue) other;
        } else {
            otherJSValue = new JSValue(context, other);
        }
        final JSValue ojsv = otherJSValue;
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJSisEqual(context.contextRef(), valueRef, ojsv.valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.exception == 0 && runnable.jni.bool;
    }

    /**
     * Tests whether two values are strict equal. In JavaScript, equivalent to '==='
     * operator.
     *
     * @param other The value to test against
     * @return true if values are strict equal, false otherwise
     *
     */
    public boolean jsvalueIsStrictEqual(Object other) {
        if (other == this)
            return true;
        JSValue otherJSValue;
        if (other instanceof JSValue) {
            otherJSValue = (JSValue) other;
        } else {
            otherJSValue = new JSValue(context, other);
        }
        final JSValue ojsv = otherJSValue;
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisStrictEqual(context.contextRef(), valueRef, ojsv.valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /* Getters */

    /**
     * Gets the Boolean value of this JS value
     *
     * @return the Boolean value
     *
     */
    public Boolean jsvalueToBoolean() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJStoBoolean(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Gets the numeric value of this JS value
     *
     * @return The numeric value
     *
     */
    public Double jsvalueToNumber() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJStoNumber(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return 0.0;
        }
        return runnable.jni.number;
    }

    /**
     * Gets the JSString value of this JS value
     *
     * @return The JSString value
     *
     */
    protected JSString jsvalueToJSString() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJStoStringCopy(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return null;
        }
        return new JSString(runnable.jni.reference);
    }

    /**
     * If the JS value is an object, gets the JSObject
     *
     * @return The JSObject for this value
     *
     */
    public JSObject jsvalueToObject() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJStoObject(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSObject(context);
        }
        return context.getObjectFromRef(runnable.jni.reference);
    }

    /**
     * If the JS value is a function, gets the JSFunction
     *
     * @return The JSFunction for this value
     *
     */
    public JSFunction jsvalueToFunction() {

        if (jsvalueOrObjectIsFunction()) {
            return (JSFunction) jsvalueToObject();
        } else {
            context.throwJSException(new JSException(context, "JSObject not a function"));
            return null;
        }
    }

    public boolean jsvalueOrObjectIsFunction() {
        return jsvalueIsObject() && jsvalueToObject() instanceof JSFunction;
    }

    /**
     * If the JS value is an array, gets the JSArray
     *
     * @return The JSArray for this value
     *
     */
    public JSBaseArray jsvalueToJSArray() {
        if (jsvalueIsObject() && jsvalueToObject() instanceof JSBaseArray) {
            return (JSBaseArray) jsvalueToObject();
        } else if (!jsvalueIsObject()) {
            jsvalueToObject();
            return null;
        } else {
            context.throwJSException(new JSException(context, "JSObject not an array"));
            return null;
        }
    }

    /**
     * Gets the JSON of this JS value
     *
     * @param indent number of spaces to indent
     * @return the JSON representing this value, or null if value is undefined
     *
     */
    public String jsvalueToJSON(final int indent) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJScreateJSONString(context.contextRef(), valueRef, indent);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return null;
        }
        if (runnable.jni.reference == 0) {
            return null;
        }
        return new JSString(runnable.jni.reference).toString();
    }

    /**
     * Gets the JSON of this JS value
     *
     * @return the JSON representing this value
     *
     */
    public String jsvalueToJSON() {
        return jsvalueToJSON(0);
    }

    public Object jsvalueToJavaObject() {
       if (isArray()) {
           return jsvalueToJSArray().toArray();
       }
        else if (jsvalueIsString())
            return toString();
        else if (jsvalueIsNumber())
            return jsvalueToNumber();
        else if (jsvalueIsBoolean())
            return jsvalueToBoolean();
        else if (jsvalueOrObjectIsFunction())
        {
            return null; // jsvalueToFunction();
        } else   if (jsvalueIsObject())
        {
            return new JSObjectPropertiesMap(jsvalueToObject(), Object.class).toMap();
        }
        else
        {
            return null;
        }
            
    }

    @SuppressWarnings("unchecked")
    public Object jsvalueToJavaObject(Class clazz) {
        if (clazz == Object.class)
            return jsvalueToJavaObject();
        else if (clazz == Map.class && jsvalueIsObject())
            return new JSObjectPropertiesMap(jsvalueToObject(), Object.class).toMap();
        else if (clazz == List.class && isArray())
            return jsvalueToJSArray().toArray();
        else if (clazz == String.class && jsvalueIsString())
            return toString();
        else if ((clazz == Number.class || clazz == Double.class || clazz == double.class) && jsvalueIsNumber())
            return jsvalueToNumber();
        else if ((clazz == Float.class || clazz == float.class) && jsvalueIsNumber())
            return jsvalueToNumber().floatValue();
        else if ((clazz == Integer.class || clazz == int.class) && jsvalueIsNumber())
            return jsvalueToNumber().intValue();
        else if ((clazz == Long.class || clazz == long.class) && jsvalueIsNumber())
            return jsvalueToNumber().longValue();
        else if ((clazz == Byte.class || clazz == byte.class) && jsvalueIsNumber())
            return jsvalueToNumber().byteValue();
        else if ((clazz == Short.class || clazz == short.class) && jsvalueIsNumber())
            return jsvalueToNumber().shortValue();
        else if ((clazz == Boolean.class || clazz == boolean.class) && jsvalueIsBoolean())
            return jsvalueToBoolean();
        else if (NKScriptValue.class.isAssignableFrom(clazz) && jsvalueOrObjectIsFunction())
            return jsvalueToFunction();
        else if (clazz.isArray()) {
            Class itemClass = clazz.getComponentType();
            if (isArray())
                return jsvalueToJSArray().toArray(itemClass);
            else {
                Object arr = Array.newInstance(itemClass, 1);
                Array.set(arr, 0, jsvalueToJavaObject(itemClass));
                return arr;
            }
        } else if (JSObject.class.isAssignableFrom(clazz) && jsvalueIsObject())
            return clazz.cast(jsvalueToObject());
        else if (JSValue.class.isAssignableFrom(clazz))
            return clazz.cast(this);
        return null;
    }

    @Override
    public String toString() {
        return jsvalueToJSString().toString();
    }

    @Override
    public int hashCode() {
        if (jsvalueIsBoolean())
            return jsvalueToBoolean().hashCode();
        else if (jsvalueIsNumber())
            return jsvalueToNumber().hashCode();
        else if (jsvalueIsString())
            return this.toString().hashCode();
        else if (jsvalueIsUndefined() || jsvalueIsNull())
            return 0;
        else
            return super.hashCode();
    }

    /**
     * Gets the JSContext of this value
     *
     * @return the JSContext of this value
     *
     */
    public JSContext getContext() {
        return context;
    }

    /**
     * Gets the JavaScriptCore value reference
     *
     * @return the JavaScriptCore value reference
     *
     */
    public Long valueRef() {
        return valueRef;
    }

    protected void jsvalueUnprotect() {
        if (isProtected && !context.isDefunct)
            context.markForUnprotection(valueRef());
        isProtected = false;
    }

    /* Native functions */
    @SuppressWarnings("unused")
    protected native int NJSgetType(long contextRef, long valueRef);

    protected native boolean NJSisUndefined(long contextRef, long valueRef);

    protected native boolean NJSisNull(long contextRef, long valueRef);

    protected native boolean NJSisBoolean(long contextRef, long valueRef);

    protected native boolean NJSisNumber(long contextRef, long valueRef);

    protected native boolean NJSisString(long contextRef, long valueRef);

    protected native boolean NJSisObject(long contextRef, long valueRef);

    protected native boolean NJSisArray(long contextRef, long valueRef);

    protected native boolean NJSisDate(long contextRef, long valueRef);

    protected native JNIReturnObject NJSisEqual(long contextRef, long a, long b);

    protected native boolean NJSisStrictEqual(long contextRef, long a, long b);

    protected native JNIReturnObject NJSisInstanceOfConstructor(long contextRef, long valueRef, long constructor);

    protected native long NJSmakeUndefined(long ctx);

    protected native long NJSmakeNull(long ctx);

    protected native long NJSmakeBoolean(long ctx, boolean bool);

    protected native long NJSmakeNumber(long ctx, double number);

    protected native long NJSmakeString(long ctx, long stringRef);

    protected native long NJSmakeFromJSONString(long ctx, long stringRef);

    protected native JNIReturnObject NJScreateJSONString(long contextRef, long valueRef, int indent);

    // protected native long NJSrelease(long ctx);

    protected native boolean NJStoBoolean(long ctx, long valueRef);

    protected native JNIReturnObject NJStoNumber(long contextRef, long valueRef);

    protected native JNIReturnObject NJStoStringCopy(long contextRef, long valueRef);

    protected native JNIReturnObject NJStoObject(long contextRef, long valueRef);

    protected native void NJSprotect(long ctx, long valueRef);

    protected native void NJSunprotect(long ctx, long valueRef);

    protected native void NJSsetException(long valueRef, long exceptionRefRef);

    /**
     * Used in communicating with JavaScriptCore JNI. Clients do not need to use
     * this.
     */
    public static class JNIReturnObject {
        /**
         * The boolean return value
         */
        public boolean bool;
        /**
         * The numeric return value
         */
        public double number;
        /**
         * The reference return value
         */
        public long reference;
        /**
         * The exception reference if one was thrown, otherwise 0L
         */
        public long exception;
    }

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }
}
