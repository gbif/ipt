package org.gbif.ipt.utils;

import java.text.DecimalFormat;

/**
 * @author htobon
 */
public class CoordinateUtils {

  public final static String LATITUDE = "LAT";
  public final static String LONGITUDE = "LON";
  public static final double MIN_LONGITUDE = -180;
  public static final double MAX_LONGITUDE = 180;
  public static final double MIN_LATITUDE = -90;
  public static final double MAX_LATITUDE = 90;

  /**
   * This method convert a coordinate from decimal to degrees, minutes, seconds format (DMS).
   * 
   * @param decimalCoordinate to convert.
   * @param CoordinateType CoordinateUtils.LATITUDE or CoordinateUtils.LONGITUDE
   * @return an String with the following format: DD°MM'SS''[N, S, W or E]
   */
  public static String decToDms(double decimalCoordinate, String CoordinateType) {
    if (CoordinateType != null && !CoordinateType.equals("")) {
      StringBuilder dms = new StringBuilder();
      double absCoordinate = Math.abs(decimalCoordinate);
      int integer = (int) Math.floor(absCoordinate);
      dms.append(integer);
      dms.append("\u00B0");
      int min = (int) Math.floor(60.0 * (absCoordinate - integer));
      dms.append(min);
      dms.append("'");
      double sec = ((60.0 * (absCoordinate - integer)) - min) * 60;
      DecimalFormat f = new DecimalFormat("###.##");
      dms.append(f.format(sec));
      dms.append("''");
      if (CoordinateType.equals(LATITUDE)) {
        dms.append((Math.signum(decimalCoordinate) < 0 ? "S" : "N"));
      }
      if (CoordinateType.equals(LONGITUDE)) {
        dms.append((Math.signum(decimalCoordinate) < 0 ? "W" : "E"));
      }
      return dms.toString();
    }
    return "";
  }
}
