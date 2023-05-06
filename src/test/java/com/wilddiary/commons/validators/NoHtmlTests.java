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
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

class NoHtmlTests {
  private static Validator validator;

  @BeforeAll
  public static void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  @DisplayName("Verify annotated fields are validated")
  void test() {
    Optional<ConstraintViolation<NoHtmlContainer>> violation =
        validator.validate(new NoHtmlContainer("<script>alert(123)</script>")).stream().findFirst();
    assertTrue(violation.isPresent());
    assertTrue(violation.get().getRootBean() instanceof NoHtmlContainer);
    assertEquals("Unsafe html content", violation.get().getMessage());
  }

  @AllArgsConstructor
  @Validated
  static class NoHtmlContainer {
    @NoHtml String payload;
  }
}
