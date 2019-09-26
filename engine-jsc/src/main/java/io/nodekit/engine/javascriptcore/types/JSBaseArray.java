package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import android.webkit.ValueCallback;

/**
 * A convenience class for handling JavaScript arrays. Implements java.util.List
 * interface for simple integration with Java methods.
 */
public abstract class JSBaseArray<T> extends JSFunction implements List<T> {

    protected Class<T> mType;
    protected int mLeftBuffer = 0;
    protected int mRightBuffer = 0;
    protected JSBaseArray<T> mSuperList = null;

    protected JSBaseArray(long valueRef, JSContext ctx, Class<T> cls) {
        super(valueRef, ctx);
        mType = cls;
    }

    protected JSBaseArray(JSBaseArray<T> superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        mType = cls;
        mLeftBuffer = leftBuffer;
        mRightBuffer = rightBuffer;
        context = superList.context;
        valueRef = superList.valueRef();
        mSuperList = superList;
    }

    protected JSBaseArray(JSContext ctx, Class<T> cls) {
        context = ctx;
        mType = cls;
    }

    /**
     * Converts to a static array with elements of class 'clazz'
     *
     * @param clazz The class to convert the elements to (Integer.class,
     *              Double.class, String.class, JSValue.class, etc.)
     * @return The captured static array
     *
     */
    public Object[] toArray(Class clazz) {
        int count = size();

        Object[] array = (Object[]) Array.newInstance(clazz, count);
        for (int i = 0; i < count; i++) {
            array[i] = elementAtIndex(i).jsvalueToJavaObject(clazz);
        }
        return array;
    }

    /**
     * Extracts Java JSValue array from JavaScript array
     *
     * @return JavaScript array as Java array of JSValues
     * @see List#toArray()
     */
    @Override
    @NonNull
    public Object[] toArray() {
        int count = size();

        Object[] array = (Object[]) Array.newInstance(Object.class, count);
        for (int i = 0; i < count; i++) {
            array[i] = elementAtIndex(i).jsvalueToJavaObject();
        }
        return array;
    }

    //
    // NodeKit Javascript Interface
    //

    public void valueAtIndex(int index, ValueCallback<String> completionHandler) {
        String result = get(index).toString();
        completionHandler.onReceiveValue(result);
    }

    public void setValue(Object value, int atIndex) {
        set(atIndex, (T) value);
    }

    /**
     * Gets JSValue at 'index'
     *
     * @param index Index of the element to get
     * @return The JSValue at index 'index'
     * @see List#get(int)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get(final int index) {
        int count = size();
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return (T) elementAtIndex(index).jsvalueToJavaObject(mType);
    }

    /**
     * Adds a JSValue to the end of an array. The Java Object is converted to a
     * JSValue.
     *
     * @param val The Java object to add to the array, will get converted to a
     *            JSValue
     * @see List#add(Object)
     *
     */
    @Override
    public boolean add(final T val) {
        int count = size();
        elementAtIndex(count, val);
        return true;
    }

    /**
     * @see List#size()
     *
     */
    @Override
    public int size() {
        if (mSuperList == null) {
            return property("length").jsvalueToNumber().intValue();
        } else {
            return Math.max(0, mSuperList.size() - mLeftBuffer - mRightBuffer);
        }
    }

    protected JSValue arrayElement(final int index) {
        return propertyAtIndex(index);
    }

    protected void arrayElement(final int index, final T value) {
        propertyAtIndex(index, value);
    }

    protected JSValue elementAtIndex(final int index) {
        if (mSuperList == null)
            return arrayElement(index);
        else
            return mSuperList.elementAtIndex(index + mLeftBuffer);
    }

    protected void elementAtIndex(final int index, final T value) {
        if (mSuperList == null)
            arrayElement(index, value);
        else
            mSuperList.elementAtIndex(index + mLeftBuffer, value);
    }

