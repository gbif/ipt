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
package org.gbif.ipt.utils;

import java.text.DecimalFormat;

public class CoordinateUtils {

  public static final String LATITUDE = "LAT";
  public static final String LONGITUDE = "LON";
  public static final double MIN_LONGITUDE = -180;
  public static final double MAX_LONGITUDE = 180;
  public static final double MIN_LATITUDE = -90;
  public static final double MAX_LATITUDE = 90;


  private CoordinateUtils() {
    // private constructor
  }

  /**
   * This method convert a coordinate from decimal to degrees, minutes, seconds format (DMS).
   * 
   * @param decimalCoordinate to convert.
   * @param coordinateType CoordinateUtils.LATITUDE or CoordinateUtils.LONGITUDE
   * @return an String with the following format: DD°MM'SS''[N, S, W or E]
   */
  public static String decToDms(double decimalCoordinate, String coordinateType) {
    if (coordinateType != null && !(coordinateType.length() == 0)) {
      StringBuilder dms = new StringBuilder();
      double absCoordinate = Math.abs(decimalCoordinate);
      int integer = (int) Math.floor(absCoordinate);
      dms.append(integer);
      dms.append("\u00B0");
      int min = (int) Math.floor(60.0 * (absCoordinate - integer));
      dms.append(min);
      dms.append('\'');
      double sec = (60.0 * (absCoordinate - integer) - min) * 60;
      DecimalFormat f = new DecimalFormat("###.##");
      dms.append(f.format(sec));
      dms.append("''");
      if (coordinateType.equals(LATITUDE)) {
        dms.append(Math.signum(decimalCoordinate) < 0 ? "S" : "N");
      }
      if (coordinateType.equals(LONGITUDE)) {
        dms.append(Math.signum(decimalCoordinate) < 0 ? "W" : "E");
      }
      return dms.toString();
    }
    return "";
  }
}
