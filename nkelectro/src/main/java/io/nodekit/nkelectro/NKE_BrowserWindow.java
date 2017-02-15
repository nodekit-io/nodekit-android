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
import android.webkit.JavascriptInterface;

import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.util.NKEventHandler;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.NKApplication;

class NKE_BrowserWindow  implements NKScriptExport
{

    public static void attachTo(NKScriptContext context, Map<String, Object> appOptions) throws Exception {
        HashMap<String,Object> options = new HashMap<String, Object>();
        options.put("js","lib_electro/browserwindow.js");

        context.loadPlugin(NKE_BrowserWindow.class, "io.nodekit.electro.BrowserWindow", options);

    }

    private void initWithJSValue(NKScriptValue jsv) {
        this.jsValue = jsv;
    }

    // Package Lcoal Fields
    NKEventEmitter events = new NKEventEmitter();
    NKScriptContext context;
    Object webView = new Object();
    private NKE_BrowserWindow.Type browserType ;

    // Private fields
    private static SparseArray<NKE_BrowserWindow> windowArray = new SparseArray<NKE_BrowserWindow>();
    private int _id = 0;

    private NKE_WebContents _webContents;
    private NKEventEmitter globalEvents = NKEventEmitter.global;
    private NKScriptValue jsValue;

    // private constructor, called for principal only
    private NKE_BrowserWindow() {}


    // PUBLIC CONSTRUCTOR ACCESSIBLE FROM JAVASCRIPT
    public NKE_BrowserWindow(HashMap<String, Object> options, NKScriptValue jsvalue) {

        _id = NKScriptContextFactory.sequenceNumber++;

        this.jsValue = jsvalue;

        if (options == null)
            options = new HashMap<String, Object>();
         
        windowArray.put(_id, this);

        try {

            _webContents = new NKE_WebContents_AndroidWebView(this);

            // PARSE & STORE OPTIONS
            if (options.containsKey(NKE_BrowserWindow.Options.nkBrowserType))
                browserType = NKE_BrowserWindow.Type.valueOf((String) options.get(NKE_BrowserWindow.Options.nkBrowserType));
            else
                browserType = NKE_BrowserWindow.Type.WebView;

            final HashMap<String, Object> _options = options;

            switch (browserType) {
                case WebView:
                    NKLogging.log("+creating Native (Android WebView) Renderer");

                    NKApplication.UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            _webContents.createWebView(_id, _options);
                        }
                    });

