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
import android.util.SparseArray;
import java.util.HashMap;

import io.nodekit.nkscripting.engines.NKEngineAndroidWebView;
import io.nodekit.nkscripting.engines.NKEngineExternal;
import io.nodekit.engine.JSContext;   // common class name used by engine-jsc, engine-v8, engine-hermes, engine-quickjs 

public class NKScriptContextFactory   
{

    public enum NKTypeEngine
    {
        WebView,
        // Native webview provided by Android OS, always available but version dependent on age of phone
        External,
        // V8, JavaScriptCore, Hermes, or QuickJS used dependent on engine AAR selected by consuming app

    }

    public static SparseArray<Object> _contexts = new SparseArray<Object>();
    public static int sequenceNumber = 1;

    public static void createContext(@Nullable HashMap<String, Object> options, NKScriptContext.NKScriptContextDelegate callback) throws Exception {

        if (options == null)
        {
            options = new HashMap<String, Object>();
        }

        NKTypeEngine engine = NKTypeEngine.WebView;

        if (options.containsKey("NKS.Engine"))
            engine = (NKTypeEngine)options.get("NKS.Engine");
        else
            engine = NKTypeEngine.External;

        int id = NKScriptContextFactory.sequenceNumber++;

        switch(engine)
        {
            case WebView:
                NKEngineAndroidWebView.createContextWebView(id, options, callback);
                break;
            case External:
                JSContext jscontext = new JSContext();
                NKEngineExternal context = new NKEngineExternal(id, jscontext, options, callback);
                break;
         }
    }
}