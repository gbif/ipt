package org.gbif.ipt.struts2.converter;

import org.gbif.file.CSVReader;

import org.apache.struts2.util.StrutsTypeConverter;

import java.util.Map;

public class EscapedCharConverter extends StrutsTypeConverter {

  @Override
  public Object convertFromString(Map context, String[] values, Class toClass) {
    if (values != null && values.length > 0) {
      String escaped = values[0];
      if (escaped != null) {
        // convert escaped chars to proper chars
        String proper = escaped.replaceAll("\\\\t", String.valueOf('\t')).replaceAll("\\\\n", String.valueOf('\n')).replaceAll(
            "\\\\r", String.valueOf('\r'));
        if (proper.length() < 1) {
          return CSVReader.NULL_CHAR;
        } else {
          return proper.charAt(0);
        }
      }
    }
    return null;
  }

  @Override
  public String convertToString(Map context, Object o) {
    String s = o.toString();
    if (s == null || s.equals("") || s.equals(String.valueOf(CSVReader.NULL_CHAR))) {
      return null;
    } else {
      // convert from proper char to escaped version
      s = s.replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r");
    }
    return s;
  }
}
