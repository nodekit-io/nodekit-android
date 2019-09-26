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
import io.nodekit.nkscripting.util.NKDisposable;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;
import io.nodekit.nkscripting.util.NKSerialize;

import io.nodekit.nkscripting.channelbridge.NKScriptChannel;
import io.nodekit.nkscripting.channelbridge.NKScriptMessage;
import io.nodekit.nkscripting.NKScriptExport.NKScriptExportType;
import io.nodekit.nkscripting.util.NKTimer;

public class NKEngineAndroidWebViewChannel extends NKEngineAndroidWebView {

    @SuppressLint("setJavaScriptEnabled")
    public static void createContextWebView(int id, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {

        WebView webView = NKApplication.createInvisibleWebViewInWindow();
        addJSContextWebView(id, webView, options, callback);
     }

    public static void addJSContextWebView(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        NKEngineAndroidWebViewChannel.createContext(id, webview, options, callback);
    }

    public static void createContext(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        if (options == null)
            options = new HashMap<String, Object>();

        NKEngineAndroidWebView context = new NKEngineAndroidWebViewChannel(id, webview, options, callback);

        context.prepareEnvironment();

    }

    private HashMap<String, NKScriptMessage.Handler> _scriptMessageHandlers;
  
    private NKEngineAndroidWebViewChannel(int id, WebView webview, HashMap<String, Object> options, NKScriptContextDelegate callback) throws Exception {
        super(id, webview, options, callback);
        this._scriptMessageHandlers = new HashMap<String, NKScriptMessage.Handler>();
    }

    @Override
    public void tearDown() {
        _scriptMessageHandlers.clear();
       super.tearDown();
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

    @SuppressLint("JavascriptInterface")
    public NKScriptValue loadPlugin(Object plugin, String ns, HashMap<String, Object> options) throws Exception {

        NKScriptExportType bridge;
        NKScriptValue scriptValue;

        if (options.containsKey("bridge"))
            bridge = (NKScriptExportType) options.get("bridge");
        else
            bridge = NKScriptExportType.NKScriptExport;

        if (plugin instanceof NKDisposable) {
            disposables.add((NKDisposable) plugin);
        }

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

                NKLogging.log("NKNodeKit Plugin loaded with NKScripting channel at " + ns, NKLogging.Level.Info);

                break;

            case JavascriptInterface:

                String nsobj = "NKScriptingBridgePlugin" + Integer.toString(_id);

                _webview.addJavascriptInterface(plugin, nsobj);

                if (options.containsKey("js"))
                {
        
                    String jspath = (String) options.get("js");
        
                    String appjs = NKStorage.getResource(jspath);
            
                    String globalstubber =  "function loadplugin(){\n" + appjs + "\n}\n" + "loadplugin();" + "\n";
        
                    this.injectJavaScript(new NKScriptSource(appjs, jspath, ns));
        
                }

                NKLogging.log(String.format("NKNodeKit Plugin object %s is bound to %s (%s) with JavascriptInterface channel", plugin, ns, nsobj), NKLogging.Level.Info);

                scriptValue = new NKEngineAndroidWebViewScriptValue(ns, this);

            default:

                throw new IllegalArgumentException("Load Plugin Base called for non-handled bridge type");
        }


        return scriptValue;

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