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
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.PhylogeneticTreeManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;
import lombok.Getter;

/**
 * Shows the phylogenetic trees of a published DwC-A version, with tips linked to the specimens citing them.
 */
public class PhylogeneticTreeAction extends PortalBaseAction {

  @Serial
  private static final long serialVersionUID = 6036418437196651502L;

  private static final Logger LOG = LogManager.getLogger(PhylogeneticTreeAction.class);

  private final PhylogeneticTreeManager treeManager;

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

  @Override
  public void prepare() {
    super.prepare();
    // if no specific version is requested, use the latest published version
    if (resource != null && version == null) {
      version = resource.getLastPublishedVersionsVersion();
    }
  }

  /**
   * Viewer page.
   */
  public String view() {
    if (resource == null || version == null || !treeManager.listPublished(resource, version).contains(id)) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Raw tree file, as published.
   */
  public String file() {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      byte[] content = treeManager.readPublished(resource, version, id);
      if (content == null) {
        return NOT_FOUND;
      }
      inputStream = new ByteArrayInputStream(content);
      filename = id;
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("Failed to read phylogenetic tree {} of resource {}", id, resource.getShortname(), e);
      return NOT_FOUND;
    }
  }

  /**
   * Tab-separated tip metadata: the specimens citing each tip of the tree.
   */
  public String tips() {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      String table = treeManager.tipMetadataTable(resource, version, id);
      if (table == null) {
        return NOT_FOUND;
      }
      inputStream = new ByteArrayInputStream(table.getBytes(StandardCharsets.UTF_8));
      filename = id + "-tips.tsv";
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("Failed to build tip metadata of tree {} of resource {}", id, resource.getShortname(), e);
      return NOT_FOUND;
    }
  }

  public String getTreeFileName() {
    return id;
  }

  public String getTreeUrl() {
    return cfg.getBaseUrl() + "/phylogeny-file.do?r=" + resource.getShortname() + "&v=" + version.toPlainString()
      + "&id=" + id;
  }

  public String getTipsUrl() {
    return cfg.getBaseUrl() + "/phylogeny-tips.do?r=" + resource.getShortname() + "&v=" + version.toPlainString()
      + "&id=" + id;
  }

  public boolean isPreview() {
    return false;
  }

  public String getBackUrl() {
    return cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "&v=" + version.toPlainString();
  }
}
