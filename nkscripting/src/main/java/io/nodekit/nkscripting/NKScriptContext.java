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

import io.nodekit.nkscripting.NKScriptValue;

import android.webkit.ValueCallback;

import java.util.HashMap;

public interface NKScriptContext
{
    int id() throws Exception ;

    NKScriptValue loadPlugin(Object plugin, String ns, HashMap<String, Object> options) throws Exception ;

    void evaluateJavaScript(String javaScriptString, ValueCallback<String> callback) throws Exception ;

    void injectJavaScript(NKScriptSource source) throws Exception ;

    String serialize(Object obj) throws Exception ;

    void tearDown();

    interface NKScriptContextDelegate {

        void NKScriptEngineDidLoad(NKScriptContext context);

        void NKScriptEngineReady(NKScriptContext context);

    }

}
