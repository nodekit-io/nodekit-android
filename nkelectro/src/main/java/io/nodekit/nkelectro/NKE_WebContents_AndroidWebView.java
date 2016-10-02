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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;
import android.net.Uri;

import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.engines.androidwebview.NKEngineAndroidWebView;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkelectro.NKE_Protocol.ProtocolHandler;

class NKE_WebContents_AndroidWebView extends NKE_WebContents implements NKScriptContext.NKScriptContextDelegate {

    static void attachTo(NKScriptContext context, Map<String, Object> appOptions) throws Exception {
        HashMap<String,Object> options = new HashMap<String, Object>();
        options.put("js","lib_electro/webcontents.js");

        context.loadPlugin(NKE_WebContents_AndroidWebView.class, "io.nodekit.electro.WebContents", options);
    }

    private static HashMap<String, ProtocolHandler> registeredSchemes = new HashMap<>();

    static void registerScheme(String scheme, ProtocolHandler callback)
    {
        registeredSchemes.put(scheme, callback);
    }

    static void unregisterScheme(String scheme)
    {
        registeredSchemes.remove(scheme);
    }

    private class AndroidWebViewClient extends WebViewClient {
        NKE_WebContents_AndroidWebView _parent;

        private AndroidWebViewClient(NKE_WebContents_AndroidWebView parent) {
            this._parent = parent;
        }

        @Override
        public void onPageFinished(WebView view,
                                   String url) {
            this._parent.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            this._parent.onReceivedError(view);
        }

        @Override
        public void onLoadResource(WebView view,
                                   String url)
        {
            this._parent.onLoadResource(view, url);
        }

        @Override
        @TargetApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            HashMap<String, Object> req  =  parseUri(request.getUrl());
            if (req == null)
                return super.shouldInterceptRequest(view, request);

            req.put("method", request.getMethod());
            req.put("headers", request.getRequestHeaders());

            String scheme = (String)req.get("scheme");
            ProtocolHandler callback = registeredSchemes.get(scheme);
            WebResourceResponse connection =  callback.invoke(req);
            if (connection != null) return connection;
            return super.shouldInterceptRequest(view, request);
        }

        private HashMap<String, Object> parseUri(Uri uri) {
            HashMap<String, Object> req  = new HashMap<>();

            try {

                String host = uri.getHost().toLowerCase();
                String scheme = uri.getScheme().toLowerCase();

                if (!registeredSchemes.containsKey(scheme) && registeredSchemes.containsKey(host))
                {
                    scheme = host;
                } else if (!registeredSchemes.containsKey(scheme)) {
                    return null;
                }

                req.put("host", host);
                req.put("scheme", scheme);

                String path = uri.getPath();
                String query = uri.getQuery();

                if (path.equals("")) { path = "/"; }


                String pathWithQuery;

                if ( query!= null && !query.equals("")) {
                    pathWithQuery = path + "?" + query;
                } else {
                    pathWithQuery = path;
                }

                req.put("path", pathWithQuery);


            } catch (Exception e) {
                NKLogging.log(e);
                return null;
            }

            return req;
        }

