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
import org.gbif.ipt.model.Source;
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
  // config
  private Source source;
  // file upload
  private File file;
  private String fileContentType;
  private String fileFileName;
  private boolean analyze = false;

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
    addActionMessage("Deleting source " + id);
    source = ms.getConfig().getSource(id);
    if (source != null) {
      sourceManager.delete(ms.getConfig(), source);
      ms.saveConfig();
      return SUCCESS;
    } else {
      addActionError("Couldnt delete source");
      return INPUT;
    }
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public Source getSource() {
    return source;
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
    } else {
      // prepare a new, empty sql source
      source = new Source.FileSource();
      source.setResource(ms.getResource());
    }
  }

  @Override
  public String save() throws IOException {
    if (id != null) {
      if (this.analyze) {
        sourceManager.analyze(source);
      } else {
        return SUCCESS;
      }
    } else {
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
      } else if (source.getName() != null) {
        try {
          ms.getConfig().addSource(source);
        } catch (AlreadyExistingException e) {
          // shouldnt really happen as we validate this beforehand
          addActionError("Source with that name exists already");
        }
      }
    }
    ms.saveConfig();
    return INPUT;
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

  public void setSource(Source source) {
    this.source = source;
  }

  @Override
  public void validateHttpPostOnly() {
    // check if title exists already as a source
//    if (StringUtils.trimToEmpty(source.getName()).length() < 3) {
//      addFieldError("source.title", getText("validation.required"));
//    } else if (ms.getConfig().getSources().contains(source)) {
//      addFieldError("source.title", getText("manage.resource.source.unique"));
//    }
  }
}
