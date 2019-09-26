package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

/**
 * A wrapper class for a JavaScript ArrayBuffer
 * See: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer
 * Note, experimental ArrayBuffer.transfer() is not supported by this JavaScriptCore version
 *
 *
 */
public class JSArrayBuffer extends JSObjectWrapper {
    /**
     * Creates a new array buffer of 'length' bytes
     *
     * @param ctx    the JSContext in which to create the ArrayBuffer
     * @param length the length in bytes of the ArrayBuffer
     *
     */
    public JSArrayBuffer(JSContext ctx, int length) {
        super(new JSFunction(ctx, "_ArrayBuffer", new String[]{"length"}, "return new ArrayBuffer(length);", null, 0).call(null, length).jsvalueToObject());
    }

    /**
     * Treats an existing JSObject as an ArrayBuffer.  It is up to the user to ensure the
     * underlying JSObject is actually an ArrayBuffer.
     *
     * @param buffer The ArrayBuffer JSObject to wrap
     *
     */
    public JSArrayBuffer(JSObject buffer) {
        super(buffer);
    }

    /**
     * JavaScript: ArrayBuffer.prototype.byteLength, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/byteLength
     *
     * @return length of ArrayBuffer in bytes
     *
     */
    public int byteLength() {
        return property("byteLength").jsvalueToNumber().intValue();
    }

    /**
     * JavaScript: ArrayBuffer.isView(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/isView
     *
     * @param arg the argument to be checked
     * @return true if arg is one of the ArrayBuffer views, such as typed array objects or
     * a DataView; false otherwise
     *
     */
    public static boolean isView(JSValue arg) {
        return arg.getContext().property("ArrayBuffer").jsvalueToObject().property("isView").jsvalueToFunction().call(null, arg).jsvalueToBoolean();
    }

    /**
     * JavaScript: ArrayBuffer.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/slice
     *
     * @param begin Zero-based byte index at which to begin slicing
     * @param end   Byte index to end slicing
     * @return new ArrayBuffer with sliced contents copied
     *
     */
    public JSArrayBuffer slice(int begin, int end) {
        return new JSArrayBuffer(property("slice").jsvalueToFunction().call(this, begin, end).jsvalueToObject());
    }

    /**
     * JavaScript: ArrayBuffer.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/slice
     *
     * @param begin Zero-based byte index at which to begin slicing
     * @return new ArrayBuffer with sliced contents copied
     *
     */
    public JSArrayBuffer slice(int begin) {
        return new JSArrayBuffer(property("slice").jsvalueToFunction().call(this, begin).jsvalueToObject());
    }
}
