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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.wilddiary.commons.audit.impl.AuditBeanFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/** Enables the annotation based audit logging. */
@Retention(RUNTIME)
@Target({TYPE})
@Import({AuditBeanFactory.class, AuditAutoConfiguration.class})
public @interface EnableAudit {}
