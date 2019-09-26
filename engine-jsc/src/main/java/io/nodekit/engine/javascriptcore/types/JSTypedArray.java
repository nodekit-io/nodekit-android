package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;
import io.nodekit.engine.javascriptcore.util.JSException;

/**
 * A convenience base class for JavaScript typed arrays.  This is an abstract class, and is
 * subclassed by JSInt8Array, JSInt16Array, JSInt32Array, JSUint8Array, JSUint16Array,
 * JSUint32Array, JSUint8ClampedArray, JSFloat32Array, and JSFloat64Array
 *
 * @param <T> Parameterized type of array elements
 *
 */
public abstract class JSTypedArray<T> extends JSBaseArray<T> {

    protected JSTypedArray(JSContext ctx, int length, String jsConstructor, Class<T> cls) {
        super(ctx, cls);
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor, new String[]{"length"},
                "return new " + jsConstructor + "(length);",
                null, 0);
        JSValue newArray = constructor.call(null, length);
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSTypedArray typedArray, String jsConstructor, Class<T> cls) {
        super(typedArray.context, cls);
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor, new String[]{"tarr"},
                "return new " + jsConstructor + "(tarr);",
                null, 0);
        JSValue newArray = constructor.call(null, typedArray);
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSContext ctx, Object object, String jsConstructor, Class<T> cls) {
        super(ctx, cls);
        context = ctx;
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor, new String[]{"obj"},
                "return new " + jsConstructor + "(obj);",
                null, 0);
        JSValue newArray = constructor.call(null, object);
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSArrayBuffer buffer, int byteOffset, int length, String jsConstructor,
                           Class<T> cls) {
        super(buffer.getJSObject().getContext(), cls);
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor,
                new String[]{"buffer,byteOffset,length"},
                "return new " + jsConstructor + "(buffer,byteOffset,length);",
                null, 0);
        JSValue newArray = constructor.call(null, buffer.getJSObject(), byteOffset, length);
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSArrayBuffer buffer, int byteOffset, String jsConstructor,
                           Class<T> cls) {
        super(buffer.getJSObject().getContext(), cls);
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor,
                new String[]{"buffer,byteOffset"},
                "return new " + jsConstructor + "(buffer,byteOffset);",
                null, 0);
        JSValue newArray = constructor.call(null, buffer.getJSObject(), byteOffset);
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSArrayBuffer buffer, String jsConstructor, Class<T> cls) {
        super(buffer.getJSObject().getContext(), cls);
        JSFunction constructor = new JSFunction(context, "_" + jsConstructor,
                new String[]{"buffer"},
                "return new " + jsConstructor + "(buffer);",
                null, 0);
        JSValue newArray = constructor.call(null, buffer.getJSObject());
        valueRef = newArray.valueRef();
        NJSprotect(context.contextRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(long objRef, JSContext ctx, Class<T> cls) {
        super(objRef, ctx, cls);
    }

    @SuppressWarnings("unchecked")
    protected JSTypedArray(JSTypedArray superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        super(superList, leftBuffer, rightBuffer, cls);
    }

    /**
     * JavaScript: TypedArray.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/from
     *
     * @param obj source object
     * @return a new typed array
     *
     */
    public static JSTypedArray from(JSObject obj) {
        JSTypedArray arr = null;
        if (isTypedArray(obj)) {
            switch (obj.property("constructor").jsvalueToObject().property("name").toString()) {
                case "Int8Array":
                    arr = new JSInt8Array(obj.valueRef(), obj.getContext());
                    break;
                case "Uint8Array":
                    arr = new JSUint8Array(obj.valueRef(), obj.getContext());
                    break;
                case "Uint8ClampedArray":
                    arr = new JSUint8ClampedArray(obj.valueRef(), obj.getContext());
                    break;
                case "Int16Array":
                    arr = new JSInt16Array(obj.valueRef(), obj.getContext());
                    break;
                case "Uint16Array":
                    arr = new JSUint16Array(obj.valueRef(), obj.getContext());
                    break;
                case "Int32Array":
                    arr = new JSInt32Array(obj.valueRef(), obj.getContext());
                    break;
                case "Uint32Array":
                    arr = new JSUint32Array(obj.valueRef(), obj.getContext());
                    break;
                case "Float32Array":
                    arr = new JSFloat32Array(obj.valueRef(), obj.getContext());
                    break;
                case "Float64Array":
                    arr = new JSFloat64Array(obj.valueRef(), obj.getContext());
                    break;
            }
        }
        if (arr == null) throw new JSException(obj.getContext(), "Object not a typed array");
        arr.NJSprotect(arr.getContext().contextRef(), arr.valueRef);
        return arr;
    }

    /**
     * Determineds if a JSValue is a typed array
     *
     * @param value the JSValue to test
     * @return true if a typed array, false otherwise
     *
     */
    public static boolean isTypedArray(JSValue value) {
        if (!value.jsvalueIsObject()) return false;
        JSObject obj = value.jsvalueToObject();
        return obj.jsobjectHasProperty("BYTES_PER_ELEMENT") && obj.jsobjectHasProperty("length") &&
                obj.jsobjectHasProperty("byteOffset") && obj.jsobjectHasProperty("byteLength");
    }

    /**
     * JavaScript: TypedArray.prototype.buffer, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/buffer
     *
     * @return the underlying ArrayBuffer of this typed array
     *
     */
    public JSArrayBuffer buffer() {
        return new JSArrayBuffer(property("buffer").jsvalueToObject());
    }

    /**
     * JavaScript: TypedArray.prototype.buffer, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/byteLength
     *
     * @return the length in bytes of the underlying ArrayBuffer
     *
     */
    public int byteLength() {
        return property("byteLength").jsvalueToNumber().intValue();
    }

    /**
     * JavaScript: TypedArray.prototype.buffer, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray/byteOffset
     *
     * @return the byte offset of the typed array in the underlying ArrayBuffer
     *
     */
    public int byteOffset() {
        return property("byteOffset").jsvalueToNumber().intValue();
    }

    @Override
    protected JSValue arrayElement(final int index) {
        JSFunction getElement = new JSFunction(context, "_getElement", new String[]{"thiz", "index"},
                "return thiz[index]",
                null, 0);
        return getElement.call(null, this, index);
    }

    @Override
    protected void arrayElement(final int index, final T value) {
        JSFunction setElement = new JSFunction(context, "_setElement",
                new String[]{"thiz", "index", "value"},
                "thiz[index] = value",
                null, 0);
        setElement.call(null, this, index, value);
    }

    /**
     * Always throws UnsupportedOperationException.  Typed Arrays operate on a fixed
     * JSArrayBuffer.  Items cannot be added, inserted or removed, only modified.
     *
     * @param val The value to add to the array
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(final T val) throws JSException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    protected JSTypedArray<T> subarray(int begin, int end) {
        JSValue subarray = property("subarray").jsvalueToFunction().call(this, begin, end).jsvalueToObject();
        return (JSTypedArray<T>) subarray.jsvalueToJSArray();
    }

    @SuppressWarnings("unchecked")
    protected JSTypedArray<T> subarray(int begin) {
        JSValue subarray = property("subarray").jsvalueToFunction().call(this, begin).jsvalueToObject();
        return (JSTypedArray<T>) subarray.jsvalueToJSArray();
    }
}
