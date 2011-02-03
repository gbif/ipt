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

import org.gbif.dwc.text.ArchiveField;

import java.io.Serializable;
import java.util.Map;

/**
 * @author markus
 * 
 */
public class PropertyMapping extends ArchiveField implements Serializable {
  private static final long serialVersionUID = 775627548L;
  private Map<String, String> translation;

  public PropertyMapping() {
    super();
  }

  public PropertyMapping(ArchiveField field) {
    super(field.getIndex(), field.getTerm(), field.getDefaultValue(), field.getType());
  }

  public Map<String, String> getTranslation() {
    return translation;
  }

  /*
   * the mapping doesnt keep track of the data type.
   * Use extension and its ExtensionProperty class instead!
   * (non-Javadoc)
   * @see org.gbif.dwc.text.ArchiveField#getType()
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
}
