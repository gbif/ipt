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

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class AnnotationAction extends BaseMetadataResourceAction implements Preparable{
  @Autowired
  private AnnotationManager annotationManager;
  private List<Annotation> annotations;
  private Annotation annotation;
  private Map<String, String> annotationTypes;
  // request parameters
  private Long id;
  private String annotationType;

  @Override
public void prepare() {
	super.prepare();
	annotationTypes = translateI18nMap(new HashMap<String, String>(AnnotationType.htmlSelectMap));
}

@Override
  public String execute() {
    prepare();
    if (resourceId != null && guid != null) {
      annotations = annotationManager.getByRecord(resourceId, guid);
    }
    return SUCCESS;
  }

  public Annotation getAnnotation() {
    return annotation;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public String getAnnotationType() {
    return annotationType;
  }

  public Map<String, String> getAnnotationTypes() {
    return annotationTypes;
  }

  public Long getId() {
    return id;
  }

  public String list() {
    prepare();
    if (resource == null) {
      return RESOURCE404;
    } else {
      if (StringUtils.trimToNull(annotationType) != null) {
        annotations = annotationManager.getByType(resourceId,
            annotationType.toString());
      } else {
        annotations = annotationManager.getAll(resourceId);
      }
    }
    return SUCCESS;
  }

  public void setAnnotationType(String annotationType) {
    this.annotationType = annotationType;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String show() {
    if (id != null) {
      annotation = annotationManager.get(id);
    }
    return SUCCESS;
  }

}