/*
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

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;


/**
 * Class with utility functions for URL validation and handling.
 */
public class URLUtils {

  private static final Logger LOG = LogManager.getLogger(URLUtils.class);

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http", "https"},
    UrlValidator.ALLOW_LOCAL_URLS);

  private static final String LOCAL_IP = "127.0.0.1";
  private static final String LOCAL_HOST = "localhost";

  public static final Set<String> VALID_CONTENT_TYPES = new HashSet<String>() {{
    add("text/csv");
    add("text/tab-separated-values");
    add("application/csv");
    add("application/vnd.ms-excel");
    // add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    add("application/zip");
    // add("application/x-zip-compressed");
    // add("application/gzip");
    // add("application/x-gzip");
    // add("application/json");
    add("text/plain");
    // add("application/octet-stream");
    // add("text/xml");
    // add("application/xml");
    // add("application/vnd.oasis.opendocument.spreadsheet");
}};


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

  /**
   * Method to get content type of a URL
   *
   * This method makes a HEAD request to the given URL to fetch the content type
   * without downloading the entire content. It is useful for validating the
   * type of content before performing any further operations.
   *
   * @param urlString the URL as a string
   * @return the content type of the URL
   * @throws IOException if an I/O exception occurs
   */
  public static String getUrlContentType(String urlString) throws IOException, MimeTypeParseException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("HEAD");
    String contentType = connection.getContentType();

    if (contentType != null) {
      MimeType mimeType = new MimeType(contentType);
      return mimeType.getBaseType();
    } else {
      return null;
    }
  }

}
