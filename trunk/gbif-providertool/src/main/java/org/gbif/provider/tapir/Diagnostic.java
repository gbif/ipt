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
package org.gbif.provider.tapir;

import java.util.Date;

/**
 * TODO: Documentation.
 * 
 */
public class Diagnostic {
  private final Severity severity;
  private final Date time;
  private final String text;

  public Diagnostic(Severity severity, Date time, String text) {
    super();
    this.text = text;
    this.severity = severity;
    this.time = time;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getText() {
    return text;
  }

  public Date getTime() {
    return time;
  }

}
