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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Map;

import com.opensymphony.xwork2.conversion.TypeConversionException;
import org.apache.commons.lang3.Range;
import org.apache.struts2.util.StrutsTypeConverter;

/**
 * This class provides the method to validate the latitude and longitude coordinates as decimal numbers.
 */
public abstract class CoordinateFormatConverter extends StrutsTypeConverter {

  private static final String ANGLE = "coordinate.angle";
  private static final char ALTERNATIVE_DECIMAL_SEPARATOR = ',';
  private static final String DECIMAL_PATTERN = "###.##";


  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    // The null value is needed to validate in EmlValidator.java class
    if (values[0].length() == 0) {
      return null;
    }
    // The full name of the property which call the method contained in the Map context
    Object coordObject = context.get(ANGLE);
    // The latitude is validating in a range of doubles
    // validate coordinates in case the action context doesn't work properly.
    if (coordObject == null) {
      throw new TypeConversionException("Invalid decimal number: " + values[0]);
    } else {
      String coordinate = context.get(ANGLE).toString();
      // Assign the values of the range depending the property who calls the method.
      Range<Double> range;
      if (coordinate.equals(CoordinateUtils.LATITUDE)) {
        // The range of the latitude coordinate. (-90,90)
        range = Range.between(CoordinateUtils.MIN_LATITUDE, CoordinateUtils.MAX_LATITUDE);
      } else {
        // The range of the longitude coordinate. (-180,180)
        range = Range.between(CoordinateUtils.MIN_LONGITUDE, CoordinateUtils.MAX_LONGITUDE);
      }

      Double number;
      try {
        // Converts String to double if fails throws a NumberFormatException.
        // If the String contains a comma, a character, it throws the exception.
        number = Double.parseDouble(values[0]);
        // If the value is in the range, returns the double.
        if (range.contains(number)) {
          return number;
        } else {
          throw new TypeConversionException("Invalid decimal number: " + values[0]);
        }
      } catch (NumberFormatException e) {
        // Creating a pattern which will convert the comma to period
        // It will return a ParseException if the format was wrong.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(ALTERNATIVE_DECIMAL_SEPARATOR);
        DecimalFormat decimal = new DecimalFormat(DECIMAL_PATTERN, symbols);
        try {
          number = decimal.parse(values[0]).doubleValue();
          if (range.contains(number)) {
            return number;
          } else {
            throw new TypeConversionException("Invalid decimal number: " + values[0]);
          }
        } catch (ParseException e1) {
          throw new TypeConversionException("Invalid decimal number: " + values[0]);
        }
      }
    }
  }

  @Override
  public String convertToString(Map context, Object o) {
    if (o instanceof Double) {
      return o.toString();
    } else {
      throw new TypeConversionException("Invalid decimal number: " + o.toString());
    }
  }


}
