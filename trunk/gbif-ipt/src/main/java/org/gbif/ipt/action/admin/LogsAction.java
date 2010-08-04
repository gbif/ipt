/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.DataDir;

import com.google.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Action responsible for showing IPT logs to the admin
 * 
 */
public class LogsAction extends BaseAction {
  @Inject
  private DataDir dataDir;
  private InputStream inputStream;
  protected String log;

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
    super.log.debug("Serving logfile " + f.getAbsolutePath());
    inputStream = new FileInputStream(f);
    return SUCCESS;
  }

  public void setLog(String log) {
    this.log = log;
  }

}
