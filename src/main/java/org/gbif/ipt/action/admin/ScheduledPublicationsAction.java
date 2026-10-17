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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin-only overview of all resources that have scheduled (auto) publishing enabled.
 * Read-only: no persistence, built in memory from the ResourceManager.
 */
public class ScheduledPublicationsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 1L;

  private final ResourceManager resourceManager;

  private List<ScheduledPublicationView> scheduledResources = new ArrayList<>();

  @Inject
  public ScheduledPublicationsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager regManager,
      ResourceManager resourceManager) {
    super(textProvider, cfg, regManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    scheduledResources = resourceManager.list().stream()
        .filter(ScheduledPublicationsAction::isAutoPublishEnabled)
        .map(ScheduledPublicationsAction::toView)
        .sorted(Comparator.comparing(ScheduledPublicationView::getNextPublished,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .collect(Collectors.toList());
    return SUCCESS;
  }

  /**
   * A resource counts as scheduled when it has an update frequency set
   * (verify against Resource#usesAutoPublishing() or equivalent in your IPT version).
   */
  static boolean isAutoPublishEnabled(Resource r) {
    return r.usesAutoPublishing();
  }

  static ScheduledPublicationView toView(Resource r) {
    ScheduledPublicationView v = new ScheduledPublicationView();
    v.setShortname(r.getShortname());
    v.setTitle(r.getTitle());
    v.setManagers(managerNames(r));
    v.setFrequency(r.getUpdateFrequency() == null ? null : r.getUpdateFrequency().getIdentifier());
    v.setLastPublished(r.getLastPublished());
    v.setNextPublished(r.getNextPublished());
    v.setRecordsPublished(r.getRecordsPublished());
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

  public List<ScheduledPublicationView> getScheduledResources() {
    return scheduledResources;
  }

  /** Flat, read-only view object for the template. */
  public static class ScheduledPublicationView {

    private String shortname;
    private String title;
    private String managers;
    private String frequency;
    private Date lastPublished;
    private Date nextPublished;
    private int recordsPublished;

    public String getShortname() { return shortname; }
    public void setShortname(String shortname) { this.shortname = shortname; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getManagers() { return managers; }
    public void setManagers(String managers) { this.managers = managers; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Date getLastPublished() { return lastPublished; }
    public void setLastPublished(Date lastPublished) { this.lastPublished = lastPublished; }

    public Date getNextPublished() { return nextPublished; }
    public void setNextPublished(Date nextPublished) { this.nextPublished = nextPublished; }

    public int getRecordsPublished() { return recordsPublished; }
    public void setRecordsPublished(int recordsPublished) { this.recordsPublished = recordsPublished; }
  }
}
