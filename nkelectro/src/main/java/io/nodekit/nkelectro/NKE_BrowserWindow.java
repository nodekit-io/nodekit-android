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

package io.nodekit.nkelectro;

import android.util.SparseArray;
import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.util.NKEventHandler;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.NKScriptExport;

public class NKE_BrowserWindow  implements NKScriptExport
{
    public NKEventEmitter events = new NKEventEmitter();
    private static SparseArray<NKE_BrowserWindow> windowArray = new SparseArray<NKE_BrowserWindow>();
    public static HashMap<String, Object> startupOptions = new HashMap<String, Object>();
    public NKScriptContext context;
    public Object webView = new Object();
    public NKEBrowserType browserType = NKEBrowserType.WebView;
    private int _id = 0;
    private String _type;
    private NKE_WebContents _webContents;
    protected NKEventEmitter globalEvents = NKEventEmitter.global;

    public NKE_BrowserWindow() throws Exception {
    }

    public static void attachTo(NKScriptContext context, HashMap<String, Object> appOptions) throws Exception {

        HashMap<String,Object> options1 = new HashMap<String, Object>();

        options1.put("js","lib_electro/browserwindow.js");

        NKE_BrowserWindow principal1 = new NKE_BrowserWindow();

        NKScriptValue jsv1 = context.loadPlugin(principal1, "io.nodekit.electro.BrowserWindow", options1);

        principal1.initWithJSValue(jsv1);

    }

    private NKScriptValue jsValue;

    private void initWithJSValue(NKScriptValue jsv) {
        this.jsValue = jsv;
    }

    public NKE_BrowserWindow(HashMap<String, Object> options) {
        _id = NKScriptContextFactory.sequenceNumber++;
        if (options == null)
            options = new HashMap<String, Object>();
         
        windowArray.put(_id, this);
        try
        {

            HashMap<String,Object> options2 = new HashMap<String, Object>();

            options2.put("js","lib_electro/webcontents.js");

            _webContents = new NKE_WebContents_AndroidWebView(this);
            createBrowserWindow(options);

            NKScriptValue jsv2 = context.loadPlugin(_webContents, "io.nodekit.electro.WebContents", options2);

            _webContents.initWithJSValue(jsv2);

        }
        catch (Exception ex)
        {
            NKLogging.log("!Error Creating Window" + ex.toString());
        }

        events.on("NKE.DidFinishLoad", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                jsValue.invokeMethod("emit", new String[] { "did-finish-load" });
            }
        });

    }

    private void createBrowserWindow(HashMap<String, Object> options)  {

        // PARSE & STORE OPTIONS
        if (options.containsKey(NKEBrowserOptions.nkBrowserType))
            browserType = NKEBrowserType.valueOf((String)options.get(NKEBrowserOptions.nkBrowserType));
       else
            browserType = NKEBrowserDefaults.nkBrowserType;

        switch(browserType)
        {
            case WebView:
                NKLogging.log("+creating Native (Android WebView) Renderer");
                _webContents.createWebView(options);
                break;
            default: 
                break;
        
        }
    }

    // class/helper functions (for Java use only, equivalent functions exist in .js helper )
    public static NKE_BrowserWindow fromId(int id) {
        return windowArray.get(id);
    }

    public int getid()  {
        return _id;
    }

    public String gettype() {
        return _type;
    }

    public NKE_WebContents getwebContents()  {
        return _webContents;
    }
}


