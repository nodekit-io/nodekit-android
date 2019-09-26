/*
* nodekit.io
*
* Copyright (c) 2016-9 OffGrid Networks. All Rights Reserved.
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

package io.nodekit.nkscripting.engines;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import io.nodekit.engine.JavascriptContext;
import io.nodekit.nkscripting.NKScriptValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.util.NKDisposable;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;
import io.nodekit.nkscripting.util.NKSerialize;
import io.nodekit.nkscripting.util.NKTimer;

public class NKEngineExternal implements NKScriptContext {

    protected int _id;
    protected JavascriptContext _jsContext;
    protected ArrayList<NKScriptSource> _sourceList;
    protected ArrayList<NKDisposable> disposables = new ArrayList<>();
    protected Boolean isReady = false;
    protected NKScriptContextDelegate callback;

    public NKEngineExternal(int id, JavascriptContext context, HashMap<String, Object> options,
            NKScriptContextDelegate callback) throws Exception {

        this.callback = callback;

        this._id = id;
        this._jsContext = context;
        this._sourceList = new ArrayList<NKScriptSource>();
        NKLogging.log("NKNodeKit External ES6 Engine E" + _id, NKLogging.Level.Info);
        this.prepareEnvironment();
    }

    public int id() throws Exception {
        return _id;
    }

    @Override
    public void tearDown() {

        for (NKDisposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();

        _jsContext.close();

        _sourceList.clear();

        _jsContext = null;

    }

    @JavascriptInterface
    public void log(String msg, String severity, Object labels) {
        NKLogging.log(msg, severity, new HashMap<String, String>());
    }

    protected void prepareEnvironment() throws Exception {

        _jsContext.addJavascriptInterface(this, "NKScriptingBridge");

        String script1 = NKStorage.getResource("lib-scripting/nkscripting.js");

        if (script1 != null && script1.isEmpty()) {
            NKLogging.log("Failed to read provision script: nkscripting", NKLogging.Level.Error);
            return;
        }

        this.injectJavaScript(
                new NKScriptSource(script1, "io.nodekit.scripting/NKScripting/nkscripting.js", "nkscripting"));

        String appjs = NKStorage.getResource("lib-scripting/init_es6engine.js");

        String script2 = "function loadinit(){\n" + appjs + "\n}\n" + "loadinit();" + "\n";

        this.injectJavaScript(
                new NKScriptSource(script2, "io.nodekit.scripting/init_es6engine", "io.nodekit.scripting.init"));

        loadTimerScript();

        NKStorage.attachTo(this);
        NKTimer.attachTo(this);

        callback.NKScriptEngineDidLoad(this);

        if (!this.isReady) {
            this.isReady = true;
            this.callback.NKScriptEngineReady(this);
        }

    }

    protected void loadTimerScript() throws Exception {

        String timerSource = NKStorage.getResource("lib-scripting/timer.js");

        if (timerSource == null || timerSource.isEmpty()) {
            NKLogging.log("Failed to read provision script: timer", NKLogging.Level.Error);
            return;
        }

        this.injectJavaScript(new NKScriptSource(timerSource, "io.nodekit.scripting/NKScripting/timer.js",
                "io.nodekit.scripting.timer"));
    }

    @SuppressLint("JavascriptInterface")
    public NKScriptValue loadPlugin(Object plugin, String ns, HashMap<String, Object> options) throws Exception {

        if (plugin instanceof NKDisposable) {
            disposables.add((NKDisposable) plugin);
        }

        String nsobj = "NKScriptingBridgePlugin" + Integer.toString(_id);

        NKScriptValue jsv  = _jsContext.addJavascriptInterface(plugin, nsobj);

        String globalstubber;
        String jspath;

        if (options.containsKey("js"))
        {

            jspath = (String) options.get("js");

            String appjs = NKStorage.getResource(jspath);
    
            globalstubber = "NKScripting.createNamespace(\"" + ns + "\", " + nsobj + ");\n" + appjs;

        } else {

            jspath = ns;

            globalstubber = "NKScripting.createNamespace(\"" + ns + "\", " + nsobj + ");\n";

        }

        
        NKLogging.log(String.format("NKNodeKit Plugin object %s is bound to %s (%s) with JavascriptInterface channel",
                plugin, ns, nsobj), NKLogging.Level.Info);

        this.injectJavaScript(new NKScriptSource(globalstubber, jspath, ns));

        return jsv;

    }

    public void evaluateJavaScript(String javaScriptString, ValueCallback<String> callback) throws Exception {
        if (this._jsContext == null) {
            return;
        }
        this._jsContext.evaluateJavascript(javaScriptString, callback);
    }

    public void injectJavaScript(NKScriptSource source) throws Exception {
        this._sourceList.add(source);
        NKLogging.log(source.filename);

        try {
            source.inject(this);
        } catch (Exception e) {
            NKLogging.log(e);
        }

    }

    public String serialize(Object obj) throws Exception {
        return NKSerialize.serialize(obj);
    }

}