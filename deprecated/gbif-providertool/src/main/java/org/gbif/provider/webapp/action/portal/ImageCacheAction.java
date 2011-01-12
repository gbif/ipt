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

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.ImageCacheManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

import org.apache.struts2.util.ServletContextAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 * TODO: Documentation.
 * 
 */
public class ImageCacheAction extends BaseOccurrenceResourceAction implements
    ServletContextAware {
  private InputStream inputStream;
  @Autowired
  private AppConfig cfg;
  @Autowired
  private ImageCacheManager imageCacheManager;
  private ServletContext context;

  @Override
  public String execute() throws FileNotFoundException {
    // imageCacheManager
    if (resourceId != null) {
      OccurrenceResource res = occResourceManager.get(resourceId);
      File data = cfg.getArchiveFile(res.getId());
      inputStream = new FileInputStream(data);
      return SUCCESS;
    }
    return ERROR;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setServletContext(ServletContext context) {
    this.context = context;
  }

}