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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import static android.content.Context.WINDOW_SERVICE;

public class NKApplication {

    private static Context mContext;

    public static Handler UIHandler = new Handler(Looper.getMainLooper());

    public static Context getAppContext() {
        return mContext;
    }

    public static void setAppContext(Context context) {
        mContext = context;
    }

    @SuppressLint("setJavascriptEnabled")
    public static WebView createInvisibleWebViewInWindow() {

        if (mContext == null) {

            throw new RuntimeException("Must call NKApplication.setAppContext before creating an NKScriptContext");
        }

        if (!checkSystemAlertPermission()) {

            throw new RuntimeException("Must grant " + Manifest.permission.SYSTEM_ALERT_WINDOW + " permission to run in backgroud.");
        }

        WebView.setWebContentsDebuggingEnabled(true);

        WebView webView = new WebView(mContext);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.INVISIBLE);

        final WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        params.width = 0;
        params.height = 0;

        windowManager.addView(webView, params);

        return webView;
    }

    private static boolean checkSystemAlertPermission() {

        int res = mContext.checkCallingOrSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);

        return (res == PackageManager.PERMISSION_GRANTED);
    }
}