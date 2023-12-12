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
import org.gbif.ipt.model.converter.DataSchemaFieldConverter;
import org.gbif.ipt.model.converter.DataSchemaIdentifierConverter;
import org.gbif.ipt.model.converter.DataTableSchemaNameConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResourceConvertersManager {

  private final UserEmailConverter userConverter;
  private final OrganisationKeyConverter orgConverter;
  private final ExtensionRowTypeConverter extensionConverter;
  private final ConceptTermConverter conceptTermConverter;
  private final DataSchemaIdentifierConverter dataSchemaConverter;
  private final DataTableSchemaNameConverter dataTableSchemaNameConverter;
  private final DataSchemaFieldConverter dataSchemaFieldConverter;
  private final JdbcInfoConverter jdbcInfoConverter;

  @Inject
  public ResourceConvertersManager(
      UserEmailConverter userConverter, OrganisationKeyConverter orgConverter,
      ExtensionRowTypeConverter extensionConverter, ConceptTermConverter conceptTermConverter,
      DataSchemaIdentifierConverter dataSchemaConverter, DataTableSchemaNameConverter dataTableSchemaNameConverter,
      DataSchemaFieldConverter dataSchemaFieldConverter, JdbcInfoConverter jdbcInfoConverter) {
    this.userConverter = userConverter;
    this.orgConverter = orgConverter;
    this.extensionConverter = extensionConverter;
    this.conceptTermConverter = conceptTermConverter;
    this.dataSchemaConverter = dataSchemaConverter;
    this.dataTableSchemaNameConverter = dataTableSchemaNameConverter;
    this.dataSchemaFieldConverter = dataSchemaFieldConverter;
    this.jdbcInfoConverter = jdbcInfoConverter;
  }

  public UserEmailConverter getUserConverter() {
    return userConverter;
  }

  public OrganisationKeyConverter getOrgConverter() {
    return orgConverter;
  }

  public ExtensionRowTypeConverter getExtensionConverter() {
    return extensionConverter;
  }

  public ConceptTermConverter getConceptTermConverter() {
    return conceptTermConverter;
  }

  public DataSchemaIdentifierConverter getDataSchemaConverter() {
    return dataSchemaConverter;
  }

  public DataTableSchemaNameConverter getDataSubschemaNameConverter() {
    return dataTableSchemaNameConverter;
  }

  public DataSchemaFieldConverter getDataSchemaFieldConverter() {
    return dataSchemaFieldConverter;
  }

  public JdbcInfoConverter getJdbcInfoConverter() {
    return jdbcInfoConverter;
  }
}
