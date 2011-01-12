/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.util;

import com.opensymphony.xwork2.util.TypeConversionException;

import org.apache.struts2.util.StrutsTypeConverter;

import java.text.ParseException;
import java.util.Map;

/**
 * Supports the conversion of java.sql.Date - required by JIBX.
 * 
 */
public class SqlDateConverter extends StrutsTypeConverter {

  /**
   * @see org.apache.struts2.util.StrutsTypeConverter#convertFromString(java.util.Map,
   *      java.lang.String[], java.lang.Class)
   */
  @Override
  public Object convertFromString(Map ctx, String[] value, Class arg2) {
    if (value[0] == null || value[0].trim().equals("")) {
      return null;
    }
    try {
      java.util.Date myDate = DateUtil.convertStringToDate(value[0]);
      return new java.sql.Date(myDate.getTime());
    } catch (ParseException pe) {
      pe.printStackTrace();
      throw new TypeConversionException(pe.getMessage());
    }
  }

  /**
   * @see org.apache.struts2.util.StrutsTypeConverter#convertToString(java.util.Map,
   *      java.lang.Object)
   */
  @Override
  public String convertToString(Map ctx, Object data) {
    return org.gbif.provider.util.DateUtil.convertDateToString((java.sql.Date) data);
  }
}