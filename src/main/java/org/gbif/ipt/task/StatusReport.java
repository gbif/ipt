package org.gbif.ipt.task;

import org.apache.commons.lang.xwork.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusReport {
  private final boolean completed;
  private final Exception exception;
  private final long timestamp;
  private final String state;
  private final List<TaskMessage> messages;

  public StatusReport(boolean completed, String state, List<TaskMessage> messages) {
    super();
    this.completed = completed;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
    this.exception = null;

  }

  public StatusReport(Exception exception, String state, List<TaskMessage> messages) {
    super();
    this.completed = true;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
    this.exception = exception;
  }

  public StatusReport(String state, List<TaskMessage> messages) {
    super();
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
