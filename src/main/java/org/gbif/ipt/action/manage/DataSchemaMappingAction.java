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
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;

import com.google.inject.Inject;

/**
 * Similar to {@link MappingAction}, but manage data schema mappings.
 */
public class DataSchemaMappingAction extends ManagerBaseAction {

  private static final long serialVersionUID = -2005597864256786458L;

  private final DataSchemaManager schemaManager;

  private String schemaName;
  private DataSchema dataSchema;

  @Inject
  public DataSchemaMappingAction(SimpleTextProvider textProvider, AppConfig cfg,
                                 RegistrationManager registrationManager, ResourceManager resourceManager,
                                 DataSchemaManager schemaManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.schemaManager = schemaManager;
  }

  @Override
  public String save() throws IOException {
    dataSchema = schemaManager.get(schemaName);
    return defaultResult;
  }

  @Override
  public void prepare() {
    super.prepare();
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
}
