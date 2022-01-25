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

import org.gbif.ipt.config.DataDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ReportingTask {

  protected Logger log = LogManager.getLogger(this.getClass());
  protected final DataDir dataDir;
  private final String resourceShortname;
  private final ReportHandler handler;
  private List<TaskMessage> messages = new ArrayList<>();
  private final int reportingIntervall;
  private StatusReport lastReport;
  protected BufferedWriter publicationLogWriter;

  /**
   * Constructor.
   *
   * @param reportingIntervall interval reporting is carried out in milliseconds
   * @param resourceShortname  shortname of resource
   * @param handler            ReportHandler
   * @param dataDir            DataDir
   *
   * @throws IOException if BufferedWriter to publication log file writer could not be created
   */
  protected ReportingTask(int reportingIntervall, String resourceShortname, ReportHandler handler, DataDir dataDir)
    throws IOException {
    this.resourceShortname = resourceShortname;
    this.handler = handler;
    this.reportingIntervall = reportingIntervall;
    this.dataDir = dataDir;
    this.publicationLogWriter = getPublicationLogWriter(resourceShortname);
  }

  /**
   * Logs message, and writes it to publication log file.
   *
   * @param lvl Log level
   * @param msg message
   */
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
    // write to publication log file also
    writePublicationLogMessage(msg);
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

  /**
   * Create new publication log writer ("publication.log") in resource directory. The method does not replace any
   * existing version of the file.
   *
   * @param resourceShortname resource short name
   *
   * @return BufferedWriter
   *
   * @throws IOException if publication log writer could not be created
   */
  private BufferedWriter getPublicationLogWriter(String resourceShortname) throws IOException {
    File logFile = dataDir.resourcePublicationLogFile(resourceShortname);
    return Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8);
  }

  /**
   * Write log message to publication log file as a new line but suffocate any exception thrown.
   *
   * @param message message to write
   */
  protected void writePublicationLogMessage(String message) {
    try {
      publicationLogWriter.write(message + "\n");
    } catch (IOException e) {
      log.error("Publication log file could not be written to by writer: " + e.getMessage(), e);
    }
  }

  /**
   * Close publication log file writer, if the writer is not null.
   */
  protected void closePublicationLogWriter() {
    if (publicationLogWriter != null) {
      try {
        publicationLogWriter.flush();
        publicationLogWriter.close();
      } catch (IOException e) {
        log.error("Publication log file writer could not be closed: " + e.getMessage(), e);
      }
    }
  }
}
