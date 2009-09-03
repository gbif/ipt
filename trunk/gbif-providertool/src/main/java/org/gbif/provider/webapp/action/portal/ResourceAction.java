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

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ResourceKeywordManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceAction extends BaseMetadataResourceAction implements
    Preparable {
  @Autowired
  private EmlManager emlManager;
  @Autowired
  private ResourceKeywordManager keywordManager;
  private Eml eml;
  private String format;
  private List<? extends Resource> resources;
  // for feed
  private final Date now = new Date();
  // for meta portal
  private List<String> alphabet;
  private List<String> keywords;
  private Map<String, Integer> tagcloud;
  // for searching
  private String keyword;
  private String q;
  private BBox bbox;
  private Double bboxTop;
  private Double bboxBottom;
  private Double bboxLeft;
  private Double bboxRight;
  private Integer page = 1;

  @Override
  public String execute() {
    if (resource != null) {
      eml = emlManager.load(resource);
      tagcloud = keywordManager.getCloud();
      return SUCCESS;
    }
    return RESOURCE404;
  }

  public String forward() {
    if (resource instanceof OccurrenceResource) {
      return OCCURRENCE;
    } else if (resource instanceof ChecklistResource) {
      return CHECKLIST;
    } else {
      return METADATA;
    }
  }

  public String geoSearch() {
    bbox = new BBox(bboxBottom, bboxLeft, bboxTop, bboxRight);
    resources = getResourceTypeMatchingManager().searchByBBox(bbox);
    tagcloud = keywordManager.getCloud();
    return SUCCESS;
  }

  public List<String> getAlphabet() {
    return alphabet;
  }

  public BBox getBbox() {
    return bbox;
  }

  public Eml getEml() {
    return eml;
  }

  public String getFormat() {
    return format;
  }

  public String getKeyword() {
    return keyword;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public Date getNow() {
    return now;
  }

  public Integer getPage() {
    return page;
  }

  public String getQ() {
    return q;
  }

  public List<? extends Resource> getResources() {
    return resources;
  }

  public Map<String, Integer> getTagcloud() {
    return tagcloud;
  }

  public String list() {
    resource = null;
    resources = getResourceTypeMatchingManager().latest(page, 500);
    alphabet = keywordManager.getAlphabet();
    if (alphabet.isEmpty()) {
      keywords = new ArrayList();
    } else {
      keywords = keywordManager.getKeywords(alphabet.get(0));
    }
    tagcloud = keywordManager.getCloud();
    return SUCCESS;
  }

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }

  public String search() {
    // check if a single resource is being searched.
    if (resource != null) {
      // forward in this case to different search
      return forward();
    }
    if (q != null) {
      resources = getResourceTypeMatchingManager().search(q);
    } else {
      resources = getResourceTypeMatchingManager().searchByKeyword(keyword);
    }
    tagcloud = keywordManager.getCloud();
    return SUCCESS;
  }

  public void setBboxBottom(Double bboxBottom) {
    this.bboxBottom = bboxBottom;
  }

  public void setBboxLeft(Double bboxLeft) {
    this.bboxLeft = bboxLeft;
  }

  public void setBboxRight(Double bboxRight) {
    this.bboxRight = bboxRight;
  }

  public void setBboxTop(Double bboxTop) {
    this.bboxTop = bboxTop;
  }

  public void setEml(Eml eml) {
    this.eml = eml;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public void setQ(String q) {
    this.q = q;
  }

}
