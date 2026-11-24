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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.admin.DataPackageSchemaAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * The data package page for resource managers, opened from the mapping pages: the schema and its tables and fields,
 * read only. No Registry check and no "Used by": administrators have those on the page under administration
 * ({@link DataPackageSchemaAction}).
 */
public class DataPackageAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -2786093145520846374L;

  private final DataPackageSchemaManager schemaManager;
  @Getter
  private DataPackageSchema dataPackageSchema;
  // the schema file, for "View source"
  @Getter
  private String dataPackageSchemaRawData;
  // the rules on which tables a data package has, as written in the schema
  @Getter
  private List<String> tableRules = new ArrayList<>();

  @Inject
  public DataPackageAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      DataPackageSchemaManager schemaManager) {
    super(textProvider, cfg, registrationManager);
    this.schemaManager = schemaManager;
  }

  @Override
  public String execute() throws Exception {
    dataPackageSchema = id == null ? null : schemaManager.get(id);
    if (dataPackageSchema == null) {
      return NOT_FOUND;
    }
    dataPackageSchemaRawData = schemaManager.getRawData(dataPackageSchema.getName());
    tableRules = DataPackageSchemaAction.tableRulesOf(dataPackageSchema.getTableSchemasRequirements());
    return SUCCESS;
  }
}
