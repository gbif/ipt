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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Admin-only overview of all resources that have scheduled (auto) publishing enabled.
 * Read-only: no persistence, built in memory from the ResourceManager.
 */
public class ScheduledPublicationsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 1L;

  private final ResourceManager resourceManager;
  private final ResourcePublicationManager publicationManager;
  private final VocabulariesManager vocabManager;

  @Getter
  private List<ScheduledPublicationView> scheduledResources = new ArrayList<>();
  @Getter
  private int suspendedCount;

  @Inject
  public ScheduledPublicationsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager regManager,
      ResourceManager resourceManager,
      ResourcePublicationManager publicationManager,
      VocabulariesManager vocabManager) {
    super(textProvider, cfg, regManager);
    this.resourceManager = resourceManager;
    this.publicationManager = publicationManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() {
    Map<String, String> frequencies =
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    Date now = new Date();
    // as on the manage home page: suspended only means something while auto-publishing is on; otherwise all are paused
    boolean iptAutoPublishing = cfg.isAutoPublishingEnabled();
    scheduledResources = resourceManager.list().stream()
        .filter(Resource::usesAutoPublishing)
        .map(r -> toView(r, frequencies, iptAutoPublishing && publicationManager.hasMaxProcessFailures(r, false), now))
        .sorted(Comparator.comparing(ScheduledPublicationView::getNextPublished,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
    suspendedCount = (int) scheduledResources.stream().filter(ScheduledPublicationView::isSuspended).count();
    return SUCCESS;
  }

  public boolean isAutoPublishingEnabled() {
    return cfg.isAutoPublishingEnabled();
  }

  public boolean isAutoPublishingAdminOnly() {
    return cfg.isAutoPublishingAdminOnly();
  }

  static ScheduledPublicationView toView(Resource r, Map<String, String> frequencies, boolean suspended, Date now) {
    ScheduledPublicationView v = new ScheduledPublicationView();
    v.setShortname(r.getShortname());
    v.setTitle(r.getTitle());
    v.setManagers(managerNames(r));
    if (r.getUpdateFrequency() != null) {
      String id = r.getUpdateFrequency().getIdentifier();
      v.setFrequency(frequencies.getOrDefault(id, id));
    }
    v.setLastPublished(r.getLastPublished());
    v.setNextPublished(r.getNextPublished());
    v.setRecordsPublished(r.getRecordsPublished());
    v.setSuspended(suspended);
    // a next date in the past means the scheduled run didn't happen (or hasn't finished yet)
    v.setOverdue(!suspended && r.getNextPublished() != null && r.getNextPublished().before(now));
    return v;
  }

  private static String managerNames(Resource r) {
    List<String> names = new ArrayList<>();
    User creator = r.getCreator();
    if (creator != null) {
      names.add(creator.getName());
    }
    Set<User> managers = r.getManagers();
    if (managers != null) {
      for (User m : managers) {
        if (m != null && !names.contains(m.getName())) {
          names.add(m.getName());
        }
      }
    }
    return String.join(", ", names);
  }

  /** Flat, read-only view object for the template. */
  @Getter
  @Setter
  public static class ScheduledPublicationView {
    private String shortname;
    private String title;
    private String managers;
    /** The frequency's label in the page language, e.g. "Monthly". */
    private String frequency;
    private Date lastPublished;
    private Date nextPublished;
    private long recordsPublished;
    /** Stopped after too many failed attempts in a row; publishing by hand resumes it. */
    private boolean suspended;
    private boolean overdue;
  }
}
