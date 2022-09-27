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
package org.gbif.ipt.action.portal;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Action responsible for serving config datadir ui files.
 */
public class AppFileAction extends PortalBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(AppFileAction.class);

  private final DataDir dataDir;
  protected Source source;
  private InputStream inputStream;
  protected File data;
  protected String mimeType = "text/plain";
  protected String filename;

  @Inject
  public AppFileAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                            DataDir dataDir, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.dataDir = dataDir;
  }

  @Override
  public String execute() {
    // make sure we have a download filename
    if (data == null) {
      return NOT_FOUND;
    } else if (filename == null) {
      filename = data.getName();
    }
    try {
      inputStream = new FileInputStream(data);
      // Set a Last-Modified header, even on 304 Not Modified responses.
      // Round to the nearest second, as HTTP doesn't support milliseconds.
      long lastModified = 1000 * ((data.lastModified() + 500) / 1000);
      response.setDateHeader("Last-Modified", lastModified);

      // see if we have a conditional get with If-Modified-Since header
      try {
        long since = req.getDateHeader("If-Modified-Since");
        if (since >= lastModified) {
          return NOT_MODIFIED;
        }
      } catch (IllegalArgumentException e) {
        // headers might not be formed correctly, swallow
        LOG.warn("Conditional get with If-Modified-Since header couldn't be interpreted", e);
      }
    } catch (FileNotFoundException e) {
      LOG.warn("Data dir file not found", e);
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  public File getData() {
    return data;
  }

  public String getFilename() {
    return filename;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public String getMimeType() {
    return mimeType;
  }

  // TODO: 08/09/2022 what if logo is absent (default logo)?
  public String logo() {
    for (String type : Constants.IMAGE_TYPES) {
      data = dataDir.appLogoFile(type);
      if (data.exists()) {
        mimeType = "image/" + type;
        filename = "logo" + type;
        break;
      }
    }
    if (!data.exists()) {
      // show default "empty" logo instead ?
      return NOT_FOUND;
    }
    return execute();
  }

  @Override
  public void prepare() {
    super.prepare();
  }
}
