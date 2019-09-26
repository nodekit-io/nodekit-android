package io.nodekit.engine.queue;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Alternate implementation that uses thread pool */
public class MessageWorkerPoolQueue {
    
    public MessageWorkerPoolQueue(final Runnable monitor) {
        mMonitor = monitor;
    }

    final Runnable mMonitor;

    private class JSTask extends AsyncTask<Runnable, Void, Exception> {
        @Override
        public Exception doInBackground(Runnable... params) {
            try {
                params[0].run();
                mMonitor.run();
            } catch (Exception e) {
                return e;
            }
            return null;
        }
    }

    public void sync(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                Exception e = new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable).get();
                if (e != null)  Log.e("JSWorkerQueue", e.getMessage());
            } catch (ExecutionException e) {
                Log.e("MessageWorkerPoolQueue", e.getMessage());
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        } else {
            runnable.run();
            mMonitor.run();
        }
    }

    public void async(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable);
        } else {
            runnable.run();
            mMonitor.run();
        }
    }

    public void quit() {

    }
}