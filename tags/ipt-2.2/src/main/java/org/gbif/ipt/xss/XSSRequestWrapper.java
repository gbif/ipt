package org.gbif.ipt.xss;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import static org.gbif.ipt.xss.XSSUtil.stripXSS;

/**
 * Removes possible XSS patterns in parameters.
 * http://ricardozuasti.com/2012/stronger-anti-cross-site-scripting-xss-filter-for-java-web-apps/
 */
public class XSSRequestWrapper extends HttpServletRequestWrapper {


  public XSSRequestWrapper(HttpServletRequest servletRequest) {
    super(servletRequest);
  }

  @Override
  public String[] getParameterValues(String parameter) {
    String[] values = super.getParameterValues(parameter);

    if (values == null) {
      return null;
    }

    int count = values.length;
    String[] encodedValues = new String[count];
    for (int i = 0; i < count; i++) {
      encodedValues[i] = stripXSS(values[i]);
    }

    return encodedValues;
  }

  @Override
  public String getParameter(String parameter) {
    return stripXSS(super.getParameter(parameter));
  }

  @Override
  public String getHeader(String name) {
    return stripXSS(super.getHeader(name));
  }


}
