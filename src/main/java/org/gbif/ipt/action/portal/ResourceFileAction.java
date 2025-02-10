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
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Action responsible for serving datadir resource files.
 */
public class ResourceFileAction extends PortalBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(ResourceFileAction.class);

  private final DataDir dataDir;
  protected Source source;
  private InputStream inputStream;
  protected File data;
  protected String mimeType = "text/plain";
  protected String filename;

  @Inject
  public ResourceFileAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    DataDir dataDir, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.dataDir = dataDir;
  }

  /**
   * Handles DwC-A or data package archive file download request.
   * <p>
   * Specific versions can also be resolved depending on the optional parameter version "v". If no specific version is
   *  requested, the latest published version is used.
   * <p>
   * Conditional (If-Modified-Since) requests are handled in execute().
   *
   * @return Struts2 result string
   */
  public String archive() {
    if (resource == null) {
      return NOT_FOUND;
    }

    // if no specific version is requested, use the latest published version
    if (version == null) {
      BigDecimal latestVersion = resource.getLastPublishedVersionsVersion();
      if (latestVersion == null) {
        return NOT_FOUND;
      } else {
        version = latestVersion;
      }
    }

    boolean isDataPackageResource = resource.getDataPackageIdentifier() != null;
    // construct download filename
    StringBuilder sb = new StringBuilder();

    // serve file
    if (isDataPackageResource) {
      data = dataDir.resourceDataPackageFile(resource.getShortname(), version);
      sb.append(Constants.DATA_PACKAGE_NAME + "-").append(resource.getShortname());
    } else {
      data = dataDir.resourceDwcaFile(resource.getShortname(), version);
      sb.append(Constants.DWC_ARCHIVE_NAME + "-").append(resource.getShortname());
    }

    if (version != null) {
      sb.append("-v").append(version.toPlainString());
    }
    sb.append(".zip");
    filename = sb.toString();

    mimeType = "application/zip";
    return execute();
  }

  /**
   * Handles metadata file download request. Specific versions can also be resolved depending on the optional parameter
   * "version". If no specific version is requested the latest published version is used.
   *
   * @return Struts2 result string
   */
  public String metadata() {
    if (resource == null) {
      return NOT_FOUND;
    }

    // if no specific version is requested, use the latest published version
    if (version == null) {
      BigDecimal latestVersion = resource.getLastPublishedVersionsVersion();
      if (latestVersion == null) {
        return NOT_FOUND;
      } else {
        version = latestVersion;
      }
    }

    boolean isDataPackageResource = resource.getDataPackageIdentifier() != null;
    // construct download filename
    StringBuilder sb = new StringBuilder();

    // serve file
    if (isDataPackageResource) {
      if (Constants.COL_DP.equals(resource.getCoreType())) {
        data = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType(), version);
        mimeType = "text/yaml";
        sb.append("metadata-").append(resource.getShortname());
        if (version != null) {
          sb.append("-v").append(version.toPlainString());
        }
        sb.append(".yaml");
      } else {
        data = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType(), version);
        mimeType = "application/json";
        sb.append("datapackage-").append(resource.getShortname());
        if (version != null) {
          sb.append("-v").append(version.toPlainString());
        }
        sb.append(".json");
      }
    } else {
      data = dataDir.resourceEmlFile(resource.getShortname(), version);
      mimeType = "text/xml";
      sb.append("eml-").append(resource.getShortname());
      if (version != null) {
        sb.append("-v").append(version.toPlainString());
      }
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

  public String logo() {
    if (resource == null) {
      return NOT_FOUND;
    }
    for (String type : Constants.IMAGE_TYPES) {
      data = dataDir.resourceLogoFile(resource.getShortname(), type);
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

  /**
   * Retrieve the publication log file for the resource, or return NOT_FOUND if it could not be located.
   */
  public String publicationLog() {
    if (resource == null) {
      return NOT_FOUND;
    }
    data = dataDir.resourcePublicationLogFile(resource.getShortname());
    if (data.exists()) {
      mimeType = "text/log";
      filename = DataDir.PUBLICATION_LOG_FILENAME;
    } else {
      return NOT_FOUND;
    }
    return execute();
  }

  public String sourceLog() {
    if (resource == null) {
      return NOT_FOUND;
    }
    data = dataDir.sourceLogFile(resource.getShortname(), source.getName());
    if (data.exists()) {
      mimeType = "text/log";
      filename = source.getName() + ".log";
    } else {
      return NOT_FOUND;
    }
    return execute();
  }

  @Override
  public void prepare() {
    super.prepare();
    // look for source parameter
    String src = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_SOURCE));
    if (StringUtils.isNotBlank(src)) {
      source = resource.getSource(src);
    }
  }

  /**
   * Handles RTF file download request. Specific versions can also be resolved depending on the optional parameter
   * version "v". If no specific version is requested the latest published version is used.
   *
   * @return Struts2 result string
   */
  public String rtf() {
    if (resource == null) {
      return NOT_FOUND;
    }

    data = dataDir.resourceRtfFile(resource.getShortname(), version);
    mimeType = "application/rtf";

    // construct download filename
    StringBuilder sb = new StringBuilder();
    sb.append("rtf-").append(resource.getShortname());
    if (version != null) {
      sb.append("-v").append(version.toPlainString());
    }
    sb.append(".rtf");
    filename = sb.toString();
    return execute();
  }

  public String rawsource() {
    if (resource == null || id == null) {
      return NOT_FOUND;
    }

    Source source = resource.getSource(id);

    if (source instanceof FileSource) {
      FileSource frSrc = (FileSource) source;

      data = dataDir.sourceFile(resource, frSrc);
      mimeType = "application/octet-stream";

      // construct download filename
      filename = id + frSrc.getPreferredFileSuffix();
      return execute();
    }
    else {
      return NOT_FOUND;
    }
  }
}
