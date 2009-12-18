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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.model.voc.TransformationType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Documentation.
 * 
 */
public class PropertyMappingAction extends BaseDataResourceAction implements
    Preparable {
  private static final long serialVersionUID = 14321432161L;

  @Autowired
  private SourceInspectionManager sourceInspectionManager;
  @Autowired
  private SourceManager sourceManager;
  @Autowired
  @Qualifier("viewMappingManager")
  private GenericManager<ExtensionMapping> viewMappingManager;
  @Autowired
  private ExtensionManager extensionManager;
  @Autowired
  @Qualifier("propertyMappingManager")
  private GenericManager<PropertyMapping> propertyMappingManager;
  @Autowired
  private TransformationManager transformationManager;

  // persistent stuff
  private Long mid;
  private Long eid;
  private Long sid;
  // persistent stuff
  private ExtensionMapping view;
  // transformationID for term mapping forwarding only
  private Long tid;
  private Long mappingsIdx;
  private String newProperties = ""; // space delimited list of property IDs
  // just added for mapping
  private final Map<String, List<ExtensionProperty>> availProperties = new HashMap<String, List<ExtensionProperty>>();
  private PropertyMapping guidProperty;
  private String guidPropertyName;

  // temp stuff
  private List<String> sourceColumns;

  public String delete() {
    if (resource == null) {
      return RESOURCE404;
    }
    resource.removeExtensionMapping(view);
    viewMappingManager.remove(view);
    return SUCCESS;
  }

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public Map<String, List<ExtensionProperty>> getAvailProperties() {
    return availProperties;
  }

  public List<String> getColumnOptions() {
    return sourceColumns;
  }

  public Long getEid() {
    return eid;
  }

  public PropertyMapping getGuidProperty() {
    return guidProperty;
  }

  public String getGuidPropertyName() {
    return guidPropertyName;
  }

  public Long getMid() {
    return mid;
  }

  public String getNewProperties() {
    return newProperties;
  }

  public Long getSid() {
    return sid;
  }

  public List<String> getSourceColumns() {
    return sourceColumns;
  }

  public Long getTid() {
    return tid;
  }

  public ExtensionMapping getView() {
    return view;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (resource != null) {
      if (mid != null) {
        // get existing view mapping
        view = viewMappingManager.get(mid);
        if (view != null && view.getSource() == null && sid != null) {
          // this is probably the default core mapping without a source assigned
          // yet.
          view.setSource(sourceManager.get(sid));
          viewMappingManager.save(view);
        }
      } else if (eid != null && sid != null) {
        // create new view mapping
        view = new ExtensionMapping();
        view.setResource(resource);
        view.setExtension(extensionManager.get(eid));
        view.setSource(sourceManager.get(sid));
        viewMappingManager.save(view);
        mid = view.getId();
      } else {
        log.warn("No view mapping could be loaded or created");
        return;
      }
      // generate basic column mapping options
      try {
        sourceColumns = sourceInspectionManager.getHeader(view.getSource());
      } catch (Exception e) {
        sourceColumns = new ArrayList<String>();
        log.debug("Cant read datasource column headers", e);
      }

      // try to automap columns in case there aint no mapping yet
      automap();

      // the property name used as GUID depending on resource type
      guidPropertyName = resourceType.equals(CHECKLIST)
          ? ChecklistResource.DWC_GUID_PROPERTY
          : OccurrenceResource.DWC_GUID_PROPERTY;
      // prepare list of property mappings to create form with and to be filled
      // by params interceptor
      // parse list of newly mapped properties
      List<String> newIdList = Arrays.asList(StringUtils.split(newProperties,
          " "));
      for (ExtensionProperty prop : view.getExtension().getProperties()) {
        if (prop == null) {
          continue;
        }
        // for the darwin core mapping filter some properties
        if (view.isCore()) {
          // for checklists only show the taxon group of darwin core
          if (resourceType.equals(CHECKLIST)
              && !ChecklistResource.DWC_GROUPS.contains(prop.getGroup())) {
            continue;
          }
          // remove the GUID identifier for DarwinCore
          if (prop.getName().equalsIgnoreCase(guidPropertyName)) {
            // setup GUID property depending upon resource type
            guidProperty = view.getPropertyMapping(prop.getId());
            if (guidProperty == null) {
              // setup empty one
              guidProperty = new PropertyMapping();
              guidProperty.setProperty(prop);
              view.addPropertyMapping(guidProperty);
            }
            continue;
          }
        }
        String group = prop.getGroup() == null ? view.getExtension().getName()
            : prop.getGroup();
        // is this property mapped already?
        if (!view.hasMappedProperty(prop)) {
          // no, not yet. was it just added or is it required?
          if (newIdList.contains(prop.getId().toString()) || prop.isRequired()) {
            PropertyMapping pMap = new PropertyMapping();
            pMap.setProperty(prop);
            view.addPropertyMapping(pMap);
          } else {
            // no, so add to available properties
            if (!availProperties.containsKey(group)) {
              availProperties.put(group, new ArrayList<ExtensionProperty>());
            }
            availProperties.get(group).add(prop);
          }
        }
      }
    }
  }

  public String save() throws Exception {
    if (cancel != null) {
      return "cancel";
    }
    if (delete != null) {
      return delete();
    }
    // remove empty property mappings
    List<PropertyMapping> persistentProps = new ArrayList<PropertyMapping>(
        view.getPropertyMappings().values());
    for (PropertyMapping pm : persistentProps) {
      if (pm != null && pm.getColumn() != null) {
        String key = StringUtils.trimToEmpty(pm.getColumn());
      }
      // and remove empty ones that are still persistent
      if (pm.isEmpty() && pm.getId() != null) {
        view.removePropertyMapping(pm);
        propertyMappingManager.remove(pm);
      }
    }
    // cascade-save view mapping
    view = viewMappingManager.save(view);
    return SUCCESS;
  }

  public void setEid(Long eid) {
    this.eid = eid;
  }

  public void setGuidProperty(PropertyMapping guidProperty) {
    this.guidProperty = guidProperty;
  }

  public void setMappingsIdx(Long mappingsIdx) {
    this.mappingsIdx = mappingsIdx;
  }

  public void setMid(Long mid) {
    this.mid = mid;
  }

  public void setNewProperties(String newProperties) {
    this.newProperties = newProperties.trim();
  }

  public void setSid(Long sid) {
    this.sid = sid;
  }

  public void setView(ExtensionMapping view) {
    this.view = view;
  }

  public String termMapping() throws Exception {
    save();
    if (mappingsIdx != null) {
      PropertyMapping pm = view.getPropertyMapping(mappingsIdx);
      mid = pm.getViewMapping().getId();
      tid = pm.getTermTransformationId();
      if (tid == null) {
        // create new transformation
        Transformation trans = new Transformation();
        trans.setType(TransformationType.Vocabulary);
        trans.setResource(resource);
        trans.setSource(pm.getViewMapping().getSource());
        trans.setColumn(pm.getColumn());
        if (pm.getProperty().getVocabulary() != null) {
          trans.setVoc(pm.getProperty().getVocabulary());
        }
        transformationManager.save(trans);
        tid = trans.getId();
        pm.setTermTransformation(trans);
        propertyMappingManager.save(pm);
      }
      return "terms";
    }
    return ERROR;
  }

  private void automap() {
    // if this mapping is still empty try to automap
    if (view != null && view.getMappedProperties() != null
        && view.getMappedProperties().size() < 1) {
      // regex pattern to normalise property names
      Pattern p = Pattern.compile("[\\s_-]");
      Matcher m = null;
      int autoCount = 0;
      for (ExtensionProperty prop : view.getExtension().getProperties()) {
        if (resourceType.equals(CHECKLIST)
            && !ChecklistResource.DWC_GROUPS.contains(StringUtils.trimToEmpty(prop.getGroup()))) {
          // for checklists only show the taxon group of darwin core
          continue;
        }
        m = p.matcher(prop.getName());
        String propName = m.replaceAll("");
        for (String col : sourceColumns) {
          m = p.matcher(col);
          String colName = m.replaceAll("");
          if (propName.equalsIgnoreCase(colName)) {
            PropertyMapping pm = new PropertyMapping();
            pm.setProperty(prop);
            pm.setColumn(col);
            view.addPropertyMapping(pm);
            autoCount++;
            break;
          }
        }
      }
      log.info("Automapping of columns found " + autoCount
          + " matching properties");
      saveMessage("Automapping of columns found " + autoCount
          + " matching properties");
    }
  }

}
