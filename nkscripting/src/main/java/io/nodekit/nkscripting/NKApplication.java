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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class NKApplication {

    private static Context mContext;

    public static Context getAppContext() {
        return mContext;
    }

    public static View getRootView() {
        return ((Activity) mContext).getWindow().getDecorView().getRootView();
    }

    public static void setAppContext(Activity activity) {
        mContext = activity;

        NKApplication.UIHandler = new Handler(Looper.getMainLooper());
    }

    public static Handler UIHandler;

}