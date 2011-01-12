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
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class DwcAction extends BaseDataResourceAction {
  @Autowired
  private DarwinCoreManager darwinCoreManager;
  @Autowired
  private ExtensionRecordManager extensionRecordManager;
  @Autowired
  private AnnotationManager annotationManager;
  private Long taxonId;
  private Long regionId;
  private Long id;
  private DarwinCore dwc;
  private ExtendedRecord rec;
  private String format;
  private String q;
  private NamespaceRegistry nsr;
  private Map<Object, Object> json;
  private List<DarwinCore> occurrences;
  private List<Annotation> annotations;
  @Autowired
  private AppConfig cfg;

  @Override
  public String execute() {
    setRequestedRecord();
    if (dwc != null) {
      if (dwc.getRegion() != null) {
        regionId = dwc.getRegion().getId();
      }
      if (dwc.getTaxon() != null) {
        taxonId = dwc.getTaxon().getId();
      }
      rec = extensionRecordManager.extendCoreRecord(dwc.getResource(), dwc);
      if (format != null && format.equalsIgnoreCase("xml")) {
        nsr = new NamespaceRegistry(dwc.getResource());
        return "xml";
      } else if (format != null && format.equalsIgnoreCase("json")) {
        // TODO: create map to serialise into JSON
        json = new HashMap<Object, Object>();
        return "json";
      }
      // find annotations
      annotations = annotationManager.getByRecord(dwc.getResourceId(),
          dwc.getGuid());

      return SUCCESS;
    }
    return RECORD404;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public DarwinCore getDwc() {
    return dwc;
  }

  public String getFormat() {
    return format;
  }

  @Override
  public String getGuid() {
    return guid;
  }

  public NamespaceRegistry getNsr() {
    return nsr;
  }

  public List<DarwinCore> getOccurrences() {
    return occurrences;
  }

  public String getQ() {
    return q;
  }

  public ExtendedRecord getRec() {
    return rec;
  }

  public DarwinCore getRecord() {
    return dwc;
  }

  public Long getRegionId() {
    return regionId;
  }

  public Long getTaxonId() {
    return taxonId;
  }

  public String search() {
    super.prepare();
    occurrences = darwinCoreManager.search(resourceId, q);
    return SUCCESS;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  @Override
  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setOccurrences(List<DarwinCore> occurrences) {
    this.occurrences = occurrences;
  }

  public void setQ(String q) {
    this.q = q;
  }

  private void setRequestedRecord() {
    if (id != null) {
      dwc = darwinCoreManager.get(id);
    } else if (guid != null) {
      dwc = darwinCoreManager.get(guid);
      if (dwc != null) {
        id = dwc.getId();
      }
    }
    if (resource == null && dwc != null) {
      resource = dwc.getResource();
      updateResourceType();
    }
  }

}