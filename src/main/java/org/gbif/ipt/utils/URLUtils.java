package org.gbif.ipt.utils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class with utility functions for URL validation and handling.
 */
public class URLUtils {

  private static final Logger LOG = LogManager.getLogger(URLUtils.class);

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http", "https"},
    UrlValidator.ALLOW_LOCAL_URLS);

  private static final String LOCAL_IP = "127.0.0.1";
  private static final String LOCAL_HOST = "localhost";

  private URLUtils() {

  }

  public static boolean isLocalhost(URL url) {
    return LOCAL_HOST.equalsIgnoreCase(url.getHost()) || LOCAL_IP.equalsIgnoreCase(url.getHost());
  }

  /**
   * Check if the host name of the <code>URL</code> matches the host name of the local host.
   *
   * @param url URL
   *
   * @return true if the host names match, false otherwise
   */
  public static boolean isHostName(URL url) {
    return url.getHost().equalsIgnoreCase(getHostName());
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
    return StringUtils.isNotBlank(url) && url.split(":").length > 2;
  }


  /**
   * Validates if the parameter is a valid HTTP URL.
   */
  public static boolean isURLValid(String url) {
    return URL_VALIDATOR.isValid(url);
  }


  /**
   * Extracts the HttpHost from the httpUrl parameter.
   */
  public static HttpHost getHost(String httpUrl) throws MalformedURLException {
    URL url = new URL(httpUrl);
    HttpHost host;
    if (URLUtils.hasPort(httpUrl)) {
      host = new HttpHost(url.getHost(), url.getPort());
    } else {
      host = new HttpHost(url.getHost());
    }
    return host;
  }

}
