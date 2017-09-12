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

package io.nodekit.nkscripting.engines.androidwebview;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKCallback;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;
import io.nodekit.nkscripting.util.NKSerialize;


import io.nodekit.nkscripting.channelbridge.NKScriptChannel;
import io.nodekit.nkscripting.channelbridge.NKScriptMessage;
import io.nodekit.nkscripting.NKScriptExport.NKScriptExportType;

public class NKEngineAndroidWebView extends WebViewClient implements NKScriptContext, NKScriptMessage.Controller {
    @SuppressLint("setJavaScriptEnabled")
    public static void createContextWebView(HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        WebView.setWebContentsDebuggingEnabled(true);

        ViewGroup _root = (ViewGroup) NKApplication.getRootView().findViewById(android.R.id.content);
        ViewGroup mWebContainer = (ViewGroup) _root.getChildAt(0);
        WebView webView = new WebView(NKApplication.getAppContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.INVISIBLE);
        mWebContainer.addView(webView);
        int id = NKScriptContextFactory.sequenceNumber++;
        addJSContextWebView(id, webView, options, callback);
     }

    public static void addJSContextWebView(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        NKEngineAndroidWebView.createContext(id, webview, options, callback);
    }

    public static void createContext(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        if (options == null)
            options = new HashMap<String, Object>();

        NKEngineAndroidWebView context = new NKEngineAndroidWebView(id, webview, options, callback);

        context.prepareEnvironment();

    }

    private int _id;
    private WebView _webview;
    private ArrayList<NKScriptSource> _sourceList;
    protected ArrayList<NKScriptValue> _injectedPlugins;
    private HashMap<String, NKScriptMessage.Handler> _scriptMessageHandlers;

    private Boolean isReady = false;
    private NKScriptContextDelegate callback;

    private NKEngineAndroidWebView(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        super();
        this.callback = callback;
        webview.setWebViewClient(this);

        this._id = id;
        this._webview = webview;
        this._sourceList = new ArrayList<NKScriptSource>();
        this._injectedPlugins = new ArrayList<NKScriptValue>();
        this._scriptMessageHandlers = new HashMap<String, NKScriptMessage.Handler>();
        NKLogging.log("NKNodeKit Renderer Android WebView E" + _id, NKLogging.Level.Info);
    }

