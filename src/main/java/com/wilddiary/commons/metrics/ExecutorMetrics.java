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

import com.wilddiary.commons.concurrent.TrackingExecutorService;
import com.wilddiary.commons.concurrent.TrackingThreadPoolExecutor;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.lang.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extends {@link io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics} for monitoring.
 * Provides additional task related metrics for the {@link TrackingThreadPoolExecutor}. Does not
 * record timings on operations executed in the {@link ExecutorService}, as this requires the
 * instance to be wrapped. Timings are provided separately by wrapping the executor service with
 * {@link io.micrometer.core.instrument.internal.TimedExecutorService}.
 *
 * <p>Supports {@link ThreadPoolExecutor} and {@link java.util.concurrent.ForkJoinPool} types of
 * {@link ExecutorService}. Some libraries may provide a wrapper type for {@link ExecutorService},
 * like {@link io.micrometer.core.instrument.internal.TimedExecutorService}. Make sure to pass the
 * underlying, unwrapped ExecutorService to this MeterBinder, if it is wrapped in another type.
 */
@Slf4j
public class ExecutorMetrics implements MeterBinder {
  private final ExecutorServiceMetrics serviceMetrics;
  private final ExecutorService executorService;
  private final Iterable<Tag> tags;
  private final String metricPrefix;
  private static final String DEFAULT_EXECUTOR_METRIC_PREFIX = "";

  /**
   * Create an {@code ExecutorMetrics} instance.
   *
   * @param executorService executor service
   * @param executorServiceName executor service name which will be used as {@literal name} tag
   * @param tags additional tags
   */
  public ExecutorMetrics(
      @Nullable ExecutorService executorService, String executorServiceName, Iterable<Tag> tags) {
    this.executorService = executorService;
    this.tags = Tags.concat(tags, "name", executorServiceName);
    this.metricPrefix = DEFAULT_EXECUTOR_METRIC_PREFIX;
    serviceMetrics = new ExecutorServiceMetrics(executorService, executorServiceName, tags);
  }

  /**
   * Create an {@code ExecutorMetrics} instance.
   *
   * @param executorService executor service
   * @param executorServiceName executor service name which will be used as {@literal name} tag
   * @param metricPrefix metrics prefix which will be used to prefix metric name
   * @param tags additional tags
   */
  public ExecutorMetrics(
      @Nullable ExecutorService executorService,
      String executorServiceName,
      String metricPrefix,
      Iterable<Tag> tags) {
    this.executorService = executorService;
    this.tags = Tags.concat(tags, "name", executorServiceName);
    this.metricPrefix = metricPrefix;
    serviceMetrics =
        new ExecutorServiceMetrics(executorService, executorServiceName, metricPrefix, tags);
  }

  /**
   * Register metrics to provide information about the state of some aspect of the application or
   * its container.
   *
   * @param meterRegistry - meter registry instance that keeps the metrics.
   */
  public void bindTo(MeterRegistry meterRegistry) {
    // delegate the binding to the backing binder
    this.serviceMetrics.bindTo(meterRegistry);

    // register additional metrics
    if (this.executorService instanceof TrackingExecutorService) {
      TrackingExecutorService tes = ((TrackingExecutorService) this.executorService);

      // register metrics if the handler supports counting of rejected tasks

      log.info("Registering additional metrics for TrackingExecutorService.");
      FunctionCounter.builder(
              metricPrefix + "executor.rejected",
              tes,
              TrackingExecutorService::getRejectedTaskCount)
          .tags(tags)
          .description("The approximate total number of tasks that have been rejected.")
          .baseUnit(BaseUnits.TASKS)
          .register(meterRegistry);

      FunctionCounter.builder(
              metricPrefix + "executor.submitted",
              tes,
              TrackingExecutorService::getSubmittedTaskCount)
          .tags(tags)
          .description("The approximate total number of tasks that have been submitted.")
          .baseUnit(BaseUnits.TASKS)
          .register(meterRegistry);

      FunctionCounter.builder(
              metricPrefix + "executor.completed",
              tes,
              TrackingExecutorService::getCompletedTaskCount)
          .tags(tags)
          .description("The approximate total number of tasks that have been completed.")
          .baseUnit(BaseUnits.TASKS)
          .register(meterRegistry);

      FunctionCounter.builder(
              metricPrefix + "executor.failed", tes, TrackingExecutorService::getFailedTaskCount)
          .tags(tags)
          .description("The approximate total number of tasks that have failed.")
          .baseUnit(BaseUnits.TASKS)
          .register(meterRegistry);
    }
  }
}
