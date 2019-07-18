package org.gbif.ipt.task;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Level;

/**
 * A message used in reporting a task's process, which has a level of severity, timestamp, and message.
 */
public class TaskMessage {

  public final Level level;
  public final long timestamp;
  public final String message;

  public TaskMessage(Level level, String message) {
    this.level = level;
    this.message = message;
    this.timestamp = new Date().getTime();
  }

  public Date getDate() {
    return new Date(timestamp);
  }

  @NotNull
  public Level getLevel() {
    return level;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  @NotNull
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return message;
  }
}
