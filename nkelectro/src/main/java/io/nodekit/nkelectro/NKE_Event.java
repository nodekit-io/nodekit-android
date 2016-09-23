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

import io.nodekit.nkscripting.NKScriptExport;

class NKE_Event extends HashMap<String, Object>
{
    int getsender()  {
        return (int)this.get("sender");
    }

    String getchannel()  {
        return (String)this.get("channel");
    }

    String getreplyId()  {
        return (String)this.get("replyId");
    }

    Object[] getarg() {
        return (Object[])this.get("arg");
    }

    NKE_Event(int sender, String channel, String replyId, Object[] arg)  {
        this.put("sender", sender);
        this.put("channel", channel);
        this.put("replyId", replyId);
        this.put("arg", arg);
    }

    public NKE_Event(Map<String, Object> dict) {
        this.put("sender", dict.get("sender"));
        this.put("channel", dict.get("channel"));
        this.put("replyId", dict.get("replyId"));
        this.put("arg", dict.get("arg"));
    }
}