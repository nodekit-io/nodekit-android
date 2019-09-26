package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;
import io.nodekit.engine.javascriptcore.util.JSException;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A convenience class for handling JavaScript arrays.  Implements java.util.List interface for
 * simple integration with Java methods.
 */
public class JSArray<T> extends JSBaseArray<T> {
    /**
     * Interface containing a map function
     *
     * @param <T> Parameterized type of array elements
     */
    public interface MapCallback<T> {
        /**
         * A function to map an array value to a new JSValue
         *
         * @param currentValue value to map
         * @param index        index in 'array'
         * @param array        array being traversed
         * @return mapped value
         *
         */
        JSValue callback(T currentValue, int index, JSArray<T> array);
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
     * Creates a JavaScript array object, initialized with 'array' JSValues
     *
     * @param ctx   The JSContext to create the array in
     * @param array An array of JSValues with which to initialize the JavaScript array object
     * @param cls   The class of the component objects
     */
    @SuppressWarnings("unused")
    public JSArray(JSContext ctx, JSValue[] array, Class<T> cls) {
        super(ctx, cls);
        long[] valueRefs = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            valueRefs[i] = array[i].valueRef();
        }
        valueRef = testException(NJSmakeArray(context.contextRef(), valueRefs));
        context.persistObject(this);
    }

    /**
     * Creates an empty JavaScript array object
     *
     * @param ctx The JSContext to create the array in
     * @param cls The class of the component objects
     */
    public JSArray(JSContext ctx, Class<T> cls) {
        super(ctx, cls);
        long[] valueRefs = new long[0];
        valueRef = testException(NJSmakeArray(context.contextRef(), valueRefs));
        context.persistObject(this);
    }

