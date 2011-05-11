package org.gbif.ipt.utils;

import java.text.DecimalFormat;

/**
 * @author htobon
 */
public class CoordinateUtils {
  public final static String LATITUDE = "LAT";
  public final static String LONGITUDE = "LON";

  /**
   * This method convert a coordinate from decimal to degrees, minutes, seconds format (DMS).
   * 
   * @param decimalCoordinate to convert.
   * @param CoordinateType CoordinateUtils.LATITUDE or CoordinateUtils.LONGITUDE
   * @return an String with the following format: DD°MM'SS''[N, S, W or E]
   */
  public static String decToDms(double decimalCoordinate, String CoordinateType) {
    String dms = "";
    double absCoordinate = Math.abs(decimalCoordinate);
    int integer = (int) Math.floor(absCoordinate);
    dms += integer + "\u00B0";
    int min = (int) Math.floor(60.0 * (absCoordinate - integer));
    dms += min + "'";
    double sec = ((60.0 * (absCoordinate - integer)) - min) * 60;
    DecimalFormat f = new DecimalFormat("###.##");
    dms += f.format(sec) + "''";
    if (CoordinateType.equals(LATITUDE)) {
      dms += (Math.signum(decimalCoordinate) < 0 ? "S" : "N");
    }
    if (CoordinateType.equals(LONGITUDE)) {
      dms += (Math.signum(decimalCoordinate) < 0 ? "W" : "E");
    }
    return dms;
  }
}
