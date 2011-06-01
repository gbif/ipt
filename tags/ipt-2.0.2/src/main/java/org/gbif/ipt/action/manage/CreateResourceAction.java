package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.ResourceValidator;

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
  private ResourceManager resourceManager;
  @Inject
  private DataDir dataDir;
  private File file;
  private String fileContentType;
  private String fileFileName;
  private String shortname;
  private ResourceValidator validator = new ResourceValidator();

  public String getShortname() {
    return shortname;
  }

  @Override
  public String save() throws IOException {
    try {
      File tmpFile = uploadToTmp();
      Resource resource = null;
      if (tmpFile != null) {
        resource = resourceManager.create(shortname, tmpFile, getCurrentUser(), this);
      } else {
        resource = resourceManager.create(shortname, getCurrentUser());
      }
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", getText("validation.resource.shortname.exists", new String[]{shortname}));
      return INPUT;
    } catch (ImportException e) {
      log.error("Error importing the dwc archive: " + e.getMessage(), e);
      addActionError(getText("validation.resource.import.exception") + " Archive Validator: http://tools.gbif.org/dwca-validator/"); // e.getMessage()
      return INPUT;
    }
    return SUCCESS;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileContentType(String fileContentType) {
    this.fileContentType = fileContentType;
  }

  public void setFileFileName(String fileFileName) {
    this.fileFileName = fileFileName;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  private File uploadToTmp() {
    if (fileFileName == null) {
      return null;
    }
    // the file to upload to
    File tmpFile = dataDir.tmpFile(shortname, fileFileName);
    log.debug("Uploading dwc archive file for new resource " + shortname + " to " + tmpFile.getAbsolutePath());
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