    /**
     * @see List#isEmpty() ()
     *
     */
    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * @see List#contains(Object) ()
     *
     */
    @Override
    public boolean contains(final Object object) {
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(object))
                return true;
        }
        return false;
    }

    /**
     * @see List#iterator()
     *
     */
    @Override
    public @NonNull Iterator<T> iterator() {
        return new ArrayIterator();
    }

    /**
     * @see List#toArray(Object[])
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public <U> U[] toArray(final @NonNull U[] elemArray) {
        if (size() > elemArray.length) {
            return (U[]) toArray();
        }
        ArrayIterator iterator = new ArrayIterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            elemArray[index++] = (U) next;
        }
        for (int i = index; i < elemArray.length; i++) {
            elemArray[i] = null;
        }
        return elemArray;
    }

    /**
     * @see List#remove(Object)
     *
     */
    @Override
    public boolean remove(final Object object) {
        ArrayIterator listIterator = new ArrayIterator();
        while (listIterator.hasNext()) {
            if (listIterator.next().equals(object)) {
                listIterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * @see List#containsAll(Collection)
     *
     */
    @Override
    public boolean containsAll(final @NonNull Collection<?> collection) {
        for (Object item : collection.toArray()) {
            if (!contains(item))
                return false;
        }
        return true;
    }

    /**
     * @see List#addAll(Collection)
     *
     */
    @Override
    public boolean addAll(final @NonNull Collection<? extends T> collection) {
        return addAll(size(), collection);
    }

    /**
     * @see List#addAll(int, Collection)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(final int index, final @NonNull Collection<? extends T> collection) {
        int i = index;
        for (Object item : collection.toArray()) {
            add(i++, (T) item);
        }
        return true;
    }

    /**
     * @see List#removeAll(Collection)
     *
     */
    @Override
    public boolean removeAll(final @NonNull Collection<?> collection) {
        boolean any = false;
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            T compare = listIterator.next();
            for (Object element : collection) {
                if (compare.equals(element)) {
                    listIterator.remove();
                    any = true;
                    break;
                }
            }
        }
        return any;
    }

    /**
     * @see List#retainAll(Collection)
     *
     */
    @Override
    public boolean retainAll(final @NonNull Collection<?> collection) {
        boolean any = false;
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            T compare = listIterator.next();
            boolean remove = true;
            for (Object element : collection) {
                if (compare.equals(element)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                listIterator.remove();
                any = true;
            }
        }
        return any;
    }

    /**
     * @see List#clear()
     *
     */
    @Override
    public void clear() {
        for (int i = size(); i > 0; --i) {
            remove(i - 1);
        }
    }

    /**
     * @see List#set(int, Object)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public T set(final int index, final T element) {
        int count = size();
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        JSValue oldValue = elementAtIndex(index);
        elementAtIndex(index, element);
        return (T) oldValue.jsvalueToJavaObject(mType);
    }

    /**
     * @see List#add(int, Object)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(final int index, final T element) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see List#remove(int)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public T remove(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see List#indexOf(Object)
     *
     */
    @Override
    public int indexOf(final Object object) {
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            if (listIterator.next().equals(object)) {
                return listIterator.nextIndex() - 1;
            }
        }
        return -1;
    }

    /**
     * @see List#lastIndexOf(Object)
     *
     */
    @Override
    public int lastIndexOf(final Object object) {
        ListIterator<T> listIterator = listIterator(size());
        while (listIterator.hasPrevious()) {
            if (listIterator.previous().equals(object)) {
                return listIterator.previousIndex() + 1;
            }
        }
        return -1;
    }

    /**
     * @see List#listIterator()
     *
     */
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    /**
     * @see List#listIterator(int)
     *
     */
    @Override
    @NonNull
    public ListIterator<T> listIterator(final int index) {
        return new ArrayIterator(index);
    }

    /**
     * @see List#equals(Object)
     *
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof List<?>)) {
            return false;
        }
        List<?> otherList = (List<?>) other;
        if (size() != otherList.size()) {
            return false;
        }
        Iterator<T> iterator = iterator();
        Iterator<?> otherIterator = otherList.iterator();
        while (iterator.hasNext() && otherIterator.hasNext()) {
            T next = iterator.next();
            Object otherNext = otherIterator.next();
            if (!next.equals(otherNext)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see List#hashCode()
     *
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T e : this) {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    private class ArrayIterator implements ListIterator<T> {
        private int current;
        private Integer modifiable = null;

        public ArrayIterator() {
            this(0);
        }

        public ArrayIterator(int index) {
            if (index > size())
                index = size();
            if (index < 0)
                index = 0;
            current = index;
        }

        @Override
        public boolean hasNext() {
            return (current < size());
        }

        @Override
        public boolean hasPrevious() {
            return (current > 0);
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            modifiable = current;
            return get(current++);
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            modifiable = --current;
            return get(current);
        }

        @Override
        public void remove() {
            if (modifiable == null)
                throw new NoSuchElementException();

            JSBaseArray.this.remove(modifiable.intValue());
            current = modifiable;
            modifiable = null;
        }

        @Override
        public int nextIndex() {
            return current;
        }

        @Override
        public int previousIndex() {
            return current - 1;
        }

        @Override
        public void set(T value) {
            if (modifiable == null)
                throw new NoSuchElementException();

            JSBaseArray.this.set(modifiable, value);
        }

        @Override
        public void add(T value) {
            JSBaseArray.this.add(current++, value);
            modifiable = null;
        }
    }
}
