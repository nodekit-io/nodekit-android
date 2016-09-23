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
import java.util.Map;

import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.util.NKEventHandler;

final class NKE_IpcRenderer
{

    static void attachTo(NKScriptContext context, Map<String, Object> appOptions) throws Exception {

        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib_electro/ipcrenderer.js");

        NKE_IpcRenderer principal = new NKE_IpcRenderer(context.id());

        NKScriptValue jsv = context.loadPlugin(principal, "io.nodekit.electro.ipcRenderer", options);

        principal.initWithJSValue(jsv);
    }


    private static NKEventEmitter globalEvents = NKEventEmitter.global;
    NKE_BrowserWindow _window;
    int _id;

    private NKScriptValue jsValue;

    private NKE_IpcRenderer(int id) throws Exception {
        _id = id;

        // IPC Renderer runs in same process as NKE BrowserWindow so can get actual host object
        _window = NKE_BrowserWindow.fromId(id);

        String ids = Integer.toString(id);

    }

    private void initWithJSValue(NKScriptValue jsv)  {

        this.jsValue = jsv;

        String ids = Integer.toString(_id);

        _window.events.on("NKE.IPCtoRenderer", new NKEventHandler<NKE_Event>() {
            protected void call(String event, NKE_Event item) {
                jsValue.invokeMethod("emit", new Object[]{"NKE.IPCtoRenderer", item.getsender(), item.getchannel(), item.getreplyId(), item.getarg() });
            }
        });

        globalEvents.on("NKE.IPCReplytoRenderer." + ids, new NKEventHandler<NKE_Event>() {
            protected void call(String event, NKE_Event item) {
                jsValue.invokeMethod("emit", new Object[]{"NKE.IPCtoRenderer", item.getsender(), item.getchannel(), item.getreplyId(), item.getarg() });
            }
        });

    }

    // Messages to main are sent to the global events queue, potentially cross-process
    public void ipcSend(String channel, String replyId, Object[] arg) throws Exception {
        NKE_Event payload = new NKE_Event(0, channel, replyId, arg);
        globalEvents.emit("NKE.IPCtoMain", payload, true);
    }

    // Replies to main are sent directly to the webContents window in this local process that sent the original message
    public void ipcReply(int dest, String channel, String replyId, Object result) throws Exception {
        NKE_Event payload = new NKE_Event(0, channel, replyId, null);
        _window.events.emit("NKE.IPCReplytoMain", payload, false  );
    }

}


