package org.gbif.ipt.task;

import org.apache.commons.lang.xwork.StringUtils;

import java.util.Date;
import java.util.List;

public class StatusReport {
  private final boolean completed;
  private final long timestamp;
  private final String state;
  private final List<TaskMessage> messages;

  public StatusReport(boolean completed, String state, List<TaskMessage> messages) {
    super();
    this.completed = completed;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();

  }

  public StatusReport(String state, List<TaskMessage> messages) {
    super();
    this.completed = false;
    this.state = state;
    this.messages = messages;
    this.timestamp = new Date().getTime();
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

  public boolean isCompleted() {
    return completed;
  }

  @Override
  public String toString() {
    return state + ":" + StringUtils.join(messages, ";");
  }

}