    @Override
    public void onPageFinished(WebView view,
                               String url) {

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

        _webview.pauseTimers();
        _webview.getSettings().setJavaScriptEnabled(false);
        _webview.stopLoading();

        ViewParent parent = _webview.getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)parent;
            group.removeView(_webview);
        }
    }

    @JavascriptInterface
    public void log(String msg, String severity, HashMap<String, String> labels) {
        NKLogging.log(msg, severity, labels);
    }

    @JavascriptInterface
    public String didReceiveScriptMessageSync(String channel, String message) throws Exception {
        if (this._scriptMessageHandlers.containsKey(channel))
        {
            NKScriptMessage.Handler scriptHandler = _scriptMessageHandlers.get(channel);
            Map<String, Object> body = NKSerialize.deserialize(message);
            NKScriptMessage msg = new NKScriptMessage(channel, body);
            Object result = scriptHandler.didReceiveScriptMessageSync(msg);
            return this.serialize(result);
         } else
        {
            return null;
        }
    }

    @JavascriptInterface
    public void didReceiveScriptMessage(String channel, String message) throws Exception {
        if (this._scriptMessageHandlers.containsKey(channel))
        {
            NKScriptMessage.Handler scriptHandler = _scriptMessageHandlers.get(channel);
            Map<String, Object> body = NKSerialize.deserialize(message);
            NKScriptMessage msg = new NKScriptMessage(channel, body);
            scriptHandler.didReceiveScriptMessage(msg);
        }


    }

    @JavascriptInterface
    public String didReceiveScriptMessageAsync(String channel, String message) throws Exception  {
        if (this._scriptMessageHandlers.containsKey(channel))
        {
            NKScriptMessage.Handler scriptHandler = _scriptMessageHandlers.get(channel);
            Map<String, Object> bodyMap = NKSerialize.deserialize(message);
            NKScriptMessage body = new NKScriptMessage(bodyMap);
            Object result = scriptHandler.didReceiveScriptMessageSync(body);
            return this.serialize(result);
        }

        return null;
    }


    private void prepareEnvironment() throws Exception {

        _webview.addJavascriptInterface(this, "NKScriptingBridge");

        String script1 = NKStorage.getResource("lib-scripting/nkscripting.js");

        this.injectJavaScript(new NKScriptSource(script1, "io.nodekit.scripting/NKScripting/nkscripting.js", "nkscripting"));

        String appjs = NKStorage.getResource("lib-scripting/init_androidwebview.js");

        String script2 = "function loadinit(){\n" + appjs + "\n}\n" + "loadinit();" + "\n";

        this.injectJavaScript(new NKScriptSource(script2, "io.nodekit.scripting/init_androidwebview", "io.nodekit.scripting.init"));

        String script3 = NKStorage.getResource("lib-scripting/promise.js");

        if (script3.isEmpty()) {
            NKLogging.log("Failed to read provision script: promise", NKLogging.Level.Error);
            return;
        }

        this.injectJavaScript(new NKScriptSource(script3, "io.nodekit.scripting/NKScripting/promise.js", "Promise"));

        NKStorage.attachTo(this);

        callback.NKScriptEngineDidLoad(this);

       if (_webview.getVisibility() != View.VISIBLE)
          _webview.loadDataWithBaseURL("", "<html><body>NodeKit Running</body></html>", "text/html", "UTF-8", "");

    }


    @SuppressLint("JavascriptInterface")
    public NKScriptValue loadPlugin(Object plugin, String ns, HashMap<String, Object> options) throws Exception {

        NKScriptExportType bridge;
        NKScriptValue scriptValue;

        if (options.containsKey("bridge"))
            bridge = (NKScriptExportType) options.get("bridge");
        else
            bridge = NKScriptExportType.NKScriptExport;

        switch (bridge) {
            case NKScriptExport:

                NKScriptChannel channel = new NKScriptChannel((NKScriptContext) this);

                if (plugin instanceof Class)
                {
                    scriptValue = channel.bindPluginClass((Class)plugin, ns, options);
                } else
                {
                    scriptValue = channel.bindPlugin(plugin, ns, options);
                }

                _injectedPlugins.add(scriptValue);

                NKLogging.log("NKNodeKit Plugin loaded with NKScripting channel at " + ns, NKLogging.Level.Info);

                break;

            case JavascriptInterface:

                String nsobj = "NKScriptingBridgePlugin" + Integer.toString(_id);

                String jspath = (String) options.get("js");

                String js = NKStorage.getResource(jspath);

                _webview.addJavascriptInterface(plugin, nsobj);

                NKLogging.log(String.format("NKNodeKit Plugin object %s is bound to %s (%s) with JavascriptInterface channel", plugin, ns, nsobj), NKLogging.Level.Info);

                String globalstubber = "NKScripting.createProjection(\"" + ns + "\", " + nsobj + ");\n function plugin" + _id + "(){\n" + js + "\n}\n" + "plugin" + _id + "();" + "\n";

                this.injectJavaScript(new NKScriptSource(globalstubber, jspath, ns));

                scriptValue = new NKScriptValue(ns, this);
                ;

                break;

            default:

                throw new IllegalArgumentException("Load Plugin Base called for non-handled bridge type");
        }

        _injectedPlugins.add(scriptValue);

        return scriptValue;
    }

    public void evaluateJavaScript(String javaScriptString, NKCallback<String> callback) throws Exception {
       this._webview.evaluateJavascript(javaScriptString, callback);
    }

    public void injectJavaScript(NKScriptSource source) throws Exception {
        this._sourceList.add(source);
    }

   // private void injectExecuteJavaScript(NKScriptSource source) throws Exception {
   //     source.inject(this);
  //  }

    public String serialize(Object obj) throws Exception {
        return NKSerialize.serialize(obj);
    }

    public void addScriptMessageHandler(NKScriptMessage.Handler scriptMessageHandler, String name) throws Exception {
        _scriptMessageHandlers.put(name, scriptMessageHandler);
     }

    public void removeScriptMessageHandlerForName(String name)  throws Exception
    {
        _scriptMessageHandlers.remove(name);
        String cleanup = "delete NKScripting.messageHandlers." + name;
        this.evaluateJavaScript(cleanup, null);
    }

}


