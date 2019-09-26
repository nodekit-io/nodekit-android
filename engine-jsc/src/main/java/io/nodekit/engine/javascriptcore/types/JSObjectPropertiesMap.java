package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.Serializable;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A JSObject shadow class which implements the Java Map interface. Convenient
 * for setting/getting/iterating properties.
 */
public class JSObjectPropertiesMap<V> extends JSObjectWrapper implements Map<String, V>, Serializable {

    private final Class<V> mType;

    /**
     * Creates a new Map object which operates on object 'object' and assumes type
     * 'cls'. Example: <code>
     * java.util.Map&lt;String,Double&gt; map = new JSObjectPropertiesMap&lt;String,Double&gt;(object,Double.class);
     * </code>
     *
     * @param object The JSObject whose properties will be mapped
     * @param cls    The class of the component Values; must match template
     *
     */
    public JSObjectPropertiesMap(JSObject object, Class<V> cls) {
        super(object);
        mType = cls;
    }

    /**
     * Creates a new Map object and underlying JSObject and sets initial properties
     * in 'map'. Assumes value class of type 'cls'. Example: <code>
     * java.util.Map&lt;String,Double&gt; map = new HashMap&lt;&gt;();
     * map.put("one",1.0);
     * map.put("two",2.0);
     * java.util.Map&lt;String,Double&gt; jsmap = new JSObjectPropertiesMap&lt;String,Double&gt;(context,map,Double.class)
     * </code>
     *
     * @param context The JSContext in which to create the object
     * @param map     The initial properties to set
     * @param cls     The class of the component Values; must match template
     *
     */
    public JSObjectPropertiesMap(JSContext context, Map map, Class<V> cls) {
        super(new JSObject(context, map));
        mType = cls;
    }

    /**
     * Creates a new Map object and underlying JSObject with no initial properties.
     * Assumes value class of type 'cls'. Example: <code>
     * java.util.Map&lt;String,Double&gt; jsmap = new JSObjectPropertiesMap&lt;String,Double&gt;(context,Double.class)
     * </code>
     *
     * @param context The JSContext in which to create the object
     * @param cls     The class of the component Values; must match template
     *
     */
    public JSObjectPropertiesMap(JSContext context, Class<V> cls) {
        super(new JSObject(context));
        mType = cls;
    }

    public Map<String, Object> toMap() {

        Map<String, Object> copy = new HashMap<>();

        for (Map.Entry<String, V> entry : this.entrySet()) {

            JSValue jsv = property(entry.getKey());
            Object value;
            if (jsv.jsvalueIsUndefined())
                value = null;
            else
                value = (Object) jsv.jsvalueToJavaObject();
            copy.put(entry.getKey(), value);
        }

        return copy;
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return propertyNames().length;
    }

    /**
     * @see java.util.Map#isEmpty()
     *
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     *
     */
    @Override
    public boolean containsKey(final Object key) {
        return jsobjectHasProperty(key.toString());
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     *
     */
    @Override
    public boolean containsValue(final Object value) {
        String[] properties = propertyNames();
        for (String key : properties) {
            if (property(key).equals(value))
                return true;
        }
        return false;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
        JSValue val = property(key.toString());
        if (val.jsvalueIsUndefined())
            return null;
        return (V) val.jsvalueToJavaObject(mType);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     *
     */
    @Override
    public V put(final String key, final V value) {
        final V oldValue = get(key);
        property(key, value);
        return oldValue;
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     *
     */
    @Override
    public V remove(final Object key) {
        final V oldValue = get(key);
        jsobjectDeleteProperty(key.toString());
        return oldValue;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     *
     */
    @Override
    public void putAll(final @NonNull Map<? extends String, ? extends V> map) {
        for (String key : map.keySet()) {
            put(key, map.get(key));
        }
    }

    /**
     * @see java.util.Map#clear()
     *
     */
    @Override
    public void clear() {
        for (String prop : propertyNames()) {
            jsobjectDeleteProperty(prop);
        }
    }

    /**
     * @see java.util.Map#keySet()
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Set keySet() {
        return new HashSet(Arrays.asList(propertyNames()));
    }

    /**
     * @see java.util.Map#values()
     *
     */
    @Override
    public @NonNull Collection<V> values() {
        return new AbstractList<V>() {
            @Override
            public V get(final int index) {
                String[] propertyNames = propertyNames();
                if (index > propertyNames.length) {
                    throw new IndexOutOfBoundsException();
                }
                return JSObjectPropertiesMap.this.get(propertyNames[index]);
            }

            @Override
            public int size() {
                return propertyNames().length;
            }

            @Override
            public boolean contains(Object val) {
                return containsValue(val);
            }
        };
    }

    /**
     * @see java.util.Map#entrySet()
     *
     */
    @Override
    public @NonNull Set<Entry<String, V>> entrySet() {
        return new AbstractSet<Entry<String, V>>() {

            @Override
            public @NonNull Iterator<Entry<String, V>> iterator() {
                return new SetIterator();
            }

            @Override
            public int size() {
                return propertyNames().length;
            }
        };
    }

    private class SetIterator implements Iterator<Entry<String, V>> {
        private String current = null;
        private String removal = null;

        public SetIterator() {
            String[] properties = propertyNames();
            if (properties.length > 0)
                current = properties[0];
        }

        @Override
        public boolean hasNext() {
            if (current == null)
                return false;

            // Make sure 'current' still exists
            String[] properties = propertyNames();
            for (String prop : properties) {
                if (current.equals(prop))
                    return true;
            }
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entry<String, V> next() {
            if (current == null)
                throw new NoSuchElementException();

            String[] properties = propertyNames();
            Entry<String, V> entry = null;
            int i = 0;
            for (; i < properties.length; i++) {
                if (current.equals(properties[i])) {
                    final Object key = properties[i];
                    entry = new Entry<String, V>() {
                        @Override
                        public String getKey() {
                            return (String) key;
                        }

                        @Override
                        public V getValue() {
                            return get(key);
                        }

                        @Override
                        public V setValue(V object) {
                            return put((String) key, object);
                        }
                    };
                    break;
                }
            }
            removal = current;
            if (i + 1 < properties.length)
                current = properties[i + 1];
            else
                current = null;

            if (entry != null)
                return entry;
            else
                throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (removal == null)
                throw new NoSuchElementException();

            jsobjectDeleteProperty(removal);
            removal = null;
        }
    }
}
