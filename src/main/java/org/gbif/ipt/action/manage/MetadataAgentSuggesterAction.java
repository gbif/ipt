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
import org.gbif.metadata.eml.ipt.model.Party;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.json.annotations.JSON;

public class MetadataAgentSuggesterAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -6982193399461813074L;

  public Map<String, String> suggestedResources = new HashMap<>();
  public Map<String, Agent> suggestedAgents = new HashMap<>();
  public List<SuggestedPerson> suggestedPeople = new ArrayList<>();

  @Inject
  public MetadataAgentSuggesterAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager) {
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
          .filter(agent -> agent.getFullName() != null || agent.getOrganisation() != null)
          .collect(Collectors.toMap(
              agent -> agent.getFullName() != null ? agent.getFullName() : agent.getOrganisation(),
              value -> value,
              (existing, newOne) -> existing));
    }

    return SUCCESS;
  }

  /**
   * Lists the people and organisations of the other resources the user can manage, for the contacts page's "Copy
   * from another resource" search: the parties and project personnel of each resource. The same contact information
   * in several places is listed once, with every place it is used.
   * The resource given by the r parameter (the one being edited) is left out.
   */
  public String people() {
    String currentResource = req.getParameter("r");

    for (Resource res : resourceManager.list(getCurrentUser())) {
      if (res.getEml() == null || res.getShortname().equals(currentResource)) {
        continue;
      }
      String title = StringUtils.defaultIfEmpty(res.getTitle(), res.getShortname());

      for (Party party : res.getEml().getParties()) {
        Usage usage = new Usage(title);
        usage.creator = party.isCreator();
        usage.contact = party.isContact();
        usage.metadataProvider = party.isMetadataProvider();
        usage.associatedPartyRoles = new ArrayList<>(party.getAssociatedPartyRoles());
        addSuggestedPerson(party.getAgent(), usage);
      }

      if (res.getEml().getProject() != null) {
        for (Agent personnel : res.getEml().getProject().getPersonnel()) {
          Usage usage = new Usage(title);
          usage.projectPersonnel = true;
          addSuggestedPerson(personnel, usage);
        }
      }
    }

    return SUCCESS;
  }

  private void addSuggestedPerson(Agent agent, Usage usage) {
    if (agent == null
        || (agent.getFullName() == null
            && StringUtils.isBlank(agent.getOrganisation())
            && agent.getPosition().stream().allMatch(StringUtils::isBlank))) {
      return;
    }
    for (SuggestedPerson person : suggestedPeople) {
      if (person.agent.isSameParty(agent)) {
        person.usedIn.add(usage);
        return;
      }
    }
    SuggestedPerson person = new SuggestedPerson(agent);
    person.usedIn.add(usage);
    suggestedPeople.add(person);
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

  @JSON
  public List<SuggestedPerson> getSuggestedPeople() {
    return suggestedPeople;
  }

  /** A person or organisation of another resource, with the places it is used. */
  public static class SuggestedPerson {
    private final Agent agent;
    private final List<Usage> usedIn = new ArrayList<>();

    SuggestedPerson(Agent agent) {
      this.agent = agent;
    }

    public Agent getAgent() {
      return agent;
    }

    public List<Usage> getUsedIn() {
      return usedIn;
    }
  }

  /** Where a person or organisation is used: the resource's title and the roles held there. */
  public static class Usage {
    private final String resource;
    private boolean creator;
    private boolean contact;
    private boolean metadataProvider;
    private List<String> associatedPartyRoles = new ArrayList<>();
    private boolean projectPersonnel;

    Usage(String resource) {
      this.resource = resource;
    }

    public String getResource() {
      return resource;
    }

    public boolean isCreator() {
      return creator;
    }

    public boolean isContact() {
      return contact;
    }

    public boolean isMetadataProvider() {
      return metadataProvider;
    }

    public List<String> getAssociatedPartyRoles() {
      return associatedPartyRoles;
    }

    public boolean isProjectPersonnel() {
      return projectPersonnel;
    }
  }
}
