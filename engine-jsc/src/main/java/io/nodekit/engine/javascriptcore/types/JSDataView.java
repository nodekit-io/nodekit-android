package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

/**
 * A wrapper class for a JavaScript DataView
 * See: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView
 *
 *
 */
public class JSDataView extends JSObjectWrapper {

    /**
     * Creates a new DataView JavaScript object from ArrayBuffer 'buffer' and wraps it for Java
     *
     * @param buffer the JSArrayBuffer to create a DataView from
     *
     */
    public JSDataView(JSArrayBuffer buffer) {
        super(new JSFunction(buffer.getJSObject().getContext(),
                "_DataView", new String[]{"buffer"},

                "return new DataView(buffer);",
                null, 0).call(null, buffer).jsvalueToObject());
    }

    /**
     * Creates a new DataView JavaScript object from ArrayBuffer 'buffer' starting from
     * 'byteOffset' and wraps it for Java
     *
     * @param buffer     the JSArrayBuffer to create a DataView from
     * @param byteOffset the byte offset in 'buffer' to create the DataView from
     *
     */
    public JSDataView(JSArrayBuffer buffer, int byteOffset) {
        super(new JSFunction(buffer.getJSObject().getContext(),
                "_DataView1", new String[]{"buffer", "byteOffset"},

                "return new DataView(buffer,byteOffset);",
                null, 0).call(null, buffer, byteOffset).jsvalueToObject());
    }

    /**
     * Creates a new DataView JavaScript object from ArrayBuffer 'buffer' starting from
     * 'byteOffset' and wraps it for Java
     *
     * @param buffer     the JSArrayBuffer to create a DataView from
     * @param byteOffset the byte offset in 'buffer' to create the DataView from
     * @param byteLength the length, in bytes, from 'byteOffset' to use for the DataView
     *
     */
    public JSDataView(JSArrayBuffer buffer, int byteOffset, int byteLength) {
        super(new JSFunction(buffer.getJSObject().getContext(),
                "_DataView2", new String[]{"buffer", "byteOffset", "byteLength"},

                "return new DataView(buffer,byteOffset,byteLength);",
                null, 0).call(null, buffer, byteOffset, byteLength).jsvalueToObject());
    }

    /**
     * Treats an existing JSObject as a DataView.  It is up to the user to ensure the
     * underlying JSObject is actually an DataView.
     *
     * @param view The DataView JSObject to wrap
     *
     */
    public JSDataView(JSObject view) {
        super(view);
    }

    /**
     * JavasScript DataView.prototype.buffer, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/buffer
     *
     * @return the JSArrayBuffer on which the DataView is built
     */
    public JSArrayBuffer buffer() {
        return new JSArrayBuffer(property("buffer").jsvalueToObject());
    }

    /**
     * JavasScript DataView.prototype.byteLength, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/byteLength
     *
     * @return the length in bytes of the DataView
     */
    public int byteLength() {
        return property("byteLength").jsvalueToNumber().intValue();
    }

    /**
     * JavasScript DataView.prototype.byteOffset, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/byteOffset
     *
     * @return the byte offset in the JSArrayBuffer where the DataView starts
     */
    public int byteOffset() {
        return property("byteOffset").jsvalueToNumber().intValue();
    }

