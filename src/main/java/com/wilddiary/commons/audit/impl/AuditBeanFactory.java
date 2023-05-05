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

import com.wilddiary.commons.concurrent.RejectedExecutionHandlerPolicy;
import com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor;
import com.wilddiary.commons.metrics.ExecutorMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

/** Bean factory to produce beans required for audit logs. */
@Configuration
@Slf4j
public class AuditBeanFactory {

  private final int auditQueueCapacity;
  private final int asyncCorePoolSize;
  private final int asyncMaxPoolSize;
  private final RejectedExecutionHandlerPolicy rejectedExecutionHandlerPolicy;
  private static final String DEFAULT_AUDIT_EXECUTOR_METRICS_PREFIX = "wd.commons.audit.";

  /**
   * Constructor with required parameters.
   *
   * @param auditQueueCapacity - maximum queue size for audit queue.
   * @param asyncCorePoolSize - core thread pool size
   * @param asyncMaxPoolSize - maximum thread pool size
   * @param rejectedExecutionHandlerPolicy - policy for handling rejected tasks. Defaults to ABORT.
   */
  public AuditBeanFactory(
      @Value("${wd.commons.audit.queue.capacity:2147483647}") int auditQueueCapacity,
      @Value("${wd.commons.audit.thread.pool.core.size:1}") int asyncCorePoolSize,
      @Value("${wd.commons.audit.thread.pool.max.size:4}") int asyncMaxPoolSize,
      @Value("${wd.commons.audit.thread.pool.rejectedExecutionHandlerPolicy:ABORT}")
          RejectedExecutionHandlerPolicy rejectedExecutionHandlerPolicy) {
    this.auditQueueCapacity = auditQueueCapacity;
    this.asyncCorePoolSize = asyncCorePoolSize;
    this.asyncMaxPoolSize = asyncMaxPoolSize;
    this.rejectedExecutionHandlerPolicy = rejectedExecutionHandlerPolicy;
  }

  /**
   * Produces thread pool instance for async audit logging. Binds the metrics with the metrics
   * registry.
   *
   * @return executor instance.
   */
  @Bean("asyncAuditExecutor")
  public Executor asyncAuditExecutor(MeterRegistry metricsRegistry) {
    log.info(
        "Initialising async audit executor pool with params - "
            + "corePoolSize={}, maxPoolSize={}, queueCapacity={}, "
            + "rejectedExecutionHandlerPolicy={}.",
        asyncCorePoolSize,
        asyncMaxPoolSize,
        auditQueueCapacity,
        rejectedExecutionHandlerPolicy);

    ThreadPoolExecutor asyncAuditPool =
        new TrackingThreadPoolExecutor(
            asyncCorePoolSize,
            asyncMaxPoolSize,
            3L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(auditQueueCapacity),
            new CustomizableThreadFactory("AsyncAuditPool"),
            this.rejectedExecutionHandlerPolicy);

    // monitor the pool
    ExecutorMetrics metrics =
        new ExecutorMetrics(
            asyncAuditPool,
            "asyncAuditExecutor",
            DEFAULT_AUDIT_EXECUTOR_METRICS_PREFIX,
            Collections.emptyList());
    metrics.bindTo(metricsRegistry);

    return new DelegatingSecurityContextExecutor(asyncAuditPool);
  }
}