                    break;
                default:
                    break;

            }
        } catch (Exception e) {
            NKLogging.log(e);
        }

        if (jsValue != null) {
            events.on("NKE.DidFinishLoad", new NKEventHandler<String>() {
                protected void call(String event, String test) {
                    jsValue.invokeMethod("emit", new String[]{"did-finish-load"});
                }
            });
        }

    }

    // class/helper functions (for Java use only, equivalent functions exist in .js helper )
    static NKE_BrowserWindow fromId(int id) {
        return windowArray.get(id);
    }

    @JavascriptInterface
    public int id()  {
        return _id;
    }

    @JavascriptInterface
    public NKE_WebContents webContents()  {
        return _webContents;
    }

    // PUBLIC CONSTANTS

    enum Type
    {
        WebView,
        // Native webview provided by Android OS
        CrossWalk
        // open source embedded Chromium-based engine
    }


    class Switches
    {
        // Enable plugins.
        public static final String kEnablePlugins = "enable-plugins";
        // Ppapi Flash path.
        public static final String kPpapiFlashPath = "ppapi-flash-path";
        // Ppapi Flash version.
        public static final String kPpapiFlashVersion = "ppapi-flash-version";
        // Path to client certificate.
        public static final String kClientCertificate = "client-certificate";
        // Disable HTTP cache.
        public static final String kDisableHttpCache = "disable-http-cache";
        // Register schemes to standard.
        public static final String kRegisterStandardSchemes = "register-standard-schemes";
        // Register schemes to handle service worker.
        public static final String kRegisterServiceWorkerSchemes = "register-service-worker-schemes";
        // The minimum SSL/TLS version ("tls1", "tls1.1", or "tls1.2") that
        // TLS fallback will accept.
        public static final String kSSLVersionFallbackMin = "ssl-version-fallback-min";
        // Comma-separated list of SSL cipher suites to disable.
        public static final String kCipherSuiteBlacklist = "cipher-suite-blacklist";
        // The browser process app model ID
        public static final String kAppUserModelId = "app-user-model-id";
        // The command line switch versions of the options.
        public static final String kZoomFactor = "zoom-factor";
        public static final String kPreloadScript = "preload";
        public static final String kPreloadURL = "preload-url";
        public static final String kNodeIntegration = "node-integration";
        public static final String kGuestInstanceID = "guest-instance-id";
        public static final String kOpenerID = "opener-id";
        // Widevine options
        // Path to Widevine CDM binaries.
        public static final String kWidevineCdmPath = "widevine-cdm-path";
        // Widevine CDM version.
        public static final String kWidevineCdmVersion = "widevine-cdm-version";
    }

    class Options
    {

        static final String nkBrowserType = "nk.browserType";
        public static final String kTitle = "title";
        public static final String kIcon = "icon";
        public static final String kFrame = "frame";
        public static final String kShow = "show";
        public static final String kCenter = "center";
        public static final String kX = "x";
        public static final String kY = "y";
        public static final String kWidth = "width";
        public static final String kHeight = "height";
        public static final String kMinWidth = "minWidth";
        public static final String kMinHeight = "minHeight";
        public static final String kMaxWidth = "maxWidth";
        public static final String kMaxHeight = "maxHeight";
        public static final String kResizable = "resizable";
        public static final String kFullscreen = "fullscreen";
        // Whether the window should show in taskbar.
        public static final String kSkipTaskbar = "skipTaskbar";
        // Start with the kiosk mode, see Opera's page for description:
        // http://www.opera.com/support/mastering/kiosk/
        public static final String kKiosk = "kiosk";
        // Make windows stays on the top of all other windows.
        public static final String kAlwaysOnTop = "alwaysOnTop";
        // Enable the NSView to accept first mouse event.
        public static final String kAcceptFirstMouse = "acceptFirstMouse";
        // Whether window size should include window frame.
        public static final String kUseContentSize = "useContentSize";
        // The requested title bar style for the window
        public static final String kTitleBarStyle = "titleBarStyle";
        // The menu bar is hidden unless "Alt" is pressed.
        public static final String kAutoHideMenuBar = "autoHideMenuBar";
        // Enable window to be resized larger than screen.
        public static final String kEnableLargerThanScreen = "enableLargerThanScreen";
        // Forces to use dark theme on Linux.
        public static final String kDarkTheme = "darkTheme";
        // Whether the window should be transparent.
        public static final String kTransparent = "transparent";
        // Window type hint.
        public static final String kType = "type";
        // Disable auto-hiding cursor.
        public static final String kDisableAutoHideCursor = "disableAutoHideCursor";
        // Use the OS X's standard window instead of the textured window.
        public static final String kStandardWindow = "standardWindow";
        // Default browser window background color.
        public static final String kBackgroundColor = "backgroundColor";
        // The WebPreferences.
        public static final String kWebPreferences = "webPreferences";
        // The factor of which page should be zoomed.
        public static final String kZoomFactor = "zoomFactor";
        // Script that will be loaded by guest WebContents before other scripts.
        public static final String kPreloadScript = "preload";
        // Like --preload, but the passed argument is an URL.
        static final String kPreloadURL = "preloadURL";
        // Enable the node integration.
        public static final String kNodeIntegration = "nodeIntegration";
        // Instancd ID of guest WebContents.
        public static final String kGuestInstanceID = "guestInstanceId";
        // Enable DirecNKRemotingMessage on Windows.
        public static final String kDirecNKRemotingMessage = "direcNKRemotingMessage";
        // Web runtime features.
        public static final String kExperimentalFeatures = "experimentalFeatures";
        public static final String kExperimentalCanvasFeatures = "experimentalCanvasFeatures";
        // Opener window's ID.
        public static final String kOpenerID = "openerId";
        // Enable blink features.
        public static final String kBlinkFeatures = "blinkFeatures";
    }


}


