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

package io.nodekit.nkscripting;

public interface NKScriptExport
{

    public interface Overrides
    {
        String rewriteGeneratedStub( String stub, String forKey);
    }

    public class Proxy<T> implements Overrides
    {
        private Overrides plugin;

        public Proxy(Object plugin){

            if (plugin instanceof Overrides) {
                this.plugin = (Overrides)plugin;
            } else
            {
                this.plugin = null;
            }

        }

        public String rewriteGeneratedStub(String stub, String forKey)
        {
            if (plugin != null)
                return plugin.rewriteGeneratedStub(stub, forKey);
            else
            return stub;
        }

    }

    public enum NKScriptExportType
    {
        NKScriptExport,
        // Custom Channelbridge provided by NKScripting
        JavascriptInterface
        // Native webview interface provided by Android OS
    }

}




