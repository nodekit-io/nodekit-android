package io.nodekit.engine.queue;

import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.Handler;
import android.util.Pair;
import android.support.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import android.util.Log;

import io.nodekit.engine.queue.SimpleSettableFuture;

/** Encapsulates a Thread that has a {@link Looper} running on it that can accept Runnables. */

public class MessageQueueThread {

  private final String mName;
  private final Looper mLooper;
  private final MessageQueueThreadHandler mHandler;
  private final String mAssertionErrorMessage;
  private MessageQueueThreadPerfStats mPerfStats;
  private volatile boolean mIsFinished = false;

  private MessageQueueThread(
      String name, Looper looper, QueueThreadExceptionHandler exceptionHandler) {
    this(name, looper, exceptionHandler, null);
  }

  private MessageQueueThread(
      String name,
      Looper looper,
      QueueThreadExceptionHandler exceptionHandler,
      MessageQueueThreadPerfStats stats) {
    mName = name;
    mLooper = looper;
    mHandler = new MessageQueueThreadHandler(looper, exceptionHandler);
    mPerfStats = stats;
    mAssertionErrorMessage = "Expected to be called from the '" + getName() + "' thread!";
  }

  /**
   * Runs the given Runnable on this Thread. It will be submitted to the end of the event queue even
   * if it is being submitted from the same queue Thread.
   */
  public void runOnQueue(Runnable runnable) {
    if (mIsFinished) {
      Log.w(
          "MessageQueeuThread",
          "Tried to enqueue runnable on already finished thread: '"
              + getName()
              + "... dropping Runnable.");
    }
    mHandler.post(runnable);
  }

    public void sync(final Runnable runnable, final Runnable monitor) {
        final SimpleSettableFuture<Boolean> future = new SimpleSettableFuture<Boolean>();
        runOnQueue(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                    future.set(true);
                    monitor.run();
                } catch (Exception e) {
                    future.setException(e);
                }
            }
        });
        try {
            future.get();
        }
        catch (Exception e) {
                future.setException(e);
            }
    }

  /**
   * Runs the given Callable on this Thread. It will be submitted to the end of the event queue even
   * if it is being submitted from the same queue Thread.
   */
  public <T> Future<T> callOnQueue(final Callable<T> callable) {
    final SimpleSettableFuture<T> future = new SimpleSettableFuture<>();
    runOnQueue(
        new Runnable() {
          @Override
          public void run() {
            try {
              future.set(callable.call());
            } catch (Exception e) {
              future.setException(e);
            }
          }
        });
    return future;
  }

   /**
   * @return whether the current Thread is also the Thread associated with this MessageQueueThread.
   */
  public boolean isOnThread() {
    return mLooper.getThread() == Thread.currentThread();
  }

  /**
   * Asserts {@link #isOnThread()}, throwing an exception if the assertion fails.
   */
  public void assertIsOnThread() {
    if (!isOnThread())  throw new RuntimeException(mAssertionErrorMessage);
  }

  /**
   * Asserts {@link #isOnThread()}, throwing an exception if the assertion fails.
   */
  public void assertIsOnThread(String message) {

    if (!isOnThread())  throw new RuntimeException( new StringBuilder().append(mAssertionErrorMessage).append(" ").append(message).toString());
  }

 /**
   * Quits this MessageQueueThread. If called from this MessageQueueThread, this will be the last
   * thing the thread runs. If called from a separate thread, this will block until the thread can
   * be quit and joined.
   */
  public void quitSynchronous() {
    mIsFinished = true;
    mLooper.quit();
    if (mLooper.getThread() != Thread.currentThread()) {
      try {
        mLooper.getThread().join();
      } catch (InterruptedException e) {
        throw new RuntimeException("Got interrupted waiting to join thread " + mName);
      }
    }
  }

  /**
   * Returns the perf counters taken when the framework was started. This method is intended to be
   * used for instrumentation purposes.
   */
  public MessageQueueThreadPerfStats getPerfStats() {
    return mPerfStats;
  }

  /**
   * Resets the perf counters. This is useful if the threads are being re-used. This method is
   * intended to be used for instrumentation purposes.
   */
  public void resetPerfStats() {
    assignToPerfStats(mPerfStats, -1, -1);
    runOnQueue(
        new Runnable() {
          @Override
          public void run() {
            long wallTime = SystemClock.uptimeMillis();
            long cpuTime = SystemClock.currentThreadTimeMillis();
            assignToPerfStats(mPerfStats, wallTime, cpuTime);
          }
        });
  }

  private static void assignToPerfStats(MessageQueueThreadPerfStats stats, long wall, long cpu) {
    stats.wallTime = wall;
    stats.cpuTime = cpu;
  }

  public Looper getLooper() {
    return mLooper;
  }

  public String getName() {
    return mName;
  }

  public static MessageQueueThread create(
      MessageQueueThreadSpec spec, QueueThreadExceptionHandler exceptionHandler) {
    switch (spec.getThreadType()) {
      case MAIN_UI:
        return createForMainThread(spec.getName(), exceptionHandler);
      case NEW_BACKGROUND:
        return startNewBackgroundThread(spec.getName(), spec.getStackSize(), exceptionHandler);
      default:
        throw new RuntimeException("Unknown thread type: " + spec.getThreadType());
    }
  }

    /** @return a MessageQueueThread corresponding to Android's main UI thread. */
    private static MessageQueueThread createForMainThread(
      String name, QueueThreadExceptionHandler exceptionHandler) {
    Looper mainLooper = Looper.getMainLooper();
    final MessageQueueThread mqt =
        new MessageQueueThread(name, mainLooper, exceptionHandler);

    if (Looper.myLooper().getThread() == Thread.currentThread()) {
      Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
    } else {
      runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            }
          }, 0);
    }
    return mqt;
  }

  /**
   * Creates and starts a new MessageQueueThread encapsulating a new Thread with a new Looper
   * running on it. Give it a name for easier debugging and optionally a suggested stack size. When
   * this method exits, the new MessageQueueThread is ready to receive events.
   */
  private static MessageQueueThread startNewBackgroundThread(
      final String name, long stackSize, QueueThreadExceptionHandler exceptionHandler) {
    final SimpleSettableFuture<Pair<Looper, MessageQueueThreadPerfStats>> dataFuture =
        new SimpleSettableFuture<>();
    long startTimeMillis;
    Thread bgThread =
        new Thread(
            null,
            new Runnable() {
              @Override
              public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                Looper.prepare();
                MessageQueueThreadPerfStats stats = new MessageQueueThreadPerfStats();
                long wallTime = SystemClock.uptimeMillis();
                long cpuTime = SystemClock.currentThreadTimeMillis();
                assignToPerfStats(stats, wallTime, cpuTime);
                dataFuture.set(new Pair<>(Looper.myLooper(), stats));
                Looper.loop();
              }
            },
            "mqt_" + name,
            stackSize);
    bgThread.start();

    Pair<Looper, MessageQueueThreadPerfStats> pair = dataFuture.getOrThrow();
    return new MessageQueueThread(name, pair.first, exceptionHandler, pair.second);
  }

  @Nullable private static Handler sMainHandler;

  private static void runOnUiThread(Runnable runnable, long delayInMs) {
    synchronized (MessageQueueThread.class) {
      if (sMainHandler == null) {
        sMainHandler = new Handler(Looper.getMainLooper());
      }
    }
    sMainHandler.postDelayed(runnable, delayInMs);
  }
}
