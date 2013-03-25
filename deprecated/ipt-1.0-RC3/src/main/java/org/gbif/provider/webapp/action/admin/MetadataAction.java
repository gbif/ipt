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
package org.gbif.provider.webapp.action.admin;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ResourceType;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.apache.struts2.interceptor.ServletRequestAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO: Documentation.
 * 
 */
public class MetadataAction extends BaseMetadataResourceAction implements
    Preparable, ServletRequestAware {
  protected HttpServletRequest request;
  protected List<? extends Resource> resources;
  private final Map<String, String> resourceTypeMap = translateI18nMap(new HashMap<String, String>(
      ResourceType.htmlSelectMap));

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public List<?> getResources() {
    return resources;
  }

  public Map<String, String> getResourceTypeMap() {
    return resourceTypeMap;
  }

  public String list() {
    resource = null;
    resources = resourceManager.getAll();
    return SUCCESS;
  }

  public String save() {
    if (resource == null) {
      return RESOURCE404;
    }
    if (cancel != null) {
      return "cancel";
    }

    resource.setDirty();
    resource = resourceManager.save(resource);
    return SUCCESS;
  }

  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }
}
