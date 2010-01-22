/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.hibernate;

import org.gbif.provider.model.User;

import java.util.Date;

/**
 * Persistent classes that want to track their date time last modified should
 * implement this interface so the AuditInterceptor can set these automatically.
 * Needs to have a property called "modified" which has a Date type, not only
 * the get method!
 * 
 */
public interface Timestampable {
  void setCreated(Date when);

  void setCreator(User modifier);

  void setModified(Date when);

  void setModifier(User modifier);
}
