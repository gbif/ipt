package org.gbif.ipt.task;

import org.apache.log4j.Level;

import java.util.Date;

public class TaskMessage {
  public final Level level;
  public final long timestamp;
  public final String message;
  public final String[] params;

  public TaskMessage(Level level, String message) {
    super();
    this.level = level;
    this.message = message;
    this.params = new String[0];
    this.timestamp = new Date().getTime();
  }

  public TaskMessage(Level level, String message, String[] params) {
    super();
    this.level = level;
    this.message = message;
    this.params = params;
    this.timestamp = new Date().getTime();
  }

  public Date getDate() {
    return new Date(timestamp);
  }

  public Level getLevel() {
    return level;
  }

  public String getMessage() {
    return message;
  }

  public String[] getParams() {
    return params;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return message;
  }
}
