package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.NonNull;

/**
 * A convenience class for handling JavaScript's Uint16Array
 *
 *
 */
public class JSUint16Array extends JSTypedArray<Short> {
    /**
     * Creates a typed array of length 'length' in JSContext 'context'
     *
     * @param ctx    the JSContext in which to create the typed array
     * @param length the length of the array in elements
     *
     */
    public JSUint16Array(JSContext ctx, int length) {
        super(ctx, length, "Uint16Array", Short.class);
    }

    /**
     * Creates a new JSUint16Array from the contents of another typed array
     *
     * @param tarr the typed array from which to create the new array
     *
     */
    public JSUint16Array(JSTypedArray tarr) {
        super(tarr, "Uint16Array", Short.class);
    }

    /**
     * Creates new typed array as if by TypedArray.from()
     *
     * @param ctx    The context in which to create the typed array
     * @param object The object to create the array from
     *
     */
    public JSUint16Array(JSContext ctx, Object object) {
        super(ctx, object, "Uint16Array", Short.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     * @param length     The number of bytes from 'byteOffset' to include in the array
     *
     */
    public JSUint16Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer, byteOffset, length, "Uint16Array", Short.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     *
     */
    public JSUint16Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer, byteOffset, "Uint16Array", Short.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer The JSArrayBuffer to create the typed array from
     *
     */
    public JSUint16Array(JSArrayBuffer buffer) {
        super(buffer, "Uint16Array", Short.class);
    }

    /**
     * Treats an existing value as a typed array
     *
     * @param valueRef the JavaScriptCore value reference
     * @param ctx      The JSContext of the value
     *
     */
    public JSUint16Array(long valueRef, JSContext ctx) {
        super(valueRef, ctx, Short.class);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @param end   the element to end at (exclusive)
     * @return the new typed subarray
     */
    public JSUint16Array subarray(int begin, int end) {
        return (JSUint16Array) super.subarray(begin, end);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @return the new typed subarray
     */
    public JSUint16Array subarray(int begin) {
        return (JSUint16Array) super.subarray(begin);
    }

    private JSUint16Array(JSUint16Array superList, int leftBuffer, int rightBuffer) {
        super(superList, leftBuffer, rightBuffer, Short.class);
    }

    /**
     * @see java.util.List#subList(int, int)
     *
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public JSUint16Array subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new JSUint16Array(this, fromIndex, size() - toIndex);
    }
}
