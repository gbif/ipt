/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.validation;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.doi.service.datacite.RestJsonApiDataCiteService;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.metadata.eml.Agent;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class OrganisationSupport {

  // logging
  private static final Logger LOG = LogManager.getLogger(OrganisationSupport.class);

  private RegistryManager registryManager;
  private AppConfig cfg;

  @Inject
  public OrganisationSupport(RegistryManager registryManager, AppConfig cfg) {
    this.registryManager = registryManager;
    this.cfg = cfg;
  }

  /**
   * Validate the fields entered for a new or edited Organisation. If not valid, an explanatory error message is
   * added to the action.
   *
   * @param action       action
   * @param organisation organisation
   */
  public boolean validate(BaseAction action, Organisation organisation) {
    boolean valid = true;
    if (organisation.getKey() == null || organisation.getKey().toString().length() < 1) {
      valid = false;
      action.addFieldError("organisation.key", action.getText("validation.organisation.key.required"));
    }

    if (StringUtils.trimToNull(organisation.getPassword()) == null) {
      valid = false;
      action.addFieldError("organisation.password", action.getText("validation.organisation.password.required"));
    }

    // validate if the key+password combination validates to true
    if (organisation.getKey() != null && organisation.getPassword() != null) {
      if (organisation.getKey().toString().length() > 0 && organisation.getPassword().length() > 0) {
        if (!registryManager.validateOrganisation(organisation.getKey().toString(), organisation.getPassword())) {
          valid = false;
          action.addFieldError("organisation.password", action.getText("validation.organisation.password.invalid"));
        }
      }
    }

    DOIRegistrationAgency agency = organisation.getDoiRegistrationAgency();
    String agencyUsername = StringUtils.trimToNull(organisation.getAgencyAccountUsername());
    String agencyPassword = StringUtils.trimToNull(organisation.getAgencyAccountPassword());
    String prefix = StringUtils.trimToNull(organisation.getDoiPrefix());
    boolean isAgencyAccountPrimary = organisation.isAgencyAccountPrimary();

    // validate that if any DOI registration agency account fields were entered, that they are all present
    if (agency != null || agencyUsername != null || agencyPassword != null || prefix != null || isAgencyAccountPrimary) {

      // ensure archival mode is turned ON, otherwise ensure activation of agency account fails
      if (isAgencyAccountPrimary) {
        if (!cfg.isArchivalMode()) {
          valid = false;
          action.addFieldError("organisation.agencyAccountPrimary",
            action.getText("admin.organisation.doiAccount.activated.failed"));
        }
      }

      if (agency == null) {
        valid = false;
        action.addFieldError("organisation.doiRegistrationAgency",
          action.getText("validation.organisation.doiRegistrationAgency.required"));
      }

      if (agencyUsername == null) {
        valid = false;
        action.addFieldError("organisation.agencyAccountUsername",
          action.getText("validation.organisation.agencyAccountUsername.required"));
      }

      if (agencyPassword == null) {
        valid = false;
        action.addFieldError("organisation.agencyAccountPassword",
          action.getText("validation.organisation.agencyAccountPassword.required"));
      }

      if (prefix == null) {
        valid = false;
        action.addFieldError("organisation.doiPrefix", action.getText("validation.organisation.doiPrefix.required"));
      } else if (!prefix.startsWith("10.")) {
        valid = false;
        action.addFieldError("organisation.doiPrefix", action.getText("validation.organisation.doiPrefix.invalid"));
      } else {
        // running IPT in development, the test DOI prefix is expected, but not mandatory - show warning otherwise
        if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.DEVELOPMENT
            && !Constants.TEST_DOI_PREFIX.equalsIgnoreCase(prefix)) {
          action.addActionWarning(action.getText("validation.organisation.doiPrefix.invalid.testMode"));
        }
        // running IPT in production, the test DOI prefix cannot be used
        else if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.PRODUCTION
            && Constants.TEST_DOI_PREFIX.equalsIgnoreCase(prefix)) {
          valid = false;
          action.addFieldError("organisation.doiPrefix",
            action.getText("validation.organisation.doiPrefix.invalid.productionMode"));
        }
      }

      // validate if the account configuration is correct, e.g. by reserving a test DOI
      if (valid) {
        DoiService service = new RestJsonApiDataCiteService(cfg.getDataCiteUrl(), agencyUsername, agencyPassword);

        try {
          DOI doi = DOIUtils.mintDOI(agency, prefix);
          DataCiteMetadata metadata = getTestDataCiteMetadata(doi);
          service.reserve(doi, metadata);
          // clean up
          service.delete(doi);
        } catch (DoiException e) {
          valid = false;
          String msg = action.getText("validation.organisation.agencyAccount.cantAuthenticate");
          LOG.error(msg, e);
          action.addActionError(msg);
        } finally {
          // in case fields were trimmed, re-save agency account values
          organisation.setAgencyAccountUsername(agencyUsername);
          organisation.setAgencyAccountPassword(agencyPassword);
          organisation.setDoiPrefix(prefix);
        }
      } else {
        LOG.debug(
          "Not all DOI Registration agency fields were entered correctly - bypassing DOI Registration Agency validation");
      }
    }
    return valid;
  }

  /**
   * @return DataCiteMetadata having only mandatory elements that can be used to reserve DOIs in order to test
   * authentication
   */
  private static DataCiteMetadata getTestDataCiteMetadata(DOI doi) throws InvalidMetadataException {
    Resource testResource = new Resource();
    testResource.getEml().setTitle("Test Resource");
    Agent creator = new Agent();
    creator.setFirstName("John");
    creator.setLastName("Smith");
    testResource.getEml().addCreator(creator);
    Organisation testOrganisation = new Organisation();
    testOrganisation.setKey(UUID.randomUUID().toString());
    testOrganisation.setName("Test Organisation");
    testResource.setOrganisation(testOrganisation);
    testResource.setCoreType("Occurrence");
    testResource.getEml().setDateStamp(new Date());
    // create and return test DataCiteMetadata from test resource having mandatory DataCite properties
    return DataCiteMetadataBuilder.createDataCiteMetadata(doi, testResource);
  }
}
