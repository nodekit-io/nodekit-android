/*
* nodekit.io
*
* Copyright (c) 2016 OffGrid Networks. All Rights Reserved.
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

package io.nodekit.nkscripting;

import android.support.annotation.Nullable;

import io.nodekit.nkscripting.util.NKCallback;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKSerialize;

public class NKScriptValue {

    public String namespace;

    public NKScriptContext context;

    protected NKScriptValue _origin = null;

    private static final ThreadLocal<NKScriptContext> contextThreadLocal = new ThreadLocal<NKScriptContext>();

    public static void setCurrentContext(NKScriptContext context) {
        contextThreadLocal.set(context);
    }

    public static void unsetCurrentContext() {
        contextThreadLocal.remove();
    }

    public static NKScriptContext getCurrentContext() {
        return contextThreadLocal.get();
    }

    public NKScriptValue() {}

    public NKScriptValue(String namespace, NKScriptContext context)   {
        this.namespace = namespace;
        this.context = context;
    }


    public NKScriptValue(String namespace, NKScriptContext context, NKScriptValue origin)
    {
        this.namespace = namespace;
        this.context = context;
        if (origin != null)
            this._origin = origin;
        else
            this._origin = this;
    }

    // The object is a stub for a JavaScript object which was retained as an argument.
    private int reference = 0;
    public NKScriptValue(int reference, NKScriptContext context, NKScriptValue origin)
    {
        this.namespace = String.format("%s.$references[%s]", origin.namespace, reference);
        this.reference = reference;
        this.context = context;
    }

    public void callWithArguments(Object[] arguments, NKCallback<String> completionHandler) {
        String exp = this.scriptForCallingMethod(null, arguments);
        this.evaluateExpression(exp, completionHandler);
    }

    public void invokeMethod(String method, Object[] arguments, NKCallback<String> completionHandler) {
        String exp = this.scriptForCallingMethod(method, arguments);
        this.evaluateExpression(exp, completionHandler);
    }

    public void invokeMethod(String method, Object[] arguments) {
        this.invokeMethod(method, arguments, null);
    }

    public void defineProperty(String property, Object descriptor) {
        String exp = "Object.defineProperty(" + this.namespace + ", + " + property + ", " + NKSerialize.serialize(descriptor) + ")";
        this.evaluateExpression(exp, null);
    }

    public void deleteProperty(String property) {
        String exp = "delete " + scriptForFetchingProperty(property);
        this.evaluateExpression(exp, null);
    }

    public void hasProperty(String property, NKCallback<String> completionHandler) {
        String exp = scriptForFetchingProperty(property) + "!= undefined";
        this.evaluateExpression(exp, null);
    }

    public void valueForProperty(String property, NKCallback<String> completionHandler) {
        String exp = scriptForFetchingProperty(property);
        this.evaluateExpression(exp, null);
    }

    public void setValue(Object value, String forProperty) {
        this.evaluateExpression(scriptForUpdatingProperty(forProperty, value), null);
    }

    public void valueAtIndex(int index, NKCallback<String> completionHandler) {
        this.evaluateExpression(this.namespace + "[" + index + "]", completionHandler);
    }

    public void setValue(Object value,  int atIndex) {
        this.evaluateExpression(this.namespace + "[" + atIndex + "] = " + NKSerialize.serialize(value), null);
    }

    private String scriptForFetchingProperty(@Nullable String name) {

        if (null == name) {

            return this.namespace;

        } else if (name.isEmpty()) {

            return this.namespace + "['']";

        } else if (isInteger(name)) {

            return this.namespace + "[" + name + "]";

        } else {

            return this.namespace + "." + name;

        }

    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }


    private String scriptForCallingMethod(String name, Object[] arguments)   {

        String script = scriptForFetchingProperty(name) + "(" + NKSerialize.serializeArgs(arguments) + ")";

        return "(function line_eval(){ try { return " + script + "} catch(ex) { console.log(ex.toString()); return ex} })()";

    }


    private String scriptForUpdatingProperty(String name, Object value)
    {

        return scriptForFetchingProperty(name) + " = " + NKSerialize.serialize(value);

    }

    private void evaluateExpression(String expression, NKCallback<String> completionHandler )
    {
        try {

            this.context.evaluateJavaScript(expression, completionHandler);
        } catch (Exception ex) {
             NKLogging.log(ex.toString());
             if (null != completionHandler)
                 completionHandler.onReceiveValue(null);
        }

    }

  //  private String scriptForRetaining(String script)  {

   // internal func wrapScriptObject(object: AnyObject!) -> AnyObject! {

    // Private JavaScript scripts and helpers

}


