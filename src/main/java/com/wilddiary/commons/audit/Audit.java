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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Enables public methods of a class to be audited. Private methods cannot be audited. The audit
 * messages can be valid SpEL expressions that are evaluated and resolved from the application
 * context and the method parameters. Method parameters and the application beans can be referenced
 * to construct the audit messages. It should be noted that all string literal in SpEL should be
 * enclosed between single quotes. The method arguments can be referenced using #arg0, #arg1, ...
 * and so on in SpEL expressions. The return value of the methods can be referenced using #result
 * for methods that return values. The exception thrown from the method can be referenced using
 * #exception in SpEL expressions in any of the annotation values. Refer the SpEL language reference
 * for constructing valid SpEL expressions.<br>
 * <br>
 * Below are some examples to demonstrate the usage of the {@code @Audit} annotation. <br>
 * <br>
 * <b>Example #1:</b> <br>
 * <br>
 *
 * <p>Audit before the operation is initiated. Method parameters could be used to construct the
 * audit message. Sometimes the release builds strip the debug information from the compiled
 * bytecode wherein the resolution of parameter references cannot be done. In such cases it is safer
 * to reference them as <b>#args[0]</b>, <b>#args[1]</b> and so on. <br>
 * <br>
 *
 * <pre>{@code
 * @Audit(pre = "'Initiating operation with parameters - ' + #a.toUpperCase() + ',' + #b")
 * public int createResource(String a, Integer b) {
 *   // business logic goes here
 *   return 201;
 * }
 * }</pre>
 *
 * <br>
 * <b>Example #2:</b> <br>
 * <br>
 * The below example shows referencing an application bean property to construct the audit message.
 * The bean called as <i>customer</i> with property name <i>name</i> is referenced below. The audit
 * message would be logged after the operation completes successfully. <br>
 * <br>
 *
 * <pre>{@code
 * @Audit(post = "'Privileged operation initiated by customer ' + @customer.name")
 * public String updateRecord(String a) {
 *   // business logic goes here
 *   return "record updated";
 * }
 *
 * }</pre>
 *
 * <br>
 * <b>Example #3:</b> <br>
 * <br>
 * The example below shows use of audit logging before, after and on failure of the operation. On
 * successful completion of the method the audit would be done only if the postCondition is
 * satisfied. <br>
 * <br>
 *
 * <pre>{@code
 * @Audit( pre = "'Initiating privileged operation'",
 *         failure = "'Privileged operation failed'",
 *         post = "'Privileged operation completed successfully'",
 *         postCondition = "#result > 0")
 * public int updateResource(String a) {
 *   // business logic goes here
 *   return 200;
 * }
 * }</pre>
 *
 * <br>
 * <b>References:</b>
 *
 * <ul>
 *   <li><a href="https://nussknacker.io/documentation/docs/scenarios_authoring/Spel/">SpEL
 *       CheatSheet</a>
 *   <li><a
 *       href="https://docs.spring.io/spring-framework/docs/3.0.5.RELEASE/reference/expressions.html">Spring
 *       Expression Language (SpEL)</a>
 * </ul>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface Audit {

  /**
   * Indicates if this audit message needs to be recorded asynchronously.
   *
   * @return true if the audit is asynchronous. Default is false.
   */
  boolean async() default false;

  /**
   * Represents the audit message to be recorded before the operation is initiated. The message can
   * be a valid SpEL expression.
   *
   * @return resolved audit message.
   */
  String pre() default "";

  /**
   * SpEL conditional expression that decides if the before audit should be executed. The resolved
   * condition should result in a boolean value.
   *
   * @return - resolved expression as a boolean value
   */
  String preCondition() default "";

  /**
   * Represents the audit message to be recorded after the operation is successfully completed. The
   * operation is deemed successful if completes without throwing any exception.
   *
   * @return resolved audit message.
   */
  String post() default "";

  /**
   * SpEL conditional expression that decides if the after audit should be executed. The resolved
   * condition should result in a boolean value.
   *
   * @return - resolved expression as a boolean value
   */
  String postCondition() default "";

  /**
   * Represents the audit message to be recorded on failure of the audited operation. The operation
   * is deemed a failure if it throws an exception.
   *
   * @return resolved audit message.
   */
  String failure() default "";

  /**
   * SpEL conditional expression that decides if the failure audit should be executed. The resolved
   * condition should result in a boolean value.
   *
   * @return - resolved expression as a boolean value
   */
  String failureCondition() default "";

  /**
   * Represents the identity of initiator of the operation. Usually, user/principal. Represents the
   * WHO of the WHO-WHAT-WHICH-WHERE-WHEN security incident record.
   *
   * @return the identity of the entity initiating the operation.
   */
  String subject() default "";

  /**
   * Presents the target of the operation. In a typical CRUD operation, indicates the resource on
   * which the operation was done. Represents the WHICH of the WHO-WHAT-WHICH-WHERE-WHEN security
   * incident record.
   *
   * @return the resource name.
   */
  String object() default "";

  /**
   * Indicates the type of operation requested. In a typical CRUD operation, indicates the action
   * taken on the resource. Represents the WHAT of the WHO-WHAT-WHICH-WHERE-WHEN security incident
   * record.
   *
   * @return operation done on a resource.
   */
  String action() default "";

  /**
   * Information on the origin of the request. Usually maps to the client IP address. Represents the
   * WHERE of the WHO-WHAT-WHICH-WHERE-WHEN security incident record.
   *
   * @return the origin of the request.
   */
  String origin() default "";

  /**
   * Information on the unique identifier or URI of the object. Usually maps to the URI of the
   * object.
   *
   * @return the unique identifier or URI of the object.
   */
  String path() default "";
}
