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

package io.nodekit.nkscripting.channelbridge;

import java.util.Map;

public class NKScriptMessage
{
    public interface Handler
    {
        void didReceiveScriptMessage(NKScriptMessage message)  ;
        Object didReceiveScriptMessageSync(NKScriptMessage message)  ;
    }


    public interface Controller
    {
        void addScriptMessageHandler(Handler scriptMessageHandler, String name) throws Exception;
        void removeScriptMessageHandlerForName(String name) throws Exception;
    }

    public Object body;
    public String name;

    public NKScriptMessage(String name, Object body) {
        this.body = body;
        this.name = name;
    }

    public NKScriptMessage(Map<String, Object> dictionary) {
        this.body = dictionary.get("body");
        this.name = (String)dictionary.get("name");
    }
}
