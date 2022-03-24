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
package org.gbif.ipt.model;

/**
 * A data source for mappings, exposing a record/row iterator through the SourceManager implementation.
 */
public interface Source {

  int getColumns();

  String getDateFormat();

  String getEncoding();

  /**
   * @return the character that separates values in a multi-valued field
   */
  String getMultiValueFieldsDelimitedBy();

  String getName();

  Resource getResource();

  boolean isFileSource();

  boolean isExcelSource();

  boolean isSqlSource();

  boolean isUrlSource();

  boolean isReadable();

  SourceType getSourceType();

  void setColumns(int columns);

  void setDateFormat(String dateFormat);

  void setEncoding(String encoding);

  void setMultiValueFieldsDelimitedBy(String multiValueFieldsDelimitedBy);

  void setName(String name);

  void setReadable(boolean readable);

  void setResource(Resource resource);
}
