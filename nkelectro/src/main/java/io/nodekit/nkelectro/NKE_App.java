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

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.webkit.JavascriptInterface;

import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.util.NKEventHandler;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptValue;
import java.util.HashMap;
import io.nodekit.nkscripting.util.NKLogging;

public final class NKE_App implements NKScriptExport
{
    public static void attachTo(NKScriptContext context, HashMap<String, Object> appOptions) throws Exception {

        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib_electro/app.js");

        NKE_App app = new NKE_App();

        NKScriptValue jsv = context.loadPlugin(app, "io.nodekit.electro.app", options);

        app.initWithJSValue(jsv);
    }

    private static int windowCount;

    private NKScriptValue jsValue;

    private static NKEventEmitter events = NKEventEmitter.global;

    private void initWithJSValue(NKScriptValue jsv) throws Exception {

        this.jsValue = jsv;

        windowCount = 0;

        events.once("NK.AppReady", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                jsValue.invokeMethod("emit", new String[] { "ready" });
            }
        });

        events.once("NK.AppDidFinishLaunching", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                jsValue.invokeMethod("emit", new String[] { "will-finish-launching" });
            }
        });

        events.once("NK.AppWillTerminate", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                jsValue.invokeMethod("emit", new String[] { "will-quit" });
                jsValue.invokeMethod("emit", new String[] { "quit" });
            }
        });


        events.once("NK.ProcessAdded", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                windowCount++;
            }
        });

        events.once("NK.WindowAdded", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                windowCount++;
            }
        });

        events.once("NK.ProcessRemoved", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                windowCount--;
                if (windowCount == 0)
                    events.emit("NKE.WindowAllClosed", "");
            }
        });


        events.once("NK.WindowRemoved", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                windowCount--;
                if (windowCount == 0)
                    events.emit("NKE.WindowAllClosed", "");
            }
        });

        events.once("NK.WindowAllClosed", new NKEventHandler<String>() {
            protected void call(String event, String test) {
                jsValue.invokeMethod("emit", new String[] { "window-all-closed" });
            }
        });


    }

    @JavascriptInterface
    public void quit() throws Exception {
    }

    @JavascriptInterface
    public void exit(int exitCode) throws Exception {
    }

    @JavascriptInterface
    public String getAppPath() throws Exception {
        return getSpecialPath("exe");
    }

    @JavascriptInterface
    public String getPath(String name) throws Exception {
        return getSpecialPath(name);
    }

    @JavascriptInterface
    public String getVersion() throws Exception {
        PackageInfo pInfo = NKApplication.getAppContext().getPackageManager().getPackageInfo( NKApplication.getAppContext().getPackageName(), 0);
        return pInfo.versionName;
    }

    @JavascriptInterface
    public String getName() throws Exception {
        return NKApplication.getAppContext().getPackageName();
    }

    // NOT IMPLEMENTED
    public void addRecentDocument(String path)  {
    }

    public void allowNTLMCredentialsForAllDomains(boolean allow) {
    }

    public void appendArgument(String value) {
    }

    public void appendSwitch(String switchvalue, String value)  {
    }

    public void clearRecentDocuments(String path) {
    }

    public int dockBounce(String type) {
        return 0;
    }

    public void dockCancelBounce(int id) {
    }

    public String dockGetBadge() {
        return null;
    }

    public void dockHide()  {
    }

    public void dockSetBadge(String text) {
    }

    public void dockSetMenu(Object menu)  {
    }

    public void dockShow() {
    }

    public String getLocale() {
        return null;

    }

    public void makeSingleInstance()  {
    }

    public void setAppUserModelId(String id)  {
    }

    public String setPath(String name, String path)  {
        return null;
    }

    public void setUserTasks(HashMap<String, Object> tasks) {

    }


    private String getSpecialPath(String name)  {

        switch(name) {

            case "home":  return NKApplication.getAppContext().getFilesDir().getAbsolutePath();

            case "appData":  return NKApplication.getAppContext().getFilesDir().getAbsolutePath();

            case "userData": return NKApplication.getAppContext().getFilesDir().getAbsolutePath();

            case "temp": return NKApplication.getAppContext().getCacheDir().getAbsolutePath();

            case "exe": return NKApplication.getAppContext().getApplicationInfo().dataDir;

            case "module": return "";

            case "desktop": return "";

            case "documents":  return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();

            case "downloads":  return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

            case "music": return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

            case "pictures":  return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();

            case "videos": return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();

            default: return "";

        }

    }

}

// Event: 'before-quit'
// Event: 'will-quit'
// Event: 'quit'
// Event: 'open-file' OS X
// Event: 'open-url' OS X
// Event: 'activate' OS X
// Event: 'browser-window-blur'
// Event: 'browser-window-focus'
// Event: 'browser-window-created'
// Event: 'certificate-error'
// Event: 'select-client-certificate'
// Event: 'login'
// Event: 'gpu-process-crashed'