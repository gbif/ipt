/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author markus
 * 
 */
@Singleton
public class DownloadUtil {
  private static final Logger log = Logger.getLogger(DownloadUtil.class);
  private HttpClient client;
  private static final String LAST_MODIFIED = "Last-Modified";
  private static final String MODIFIED_SINCE = "If-Modified-Since";
  // date format see http://tools.ietf.org/html/rfc2616#section-3.3
  // example:
  // Wed, 21 Jul 2010 22:37:31 GMT
  private static final SimpleDateFormat DATE_FORMAT_RFC2616 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
      Locale.US);

  @Inject
  public DownloadUtil(HttpClient client) {
    super();
    this.client = client;
  }

  public static void main(String[] args) throws Exception {
    HttpClient cl = new HttpClient();
    DownloadUtil util = new DownloadUtil(cl);
    Date last = DATE_FORMAT_RFC2616.parse("Wed, 03 Aug 2010 22:37:31 GMT");
    last = DATE_FORMAT_RFC2616.parse("Wed, 04 Aug 2010 8:14:57 GMT");

    File tmp = File.createTempFile("vocab", ".xml");
    util.downloadIfChanged(new URL("http://rs.gbif.org/vocabulary/gbif/resource_type.xml"), last, tmp);
  }

  /**
   * @param url
   * @param lastModified
   * @return body content if changed or null if unmodified since lastModified
   * @throws IOException
   */
  public String downloadIfChanged(URL url, Date lastModified) throws IOException {
    String body = null;
    HttpMethod method = new GetMethod(url.toString());
    method.setFollowRedirects(true);
    String modifiedSince = DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified);
    method.setRequestHeader(new Header(MODIFIED_SINCE, modifiedSince));
    client.executeMethod(method);
    if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      log.debug("Content not modified since last request");
    } else {
      log.debug("Get Method retrieved content. Last modified=" + method.getResponseHeader(LAST_MODIFIED).getValue());
      body = method.getResponseBodyAsString();
    }
    method.releaseConnection();
    return body;
  }

  /**
   * Downloads a url to a file if its modified since the date given.
   * Updates the last modified file property to reflect the last servers modified http header.
   * 
   * @param url
   * @param lastModified
   * @param downloadTo file to download to
   * @return true if changed or false if unmodified since lastModified
   * @throws IOException
   */
  public boolean downloadIfChanged(URL url, Date lastModified, File downloadTo) throws IOException {
    HttpMethod method = new GetMethod(url.toString());
    method.setFollowRedirects(true);

    // prepare conditional GET request headers
    if (lastModified != null) {
      method.setRequestHeader(new Header(MODIFIED_SINCE, DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified)));
      log.debug("Conditional GET: " + DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified));
    }

    // execute
    client.executeMethod(method);
    boolean downloaded = false;
    if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      log.debug("Content not modified since last request");
    } else {
      Date serverModified = null;
      try {
        serverModified = DATE_FORMAT_RFC2616.parse(method.getResponseHeader(LAST_MODIFIED).getValue());
        log.debug("Content last modified on server: " + serverModified);
      } catch (ParseException e) {
        log.debug("Cant parse http header Last-Modified date");
      }

      // copy to local file
      Writer writer = new FileWriter(downloadTo);
      InputStream is = method.getResponseBodyAsStream();
      IOUtils.copy(is, writer);
      writer.close();
      downloaded = true;
      // update last modified of file with http header date from server
      if (serverModified != null) {
        downloadTo.setLastModified(serverModified.getTime());
      }

      log.debug("Successfully downloaded " + url + " to " + downloadTo.getAbsolutePath());
    }

    method.releaseConnection();
    return downloaded;
  }

  /**
   * Downloads a url to a local file using conditional GET, i.e. only downloading the file again if it has been changed
   * since the last download
   * 
   * @param url
   * @param downloadTo
   * @return
   * @throws IOException
   */
  public boolean downloadIfChanged(URL url, File downloadTo) throws IOException {
    Date lastModified = null;
    if (downloadTo.exists()) {
      lastModified = new Date(downloadTo.lastModified());
    }
    return downloadIfChanged(url, lastModified, downloadTo);
  }

}
