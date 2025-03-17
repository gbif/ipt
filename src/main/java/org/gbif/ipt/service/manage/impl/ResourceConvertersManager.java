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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataPackageFieldConverter;
import org.gbif.ipt.model.converter.DataPackageIdentifierConverter;
import org.gbif.ipt.model.converter.TableSchemaNameConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;

import javax.inject.Inject;

import lombok.Getter;

@Getter
public class ResourceConvertersManager {

  private UserEmailConverter userConverter;
  private OrganisationKeyConverter orgConverter;
  private ExtensionRowTypeConverter extensionConverter;
  private ConceptTermConverter conceptTermConverter;
  private DataPackageIdentifierConverter dataSchemaConverter;
  private TableSchemaNameConverter tableSchemaNameConverter;
  private DataPackageFieldConverter dataPackageFieldConverter;
  private JdbcInfoConverter jdbcInfoConverter;

  public ResourceConvertersManager() {
  }

  public ResourceConvertersManager(
      UserEmailConverter userConverter, OrganisationKeyConverter orgConverter,
      ExtensionRowTypeConverter extensionConverter, ConceptTermConverter conceptTermConverter,
      DataPackageIdentifierConverter dataSchemaConverter, TableSchemaNameConverter tableSchemaNameConverter,
      DataPackageFieldConverter dataPackageFieldConverter, JdbcInfoConverter jdbcInfoConverter) {
    this.userConverter = userConverter;
    this.orgConverter = orgConverter;
    this.extensionConverter = extensionConverter;
    this.conceptTermConverter = conceptTermConverter;
    this.dataSchemaConverter = dataSchemaConverter;
    this.tableSchemaNameConverter = tableSchemaNameConverter;
    this.dataPackageFieldConverter = dataPackageFieldConverter;
    this.jdbcInfoConverter = jdbcInfoConverter;
  }

  @Inject
  public void setUserConverter(UserEmailConverter userConverter) {
    this.userConverter = userConverter;
  }

  @Inject
  public void setOrgConverter(OrganisationKeyConverter orgConverter) {
    this.orgConverter = orgConverter;
  }

  @Inject
  public void setExtensionConverter(ExtensionRowTypeConverter extensionConverter) {
    this.extensionConverter = extensionConverter;
  }

  @Inject
  public void setConceptTermConverter(ConceptTermConverter conceptTermConverter) {
    this.conceptTermConverter = conceptTermConverter;
  }

  @Inject
  public void setDataSchemaConverter(DataPackageIdentifierConverter dataSchemaConverter) {
    this.dataSchemaConverter = dataSchemaConverter;
  }

  @Inject
  public void setTableSchemaNameConverter(TableSchemaNameConverter tableSchemaNameConverter) {
    this.tableSchemaNameConverter = tableSchemaNameConverter;
  }

  @Inject
  public void setDataPackageFieldConverter(DataPackageFieldConverter dataPackageFieldConverter) {
    this.dataPackageFieldConverter = dataPackageFieldConverter;
  }

  @Inject
  public void setJdbcInfoConverter(JdbcInfoConverter jdbcInfoConverter) {
    this.jdbcInfoConverter = jdbcInfoConverter;
  }
}
