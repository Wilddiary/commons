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

import com.wilddiary.commons.audit.EvaluationContextConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Default implementation for configuring the SpEL expression context. Allows referring and
 * resolution of all application context beans in the SpEL expression.
 */
@RequiredArgsConstructor
public class DefaultEvaluationContextConfigurer implements EvaluationContextConfigurer {

  private final ConfigurableApplicationContext appContext;

  /**
   * Configures the input SpEL evaluation context with resolvers for application context beans.
   *
   * @param context SpEL evaluation context.
   * @return Configured SpEL evaluation contion
   */
  public EvaluationContext configure(EvaluationContext context) {
    if (StandardEvaluationContext.class.isAssignableFrom(context.getClass())) {
      ((StandardEvaluationContext) context)
          .setBeanResolver(new BeanFactoryResolver(appContext.getBeanFactory()));
    }
    return context;
  }
}
