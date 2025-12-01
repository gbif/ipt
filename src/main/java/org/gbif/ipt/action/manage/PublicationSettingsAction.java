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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public class PublicationSettingsAction extends ManagerBaseAction {

  private final VocabulariesManager vocabManager;

  private Organisation organisation;
  @Getter
  private Map<String, String> organisations;
  @Getter
  private Map<String, String> autoPublishFrequencies;

  @Inject
  public PublicationSettingsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
  }

  @Override
  public void prepare() {
    super.prepare();

    // load organisations map
    loadOrganisations();

    // publishing organization, if provided, must match organization
    organisation = StringUtils.isEmpty(id) ? null : registrationManager.get(id);

    // auto publish frequencies
    autoPublishFrequencies = new LinkedHashMap<>();

    Map<String, String> filteredFrequencies =
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false);
    MapUtils.removeNonMatchingKeys(filteredFrequencies, MaintenanceUpdateFrequency.NON_ZERO_DAYS_UPDATE_PERIODS);
    autoPublishFrequencies.putAll(filteredFrequencies);
  }

  @Override
  public String save() {
    // publishing organisation - mandatory
    if (StringUtils.isEmpty(id)) {
      addFieldError("id",
          getText(
              "validation.required",
              new String[]{getText("portal.home.organisation")}));

      return INPUT;
    } else if (registrationManager.get(id) == null) {
      addFieldError("id",
          getText(
              "eml.publishingOrganisation.notFound",
              resource.getOrganisation().getKey().toString()));

      return INPUT;
    }

    if (organisation != null) {
      // set organisation, note organisation is locked after:
      // 1) DOI assigned
      // 2) after registration with GBIF
      if (!resource.isAlreadyAssignedDoi() && !resource.isRegistered()) {
        resource.setOrganisation(organisation);
        resourceManager.save(resource);
        // TODO: set publisher and save EML too?
//        resource.getEml().setPublisher(organisation.getKey().toString(), organisation.getName());
//        resourceManager.saveEml(resource);
      }
    }

    // TODO: add a success message
    return SUCCESS;
  }

  /**
   * Populate organisations dropdown options/list, with placeholder option, followed by list of organisations able to
   * host resources. There must be more than the default organisation "No organisation" in order to include the
   * placeholder option.
   */
  private void loadOrganisations() {
    List<Organisation> associatedOrganisations = registrationManager.list();
    organisations = new LinkedHashMap<>();
    if (!associatedOrganisations.isEmpty()) {
      organisations.put("", getText("admin.organisation.name.select"));

      // add default organisation "No organisation" as first option
      Organisation noOrganisation = getDefaultOrganisation();
      if (noOrganisation != null) {
        organisations.put(noOrganisation.getKey().toString(), getText("eml.publishingOrganisation.none"));
      }

      // then add remaining organisations in the order they have been sorted, excluding the default organisation
      for (Organisation o : associatedOrganisations) {
        if (!Constants.DEFAULT_ORG_KEY.equals(o.getKey())) {
          organisations.put(o.getKey().toString(), o.getName());
        }
      }
    }
  }
}
