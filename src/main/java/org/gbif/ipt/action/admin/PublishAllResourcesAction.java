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
import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.BulkPublicationManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;

import java.io.Serial;
import java.util.List;

import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * Starts a bulk publication in the background and returns at once to the bulk publishing page, which shows its
 * progress.
 */
public class PublishAllResourcesAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -2717994514136947049L;

  private final BulkPublicationManager bulkPublicationManager;
  private List<String> selectedResources;
  private List<String> excludedResources;
  private BulkPublication.Scope publishMode;

  @Inject
  public PublishAllResourcesAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      BulkPublicationManager bulkPublicationManager) {
    super(textProvider, cfg, registrationManager);
    this.bulkPublicationManager = bulkPublicationManager;
  }

  @Override
  @RequirePost
  public String execute() throws Exception {
    BulkPublication.Scope scope = publishMode != null ? publishMode : BulkPublication.Scope.CHANGED;
    if (bulkPublicationManager.isRunning()) {
      addActionWarning(getText("admin.bulk.alreadyRunning"));
      return SUCCESS;
    }
    if (bulkPublicationManager.resourcesInScope(scope, selectedResources, excludedResources).isEmpty()) {
      addActionWarning(getText("admin.config.updateMetadata.none"));
      return SUCCESS;
    }
    try {
      bulkPublicationManager.start(scope, selectedResources, excludedResources, startedBy());
    } catch (IllegalStateException e) {
      // another admin started one meanwhile
      addActionWarning(getText("admin.bulk.alreadyRunning"));
    }
    return SUCCESS;
  }

  private String startedBy() {
    User user = getCurrentUser();
    if (user == null) {
      return null;
    }
    String name = user.getName() != null ? user.getName().trim() : "";
    return name.isEmpty() ? user.getEmail() : name;
  }

  @StrutsParameter
  public void setSelectedResources(List<String> selectedResources) {
    this.selectedResources = selectedResources;
  }

  @StrutsParameter
  public void setExcludedResources(List<String> excludedResources) {
    this.excludedResources = excludedResources;
  }

  @StrutsParameter
  public void setPublishMode(BulkPublication.Scope publishMode) {
    this.publishMode = publishMode;
  }
}
