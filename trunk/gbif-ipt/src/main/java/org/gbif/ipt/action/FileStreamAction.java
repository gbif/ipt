/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A generic streaming class that returns entire file contents from the server no matter where they are located. Please
 * override the prepare() or execute() method to set the actual data file.
 * 
 * @author markus
 * 
 */
public class FileStreamAction extends BaseAction {
  private InputStream inputStream;
  protected File data;
  protected String mimeType = "text/plain";

  @Override
  public String execute() throws FileNotFoundException {
    // server file as set in prepare method
    inputStream = new FileInputStream(data);
    return SUCCESS;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

}