        @Override
        public final WebResourceResponse shouldInterceptRequest(WebView view, String url) {

            Uri uri = Uri.parse(url);
            HashMap<String, Object> req  =  parseUri(uri);
            if (req == null)
                return super.shouldInterceptRequest(view, url);

            req.put("method", "GET");
            req.put("headers", new HashMap<String, String>());

            String scheme = (String)req.get("scheme");
            ProtocolHandler callback = registeredSchemes.get(scheme);
            WebResourceResponse connection =  callback.invoke(req);
            if (connection != null) return connection;
            return super.shouldInterceptRequest(view, url);
        }
    }

    private WebView webView;
    private Boolean _isLoading;
    private HashMap<String, Object> options;


    private NKE_WebContents_AndroidWebView() {
        super();
    }

    public NKE_WebContents_AndroidWebView(NKE_BrowserWindow browserWindow) {
        super(browserWindow);
    }

    @SuppressLint("setJavaScriptEnabled")
    @UiThread
    public void createWebView(int id, HashMap<String, Object> options)
    {

        this.options = options;
        FrameLayout _root = (FrameLayout) NKApplication.getRootView().findViewById(android.R.id.content);
        FrameLayout mWebContainer = (FrameLayout) _root.getChildAt(0);
        WebView webView = new WebView(NKApplication.getAppContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.VISIBLE);
        mWebContainer.addView(webView);
        this.webView = webView;
        _browserWindow.webView = webView;

        try {
            NKEngineAndroidWebView.addJSContextWebView(id, webView, options, this);
        } catch (Exception e) {
            NKLogging.log(e);
        }

    }

    public void NKScriptEngineDidLoad(NKScriptContext context) {
        _browserWindow.context = context;

        if (!options.containsKey("nke.InstallElectro") || (Boolean) options.get("nke.InstallElectro")) {
            try {
                NKElectro.addToRendererContext(_browserWindow.context, options);
            } catch (Exception e) {
                NKLogging.log(e);
            }
        }

        String url;
        if (options.containsKey(NKE_BrowserWindow.Options.kPreloadURL))
            url = (String)options.get(NKE_BrowserWindow.Options.kPreloadURL);
        else
            url = "about:blank";
        webView.setWebViewClient(new AndroidWebViewClient(this));
        webView.loadUrl(url);

    }

    public void NKScriptEngineReady(NKScriptContext context)
    {
        this.init_IPC();

         NKLogging.log(String.format("+E%s Renderer Ready", _id));

        _browserWindow.events.emit("NKE.DidFinishLoad", Integer.toString(_id));

    }

    // Android WebView Client Delegate Methods

    private void onLoadResource (WebView view,
                                String url)
    {
        _isLoading = true;
    }

    private  void onPageFinished(WebView sender,
                               String url) {
        _isLoading = false;

        _browserWindow.events.emit("NKE.DidFinishLoad",  Integer.toString(_id));

    }

    private void onReceivedError(WebView view) {
        _isLoading = false;
        _browserWindow.events.emit("NKE.DidFailLoading", _id);
    }


    // Helper Methods

    @SuppressWarnings("unchecked")
    @Nullable
    private <T>T itemOrDefault(Map<String, Object> options, String key) {
        if (options.containsKey(key))
            return (T)options.get(key);
        else
             return null;
    }

    // JavaScript Public Methods (for instance)


    @JavascriptInterface
    public void loadURL(String url, Map<String, Object> options)
    {

        String httpReferrer = itemOrDefault(options, "httpReferrer");
        String userAgent = itemOrDefault(options, "userAgent");
        Map<String, String> extraHeaders = itemOrDefault(options, "extraHeaders");

        HashMap<String, String> request = new HashMap<String, String>();
        if ((userAgent) != null)
            webView.getSettings().setUserAgentString(userAgent);

        if ((httpReferrer) != null)
              request.put("Referrer", httpReferrer);

        if ((extraHeaders) != null)
        {
            for (String item : extraHeaders.keySet())
            {
                request.put(item, extraHeaders.get(item));
            }
        }

        webView.loadUrl(url, request);
    }

    @JavascriptInterface
    public String getURL()
    {
        return webView.getUrl();
    }

    @JavascriptInterface
    public String getTitle()
    {
        return webView.getTitle();
    }

    @JavascriptInterface
    public Boolean isLoading()
    {
        return _isLoading;
    }

    @JavascriptInterface
    public Boolean canGoBack()
    {
        return webView.canGoBack();
    }

    @JavascriptInterface
    public Boolean canGoForward()
    {
        return webView.canGoForward();
    }

    @JavascriptInterface
    public void executeJavaScript(String code, String userGesture) throws Exception
    {
        _browserWindow.context.evaluateJavaScript(code, null);
    }

    @JavascriptInterface
    public String getUserAgent()
    {
        return webView.getSettings().getUserAgentString();
    }

    @JavascriptInterface
    public void goBack()
    {
        webView.goBack();
    }

    @JavascriptInterface
    public void goForward()
    {
        webView.goForward();
    }

    @JavascriptInterface
    public void reload()
    {
        webView.reload();
    }

    @JavascriptInterface
    public void reloadIgnoringCache()
    {
        webView.loadUrl(webView.getUrl());
    }

    @JavascriptInterface
    public void setUserAgent(String userAgent)
    {
       throw new UnsupportedOperationException("Not Implemented");
    }

    @JavascriptInterface
    public void stop()
    {
        webView.stopLoading();
    }

    /* ****************************************************************** *
     *               REMAINDER OF ELECTRO API NOT IMPLEMENTED             *
     * ****************************************************************** */
    public NKScriptValue getSession()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void addWorkSpace(String path)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void beginFrameSubscription(NKScriptValue callback)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void canGoToOffset(int offset)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void clearHistory()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void closeDevTools()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void copyclipboard()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void cut()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void delete()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void disableDeviceEmulation()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void downloadURL(String url)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void enableDeviceEmulation(Map<String, Object> parameters)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void endFrameSubscription()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void goToIndex(int index)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void goToOffset(int offset)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void hasServiceWorker(NKScriptValue callback)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void insertCSS(String css)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void inspectElement(int x, int y)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void inspectServiceWorker()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public Boolean isAudioMuted()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void isCrashed()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void isDevToolsFocused()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void isDevToolsOpened()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public Boolean isWaitingForResponse()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void openDevTools(Map<String, Object> options)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void paste()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void pasteAndMatchStyle()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void print(Map<String, Object> options)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void printToPDF(Map<String, Object> options, NKScriptValue callback)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void redo()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void removeWorkSpace(String path)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void replace(String text)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void replaceMisspelling(String text)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void savePage(String fullstring, String saveType, NKScriptValue callback)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void selectAll()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void sendInputEvent(Map<String, Object> e)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void setAudioMuted(Boolean muted)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void toggleDevTools()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void undo()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void unregisterServiceWorker(NKScriptValue callback)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void unselect()
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    // Event:  'certificate-error'
    // Event:  'crashed'
    // Event:  'destroyed'
    // Event:  'devtools-closed'
    // Event:  'devtools-focused'
    // Event:  'devtools-opened'
    // Event:  'did-frame-finish-load'
    // Event:  'did-get-redirect-request'
    // Event:  'did-get-response-details'
    // Event:  'did-start-loading'
    // Event:  'did-stop-loading'
    // Event:  'dom-ready'
    // Event:  'login'
    // Event:  'new-window'
    // Event:  'page-favicon-updated'
    // Event:  'plugin-crashed'
    // Event:  'select-client-certificate'
    // Event:  'will-navigate'

}

