package io.nodekit.engine;

import io.nodekit.nkscripting.NKScriptValue;

import java.lang.reflect.Method;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* Generic Javascript context interface that unifies the API across webviews, V8,
* JavaScriptCore, Hermes and QuickJS
*
* Each context represents a single threaded runtime with its own global object
*/
public interface JavascriptContext {

  
	/**
     * Injects the supplied Java object into this JavaScript context, using the
     * supplied name. This allows the Java object's methods to be
     * accessed from JavaScript.   Class properties and those methods that are annotated 
     * android.webkit.JavascriptInterface will be exported.
     * <p> Note that on webview contexts, injected objects will not appear in JavaScript 
	 * until the page is next (re)loaded. 
     *
     * @param object the Java object to inject into this WebView's JavaScript
     *               context. {@code null} values are ignored.
     * @param name the name used to expose the object in JavaScript
     */
     NKScriptValue addJavascriptInterface(Object object, String name);
	
	/**
     * Removes a previously injected Java object from this JavaScript context. Note that
     * the removal will not be reflected in JavaScript until the page is next
     * (re)loaded. See {@link #addJavascriptInterface}.
     *
     * @param name the name used to expose the object in JavaScript
     */
    void removeJavascriptInterface(@NonNull String name);

	 /**
     * Asynchronously evaluates JavaScript in this JavasSript context..
     * If non-null, {@code resultCallback} will be invoked with any result returned from that
     * execution. On webview contexts, this method must be called on the UI thread and the callback will
     * be made on the UI thread.
     * @param script the JavaScript to execute.
     * @param resultCallback A callback to be invoked when the script execution
     *                       completes with the result of the execution (if any).
     *                       May be {@code null} if no notification of the result is required.
     */
	void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback) throws Exception;
	
	/**
     * Stop the Javascript runtime associated with this context and release all resources
     */
	void close();
  
}
