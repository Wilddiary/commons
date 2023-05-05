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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditManagerTest {

  @Test
  @DisplayName("Should delegate audits to auditor ")
  void testDelegationToAuditor() throws AuditFailureException {
    AuditRecord auditRecord = Mockito.mock(AuditRecord.class);
    Auditor auditor = Mockito.mock(Auditor.class);
    Executor executor = Mockito.mock(ThreadPoolExecutor.class);
    AuditManager am = new AuditManager(auditor, executor);
    am.handleRecord(auditRecord);
    verify(auditor, times(1)).audit(auditRecord);
  }

  @Test
  @DisplayName("Should execute asynchronous audits on separate thread")
  void testAsyncAuditDelegation() throws AuditFailureException {
    Executor executor = mock(Executor.class);
    AuditManager am = new AuditManager(mock(Auditor.class), executor);
    AuditRecord auditRecord = mock(AuditRecord.class);
    when(auditRecord.isAsync()).thenReturn(true);
    am.handleRecord(auditRecord);
    verify(executor, times(1)).execute(any(AuditManager.AsyncAuditTask.class));
  }

  @Test
  @DisplayName("Should not execute synchronous audits on separate thread")
  void testSyncAuditDelegation() throws AuditFailureException {
    Executor executor = mock(Executor.class);
    AuditManager am = new AuditManager(mock(Auditor.class), executor);
    AuditRecord auditRecord = mock(AuditRecord.class);
    when(auditRecord.isAsync()).thenReturn(false);
    am.handleRecord(auditRecord);
    verify(executor, times(0)).execute(any(AuditManager.AsyncAuditTask.class));
  }

  @Test
  void testExceptionFromDelegateIsPropogated() throws AuditFailureException {
    AuditRecord auditRecord = Mockito.mock(AuditRecord.class);
    Auditor auditor = Mockito.mock(Auditor.class);
    Executor executor = Mockito.mock(ThreadPoolExecutor.class);
    AuditManager am = new AuditManager(auditor, executor);

    // verify exception propagation and custom exception class as well

    // Blank exception
    doThrow(new AuditFailureException()).when(auditor).audit(any(AuditRecord.class));

    AuditFailureException failureException =
        assertThrows(AuditFailureException.class, () -> am.handleRecord(auditRecord));
    assertNull(failureException.getMessage());

    // Exception with message
    doThrow(new AuditFailureException("ABC")).when(auditor).audit(any(AuditRecord.class));

    failureException =
        assertThrows(AuditFailureException.class, () -> am.handleRecord(auditRecord));
    assertEquals("ABC", failureException.getMessage());

    // Exception with message and cause
    doThrow(new AuditFailureException("XYZ", new NullPointerException()))
        .when(auditor)
        .audit(any(AuditRecord.class));

    failureException =
        assertThrows(AuditFailureException.class, () -> am.handleRecord(auditRecord));
    assertEquals("XYZ", failureException.getMessage());
    assertInstanceOf(NullPointerException.class, failureException.getCause());
  }
}
