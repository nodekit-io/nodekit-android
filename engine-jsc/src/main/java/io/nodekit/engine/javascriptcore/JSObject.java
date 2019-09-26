package io.nodekit.engine.javascriptcore;

import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.engine.javascriptcore.types.JSObjectPropertiesMap;
import io.nodekit.engine.javascriptcore.util.JSException;

import android.webkit.ValueCallback;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A JavaScript object.
 */
public class JSObject extends JSValue implements NKScriptValue {

    //
    // NKScriptValue interface
    //

    public void invokeMethod(String method, Object[] arguments, ValueCallback<String> completionHandler) {
        JSValue result = property(method).jsvalueToFunction().apply(null, arguments);
        completionHandler.onReceiveValue(result.toString());
    }

    public void invokeMethod(String method, Object[] arguments) {
        property(method).jsvalueToFunction().apply(null, arguments);
    }

    public void defineProperty(String key, Object value) {
        property(key, value);
    }

    public void deleteProperty(String key) {
        jsobjectDeleteProperty(key);
    }

    public void hasProperty(String key, ValueCallback<String> completionHandler) {
        boolean result = jsobjectHasProperty(key);
        completionHandler.onReceiveValue(result ? "true" : null);
    }

    public void valueForProperty(String property, ValueCallback<String> completionHandler) {
        JSValue result = property(property);
        completionHandler.onReceiveValue(result.toString());
    }

    public  void setValue(Object value, String forProperty) {
        property(forProperty, value);
    }


    protected JSFunction isInstanceOf = null;

    private JSObject thiz = null;

    protected final List<JSObject> zombies = new ArrayList<>();

    /**
     * Specifies that a property has no special attributes.
     */
    public static int JSPropertyAttributeNone = 0;
    /**
     * Specifies that a property is read-only.
     */
    public static int JSPropertyAttributeReadOnly = 1 << 1;
    /**
     * Specifies that a property should not be enumerated by
     * JSPropertyEnumerators and JavaScript for...in loops.
     */
    public static int JSPropertyAttributeDontEnum = 1 << 2;
    /**
     * Specifies that the delete operation should fail on a property.
     */
    public static int JSPropertyAttributeDontDelete = 1 << 3;

