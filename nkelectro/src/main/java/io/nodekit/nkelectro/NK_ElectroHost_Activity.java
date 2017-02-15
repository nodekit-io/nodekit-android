/*
* nodekit.io
*
* Copyright (c) 2016-7 OffGrid Networks. All Rights Reserved.
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

import android.app.Activity;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.util.NKLogging;

import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.util.NKSerialize;
import io.nodekit.nkscripting.util.NKStorage;

public class NK_ElectroHost_Activity extends Activity implements NKScriptContext.NKScriptContextDelegate  {

    protected HashMap<String, Object> options;

    protected NKE_BrowserWindow uiHostWindow;

    protected NK_ElectroHost_Activity() {
       this.options = new HashMap<String, Object>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_container);
        NKApplication.setAppContext(this);

        if (!this.options.containsKey("main")) {
            try {
                String packageJson = NKStorage.getResource("app/app.nkar/package.json");
                Map<String, Object> body = NKSerialize.deserialize(packageJson);

                if (body.containsKey("nodekit"))
                {
                    Map<String, Object>  config = (Map<String, Object> ) body.get("nodekit");
                    if (config.containsKey("main")) {
                        this.options.put("main", config.get("main"));
                    } else if (body.containsKey("main"))
                    {
                        this.options.put("main", body.get("main"));
                    }
                }

            } catch (Exception e) {
                NKLogging.log(e);
            }
        }

        if (options.containsKey("main") && ((String)options.get("main")).startsWith("app://")) {

                HashMap<String, Object> uiOptions = new HashMap<String, Object>();
                uiOptions.put("nk.InstallElectro", false);
                uiOptions.put("nk.ScriptContextDelegate", this);

                if (options.containsKey("nk.allowCustomProtocol"))
                    uiOptions.put("nk.allowCustomProtocol", options.get("nk.allowCustomProtocol"));

                if (options.containsKey("nk.taskBarPopup"))
                    uiOptions.put("nk.taskBarPopup", options.get("nk.taskBarPopup"));

                if (options.containsKey("nk.taskBarIcon"))
                    uiOptions.put("nk.taskBarIcon", options.get("nk.taskBarIcon"));

                if (options.containsKey("width"))
                    uiOptions.put("width", options.get("width"));
                if (options.containsKey("height"))
                    uiOptions.put("height", options.get("height"));

                uiOptions.put("preloadURL", options.get("main"));

                if (options.containsKey("Engine"))
                    uiOptions.put("Engine", options.get("Engine"));

                if (options.containsKey("title"))
                    uiOptions.put("title", options.get("title"));

                uiHostWindow = new NKE_BrowserWindow(uiOptions, null);


        }

        else
        {
            try {
                NKScriptContextFactory.createContext(this.options, this);
            }
            catch (Exception e) {
                NKLogging.log(e);
            }

        }

    }

    public void NKScriptEngineDidLoad(NKScriptContext context) {
        NKLogging.log("ScriptEngine Loaded");
        this.context = context;

        try {
            NKElectro.addToContext(context, this.options);
        } catch (Exception e) {
            NKLogging.log(e);
        }

    }

    private NKScriptContext context;

    public void NKScriptEngineReady(NKScriptContext context) {
        NKLogging.log("ScriptEngine Ready");

        if (this.options.containsKey("main")) {
            String script = "process.bootstrap('app/" + (String)this.options.get("main") + "');";

            try {
                context.evaluateJavaScript(script, null);
            } catch (Exception e) {
                NKLogging.log(e);
            }
        }

        NKEventEmitter.global.emit("NK.AppReady", "");

    }
}
