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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.BulkPublicationManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.BulkPublicationManagerImpl;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * The bulk publishing page: every resource with what changed since its last publication and whether it can be
 * published, and the running or last run.
 */
public class BulkPublicationAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -8016332755710954222L;

  private final ResourceManager resourceManager;
  private final BulkPublicationManager bulkPublicationManager;
  private final VocabulariesManager vocabManager;

  @Getter
  private final List<Row> rows = new ArrayList<>();
  @Getter
  private BulkPublication run;
  // the first page of the run's results
  @Getter
  private BulkRunPage runPage;

  @Inject
  public BulkPublicationAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      BulkPublicationManager bulkPublicationManager,
      VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.bulkPublicationManager = bulkPublicationManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() throws Exception {
    run = bulkPublicationManager.getCurrent();
    runPage = BulkRunPage.of(run, null, 1, false);

    Map<String, String> frequencies =
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    for (Resource resource : resourceManager.list()) {
      Row row = new Row(resource, bulkPublicationManager.isChanged(resource), bulkPublicationManager.notReady(resource));
      if (cfg.isAutoPublishingEnabled() && resource.usesAutoPublishing() && resource.getUpdateFrequency() != null) {
        String id = resource.getUpdateFrequency().getIdentifier();
        row.autoPublishing = frequencies.getOrDefault(id, id);
      }
      rows.add(row);
    }
    return SUCCESS;
  }

  /**
   * @return no open logs: the page renders the run with all logs closed
   */
  public Set<String> getOpenLogs() {
    return Set.of();
  }

  public boolean isRunning() {
    return run != null && !runPage.isFinished();
  }

  /**
   * One resource on the page, as it is now.
   */
  @Getter
  public static class Row {

    private final String shortname;
    private final String title;
    private final String format;
    private final PublicationStatus status;
    private final PublicationStatus pendingStatus;
    private final Date lastPublished;
    private final String lastVersion;
    private final String nextVersion;
    // in the "changed since last published" scope
    private final boolean changed;
    // what changed, by the modification dates: data, mappings, metadata
    private final List<String> changes = new ArrayList<>();
    // database or URL sources: read again at each publication, so changes can't be seen from dates
    private final boolean liveSources;
    private final BulkPublication.NotReady notReady;
    private String autoPublishing;

    Row(Resource resource, boolean changed, BulkPublication.NotReady notReady) {
      this.shortname = resource.getShortname();
      this.title = resource.getTitle() != null && !resource.getTitle().isBlank() ? resource.getTitle() : shortname;
      this.format = BulkPublicationManagerImpl.formatOf(resource);
      this.status = resource.getStatus();
      this.pendingStatus = resource.getPendingStatus() != resource.getStatus() ? resource.getPendingStatus() : null;
      this.lastPublished = resource.getLastPublished();
      this.lastVersion = resource.getLastPublishedVersionsVersion() != null
          ? resource.getLastPublishedVersionsVersion().toPlainString() : null;
      this.nextVersion = resource.getNextVersion().toPlainString();
      this.changed = changed;
      this.notReady = notReady;

      if (lastPublished != null) {
        if (after(resource.getSourcesModified())) {
          changes.add("data");
        }
        if (after(resource.getMappingsModified())) {
          changes.add("mappings");
        }
        if (resource.getMetadataModified() == null || after(resource.getMetadataModified())) {
          changes.add("metadata");
        }
      }

      // any, mapped or not, as ResourcePublicationManagerImpl.isOnlyFileSources decides what is changed
      this.liveSources = resource.getSources().stream().anyMatch(s -> s.isUrlSource() || s.isSqlSource());
    }

    private boolean after(Date date) {
      return date != null && date.after(lastPublished);
    }

    public boolean isNeverPublished() {
      return lastPublished == null;
    }
  }
}
