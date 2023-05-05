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

package com.wilddiary.commons.audit.impl;

import com.wilddiary.commons.audit.AuditFailureException;
import com.wilddiary.commons.audit.AuditRecord;
import com.wilddiary.commons.audit.Auditor;

/**
 * Simple auditor implementation that prints the audit records to standard output. Can be used for
 * testing purposes only. Not to be used in production.
 */
public class ConsoleAuditor implements Auditor {

  /**
   * Prints the input audit record to standard output.
   *
   * @param auditRecord audit record instance.
   * @throws AuditFailureException when cannot print the audit record.
   */
  @Override
  public void audit(AuditRecord auditRecord) throws AuditFailureException {
    System.out.println(auditRecord);
  }
}
