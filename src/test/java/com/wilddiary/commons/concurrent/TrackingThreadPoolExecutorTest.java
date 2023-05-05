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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

class TrackingThreadPoolExecutorTest {
  /**
   * Method under test: {@link TrackingThreadPoolExecutor#TrackingThreadPoolExecutor(int, int, long,
   * TimeUnit, BlockingQueue)}
   */
  @Test
  void testConstructor1() {

    // Arrange and Act
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(3, 5, 1L, TimeUnit.MINUTES, queue);

    assertEquals(3, executor.getCorePoolSize());
    assertEquals(5, executor.getMaximumPoolSize());
    assertEquals(60, executor.getKeepAliveTime(TimeUnit.SECONDS));
    assertEquals(queue, executor.getQueue());
    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());
  }

  /**
   * Method under test: {@link TrackingThreadPoolExecutor#TrackingThreadPoolExecutor(int, int, long,
   * TimeUnit, BlockingQueue, ThreadFactory)}
   */
  @Test
  void testConstructor2() {

    // Arrange and Act
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(66);
    ThreadFactory factory = new CustomizableThreadFactory("XYZ");
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(5, 10, 5L, TimeUnit.MINUTES, queue, factory);

    assertEquals(5, executor.getCorePoolSize());
    assertEquals(10, executor.getMaximumPoolSize());
    assertEquals(300, executor.getKeepAliveTime(TimeUnit.SECONDS));
    assertEquals(queue, executor.getQueue());
    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());
    assertEquals(factory, executor.getThreadFactory());
  }

  /**
   * Method under test: {@link TrackingThreadPoolExecutor#TrackingThreadPoolExecutor(int, int, long,
   * TimeUnit, BlockingQueue, RejectedExecutionHandler)}
   */
  @Test
  void testConstructor3() {
    // Arrange and Act
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(66);
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            5, 10, 5L, TimeUnit.MINUTES, queue, RejectedExecutionHandlerPolicy.DISCARD);

    assertEquals(5, executor.getCorePoolSize());
    assertEquals(10, executor.getMaximumPoolSize());
    assertEquals(300, executor.getKeepAliveTime(TimeUnit.SECONDS));
    assertEquals(queue, executor.getQueue());
    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());
    assertDoesNotThrow(
        () -> {
          Field delegate =
              executor.getRejectedExecutionHandler().getClass().getDeclaredField("delegate");
          delegate.setAccessible(true);
          assertEquals(
              RejectedExecutionHandlerPolicy.DISCARD,
              delegate.get(executor.getRejectedExecutionHandler()));
        });
  }

  /**
   * Method under test: {@link TrackingThreadPoolExecutor#TrackingThreadPoolExecutor(int, int, long,
   * TimeUnit, BlockingQueue, ThreadFactory, RejectedExecutionHandler)}
   */
  @Test
  void testConstructor4() {
    // Arrange and Act
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(66);
    ThreadFactory factory = new CustomizableThreadFactory("XYZ");
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            5, 10, 5L, TimeUnit.MINUTES, queue, factory, RejectedExecutionHandlerPolicy.DISCARD);

    assertEquals(5, executor.getCorePoolSize());
    assertEquals(10, executor.getMaximumPoolSize());
    assertEquals(300, executor.getKeepAliveTime(TimeUnit.SECONDS));
    assertEquals(queue, executor.getQueue());
    assertEquals(factory, executor.getThreadFactory());
    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());
    assertDoesNotThrow(
        () -> {
          Field delegate =
              executor.getRejectedExecutionHandler().getClass().getDeclaredField("delegate");
          delegate.setAccessible(true);
          assertEquals(
              RejectedExecutionHandlerPolicy.DISCARD,
              delegate.get(executor.getRejectedExecutionHandler()));
        });
  }

  /**
   * Method under test: {@link
   * TrackingThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler)}
   */
  @Test
  @DisplayName("Verify that RejectedExecutionHandler proxy is not proxied again")
  void testSetRejectedExecutionHandler1() {
    // Arrange and Act
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    // set the proxied rejected execution handler again
    executor.setRejectedExecutionHandler(executor.getRejectedExecutionHandler());

    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());

    assertDoesNotThrow(
        () -> {
          Field delegate =
              executor.getRejectedExecutionHandler().getClass().getDeclaredField("delegate");
          delegate.setAccessible(true);
          assertEquals(
              RejectedExecutionHandlerPolicy.DISCARD,
              delegate.get(executor.getRejectedExecutionHandler()));
        });
  }

  /**
   * Method under test: {@link
   * TrackingThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler)}
   */
  @Test
  @DisplayName(
      "Verify that a new rejected execution handler set via setter overrides the one set "
          + "in the constructor")
  void testSetRejectedExecutionHandler2() {
    // Arrange and Act
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    // set a new policy
    executor.setRejectedExecutionHandler(RejectedExecutionHandlerPolicy.CALLER_RUNS);

    assertEquals(
        "com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor.RejectedExecutionHandlerProxy",
        executor.getRejectedExecutionHandler().getClass().getCanonicalName());

    assertDoesNotThrow(
        () -> {
          Field delegate =
              executor.getRejectedExecutionHandler().getClass().getDeclaredField("delegate");
          delegate.setAccessible(true);
          assertEquals(
              RejectedExecutionHandlerPolicy.CALLER_RUNS,
              delegate.get(executor.getRejectedExecutionHandler()));
        });
  }

  /**
   * Method under test: {@link
   * TrackingThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler)}
   */
  @Test
  @DisplayName(
      "Verify that a new rejected execution handler policy set in the middle of "
          + "execution does not affect the metrics collection ")
  void testSetRejectedExecutionHandler3() {
    // Arrange and Act
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    executor.shutdown();

    // submit some tasks for existing policy
    executor.submit(() -> {});

    // set a new policy
    executor.setRejectedExecutionHandler(RejectedExecutionHandlerPolicy.CALLER_RUNS);

    // submit new tasks after policy change
    executor.submit(() -> {});

    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getSubmittedTaskCount() > 1);
    // assert metrics are wholesome
    assertEquals(2L, executor.getRejectedTaskCount());
  }

  /** Method under test: {@link TrackingThreadPoolExecutor#getSubmittedTaskCount()} */
  @Test
  void testGetSubmittedTaskCount() {
    // Arrange, Act and Assert
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    executor.submit(
        () -> {
          throw new RuntimeException();
        });
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getSubmittedTaskCount() == 1);
    assertEquals(1L, executor.getSubmittedTaskCount());
  }

  /** Method under test: {@link TrackingThreadPoolExecutor#getCompletedTaskCount()} */
  @Test
  void testGetCompletedTaskCount() {
    // Arrange, Act and Assert
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    executor.submit(() -> {});
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getCompletedTaskCount() == 1);
    assertEquals(1L, executor.getCompletedTaskCount());
  }

  /** Method under test: {@link TrackingThreadPoolExecutor#getFailedTaskCount()} */
  @Test
  void testGetFailedTaskCount() {
    // Arrange, Act and Assert
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    executor.submit(
        () -> {
          throw new RuntimeException();
        });
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getFailedTaskCount() == 1);
    assertEquals(1L, executor.getFailedTaskCount());
  }

  /** Method under test: {@link TrackingThreadPoolExecutor#getRejectedTaskCount()} */
  @Test
  void testGetRejectedTaskCount() {
    // Arrange, Act and Assert
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    executor.shutdown();
    executor.submit(() -> {});
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getRejectedTaskCount() == 1);
    assertEquals(1L, executor.getRejectedTaskCount());
  }

  /** Method under test: {@link TrackingThreadPoolExecutor#toString()} */
  @Test
  void testToString() {
    // Arrange, Act and Assert
    TrackingThreadPoolExecutor executor =
        new TrackingThreadPoolExecutor(
            3,
            3,
            1L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            RejectedExecutionHandlerPolicy.DISCARD);
    int taskCount = new Random().nextInt(5);
    for (int i = 0; i < taskCount; i++) {
      executor.submit(() -> {});
    }
    await().atMost(5, TimeUnit.SECONDS).until(() -> executor.getCompletedTaskCount() == taskCount);
    assertTrue(executor.toString().contains("completed tasks = " + taskCount));
  }
}
