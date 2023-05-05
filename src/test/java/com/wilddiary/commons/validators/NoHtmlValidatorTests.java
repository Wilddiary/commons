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

package com.wilddiary.commons.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NoHtmlValidatorTests {

  private NoHtmlValidator validator = new NoHtmlValidator();

  @ParameterizedTest
  @MethodSource("provideTestValues")
  @DisplayName("Verify validation works")
  void testWithInvalidInput(String input, boolean expected) {
    assertEquals(validator.isValid(input, null), expected);
  }

  private static Stream<Arguments> provideTestValues() {
    return Stream.of(
        Arguments.of(null, true),
        Arguments.of("asasdasdasd", true),
        Arguments.of("asasd asdasd", true),
        Arguments.of("\"%3cscript%3ealert(document.cookie)%3c/script%3e", true),
        Arguments.of("<script>alert(123)</script>", false),
        Arguments.of("\"><script>alert(document.cookie)</script>", false),
        Arguments.of("\"><script >alert(document.cookie)</script >", false),
        Arguments.of("\"><ScRiPt>alert(document.cookie)</ScRiPt>", false),
        Arguments.of("<SCRIPT%20a=\">\"%20SRC=\"http://attacker/xss.js\"></SCRIPT>", false),
        Arguments.of("<scr<script>ipt>alert(document.cookie)</script>", false));
  }
}
