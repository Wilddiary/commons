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

import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Encapsulates the auditor implementation and delegates the audit operation. */
@Component
@Slf4j
public class AuditManager {

  private final Auditor auditor;
  private final Executor asyncExecutor;

  /**
   * Parametrized Constructor that takes in the auditor and async audit executor.
   *
   * @param auditor - auditor instance to be used for audit delegation
   * @param asyncExecutor - executor to used for async audits
   */
  public AuditManager(Auditor auditor, @Qualifier("asyncAuditExecutor") Executor asyncExecutor) {
    this.auditor = auditor;
    this.asyncExecutor = asyncExecutor;
    log.info("Audit manager initialized with {}.", auditor.getClass().getName());
  }

  /**
   * Delegates the AuditRecord to the auditor for logging. Decides if the logging should be
   * synchronous or asynchronous.
   *
   * @param auditRecord The audit record that is to be logged.
   * @throws AuditFailureException if failure occurs for some reason.
   */
  public void handleRecord(AuditRecord auditRecord) throws AuditFailureException {
    if (auditRecord.isAsync()) { // record on separate thread
      try {
        asyncExecutor.execute(new AsyncAuditTask(auditor, auditRecord));
      } catch (Throwable t) {
        if (AuditFailureException.class.isAssignableFrom(t.getClass())) {
          throw t;
        } else {
          throw new AuditFailureException("Audit failure.", t);
        }
      }
    } else { // record on calling thread
      this.auditor.audit(auditRecord);
    }
  }

  /** A runnable task that facilitates asynchronous logging of the audit record. */
  @Slf4j
  @RequiredArgsConstructor
  static class AsyncAuditTask implements Runnable {

    private final Auditor auditor;
    private final AuditRecord auditRecord;

    @SneakyThrows
    @Override
    public void run() {
      this.auditor.audit(auditRecord);
    }
  }
}
