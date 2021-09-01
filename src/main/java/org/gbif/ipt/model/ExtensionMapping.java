/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.dwc.terms.Term;
import org.gbif.ipt.config.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ExtensionMapping implements Serializable {

  private static final long serialVersionUID = 23789961641L;

  public static final Integer IDGEN_LINE_NUMBER = -1;
  public static final Integer IDGEN_UUID = -2;
  public static final Integer NO_ID = -3;

  private Source source;
  private Extension extension; // persist only the rowType
  // TreeSet will ensure set of PropertyMappings remain ordered
  private Set<PropertyMapping> fields = new TreeSet<>();
  private Integer idColumn;
  private String idSuffix;
  private RecordFilter filter;
  private boolean doiUsedForDatasetId;
  private Date lastModified;

  /**
   * @param peek Peek of File source.
   *
   * @return list of columns names depending on its mapping.
   */
  public List<String> getColumns(List<String[]> peek) {
    if (!peek.isEmpty()) {
      int columnsCount = peek.get(0).length;
      List<String> columns = new ArrayList<>(columnsCount);
      for (int count = 0; count < columnsCount; count++) {
        String value = null;
        for (String[] row : peek) {
          if (row[count] != null && !(row[count].length() == 0)) {
            // add column number and first value as example
            // e.g. Column #2 - Puma conco...
            value = row[count].length() > 10 ? row[count].substring(0, 10) + "..." : row[count];
            break;
          }
        }
        if (value == null) {
          columns.add("Column #" + (count + 1));
        } else {
          columns.add("Column #" + (count + 1) + " - " + value);
        }
      }
      return columns;
    }
    return new ArrayList<>();
  }

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

  public RecordFilter getFilter() {
    return filter;
  }

  public Integer getIdColumn() {
    return idColumn;
  }

  public String getIdSuffix() {
    return idSuffix;
  }

  public Source getSource() {
    return source;
  }

  public boolean isCore() {
    return extension != null && extension.isCore();
  }

  /**
   * Return true if the extension has core type Taxon.
   *
   * @return true if the extension has core type Taxon.
   */
  public boolean isTaxonCore() {
    return isCore() && extension.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON);
  }

  public boolean isMapped(Term t) {
    PropertyMapping pm = getField(t.qualifiedName());
    return pm != null && (pm.getIndex() != null || pm.getDefaultValue() != null);
  }

  /**
   * @return true if the DOI assigned to the resource should be used as the datasetId (if applicable)
   */
  public boolean isDoiUsedForDatasetId() {
    return doiUsedForDatasetId;
  }

  /**
   * @return date extension mapping was last modified
   */
  public Date getLastModified() {
    return lastModified;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setFields(Set<PropertyMapping> fields) {
    this.fields = fields;
  }

  public void setFilter(RecordFilter filter) {
    this.filter = filter;
  }

  public void setIdColumn(Integer idColumn) {
    this.idColumn = idColumn;
  }

  public void setIdSuffix(String idSuffix) {
    this.idSuffix = idSuffix;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public void setDoiUsedForDatasetId(boolean doiUsedForDatasetId) {
    this.doiUsedForDatasetId = doiUsedForDatasetId;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
}
