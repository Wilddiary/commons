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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

/**
 * Validate a rich text value provided by the user to ensure that it contains no malicious code,
 * such as embedded &lt;script&gt; elements.
 */
@Documented
@Constraint(validatedBy = {NoHtmlValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface NoHtml {
  /** Message on violation. */
  String message() default "Unsafe html content";

  /** Allows the specification of validation groups, to which this constraint belongs. */
  Class<?>[] groups() default {};

  /**
   * Payload that can be used by clients of the Bean Validation API to assign custom payload objects
   * to a constraint. This attribute is not used by the API itself.
   */
  Class<? extends Payload>[] payload() default {};
}
