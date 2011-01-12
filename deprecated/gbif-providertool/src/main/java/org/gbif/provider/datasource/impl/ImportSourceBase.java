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
package org.gbif.provider.datasource.impl;

import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.TermMappingManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TODO: Documentation.
 * 
 */
public abstract class ImportSourceBase implements ImportSource {
  protected static final Pattern ESCAPE_PATTERN = Pattern.compile("[\\t\\n\\r]");
  protected Log log = LogFactory.getLog(getClass());
  @Autowired
  protected TermMappingManager termMappingManager;
  @Autowired
  protected AnnotationManager annotationManager;

  protected boolean hasNext;
  protected Collection<PropertyMapping> properties;
  protected String coreIdColumn;
  protected String guidColumn;
  protected String linkColumn;
  protected String linkTemplate;
  protected DataResource resource;
  protected Long resourceId;
  // key=header column name, value=term mapping map
  protected Map<String, Map<String, String>> vocMap = new HashMap<String, Map<String, String>>();

  public void init(DataResource resource, ExtensionMapping view)
      throws ImportSourceException {
    this.resource = resource;
    this.resourceId = resource.getId();
    if (view.isCore()) {
      PropertyMapping pm = view.getPropertyMappingByName(resource.getDwcGuidPropertyName());
      if (pm != null) {
        this.guidColumn = pm.getColumn();
      }
    }
    this.linkColumn = view.getLinkColumn();
    this.linkTemplate = view.getLinkTemplate();
    // maybe better clone mappings ?
    this.properties = view.getPropertyMappings().values();
    this.coreIdColumn = view.getCoreIdColumn();
    // see if term mappings exist and keep them in vocMap in that case
    for (PropertyMapping pm : this.properties) {
      Map<String, String> tmap = termMappingManager.getMappingMap(pm.getTermTransformationId());
      if (!tmap.isEmpty()) {
        vocMap.put(pm.getColumn(), tmap);
      }
    }
  }

  protected String escapeRawValue(String val) {
    if (StringUtils.trimToNull(val) == null) {
      return null;
    }
    return ESCAPE_PATTERN.matcher(val).replaceAll(" ");
  }

}
