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

package io.nodekit.nkscripting.util;

import android.webkit.JavascriptInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKApplication;

public class NKStorage {

    @JavascriptInterface
    public String getSourceSync(String module) {
        return NKStorage.getResource(module);
    }

 //   func existsSync(module: String) -> Bool
  // func statSync(module: String) -> Dictionary<String, AnyObject>

 //   func getDirectorySync(module: String) -> [String]

    public static String getResource(String fileName) {

         StringBuilder sb = new StringBuilder();

        try {
            InputStream stream = NKApplication.getAppContext().getAssets().open(fileName);
            byte buffer[] = new byte[16384];  // read 16k blocks
            int len; // how much content was read?
            while( ( len = stream.read( buffer ) ) > 0 ){
                sb.append(new String(buffer, 0, len));
            }

            stream.close();
        } catch (IOException e) {
            NKLogging.log("Error reading " + fileName + ": " + e.toString());
        }

        return sb.toString();
    }

    public static void attachTo( NKScriptContext context) throws Exception
    {
        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib-scripting/native_module.js");

        context.loadPlugin(new NKStorage(), "io.nodekit.scripting.storage", options);
    }

}