    /**
     * Creates a new, empty JavaScript object.  In JS:
     * <pre>
     * {@code
     * var obj = {}; // OR
     * var obj = new Object();
     * }
     * </pre>
     *
     * @param ctx The JSContext to create the object in
     *
     */
    public JSObject(JSContext ctx) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = NJSmake(context.contextRef(), 0L);
            }
        });
        context.persistObject(this);
    }

    /**
     * Called only by convenience subclasses.  If you use
     * this, you must set context and valueRef yourself.
     */
    public JSObject() {
    }

    /**
     * Wraps an existing object from JavaScript
     *
     * @param objRef The JavaScriptCore object reference
     * @param ctx    The JSContext of the reference
     *
     */
    protected JSObject(final long objRef, JSContext ctx) {
        super(objRef, ctx);
        context.persistObject(this);
    }

    /**
     * Creates a new object with function properties set for each method
     * in the defined interface.
     * In JS:
     * <pre>
     * {@code
     * var obj = {
     *     func1: function(a)   { alert(a); },
     *     func2: function(b,c) { alert(b+c); }
     * };
     * }
     * </pre>
     * Where func1, func2, etc. are defined in interface 'iface'.  This JSObject
     * must implement 'iface'.
     *
     * @param ctx   The JSContext to create the object in
     * @param iface The Java Interface defining the methods to expose to JavaScript
     *
     */
    public JSObject(JSContext ctx, final Class<?> iface) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = NJSmake(context.contextRef(), 0L);
                Method[] methods = iface.getDeclaredMethods();
                for (Method m : methods) {
                    JSObject f = new JSFunction(context, m,
                            JSObject.class, JSObject.this);
                    property(m.getName(), f);
                }
            }
        });
        context.persistObject(this);
    }

    /**
     * Creates a new function object with the entries in 'map' set as properties.
     *
     * @param ctx The JSContext to create object in
     * @param map The map containing the properties
     */
    @SuppressWarnings("unchecked")
    public JSObject(JSContext ctx, final Map map) {
        this(ctx);
        new JSObjectPropertiesMap<>(this, Object.class).putAll(map);
    }

    /**
     * Determines if the object contains a given property
     *
     * @param prop The property to test the existence of
     * @return true if the property exists on the object, false otherwise
     *
     */
    public boolean jsobjectHasProperty(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJShasProperty(context.contextRef(), valueRef, new JSString(prop).stringRef());
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Gets the property named 'prop'
     *
     * @param prop The name of the property to fetch
     * @return The JSValue of the property, or null if it does not exist
     *
     */
    public JSValue property(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJSgetProperty(context.contextRef(), valueRef, new JSString(prop).stringRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSValue(context);
        }
        return new JSValue(runnable.jni.reference, context);
    }

    /**
     * Sets the value of property 'prop'
     *
     * @param prop       The name of the property to set
     * @param value      The Java object to set.  The Java object will be converted to a JavaScript object
     *                   automatically.
     * @param attributes And OR'd list of JSProperty constants
     *
     */
    public void property(final String prop, final Object value, final int attributes) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                JSString name = new JSString(prop);
                jni = NJSsetProperty(
                        context.contextRef(),
                        valueRef,
                        name.stringRef,
                        (value instanceof JSValue) ? ((JSValue) value).valueRef() : new JSValue(context, value).valueRef(),
                        attributes);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
        }
    }

    /**
     * Sets the value of property 'prop'.  No JSProperty attributes are set.
     *
     * @param prop  The name of the property to set
     * @param value The Java object to set.  The Java object will be converted to a JavaScript object
     *              automatically.
     *
     */
    public void property(String prop, Object value) {
        property(prop, value, JSPropertyAttributeNone);
    }

    /**
     * Deletes a property from the object
     *
     * @param prop The name of the property to delete
     * @return true if the property was deleted, false otherwise
     *
     */
    public boolean jsobjectDeleteProperty(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                JSString name = new JSString(prop);
                jni = NJSdeleteProperty(context.contextRef(), valueRef, name.stringRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return false;
        }
        return runnable.jni.bool;
    }

    /**
     * Returns the property at index 'index'.  Used for arrays.
     *
     * @param index The index of the property
     * @return The JSValue of the property at index 'index'
     *
     */
    public JSValue propertyAtIndex(final int index) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJSgetPropertyAtIndex(context.contextRef(), valueRef, index);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSValue(context);
        }
        return new JSValue(runnable.jni.reference, context);
    }

    /**
     * Sets the property at index 'index'.  Used for arrays.
     *
     * @param index The index of the property to set
     * @param value The Java object to set, will be automatically converted to a JavaScript value
     *
     */
    public void propertyAtIndex(final int index, final Object value) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJSsetPropertyAtIndex(context.contextRef(), valueRef, index,
                        (value instanceof JSValue) ? ((JSValue) value).valueRef() : new JSValue(context, value).valueRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
        }
    }

    private abstract class StringArrayReturnClass implements Runnable {
        public String[] sArray;
    }

    /**
     * Gets the list of set property names on the object
     *
     * @return A string array containing the property names
     *
     */
    public String[] propertyNames() {
        StringArrayReturnClass runnable = new StringArrayReturnClass() {
            @Override
            public void run() {
                long propertyNameArray = NJScopyPropertyNames(context.contextRef(), valueRef);
                long[] refs = NJSgetPropertyNames(propertyNameArray);
                String[] names = new String[refs.length];
                for (int i = 0; i < refs.length; i++) {
                    JSString name = new JSString(refs[i]);
                    names[i] = name.toString();
                }
                NJSreleasePropertyNames(propertyNameArray);
                sArray = names;
            }
        };
        context.sync(runnable);
        return runnable.sArray;
    }

    /**
     * Determines if the object is a function
     *
     * @return true if the object is a function, false otherwise
     *
     */
    public boolean jsvalueOrObjectIsFunction() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisFunction(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Determines if the object is a constructor
     *
     * @return true if the object is a constructor, false otherwise
     *
     */
    public boolean jsobjectIsConstructor() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = NJSisConstructor(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    @Override
    public int hashCode() {
        return valueRef().intValue();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        context.finalizeObject(this);
    }

    protected void setThis(JSObject thiz) {
        this.thiz = thiz;
    }

    public JSObject getThis() {
        return thiz;
    }

    public JSValue __nullFunc() {
        return new JSValue(context);
    }

        //
    // JS Function
    //
  
    public void callWithArguments(Object[] arguments, @Nullable ValueCallback<String> completionHandler) {
        // overriden by JSFunction 
    }


    //
    // JS Array
    //

    public void valueAtIndex(int index, ValueCallback<String> completionHandler) {
    }

    public  void setValue(Object value, int atIndex) {
           // overriden by JSArray 
    }

    /* Native Methods */

    protected native long NJSmake(long ctx, long data);

    protected native long NJSmakeInstance(long ctx);

    protected native JNIReturnObject NJSmakeArray(long ctx, long[] args);

    protected native JNIReturnObject NJSmakeDate(long ctx, long[] args);

    protected native JNIReturnObject NJSmakeError(long ctx, long[] args);

    protected native JNIReturnObject NJSmakeRegExp(long ctx, long[] args);

    protected native long NJSgetPrototype(long ctx, long object);

    protected native void NJSsetPrototype(long ctx, long object, long value);

    protected native boolean NJShasProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject NJSgetProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject NJSsetProperty(long ctx, long object, long propertyName, long value, int attributes);

    protected native JNIReturnObject NJSdeleteProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject NJSgetPropertyAtIndex(long ctx, long object, int propertyIndex);

    protected native JNIReturnObject NJSsetPropertyAtIndex(long ctx, long object, int propertyIndex, long value);

    protected native long NJSgetPrivate(long object);

    protected native boolean NJSetPrivate(long object, long data);

    protected native boolean NJSisFunction(long ctx, long object);

    protected native JNIReturnObject NJScallAsFunction(long ctx, long object, long thisObject, long[] args);

    protected native boolean NJSisConstructor(long ctx, long object);

    protected native JNIReturnObject NJScallAsConstructor(long ctx, long object, long[] args);

    protected native long NJScopyPropertyNames(long ctx, long object);

    protected native long[] NJSgetPropertyNames(long propertyNameArray);

    protected native void NJSreleasePropertyNames(long propertyNameArray);

    protected native long NJSmakeFunctionWithCallback(long ctx, long name);

    protected native JNIReturnObject NJSmakeFunction(long ctx, long name, long[] parameterNames,
                                                  long body, long sourceURL, int startingLineNumber);

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }
}