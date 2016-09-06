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

public class NKE_Event extends HashMap<String, Object>
{
    public int getsender()  {
        return (int)this.get("sender");
    }

    public String getchannel()  {
        return (String)this.get("channel");
    }

    public String getreplyId()  {
        return (String)this.get("replyId");
    }

    public Object[] getarg() {
        return (Object[])this.get("arg");
    }

    public NKE_Event(int sender, String channel, String replyId, Object[] arg)  {
        this.put("sender", sender);
        this.put("channel", channel);
        this.put("replyId", replyId);
        this.put("arg", arg);
    }

    public NKE_Event(HashMap<String, Object> dict) {
        this.put("sender", dict.get("sender"));
        this.put("channel", dict.get("channel"));
        this.put("replyId", dict.get("replyId"));
        this.put("arg", dict.get("arg"));
    }
}