package io.nodekit.engine.javascriptcore;

import io.nodekit.engine.javascriptcore.types.JSProperty;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Helper methods to register fields and methods in JSContext based on methods and properties of a given Java object
 */
public class JSProxy {
    private static final String TAG = JSObject.class.getSimpleName();

    private abstract static class JNIJSValueReturnClass implements Runnable {
        JSObject jsObject;
    }

    protected static JSObject register( final JSContext context, final Object obj, final String name) {

        JNIJSValueReturnClass payload = new JNIJSValueReturnClass() {
            @Override
            public void run() {
                jsObject = new JSObject(context);
                registerFields(jsObject, obj);
                registerMethods(jsObject, obj);
                context.property(name, jsObject);
            }
        };

        context.sync(payload);

        return payload.jsObject;

    }

    /**
     * Java fields export to JS: 1. JSProperty are exporting automatically 2. Others
     * - if the @JavascriptInterface annotation presents
     */
    private static void registerFields(JSObject jsObject, Object obj) {
        Field[] fields = obj.getClass().getFields();
        for (Field field : fields) {
            Class type = field.getType();
            if (type == JSProperty.class) {
                try {
                    Constructor<JSProperty> cons = type.getDeclaredConstructor(JSContext.class, Class.class);
                    field.setAccessible(true);
                    Class genericClass = (Class) ((ParameterizedType) field.getGenericType())
                            .getActualTypeArguments()[0];
                    JSProperty prop = cons.newInstance(jsObject.context, genericClass);
                    field.set(obj, prop);
                    jsObject.property(field.getName(), prop);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (field.isAnnotationPresent(JavascriptInterface.class)) {
                try {
                    field.setAccessible(true);
                    jsObject.property(field.getName(), field.get(obj));
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "The field " + field.getName() + " is signed "
                            + "for export to js, has a private access modificator");
                }
            }
        }
    }

    private static void registerMethods(JSObject jsObject, Object obj) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (!method.isBridge() && method.isAnnotationPresent(JavascriptInterface.class)) {
                JSFunction f = new JSFunction(jsObject.context, method, JSObject.class, obj);
                jsObject.property(method.getName(), f);
            }
        }

    }
}
