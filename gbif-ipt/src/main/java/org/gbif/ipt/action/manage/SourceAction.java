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

package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author markus
 * 
 */
public class SourceAction extends POSTAction {
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  @Inject
  private ResourceManagerSession ms;
  @Inject
  private SourceManager sourceManager;
  @Inject
  private DataDir dataDir;
  @Inject
  private JdbcSupport jdbcSupport;
  // config
  private Source source;
  private String jdbc;
  // file upload
  private File file;
  private String fileContentType;
  private String fileFileName;
  private boolean analyze = false;

  public String add() throws IOException {
    // new one
    if (file != null) {
      // uploaded a new file
      // copy file temporarily to keep its original name
      File tmpFile = copyToTmp();
      tmpFile.deleteOnExit();
      // create a new file source
      try {
        source = sourceManager.add(ms.getConfig(), tmpFile);
        if (ms.getConfig().getSource(source.getName()) != null) {
          addActionMessage("Replacing existing source " + source.getName());
        } else {
          addActionMessage("Added a new file source");
        }
      } catch (ImportException e) {
        // even though we have problems with this source we'll keep it for manual corrections
        log.error("Source error: " + e.getMessage(), e);
        addActionError("Source error: " + e.getMessage());
      }
    }
    return INPUT;
  }

  private File copyToTmp() {
    File tmpFile = dataDir.tmpFile(fileFileName);
    if (tmpFile.exists()) {
      tmpFile.delete();
    }
    // retrieve the file data
    try {
      InputStream input = new FileInputStream(file);
      // write the file to the file specified
      OutputStream output = new FileOutputStream(tmpFile);
      IOUtils.copy(input, output);
      output.close();
      input.close();
    } catch (IOException e) {
      log.error(e);
    }
    return tmpFile;
  }

  @Override
  public String delete() {
    if (sourceManager.delete(ms.getConfig(), ms.getConfig().getSource(id))) {
      addActionMessage("Deleted source " + id);
      ms.saveConfig();
    } else {
      addActionMessage("Couldnt delete source " + id);
    }
    return SUCCESS;
  }

  public FileSource getFileSource() {
    if (source != null && source instanceof FileSource) {
      return (FileSource) source;
    }
    return null;
  }

  public Map<String, String> getJdbcOptions() {
    return jdbcSupport.optionMap();
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public Source getSource() {
    return source;
  }

  public SqlSource getSqlSource() {
    if (source != null && source instanceof SqlSource) {
      return (SqlSource) source;
    }
    return null;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (id != null) {
      source = ms.getConfig().getSource(id);
    } else if (file == null) {
      // prepare a new, empty sql source
      source = new Source.SqlSource();
      source.setResource(ms.getResource());
      ((SqlSource) source).setJdbc(jdbcSupport.get("mysql"));
    } else {
      // prepare a new, empty sql source
      source = new Source.FileSource();
      source.setResource(ms.getResource());
    }
  }

  @Override
  public String save() throws IOException {
    // treat jdbc special
    String result = INPUT;
    if (source != null && jdbc != null) {
      ((SqlSource) source).setJdbc(jdbcSupport.get(jdbc));
    }
    // existing source
    if (id != null) {
      if (this.analyze) {
        sourceManager.analyze(source);
      } else {
        result = SUCCESS;
      }
    } else {
      // new one
      if (file != null) {
        // uploaded a new file
        // copy file temporarily to keep its original name
        File tmpFile = copyToTmp();
        tmpFile.deleteOnExit();
        // create a new file source
        try {
          source = sourceManager.add(ms.getConfig(), tmpFile);
          if (ms.getConfig().getSource(source.getName()) != null) {
            addActionMessage("Replacing existing source " + source.getName());
          } else {
            addActionMessage("Added a new file source");
          }
        } catch (ImportException e) {
          // even though we have problems with this source we'll keep it for manual corrections
          log.error("Source error: " + e.getMessage(), e);
          addActionError("Source error: " + e.getMessage());
        }
      } else {
        try {
          ms.getConfig().addSource(source);
        } catch (AlreadyExistingException e) {
          // shouldnt really happen as we validate this beforehand - still catching it here to be safe
          addActionError("Source with that name exists already");
        }
      }
    }
    ms.saveConfig();
    return result;
  }

  public void setAnalyze(String analyze) {
    if (StringUtils.trimToNull(analyze) != null) {
      this.analyze = true;
    }
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

  public void setJdbc(String jdbc) {
    this.jdbc = jdbc;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  @Override
  public void validateHttpPostOnly() {
    if (source != null) {
      // ALL SOURCES
      // check if title exists already as a source
      if (StringUtils.trimToEmpty(source.getName()).length() < 3) {
        addFieldError("source.name", getText("validation.required"));
      } else if (id == null && ms.getConfig().getSources().contains(source)) {
        addFieldError("source.name", getText("manage.source.unique"));
      }

      if (SqlSource.class.isInstance(source)) {
        // SQL SOURCE
        SqlSource src = (SqlSource) source;
        // pure ODBC connections need only a DSN, no server
        if (jdbc != null && !jdbc.equalsIgnoreCase("odbc") && StringUtils.trimToEmpty(src.getHost()).length() < 2) {
          addFieldError("sqlSource.host", getText("validation.required"));
        }
        if (StringUtils.trimToEmpty(src.getDatabase()).length() < 2) {
          addFieldError("sqlSource.database", getText("validation.required"));
        }
      } else {
        // FILE SOURCE
        FileSource src = (FileSource) source;
      }
    }
  }
}