    /**
     * Creates a JavaScript array object, initialized with 'array' Java values
     *
     * @param ctx   The JSContext to create the array in
     * @param array An array of Java objects with which to initialize the JavaScript array object.  Each
     *              Object will be converted to a JSValue
     * @param cls   The class of the component objects
     */
    public JSArray(JSContext ctx, Object[] array, Class<T> cls) {
        super(ctx, cls);
        long[] valueRefs = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            JSValue v = new JSValue(context, array[i]);
            valueRefs[i] = v.valueRef();
        }
        valueRef = testException(NJSmakeArray(context.contextRef(), valueRefs));
        context.persistObject(this);
    }

    @SuppressWarnings("unchecked")
    public JSArray(long valueRef, JSContext ctx) {
        super(valueRef, ctx, (Class<T>) JSValue.class);
    }

    @SuppressWarnings("unchecked")
    public JSArray(long valueRef, JSContext ctx, Class<T> cls) {
        super(valueRef, ctx, cls);
    }

    private JSArray(JSArray<T> superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        super(superList, leftBuffer, rightBuffer, cls);
    }

    /**
     * Creates a JavaScript array object, initialized with 'list' Java values
     *
     * @param ctx  The JSContext to create the array in
     * @param list The Collection of values with which to initialize the JavaScript array object.  Each
     *             object will be converted to a JSValue
     * @param cls  The class of the component objects
     */
    public JSArray(JSContext ctx, Collection list, Class<T> cls) {
        this(ctx, list.toArray(), cls);
    }

    /**
     * @see java.util.List#add(int, Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(final int index, final T element) {
        if (this == element) {
            throw new IllegalArgumentException();
        }
        int count = size();
        if (index > count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        splice(index, 0, element);
    }

    /**
     * @see java.util.List#remove(int)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T remove(final int index) {
        int count = size();
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (mSuperList == null) {
            return splice(index, 1).get(0);
        } else {
            return mSuperList.remove(index + mLeftBuffer);
        }
    }

    /**
     * @see java.util.List#subList(int, int)
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<T> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new JSArray(this, fromIndex, size() - toIndex, mType);
    }

    /** JavaScript methods **/

    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     *
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A JavaScript function to map each new element of the array
     * @param thiz      The 'this' pointer passed to 'mapFn'
     * @return A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike, JSFunction mapFn, JSObject thiz) {
        JSFunction from = ctx.property("Array").jsvalueToObject().property("from").jsvalueToFunction();
        return (JSArray) from.call(null, arrayLike, mapFn, thiz).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     *
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A JavaScript function to map each new element of the array
     * @return A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike, JSFunction mapFn) {
        JSFunction from = ctx.property("Array").jsvalueToObject().property("from").jsvalueToFunction();
        return (JSArray) from.call(null, arrayLike, mapFn).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     *
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @return A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike) {
        JSFunction from = ctx.property("Array").jsvalueToObject().property("from").jsvalueToFunction();
        return (JSArray) from.call(null, arrayLike).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     *
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A Java function to map each new element of the array
     * @return A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike, final MapCallback<JSValue> mapFn) {
        JSFunction from = ctx.property("Array").jsvalueToObject().property("from").jsvalueToFunction();
        return (JSArray) from.call(null, arrayLike, new JSFunction(ctx, "_callback") {
            @SuppressWarnings("unused")
            public JSValue _callback(JSValue currentValue, int index, JSArray array) {
                return mapFn.callback(currentValue, index, array);
            }
        }).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.isArray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/n_isArray
     *
     * @param value the value to test
     * @return true if 'value' is an array, false otherwise
     */
    public static boolean NJSisArray(JSValue value) {
        if (value == null) return false;
        JSFunction NJSisArray = value.getContext().property("Array").jsvalueToObject().property("n_isArray").jsvalueToFunction();
        return NJSisArray.call(null, value).jsvalueToBoolean();
    }

    /**
     * JavaScript Array.of(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/of
     *
     * @param ctx    The JSContext in which to create the array
     * @param params Elements to add to the array
     * @return the new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> of(JSContext ctx, Object... params) {
        JSFunction of = ctx.property("Array").jsvalueToObject().property("of").jsvalueToFunction();
        return (JSArray) of.apply(null, params).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.concat(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/concat
     *
     * @param params values to concantenate to the array
     * @return a new JSArray
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> concat(Object... params) {
        JSArray concat = (JSArray) property("concat").jsvalueToFunction().apply(this, params).jsvalueToJSArray();
        concat.mType = mType;
        return concat;
    }

    /**
     * JavaScript Array.prototype.pop(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/pop
     *
     * @return the popped element
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        return (T) property("pop").jsvalueToFunction().call(this).jsvalueToJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.push(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/push
     *
     * @param elements The elements to push on the array
     * @return new size of the mutated array
     */
    @SuppressWarnings("unchecked")
    public int push(T... elements) {
        return property("push").jsvalueToFunction().apply(this, elements).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.shift(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/shift
     *
     * @return the element shifted off the front of the array
     */
    @SuppressWarnings("unchecked")
    public T shift() {
        JSValue shifted = property("shift").jsvalueToFunction().call(this);
        if (shifted.jsvalueIsUndefined()) return null;
        else return (T) shifted.jsvalueToJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.splice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/splice
     *
     * @param start       the index to start splicing from (inclusive)
     * @param deleteCount the number of elements to remove
     * @param elements    the elements to insert into the array at index 'start'
     * @return a new array containing the removed elements
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> splice(int start, int deleteCount, T... elements) {
        ArrayList<Object> args = new ArrayList<>(Arrays.asList((Object[]) elements));
        args.add(0, deleteCount);
        args.add(0, start);
        JSArray<T> splice = (JSArray<T>) (property("splice").jsvalueToFunction().apply(this, args.toArray()).jsvalueToJSArray());
        splice.mType = mType;
        return splice;
    }

    /**
     * JavaScript Array.prototype.toLocaleString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/toLocaleString
     * Note: AndroidJSCore does not include the localization library by default, as it adds too
     * much data to the build.  This function is supported for completeness, but localized values will
     * show as empty strings
     *
     * @return a localized string representation of the array
     */
    public String toLocaleString() {
        return property("toLocaleString").jsvalueToFunction().call(this).toString();
    }

    /**
     * JavaScript Array.prototype.unshift(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/unshift
     *
     * @param elements The values to add to the front of the array
     * @return the new size of the mutated array
     */
    @SuppressWarnings("unchecked")
    public int unshift(T... elements) {
        return property("unshift").jsvalueToFunction().apply(this, elements).jsvalueToNumber().intValue();
    }

    /** JavaScript methods **/

    /**
     * Interface containing a condition test callback function
     *
     * @param <T> Parameterized type of array elements
     */
    public interface EachBooleanCallback<T> {
        /**
         * A function to test each element of an array for a condition
         *
         * @param currentValue value to test
         * @param index        index in 'array'
         * @param array        array being traversed
         * @return true if condition is met, false otherwise
         *
         */
        boolean callback(T currentValue, int index, JSArray<T> array);
    }

    /**
     * Interface containing a function to call on each element of an array
     *
     * @param <T> Parameterized type of array elements
     */
    public interface ForEachCallback<T> {
        /**
         * A function to call on each element of the array
         *
         * @param currentValue current value in the array
         * @param index        index in 'array'
         * @param array        array being traversed
         */
        void callback(T currentValue, int index, JSArray<T> array);
    }

    /**
     * Interface containing a reduce function
     *
     */
    public interface ReduceCallback {
        /**
         * A function to reduce a mapped value into an accumulator
         *
         * @param previousValue previous value of the accumulator
         * @param currentValue  value of mapped item
         * @param index         index in 'array'
         * @param array         map array being traversed
         * @return new accumulator value
         *
         */
        JSValue callback(JSValue previousValue, JSValue currentValue, int index, JSArray<JSValue> array);
    }

    /**
     * Interface containing a compare function callback for sort
     *
     * @param <T> Parameterized type of array elements
     */
    public interface SortCallback<T> {
        /**
         * A function for comparing values in a sort
         *
         * @param a first value
         * @param b second value
         * @return 0 if values are the same, negative if 'b' comes before 'a', and positive if 'a' comes
         * before 'b'
         *
         */
        double callback(T a, T b);
    }

    protected JSValue each(JSFunction callback, JSObject thiz, String each) {
        return property(each).jsvalueToFunction().call(this, callback, thiz);
    }

    protected JSValue each(final EachBooleanCallback<T> callback, String each) {
        return property(each).jsvalueToFunction().call(this, new JSFunction(context, "_callback") {
            @SuppressWarnings("unchecked,unused")
            public boolean _callback(T currentValue, int index, JSArray array) {
                return callback.callback((T) ((JSValue) currentValue).jsvalueToJavaObject(mType), index, array);
            }
        });
    }

    protected JSValue each(final ForEachCallback<T> callback, String each) {
        return property(each).jsvalueToFunction().call(this, new JSFunction(context, "_callback") {
            @SuppressWarnings("unchecked,unused")
            public void _callback(T currentValue, int index, JSArray array) {
                callback.callback((T) ((JSValue) currentValue).jsvalueToJavaObject(mType), index, array);
            }
        });
    }

    protected JSValue each(final ReduceCallback callback, String each, Object initialValue) {
        return property(each).jsvalueToFunction().call(this, new JSFunction(context, "_callback") {
            @SuppressWarnings("unused")
            public JSValue _callback(JSValue previousValue, JSValue currentValue, int index, JSArray<JSValue> array) {
                return callback.callback(previousValue, currentValue, index, array);
            }
        }, initialValue);
    }

    /**
     * An array entry Iterator
     *
     * @param <U> Parameterized type of array elements
     *
     */
    public class EntriesIterator<U> extends JSIterator<Map.Entry<Integer, U>> {
        protected EntriesIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next entry in the array
         *
         * @return a Map.Entry element containing the index and value
         */
        @Override
        @SuppressWarnings("unchecked")
        public Map.Entry<Integer, U> next() {
            JSObject next = jsnext().value().jsvalueToObject();
            return new AbstractMap.SimpleEntry<>(next.propertyAtIndex(0).jsvalueToNumber().intValue(), (U) next.propertyAtIndex(1).jsvalueToJavaObject(mType));
        }
    }

    /**
     * JavaScript Array.prototype.entries(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/entries
     *
     * @return an entry iterator
     *
     */
    public EntriesIterator<T> entries() {
        return new EntriesIterator<>(property("entries").jsvalueToFunction().call(this).jsvalueToObject());
    }

    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return true if every element in the array meets the condition, false otherwise
     *
     */
    public boolean every(JSFunction callback, JSObject thiz) {
        return each(callback, thiz, "every").jsvalueToBoolean();
    }

    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     *
     * @param callback the JavaScript function to call on each element
     * @return true if every element in the array meets the condition, false otherwise
     *
     */
    public boolean every(JSFunction callback) {
        return every(callback, null);
    }

    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     *
     * @param callback the Java function to call on each element
     * @return true if every element in the array meets the condition, false otherwise
     *
     */
    public boolean every(final EachBooleanCallback<T> callback) {
        return each(callback, "every").jsvalueToBoolean();
    }

    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return the first value matching the condition set by the function
     *
     */
    @SuppressWarnings("unchecked")
    public T find(JSFunction callback, JSObject thiz) {
        return (T) each(callback, thiz, "find").jsvalueToJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     *
     * @param callback the JavaScript function to call on each element
     * @return the first value matching the condition set by the function
     *
     */
    public T find(JSFunction callback) {
        return find(callback, null);
    }

    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     *
     * @param callback the Java function to call on each element
     * @return the first value matching the condition set by the function
     *
     */
    @SuppressWarnings("unchecked")
    public T find(final EachBooleanCallback<T> callback) {
        return (T) each(callback, "find").jsvalueToJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return the index of the first value matching the condition set by the function
     *
     */
    public int findIndex(JSFunction callback, JSObject thiz) {
        return each(callback, thiz, "findIndex").jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     *
     * @param callback the JavaScript function to call on each element
     * @return the index of the first value matching the condition set by the function
     *
     */
    public int findIndex(JSFunction callback) {
        return findIndex(callback, null);
    }

    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     *
     * @param callback the Java function to call on each element
     * @return the index of the first value matching the condition set by the function
     *
     */
    public int findIndex(final EachBooleanCallback<T> callback) {
        return each(callback, "findIndex").jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     *
     */
    public void forEach(JSFunction callback, JSObject thiz) {
        each(callback, thiz, "forEach");
    }

    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     *
     * @param callback the JavaScript function to call on each element
     *
     */
    public void forEach(JSFunction callback) {
        forEach(callback, null);
    }

    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     *
     * @param callback the Java function to call on each element
     *
     */
    public void forEach(final ForEachCallback<T> callback) {
        each(callback, "forEach");
    }

    /**
     * JavaScript Array.prototype.includes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
     *
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from
     * @return true if the element exists in the array, false otherwise
     *
     */
    public boolean includes(T element, int fromIndex) {
        return property("includes").jsvalueToFunction().call(this, element, fromIndex).jsvalueToBoolean();
    }

    /**
     * JavaScript Array.prototype.includes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
     *
     * @param element the value to search for
     * @return true if the element exists in the array, false otherwise
     *
     */
    public boolean includes(T element) {
        return includes(element, 0);
    }

    /**
     * JavaScript Array.prototype.indexOf(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf
     *
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from
     * @return index of the first instance of 'element', -1 if not found
     *
     */
    public int indexOf(T element, int fromIndex) {
        return property("indexOf").jsvalueToFunction().call(this, element, fromIndex).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.join(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/join
     *
     * @param separator the separator to use between values
     * @return a string representation of the joined array
     *
     */
    public String join(String separator) {
        return property("join").jsvalueToFunction().call(this, separator).toString();
    }

    /**
     * JavaScript Array.prototype.join(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/join
     *
     * @return a string representation of the joined array with a comma separator
     *
     */
    public String join() {
        return property("join").jsvalueToFunction().call(this).toString();
    }

    /**
     * An array key Iterator
     *
     *
     */
    public class KeysIterator extends JSIterator<Integer> {
        protected KeysIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next key in the array
         *
         * @return the array index
         */
        @Override
        @SuppressWarnings("unchecked")
        public Integer next() {
            Next jsnext = jsnext();

            if (jsnext.value().jsvalueIsUndefined()) return null;

            JSValue next = jsnext.value();
            return (Integer) next.jsvalueToJavaObject(Integer.class);
        }
    }

    /**
     * JavaScript Array.prototype.keys(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/keys
     *
     * @return An array index iterator
     *
     */
    public KeysIterator keys() {
        return new KeysIterator(property("keys").jsvalueToFunction().call(this).jsvalueToObject());
    }

    /**
     * JavaScript Array.prototype.lastIndexOf(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/lastIndexOf
     *
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from (reverse order)
     * @return index of the last instance of 'element', -1 if not found
     *
     */
    public int lastIndexOf(T element, int fromIndex) {
        return property("lastIndexOf").jsvalueToFunction().call(this, element, fromIndex).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     *
     * @param callback     The JavaScript reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduce(JSFunction callback, Object initialValue) {
        return property("reduce").jsvalueToFunction().call(this, callback, initialValue);
    }

    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     *
     * @param callback The JavaScript reduce function to call
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduce(JSFunction callback) {
        return property("reduce").jsvalueToFunction().call(this, callback);
    }

    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     *
     * @param callback     The Java reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduce(final ReduceCallback callback, Object initialValue) {
        return each(callback, "reduce", initialValue);
    }

    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     *
     * @param callback The Java reduce function to call
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduce(final ReduceCallback callback) {
        return reduce(callback, null);
    }

    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     *
     * @param callback     The JavaScript reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduceRight(JSFunction callback, Object initialValue) {
        return property("reduceRight").jsvalueToFunction().call(this, callback, initialValue);
    }

    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     *
     * @param callback The JavaScript reduce function to call
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduceRight(JSFunction callback) {
        return property("reduceRight").jsvalueToFunction().call(this, callback);
    }

    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     *
     * @param callback     The Java reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduceRight(final ReduceCallback callback, Object initialValue) {
        return each(callback, "reduceRight", initialValue);
    }

    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     *
     * @param callback The Java reduce function to call
     * @return A reduction of the mapped array
     *
     */
    public JSValue reduceRight(final ReduceCallback callback) {
        return reduceRight(callback, null);
    }

    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return true if some element in the array meets the condition, false otherwise
     *
     */
    public boolean some(JSFunction callback, JSObject thiz) {
        return each(callback, thiz, "some").jsvalueToBoolean();
    }

    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     *
     * @param callback the JavaScript function to call on each element
     * @return true if some element in the array meets the condition, false otherwise
     *
     */
    public boolean some(JSFunction callback) {
        return some(callback, null);
    }

    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     *
     * @param callback the Java function to call on each element
     * @return true if some element in the array meets the condition, false otherwise
     *
     */
    public boolean some(final EachBooleanCallback<T> callback) {
        return each(callback, "some").jsvalueToBoolean();
    }

    /**
     * An array value iterator
     *
     * @param <U> Parameterized type of array elements
     *
     */
    public class ValuesIterator<U> extends JSIterator<U> {
        protected ValuesIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next element of the array
         *
         * @return the next value in the array
         */
        @Override
        @SuppressWarnings("unchecked")
        public U next() {
            Next jsnext = jsnext();
            return (U) jsnext.value().jsvalueToJavaObject(mType);
        }
    }

    /**
     * JavaScript Array.prototype.values(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/values
     *
     * @return an array value iterator
     *
     */
    public ValuesIterator<T> values() {
        return new ValuesIterator<>(property("values").jsvalueToFunction().call(this).jsvalueToObject());
    }

    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     *
     * @param target index to copy sequence to
     * @param start  index from which to start copying from
     * @param end    index from which to end copying from
     * @return this (mutable operation)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> copyWithin(int target, int start, int end) {
        return (JSArray<T>) property("copyWithin").jsvalueToFunction().call(this, target, start, end).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     *
     * @param target index to copy sequence to
     * @param start  index from which to start copying from
     * @return this (mutable operation)
     *
     */
    public JSArray<T> copyWithin(int target, int start) {
        return copyWithin(target, start, size());
    }

    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     *
     * @param target index to copy sequence to
     * @return this (mutable operation)
     *
     */
    public JSArray<T> copyWithin(int target) {
        return copyWithin(target, 0);
    }

    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     *
     * @param value the value to fill
     * @param start the index to start filling
     * @param end   the index (exclusive) to stop filling
     * @return this (mutable)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> fill(T value, int start, int end) {
        return (JSArray<T>) (property("fill").jsvalueToFunction().call(this, value, start, end).jsvalueToJSArray());
    }

    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     *
     * @param value the value to fill
     * @param start the index to start filling
     * @return this (mutable)
     *
     */
    public JSArray<T> fill(T value, int start) {
        return fill(value, start, size());
    }

    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     *
     * @param value the value to fill
     * @return this (mutable)
     *
     */
    public JSArray<T> fill(T value) {
        return fill(value, 0);
    }

    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return a new filtered array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> filter(JSFunction callback, JSObject thiz) {
        JSArray<T> filter = (JSArray<T>) (each(callback, thiz, "filter").jsvalueToJSArray());
        filter.mType = mType;
        return filter;
    }

    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     *
     * @param callback the JavaScript function to call on each element
     * @return a new filtered array
     *
     */
    public JSArray<T> filter(JSFunction callback) {
        return filter(callback, null);
    }

    @SuppressWarnings("unchecked")
    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     *
     * @param callback the Java function to call on each element
     * @return a new filtered array
     */ public JSArray<T> filter(final EachBooleanCallback<T> callback) {
        JSArray<T> filter = (JSArray<T>) (each(callback, "filter").jsvalueToJSArray());
        filter.mType = mType;
        return filter;
    }

    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     *
     * @param callback the JavaScript function to call on each element
     * @param thiz     the 'this' value passed to callback
     * @return a new mapped array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<JSValue> map(JSFunction callback, JSObject thiz) {
        return (JSArray<JSValue>) each(callback, thiz, "map").jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     *
     * @param callback the JavaScript function to call on each element
     * @return a new mapped array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<JSValue> map(JSFunction callback) {
        return map(callback, null);
    }

    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     *
     * @param callback the Java function to call on each element
     * @return a new mapped array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<JSValue> map(final MapCallback<T> callback) {
        return (JSArray<JSValue>) property("map").jsvalueToFunction().call(this, new JSFunction(context, "_callback") {
            @SuppressWarnings("unchecked,unused")
            public JSValue _callback(T currentValue, int index, JSArray<T> array) {
                return callback.callback((T) ((JSValue) currentValue).jsvalueToJavaObject(mType), index, array);
            }
        }).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.reverse(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reverse
     *
     * @return this (mutable)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> reverse() {
        return (JSArray<T>) (property("reverse").jsvalueToFunction().call(this).jsvalueToJSArray());
    }

    /**
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     *
     * @param begin the index to begin slicing (inclusive)
     * @param end   the index to end slicing (exclusive)
     * @return the new sliced array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice(int begin, int end) {
        return (JSArray<T>) property("slice").jsvalueToFunction().call(this, begin, end).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     *
     * @param begin the index to begin slicing (inclusive)
     * @return the new sliced array
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice(int begin) {
        return (JSArray<T>) property("slice").jsvalueToFunction().call(this, begin).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     *
     * @return the new sliced array (essentially a copy of the original array)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice() {
        return (JSArray<T>) property("slice").jsvalueToFunction().call(this).jsvalueToJSArray();
    }

    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     *
     * @param compare the JavaScript compare function to use for sorting
     * @return this (mutable)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort(JSFunction compare) {
        return (JSArray<T>) (property("sort").jsvalueToFunction().call(this, compare).jsvalueToJSArray());
    }

    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     *
     * @param callback the Java compare function to use for sorting
     * @return this (mutable)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort(final SortCallback<T> callback) {
        return (JSArray<T>) (property("sort").jsvalueToFunction().call(this, new JSFunction(context, "_callback") {
            @SuppressWarnings("unused")
            public double _callback(T a, T b) {
                return callback.callback((T) ((JSValue) a).jsvalueToJavaObject(mType), (T) ((JSValue) b).jsvalueToJavaObject(mType));
            }
        }).jsvalueToJSArray());
    }

    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     *
     * @return this (mutable)
     *
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort() {
        return (JSArray<T>) (property("sort").jsvalueToFunction().call(this).jsvalueToJSArray());
    }
}
