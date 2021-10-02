/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.struts2.converter;

import org.gbif.metadata.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

public class DateFormatConverter extends StrutsTypeConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    if (values != null && values.length > 0) {
      Date date = null;
      try {
        if (values[0].length() == 4) {
          // A date containing only a year is going to be represented as a date with 1 millisecond (yyyy-01/01 00:00:00:01).
          // This is an internal protocol implemented to communicate with the free marker. See EmlFactory#calendarDate.
          SimpleDateFormat usaFormat = new SimpleDateFormat("yyyy");
          date = usaFormat.parse(values[0]);
          date = new Date(date.getTime() + 1);
        }
      } catch (ParseException e) {
      }

      // this util tries varies popular date formats with ISO first
      if (date == null) {
        date = DateUtils.parse(values[0]);
      }

      // TODO - There should be a way to send an error to the user telling that the date has a wrong format.
      // Throwing XWorkException or TypeConversionException doesn't work.
      //
      return date;
    }
    return null;
  }

  @Override
  public String convertToString(Map context, Object o) {
    if (o instanceof Date) {
      Date d = (Date) o;
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      if (new SimpleDateFormat("SSS").format(d).equals("001")) {
        return format.format(d).split("-")[0];
      }
      return format.format(d);
    }
    return null;
  }

}
