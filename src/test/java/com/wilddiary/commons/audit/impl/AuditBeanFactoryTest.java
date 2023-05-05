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

package com.wilddiary.commons.audit.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.wilddiary.commons.concurrent.RejectedExecutionHandlerPolicy;
import com.wilddiary.commons.concurrent.TrackingExecutorService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.test.util.ReflectionTestUtils;

class AuditBeanFactoryTest {

  @Test
  void testAsyncExecutorBeanCreation() {
    Random randomizer = new Random();
    int capacity = 1 + randomizer.nextInt(100);
    int coreSize = 1 + randomizer.nextInt(50);
    int maxSize = coreSize + 2;
    AuditBeanFactory abf =
        new AuditBeanFactory(
            capacity, coreSize, maxSize, RejectedExecutionHandlerPolicy.CALLER_RUNS);
    DelegatingSecurityContextExecutor delegatingExecutor =
        (DelegatingSecurityContextExecutor) abf.asyncAuditExecutor(new SimpleMeterRegistry());

    ThreadPoolExecutor executor =
        (ThreadPoolExecutor) ReflectionTestUtils.getField(delegatingExecutor, "delegate");

    // verify that config params are set correctly
    assertNotNull(executor);
    assertEquals(capacity, executor.getQueue().remainingCapacity());
    assertEquals(coreSize, executor.getCorePoolSize());
    assertEquals(maxSize, executor.getMaximumPoolSize());
    // verify that metrics collection is in place
    assertInstanceOf(TrackingExecutorService.class, executor);

    // verify that the policy is set correctly
    RejectedExecutionHandler delegatedRejectedExecutionHandler =
        (RejectedExecutionHandler)
            ReflectionTestUtils.getField(executor.getRejectedExecutionHandler(), "delegate");
    assertNotNull(delegatedRejectedExecutionHandler);
    assertEquals(RejectedExecutionHandlerPolicy.CALLER_RUNS, delegatedRejectedExecutionHandler);
  }
}
