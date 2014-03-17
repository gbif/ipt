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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * The Action responsible for serving datadir resource files.
 */
public class ResourceFileAction extends PortalBaseAction {

  // logging
  private static final Logger LOG = Logger.getLogger(ResourceFileAction.class);

  private final DataDir dataDir;
  protected ResourceManager resourceManager;
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
   * Handles DwC-A file download request. The method checks if the request is a conditional get with If-Modified-Since
   * header. If the If-Modified-Since date is greater than the last published date, the NOT_MODIFIED string is returned.
   * Specific versions can also be resolved depending on the optional parameter version "v". If no specific version is
   * requested the latest published version is used.
   * 
   * @return Struts2 result string
   */
  public String dwca() {
    if (resource == null) {
      return NOT_FOUND;
    }
    // see if we have a conditional get with If-Modified-Since header
    try {
      long since = req.getDateHeader("If-Modified-Since");
      if (since > 0 && resource.getLastPublished() != null) {
        long last = resource.getLastPublished().getTime();
        if (last < since) {
          return NOT_MODIFIED;
        }
      }
    } catch (IllegalArgumentException e) {
      // headers might not be formed correctly, swallow
      LOG.warn("Conditional get with If-Modified-Since header couldnt be interpreted", e);
    }
    // serve file as set in prepare method
    data = dataDir.resourceDwcaFile(resource.getShortname(), version);

    // construct download filename
    StringBuilder sb = new StringBuilder();
    sb.append("dwca-" + resource.getShortname());
    if (version != null) {
      sb.append("-v" + String.valueOf(version));
    }
    sb.append(".zip");
    filename = sb.toString();

    mimeType = "application/zip";
    return execute();
  }

  /**
   * Handles EML file download request. Specific versions can also be resolved depending on the optional parameter
   * "version". If no specific version is requested the latest published version is used.
   * 
   * @return Struts2 result string
   */
  public String eml() {
    data = dataDir.resourceEmlFile(resource.getShortname(), version);
    mimeType = "text/xml";

    // construct download filename
    StringBuilder sb = new StringBuilder();
    sb.append("eml-" + resource.getShortname());
    if (version != null) {
      sb.append("-v" + String.valueOf(version));
    }
    sb.append(".xml");
    filename = sb.toString();

    return execute();
  }

  @Override
  public String execute() {
    // make sure we have a download filename
    if (filename == null) {
      filename = data.getName();
    }
    try {
      inputStream = new FileInputStream(data);
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
    if (!Strings.isNullOrEmpty(src)) {
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
    data = dataDir.resourceRtfFile(resource.getShortname(), version);
    mimeType = "application/rtf";

    // construct download filename
    StringBuilder sb = new StringBuilder();
    sb.append("rtf-" + resource.getShortname());
    if (version != null) {
      sb.append("-v" + String.valueOf(version));
    }
    sb.append(".rtf");
    filename = sb.toString();

    return execute();
  }
}
