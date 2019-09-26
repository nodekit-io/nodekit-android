package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.NonNull;

/**
 * A convenience class for handling JavaScript's Int32Array
 *
 *
 */
public class JSInt32Array extends JSTypedArray<Integer> {
    /**
     * Creates a typed array of length 'length' in JSContext 'context'
     *
     * @param ctx    the JSContext in which to create the typed array
     * @param length the length of the array in elements
     *
     */
    public JSInt32Array(JSContext ctx, int length) {
        super(ctx, length, "Int32Array", Integer.class);
    }

    /**
     * Creates a new JSInt32Array from the contents of another typed array
     *
     * @param tarr the typed array from which to create the new array
     *
     */
    public JSInt32Array(JSTypedArray tarr) {
        super(tarr, "Int32Array", Integer.class);
    }

    /**
     * Creates new typed array as if by TypedArray.from()
     *
     * @param ctx    The context in which to create the typed array
     * @param object The object to create the array from
     *
     */
    public JSInt32Array(JSContext ctx, Object object) {
        super(ctx, object, "Int32Array", Integer.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     * @param length     The number of bytes from 'byteOffset' to include in the array
     *
     */
    public JSInt32Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer, byteOffset, length, "Int32Array", Integer.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     *
     */
    public JSInt32Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer, byteOffset, "Int32Array", Integer.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer The JSArrayBuffer to create the typed array from
     *
     */
    public JSInt32Array(JSArrayBuffer buffer) {
        super(buffer, "Int32Array", Integer.class);
    }

    /**
     * Treats an existing value as a typed array
     *
     * @param valueRef the JavaScriptCore value reference
     * @param ctx      The JSContext of the value
     *
     */
    public JSInt32Array(long valueRef, JSContext ctx) {
        super(valueRef, ctx, Integer.class);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @param end   the element to end at (exclusive)
     * @return the new typed subarray
     */
    public JSInt32Array subarray(int begin, int end) {
        return (JSInt32Array) super.subarray(begin, end);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @return the new typed subarray
     */
    public JSInt32Array subarray(int begin) {
        return (JSInt32Array) super.subarray(begin);
    }

    private JSInt32Array(JSInt32Array superList, int leftBuffer, int rightBuffer) {
        super(superList, leftBuffer, rightBuffer, Integer.class);
    }

    /**
     * @see java.util.List#subList(int, int)
     *
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public JSInt32Array subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new JSInt32Array(this, fromIndex, size() - toIndex);
    }
}
