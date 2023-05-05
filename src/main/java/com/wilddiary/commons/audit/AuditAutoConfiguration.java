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

package com.wilddiary.commons.audit;

import com.wilddiary.commons.audit.impl.ConsoleAuditor;
import com.wilddiary.commons.audit.impl.DefaultEvaluationContextConfigurer;
import com.wilddiary.commons.audit.impl.DefaultOriginResolver;
import com.wilddiary.commons.audit.impl.DefaultSubjectResolver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Auto configuration for audit library. */
@Configuration
@Import({AuditProcessorAspect.class, AuditManager.class})
public class AuditAutoConfiguration {

  @Autowired private ApplicationContext context;

  /**
   * Plugs-in a default implementation for Subject resolution if none is provided. Can be overridden
   * with custom implementation.
   *
   * @return Default subject resolver instance
   */
  @Bean
  @ConditionalOnMissingBean(value = SubjectResolver.class, search = SearchStrategy.CURRENT)
  public SubjectResolver subjectResolver() {
    return new DefaultSubjectResolver();
  }

  /**
   * Plugs-in a default implementation for origin resolution if none is provided. Can be overridden
   * with custom implementation.
   *
   * @return Default origin resolver instance
   */
  @Bean
  @ConditionalOnMissingBean(value = DefaultOriginResolver.class, search = SearchStrategy.CURRENT)
  public DefaultOriginResolver originResolver() {
    return new DefaultOriginResolver();
  }

  /**
   * Plugs-in a default implementation for Auditor if none is provided. Can be overridden with
   * custom implementation.
   *
   * @return Default console based auditor instance
   */
  @Bean
  @ConditionalOnMissingBean(value = Auditor.class, search = SearchStrategy.CURRENT)
  public ConsoleAuditor consoleAuditor() {
    return new ConsoleAuditor();
  }

  @Bean
  @Lazy
  @Order(Ordered.LOWEST_PRECEDENCE)
  @ConditionalOnMissingBean(value = MeterRegistry.class, search = SearchStrategy.CURRENT)
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

  /**
   * Plugs-in a default implementation for SpEL evaluation context configurer.
   *
   * @param appContext spring application context
   * @return configured SpEL evaluation context
   */
  @Bean
  @ConditionalOnMissingBean(EvaluationContextConfigurer.class)
  public EvaluationContextConfigurer evalContextConfigurer(
      ConfigurableApplicationContext appContext) {
    return new DefaultEvaluationContextConfigurer(appContext);
  }
}
