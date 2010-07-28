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

package org.gbif.ipt.struts2.converter;

import com.opensymphony.xwork2.conversion.TypeConversionException;

import org.apache.struts2.util.StrutsTypeConverter;

import java.util.Map;
import java.util.UUID;

/**
 * @author markus
 * 
 */
public class UuidConverter extends StrutsTypeConverter {

  /*
   * (non-Javadoc)
   * @see org.apache.struts2.util.StrutsTypeConverter#convertFromString(java.util.Map, java.lang.String[],
   * java.lang.Class)
   */
  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    UUID uuid;
    try {
      uuid = UUID.fromString(values[0]);
    } catch (Exception e) {
      throw new TypeConversionException(e);
    }
    return uuid;
  }

  /*
   * (non-Javadoc)
   * @see org.apache.struts2.util.StrutsTypeConverter#convertToString(java.util.Map, java.lang.Object)
   */
  @Override
  public String convertToString(Map context, Object o) {

    return o.toString();
  }
}
