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

package io.nodekit.nodekitandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.util.NKLogging;

import io.nodekit.nkelectro.NKElectro;
import io.nodekit.nkscripting.util.NKStorage;
import io.nodekit.nkscripting.util.NKEventEmitter;

import android.widget.Button;

public class MainActivity extends Activity implements NKScriptContext.NKScriptContextDelegate, android.view.View.OnClickListener {

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_container);
        NKApplication.setAppContext(this);

        Button mClickButton1 = (Button)findViewById(R.id.button);
        mClickButton1.setOnClickListener(this);

        NKEventEmitter.global.emit("NK.AppReady", "");
    }


// somewhere else in your code

    public void onClick(android.view.View v) {
        switch (v.getId()) {
            case  R.id.button: {
                if (this.isRunning) {
                    stop();
                } else {
                    start();
                }
                break;
            }

        }
    }

    void start() {

        if (!checkDrawOverlayPermission()) {

            return;
        }

        try {
            NKScriptContextFactory.createContext(null, this);
        }
        catch (Exception e) {
            Log.v("NodeKitAndroid", e.toString());
        }
    }

    void stop() {
        context.tearDown();
        context = null;
        isRunning = false;
    }

    public final static int REQUEST_CODE = -1010101;

    public boolean checkDrawOverlayPermission() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.canDrawOverlays(this)) {

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));

                startActivityForResult(intent, REQUEST_CODE);

                return false;
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= 23) {

            if (Settings.canDrawOverlays(this)) {

                start();
            }
        }
    }

    public void NKScriptEngineDidLoad(NKScriptContext context) {
        NKLogging.log("ScriptEngine Loaded");
        this.context = context;

        HashMap<String, Object> optionsDefault = new HashMap<String, Object>();
        try {
            NKElectro.addToContext(context, optionsDefault);
        } catch (Exception e) {
            NKLogging.log(e);
        }

    }

    private NKScriptContext context;

    public void NKScriptEngineReady(NKScriptContext context) {

        NKLogging.log("ScriptEngine Ready");

        bootstrap();
    }

    void bootstrap() {

        String script = "process.bootstrap('app/index.js');";

        try {
            context.evaluateJavaScript(script, null);
            NKEventEmitter.global.emit("NK.AppReady", "");
            isRunning = true;
        } catch (Exception e) {
            NKLogging.log(e);
        }
    }
}
