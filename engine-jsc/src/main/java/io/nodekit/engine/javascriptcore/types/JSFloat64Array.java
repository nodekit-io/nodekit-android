package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.NonNull;

/**
 * A convenience class for handling JavaScript's Float64Array
 *
 *
 */
public class JSFloat64Array extends JSTypedArray<Double> {
    /**
     * Creates a typed array of length 'length' in JSContext 'context'
     *
     * @param ctx    the JSContext in which to create the typed array
     * @param length the length of the array in elements
     *
     */
    public JSFloat64Array(JSContext ctx, int length) {
        super(ctx, length, "Float64Array", Double.class);
    }

    /**
     * Creates a new JSFloat64Array from the contents of another typed array
     *
     * @param tarr the typed array from which to create the new array
     *
     */
    public JSFloat64Array(JSTypedArray tarr) {
        super(tarr, "Float64Array", Double.class);
    }

    /**
     * Creates new typed array as if by TypedArray.from()
     *
     * @param ctx    The context in which to create the typed array
     * @param object The object to create the array from
     *
     */
    public JSFloat64Array(JSContext ctx, Object object) {
        super(ctx, object, "Float64Array", Double.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     * @param length     The number of bytes from 'byteOffset' to include in the array
     *
     */
    public JSFloat64Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer, byteOffset, length, "Float64Array", Double.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer     The JSArrayBuffer to create the typed array from
     * @param byteOffset The byte offset in the ArrayBuffer to start from
     *
     */
    public JSFloat64Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer, byteOffset, "Float64Array", Double.class);
    }

    /**
     * Creates a typed array from a JSArrayBuffer
     *
     * @param buffer The JSArrayBuffer to create the typed array from
     *
     */
    public JSFloat64Array(JSArrayBuffer buffer) {
        super(buffer, "Float64Array", Double.class);
    }

    /**
     * Treats an existing value as a typed array
     *
     * @param valueRef the JavaScriptCore value reference
     * @param ctx      The JSContext of the value
     *
     */
    public JSFloat64Array(long valueRef, JSContext ctx) {
        super(valueRef, ctx, Double.class);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @param end   the element to end at (exclusive)
     * @return the new typed subarray
     */
    public JSFloat64Array subarray(int begin, int end) {
        return (JSFloat64Array) super.subarray(begin, end);
    }

    /**
     * JavaScript: TypedArray.prototype.subarray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/subarray
     *
     * @param begin the element to begin at (inclusive)
     * @return the new typed subarray
     */
    public JSFloat64Array subarray(int begin) {
        return (JSFloat64Array) super.subarray(begin);
    }

    private JSFloat64Array(JSFloat64Array superList, int leftBuffer, int rightBuffer) {
        super(superList, leftBuffer, rightBuffer, Double.class);
    }

    /**
     * @see java.util.List#subList(int, int)
     *
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public JSFloat64Array subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new JSFloat64Array(this, fromIndex, size() - toIndex);
    }
}
