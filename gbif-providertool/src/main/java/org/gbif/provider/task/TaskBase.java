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
package org.gbif.provider.task;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <R>
 */
public abstract class TaskBase<T, R extends DataResource> implements Task<T> {
  protected GenericResourceManager<R> resourceManager;
  protected final Log log = LogFactory.getLog(getClass());
  @Autowired
  protected AppConfig cfg;
  @Autowired
  protected AnnotationManager annotationManager;
  // needs manual setting when task is created
  private Long resourceId;
  private String title;

  public TaskBase(GenericResourceManager<R> resourceManager) {
    super();
    this.resourceManager = resourceManager;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public String getTitle() {
    if (title == null) {
      // lazy load title
      title = StringUtils.trimToEmpty(loadResource().getTitle());
    }
    return title;
  }

  public void init(Long resourceId) {
    if (resourceId == null) {
      throw new NullPointerException("ResourceID required");
    }
    this.resourceId = resourceId;
  }

  public R loadResource() {
    return resourceManager.get(resourceId);
  }
}
