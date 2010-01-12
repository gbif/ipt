/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.util;

import org.apache.log4j.RollingFileAppender;

import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
public class IptFileAppender extends RollingFileAppender {
  public static String LOGDIR = "";

  @Override
  public synchronized void setFile(String fileName, boolean append,
      boolean bufferedIO, int bufferSize) throws IOException {
    // modify fileName if relative
    if (!fileName.startsWith("/")) {
      fileName = LOGDIR + "/" + fileName;
    }
    super.setFile(fileName, append, bufferedIO, bufferSize);
  }

}
