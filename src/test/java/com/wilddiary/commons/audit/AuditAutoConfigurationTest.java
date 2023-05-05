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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.wilddiary.commons.audit.impl.DefaultEvaluationContextConfigurer;
import com.wilddiary.commons.audit.impl.DefaultSubjectResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

class AuditAutoConfigurationTest {

  @Test
  void subjectResolver() {
    AuditAutoConfiguration aac = new AuditAutoConfiguration();
    SubjectResolver subjectResolver = aac.subjectResolver();
    assertTrue(subjectResolver != null && subjectResolver instanceof DefaultSubjectResolver);
  }

  @Test
  void evalContextConfigurer() {
    AuditAutoConfiguration aac = new AuditAutoConfiguration();
    ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
    when(context.getBeanFactory()).thenReturn(new DefaultListableBeanFactory());

    EvaluationContextConfigurer evaluationContextConfigurer = aac.evalContextConfigurer(context);
    assertTrue(
        evaluationContextConfigurer != null
            && evaluationContextConfigurer instanceof DefaultEvaluationContextConfigurer);
    StandardEvaluationContext unconfiguredEvaluationContext = new StandardEvaluationContext();
    assertNull(unconfiguredEvaluationContext.getBeanResolver());
    // ensure eval context is being set with the bean resolver
    EvaluationContext configuredEvalContext =
        evaluationContextConfigurer.configure(unconfiguredEvaluationContext);
    assertNotNull(configuredEvalContext.getBeanResolver());
  }
}
