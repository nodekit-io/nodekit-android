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

import android.webkit.JavascriptInterface;

import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.util.NKEventHandler;

public final class NKE_IpcMain   
{
    public static void attachTo(NKScriptContext context, HashMap<String, Object> appOptions) throws Exception {

        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib_electro/ipcmain.js");

        NKE_IpcMain ipc = new NKE_IpcMain();

        NKScriptValue jsv = context.loadPlugin(ipc, "io.nodekit.electro.ipcMain", options);

        ipc.initWithJSValue(jsv);
    }

    private static NKEventEmitter globalEvents = NKEventEmitter.global;

    private NKScriptValue jsValue;

    private void initWithJSValue(NKScriptValue jsv) {

        this.jsValue = jsv;

        globalEvents.on("NK.IPCtoMain", new NKEventHandler<NKE_Event>() {
            protected void call(String event, NKE_Event item) {
                jsValue.invokeMethod("emit", new Object[]{"NKE.IPCtoMain", item.getsender(), item.getchannel(), item.getreplyId(), item.getarg() });
            }
        });
    }

    // Forward replies to renderer to the events queue for that renderer, using global queue since we may be cross process
    @JavascriptInterface
    public static void ipcReply(int dest, String channel, String replyId, Object result) {
        NKE_Event payload = new NKE_Event(0, channel, replyId, null);
        globalEvents.emit("NKE.IPCReplytoRenderer." + dest, payload, true);
    }

    public static void ipcSend(String channel, String replyId, Object[] arg) throws Exception {
        throw new UnsupportedOperationException("Event subscription only API.  Sends are handled in WebContents API");
    }

}


