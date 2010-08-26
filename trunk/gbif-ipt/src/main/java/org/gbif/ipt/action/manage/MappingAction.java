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

import org.gbif.dwc.text.ArchiveField;
import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import org.apache.commons.lang.xwork.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class MappingAction extends POSTAction {
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  @Inject
  private ResourceManagerSession ms;
  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private SourceManager sourceManager;
  // config
  private ExtensionMapping mapping;
  private String source;
  private List<String> columns;
  private List<String[]> peek;
  private List<ArchiveField> fields;

  @Override
  public String delete() {
    if (ms.getConfig().deleteMapping(mapping)) {
      addActionMessage("Deleted mapping " + id);
      ms.saveConfig();
    } else {
      addActionMessage("Couldnt delete mapping " + id);
    }
    return SUCCESS;
  }

  public List<String> getColumns() {
    return columns;
  }

  public List<ArchiveField> getFields() {
    return fields;
  }

  public ExtensionMapping getMapping() {
    return mapping;
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public List<String[]> getPeek() {
    return peek;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (id != null) {
      // existing mapping?
      mapping = ms.getConfig().getMapping(id);
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
      fields = new ArrayList<ArchiveField>(mapping.getExtension().getProperties().size());
      for (ExtensionProperty p : mapping.getExtension().getProperties()) {
        // mapped already?
        ArchiveField f = mapping.getField(p.getQualname());
        if (f == null) {
          f = new ArchiveField();
        }
        f.setTerm(p);
        fields.add(f);
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
    if (ms.getConfig().getMapping(id) == null) {
      // is this a core "extension" ?
      if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(id) || Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(id)) {
        ms.getConfig().setCore(mapping);
      } else {
        ms.getConfig().addExtension(mapping);
      }
      // read source as prepare wasnt yet ready for this
      readSource();
    } else {
      // save field mappings
      System.out.println("fields.size()=" + fields.size());
      Set<ArchiveField> mappedFields = new HashSet<ArchiveField>();
      for (ArchiveField f : fields) {
        if (f.getIndex() != null || StringUtils.trimToNull(f.getDefaultValue()) != null) {
          mappedFields.add(f);
          System.out.println(f.getTerm() + ": column " + f.getIndex() + ", value=" + f.getDefaultValue());
        }
      }
      System.out.println("mappedFields.size()=" + mappedFields.size());
      // back to mapping object
      mapping.setFields(mappedFields);
    }
    // save entire resource config
    ms.saveConfig();
    return INPUT;
  }

  public void setFields(List<ArchiveField> fields) {
    this.fields = fields;
  }

  public void setMapping(ExtensionMapping mapping) {
    this.mapping = mapping;
  }

  public void setSource(String source) {
    Source src = ms.getConfig().getSource(source);
    mapping.setSource(src);
  }

  @Override
  public void validateHttpPostOnly() {
    if (mapping != null && mapping.getSource() == null) {
      addFieldError("manage.mapping.source", getText("validation.required"));
    }
  }
}
