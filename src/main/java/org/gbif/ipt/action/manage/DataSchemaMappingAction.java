/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceWithHeader;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

/**
 * Similar to {@link MappingAction}, but manage data schema mappings.
 */
public class DataSchemaMappingAction extends ManagerBaseAction {

  private static final long serialVersionUID = -2005597864256786458L;

  private final DataSchemaManager schemaManager;
  private final SourceManager sourceManager;

  private String schemaName;
  private DataSchema dataSchema;
  private Integer mid;
  private DataSchemaMapping mapping;
  private List<String> columns;
  private List<String[]> peek;

  @Inject
  public DataSchemaMappingAction(SimpleTextProvider textProvider, AppConfig cfg,
                                 RegistrationManager registrationManager, ResourceManager resourceManager,
                                 DataSchemaManager schemaManager, SourceManager sourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.schemaManager = schemaManager;
    this.sourceManager = sourceManager;
  }

  @Override
  public String save() throws IOException {
    dataSchema = schemaManager.get(schemaName);
    return defaultResult;
  }

  private void readSource() {
    Source src = mapping.getSource();
    if (src == null) {
      columns = new ArrayList<>();
    } else {
      peek = sourceManager.peek(src, 5);
      // If user wants to import a source without a header lines, the columns are going to be numbered with the first
      // non-null value as an example. Otherwise, read the file/database normally.
      if ((src.isUrlSource() || src.isFileSource())
          && ((SourceWithHeader) src).getIgnoreHeaderLines() == 0) {
        columns = mapping.getColumns(peek);
      } else {
        columns = sourceManager.columns(src);
      }
      if (columns.isEmpty() && src.getName() != null) {
        addActionWarning(getText("manage.mapping.source.no.columns", new String[] {src.getName()}));
      }
    }
  }

  @Override
  public void prepare() {
    super.prepare();

    // get mapping sequence id from parameters as setters are not called yet
    String midStr = StringUtils.trimToNull(req.getParameter("mid"));
    if (midStr != null) {
      mid = Integer.valueOf(midStr);
    }

    if (id != null) {
      if (mid == null) {
        DataSchema ds = schemaManager.get(id);
        if (ds != null) {
          mapping = new DataSchemaMapping();
          mapping.setDataSchema(ds);
        }
        // The data schema could have been null if:
        // 1. The user tried to add a mapping with the select help option, no schema would have been found
        // 2. No schema could be retrieved for the id
        // The result should be the user stays on the overview page, and displays a warning informing them that they
        // need to perform another selection.
        else {
          addActionError(getText("manage.overview.mappings.select.invalid"));
          defaultResult = "error";
        }
      } else {
        List<DataSchemaMapping> maps = resource.getDataSchemaMappings(id);
        mapping = maps.get(mid);
      }
    } else {
      // worst case, just redirect to resource not found page
      notFound = true;
    }

    if (mapping != null && mapping.getDataSchema() != null) {
      // is source assigned yet?
      if (mapping.getSource() == null) {
        // get source parameter as setters are not called yet
        String source = StringUtils.trimToNull(req.getParameter("source"));
        if (source != null) {
          Source src = resource.getSource(source);
          mapping.setSource(src);
        } else {
          // show set source form
          defaultResult = "source";
        }
      }
    }
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public DataSchema getDataSchema() {
    return dataSchema;
  }

  public DataSchemaMapping getMapping() {
    return mapping;
  }
}
