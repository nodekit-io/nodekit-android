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

import io.nodekit.nkscripting.engines.androidwebview.NKEngineAndroidWebView;

public class NKScriptContextFactory   
{

    public enum NKTypeEngine
    {
        WebView,
        // Native webview provided by Android OS
        CrossWalk
        // open source embedded V8 engine
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
            engine = NKTypeEngine.WebView;

        switch(engine)
        {
            case WebView:
                NKEngineAndroidWebView.createContextWebView(options, callback);
                break;
            case CrossWalk:
                throw new UnsupportedOperationException("CrossWalk not suported in NKScripting Lite");
        }
    }
}


