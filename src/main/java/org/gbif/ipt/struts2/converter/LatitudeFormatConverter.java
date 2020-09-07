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

import org.gbif.ipt.utils.CoordinateUtils;

import java.util.Map;


/**
 * This class validates if the latitude field value is a decimal number.
 */
public class LatitudeFormatConverter extends CoordinateFormatConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    context.put("coordinate.angle", CoordinateUtils.LATITUDE);
    return super.convertFromString(context, values, toClass);
  }

}
