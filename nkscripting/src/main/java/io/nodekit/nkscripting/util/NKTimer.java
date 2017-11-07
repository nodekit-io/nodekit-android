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

import android.os.Handler;
import android.webkit.JavascriptInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.NKScriptValue;


public class NKTimer implements NKScriptExport {

    private static String JS_NAMPESPACE = "NodeKitTimer";

    private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(100);

    private Map<String, NKTimerTask> tasks = new HashMap<>();

    public static void attachTo( NKScriptContext context) throws Exception {
        HashMap<String,Object> options = new HashMap<String, Object>();

        options.put("js","lib-scripting/timer.js");

        context.loadPlugin(new NKTimer(), JS_NAMPESPACE, options);
    }

    @JavascriptInterface
    public String setTimeoutSync(NKScriptValue callback, Number milliseconds) {

        Long delay = milliseconds.longValue();

        final String uuid = newUUID();

        ScheduledFuture<?> future = executor.schedule(new Runnable() {
            @Override
            public void run() {
                handlerTaskFire(uuid);
            }
        }, delay, TimeUnit.MILLISECONDS);

        NKTimerTask task = new NKTimerTask(future, callback, false);

        tasks.put(uuid, task);

        return uuid;
    }

    @JavascriptInterface
    public String setIntervalSync(NKScriptValue callback, Number milliseconds) {

        Long delay = milliseconds.longValue();

        final String uuid = newUUID();

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                handlerTaskFire(uuid);
            }
        }, 0, delay, TimeUnit.MILLISECONDS);

        NKTimerTask task = new NKTimerTask(future, callback, true);

        tasks.put(uuid, task);

        return uuid;
    }

    @JavascriptInterface
    public void clearTimeoutSync(String identifier) {

        NKTimerTask task = tasks.remove(identifier);

        if (task != null) {

            task.cancel();
        }
    }

    private void handlerTaskFire(String identifier) {

        final NKTimerTask task = tasks.get(identifier);

        if (task == null) {

            return;
        }

        final NKScriptValue callback = task.getCallback();

        if (!task.isRepeating()) {

            clearTimeoutSync(identifier);
        }

        Handler mainHandler = new Handler(NKApplication.getAppContext().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                Object[] args = new Object[0];

                callback.callWithArguments(args, null);
            }
        };
        mainHandler.post(myRunnable);
    }

    private String newUUID() {

        String uuid;

        do {

            uuid = UUID.randomUUID().toString();

        } while (tasks.get(uuid) != null);

        return uuid;
    }
}
