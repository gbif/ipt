/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class TranslationAction extends ManagerBaseAction {
  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private SourceManager sourceManager;
  //
  private static final String REQ_PARAM_TERM = "term";
  private static final String REQ_PARAM_MAPPING = "mapping";
  // config
  private Set<String> values;
  private PropertyMapping field;
  private ExtensionProperty property;
  private ExtensionMapping mapping;

  public TranslationAction() {
    super();
    defaultResult = SUCCESS;
  }

  @Override
  public String delete() {
    addActionMessage("Couldnt delete translation for term " + field.getTerm());
    return SUCCESS;
  }

  public PropertyMapping getField() {
    return field;
  }

  public ExtensionProperty getProperty() {
    return property;
  }

  public Set<String> getValues() {
    return values;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    notFound = true;
    mapping = resource.getMapping(req.getParameter(REQ_PARAM_MAPPING));
    System.out.println("PREPARE mapping=" + req.getParameter(REQ_PARAM_MAPPING) + ", term="
        + req.getParameter(REQ_PARAM_TERM));
    System.out.println("PREPARE mapping:" + mapping);
    if (mapping != null) {
      field = mapping.getField(req.getParameter(REQ_PARAM_TERM));
      System.out.println("PREPARE field:" + field);
      if (field != null) {
        notFound = false;
        property = mapping.getExtension().getProperty(field.getTerm());
        if (field.getTranslation() == null || field.getTranslation().isEmpty()) {
          reloadSourceValues();
        }
      }
    }
  }

  public String reload() {
    try {
      reloadSourceValues();
      saveResource();
    } catch (SourceException e) {
      addActionError(e.getMessage());
    }
    return SUCCESS;
  }

  private void reloadSourceValues() throws SourceException {
    Map<String, String> tmap = new HashMap<String, String>();
    // reload new values
    for (String val : sourceManager.inspectColumn(mapping.getSource(), field.getIndex(), 1000)) {
      tmap.put(val, null);
    }
    // keep existing translations
    if (field.getTranslation() != null) {
      for (Entry<String, String> entry : field.getTranslation().entrySet()) {
        // only keep entries with values mapped that exist in the newly reloaded map
        if (entry.getValue() != null && tmap.containsKey(entry.getKey())) {
          tmap.put(entry.getKey(), entry.getValue());
        }
      }
    }
    // put map back to field
    field.setTranslation(tmap);

    addActionMessage("Reloaded " + tmap.size() + " distinct values from source");
  }

  @Override
  public String save() throws IOException {
    // save entire resource config
    saveResource();
    return SUCCESS;
  }

}
