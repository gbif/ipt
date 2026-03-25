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
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.StatusReport;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;

import lombok.Getter;

public class PublishingStatusAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -3984381818825518246L;

  private final ResourceManager resourceManager;

  @Getter
  private Map<String, StatusReport> runningPublications = new HashMap<>();
  @Getter
  private Map<String, StatusReport> completedPublications = new HashMap<>();

  @Inject
  public PublishingStatusAction(SimpleTextProvider textProvider,
                                AppConfig cfg,
                                RegistrationManager registrationManager,
                                ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    Map<String, StatusReport> allReports = resourceManager.getProcessReports();

    completedPublications = allReports.entrySet().stream()
        .filter(entry -> entry.getValue().isCompleted())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    runningPublications = allReports.entrySet().stream()
        .filter(entry -> !entry.getValue().isCompleted())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return SUCCESS;
  }
}
