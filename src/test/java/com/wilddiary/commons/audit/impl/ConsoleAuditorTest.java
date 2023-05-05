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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wilddiary.commons.audit.AuditFailureException;
import com.wilddiary.commons.audit.AuditRecord;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleAuditorTest {

  @BeforeEach
  void setUp() {}

  @Test
  void record() throws AuditFailureException, JsonProcessingException, JSONException {
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    AuditRecord record =
        AuditRecord.builder()
            .timestamp(System.currentTimeMillis())
            .origin("10.21.33.11")
            .action("read")
            .subject("user@xyz.com")
            .object("ProtectedResource")
            .message("User <user@xyz.com accessed the ProtectedResource.")
            .path("/protected-resource")
            .build();

    new ConsoleAuditor().audit(record);
    assertEquals(outContent.toString(), record.toString() + "\n");
  }
}
