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

import org.gbif.dwc.ArchiveField;

import java.io.Serializable;
import java.util.Map;

public class PropertyMapping extends ArchiveField implements Serializable, Comparable<PropertyMapping> {

  private static final long serialVersionUID = 775627548L;
  private Map<String, String> translation;

  public PropertyMapping() {
  }

  public PropertyMapping(ArchiveField field) {
    super(field.getIndex(), field.getTerm(), field.getDefaultValue(), field.getType());
  }

  public Map<String, String> getTranslation() {
    return translation;
  }

  /**
   * The mapping doesn't keep track of the data type.
   *
   * @deprecated Use extension and its ExtensionProperty class instead!
   */
  @Override
  @Deprecated
  public DataType getType() {
    return super.getType();
  }

  public void setTranslation(Map<String, String> translation) {
    this.translation = translation;
  }

  @Override
  public String toString() {
    return "PM:" + getTerm() + ";Idx=" + getIndex() + ";Val=" + getDefaultValue();
  }

  /**
   * Compares two PropertyMapping lexicographically based on their qualified normalized names,
   * e.g. "http://purl.org/dc/terms/modified". This way, if 2 terms from 2 namespaces contain the same name, they will
   * still be consistently sorted each time via their namespace to avoid conflict. For example,
   * "http://purl.org/dc/terms/modified" lexicographically comes before "http://rs.tdwg.org/dwc/terms/basisofrecord".
   *
   * @param propertyMapping PropertyMapping
   *
   * @return 0 if names are equal, -1 if argument is lexicographically less, 1 if argument is lexicographically greater
   */
  @Override
  public int compareTo(PropertyMapping propertyMapping) {
    return this.getTerm().qualifiedName().compareTo(propertyMapping.getTerm().qualifiedName());
  }
}
