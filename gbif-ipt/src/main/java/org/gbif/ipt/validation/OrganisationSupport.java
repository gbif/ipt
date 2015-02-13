/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.doi.service.ServiceConfig;
import org.gbif.doi.service.datacite.DataCiteService;
import org.gbif.doi.service.ezid.EzidService;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.metadata.eml.Agent;

import java.util.Date;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class OrganisationSupport {

  // logging
  private static final Logger LOG = Logger.getLogger(OrganisationSupport.class);

  private RegistryManager registryManager;
  private AppConfig cfg;
  private DefaultHttpClient client;

  @Inject
  public OrganisationSupport(RegistryManager registryManager, AppConfig cfg, DefaultHttpClient client) {
    this.registryManager = registryManager;
    this.cfg = cfg;
    this.client = client;
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

    // validate that if any DOI registration agency account fields were entered, that they are all present
    if (agency != null || agencyUsername != null || agencyPassword != null || prefix != null) {

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
        if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.DEVELOPMENT && !Constants.TEST_DOI_PREFIX
          .equalsIgnoreCase(prefix) && !Constants.EZID_TEST_DOI_SHOULDER.equalsIgnoreCase(prefix)) {
          action.addActionWarning(action.getText("validation.organisation.doiPrefix.invalid.testMode"));
        }
        // running IPT in production, the test DOI prefix cannot be used
        else if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.PRODUCTION && (Constants.TEST_DOI_PREFIX
          .equalsIgnoreCase(prefix) || Constants.EZID_TEST_DOI_SHOULDER.equalsIgnoreCase(prefix))) {
          valid = false;
          action.addFieldError("organisation.doiPrefix",
            action.getText("validation.organisation.doiPrefix.invalid.productionMode"));
        }
      }

      // validate if the account configuration is correct, e.g. by reserving a test DOI
      if (valid) {
        DoiService service;

        // before configuring EZID service: clear EZID session cookie otherwise connection reuses existing login
        if (agency.equals(DOIRegistrationAgency.EZID) && !client.getCookieStore().getCookies().isEmpty()) {
          client.getCookieStore().clear();
        }

        ServiceConfig cfg = new ServiceConfig(agencyUsername, agencyPassword);
        service = (agency.equals(DOIRegistrationAgency.DATACITE)) ? new DataCiteService(client, cfg) :
          new EzidService(client, cfg);

        try {
          DOI doi = DOIUtils.mintDOI(agency, prefix);
          DataCiteMetadata metadata = getTestDataCiteMetadata(doi);
          service.reserve(doi, metadata);
          // clean up
          service.delete(doi);
        } catch (InvalidMetadataException e) {
          valid = false;
          String msg = "An unexpected error occurred while trying to authenticate your " + agency.name().toLowerCase()
                       + " account. Please try again, or contact your IPT administrator for help: " + e.getMessage();
          action.addActionError(msg);
          LOG.error(msg);
        } catch (DoiException e) {
          valid = false;
          String msg = "Authentication failed! Please verify your " + agency.name().toLowerCase()
                       + " account is entered correctly and try again";
          action.addActionError(msg);
          LOG.error(msg);
        } finally {
          // in case fields were trimmed, re-save agency account values
          organisation.setAgencyAccountUsername(agencyUsername);
          organisation.setAgencyAccountPassword(agencyPassword);
          organisation.setDoiPrefix(prefix);
        }
      } else {
        LOG.debug("Not all DOI Registration agency fields were entered correctly - bypassing DOI Registration Agency validation");
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
    testOrganisation.setName("Test Organisation");
    testResource.setOrganisation(testOrganisation);
    testResource.getEml().setDateStamp(new Date());
    // create and return test DataCiteMetadata from test resource having mandatory DataCite properties
    return DataCiteMetadataBuilder.createDataCiteMetadata(doi, testResource);
  }
}
