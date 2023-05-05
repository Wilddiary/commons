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

import com.wilddiary.commons.audit.impl.DefaultEvaluationContextConfigurer;
import com.wilddiary.commons.audit.impl.DefaultOriginResolver;
import com.wilddiary.commons.audit.impl.DefaultSubjectResolver;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {AuditProcessorAspectTest.class})
class AuditProcessorAspectTest {
  @Autowired ConfigurableApplicationContext ctx;

  private AuditProcessorAspectTest proxy;
  private Queue<AuditRecord> auditQueue;
  private ByteArrayOutputStream consoleOutput;

  @RequiredArgsConstructor
  public static class ListAuditor implements Auditor {

    private final Queue<AuditRecord> queue;

    @Override
    public void audit(AuditRecord auditRecord) throws AuditFailureException {
      this.queue.add(auditRecord);
    }
  }

  @BeforeEach
  void setup() {
    auditQueue = new LinkedList<>();
    AspectJProxyFactory factory = new AspectJProxyFactory(new AuditProcessorAspectTest());
    factory.addAspect(
        new AuditProcessorAspect(
            new AuditManager(new ListAuditor(auditQueue), Executors.newFixedThreadPool(1)),
            new DefaultSubjectResolver(),
            new DefaultOriginResolver(),
            new DefaultEvaluationContextConfigurer(ctx)));
    this.proxy = factory.getProxy();

    consoleOutput = new ByteArrayOutputStream();
    System.setOut(new PrintStream(consoleOutput));
  }

  @Audit(
      pre = "'Audit message before invocation'",
      action = "'Update'",
      origin = "'Web'",
      object = "'DB'")
  public void m1() {
    System.out.println("m1 is called");
  }

