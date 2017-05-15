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

import android.util.Log;
import java.util.HashMap;

public class NKLogging {

    public static class Entry {
        String message = null;
        Level severity = null;
        HashMap<String, String> labels = null;

        Entry(String message, Level severity, HashMap<String, String> labels){
            this.severity = severity;
            this.message = message;
            this.labels = labels;
        }

        public String getMessage() {
            return message;
        }

        public String getSeverity() {
            return severity.toString();
        }

        public HashMap<String, String>  getLabels() {
            return labels;
        }
    }

    public enum Level
    {
        Emergency,
        Alert ,
        Critical,
        Error ,
        Warning ,
        Notice,
        Info ,
        Debug
    }

    private static final String TAG = "NodeKit";

    public static void log(String message)
    {
       log(message, Level.Debug, new HashMap<String, String>() );
    }

    public static void log(String message, Level severity)
    {
        log(message, severity, new HashMap<String, String>());
    }

    public static void log(String message, String severity, HashMap<String, String> labels)
    {
        log(message, Level.valueOf(severity), labels);
    }

    public static void log(String message, Level severity, HashMap<String, String> labels)
    {
        Log.v(TAG, message);
        NKEventEmitter.global.emit("log", new Entry(message, severity, labels));
    }

    public static void log(Exception e)
    {
        log("ERROR " + e.toString(), Level.Error, new HashMap<String, String>() );
        e.printStackTrace();
    }

}
