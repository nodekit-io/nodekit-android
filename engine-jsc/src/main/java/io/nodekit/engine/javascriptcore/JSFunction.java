package io.nodekit.engine.javascriptcore;



import io.nodekit.engine.javascriptcore.util.JSException;
import io.nodekit.engine.javascriptcore.types.JSArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.support.annotation.Nullable;
import java.lang.Override;
import java.util.ArrayList;

import android.util.Log;
import android.webkit.ValueCallback;

/**
 * A JavaScript function object.
 *
 *
 */
public class JSFunction extends JSObject {

    protected Method method = null;

    private Object invokeObject = null;

    private Class<? extends JSObject> subclass = null;

    /**
     * Called only by convenience subclasses.  If you use
     * this, you must set context and valueRef yourself.  Also,
     * don't forget to call NJSprotect()!
     */
    protected JSFunction() {
    }

    /**
     * Creates a JavaScript function that takes parameters 'parameterNames' and executes the
     * JS code in 'body'.
     *
     * @param ctx                The JSContext in which to create the function
     * @param name               The name of the function
     * @param parameterNames     A String array containing the names of the parameters
     * @param body               The JavaScript code to execute in the function
     * @param sourceURL          The URI of the source file, only used for reporting in stack trace (optional)
     * @param startingLineNumber The beginning line number, only used for reporting in stack trace (optional)
     *
     */
    public JSFunction(JSContext ctx, final String name, final String[] parameterNames,
                      final String body, final String sourceURL, final int startingLineNumber) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                long[] names = new long[parameterNames.length];
                for (int i = 0; i < parameterNames.length; i++) {
                    names[i] = new JSString(parameterNames[i]).stringRef();
                }
                JNIReturnObject jni = NJSmakeFunction(
                        context.contextRef(),
                        new JSString(name).stringRef(),
                        names,
                        new JSString(body).stringRef(),
                        (sourceURL == null) ? 0L : new JSString(sourceURL).stringRef(),
                        startingLineNumber);
                valueRef = testException(jni);
            }
        });
        context.persistObject(this);
    }

    private long testException(JNIReturnObject jni) {
        if (jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            return (NJSmake(context.contextRef(), 0L));
        } else {
            return jni.reference;
        }
    }

    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     *
     * @param ctx           The JSContext to create the object in
     * @param method        The method to invoke
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     *
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass,
                      Object invokeObject) {
        context = ctx;
        this.method = method;
        this.invokeObject = (invokeObject == null) ? this : invokeObject;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = NJSmakeFunctionWithCallback(context.contextRef(),
                        new JSString(method.getName()).stringRef());
                subclass = instanceClass;
            }
        });

        context.persistObject(this);
        context.zombies.add(this);
    }

    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * @param ctx           The JSContext to create the object in
     * @param method        The method to invoke
     * @param instanceClass The class to be created on 'new' call
     *
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass) {
        this(ctx, method, instanceClass, null);
    }

    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     *
     */
    public JSFunction(JSContext ctx,
                      final Method method) {
        this(ctx, method, JSObject.class);
    }

    /**
     * Creates a new function which basically does nothing.
     * @param ctx The JSContext to create the object in
     *
     */
    public JSFunction(JSContext ctx) {
        this(ctx, (String) null);
    }

    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * @param ctx           The JSContext to create the object in
     * @param methodName    The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     *
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass,
                      JSObject invokeObject) {
        context = ctx;
        this.invokeObject = (invokeObject == null) ? this : invokeObject;
        String name = (methodName == null) ? "__nullFunc" : methodName;
        Method[] methods = this.invokeObject.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                this.method = method;
                break;
            }
        }
        if (method == null) {
            context.throwJSException(new JSException(context, "No such method. Did you make it public?"));
        }
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = NJSmakeFunctionWithCallback(context.contextRef(),
                        new JSString(method.getName()).stringRef());
                subclass = instanceClass;
            }
        });

        context.persistObject(this);
        context.zombies.add(this);
    }

    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * @param ctx           The JSContext to create the object in
     * @param methodName    The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     *
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass) {
        this(ctx, methodName, instanceClass, null);
    }

    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * @param ctx        The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     *
     */
    public JSFunction(JSContext ctx,
                      final String methodName) {
        this(ctx, methodName, JSObject.class);
    }

    /**
     * Wraps an existing object as a JSFunction
     *
     * @param objRef  The JavaScriptCore object reference
     * @param context The JSContext the object
     *
     */
    public JSFunction(final long objRef, JSContext context) {
        super(objRef, context);
    }


    // JSFunction
    @Override
    public void callWithArguments(Object[] arguments, @Nullable ValueCallback<String> completionHandler) {
        if (completionHandler == null)
            apply(null, arguments);
        else {
            JSValue result = apply(null, arguments);
            completionHandler.onReceiveValue(result.toString());
        }
    }

    /**
     * Calls this JavaScript function, similar to 'Function.call()' in JavaScript
     *
     * @param thiz The 'this' object on which the function operates, null if not on a constructor object
     * @param args The argument list to be passed to the function
     * @return The JSValue returned by the function
     *
     */
    public JSValue call(final JSObject thiz, final Object... args) {
        return apply(thiz, args);
    }

    private long[] argsToValueRefs(final Object[] args) {
        ArrayList<JSValue> largs = new ArrayList<>();
        if (args != null) {
            for (Object o : args) {
                JSValue v;
                if (o == null) break;
                if (o.getClass() == Void.class)
                    v = new JSValue(context);
                else if (o instanceof JSValue)
                    v = (JSValue) o;
                else if (o instanceof Object[])
                    v = new JSArray<>(context, (Object[]) o, Object.class);
                else
                    v = new JSValue(context, o);
                largs.add(v);
            }
        }
        long[] valueRefs = new long[largs.size()];
        for (int i = 0; i < largs.size(); i++) {
            valueRefs[i] = largs.get(i).valueRef();
        }
        return valueRefs;
    }

    /**
     * Calls this JavaScript function, similar to 'Function.apply() in JavaScript
     *
     * @param thiz The 'this' object on which the function operates, null if not on a constructor object
     * @param args An array of arguments to be passed to the function
     * @return The JSValue returned by the function
     *
     */
    public JSValue apply(final JSObject thiz, final Object[] args) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJScallAsFunction(context.contextRef(), valueRef, (thiz == null) ? 0L : thiz.valueRef(),
                        argsToValueRefs(args));
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
     * Calls this JavaScript function with no args and 'this' as null
     *
     * @return The JSValue returned by the function
     *
     */
    public JSValue call() {
        return call(null);
    }

    /**
     * Calls this JavaScript function as a constructor, i.e. same as calling 'new func(args)'
     *
     * @param args The argument list to be passed to the function
     * @return an instance object of the constructor
     *
     */
    public JSObject newInstance(final Object... args) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = NJScallAsConstructor(context.contextRef(), valueRef, argsToValueRefs(args));
            }
        };
        context.sync(runnable);
        return context.getObjectFromRef(testException(runnable.jni));
    }

    /**
     * Gets the prototype object, if it exists
     *
     * @return A JSValue referencing the prototype object, or null if none
     *
     */
    public JSValue prototype() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.reference = NJSgetPrototype(context.contextRef(), valueRef);
            }
        };
        context.sync(runnable);
        if (runnable.jni.reference == 0) return null;
        return new JSValue(runnable.jni.reference, context);
    }

    /**
     * Sets the prototype object
     *
     * @param proto The object defining the function prototypes
     *
     */
    public void prototype(final JSValue proto) {
        context.sync(new Runnable() {
            @Override
            public void run() {
                NJSsetPrototype(context.contextRef(), valueRef, proto.valueRef());
            }
        });
    }

    // native call
    private long functionCallback(long contextRef, long functionRef, long thisObjectRef,
                                  long argumentsValueRef[], long exceptionRefRef) {

        if (BuildConfig.DEBUG && functionRef != valueRef()) throw new AssertionError();
        try {
            JSValue[] args = new JSValue[argumentsValueRef.length];
            for (int i = 0; i < argumentsValueRef.length; i++) {
                JSObject obj = context.getObjectFromRef(argumentsValueRef[i], false);
                if (obj != null) args[i] = obj;
                else args[i] = new JSValue(argumentsValueRef[i], context);
            }
            JSObject thiz = context.getObjectFromRef(thisObjectRef);
            JSValue value = function(thiz, args, invokeObject);
            NJSsetException(0L, exceptionRefRef);
            return value.valueRef();
        } catch (JSException e) {
            e.printStackTrace();
            NJSsetException(e.getError().valueRef(), exceptionRefRef);
            return 0L;
        }
    }

    protected JSValue function(JSObject thiz, JSValue[] args) {
        return function(thiz, args, this);
    }

    protected JSValue function(JSObject thiz, JSValue[] args, final Object invokeObject) {
        Class<?>[] pType = method.getParameterTypes();
        Object[] passArgs = new Object[pType.length];
        for (int i = 0; i < passArgs.length; i++) {
            if (i < args.length) {
                if (args[i] == null) passArgs[i] = null;
                else passArgs[i] = args[i].jsvalueToJavaObject(pType[i]);
            } else {
                passArgs[i] = null;
            }
        }
        JSValue returnValue;
        JSObject stack = null;
        try {
         //   stack = invokeObject.getThis();
         //   invokeObject.setThis(thiz);
            Object ret = method.invoke(invokeObject, passArgs);
            if (method.getReturnType() == Void.class || ret == null)
                returnValue = new JSValue(context);
            else if (ret instanceof JSValue)
                returnValue = (JSValue) ret;
            else
                returnValue = new JSValue(context, ret);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = new JSValue(context);
        } catch (IllegalAccessException e) {
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = new JSValue(context);
        } finally {
         //   invokeObject.setThis(stack);
        }
        return returnValue;
    }

    private abstract class JSObjectReturnClass implements Runnable {
        public JSObject object;

        @Override
        public void run() {
            object = execute();
        }

        abstract JSObject execute();
    }

    protected JSObject constructor(final JSValue[] args) {
        JSObjectReturnClass runnable = new JSObjectReturnClass() {
            @Override
            public JSObject execute() {
                JSObject proto = context.getObjectFromRef(NJSgetPrototype(context.contextRef(), valueRef()));
                try {
                    Constructor<?> defaultConstructor = subclass.getConstructor();
                    JSObject thiz = (JSObject) defaultConstructor.newInstance();
                    thiz.context = context;
                    thiz.valueRef = NJSmake(context.contextRef(), 0); //NJSmakeInstance(context.contextRef());
                    thiz.isInstanceOf = JSFunction.this;
                    thiz.property("constructor", JSFunction.this, JSObject.JSPropertyAttributeDontEnum);
                    function(thiz, args);
                    context.persistObject(thiz);
                    context.zombies.add(thiz);
                    if (proto != null) {
                        for (String prop : proto.propertyNames()) {
                            thiz.property(prop, proto.property(prop));
                        }
                    }
                    return thiz;
                } catch (NoSuchMethodException e) {
                    String error = e.toString() + "If " + subclass.getName() + " is an embedded " +
                            "class, did you specify it as 'static'?";
                    context.throwJSException(new JSException(context, error));
                } catch (InvocationTargetException e) {
                    String error = e.toString() + "; Did you remember to call super?";
                    context.throwJSException(new JSException(context, error));
                } catch (IllegalAccessException e) {
                    String error = e.toString() + "; Is your constructor public?";
                    context.throwJSException(new JSException(context, error));
                } catch (InstantiationException e) {
                    context.throwJSException(new JSException(context, e.toString()));
                }
                return new JSObject(context);
            }
        };
        context.sync(runnable);
        return runnable.object;
    }

    // native call
    private long constructorCallback(long contextRef, long constructorRef,
                                     long argumentsValueRef[], long exceptionRefRef) {

        try {
            JSValue[] args = new JSValue[argumentsValueRef.length];
            for (int i = 0; i < argumentsValueRef.length; i++) {
                JSObject obj = context.getObjectFromRef(argumentsValueRef[i], false);
                if (obj != null) args[i] = obj;
                else args[i] = new JSValue(argumentsValueRef[i], context);
            }
            JSObject newObj = constructor(args);
            NJSsetException(0L, exceptionRefRef);
            return newObj.valueRef();
        } catch (JSException e) {
            NJSsetException(e.getError().valueRef(), exceptionRefRef);
            return 0L;
        }
    }

    // native call
    private boolean hasInstanceCallback(long contextRef, long constructorRef,
                                        long possibleInstanceRef, long exceptionRefRef) {
        NJSsetException(0L, exceptionRefRef);

        JSValue instance = new JSValue(possibleInstanceRef, context);
        return (instance.jsvalueIsObject() && ((instance.jsvalueToObject()).isInstanceOf == this));
    }

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }
}
