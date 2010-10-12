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

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import org.apache.commons.lang.xwork.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class MappingAction extends ManagerBaseAction {
  private static final Pattern normTerm = Pattern.compile("[\\W\\s_0-9]+");
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private SourceManager sourceManager;
  @Inject
  private VocabulariesManager vocabManager;
  // config
  private ExtensionMapping mapping;
  private String source;
  private List<String> columns;
  private List<String[]> peek;
  private List<PropertyMapping> fields;
  private Map<String, Map<String, String>> vocabTerms = new HashMap<String, Map<String, String>>();
  private ExtensionProperty coreid;

  private void automap() {
    int automapped = 0;
    for (PropertyMapping f : fields) {
      int idx = 0;
      for (String col : columns) {
        if (col == null) {
          continue;
        }
        col = normTerm.matcher(col.toLowerCase()).replaceAll("");
        if (col.contains(":")) {
          col = StringUtils.substringAfter(col, ":");
        }
        if (f.getTerm().simpleNormalisedName().equalsIgnoreCase(col)) {
          f.setIndex(idx);
          automapped++;
          break;
        }
        idx++;
      }
    }
    addActionMessage("Automapped " + automapped + " columns based on header names");
  }

  @Override
  public String delete() {
    if (resource.deleteMapping(mapping)) {
      addActionMessage("Deleted mapping " + id);
      saveResource();
    } else {
      addActionMessage("Couldnt delete mapping " + id);
    }
    return SUCCESS;
  }

  public List<String> getColumns() {
    return columns;
  }

  public ExtensionProperty getCoreid() {
    return coreid;
  }

  public List<PropertyMapping> getFields() {
    return fields;
  }

  public ExtensionMapping getMapping() {
    return mapping;
  }

  public List<String[]> getPeek() {
    return peek;
  }

  public Map<String, Map<String, String>> getVocabTerms() {
    return vocabTerms;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (id != null) {
      mapping = resource.getMapping(id);
      // existing mapping?
      if (mapping == null) {
        // a new new mapping
        mapping = new ExtensionMapping();
        mapping.setExtension(extensionManager.get(id));
      }
      readSource();
    }

    if (mapping == null || mapping.getExtension() == null) {
      notFound = true;
    } else {
      // setup the core record id term
      String coreRowType = resource.getCoreRowType();
      if (coreRowType == null) {
        // not yet set, the id of this mapping should be the core row type!
        coreRowType = id;
      }
      String coreIdTerm = Constants.DWC_OCCURRENCE_ID;
      if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
        coreIdTerm = Constants.DWC_TAXON_ID;
      }
      coreid = extensionManager.get(coreRowType).getProperty(coreIdTerm);

      // prepare all other fields
      fields = new ArrayList<PropertyMapping>(mapping.getExtension().getProperties().size());
      for (ExtensionProperty p : mapping.getExtension().getProperties()) {
        // ignore core id term
        if (p.equals(coreid)) {
          continue;
        }
        // uses a vocabulary?
        if (p.getVocabulary() != null) {
          vocabTerms.put(p.getVocabulary().getUri(),
              vocabManager.getI18nVocab(p.getVocabulary().getUri(), getLocaleLanguage(), true));
        }
        // mapped already?
        PropertyMapping f = mapping.getField(p.getQualname());
        if (f == null) {
          // no, create bare mapping field
          f = new PropertyMapping();
        }
        f.setTerm(p);
        fields.add(f);
      }
      // finally do automapping for if no fields are found
      if (mapping.getFields().isEmpty()) {
        automap();
      }

    }
  }

  private void readSource() {
    if (mapping.getSource() != null) {
      peek = sourceManager.peek(mapping.getSource(), 5);
      columns = sourceManager.columns(mapping.getSource());
    } else {
      columns = new ArrayList<String>();
    }
  }

  @Override
  public String save() throws IOException {
    // a new mapping?
    if (resource.getMapping(id) == null) {
      // is this a core "extension" ?
      if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(id) || Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(id)) {
        resource.setCore(mapping);
      } else {
        resource.addExtension(mapping);
      }
      // read source as prepare wasnt yet ready for this
      readSource();
    } else {
      // save field mappings
      Set<PropertyMapping> mappedFields = new HashSet<PropertyMapping>();
      for (PropertyMapping f : fields) {
        if (f.getIndex() != null || StringUtils.trimToNull(f.getDefaultValue()) != null) {
          mappedFields.add(f);
        }
      }
      // back to mapping object
      mapping.setFields(mappedFields);
    }
    // save entire resource config
    saveResource();
    return INPUT;
  }

  public void setFields(List<PropertyMapping> fields) {
    this.fields = fields;
  }

  public void setMapping(ExtensionMapping mapping) {
    this.mapping = mapping;
  }

  public void setSource(String source) {
    Source src = resource.getSource(source);
    mapping.setSource(src);
  }

  @Override
  public void validateHttpPostOnly() {
    if (mapping != null && mapping.getSource() == null) {
      addFieldError("manage.mapping.source",
          getText("validation.required", new String[]{getText("manage.mapping.source")}));
    }
  }
}
