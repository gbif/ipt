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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serial;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * The Action responsible for serving datadir resource files.
 */
public class ResourceFileAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -3304799051086050164L;

  private static final Logger LOG = LogManager.getLogger(ResourceFileAction.class);

  private final DataDir dataDir;
  @Getter
  private InputStream inputStream;
  @Getter
  protected File data;
  @Getter
  protected String mimeType = "text/plain";
  @Getter
  protected String filename;

  @Inject
  public ResourceFileAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                            DataDir dataDir, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.dataDir = dataDir;
  }

  /**
   * Handles metadata file download request.
   *
   * @return Struts2 result string
   */
  public String metadata() {
    if (resource == null) {
      return NOT_FOUND;
    }

    boolean isDataPackageResource = resource.getDataPackageIdentifier() != null;
    // construct download filename
    StringBuilder sb = new StringBuilder();

    // serve file
    if (isDataPackageResource) {
      if (Constants.COL_DP.equals(resource.getCoreType())) {
        data = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
        mimeType = "text/yaml";
        sb.append("metadata-").append(resource.getShortname());
        sb.append(".yaml");
      } else {
        data = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
        mimeType = "application/json";
        sb.append("datapackage-").append(resource.getShortname());
        sb.append(".json");
      }
    } else {
      data = dataDir.resourceEmlFile(resource.getShortname());
      mimeType = "text/xml";
      sb.append("eml-").append(resource.getShortname());
      sb.append(".xml");
    }

    filename = sb.toString();
    return execute();
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

  @Override
  public void prepare() {
    super.prepare();
  }
}
