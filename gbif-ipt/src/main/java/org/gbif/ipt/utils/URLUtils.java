package org.gbif.ipt.utils;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import com.google.common.base.Strings;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;


public class URLUtils {

  private static final Logger LOG = Logger.getLogger(URLUtils.class);

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http", "https"},
    UrlValidator.ALLOW_LOCAL_URLS);

  private URLUtils() {

  }

  public static boolean isLocalhost(URL url) {
    return "localhost".equals(url.getHost()) || "127.0.0.1".equals(url.getHost())
      || url.getHost().equalsIgnoreCase(getHostName());
  }

  /**
   * Returns the local host name.
   */
  public static String getHostName() {
    String hostName = "";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      LOG.info("No IP address for the local hostname could be found", e);
    }
    return hostName;
  }

  /**
   * Validates if the url contains a Port section "path:port".
   */
  public static boolean hasPort(String url) {
    return !Strings.isNullOrEmpty(url) && url.split(":").length > 2;
  }


  /**
   * Validates if the parameter is a valid HTTP URL.
   */
  public static boolean isURLValid(String url) {
    return URL_VALIDATOR.isValid(url);
  }

}
