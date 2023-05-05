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
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@ExtendWith(MockitoExtension.class)
class DefaultEvaluationContextConfigurerTest {
  @Mock ConfigurableApplicationContext appContext;

  @Mock ConfigurableListableBeanFactory clbf;

  @Test
  @DisplayName("Verify the standard evaluation context is configured correctly.")
  void testStandardEvaluationContextConfiguration() {
    StandardEvaluationContext sec = new StandardEvaluationContext();
    when(appContext.getBeanFactory()).thenReturn(this.clbf);
    assertNull(sec.getBeanResolver());

    final DefaultEvaluationContextConfigurer decc =
        new DefaultEvaluationContextConfigurer(this.appContext);
    decc.configure(sec);
    // assert the eval context is configured.
    assertNotNull(sec.getBeanResolver());
  }

  @Test
  @DisplayName("Verify non-standard evaluation context remain unchanged.")
  void testNonStandardEvaluationContextConfiguration() {
    SimpleEvaluationContext dec = SimpleEvaluationContext.forReadWriteDataBinding().build();
    assertNull(dec.getBeanResolver());

    final DefaultEvaluationContextConfigurer decc =
        new DefaultEvaluationContextConfigurer(this.appContext);
    decc.configure(dec);
    // assert the eval context is unchanged.
    assertNull(dec.getBeanResolver());
  }
}
