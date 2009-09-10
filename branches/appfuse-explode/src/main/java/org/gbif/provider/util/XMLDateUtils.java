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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * XSD dateTime utilities
 * 
 * TODO: this does NOT take into account timezones properly...
 * 
 */
public class XMLDateUtils {
  private static SimpleDateFormat xsdDateFormat = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ssZ");
  private static Log log = LogFactory.getLog(XMLDateUtils.class);

  /**
   * Parses a date or swallows errors with a warning log
   * 
   * @param xmlDateTime To parse
   * @return The date or null
   */
  public static Date toDate(String xmlDateTime) {
    if (xmlDateTime.length() != 25) {
      log.warn("Date not in expected xml datetime format (not 25 characters): "
          + xmlDateTime);
    } else {
      StringBuilder sb = new StringBuilder(xmlDateTime);
      sb.deleteCharAt(22);
      try {
        return xsdDateFormat.parse(sb.toString());
      } catch (ParseException e) {
        log.warn("Ignoring issued since unparsable: " + xmlDateTime);
      }
    }
    return null;
  }
}
