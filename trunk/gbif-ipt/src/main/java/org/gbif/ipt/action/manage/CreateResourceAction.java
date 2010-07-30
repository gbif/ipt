package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.ResourceSupport;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreateResourceAction extends POSTAction {
  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  @Inject
  private ResourceManager resourceManager;
  private String shortname;
  // file upload
  private File file;
  private String fileContentType;
  private String fileFileName;
  private ResourceSupport validator = new ResourceSupport();

  public String getShortname() {
    return shortname;
  }

  @Override
  public String save() throws IOException {
    try {
      Resource res = resourceManager.create(shortname, getCurrentUser());
      File tmpFile = uploadToTmp();
      if (tmpFile != null) {
        // TODO: read existing archive
      }
      ms.load(getCurrentUser(), res);
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", getText("validation.resource.shortname.exists", new String[]{shortname}));
      return INPUT;
    }
    return SUCCESS;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  private File uploadToTmp() {
    if (fileFileName == null) {
      return null;
    }
    // the file to upload to
    File tmpFile = dataDir.tmpFile(shortname + "-src-", ".tmp");
    log.debug("Uploading file for new resource " + shortname + " to " + tmpFile.getAbsolutePath());
    // retrieve the file data
    InputStream input;
    try {
      input = new FileInputStream(file);
      // write the file to the file specified
      OutputStream output = new FileOutputStream(tmpFile);
      IOUtils.copy(input, output);
      output.close();
      input.close();
      log.debug("Uploaded file " + fileFileName + " with content-type " + fileContentType);
    } catch (IOException e) {
      log.error(e);
      return null;
    }
    return tmpFile;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      validator.validateShortname(this, shortname);
    }
  }

}
