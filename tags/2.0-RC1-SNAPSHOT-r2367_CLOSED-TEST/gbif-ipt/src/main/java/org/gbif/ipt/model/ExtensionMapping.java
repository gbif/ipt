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

package org.gbif.ipt.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class ExtensionMapping {
  private Source source;
  private Extension extension; // persist only the rowType
  private Set<PropertyMapping> fields = new HashSet<PropertyMapping>();
  private Integer idColumn;

  public Extension getExtension() {
    return extension;
  }

  public PropertyMapping getField(String qname) {
    for (PropertyMapping f : fields) {
      if (f.getTerm().qualifiedName().equals(qname)) {
        return f;
      }
    }
    return null;
  }

  public Set<PropertyMapping> getFields() {
    return fields;
  }

  public Integer getIdColumn() {
    return idColumn;
  }

  public Source getSource() {
    return source;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setFields(Set<PropertyMapping> fields) {
    this.fields = fields;
  }

  public void setIdColumn(Integer idColumn) {
    this.idColumn = idColumn;
  }

  public void setSource(Source source) {
    this.source = source;
  }

}
