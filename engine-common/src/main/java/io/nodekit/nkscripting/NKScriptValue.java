package io.nodekit.nkscripting;

import java.lang.reflect.Method;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* Generic Script Value representing a value in a javascript context
*/
public interface NKScriptValue {

    //
    // JS Function
    //
  
    void callWithArguments(Object[] arguments, @Nullable ValueCallback<String> completionHandler);

    //
    // JS Object
    //

    void invokeMethod(String method, Object[] arguments, ValueCallback<String> completionHandler);

    void invokeMethod(String method, Object[] arguments);

    void defineProperty(String property, Object descriptor);

    void deleteProperty(String property);

    void hasProperty(String property, ValueCallback<String> completionHandler);

    void valueForProperty(String property, ValueCallback<String> completionHandler);

    void setValue(Object value, String forProperty);

    //
    // JS Array
    //

    void valueAtIndex(int index, ValueCallback<String> completionHandler);

    void setValue(Object value, int atIndex);

}
