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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/** Policy options for handling the tasks rejected by an {@link java.util.concurrent.Executor}. */
@Slf4j
public enum RejectedExecutionHandlerPolicy implements RejectedExecutionHandler {
  CALLER_RUNS(new ThreadPoolExecutor.CallerRunsPolicy()),
  ABORT(new ThreadPoolExecutor.AbortPolicy()),
  DISCARD(new ThreadPoolExecutor.DiscardPolicy()),
  DISCARD_OLDEST(new ThreadPoolExecutor.DiscardOldestPolicy());

  private final RejectedExecutionHandler delegate;

  RejectedExecutionHandlerPolicy(RejectedExecutionHandler handler) {
    this.delegate = handler;
  }

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    log.warn(
        "Task rejected. Executor State = { QueueSize={}, PoolSize={}, IsShutDown={} }",
        e.getQueue().size(),
        e.getPoolSize(),
        e.isShutdown());
    this.delegate.rejectedExecution(r, e);
  }
}
