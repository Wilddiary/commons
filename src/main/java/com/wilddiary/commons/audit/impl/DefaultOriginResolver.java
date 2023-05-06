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

import com.wilddiary.commons.audit.OriginResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * Default implementation for resolving the origin (client IP address). Resolves to the IP address
 * from the security context authentication instance.
 */
@Slf4j
public class DefaultOriginResolver implements OriginResolver {

  public static final String UNKNOWN_ORIGIN = "Unknown";

  @Override
  public String resolve(SecurityContext context) {

    if (context != null
        && context.getAuthentication() != null
        && context.getAuthentication().getDetails() != null
        && context.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
      String remoteAddress = details.getRemoteAddress();
      log.debug("Authentication type - {}", context.getAuthentication().getClass().getName());
      log.debug("Resolved origin is - {}", remoteAddress);
      return remoteAddress;
    }
    log.debug("Failed to resolve origin for sc - {}", context);
    return UNKNOWN_ORIGIN;
  }
}
