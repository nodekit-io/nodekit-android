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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.util.NKDisposable;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;
import io.nodekit.nkscripting.util.NKSerialize;
import io.nodekit.nkscripting.util.NKTimer;
import io.nodekit.nkscripting.NKScriptValue;

public class NKEngineAndroidWebView extends WebViewClient implements NKScriptContext {

    @SuppressLint("setJavaScriptEnabled")
    public static void createContextWebView(int id, HashMap<String, Object> options, NKScriptContextDelegate callback)
            throws Exception {

        WebView webView = NKApplication.createInvisibleWebViewInWindow();
        addJSContextWebView(id, webView, options, callback);
    }

    public static void addJSContextWebView(int id, WebView webview, HashMap<String, Object> options,
            NKScriptContextDelegate callback) throws Exception {
        NKEngineAndroidWebView.createContext(id, webview, options, callback);
    }

    public static void createContext(int id, WebView webview, HashMap<String, Object> options,
            NKScriptContextDelegate callback) throws Exception {
        if (options == null)
            options = new HashMap<String, Object>();

        NKEngineAndroidWebView context = new NKEngineAndroidWebView(id, webview, options, callback);

        context.prepareEnvironment();

    }

    protected int _id;
    protected WebView _webview;
    protected ArrayList<NKScriptSource> _sourceList;
    protected ArrayList<NKDisposable> disposables = new ArrayList<>();

    protected Boolean isReady = false;
    protected NKScriptContextDelegate callback;

    protected NKEngineAndroidWebView(int id, WebView webview, HashMap<String, Object> options,
            NKScriptContextDelegate callback) throws Exception {
        super();
        this.callback = callback;
        webview.setWebViewClient(this);

        this._id = id;
        this._webview = webview;
        this._sourceList = new ArrayList<NKScriptSource>();
         NKLogging.log("NKNodeKit Renderer Android WebView E" + _id, NKLogging.Level.Info);
    }

    @Override
    public void onPageFinished(WebView view, String url) {

        for (NKScriptSource source : _sourceList) {
            try {
                NKLogging.log(source.filename);
                source.inject(this);
            } catch (Exception e) {
                NKLogging.log(e);
            }
        }

        if (!isReady) {
            isReady = true;
            this.callback.NKScriptEngineReady(this);
        }

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

        _webview.getSettings().setJavaScriptEnabled(false);
        _webview.stopLoading();

        _sourceList.clear();

        ViewParent parent = _webview.getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            group.removeView(_webview);
        }

        _webview.destroy();
        _webview = null;
    }

    @JavascriptInterface
    public void log(String msg, String severity, HashMap<String, String> labels) {
        NKLogging.log(msg, severity, labels);
    }

    protected void prepareEnvironment() throws Exception {

        _webview.addJavascriptInterface(this, "NKScriptingBridge");

        String script1 = NKStorage.getResource("lib-scripting/nkscripting.js");

        if (script1 != null && script1.isEmpty()) {
            NKLogging.log("Failed to read provision script: nkscripting", NKLogging.Level.Error);
            return;
        }

        this.injectJavaScript(
                new NKScriptSource(script1, "io.nodekit.scripting/NKScripting/nkscripting.js", "nkscripting"));

        String appjs = NKStorage.getResource("lib-scripting/init_androidwebview.js");

        String script2 = "function loadinit(){\n" + appjs + "\n}\n" + "loadinit();" + "\n";

        this.injectJavaScript(
                new NKScriptSource(script2, "io.nodekit.scripting/init_androidwebview", "io.nodekit.scripting.init"));

        String script3 = NKStorage.getResource("lib-scripting/promise.js");

        if (script3 != null && script3.isEmpty()) {
            NKLogging.log("Failed to read provision script: promise", NKLogging.Level.Error);
            return;
        }

        this.injectJavaScript(new NKScriptSource(script3, "io.nodekit.scripting/NKScripting/promise.js", "Promise"));

        loadTimerScript();

        NKStorage.attachTo(this);
        NKTimer.attachTo(this);

        callback.NKScriptEngineDidLoad(this);

        if (_webview.getVisibility() != View.VISIBLE)
            _webview.loadDataWithBaseURL("", "<html><body>NodeKit Running</body></html>", "text/html", "UTF-8", "");

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

        _webview.addJavascriptInterface(plugin, nsobj);

        if (options.containsKey("js"))
        {

            String jspath = (String) options.get("js");

            String appjs = NKStorage.getResource(jspath);
    
            String globalstubber =  "function loadplugin(){\n" + appjs + "\n}\n" + "\n" + "loadplugin();" + "\n";

            this.injectJavaScript(new NKScriptSource(appjs, jspath, ns));

        }

        NKLogging.log(String.format("NKNodeKit Plugin object %s is bound to %s (%s) with JavascriptInterface channel",
                plugin, ns, nsobj), NKLogging.Level.Info);

        NKScriptValue scriptValue = new NKEngineAndroidWebViewScriptValue(ns, this);

        return scriptValue;

    }

    public void evaluateJavaScript(String javaScriptString, ValueCallback<String> callback) throws Exception {
        if (this._webview == null) {
            return;
        }
        this._webview.evaluateJavascript(javaScriptString, callback);
    }

    public void injectJavaScript(NKScriptSource source) throws Exception {
        this._sourceList.add(source);
    }

    public String serialize(Object obj) throws Exception {
        return NKSerialize.serialize(obj);
    }


}