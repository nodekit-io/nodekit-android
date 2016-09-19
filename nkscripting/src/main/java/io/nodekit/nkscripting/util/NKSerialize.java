/*
* nodekit.io
*
* Copyright (c) 2016 OffGrid Networks. All Rights Reserved.
* Portions Copyright (c) 2016 LazyDeer under MIT License
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.nodekit.nkscripting.util;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unchecked")
public class NKSerialize {

    @Nullable
    public static <T> T deserialize(String json) {
        try {
            Object jsonTest = new JSONTokener(json).nextValue();
            if(jsonTest == JSONObject.NULL) {
                return null;
            } else if (jsonTest instanceof JSONObject)
            {
                JSONObject jsonObject = new JSONObject(json);
                return (T)NKSerialize.toMap(jsonObject);
            }
            else if (jsonTest instanceof JSONArray) {
                 JSONArray jsonArray = new JSONArray(json);
                return (T)NKSerialize.toList(jsonArray);
            }
        }  catch (Exception e) {
            //
        }

        return null;
    }

    @Nullable
    public static Object deserialize(String json, Class type) {
        try {
            Object jsonTest = new JSONTokener(json).nextValue();
            if (jsonTest instanceof JSONObject)
            {
                //you have an object
                JSONObject jsonObject = new JSONObject(json);
                return NKSerialize.jsonToNative(jsonObject, type);
            }
            else if (jsonTest instanceof JSONArray) {
                //you have an array
                JSONArray jsonArray = new JSONArray(json);
                return NKSerialize.jsonArrayToNativeList(jsonArray, type);
            }
        }  catch (Exception e) {
            //
        }

        return null;
    }

    protected static Map<String, Object> toMap(JSONObject object)  {

        Map<String, Object> map = new HashMap<String, Object>();

        try {

            Iterator<String> keysItr = object.keys();
            while(keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = object.get(key);

                if(value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                }

                else if(value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }

                map.put(key, value);
            }

        } catch (Exception ex)
        {
            NKLogging.log(ex.toString());
        }

        return map;
    }

    protected static List<Object> toList(JSONArray array) throws JSONException {

        List<Object> list = new ArrayList<Object>();

        try {

            for(int i = 0; i < array.length(); i++) {
                Object value = array.get(i);
                if(value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                }

                else if(value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                list.add(value);
            }
        } catch (Exception ex)
        {
            NKLogging.log(ex.toString());
        }

        return list;
    }

    public static String serialize(@Nullable Object obj) {

        if (null == obj) {
            return "undefined";
        }

        try {


            Class type = obj.getClass();

            if (obj instanceof String) {
                return JSONObject.quote((String) obj);
            } else if (isNumberType(type)) {
                return obj.toString();
            } else if (type == Boolean.class) {
                return ((Boolean) obj) ? "true" : "false";
            } else if (type == Date.class) {
                SimpleDateFormat formatter;

                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                return "\"" + formatter.format((Date) obj) + "\"";
            } else if (obj instanceof Collection<?>) {
                List<?> list = (List<?>) obj;
                StringBuilder sb = new StringBuilder();
                Boolean started = false;
                for (Object child : list) {
                    if (!started) {
                        sb.append("[");
                        started = true;
                    } else {
                        sb.append(",");
                    }
                    sb.append(NKSerialize.serialize(child));

                }
                if (started) {
                    sb.append("]");
                }
                return sb.toString();
            } else if (obj instanceof Map<?, ?>) {
                Map<String, ?> map = (Map<String, ?>) obj;
                StringBuilder sb = new StringBuilder();
                Boolean started = false;
                for (String key : map.keySet()) {
                    if (!started) {
                        sb.append("{");
                        started = true;
                    } else {
                        sb.append(",");
                    }
                    sb.append("\"").append(NKSerialize.serialize(key)).append("\": ").append(NKSerialize.serialize(map.get(key)));

                }
                if (started) {
                    sb.append("}");
                }
                return sb.toString();
            }

        }
        catch (Exception ex)
        {
            NKLogging.log(ex.toString());
        }

        return obj.toString();

    }

    public static String serializeArgs(Object[] list) {
        StringBuilder sb = new StringBuilder();
        Boolean started = false;
        for (Object child : list) {
            if (!started)
            {
                started = true;
            } else
            {
                sb.append(",");
            }
            sb.append(NKSerialize.serialize(child));

        }
        return sb.toString();
    }

    protected static final Set<Class> NUMBER_TYPES = new HashSet(Arrays.asList(
            Short.class, Integer.class, Long.class, Float.class, Double.class));

    protected static boolean isNumberType(Class clazz) {
        return NUMBER_TYPES.contains(clazz);
    }

    // ANNOTATIONS AND INTERFACES
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SerializeIgnore {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SerializeBy {
        Class value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SerializeCollectionInitBy {
        Class value();
    }

    public interface Serializer<T> {
        String serialization(T t);
        T deserialization(String s);
    }


    protected static <T> T fromJson(JSONObject jsonObject, Class type) {
        return NKSerialize.jsonToNative(jsonObject, type);
    }

    protected static <T> Collection<T> fromJsonArray(JSONArray array, Class type) {
        return NKSerialize.jsonArrayToNativeList(array,type);
    }

    protected static <T> JSONObject toJson(T t){
        return NKSerialize.nativeToJson(t);
    }

    private static final Set<Class> WRAPPER_TYPES = new HashSet(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));

    private static boolean isWrapperType(Class clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }


    @Nullable
    private static <T> T jsonToNative(JSONObject jsonObject, Class type) {
        T t = null;
        try {
            t = (T) type.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != t) {
            t = parseEntity(jsonObject, t);
            return t;
        }
        return null;
    }

    private static <T> Collection<T> jsonArrayToNativeList(JSONArray array, Class type) {
        Collection collection = new ArrayList<>();
        BasicType basicType = isBasicType(type);
        try {
            if (basicType != BasicType.OTHER_TYPE) {
                collection = setCollectionBasicValue(collection, array, type);
            } else {
                collection = setCollectionCustomValue(collection, array, type);
            }
        } catch (Exception e) {
            // ignore
        }
        return collection;
    }

    private static <T> JSONObject nativeToJson(T t) {
        JSONObject jsonObject = new JSONObject();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(SerializeIgnore.class)) {
                continue;
            }
            field.setAccessible(true);
            String name = field.getName();
            try {
                Object value = parseFieldValue(field, t);
                if (null == value || value.equals("null")) {
                    jsonObject.put(name, "");
                } else {
                    jsonObject.put(name, value);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return jsonObject;
    }


    private static <T> T parseEntity(JSONObject jsonObject, T t) {
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(SerializeIgnore.class)) {
                parseField(field, jsonObject, t);
            }
        }
        return t;
    }

    private static <T> void parseField(Field field, JSONObject jsonObject, T t) {
        field.setAccessible(true);
        Type fieldType = field.getType();
        Type fieldGenericType = field.getGenericType();
        String fieldName = field.getName();
        try {
            if (field.isAnnotationPresent(SerializeBy.class)) {
                Class s = field.getAnnotation(SerializeBy.class).value();
                Serializer serializer = (Serializer) s.newInstance();
                field.set(t, serializer.deserialization(jsonObject.getString(fieldName)));
                return;
            }
            if (fieldType == fieldGenericType) {
                BasicType basicType = isBasicType(fieldType);
                if (basicType != BasicType.OTHER_TYPE) {
                    setBasicValue(field, jsonObject, t, fieldName, basicType);
                } else {
                    setCustomValue(field, jsonObject, t, fieldType, fieldName);
                }
            } else {
                ParameterizedType integerListType = (ParameterizedType) fieldGenericType;
                Type genericType = integerListType.getActualTypeArguments()[0];
                BasicType basicType = isBasicType(genericType);
                Collection collection = null;
                if (field.isAnnotationPresent(SerializeCollectionInitBy.class)) {
                    Class c = field.getAnnotation(SerializeCollectionInitBy.class).value();
                    collection = (Collection) c.newInstance();
                }
                if (collection == null){
                    collection = new ArrayList();
                }
                JSONArray jsonArray = jsonObject.getJSONArray(fieldName);
                if (basicType != BasicType.OTHER_TYPE) {
                    collection = setCollectionBasicValue(collection, jsonArray, genericType);
                    field.set(t, collection);
                } else {
                    collection = setCollectionCustomValue(collection, jsonArray, genericType);
                    field.set(t, collection);
                }
            }
        } catch (Exception e) {
            if (e instanceof InstantiationException) {
                throw new RuntimeException(new Exception("@SerializeCollectionInitBy must specific a collection implementation subclass "));
            }
        }
    }

    private static  Collection setCollectionCustomValue(
            Collection collection, JSONArray jsonArray, Type genericType) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Object o = ((Class) genericType).cast(generateInstance(genericType));
            o = parseEntity(jsonObject, o);
            collection.add(o);
        }
        return collection;
    }

    private static  Collection setCollectionBasicValue(
            Collection collection, JSONArray jsonArray, Type genericType) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            collection.add(((Class) genericType).cast(jsonArray.get(i)));
        }
        return collection;
    }

    private static <T> void setCustomValue(Field field, JSONObject jsonObject, T t,
                                    Type fieldType, String fieldName) throws Exception {
        JSONObject value = jsonObject.getJSONObject(fieldName);
        Object o = generateInstance(fieldType);
        o = parseEntity(value, o);
        field.set(t, o);
    }

    private static <T> void setBasicValue(Field field, JSONObject jsonObject,
                                   T t, String fieldName, BasicType basicType) throws Exception {
        Object value = null;
        switch (basicType) {
            case INT:
                value = jsonObject.getInt(fieldName);
                break;
            case LONG:
                value = jsonObject.getLong(fieldName);
                break;
            case DOUBLE:
                value = jsonObject.getDouble(fieldName);
                break;
            case FLOAT:
                value = (float) jsonObject.getDouble(fieldName);
                break;
            case BOOLEAN:
                value = jsonObject.getBoolean(fieldName);
                break;
            case STRING:
                value = jsonObject.getString(fieldName);
                break;
            default:
                value = field.getType()
                        .cast(jsonObject.get(fieldName));
        }
        field.set(t, value);

    }

    @Nullable
    private static Object generateInstance(Type type) {
        Class<?> genericsType = null;
        try {
            genericsType = Class.forName(getClassName(type));
            Object o = genericsType.newInstance();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static String CLASS_PREFIX = "class ";
    protected static String INTERFACE_PREFIX = "interface ";

    private static String getClassName(Type type) {
        String fullName = type.toString();
        if (fullName.startsWith(CLASS_PREFIX))
            return fullName.substring(CLASS_PREFIX.length());
        if (fullName.startsWith(INTERFACE_PREFIX))
            return fullName.substring(INTERFACE_PREFIX.length());
        return fullName;
    }

    private static BasicType isBasicType(Type type) {
        if (type == int.class || type == Integer.class) {
            return BasicType.INT;
        }
        if (type == long.class || type == Long.class) {
            return BasicType.LONG;
        }
        if (type == boolean.class || type == Boolean.class) {
            return BasicType.BOOLEAN;
        }
        if (type == double.class || type == Double.class) {
            return BasicType.DOUBLE;
        }
        if (type == float.class || type == Float.class) {
            return BasicType.FLOAT;
        }
        if (type == String.class) {
            return BasicType.STRING;
        } else {
            return BasicType.OTHER_TYPE;
        }
    }

    private static enum BasicType {
        INT, LONG, BOOLEAN, DOUBLE,
        FLOAT, STRING, OTHER_TYPE
    }

    private static <T> Object parseFieldValue(Field field, T t) throws Exception {
        if (field.isAnnotationPresent(SerializeBy.class)) {
            Class s = field.getAnnotation(SerializeBy.class).value();
            Serializer serializer = (Serializer) s.newInstance();
            return serializer.serialization(field.get(t));
        }
        Type fieldType = field.getType();
        Type fieldGenericType = field.getGenericType();

        if (fieldType == fieldGenericType) {
            BasicType basicType = isBasicType(fieldType);
            if (basicType != BasicType.OTHER_TYPE) {
                return field.get(t);
            } else {
                return nativeToJson(field.get(t));
            }
        } else {
            ParameterizedType integerListType = (ParameterizedType) fieldGenericType;
            Type genericType = integerListType.getActualTypeArguments()[0];
            BasicType basicType = isBasicType(genericType);

            JSONArray jsonArray = new JSONArray();
            if (basicType != BasicType.OTHER_TYPE) {
                List o = (List) field.get(t);
                for (int i = 0; i < o.size(); i++) {
                    jsonArray.put(o.get(i));
                }
            } else {
                List o = (List) field.get(t);
                for (int i = 0; i < o.size(); i++) {
                    jsonArray.put(nativeToJson(o.get(i)));
                }
            }
            return jsonArray;
        }
    }
}
