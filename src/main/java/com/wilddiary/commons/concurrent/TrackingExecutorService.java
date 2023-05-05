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

import java.util.concurrent.ExecutorService;

/** Extends {@link ExecutorService} to track task level execution metrics. */
public interface TrackingExecutorService extends ExecutorService {

  /**
   * Provides an approximate number of tasks that failed during execution. Task that throws an
   * exception during execution is considered a failed task.
   *
   * @return count of failed tasks
   */
  long getFailedTaskCount();

  /**
   * Provides an approximate count of tasks that were submitted for execution.
   *
   * @return count of submitted tasks
   */
  long getSubmittedTaskCount();

  /**
   * Provides an approximate count of tasks that completed successfully. Tasks that execute without
   * throwing any exception are considered successful.
   *
   * @return count of completed tasks
   */
  long getCompletedTaskCount();

  /**
   * Provides an approximate count of tasks that were rejected.
   *
   * @return count of rejected tasks
   */
  long getRejectedTaskCount();
}
