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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.LabelValue;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;

import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public class BaseResourceAction<T extends Resource> extends BaseAction implements SessionAware, Preparable {
  private static final long serialVersionUID = 1643640896L;
  @Autowired
  protected ChecklistResourceManager checklistResourceManager;
  @Autowired
  protected OccResourceManager occResourceManager;
  @Autowired
  @Qualifier("resourceManager")
  protected GenericResourceManager<Resource> metaResourceManager;

  protected GenericResourceManager<T> resourceManager;
  protected Long resourceId;
  protected String guid;
  protected T resource;
  protected Map session;
  protected String resourceType;
  private Map<String, String> resourceTypes;



  public String getGuid() {
    return guid;
  }

  public T getResource() {
    return resource;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public Map<String, String> getResourceTypes() {
    return resourceTypes;
  }

  public void prepare() {
	  this.resourceTypes = this.translateI18nMap(new HashMap<String, String>(ExtensionType.htmlSelectMap));
		      
    if (resourceId != null) {
      resource = resourceManager.get(resourceId);
    } else if (guid != null) {
      resource = resourceManager.get(guid);
    }
    if (resource != null) {
      // update recently viewed resources in session
      updateRecentResouces();
      // if resource instance exists this defines the resourceType we are
      // dealing with
      updateResourceType();
    }
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setResource(T resource) {
    this.resource = resource;
  }

  public void setResourceId(Long resourceId) {
    this.resourceId = resourceId;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public void setSession(Map session) {
    this.session = session;
  }

  protected GenericResourceManager<? extends Resource> getResourceTypeMatchingManager() {
    if (resourceType != null
        && resourceType.equalsIgnoreCase(ExtensionType.Occurrence.alias)) {
      return occResourceManager;
    } else if (resourceType != null
        && resourceType.equalsIgnoreCase(ExtensionType.Checklist.alias)) {
      return checklistResourceManager;
    } else {
      return metaResourceManager;
    }
  }

  protected void setResourceTypes(Map<String, String> resourceTypes) {
    this.resourceTypes = resourceTypes;
  }

  protected void updateRecentResouces() {
    LabelValue res = new LabelValue(resource.getTitle(),
        resource.getId().toString());
    Queue<LabelValue> queue;
    Object rr = session.get(Constants.RECENT_RESOURCES);
    if (rr != null && rr instanceof Queue) {
      queue = (Queue) rr;
    } else {
      queue = new ConcurrentLinkedQueue<LabelValue>();
    }
    // remove old entry from queue if it existed before and insert at tail again
    queue.remove(res);
    queue.add(res);
    if (queue.size() > 10) {
      // only remember last 10 resources
      queue.remove();
    }
    // save back to session
    session.put(Constants.RECENT_RESOURCES, queue);
  }

  protected void updateResourceType() {
    if (resource != null) {
      resourceId = resource.getId();
      if (resource instanceof OccurrenceResource) {
        resourceType = OCCURRENCE;
      } else if (resource instanceof ChecklistResource) {
        resourceType = CHECKLIST;
      } else if (resource instanceof Resource) {
        resourceType = METADATA;
      }
    }
  }

}
