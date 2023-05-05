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

import com.wilddiary.commons.audit.AuditRecord.AuditRecordBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepts, evaluates and processes the {@link Audit} annotations. Initiates the audit operation
 * at runtime.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditProcessorAspect {

  public static final String VAR_NAME_EXCEPTION = "exception";
  public static final String VAR_NAME_RESULT = "result";
  public static final String VAR_NAME_PREFIX_ARG = "arg";
  private final AuditManager manager;
  private final SubjectResolver subjectResolver;
  private final OriginResolver originResolver;
  private final EvaluationContextConfigurer evalContextConfigurer;
  private final ExpressionParser parser = new SpelExpressionParser();

  /**
   * Intercepts calls to methods annotated with {@link Audit} annotation. Evaluates and delegates
   * the calls to {@link AuditManager} for processing the audits.
   *
   * @param jp joinpoint instance for intercepted method.
   * @return return value of the intercepted method.
   * @throws AuditFailureException if exception are encountered during processing the audits.
   */
  @Around("@annotation(com.wilddiary.commons.audit.Audit)")
  public Object audit(ProceedingJoinPoint jp) throws Throwable {

    Method method = ((MethodSignature) jp.getSignature()).getMethod();
    Audit methodAnnotation = method.getAnnotation(Audit.class);

    // prepare SpEL evaluation context with method arguments
    Object[] args = jp.getArgs();
    EvaluationContext evalContext =
        evalContextConfigurer.configure(new StandardEvaluationContext(args));
    prepareEvaluationContext(evalContext, method, args);

    // build the audit record
    AuditRecordBuilder builder =
        AuditRecord.builder()
            .async(methodAnnotation.async())
            .context(SecurityContextHolder.getContext())
            .subject(
                methodAnnotation.subject().trim().isEmpty()
                    ? subjectResolver.resolve(SecurityContextHolder.getContext())
                    : resolveMessage(methodAnnotation.subject(), evalContext))
            .action(resolveMessage(methodAnnotation.action(), evalContext))
            .object(resolveMessage(methodAnnotation.object(), evalContext))
            .path(resolveMessage(methodAnnotation.path(), evalContext))
            .origin(
                methodAnnotation.origin().trim().isEmpty()
                    ? originResolver.resolve(SecurityContextHolder.getContext())
                    : resolveMessage(methodAnnotation.origin(), evalContext));

    // audit at entry if requested and condition is met
    audit(methodAnnotation.pre(), methodAnnotation.preCondition(), evalContext, builder, manager);

    Object result = null;
    try {
      result = jp.proceed(jp.getArgs());

      // add the return value to the eval context for resolution
      evalContext.setVariable(VAR_NAME_RESULT, result);
      // audit at exit if requested and condition is met
      audit(
          methodAnnotation.post(), methodAnnotation.postCondition(), evalContext, builder, manager);

    } catch (Throwable e) {
      log.debug("Exception occurred.", e);
      // add exception to eval context for resolution
      evalContext.setVariable(VAR_NAME_EXCEPTION, e);
      // audit on failure if requested and condition is met
      audit(
          methodAnnotation.failure(),
          methodAnnotation.failureCondition(),
          evalContext,
          builder,
          manager);
      throw e;
    }
    return result;
  }

  /**
   * Perform audit based on the input audit expression if the audit condition evaluates is
   * satisfied.
   *
   * @param auditExpression A SpEL expression that would be evaluated to form the audit text. A null
   *     or empty expression would prevent the audit.
   * @param auditCondition A SpEL expression evaluating to a boolean value. The audit would be
   *     performed only if this condition is satisfied. A null or empty condition evaluates to true.
   * @param evalContext EvaluationContext instance that would be used to evaluate the SpEL
   *     expressions.
   * @param builder A partially built AuditRecordBuilder instance that would be populated with the
   *     evaluated audit text and used to build the final audit record.
   * @param manager The audit manager instance that would handle the audit records
   * @throws AuditFailureException thrown when the audit fails
   */
  private void audit(
      String auditExpression,
      String auditCondition,
      EvaluationContext evalContext,
      AuditRecord.AuditRecordBuilder builder,
      AuditManager manager)
      throws AuditFailureException {
    if (Objects.isNull(auditExpression) || auditExpression.isEmpty()) {
      return;
    }

    // record if condition is specified and it satisfies
    if (Objects.isNull(auditCondition)
        || auditCondition.trim().isEmpty()
        || resolveCondition(auditCondition, evalContext)) {
      AuditRecord auditRecord =
          builder
              .message(resolveMessage(auditExpression, evalContext))
              .timestamp(System.currentTimeMillis())
              .build();
      log.debug("Audit record - {}", auditRecord.toString());
      manager.handleRecord(auditRecord);
    }
  }

  /**
   * Prepares the evaluation context for evaluation by seetings up required variables.
   *
   * @param evalContext Evaluation Context instance for the intercepted method.
   * @param method Method which was intercepted.
   * @param args arguments to the intercepted method.
   */
  private void prepareEvaluationContext(
      EvaluationContext evalContext, Method method, Object[] args) {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      // set the params with their names in the context so that they are accessible by their formal
      // name
      evalContext.setVariable(parameters[i].getName(), args[i]);
      if (!parameters[i].getName().startsWith(VAR_NAME_PREFIX_ARG)) {
        // sometimes formal names are not visible if the debug information is stripped and hence we
        // add them with arg0, arg1 and so on.
        evalContext.setVariable(VAR_NAME_PREFIX_ARG + i, args[i]);
      }
    }
  }

  /**
   * Resolves the input SpEL expression using the evaluation context and return the result as an
   * instance of the input type.
   *
   * @param expressionString - SpEL expression to resolve
   * @param evalContext - SpEL evaluation context
   * @param clazz - class of output type
   * @param <T> - generic return type
   * @return - result of the resolution as an instance of the input type
   */
  private <T> T resolve(String expressionString, EvaluationContext evalContext, Class<T> clazz) {
    Expression expression = this.parser.parseExpression(expressionString);
    return expression.getValue(evalContext, clazz);
  }

  /**
   * Resolves the input SpEL expression using the evaluation context and return the result as a
   * string.
   *
   * @param expressionString - SpEL expression to resolve
   * @param evalContext - SpEL evaluation context
   * @return - resolved expression as a string
   */
  private String resolveMessage(String expressionString, EvaluationContext evalContext) {
    if (expressionString == null || evalContext == null || expressionString.trim().isBlank()) {
      return expressionString;
    }
    return resolve(expressionString, evalContext, String.class);
  }

  /**
   * Resolves the input SpEL conditional expression using the evaluation context and return the
   * result as boolean.
   *
   * @param expressionString - SpEL expression to resolve
   * @param evalContext - SpEL evaluation context
   * @return - resolved expression as a boolean
   */
  private boolean resolveCondition(String expressionString, EvaluationContext evalContext) {
    if (expressionString == null || evalContext == null || expressionString.trim().isBlank()) {
      return true;
    }
    return resolve(expressionString, evalContext, Boolean.class);
  }
}
