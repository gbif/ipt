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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.Transformation;
import org.gbif.provider.model.voc.TransformationType;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class TransformationAction extends BaseDataResourceAction implements
    Preparable {
  private static final long serialVersionUID = -3698914322584074200L;

  @Autowired
  private TransformationManager transformationManager;
  private Long tid;
  private final Map<Integer, String> transformationTypes = TransformationType.htmlSelectMap;
  private List<Transformation> transformations;

  public String delete() {
    transformationManager.remove(tid);
    return execute();
  }

  /**
   * Default method - returns "input"
   * 
   * @return "input"
   */
  @Override
  public String execute() {
    transformations = transformationManager.getAll(resourceId);
    Collections.sort(transformations);
    return SUCCESS;
  }

  public Long getTid() {
    return tid;
  }

  public List<Transformation> getTransformations() {
    return transformations;
  }

  public Map<Integer, String> getTransformationTypes() {
    return transformationTypes;
  }

  @Override
  public void prepare() {
    super.prepare();
  }

  public void setTid(Long tid) {
    this.tid = tid;
  }

}
