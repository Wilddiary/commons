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

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

class DefaultSubjectResolverTest {

  @Test
  void testResolveForMissingSecurityContext() {
    DefaultSubjectResolver sr = new DefaultSubjectResolver();
    assertEquals(DefaultSubjectResolver.UNKNOWN_SUBJECT, sr.resolve(null));
  }

  @Test
  void testResolveForSecurityContextWithoutAuthentication() {
    SecurityContext securityContext = getSecurityContextWithoutAuthentication();
    DefaultSubjectResolver sr = new DefaultSubjectResolver();
    assertEquals(DefaultSubjectResolver.UNKNOWN_SUBJECT, sr.resolve(securityContext));
  }

  @Test
  void testResolveForSecurityContextWithoutPrincipal() {
    SecurityContext securityContext = getSecurityContextWithoutPrincipal();
    DefaultSubjectResolver sr = new DefaultSubjectResolver();
    assertEquals(DefaultSubjectResolver.UNKNOWN_SUBJECT, sr.resolve(securityContext));
  }

  @Test
  void testResolveForSecurityContextWithUnknownPrincipalType() {
    String randomUserName = UUID.randomUUID().toString();
    SecurityContext securityContext = getSecurityContextWithAnonymousPrincipal(randomUserName);
    DefaultSubjectResolver sr = new DefaultSubjectResolver();
    assertEquals(randomUserName, sr.resolve(securityContext));
  }

  @Test
  void testResolveForValidSecurityContext() {
    String randomUserName = UUID.randomUUID().toString();
    SecurityContext securityContext = getSecurityContextForSubject(randomUserName);

    DefaultSubjectResolver sr = new DefaultSubjectResolver();
    assertEquals(randomUserName, sr.resolve(securityContext));
  }

  private SecurityContext getSecurityContextForSubject(String subject) {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn(subject);
    return securityContext;
  }

  private SecurityContext getSecurityContextWithAnonymousPrincipal(String subject) {
    Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority("admin"));

    SecurityContextImpl sc = new SecurityContextImpl();
    sc.setAuthentication(new AnonymousAuthenticationToken("ABC", subject, authorities));
    return sc;
  }

  private SecurityContext getSecurityContextWithoutPrincipal() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn(null);
    return securityContext;
  }

  private SecurityContext getSecurityContextWithoutAuthentication() {
    SecurityContextImpl sc = new SecurityContextImpl();
    sc.setAuthentication(null);
    return sc;
  }
}