    /**
     * JavasScript DataView.prototype.getFloat32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getFloat32
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Float getFloat32(int byteOffset, boolean littleEndian) {
        return property("getFloat32").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber().floatValue();
    }

    /**
     * JavasScript DataView.prototype.getFloat32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getFloat32
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Float getFloat32(int byteOffset) {
        return property("getFloat32").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().floatValue();
    }

    /**
     * JavasScript DataView.prototype.setFloat32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setFloat32
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setFloat32(int byteOffset, Float value, boolean littleEndian) {
        property("setFloat32").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setFloat32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setFloat32
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setFloat32(int byteOffset, Float value) {
        property("setFloat32").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getFloat64(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getFloat64
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Double getFloat64(int byteOffset, boolean littleEndian) {
        return property("getFloat64").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber();
    }

    /**
     * JavasScript DataView.prototype.getFloat64(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getFloat64
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Double getFloat64(int byteOffset) {
        return property("getFloat64").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber();
    }

    /**
     * JavasScript DataView.prototype.setFloat64(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setFloat64
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setFloat64(int byteOffset, Double value, boolean littleEndian) {
        property("setFloat64").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setFloat64(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setFloat64
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setFloat64(int byteOffset, Double value) {
        property("setFloat64").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getInt32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getInt32
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Integer getInt32(int byteOffset, boolean littleEndian) {
        return property("getInt32").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber().intValue();
    }

    /**
     * JavasScript DataView.prototype.getInt32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getInt32
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Integer getInt32(int byteOffset) {
        return property("getInt32").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().intValue();
    }

    /**
     * JavasScript DataView.prototype.setInt32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setInt32
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setInt32(int byteOffset, Integer value, boolean littleEndian) {
        property("setInt32").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setInt32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setInt32
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setInt32(int byteOffset, Integer value) {
        property("setInt32").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getUint32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getUint32
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Long getUint32(int byteOffset, boolean littleEndian) {
        return property("getUint32").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber().longValue();
    }

    /**
     * JavasScript DataView.prototype.getUint32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getUint32
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Long getUint32(int byteOffset) {
        return property("getUint32").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().longValue();
    }

    /**
     * JavasScript DataView.prototype.setUint32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setUint32
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setUint32(int byteOffset, Long value, boolean littleEndian) {
        property("setUint32").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setUint32(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setUint32
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setUint32(int byteOffset, Long value) {
        property("setUint32").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getInt16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getInt16
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Short getInt16(int byteOffset, boolean littleEndian) {
        return property("getInt16").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber().shortValue();
    }

    /**
     * JavasScript DataView.prototype.getInt16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getInt16
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Short getInt16(int byteOffset) {
        return property("getInt16").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().shortValue();
    }

    /**
     * JavasScript DataView.prototype.setInt16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setInt16
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setInt16(int byteOffset, Short value, boolean littleEndian) {
        property("setInt16").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setInt16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setInt16
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setInt16(int byteOffset, Short value) {
        property("setInt16").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getUint16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getUint16
     *
     * @param byteOffset   the byte offset to read from
     * @param littleEndian whether the value is stored with little endianness
     * @return the value at byteOffset
     */
    public Short getUint16(int byteOffset, boolean littleEndian) {
        return property("getUint16").jsvalueToFunction().call(this, byteOffset, littleEndian)
                .jsvalueToNumber().shortValue();
    }

    /**
     * JavasScript DataView.prototype.getUint16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getUint16
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Short getUint16(int byteOffset) {
        return property("getUint16").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().shortValue();
    }

    /**
     * JavasScript DataView.prototype.setUint16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setUint16
     *
     * @param byteOffset   the byte offset to write to
     * @param value        the value to store at 'byteOffset'
     * @param littleEndian whether the value is to be stored with little endianness
     */
    public void setUint16(int byteOffset, Short value, boolean littleEndian) {
        property("setUint16").jsvalueToFunction().call(this, byteOffset, value, littleEndian);
    }

    /**
     * JavasScript DataView.prototype.setUint16(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setUint16
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setUint16(int byteOffset, Short value) {
        property("setUint16").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getInt8(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getInt8
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Byte getInt8(int byteOffset) {
        return property("getInt8").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().byteValue();
    }

    /**
     * JavasScript DataView.prototype.setInt8(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setInt8
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setInt8(int byteOffset, Byte value) {
        property("setInt8").jsvalueToFunction().call(this, byteOffset, value);
    }

    /**
     * JavasScript DataView.prototype.getUint8(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/getUint8
     *
     * @param byteOffset the byte offset to read from
     * @return the value at byteOffset
     */
    public Byte getUint8(int byteOffset) {
        return property("getUint8").jsvalueToFunction().call(this, byteOffset)
                .jsvalueToNumber().byteValue();
    }

    /**
     * JavasScript DataView.prototype.setUint8(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView/setUint8
     *
     * @param byteOffset the byte offset to write to
     * @param value      the value to store at 'byteOffset'
     */
    public void setUint8(int byteOffset, Byte value) {
        property("setUint8").jsvalueToFunction().call(this, byteOffset, value);
    }

}
