/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023, Wilddiary.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.wilddiary.commons.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** Extends {@link ThreadPoolExecutor} to track granular task level execution metrics. */
public class TrackingThreadPoolExecutor extends ThreadPoolExecutor
    implements TrackingExecutorService {

  private final AtomicLong completedTaskCount = new AtomicLong();
  private final AtomicLong submittedTaskCount = new AtomicLong();
  private final AtomicLong failedTaskCount = new AtomicLong();
  private final AtomicLong rejectedTaskCount = new AtomicLong();
  private final Runnable rejectedExecutionCallback = this.rejectedTaskCount::incrementAndGet;
  private RejectedExecutionHandlerProxy rejectedExecutionHandlerProxy;

  /**
   * Creates a new {@code TrackingThreadPoolExecutor} with the given initial parameters, the
   * {@linkplain Executors#defaultThreadFactory default thread factory} and the {@linkplain
   * RejectedExecutionHandlerPolicy#ABORT default rejected execution handler}.
   *
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @throws IllegalArgumentException if one of the following holds:<br>
   *     {@code corePoolSize < 0}<br>
   *     {@code keepAliveTime < 0}<br>
   *     {@code maximumPoolSize <= 0}<br>
   *     {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException if {@code workQueue} is null
   */
  public TrackingThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    this.rejectedExecutionHandlerProxy =
        new RejectedExecutionHandlerProxy(
            RejectedExecutionHandlerPolicy.ABORT, this.rejectedExecutionCallback);
    this.setRejectedExecutionHandler(this.rejectedExecutionHandlerProxy);
  }

  /**
   * Creates a new {@code TrackingThreadPoolExecutor} with the given initial parameters and the
   * {@linkplain RejectedExecutionHandlerPolicy#ABORT default rejected execution handler}.
   *
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param threadFactory the factory to use when the executor creates a new thread
   * @throws IllegalArgumentException if one of the following holds:<br>
   *     {@code corePoolSize < 0}<br>
   *     {@code keepAliveTime < 0}<br>
   *     {@code maximumPoolSize <= 0}<br>
   *     {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException if {@code workQueue} or {@code threadFactory} is null
   */
  public TrackingThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    this.rejectedExecutionHandlerProxy =
        new RejectedExecutionHandlerProxy(
            RejectedExecutionHandlerPolicy.ABORT, this.rejectedExecutionCallback);
    this.setRejectedExecutionHandler(this.rejectedExecutionHandlerProxy);
  }

  /**
   * Creates a new {@code TrackingThreadPoolExecutor} with the given initial parameters and the
   * {@linkplain Executors#defaultThreadFactory default thread factory}.
   *
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param handler the handler to use when execution is blocked because the thread bounds and queue
   *     capacities are reached
   * @throws IllegalArgumentException if one of the following holds:<br>
   *     {@code corePoolSize < 0}<br>
   *     {@code keepAliveTime < 0}<br>
   *     {@code maximumPoolSize <= 0}<br>
   *     {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException if {@code workQueue} or {@code handler} is null
   */
  public TrackingThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    this.rejectedExecutionHandlerProxy =
        new RejectedExecutionHandlerProxy(handler, this.rejectedExecutionCallback);
    this.setRejectedExecutionHandler(this.rejectedExecutionHandlerProxy);
  }

  /**
   * Creates a new {@code TrackingThreadPoolExecutor} with the given initial parameters.
   *
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param threadFactory the factory to use when the executor creates a new thread
   * @param handler the handler to use when execution is blocked because the thread bounds and queue
   *     capacities are reached
   * @throws IllegalArgumentException if one of the following holds:<br>
   *     {@code corePoolSize < 0}<br>
   *     {@code keepAliveTime < 0}<br>
   *     {@code maximumPoolSize <= 0}<br>
   *     {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException if {@code workQueue} or {@code threadFactory} or {@code handler}
   *     is null
   */
  public TrackingThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    this.rejectedExecutionHandlerProxy =
        new RejectedExecutionHandlerProxy(handler, this.rejectedExecutionCallback);
    this.setRejectedExecutionHandler(this.rejectedExecutionHandlerProxy);
  }

  /**
   * Sets a new handler for unexecutable tasks.
   *
   * @param handler the new handler
   * @throws NullPointerException if handler is null
   * @see #getRejectedExecutionHandler
   */
  @Override
  public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
    if (handler == null) {
      this.rejectedExecutionHandlerProxy = null;
    } else {
      if (handler instanceof RejectedExecutionHandlerProxy) {
        ((RejectedExecutionHandlerProxy) handler).setCallback(this.rejectedExecutionCallback);
      } else {
        handler = new RejectedExecutionHandlerProxy(handler, this.rejectedExecutionCallback);
      }
      this.rejectedExecutionHandlerProxy = (RejectedExecutionHandlerProxy) handler;
    }
    super.setRejectedExecutionHandler(this.rejectedExecutionHandlerProxy);
  }

  @Override
  public void execute(Runnable command) {
    super.execute(command);
    submittedTaskCount.incrementAndGet();
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);

    if (t == null && r instanceof Future) {
      try {
        ((Future) r).get();
      } catch (CancellationException ce) {
        t = ce;
      } catch (ExecutionException ee) {
        t = ee.getCause();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt(); // ignore/reset
      }
    }

    if (t != null) {
      this.failedTaskCount.incrementAndGet();
    } else {
      this.completedTaskCount.incrementAndGet();
    }
  }

  @Override
  public long getCompletedTaskCount() {
    return this.completedTaskCount.longValue();
  }

  @Override
  public long getSubmittedTaskCount() {
    return this.submittedTaskCount.longValue();
  }

  @Override
  public long getFailedTaskCount() {
    return this.failedTaskCount.longValue();
  }

  @Override
  public long getRejectedTaskCount() {
    return this.rejectedTaskCount.longValue();
  }

  /**
   * Returns a string identifying this pool, as well as its state, including indications of run
   * state and estimated worker and task counts.
   *
   * @return a string identifying this pool, as well as its state
   */
  @Override
  public String toString() {
    String runState = "Running";
    if (this.isTerminating()) {
      runState = "Shutting down";
    }
    if (this.isTerminated()) {
      runState = "Terminated";
    }
    return getClass().getName()
        + "@"
        + Integer.toHexString(hashCode())
        + "["
        + runState
        + ", pool size = "
        + this.getPoolSize()
        + ", active threads = "
        + this.getActiveCount()
        + ", queued tasks = "
        + this.getQueue().size()
        + ", completed tasks = "
        + this.getCompletedTaskCount()
        + "]";
  }

  /**
   * Proxies an input {@link RejectedExecutionHandler} instance to invoke a callback function on
   * every rejected task.
   */
  @Slf4j
  @Getter
  @Setter
  static class RejectedExecutionHandlerProxy implements RejectedExecutionHandler {

    private final RejectedExecutionHandler delegate;
    private Runnable callback;

    /**
     * Parameterized constructor.
     *
     * @param delegate - input RejectedExecutionHandler instance to wrap
     * @param callback - callback function
     */
    RejectedExecutionHandlerProxy(RejectedExecutionHandler delegate, Runnable callback) {
      this.delegate = delegate;
      this.callback = callback;
    }

    /**
     * Invokes the callback on every rejected task and delegates the handling of the tasks to the
     * backing handler.
     *
     * @param r - the runnable task that was rejected.
     * @param executor - the executor that rejected the task.
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      log.warn("Task rejected by {}.", executor);
      this.callback.run();
      this.delegate.rejectedExecution(r, executor);
    }
  }
}
