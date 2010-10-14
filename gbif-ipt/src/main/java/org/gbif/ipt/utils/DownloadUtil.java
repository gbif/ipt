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

import org.gbif.ipt.utils.HttpUtil.Response;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author markus
 * 
 */
@Singleton
public class DownloadUtil {
  private static final Logger log = Logger.getLogger(DownloadUtil.class);
  private HttpClient client;
  private HttpUtil http;
  private static final String LAST_MODIFIED = "Last-Modified";
  private static final String MODIFIED_SINCE = "If-Modified-Since";
  // date format see http://tools.ietf.org/html/rfc2616#section-3.3
  // example:
  // Wed, 21 Jul 2010 22:37:31 GMT
  protected static final SimpleDateFormat DATE_FORMAT_RFC2616 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
      Locale.US);

  @Inject
  public DownloadUtil(DefaultHttpClient client, HttpUtil httpUtil) {
    super();
    this.client = client;
    this.http = httpUtil;
  }

  public void download(URL url, File downloadTo) throws IOException {
    HttpGet get = new HttpGet(url.toString());

    // execute
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      // copy stream to local file
      FileUtils.forceMkdir(downloadTo.getParentFile());
      Writer writer = new FileWriter(downloadTo);
      InputStream is = entity.getContent();
      IOUtils.copy(is, writer);
      writer.close();
    }

    // close http connection
    entity.consumeContent();

    log.debug("Successfully downloaded " + url + " to " + downloadTo.getAbsolutePath());
  }

  /**
   * @param url
   * @param lastModified
   * @return body content if changed or null if unmodified since lastModified
   * @throws IOException
   */
  public String downloadIfChanged(URL url, Date lastModified) throws IOException {
    Map<String, String> header = new HashMap<String, String>();
    header.put(MODIFIED_SINCE, DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified));

    try {
      Response resp = http.get(url.toString(), header, null);
      if (resp.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        log.debug("Content not modified since last request");
      } else {
        log.debug("Get Method retrieved content. Last modified=" + resp.getFirstHeader(LAST_MODIFIED).getValue());
      }
      return resp.content;
    } catch (URISyntaxException e) {
      // comes from a URL instance - cant be wrong
      log.error(e);
    }
    return null;
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
    HttpGet get = new HttpGet(url.toString());

    // prepare conditional GET request headers
    if (lastModified != null) {
      get.addHeader(MODIFIED_SINCE, DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified));
      log.debug("Conditional GET: " + DateFormatUtils.SMTP_DATETIME_FORMAT.format(lastModified));
    }

    // execute
    boolean downloaded = false;
    HttpResponse response = client.execute(get);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      log.debug("Content not modified since last request");
    } else {
      Date serverModified = null;
      HttpEntity entity = response.getEntity();
      if (entity != null) {

        try {
          serverModified = DATE_FORMAT_RFC2616.parse(response.getFirstHeader(LAST_MODIFIED).getValue());
          log.debug("Content last modified on server: " + serverModified);
        } catch (ParseException e) {
          log.debug("Cant parse http header Last-Modified date");
        }

        // copy stream to local file
        FileUtils.forceMkdir(downloadTo.getParentFile());
        Writer writer = new FileWriter(downloadTo);
        InputStream is = entity.getContent();
        IOUtils.copy(is, writer);
        writer.close();
        downloaded = true;
        // update last modified of file with http header date from server
        if (serverModified != null) {
          downloadTo.setLastModified(serverModified.getTime());
        }
      }

      // close http connection
      entity.consumeContent();

      log.debug("Successfully downloaded " + url + " to " + downloadTo.getAbsolutePath());
    }

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
