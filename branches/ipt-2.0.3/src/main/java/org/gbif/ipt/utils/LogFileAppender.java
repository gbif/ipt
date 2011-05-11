/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.utils;

import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;

/**
 * TODO: Documentation.
 */
public class LogFileAppender extends RollingFileAppender {
  public static String LOGDIR = "";

  @Override
  public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
      throws IOException {
	File logfile=new File(fileName);
    // modify fileName if relative
    if (!LOGDIR.equals("") && !logfile.isAbsolute()) {
      fileName = LOGDIR + File.separator + fileName;
    }
    super.setFile(fileName, append, bufferedIO, bufferSize);
  }

}
