/**
 * 
 */
package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The Action responsible for serving datadir resource files
 * 
 */
public class ResourceFileAction extends BaseAction {
  @Inject
  private DataDir dataDir;
  @Inject
  protected ResourceManager resourceManager;
  protected String r;
  protected String s;
  protected Integer version;
  protected Resource resource;
  protected Source source;
  private InputStream inputStream;
  protected File data;
  protected String mimeType = "text/plain";
  protected String filename;

  public String dwca() {
    if (resource == null) {
      return NOT_FOUND;
    }
    // server file as set in prepare method
    data = dataDir.resourceDwcaFile(resource.getShortname());
    filename = "dwca-" + resource.getShortname() + ".zip";
    mimeType = "application/zip";
    return execute();
  }

  public String eml() {
    // if no specific version is requested use the latest published
    if (version == null) {
      version = resource.getEmlVersion();
    }
    data = dataDir.resourceEmlFile(resource.getShortname(), version);
    mimeType = "text/xml";
    filename = "eml-" + resource.getShortname() + "-v" + version + ".xml";
    return execute();
  }

  @Override
  public String execute() {
    // make sure we have a downlaod filename
    if (filename == null) {
      filename = data.getName();
    }
    try {
      inputStream = new FileInputStream(data);
    } catch (FileNotFoundException e) {
      log.warn("Data dir file not found", e);
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

  public String getR() {
    return r;
  }

  public Resource getResource() {
    return resource;
  }

  public Integer getVersion() {
    return version;
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
  
  public String publicationLog() {
      	data = dataDir.resourcePublicationLogFile(resource.getShortname());
      	if (resource.isPublished() && data.exists()) {
	        mimeType = "text/log" ;
	        filename = "publication.log";
	    }else{
	      return NOT_FOUND;
	    }
	    return execute();
  }
  
  public String sourceLog() {
    	data = dataDir.sourceLogFile(resource.getShortname(), source.getName()) ;
    	if (data.exists()) {
	        mimeType = "text/log" ;
	        filename = source.getName()+".log";
	    }else{
	      return NOT_FOUND;
	    }
	    return execute();
  }

  @Override
  public void prepare() {
    r = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
    s = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_SOURCE));
    if (r == null) {
      // try session instead
      try {
        r = (String) session.get(Constants.SESSION_RESOURCE);
      } catch (Exception e) {
        // swallow. if session is not yet opened we get an exception here...
      }
    }
    resource = resourceManager.get(r);
    source = resource.getSource(s);
  }

  public String rtf() {
    data = dataDir.resourceRtfFile(resource.getShortname());
    mimeType = "application/rtf";
    filename = resource.getShortname() + "-metadata.rtf";
    return execute();
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
