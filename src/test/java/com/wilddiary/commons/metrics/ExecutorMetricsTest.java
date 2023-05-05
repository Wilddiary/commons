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

package com.wilddiary.commons.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.wilddiary.commons.concurrent.RejectedExecutionHandlerPolicy;
import com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

class ExecutorMetricsTest {

  @Test
  void testMetricsCollectionWithDefaultPrefixAndCompleteAll() {
    // Arrange
    String prefix = null;
    MeterRegistry registry = new SimpleMeterRegistry();
    TrackingThreadPoolExecutor executor =
        initExecutorPool(
            5, "Test-Executor-Pool", prefix, RejectedExecutionHandlerPolicy.DISCARD, registry);

    // Act
    int taskCount = new Random().nextInt(5) + 1;
    submitTasks(taskCount, executor, TaskAction.COMPLETE);

    // assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getCompletedTaskCount() == taskCount);
    assertMetrics(registry, prefix, taskCount, taskCount, 0, 0);
  }

  @Test
  void testMetricsCollectionWithCustomPrefixAndCompleteAll() {
    // Arrange
    String prefix = "abc.";
    MeterRegistry registry = new SimpleMeterRegistry();
    TrackingThreadPoolExecutor executor =
        initExecutorPool(
            5, "Test-Executor-Pool", prefix, RejectedExecutionHandlerPolicy.DISCARD, registry);

    // Act
    int taskCount = new Random().nextInt(5) + 1;
    submitTasks(taskCount, executor, TaskAction.COMPLETE);

    // assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getCompletedTaskCount() == taskCount);
    assertMetrics(registry, prefix, taskCount, taskCount, 0, 0);
  }

  @Test
  void testMetricsCollectionWithCustomPrefixAndRejectAll() {
    // Arrange
    String prefix = "qwerqwe.";
    MeterRegistry registry = new SimpleMeterRegistry();
    TrackingThreadPoolExecutor executor =
        initExecutorPool(
            5, "Test-Executor-Pool", prefix, RejectedExecutionHandlerPolicy.DISCARD, registry);

    // Act
    int taskCount = new Random().nextInt(5) + 1;
    submitTasks(taskCount, executor, TaskAction.REJECT);

    // assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getRejectedTaskCount() == taskCount);
    assertMetrics(registry, prefix, taskCount, 0, 0, taskCount);
  }

  @Test
  void testMetricsCollectionWithCustomPrefixAndFailAll() {
    // Arrange
    String prefix = "xyxyx.";
    MeterRegistry registry = new SimpleMeterRegistry();
    TrackingThreadPoolExecutor executor =
        initExecutorPool(
            5, "Test-Executor-Pool", prefix, RejectedExecutionHandlerPolicy.DISCARD, registry);

    // Act
    int taskCount = new Random().nextInt(5) + 1;
    submitTasks(taskCount, executor, TaskAction.FAIL);

    // assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getFailedTaskCount() == taskCount);
    assertMetrics(registry, prefix, taskCount, 0, taskCount, 0);
  }

  private enum TaskAction {
    COMPLETE,
    FAIL,
    REJECT
  }

  private TrackingThreadPoolExecutor initExecutorPool(
      int capacity,
      String poolName,
      String prefix,
      RejectedExecutionHandlerPolicy policy,
      MeterRegistry registry) {

    // init
    TrackingThreadPoolExecutor pool =
        new TrackingThreadPoolExecutor(
            1,
            2,
            3L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(capacity),
            new CustomizableThreadFactory(poolName),
            policy);

    // bind to registry
    ExecutorMetrics executorMetrics;
    if (prefix == null) {
      executorMetrics = new ExecutorMetrics(pool, poolName, new ArrayList<>());
    } else {
      executorMetrics = new ExecutorMetrics(pool, poolName, prefix, new ArrayList<>());
    }
    executorMetrics.bindTo(registry);

    return pool;
  }

  private void submitTasks(int count, ExecutorService executor, TaskAction action) {
    if (action.equals(TaskAction.REJECT)) {
      executor.shutdown();
    }

    for (int i = 0; i < count; i++) {
      switch (action) {
        case COMPLETE:
        case REJECT:
          executor.submit(() -> {});
          break;
        case FAIL:
          executor.submit(
              () -> {
                throw new RuntimeException();
              });
      }
    }
  }

  private void assertMetrics(
      MeterRegistry registry,
      String prefix,
      long submitted,
      long completed,
      long failed,
      long rejected) {

    if (prefix == null) {
      prefix = "";
    }

    String finalPrefix = prefix;
    assertDoesNotThrow(
        () -> {
          FunctionCounter rejectedTasksCounter =
              registry.get(finalPrefix + "executor.rejected").functionCounter();
          assertThat(rejectedTasksCounter.count()).isEqualTo(rejected);
        });

    assertDoesNotThrow(
        () -> {
          FunctionCounter submittedTasksCounter =
              registry.get(finalPrefix + "executor.submitted").functionCounter();
          assertThat(submittedTasksCounter.count()).isEqualTo(submitted);
        });

    assertDoesNotThrow(
        () -> {
          FunctionCounter completedTasksCounter =
              registry.get(finalPrefix + "executor.completed").functionCounter();
          assertThat(completedTasksCounter.count()).isEqualTo(completed);
        });

    assertDoesNotThrow(
        () -> {
          FunctionCounter completedTasksCounter =
              registry.get(finalPrefix + "executor.failed").functionCounter();
          assertThat(completedTasksCounter.count()).isEqualTo(failed);
        });
  }
}
