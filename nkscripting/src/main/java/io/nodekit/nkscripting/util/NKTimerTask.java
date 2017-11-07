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

import android.support.annotation.NonNull;
import java.util.concurrent.ScheduledFuture;
import io.nodekit.nkscripting.NKScriptValue;

public class NKTimerTask {

    private ScheduledFuture<?> future;
    private NKScriptValue callback;
    private boolean isRepeating;

    NKTimerTask(@NonNull ScheduledFuture<?> future, @NonNull NKScriptValue callback, boolean isRepeating) {
        this.future = future;
        this.callback = callback;
        this.isRepeating = isRepeating;
    }

    public @NonNull NKScriptValue getCallback() {
        return callback;
    }

    boolean isRepeating() {
        return isRepeating;
    }

    void cancel() {

        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }
}
