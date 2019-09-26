package io.nodekit.engine.queue;

/**
 * Interface for a class that knows how to handle an Exception thrown while executing a Runnable
 * submitted via {@link MessageQueueThread#runOnQueue}.
 */
public interface QueueThreadExceptionHandler {

  void handleException(Exception e);
}
