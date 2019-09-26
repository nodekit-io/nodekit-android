package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

/**
 * A convenience class for managing JavaScript error objects
 *
 */
public class JSError extends JSObject {
    /**
     * Generates a JavaScript throwable exception object
     * @param ctx  The context in which to create the error
     * @param message  The description of the error
     *
     */
    public JSError(JSContext ctx, String message) {
        context = ctx;
        long [] args = {
                new JSValue(context,message).valueRef()
        };
        JNIReturnObject jni = NJSmakeError(context.contextRef(), args);
        if (BuildConfig.DEBUG && jni.exception != 0) throw new AssertionError();
        valueRef = jni.reference;
    }
    /**
     * Generates a JavaScript throwable exception object
     * @param ctx  The context in which to create the error
     *
     */
    public JSError(JSContext ctx) {
        context = ctx;
        JNIReturnObject jni = NJSmakeError(context.contextRef(), new long[0]);
        if (BuildConfig.DEBUG && jni.exception != 0) throw new AssertionError();
        valueRef = jni.reference;
    }

    /**
     * Constructs a JSError from a JSValue.  Assumes JSValue is a properly constructed JS Error
     * object.
     * @param error the JavaScript Error object
     */
    public JSError(JSValue error) {
        super(error.valueRef(), error.getContext());
    }

    /**
     * Deprecated since 3.0.  filename and lineNumber not supported by JavaScriptCoreGTK
     * @param ctx  The context in which to create the error
     * @param message  The description of the error
     * @param filename   The name of the file in which the error occurred. This is used for stack
     *                   tracing and is optional.
     * @param lineNumber  The line number where the error occurred. This is used for stack tracing
     *                    and is optional.
     *
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSError(JSContext ctx, String message, String filename, Integer lineNumber) {
        throw new UnsupportedOperationException();
    }

    /**
     * JavaScript error stack trace, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error/Stack
     * @return stack trace for error
     *
     */
    public String stack() {
        return property("stack").toString();
    }

    /**
     * JavaScript error message, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error/message
     * @return error message
     *
     */
    public String message() {
        return property("message").toString();
    }

    /**
     * JavaScript error name, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error/name
     * @return error name
     *
     */
    public String name() {
        return property("name").toString();
    }
}
