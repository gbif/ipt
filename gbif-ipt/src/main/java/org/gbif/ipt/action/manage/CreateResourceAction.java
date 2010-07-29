package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

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

  public String getShortname() {
    return shortname;
  }

  @Override
  public String save() throws IOException {
    try {
      Resource res = resourceManager.create(shortname, getCurrentUser());
      if (upload()) {
        // TODO: read existing archive
      }
      ms.load(getCurrentUser(), res);
    } catch (AlreadyExistingException e) {
      addFieldError("resource.shortname", "Resource exists already");
    }
    return SUCCESS;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  private boolean upload() {
    if (fileFileName == null) {
      return false;
    }
    // the file to upload to
    File sourceFile = dataDir.resourceFile(shortname, "source/" + fileFileName);
    log.debug("Uploading file for new resource " + shortname + " to " + sourceFile.getAbsolutePath());
    // retrieve the file data
    InputStream stream;
    try {
      stream = new FileInputStream(file);
      // write the file to the file specified
      OutputStream bos = new FileOutputStream(sourceFile);
      int bytesRead;
      byte[] buffer = new byte[8192];

      while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
        bos.write(buffer, 0, bytesRead);
      }

      bos.close();
      stream.close();
    } catch (IOException e) {
      log.error(e);
      return false;
    }

    // process file.
    log.debug("Uploaded file " + fileFileName + " with content-type " + fileContentType);
    // is it a compressed archive?
    // check http content type
    if (fileContentType.endsWith("/zip")) {
      log.warn("zip file uploaded");
    } else {
      log.warn("other file uploaded: " + fileContentType);
    }
    return true;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (shortname == null || shortname.length() < 3) {
        addFieldError("shortname", "Short resource name must be unique and at least 3 characters long");
      }
    }
  }

}
