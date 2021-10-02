/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class StatusReport {

  private final boolean completed;
  private final Exception exception;
  private final long timestamp;
  private final String state;
  private final List<TaskMessage> messages;

  public StatusReport(boolean completed, String state, List<TaskMessage> messages) {
    this.completed = completed;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
    this.exception = null;

  }

  public StatusReport(Exception exception, String state, List<TaskMessage> messages) {
    this.completed = true;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
    this.exception = exception;
  }

  public StatusReport(String state, List<TaskMessage> messages) {
    this.completed = false;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
    this.exception = null;
  }

  public Exception getException() {
    return exception;
  }

  public String getExceptionMessage() {
    return exception.getMessage();
  }

  public List<String> getExceptionStacktrace() {
    List<String> trace = new ArrayList<String>();
    for (StackTraceElement el : exception.getStackTrace()) {
      trace.add(el.toString());
    }
    return trace;
  }

  public List<TaskMessage> getMessages() {
    return messages;
  }

  public String getState() {
    return state;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean hasException() {
    return exception != null;
  }

  public boolean isCompleted() {
    return completed;
  }

  @Override
  public String toString() {
    return state + ":" + StringUtils.join(messages, ";");
  }

}