  @Test
  void auditBefore() throws Throwable {
    proxy.m1();

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message before invocation", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Update", auditRecord.getAction()),
        () -> assertEquals("Web", auditRecord.getOrigin()),
        () -> assertEquals("DB", auditRecord.getObject()),
        () -> assertEquals("m1 is called", consoleOutput.toString().trim()));
  }

  @Audit(
      post = "'Audit message after invocation'",
      action = "'Create'",
      origin = "'local'",
      object = "'Cache'")
  public void m2() {
    System.out.println("m2 is called");
  }

  @Test
  void auditAfter() throws Throwable {
    proxy.m2();
    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message after invocation", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Create", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Cache", auditRecord.getObject()),
        () -> assertEquals("m2 is called", consoleOutput.toString().trim()));
  }

  @Audit(
      failure = "'Audit message after failure'",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m3_1() {
    System.out.println("m3_1 is called");
    throw new RuntimeException("Simulating failure");
  }

  @Test
  void testAuditFailureOnFailure() {
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              proxy.m3_1();
            });
    assertEquals("Simulating failure", exception.getMessage());

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message after failure", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m3_1 is called")));
  }

  @Audit(
      failure = "'Audit message after failure'",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m3_2() {
    System.out.println("m3_2 is called");
    // Operation completes without failure. Failure audit should not be triggered.
  }

  @Test
  void testAuditFailureWhenNoFailure() throws Throwable {
    proxy.m3_2();

    assertThrows(
        NoSuchElementException.class,
        () -> {
          this.auditQueue.remove();
        });
    assertAll(() -> assertTrue(consoleOutput.toString().contains("m3_2 is called")));
  }

  @Audit(pre = "''", action = "'Read'", origin = "'local'", object = "'Configuration'")
  public void m4() {
    System.out.println("m4 is called");
  }

  @Test
  void testAuditBeforeWithEmptyMessage() throws Throwable {
    proxy.m4();

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m4 is called")));
  }

  @Audit(action = "'Read'", origin = "'local'", object = "'Configuration'")
  public void m5() {
    System.out.println("m5 is called");
  }

  @Test
  void testAuditWithNoAnchor() throws Throwable {
    proxy.m5();

    assertThrows(
        NoSuchElementException.class,
        () -> {
          this.auditQueue.remove();
        });
    assertAll(() -> assertTrue(consoleOutput.toString().contains("m5 is called")));
  }

  @Audit(
      pre = "'Audit message before invocation. Inputs are - ' + #a + ' and ' + #b",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m6(String a, int b) {
    System.out.println("m6 is called");
  }

  @Test
  @Disabled(
      "Disabled due to intermittent failures. The formal argument names may or may not be available at runtime "
          + "depending on the availability of the debug information in the compiled classes. Release builds are usually "
          + "stripped of debug information.")
  void testAuditBeforeWithInputResolutionByNameInMessage() throws Throwable {
    proxy.m6("ABC", 10);

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () ->
            assertEquals(
                "Audit message before invocation. Inputs are - ABC and 10",
                auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m6 is called")));
  }

  @Audit(
      pre = "'Audit message before invocation. Inputs are - ' + #arg0 + ' and ' + #arg1",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m7(String a, int b) {
    System.out.println("m7 is called");
  }

  @Test
  void testAuditBeforeWithInputResolutionByArgInMessage() throws Throwable {
    proxy.m7("ABC", 10);

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () ->
            assertEquals(
                "Audit message before invocation. Inputs are - ABC and 10",
                auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m7 is called")));
  }

  @Audit(
      pre = "'Audit message before invocation. Inputs are - ' + #arg0 + ' and ' + #arg1",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m8(String arg0, int arg1) {
    System.out.println("m8 is called");
  }

  @Test
  void testAuditBeforeWithInputNameStartsWithArgAndReferencedAsArgInMessage() throws Throwable {
    proxy.m8("ABC", 10);

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () ->
            assertEquals(
                "Audit message before invocation. Inputs are - ABC and 10",
                auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m8 is called")));
  }

  @Audit(failure = "''", action = "'Read'", origin = "'local'", object = "'Configuration'")
  public void m9() {
    System.out.println("m9 is called");
    throw new RuntimeException("Simulating failure");
  }

  @Test
  void testAuditFailureWhenFailureWithEmptyMessage() throws Throwable {
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              proxy.m9();
            });
    assertEquals("Simulating failure", exception.getMessage());

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m9 is called")));
  }

  @Audit(
      post = "'Audit message after invocation. Return value is - ' + #result",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public int m10() {
    System.out.println("m10 is called");
    return 100;
  }

  @Test
  void testAuditAfterWithReturnValueReferencedInMessage() throws Throwable {
    proxy.m10();

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () ->
            assertEquals(
                "Audit message after invocation. Return value is - 100", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m10 is called")));
  }

  @Audit(
      failure = "'Audit message on failure invocation. Exception is - ' + #exception",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m11() {
    System.out.println("m11 is called");
    throw new RuntimeException("Simulated exception");
  }

  @Test
  void testAuditFailureWithExceptionReferencedInMessage() throws Throwable {
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              proxy.m11();
            });
    assertEquals("Simulated exception", exception.getMessage());

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () ->
            assertEquals(
                "Audit message on failure invocation. Exception is - java.lang.RuntimeException: Simulated exception",
                auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m11 is called")));
  }

  @Audit(
      pre = "'Audit message before invocation'",
      preCondition = "2 == 2",
      action = "'Read'",
      origin = "'local'",
      object = "'Configuration'")
  public void m12() {
    System.out.println("m12 is called");
  }

  @Test
  void testAuditBeforeWithSimpleTrueCondition() throws Throwable {
    proxy.m12();

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message before invocation", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("local", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m12 is called")));
  }

  @Audit(pre = "'Audit message before invocation'", preCondition = "#arg0 le 100")
  public void m13(int arg0) {
    System.out.println("m13 is called");
  }

  @Test
  void testAuditBeforeWithTrueConditionUsingArgument() throws Throwable {
    proxy.m13(10);

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message before invocation", auditRecord.getMessage()),
        () -> assertTrue(consoleOutput.toString().contains("m13 is called")));
  }

  @Test
  void testAuditBeforeWithFalseConditionUsingArgument() throws Throwable {
    proxy.m13(500);

    assertThrows(
        NoSuchElementException.class,
        () -> {
          this.auditQueue.remove();
        });
    assertAll(() -> assertTrue(consoleOutput.toString().contains("m13 is called")));
  }

  @Audit(post = "'Audit message after invocation'", postCondition = "#arg0 le 100")
  public void m14(int arg0) {
    System.out.println("m14 is called");
  }

  @Test
  void testAuditAfterWithTrueConditionUsingArgument() throws Throwable {
    proxy.m14(10);

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message after invocation", auditRecord.getMessage()),
        () -> assertTrue(consoleOutput.toString().contains("m14 is called")));
  }

  @Test
  void testAuditAfterWithFalseConditionUsingArgument() throws Throwable {
    proxy.m14(500);

    assertThrows(
        NoSuchElementException.class,
        () -> {
          this.auditQueue.remove();
        });
    assertAll(() -> assertTrue(consoleOutput.toString().contains("m14 is called")));
  }

  @Audit(
      failure = "'Audit message on failure'",
      failureCondition = "#exception instanceof T(java.lang.NullPointerException) && #arg0 < 100 ")
  public int m15(int arg0) {
    System.out.println("m15 is called");
    throw new NullPointerException("Just throwing");
  }

  @Test
  void testAuditOnFailureWithTrueConditionUsingExceptionAndArgument() throws Throwable {
    assertThrows(
        NullPointerException.class,
        () -> {
          proxy.m15(10);
        });

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message on failure", auditRecord.getMessage()),
        () -> assertTrue(consoleOutput.toString().contains("m15 is called")));
  }

  @Test
  void testAuditOnFailureWithFalseConditionUsingExceptionAndArgument() throws Throwable {
    assertThrows(
        NullPointerException.class,
        () -> {
          proxy.m15(500);
        });

    assertThrows(
        NoSuchElementException.class,
        () -> {
          this.auditQueue.remove();
        });
    assertAll(() -> assertTrue(consoleOutput.toString().contains("m15 is called")));
  }

  @Audit(
      post = "'Audit message before invocation'",
      postCondition = "2 == 2",
      action = "'Read'",
      object = "'Configuration'")
  public void m16() {
    System.out.println("m16 is called");
  }

  @Test
  void testOriginResolverIsInvokedWhenOriginExpressionIsNotDefined() throws Throwable {
    proxy.m16();

    AuditRecord auditRecord = this.auditQueue.remove();
    assertNotNull(auditRecord);
    assertAll(
        () -> assertEquals("Audit message before invocation", auditRecord.getMessage()),
        () -> assertEquals("Unknown", auditRecord.getSubject()),
        () -> assertEquals("Read", auditRecord.getAction()),
        () -> assertEquals("Unknown", auditRecord.getOrigin()),
        () -> assertEquals("Configuration", auditRecord.getObject()),
        () -> assertTrue(consoleOutput.toString().contains("m16 is called")));
  }
}
