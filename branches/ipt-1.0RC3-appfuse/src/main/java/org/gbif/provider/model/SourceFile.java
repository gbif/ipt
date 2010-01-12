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
package org.gbif.provider.model;

import org.gbif.provider.util.AppConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class SourceFile extends SourceBase {
  private static Log log = LogFactory.getLog(SourceFile.class);
  private Date dateUploaded;
  private long fileSize;
  private boolean headers = false;

  public SourceFile() {
    super();
  }

  public SourceFile(File targetFile) {
    super();
    setFile(targetFile);
  }

  @Transient
  public Date getDateUploaded() {
    return dateUploaded;
  }

  @Transient
  public String getFilename() {
    return name;
  }

  @Transient
  public long getFileSize() {
    return fileSize;
  }

  @Transient
  public long getFileSizeInKB() {
    return fileSize / 1024;
  }

  @Transient
  public boolean hasHeaders() {
    return headers;
  }

  public boolean isHeaders() {
    return headers;
  }

  @Override
  @Transient
  public boolean isValid() {
    if (resource != null) {
      if (AppConfig.getResourceSourceFile(resource.getId(), name).exists()) {
        return true;
      }
    }
    return false;
  }

  public void setDateUploaded(Date dateUploaded) {
    this.dateUploaded = dateUploaded;
  }

  public void setFile(File file) {
    if (file != null) {
      setFilename(file.getName());
    } else {
      setFilename(null);
    }
  }

  public void setFilename(String filename) {
    this.name = filename;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public void setHeaders(boolean headers) {
    this.headers = headers;
  }

}
