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
package org.gbif.provider.webapp.action;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;

/**
 * Homepage of the application giving initial statistics and listing imported
 * resources.
 * 
 */
public class IndexAction extends BaseAction {
  @Autowired
  @Qualifier("dataResourceManager")
  private GenericResourceManager<DataResource> dataResourceManager;
  private List<DataResource> resources;
  @Autowired
  private AppConfig cfg;
  private final Date pubDate = new Date();

  public String about() {
    return SUCCESS;
  }

  @Override
  public String execute() {
    resources = dataResourceManager.getAllDistinct();
    return SUCCESS;
  }

  @Override
  public AppConfig getCfg() {
    return cfg;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public List<DataResource> getResources() {
    return resources;
  }

}
