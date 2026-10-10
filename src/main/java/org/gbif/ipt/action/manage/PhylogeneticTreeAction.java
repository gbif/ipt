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
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.PhylogeneticTreeManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import jakarta.inject.Inject;
import lombok.Getter;

/**
 * Manages the phylogenetic tree files of a resource: upload, delete, download and preview of the working copies.
 */
public class PhylogeneticTreeAction extends ManagerBaseAction implements UploadedFilesAware {

  @Serial
  private static final long serialVersionUID = -2290387104283736251L;

  private static final Logger LOG = LogManager.getLogger(PhylogeneticTreeAction.class);

  private final PhylogeneticTreeManager treeManager;
  private List<UploadedFile> uploadedFiles = new ArrayList<>();

  @Getter
  private InputStream inputStream;
  @Getter
  private String filename;

  @Inject
  public PhylogeneticTreeAction(SimpleTextProvider textProvider, AppConfig cfg,
                                RegistrationManager registrationManager, ResourceManager resourceManager,
                                PhylogeneticTreeManager treeManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.treeManager = treeManager;
  }

  public String upload() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (uploadedFiles == null || uploadedFiles.isEmpty() || uploadedFiles.get(0).getContent() == null) {
      addActionError(getText("manage.overview.phylogenies.upload.empty"));
      return ERROR;
    }

    UploadedFile upload = uploadedFiles.get(0);
    String name = upload.getOriginalName();
    try {
      boolean replaced = treeManager.get(resource, name) != null;
      treeManager.add(resource, (File) upload.getContent(), name);
      resource.setSourcesModified(new Date());
      saveResource();
      addActionMessage(getText(replaced ? "manage.overview.phylogenies.upload.replaced"
        : "manage.overview.phylogenies.upload.success", new String[] {name}));
      return SUCCESS;
    } catch (InvalidFilenameException e) {
      addActionError(getText("manage.overview.phylogenies.upload.invalidName", new String[] {name}));
      return ERROR;
    } catch (IOException e) {
      LOG.error("Failed to store phylogenetic tree file {}", name, e);
      addActionError(getText("manage.overview.phylogenies.upload.failed", new String[] {name}));
      return ERROR;
    }
  }

  @Override
  public String delete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (treeManager.delete(resource, id)) {
      resource.setSourcesModified(new Date());
      saveResource();
      addActionMessage(getText("manage.overview.phylogenies.delete.success", new String[] {id}));
      return SUCCESS;
    }
    addActionError(getText("manage.overview.phylogenies.delete.failed", new String[] {id}));
    return ERROR;
  }

  /**
   * Streams the working copy of a tree file.
   */
  public String download() {
    if (resource == null) {
      return NOT_FOUND;
    }
    File file = treeManager.get(resource, id);
    if (file == null) {
      return NOT_FOUND;
    }
    try {
      inputStream = new FileInputStream(file);
      filename = file.getName();
      return SUCCESS;
    } catch (FileNotFoundException e) {
      return NOT_FOUND;
    }
  }

  /**
   * Shows the working copy of a tree file in the viewer, with tips linked to the specimens of the last published
   * version, if any.
   */
  public String preview() {
    if (resource == null || treeManager.get(resource, id) == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  public String getTreeFileName() {
    return id;
  }

  public String getTreeUrl() {
    return cfg.getBaseUrl() + "/manage/phylogeny-file.do?r=" + resource.getShortname() + "&id=" + id;
  }

  /**
   * Tip metadata comes from the last published version: it's built from the published extension data.
   */
  public String getTipsUrl() {
    BigDecimal published = resource.getLastPublishedVersionsVersion();
    if (published == null) {
      return null;
    }
    return cfg.getBaseUrl() + "/phylogeny-tips.do?r=" + resource.getShortname() + "&v=" + published.toPlainString()
      + "&id=" + id;
  }

  public boolean isPreview() {
    return true;
  }

  public String getBackUrl() {
    return cfg.getBaseUrl() + "/manage/resource.do?r=" + resource.getShortname() + "#anchor-phylogenies";
  }

  @Override
  public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
    this.uploadedFiles = uploadedFiles;
  }
}
