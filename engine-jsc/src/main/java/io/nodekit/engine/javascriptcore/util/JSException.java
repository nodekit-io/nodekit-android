package io.nodekit.engine.javascriptcore.util;

import io.nodekit.engine.javascriptcore.types.JSError;
import io.nodekit.engine.javascriptcore.JSValue;
import io.nodekit.engine.javascriptcore.JSContext;

/**
 * A JSException is thrown for a number of different reasons, mostly by the JavaScriptCore
 * library.  The description of the exception is given in the message.
 *
 *
 */
public class JSException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private JSError error;

    /**
     * Creates a Java exception from a thrown JavaScript exception
     *
     * @param error The JSValue thrown by the JavaScriptCore engine
     *
     */
    public JSException(JSValue error) {
        super(new JSError(error).message());
        this.error = new JSError(error);
    }

    /**
     * Creates a JavaScriptCore exception from a string message
     *
     * @param ctx     The JSContext in which to create the exception
     * @param message The exception meessage
     *
     */
    public JSException(JSContext ctx, String message) {
        super(message);
        try {
            this.error = new JSError(ctx, message);
        } catch (JSException e) {
            // We are having an Exception Inception. Stop the madness
            this.error = null;
        }
    }

    /**
     * Gets the JSValue of the thrown exception
     *
     * @return the JSValue of the JavaScriptCore exception
     *
     */
    public JSError getError() {
        return error;
    }

    /**
     * JavaScript error stack trace, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error/Stack
     *
     * @return stack trace for error
     *
     */
    public String stack() {
        return (error != null) ? error.stack() : "undefined";
    }

    /**
     * JavaScript error name, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error/name
     *
     * @return error name
     *
     */
    public String name() {
        return (error != null) ? error.name() : "JSError";
    }

    @Override
    public String toString() {
        if (error != null) {
            try {
                return error.toString();
            } catch (JSException e) {
                return "Unknown Error";
            }
        }
        return "Unknown Error";
    }
}
