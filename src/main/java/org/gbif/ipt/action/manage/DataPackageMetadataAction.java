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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.voc.DataPackageMetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class DataPackageMetadataAction extends ManagerBaseAction {

  private static final Logger LOG = LogManager.getLogger(DataPackageMetadataAction.class);

  private static final long serialVersionUID = -1669636958170716515L;

  private DataPackageMetadataSection section = DataPackageMetadataSection.BASIC_SECTION;
  private DataPackageMetadataSection next = DataPackageMetadataSection.GEOGRAPHIC_SECTION;
  private Map<String, String> organisations;

  @Inject
  public DataPackageMetadataAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                   ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  // TODO: 13/10/2022 implement prepare
  @Override
  public void prepare() {
    super.prepare();
    if (session.get(Constants.SESSION_USER) == null) {
      return;
    }

    // take the section parameter from the requested url
    section = DataPackageMetadataSection.fromName(StringUtils.substringBetween(req.getRequestURI(), "datapackage-metadata-", "."));

    switch (section) {
      case BASIC_SECTION:
        // load organisations map
        loadOrganisations();

        if (isHttpPost()) {
          // if IPT isn't registered there are no publishing organisations to choose from, so set to "No organisation"
          if (getRegisteredIpt() == null && getDefaultOrganisation() != null) {
            resource.setOrganisation(getDefaultOrganisation());
            addActionWarning(getText("manage.overview.visibility.missing.organisation"));
          }

          // publishing organisation, if provided must match organisation
          String id = getId();
          Organisation organisation = (id == null) ? null : registrationManager.get(id);
          if (organisation != null) {
            // set organisation: note organisation is locked after 1) DOI assigned, or 2) after registration with GBIF
            if (!resource.isAlreadyAssignedDoi() && !resource.isRegistered()) {
              resource.setOrganisation(organisation);
            }
          }
        }
        break;

      case GEOGRAPHIC_SECTION:
        break;

      case TAXONOMIC_SECTION:
        break;

      case TEMPORAL_SECTION:
        break;

      case KEYWORDS_SECTION:
        break;

      case PROJECT_SECTION:
        break;

      case OTHER_SECTION:
        break;

      default: break;
    }
  }

  @Override
  public String save() throws Exception {
    // before saving, the minimum amount of mandatory metadata must have been provided, and ALL metadata sections must
    // be valid, otherwise an error is displayed
    // TODO: 13/10/2022 implement validation
    if (true) {
      // Save metadata information (datapackage.json)
      resourceManager.saveDatapackageMetadata(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[] {getText("submenu." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);

      // progress to next section, since save succeeded
      switch (section) {
        case BASIC_SECTION:
          next = DataPackageMetadataSection.GEOGRAPHIC_SECTION;
          break;
        case GEOGRAPHIC_SECTION:
          next = DataPackageMetadataSection.TAXONOMIC_SECTION;
          break;
        case TAXONOMIC_SECTION:
          next = DataPackageMetadataSection.TEMPORAL_SECTION;
          break;
        case TEMPORAL_SECTION:
          next = DataPackageMetadataSection.KEYWORDS_SECTION;
          break;
        case KEYWORDS_SECTION:
          next = DataPackageMetadataSection.PROJECT_SECTION;
          break;
        case PROJECT_SECTION:
          next = DataPackageMetadataSection.OTHER_SECTION;
          break;
        case OTHER_SECTION:
          next = DataPackageMetadataSection.BASIC_SECTION;
          break;
        default: break;
      }
    } else {
      // stay on the same section, since save failed
      next = section;
    }

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

      // add placeholder if there is more than the default organisation "No organisation"
      if (associatedOrganisations.size() > 1) {
        organisations.put("", getText("admin.organisation.name.select"));
      }

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

  public String getNext() {
    return next.getName();
  }

  public String getSection() {
    return section.getName();
  }

  public DataPackageMetadata getMetadata() {
    return resource.getDataPackageMetadata();
  }

  /**
   * @return list of organisations associated to IPT that can publish resources
   */
  public Map<String, String> getOrganisations() {
    return organisations;
  }
}
