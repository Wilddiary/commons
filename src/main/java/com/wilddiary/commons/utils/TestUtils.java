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

package com.wilddiary.commons.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jparams.verifier.tostring.ToStringVerifier;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.slf4j.LoggerFactory;

/**
 * Test utility to auto verify POJOs for their basic contracts. Tests all basic contracts and
 * improves code coverage.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
  private static final Validator ACCESSOR_VALIDATOR =
      ValidatorBuilder.create().with(new GetterTester()).with(new SetterTester()).build();

  /**
   * Utility method to auto verify getters and setters contracts of POJOs. Improves code coverage.
   *
   * @param clazzes POJO classes to verify
   */
  public static void verifyAccessors(final Class<?>... clazzes) {
    for (Class<?> clazz : clazzes) {
      ACCESSOR_VALIDATOR.validate(PojoClassFactory.getPojoClass(clazz));
    }
  }

  /**
   * Utility method to verify equals() and hashcode() implementation of classes. Verifies the class
   * does not violate the equals and hashcode contract.
   *
   * @param clazzes classes to verify
   */
  public static void verifyEqualsAndHashcode(final Class<?>... clazzes) {
    for (Class<?> clazz : clazzes) {
      EqualsVerifier.simple()
          .forClass(clazz)
          .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
          .verify();
    }
  }

  /**
   * Utility method to test toString() implementation of model classes. Verifies all fields of model
   * classes are included in the toString() output.
   *
   * @param clazzes classes to verify
   */
  public static void verifyToString(final Class<?>... clazzes) {
    for (Class<?> clazz : clazzes) {
      ToStringVerifier.forClass(clazz).verify();
    }
  }

  /**
   * Utility method to auto verify POJO class contracts like getters, setters, equals, hashcode and
   * toString contracts. Single method to verify the basics. Improves code coverage.
   *
   * @param clazzes POJO classes to verify.
   */
  public static void verifyBasics(final Class<?>... clazzes) {
    for (Class<?> clazz : clazzes) {
      verifyAccessors(clazz);
      verifyEqualsAndHashcode(clazz);
      verifyToString(clazz);
    }
  }

  /**
   * Returns an in-memory log appender for input class. Useful to catch log messages for unit tests.
   *
   * @param clazz input class
   * @return in-memory list appender for the input class
   */
  public static ListAppender<ILoggingEvent> getListAppenderForClass(Class<?> clazz) {
    Logger logger = (Logger) LoggerFactory.getLogger(clazz);
    ListAppender<ILoggingEvent> loggingEventListAppender = new ListAppender<>();
    loggingEventListAppender.start();
    logger.addAppender(loggingEventListAppender);
    logger.setLevel(Level.DEBUG);
    logger.setAdditive(true);
    return loggingEventListAppender;
  }
}
