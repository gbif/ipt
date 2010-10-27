package org.gbif.ipt.struts2.converter;

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
            "\\\\r", String.valueOf('\r')).replaceAll("\\\\f", String.valueOf('\f'));
        return proper;
      }
    }
    return null;
  }

  @Override
  public String convertToString(Map context, Object o) {
    String s = o.toString();
    if (s == null || s.equals("")) {
      return null;
    } else {
      // convert from proper char to escaped version
      s = s.replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r").replaceAll("\\f", "\\\\f");
    }
    return s;
  }
}
