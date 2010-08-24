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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private Map<Integer, String> columns;
  private List<String[]> peek;

  @Override
  public String delete() {
    if (true) {
      addActionMessage("Deleted mapping " + id);
      ms.saveConfig();
    } else {
      addActionMessage("Couldnt delete mapping " + id);
    }
    return SUCCESS;
  }

  public Map<Integer, String> getColumns() {
    return columns;
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
      columns = new HashMap<Integer, String>();
      if (mapping.getSource() != null) {
        peek = sourceManager.peek(mapping.getSource(), 5);
        int idx = 0;
        for (String col : sourceManager.columns(mapping.getSource())) {
          columns.put(idx, col);
          idx++;
        }
      }
    }

    if (mapping == null || mapping.getExtension() == null) {
      notFound = true;
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
    } else {
      // TODO: save field mappings
    }
    // save entire resource config
    ms.saveConfig();
    return SUCCESS;
  }

  public void setMapping(ExtensionMapping mapping) {
    this.mapping = mapping;
  }

  @Override
  public void validateHttpPostOnly() {
    if (mapping != null) {

    }
  }
}
