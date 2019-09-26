package io.nodekit.engine.javascriptcore;

import io.nodekit.engine.queue.MessageWorkerPoolQueue;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class JSString {

    private static final MessageWorkerPoolQueue workerQueue = new MessageWorkerPoolQueue(new Runnable() {
        @Override
        public void run() {
        }
    });

    private abstract class JNIStringReturnClass implements Runnable {
        String string;
    }

    protected Long stringRef;

    /**
     * Creates a JavaScript string from a Java string
     *
     * @param s The Java string with which to initialize the JavaScript string
     *
     */
    public JSString(final String s) {
        if (s == null) stringRef = 0L;
        else {
            workerQueue.sync(new Runnable() {
                @Override
                public void run() {
                    stringRef = NJScreateWithCharacters(s);
                }
            });
        }
    }

    /**
     * Wraps an existing JavaScript string
     *
     * @param stringRef The JavaScriptCore reference to the string
     */
    public JSString(Long stringRef) {
        this.stringRef = stringRef;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (stringRef != 0)
            NJSrelease(stringRef);
    }

    @Override
    public String toString() {
        JNIStringReturnClass payload = new JNIStringReturnClass() {
            @Override
            public void run() {
                string = NJStoString(stringRef);
            }
        };
        workerQueue.sync(payload);
        return payload.string;
    }

    /**
     * Gets the JavaScriptCore string reference
     *
     * @return the JavaScriptCore string reference
     *
     */
    public Long stringRef() {
        return stringRef;
    }

    protected native long NJScreateWithCharacters(String str);

    protected native long NJSretain(long strRef);

    protected native void NJSrelease(long stringRef);

    protected native boolean NJSisEqual(long a, long b);

    protected native String NJStoString(long strRef);

    @SuppressWarnings("unused")
    protected native int NJSgetLength(long stringRef);

    @SuppressWarnings("unused")
    protected native long NJScreateWithUTF8CString(String str);

    @SuppressWarnings("unused")
    protected native int NJSgetMaximumUTF8CStringSize(long stringRef);

    @SuppressWarnings("unused")
    protected native boolean NJSisEqualToUTF8CString(long a, String b);
}
