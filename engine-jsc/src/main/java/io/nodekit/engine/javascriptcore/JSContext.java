package io.nodekit.engine.javascriptcore;

import io.nodekit.engine.queue.MessageQueueThread;
import io.nodekit.engine.queue.QueueThreadExceptionHandler;
import io.nodekit.engine.queue.MessageQueueThreadSpec;

import io.nodekit.engine.javascriptcore.types.JSArray;
import io.nodekit.engine.javascriptcore.types.JSTypedArray;
import io.nodekit.engine.javascriptcore.util.JSException;
import io.nodekit.nkscripting.NKScriptValue;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.webkit.ValueCallback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Wraps a JavaScriptCore context
 */
public class JSContext extends JSObject implements io.nodekit.engine.JavascriptContext, QueueThreadExceptionHandler {

    /**
     * Object interface for handling JSExceptions.
     *
     *
     */
    public interface IJSExceptionHandler {
        /**
         * Implement this method to catch JSExceptions
         *
         * @param exception caught exception
         *
         */
        void handle(JSException exception);
    }

    protected Long ctx;

    private IJSExceptionHandler exceptionHandler;

    public final Object mMutex = new Object();

    private final MessageQueueThread mqtModule = MessageQueueThread
            .create(MessageQueueThreadSpec.newBackgroundThreadSpec("nodekit-b"), this);

    private final MessageQueueThread mqtES6 = MessageQueueThread
            .create(MessageQueueThreadSpec.newBackgroundThreadSpec("nodekit-jsc"), this);

    private final List<Long> deadReferences = new ArrayList<>();

    /**
     * Creates a new JavaScript context
     */
    public JSContext() {
        context = this;
        sync(new Runnable() {
            @Override
            public void run() {
                static_init();
                ctx = NJScreate();
                valueRef = NJSgetGlobalObject(ctx);
            }
        });
    }

    /**
     * Creates a new JavaScript context in the context group 'inGroup'.
     *
     * @param inGroup The context group to create the context in
     *
     */
    public JSContext(final JSContextGroup inGroup) {
        context = this;
        sync(new Runnable() {
            @Override
            public void run() {
                static_init();
                ctx = NJScreateInGroup(inGroup.groupRef());
                valueRef = NJSgetGlobalObject(ctx);
            }
        });
    }

    /**
     * Creates a JavaScript context, and defines the global object with interface
     * 'iface'. This object must implement 'iface'. The methods in 'iface' will be
     * exposed to the JavaScript environment.
     *
     * @param iface The interface to expose to JavaScript
     *
     */
    public JSContext(final Class<?> iface) {
        context = this;
        sync(new Runnable() {
            @Override
            public void run() {
                static_init();
                ctx = NJScreate();
                valueRef = NJSgetGlobalObject(ctx);
                Method[] methods = iface.getDeclaredMethods();
                for (Method m : methods) {
                    JSObject f = new JSFunction(context, m, JSObject.class, context);
                    property(m.getName(), f);
                }
            }
        });
    }

    /**
     * Creates a JavaScript context in context group 'inGroup', and defines the
     * global object with interface 'iface'. This object must implement 'iface'. The
     * methods in 'iface' will be exposed to the JavaScript environment.
     *
     * @param inGroup The context group to create the context in
     * @param iface   The interface to expose to JavaScript
     *
     */
    public JSContext(final JSContextGroup inGroup, final Class<?> iface) {
        context = this;
        sync(new Runnable() {
            @Override
            public void run() {
                static_init();
                ctx = NJScreateInGroup(inGroup.groupRef());
                valueRef = NJSgetGlobalObject(ctx);
                Method[] methods = iface.getDeclaredMethods();
                for (Method m : methods) {
                    JSObject f = new JSFunction(context, m, JSObject.class, context);
                    property(m.getName(), f);
                }
            }
        });
    }

    public void handleException(Exception e) {
        Log.v("NodeKitAndroid", e.toString());
    }

    public NKScriptValue addJavascriptInterface(Object object, String name) {
         return JSProxy.register(this, object, name);
    }

