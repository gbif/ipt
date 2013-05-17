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
import org.apache.log4j.Logger;

/**
 * The Action responsible for showing IPT logs to the admin.
 */
public class LogsAction extends BaseAction {

  // logging
  private static final Logger logger = Logger.getLogger(LogsAction.class);

  private DataDir dataDir;
  private InputStream inputStream;
  protected String log;

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
    File f = dataDir.loggingFile(log + ".log");
    logger.debug("Serving logfile " + f.getAbsolutePath());
    inputStream = new FileInputStream(f);
    return SUCCESS;
  }

  public void setLog(String log) {
    this.log = log;
  }

}
