package org.gbif.ipt.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public abstract class ReportingTask {

  protected Logger log = Logger.getLogger(this.getClass());
  private final String resourceShortname;
  private final ReportHandler handler;
  private List<TaskMessage> messages = new ArrayList<TaskMessage>();
  private final int reportingIntervall;
  private StatusReport lastReport;

  /**
   * @param reportingIntervall in milliseconds
   */
  protected ReportingTask(int reportingIntervall, String resourceShortname, ReportHandler handler) {
    this.resourceShortname = resourceShortname;
    this.handler = handler;
    this.reportingIntervall = reportingIntervall;
  }

  protected void addMessage(Level lvl, String msg) {
    if (Level.ERROR.equals(lvl)) {
      log.error(msg);
    } else if (Level.WARN.equals(lvl)) {
      log.warn(msg);
    } else if (Level.INFO.equals(lvl)) {
      log.info(msg);
    } else {
      log.debug(msg);
    }
    messages.add(new TaskMessage(lvl, msg));
  }

  protected abstract boolean completed();

  protected abstract Exception currentException();

  protected abstract String currentState();

  /**
   * Reports back the state of the task to the reporting handler configured.
   * Call this method at least once a second inside your task if possible, so users keep updated.
   */
  public StatusReport report() {
    Exception e = currentException();
    if (e != null) {
      lastReport = new StatusReport(e, currentState(), messages);
    } else {
      lastReport = new StatusReport(completed(), currentState(), messages);
    }
    handler.report(resourceShortname, lastReport);
    return lastReport;
  }

  /**
   * Calls method report if a certain amount of time specified by reportingIntervall in milliseconds has passed since
   * the last reporting.
   */
  public void reportIfNeeded() {
    if (lastReport == null || new Date().getTime() - lastReport.getTimestamp() > reportingIntervall) {
      report();
    }
  }
}
