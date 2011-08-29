/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
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

package org.gbif.ipt.struts2.converter;

import java.util.Map;

import org.apache.commons.lang.xwork.math.DoubleRange;
import org.apache.struts2.util.StrutsTypeConverter;

/**
 * This class validates if the longitude field value is a decimal number.
 * 
 * @author julieth
 */
public class LongitudeFormatConverter extends StrutsTypeConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    // The longitude is validating in a range of doubles
    DoubleRange range = new DoubleRange(-180, 180);
    try {
      Double decimal = Double.parseDouble(values[0].replaceAll(",", "."));
      if (range.containsDouble(decimal)) {
        return decimal;
      }
    } catch (NumberFormatException e) {
    }
    return null;
  }

  @Override
  public String convertToString(Map context, Object o) {
    if (o instanceof Double) {
      return ((Double) o).toString();
    }
    return null;
  }

}
