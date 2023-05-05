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

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestUtilsTest {

  @Test
  void verifyAccessorsContractValidation() {
    Assertions.assertThrows(
        AssertionError.class,
        () -> TestUtils.verifyAccessors(TestStudentClassWithViolatedAccessorsContract.class));

    Assertions.assertDoesNotThrow(
        () ->
            TestUtils.verifyAccessors(
                TestStudentClassWithGoodEqualsAndHashCodeAndToStringImpl.class));
  }

  @Test
  void verifyEqualsAndHashcodeContractValidation() {
    Assertions.assertDoesNotThrow(
        () ->
            TestUtils.verifyEqualsAndHashcode(
                TestStudentClassWithGoodEqualsAndHashCodeAndToStringImpl.class));

    Assertions.assertThrows(
        AssertionError.class,
        () ->
            TestUtils.verifyEqualsAndHashcode(
                TestStudentClassWithBadEqualsAndHashCodeAndToStringImpl.class));
  }

  @Test
  void verifyToStringContractValidation() {
    Assertions.assertDoesNotThrow(
        () ->
            TestUtils.verifyToString(
                TestStudentClassWithGoodEqualsAndHashCodeAndToStringImpl.class));

    Assertions.assertThrows(
        AssertionError.class,
        () ->
            TestUtils.verifyToString(
                TestStudentClassWithBadEqualsAndHashCodeAndToStringImpl.class));
  }

  @Test
  void verifyBasicsValidation() {
    Assertions.assertDoesNotThrow(
        () ->
            TestUtils.verifyBasics(TestStudentClassWithGoodEqualsAndHashCodeAndToStringImpl.class));

    Assertions.assertThrows(
        AssertionError.class,
        () -> TestUtils.verifyToString(TestStudentClassWithViolatedAccessorsContract.class));

    Assertions.assertThrows(
        AssertionError.class,
        () ->
            TestUtils.verifyToString(
                TestStudentClassWithBadEqualsAndHashCodeAndToStringImpl.class));
  }

  @Test
  void testGetListAppender() {
    // generate random messages and log them and verify the appender returns the same messages in
    // the same order.
    ListAppender<ILoggingEvent> appender = TestUtils.getListAppenderForClass(LogPrinter.class);
    LogPrinter printer = new LogPrinter();
    Collection<String> randomMessages = new ArrayList<>();
    int count = new Random().nextInt(5) + 1;
    for (int i = 0; i < count; i++) {
      randomMessages.add(UUID.randomUUID().toString());
    }
    printer.printLogs(randomMessages);
    Collection<String> logs =
        appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.toList());
    assertEquals(randomMessages, logs);
  }

  @Getter
  @Setter
  public static class TestStudentClassWithBadEqualsAndHashCodeAndToStringImpl {
    private int id;
    private String name;

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public String toString() {
      return "TestStudentClassWithBadEqualsAndHashCodeAndToStringImpl{" + "id=" + id + '}';
    }
  }

  @ToString
  @EqualsAndHashCode
  @Getter
  @Setter
  public static class TestStudentClassWithGoodEqualsAndHashCodeAndToStringImpl {
    private int id;
    private String name;
  }

  @Getter
  @Setter
  public static class TestStudentClassWithViolatedAccessorsContract {
    private int id;
    private String name;

    public void setName(String name) {
      this.name = name.toLowerCase() + " - ABC";
    }
  }

  @Slf4j
  static class LogPrinter {
    public void printLogs(Collection<String> logMessages) {
      for (String message : logMessages) {
        log.info(message);
      }
    }
  }
}
