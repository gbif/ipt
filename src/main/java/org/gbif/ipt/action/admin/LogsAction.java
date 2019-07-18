package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Action responsible for showing IPT logs to the admin.
 */
public class LogsAction extends BaseAction {

  private static final long serialVersionUID = -5038153790552063249L;

  // logging
  private static final Logger LOG = LogManager.getLogger(LogsAction.class);

  private final DataDir dataDir;
  private InputStream inputStream;
  private String log;


  @Inject
  public LogsAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.dataDir = dataDir;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public String getLog() {
    return log;
  }

  public String logfile() throws IOException {
    // server file as set in prepare method
    File logFile = dataDir.loggingFile(log + ".log");
    // Log file must exist and be a file inside the ipt data directory/log
    if (logFile.exists() && dataDir.loggingDir().equals(logFile.getParentFile())) {
      LOG.debug("Serving logfile " + logFile.getAbsolutePath());
      inputStream = new FileInputStream(logFile);
      return SUCCESS;
    } else {
      return NOT_FOUND;
    }
  }

  public void setLog(String log) {
    this.log = log;
  }

}
