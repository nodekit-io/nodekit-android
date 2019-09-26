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

import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.NKScriptValueImpl;
import io.nodekit.nkscripting.util.NKEventHandler;

abstract class NKE_WebContents {

    NKE_BrowserWindow _browserWindow;

    int _id;

    NKE_WebContents() {}

    NKE_WebContents(NKE_BrowserWindow browserWindow)
    {
        this._browserWindow = browserWindow;
        this._id = browserWindow.id();

        // Event:  'did-fail-load'
        // Event:  'did-finish-load'

        _browserWindow.events.on("NKE.DidFinishLoad", new NKEventHandler<String>() {
            protected void call(String event, String item) {
                NKScriptValueImpl.invokeMethodForObject(this, "emit", new String[]{"did-finish-load"});
            }
        });

        _browserWindow.events.on("NKE.DidFailLoading", new NKEventHandler<String>() {
            protected void call(String event, String item) {
                NKScriptValueImpl.invokeMethodForObject(this, "emit", new String[]{"did-fail-loading"});
            }
        });

    }

    abstract public void createWebView(int id, HashMap<String, Object> options);

    void init_IPC()
    {
        _browserWindow.events.on("NKE.IPCReplytoMain", new NKEventHandler<NKE_Event>() {
            protected void call(String event, NKE_Event item) {
                NKScriptValueImpl.invokeMethodForObject(this, "emit", new Object[]{"NKE.IPCReplytoMain", item.getsender(), item.getchannel(), item.getreplyId(), item.getarg()[0]});
            }
        });
    }

    // Messages to renderer are sent to the window events queue for that renderer which will be in same process as ipcRenderer
    public void ipcSend(String channel, String replyId, Object[] arg)
    {
        NKE_Event payload = new NKE_Event(0, channel, replyId, arg);
        _browserWindow.events.emit("NKE.IPCtoRenderer", payload);
    }

    // Replies to renderer to the window events queue for that renderer
    public void ipcReply(int dest, String channel, String replyId, Object result)
    {
        NKE_Event payload = new NKE_Event(0, channel, replyId, new Object[] { result });
        _browserWindow.events.emit("NKE.IPCReplytoRenderer", payload);
    }

}