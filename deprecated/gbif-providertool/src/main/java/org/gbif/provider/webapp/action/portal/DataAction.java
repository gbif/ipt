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
package org.gbif.provider.webapp.action.portal;

import static org.gbif.provider.util.Constants.DEFAULT_LOGO;

import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * TODO: Documentation.
 * 
 */
public class DataAction extends BaseMetadataResourceAction {
  private InputStream inputStream;
  @Autowired
  private AppConfig cfg;
  private Integer version = 0;
  private String format;

  public String eml() throws FileNotFoundException {
    setResourceId();
    if (resourceId != null) {
      File eml = null;
      if (version > 0) {
        eml = cfg.getEmlFile(resourceId, version);
      } else {
        eml = cfg.getEmlFile(resourceId);
      }
      try {
        inputStream = new FileInputStream(eml);
      } catch (FileNotFoundException e) {
        return RESOURCE404;
      }
      return SUCCESS;
    }
    return RESOURCE404;
  }

  @Override
  public String execute() throws FileNotFoundException {
    setResourceId();
    if (resourceId != null) {
      File data = null;
      if (format.equalsIgnoreCase("tcs")) {
        data = cfg.getArchiveTcsFile(resourceId);
      } else {
        data = cfg.getArchiveFile(resourceId);
      }
      inputStream = new FileInputStream(data);
      return SUCCESS;
    }
    return RESOURCE404;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public String logo() throws FileNotFoundException {
    setResourceId();
    if (resourceId != null) {
      File logo = cfg.getResourceLogoFile(resourceId);
      try {
        inputStream = new FileInputStream(logo);
      } catch (FileNotFoundException e) {
        logo = cfg.getWebappFile(DEFAULT_LOGO);
        inputStream = new FileInputStream(logo);
      }
      return SUCCESS;
    }
    return RESOURCE404;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  private void setResourceId() {
    if (resourceId == null) {
      prepare();
      if (resource != null) {
        resourceId = resource.getId();
      }
    }
  }

}