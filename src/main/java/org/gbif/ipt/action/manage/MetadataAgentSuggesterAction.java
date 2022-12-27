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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.ipt.model.Agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.json.annotations.JSON;

import com.google.inject.Inject;

public class MetadataAgentSuggesterAction extends ManagerBaseAction {

  private static final long serialVersionUID = -6982193399461813074L;

  public Map<String, String> suggestedResources = new HashMap<>();
  public Map<String, Agent> suggestedAgents = new HashMap<>();

  @Inject
  public MetadataAgentSuggesterAction(SimpleTextProvider textProvider, AppConfig cfg,
                                      RegistrationManager registrationManager, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  @Override
  public void prepare() {
    super.prepare();
  }

  @Override
  public String execute() {
    List<Resource> resources = resourceManager.list(getCurrentUser());

    suggestedResources = resources.stream()
        .collect(Collectors.toMap(Resource::getShortname, value -> StringUtils.defaultIfEmpty(value.getTitle(), value.getShortname())));

    String resourceShortname = req.getParameter("r");
    String agentType = req.getParameter("type");

    if (StringUtils.isNotEmpty(resourceShortname) && StringUtils.isNotEmpty(agentType)) {
      suggestedAgents = resources.stream()
          .filter(res -> res.getShortname().equals(resourceShortname))
          .map(res -> getMetadataAgentsByType(res, agentType))
          .flatMap(Collection::stream)
          .filter(agent -> agent.getFullName() != null)
          .collect(Collectors.toMap(Agent::getFullName, value -> value));
    }

    return SUCCESS;
  }

  private List<Agent> getMetadataAgentsByType(Resource resource, String type) {
    if (resource.getEml() == null) {
      return new ArrayList<>();
    } else if ("creators".equals(type)) {
      return resource.getEml().getCreators();
    } else if ("contacts".equals(type)) {
      return resource.getEml().getContacts();
    } else if ("metadataProviders".equals(type)) {
      return resource.getEml().getMetadataProviders();
    } else if ("associatedParties".equals(type)) {
      return resource.getEml().getAssociatedParties();
    } else if ("projectPersonnel".equals(type) && resource.getEml().getProject() != null) {
      return resource.getEml().getProject().getPersonnel();
    }

    return new ArrayList<>();
  }

  @JSON
  public Map<String, String> getSuggestedResources() {
    return suggestedResources;
  }

  @JSON
  public Map<String, Agent> getSuggestedAgents() {
    return suggestedAgents;
  }
}
