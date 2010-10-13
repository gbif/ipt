package org.gbif.ipt.utils;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.task.TaskMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ActionLogger {
  private final Logger log;
  private final BaseAction action;

  public ActionLogger(Logger log, BaseAction action) {
    super();
    this.log = log;
    this.action = action;
  }

  public void error(String message) {
    action.addActionWarning(action.getText(message));
    log.error(message);
  }

  public void error(String message, String[] args) {
    action.addActionWarning(action.getText(message, args));
    log.error(message);
  }

  public void error(String message, String[] args, Throwable t) {
    action.addActionWarning(action.getText(message, args));
    log.error(message, t);
  }

  public void error(String message, Throwable t) {
    action.addActionWarning(action.getText(message));
    log.error(message, t);
  }

  public void error(Throwable t) {
    action.addActionWarning(t.getMessage());
    log.error(t);
  }

  public void info(String message) {
    action.addActionMessage(action.getText(message));
    log.info(message);
  }

  public void info(String message, String[] args) {
    action.addActionMessage(action.getText(message, args));
    log.info(message);
  }

  public void info(String message, String[] args, Throwable t) {
    action.addActionMessage(action.getText(message, args));
    log.info(message, t);
  }

  public void info(String message, Throwable t) {
    action.addActionMessage(action.getText(message));
    log.info(message, t);
  }

  public void info(Throwable t) {
    action.addActionMessage(t.getMessage());
    log.info(t);
  }

  public void log(TaskMessage msg) {
    if (Level.ERROR.equals(msg.level)) {
      action.addActionWarning(action.getText(msg.message, msg.params));
      log.error(msg.message);
    } else {
      action.addActionMessage(action.getText(msg.message, msg.params));
      log.log(msg.level, msg.message);
    }
  }

  public void warn(String message) {
    info(message);
  }

  public void warn(String message, String[] args) {
    info(message, args);
  }

  public void warn(String message, String[] args, Throwable t) {
    info(message, args, t);
  }

  public void warn(String message, Throwable t) {
    info(message, t);
  }

  public void warn(Throwable t) {
    info(t);
  }

}
