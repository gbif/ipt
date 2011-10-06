/**
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.RollingFileAppender;

/**
 * LogFileAppender works to backup the IPT log files (admin.log, debug.log). Plus, it looks for a temporal path location
 * while Tomcat, Jetty, or any other server initialise the project at first time before the user is allowed to configure
 * a properly DataDir.
 */
public class LogFileAppender extends RollingFileAppender {

  public static String LOGDIR = "";
  // temporal paths which can be used depending on the OS.
  private static final String[] PATHS = new String[] {System.getProperty("java.io.tmpdir"),
    System.getProperty("user.home"), System.getProperty("user.dir")};

  /**
   * Find temporal path with writing permissions depending on the Operating System.
   * 
   * @return the location of the temporal file.
   */
  private String findTempDir() {
    File logFile = null;
    for (String path : PATHS) {
      // Create file instance.
      logFile = new File(path, "admin.log");

      // Has the file writing permissions?
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        // Do nothing here.
      }
      if (logFile.canWrite()) {
        return path;
      }
    }
    return "";
  }

  @Override
  public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
    throws IOException {
    File logfile = new File(fileName);

    if (!LOGDIR.equals("")) {
      // modify fileName if relative
      if (!logfile.isAbsolute()) {
        fileName = LOGDIR + File.separator + fileName;
      }
    } else {
      // if LOGDIR is not initialised, find a temporal location while user configure the IPT DataDir.
      fileName = findTempDir() + File.separator + fileName;
    }
    super.setFile(fileName, append, bufferedIO, bufferSize);
  }
}
