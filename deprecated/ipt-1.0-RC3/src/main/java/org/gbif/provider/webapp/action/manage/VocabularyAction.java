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

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class VocabularyAction extends BaseAction {
  @Autowired
  private ExtensionPropertyManager propertyManager;
  @Autowired
  private ThesaurusManager thesaurusManager;

  private Map<String, String> voc;
  private ExtensionProperty prop;
  private boolean empty = true;
  private boolean alpha = false;
  private String val;
  private String uri;
  private Long id;

  @Override
  public String execute() {
    prop = propertyManager.get(id);
    if (prop != null && prop.getVocabulary() != null) {
      voc = thesaurusManager.getConceptCodeMap(prop.getVocabulary().getUri(),
          getLocaleLanguage(), false);
    }
    return SUCCESS;
  }

  public Long getId() {
    return id;
  }

  public ExtensionProperty getProp() {
    return prop;
  }

  public String getVal() {
    return val;
  }

  public Map<String, String> getVoc() {
    return voc;
  }

  public boolean isEmpty() {
    return empty;
  }

  public void setAlpha(boolean alpha) {
    this.alpha = alpha;
  }

  public void setEmpty(boolean empty) {
    this.empty = empty;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setVal(String val) {
    this.val = val;
  }

  public String voc() {
    voc = thesaurusManager.getConceptCodeMap(uri, getLocaleLanguage(), alpha);
    return SUCCESS;
  }

}
