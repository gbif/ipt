/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.util.ZipUtil;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public class SourceAction extends BaseDataResourceAction implements Preparable {
  private static final long serialVersionUID = -3698917712584074200L;
  @Autowired
  private SourceInspectionManager sourceInspectionManager;
  @Autowired
  private SourceManager sourceManager;
  private final List<SourceFile> fileSources = new ArrayList<SourceFile>();
  private final List<SourceSql> sqlSources = new ArrayList<SourceSql>();
  private SourceBase source = new SourceSql();
  // file upload
  private File file;
  private String fileContentType;
  private String fileFileName;
  private Long sid;
  // preview only
  private List<String> previewHeader;
  private List<List<? extends Object>> preview;

  public String delete() {
    if (sid != null) {
      sourceManager.remove(sid);
    }
    return "delete";
  }

  /**
   * Default method - returns "input"
   * 
   * @return "input"
   */
  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public File getFile() {
    return file;
  }

  public String getFileContentType() {
    return fileContentType;
  }

  public String getFileFileName() {
    return fileFileName;
  }

  public List<SourceFile> getFileSources() {
    return fileSources;
  }

  public List<List<? extends Object>> getPreview() {
    return preview;
  }

  public List<String> getPreviewHeader() {
    return previewHeader;
  }

  public Long getSid() {
    return sid;
  }

  public SourceBase getSource() {
    return source;
  }

  public SourceFile getSourceFile() {
    return (SourceFile) source;
  }

  public List<SourceBase> getSources() {
    return ListUtils.union(fileSources, fileSources);
  }

  public List<SourceSql> getSqlSources() {
    return sqlSources;
  }

  public String list() {
    if (resource == null) {
      return RESOURCE404;
    }
    if (resourceId == null) {
      return ERROR;
    }
    List<SourceBase> sources = sourceManager.getAll(resourceId);
    for (SourceBase s : sources) {
      if (s instanceof SourceFile) {
        // try to determine filesize
        SourceFile sf = (SourceFile) s;
        File f = cfg.getSourceFile(resourceId, sf.getFilename());
        if (!f.exists() || !f.isFile()) {
          log.warn(String.format(
              "SourceFile %s for resource %s doesn't exist.", sf.getFilename(),
              resourceId));
          sf.setFileSize(-1L);
        } else {
          sf.setFileSize(f.length());
          if (sf.getDateUploaded() == null) {
            sf.setDateUploaded(new Date(f.lastModified()));
          }
        }
        fileSources.add(sf);
      } else {
        sqlSources.add((SourceSql) s);
      }
    }
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (sid != null) {
      source = sourceManager.get(sid);
    } else {
      source.setResource(resource);
    }
  }

  public String save() {
    if (cancel != null) {
      return "cancel";
    }
    if (delete != null) {
      return delete();
    }
    sourceManager.save(source);
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

  public void setSid(Long id) {
    this.sid = id;
  }

  public void setSource(SourceFile source) {
    this.source = source;
  }

  public void setSource(SourceSql source) {
    this.source = source;
  }

  public void setSourceFile(SourceFile source) {
    this.source = source;
  }

  public String sourcePreview() {
    if (source == null || source.getId() == null) {
      if (sid == null) {
        throw new NullPointerException("SourceID sid required");
      } else {
        throw new IllegalArgumentException("SourceID sid doesnt exist");
      }
    }
    log.debug("prepareSourceDataPreview");
    // get resultset preview
    try {
      // get first 5 rows into list of list for previewing data
      preview = sourceInspectionManager.getPreview(source);
      previewHeader = (List<String>) preview.remove(0);
    } catch (Exception e) {
      String msg = getText("view.sqlError");
      saveMessage(msg);
      log.warn(msg, e);
    }
    return SUCCESS;
  }

  public String updateFile() {
    sourceManager.save(source);
    return SUCCESS;
  }

  /**
   * Upload a source file
   * 
   * @return String with result (cancel, input or sucess)
   * @throws Exception if something goes wrong
   */
  public String upload() throws Exception {
    // the file to upload to
    File targetFile = cfg.getSourceFile(resource.getId(), fileFileName);
    log.debug(String.format("Uploading source file for resource %s to file %s",
        resource.getId(), targetFile.getAbsolutePath()));
    // retrieve the file data
    InputStream stream = new FileInputStream(file);

    // write the file to the file specified
    OutputStream bos = new FileOutputStream(targetFile);
    int bytesRead;
    byte[] buffer = new byte[8192];

    while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
      bos.write(buffer, 0, bytesRead);
    }

    bos.close();
    stream.close();

    // process file.
    log.debug("Uploaded file " + fileFileName + " with content-type "
        + fileContentType);
    // is it a compressed archive?
    // check http content type
    if (fileContentType.endsWith("/zip")) {
      List<File> sourceFiles = ZipUtil.unzipFile(cfg.getResourceSourceFile(
          resourceId, ""), targetFile);
      for (File sf : sourceFiles) {
        insertSourceFile(sf);
      }
    } else {
      insertSourceFile(targetFile);
    }

    // get sources data
    return list();
  }

  /*
   * Validate source file upload is a valid tab file (non-Javadoc)
   * 
   * @see com.opensymphony.xwork2.ActionSupport#validate()
   */
  public void validateSave() {
    if (source != null) {
      getFieldErrors().clear();
      try {
        sourceInspectionManager.getHeader(source);
      } catch (Exception e) {
        log.info(getText("sources.invalidSql"), e);
        this.addActionError(getText("sources.invalidSql"));
      }
    }
  }

  /*
   * Validate sql source change/insert is a valid SQL statement (non-Javadoc)
   * 
   * @see com.opensymphony.xwork2.ActionSupport#validate()
   */
  public void validateUpload() {
    if (getRequest().getMethod().equalsIgnoreCase("post")) {
      getFieldErrors().clear();
      if ("".equals(fileFileName) || file == null) {
        super.addFieldError("file", getText("errors.requiredField",
            new String[] {getText("uploadForm.file")}));
      } else if (file.length() > 104857600) {
        addActionError(getText("maxLengthExceeded"));
      }
    }
  }

  private void insertSourceFile(File srcFile) {
    SourceFile fsource = sourceManager.getSourceByFilename(resourceId,
        srcFile.getName());
    if (fsource == null) {
      // new source
      fsource = new SourceFile();
      fsource.setResource(resource);
      fsource.setFilename(srcFile.getName());
    }
    // set new upload timestamp
    fsource.setDateUploaded(new Date());

    List<String> headers = null;
    ArrayList<Object> msgParams = new ArrayList<Object>();
    msgParams.add(srcFile.getName());
    try {
      fsource.setHeaders(true);
      headers = sourceInspectionManager.getHeader(fsource);
      // see if first row columns are unique and do not contain empty fields
      Set<String> tmp = new HashSet<String>();
      for (String h : headers) {
        h = StringUtils.trimToNull(h);
        // check for unique columns, allowing 1 NULL header (often the last
        // column)
        if (tmp.contains(h)) {
          // non unique columns. Set header to false by default
          fsource.setHeaders(false);
          break;
        } else {
          tmp.add(h);
        }
      }
    } catch (Exception e) {
      log.error("Error inspecting source file " + fsource.getName(), e);
    }
    log.info(String.format(
        "Source file %s uploaded with %s columns and %s header row",
        srcFile.getAbsolutePath(), headers.size(), fsource.hasHeaders() ? "one"
            : "no"));
    if (headers != null && headers.size() > 1) {
      // save file in view mapping
      sourceManager.save(fsource);
      msgParams.add(String.valueOf(headers.size()));
      saveMessage(getText("sources.sourceFileUploaded", msgParams));
    } else {
      fsource.setResource(null);
      ArrayList<Object> params = new ArrayList<Object>();
      params.add(srcFile.getName());
      params.add(String.valueOf(headers.size()));
      saveMessage(getText("sources.sourceFileBroken", params));
    }
  }

}