    public void removeJavascriptInterface(@NonNull String name) {
        this.property(name, new JSValue(this));
    }

    public void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback) throws Exception {
        if (resultCallback == null) {
            this.evaluateScript(script);
        } else {
            JSValue result = this.evaluateScript(script);
            resultCallback.onReceiveValue(result.toString());
        }
    }

    /**
     * Stop the Javascript runtime associated with this context and release all
     * resources
     */
    public void close() {
        try {
            this.finalize();
        } catch (Throwable t) {

        }
    }
 
    protected Runnable loopMonitor = new Runnable() {
        @Override
        public void run() {
            if (deadReferences.size() > 100) {
                cleanDeadReferences();
            }
        }
    };

    protected void sync(Runnable runnable) {

        if (mqtES6.isOnThread()) {
            runnable.run();
            loopMonitor.run();
            return;
        }

        mqtES6.sync(runnable, loopMonitor);

    }

    protected void async(final Runnable runnable) {
        mqtES6.runOnQueue(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                loopMonitor.run();
            }
        });
    }

    protected void markForUnprotection(Long valueR) {
        synchronized (mMutex) {
            deadReferences.add(valueR);
        }
    }

    private void cleanDeadReferences() {
        synchronized (mMutex) {
            for (Long reference : deadReferences) {
                NJSunprotect(contextRef(), reference);
            }
            deadReferences.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {

        super.finalize();
        cleanDeadReferences();
        isDefunct = true;
        NJSrelease(ctx);
        if (mqtModule != null) {
            mqtModule.quitSynchronous();
        }
        if (mqtES6 != null) {
            mqtES6.quitSynchronous();
        }
    }

    /**
     * Sets the JS exception handler for this context. Any thrown JSException in
     * this context will call the 'handle' method on this object. The calling
     * function will return with an undefined value.
     *
     * @param handler An object that implements 'IJSExceptionHandler'
     *
     */
    public void setExceptionHandler(IJSExceptionHandler handler) {
        exceptionHandler = handler;
    }

    /**
     * Clears a previously set exception handler.
     *
     *
     */
    public void clearExceptionHandler() {
        exceptionHandler = null;
    }

    /**
     * If an exception handler is set, calls the exception handler, otherwise throws
     * the JSException.
     *
     * @param exception The JSException to be thrown
     *
     */
    public void throwJSException(JSException exception) {
        if (exceptionHandler == null) {
            throw exception;
        } else {
            // Before handling this exception, disable the exception handler. If a
            // JSException
            // is thrown in the handler, then it would recurse and blow the stack. This way
            // an
            // actual exception will get thrown. If successfully handled, then turn it back
            // on.
            IJSExceptionHandler temp = exceptionHandler;
            exceptionHandler = null;
            temp.handle(exception);
            exceptionHandler = temp;
        }
    }

    /**
     * Gets the context group to which this context belongs.
     *
     * @return The context group to which this context belongs
     */
    public JSContextGroup getGroup() {
        Long g = NJSgetGroup(ctx);
        if (g == 0)
            return null;
        return new JSContextGroup(g);
    }

    /**
     * Gets the JavaScriptCore context reference
     *
     * @return the JavaScriptCore context reference
     */
    public Long contextRef() {
        return ctx;
    }

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }

    /**
     * Executes a the JavaScript code in 'script' in this context
     *
     * @param script             The code to execute
     * @param thiz               The 'this' object
     * @param sourceURL          The URI of the source file, only used for reporting
     *                           in stack trace (optional)
     * @param startingLineNumber The beginning line number, only used for reporting
     *                           in stack trace (optional)
     * @return The return value returned by 'script'
     *
     */
    public JSValue evaluateScript(final String script, final JSObject thiz, final String sourceURL,
            final int startingLineNumber) {

        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                JSString jsscript = new JSString(script);
                JSString jssourceURL = new JSString(sourceURL);
                jni = NJSevaluateScript(ctx, jsscript.stringRef(), (thiz == null) ? 0L : thiz.valueRef(),
                        jssourceURL.stringRef(), startingLineNumber);
            }
        };
        sync(runnable);

        if (runnable.jni.exception != 0) {
            throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSValue(this);
        }
        return new JSValue(runnable.jni.reference, this);
    }

    /**
     * Executes a the JavaScript code in 'script' in this context
     *
     * @param script The code to execute
     * @param thiz   The 'this' object
     * @return The return value returned by 'script'
     *
     */
    public JSValue evaluateScript(String script, JSObject thiz) {
        return evaluateScript(script, thiz, null, 0);
    }

    /**
     * Executes a the JavaScript code in 'script' in this context
     *
     * @param script The code to execute
     * @return The return value returned by 'script'
     *
     */
    public JSValue evaluateScript(String script) {
        return evaluateScript(script, null, null, 0);
    }

    private Map<Long, WeakReference<JSObject>> objects = new HashMap<>();

    /**
     * Keeps a reference to an object in this context. This is used so that only one
     * Java object instance wrapping a JavaScript object is maintained at any time.
     * This way, local variables in the Java object will stay wrapped around all
     * returns of the same instance. This is handled by JSObject, and should not
     * need to be called by clients.
     *
     * @param obj The object with which to associate with this context
     *
     */
    public synchronized void persistObject(JSObject obj) {
        objects.put(obj.valueRef(), new WeakReference<>(obj));
    }

    /**
     * Removes a reference to an object in this context. Should only be used from
     * the 'finalize' object method. This is handled by JSObject, and should not
     * need to be called by clients.
     *
     * @param obj the JSObject to dereference
     *
     */
    protected synchronized void finalizeObject(JSObject obj) {
        objects.remove(obj.valueRef());
    }

    /**
     * Reuses a stored reference to a JavaScript object if it exists, otherwise, it
     * creates the reference.
     *
     * @param objRef the JavaScriptCore object reference
     * @param create whether to create the object if it does not exist
     * @return The JSObject representing the reference
     *
     */
    protected synchronized JSObject getObjectFromRef(long objRef, boolean create) {
        if (objRef == valueRef())
            return this;
        WeakReference<JSObject> wr = objects.get(objRef);
        JSObject obj = null;
        if (wr != null) {
            obj = wr.get();
            if (obj != null)
                obj.NJSunprotect(contextRef(), obj.valueRef());
        }
        if (obj == null && create) {
            obj = new JSObject(objRef, this);
            if (NJSisArray(contextRef(), objRef))
                obj = new JSArray(objRef, this);
            else if (JSTypedArray.isTypedArray(obj))
                obj = JSTypedArray.from(obj);
            else if (NJSisFunction(contextRef(), objRef))
                obj = new JSFunction(objRef, this);
        }
        return obj;
    }

    protected synchronized JSObject getObjectFromRef(long objRef) {
        return getObjectFromRef(objRef, true);
    }

    /**
     * Forces JavaScript garbage collection on this context
     *
     *
     */
    public void garbageCollect() {
        async(new Runnable() {
            @Override
            public void run() {
                NJSgarbageCollect(ctx);
            }
        });
    }

    protected static native void NJSstaticInit();

    protected native long NJScreate();

    protected native long NJScreateInGroup(long group);

    protected native long NJSretain(long ctx);

    protected native long NJSrelease(long ctx);

    protected native long NJSgetGroup(long ctx);

    protected native long NJSgetGlobalObject(long ctx);

    protected native JNIReturnObject NJSevaluateScript(long ctx, long script, long thisObject, long sourceURL,
            int startingLineNumber);

    protected native JNIReturnObject NJScheckScriptSyntax(long ctx, long script, long sourceURL,
            int startingLineNumber);

    protected native void NJSgarbageCollect(long ctx);

    static boolean isInit = false;

    private static void static_init() {
        synchronized (JSContext.class) {
            if (!isInit) {
                System.loadLibrary("native-lib");
                NJSstaticInit();
                isInit = true;
            }
        }
    }
}
