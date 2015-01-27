/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class OrganisationSupport {

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
   * @param action action
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

    // validate that if any DOI registration agency account fields were entered, that they are all present
    if (organisation.getDoiRegistrationAgency() != null ||
        StringUtils.trimToNull(organisation.getAgencyAccountUsername()) != null ||
        StringUtils.trimToNull(organisation.getAgencyAccountPassword()) != null ||
        StringUtils.trimToNull(organisation.getDoiPrefix()) != null ||
        organisation.isAgencyAccountPrimary()) {

      if (organisation.getDoiRegistrationAgency() == null) {
        valid = false;
        action.addFieldError("organisation.doiRegistrationAgency", action.getText("validation.organisation.doiRegistrationAgency.required"));
      }

      if (StringUtils.trimToNull(organisation.getAgencyAccountUsername()) == null) {
        valid = false;
        action.addFieldError("organisation.agencyAccountUsername", action.getText("validation.organisation.agencyAccountUsername.required"));
      }

      if (StringUtils.trimToNull(organisation.getAgencyAccountPassword()) == null) {
        valid = false;
        action.addFieldError("organisation.agencyAccountPassword", action.getText("validation.organisation.agencyAccountPassword.required"));
      }

      if (StringUtils.trimToNull(organisation.getDoiPrefix()) == null) {
        valid = false;
        action.addFieldError("organisation.doiPrefix", action.getText("validation.organisation.doiPrefix.required"));
      } else {
        // running IPT in development, the test DOI prefix is expected, but not mandatory - show warning otherwise
        if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.DEVELOPMENT
            && !Constants.TEST_DOI_PREFIX.equalsIgnoreCase(StringUtils.trim(organisation.getDoiPrefix()))) {
          action.addActionWarning(action.getText("validation.organisation.doiPrefix.invalid.testMode"));
        }
        // running IPT in production, the test DOI prefix cannot be used
        else if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.PRODUCTION
                 && Constants.TEST_DOI_PREFIX.equalsIgnoreCase(StringUtils.trim(organisation.getDoiPrefix()))) {
          valid = false;
          action.addFieldError("organisation.doiPrefix", action.getText("validation.organisation.doiPrefix.invalid.productionMode"));
        }
      }
    }
    // TODO validate if the account username and password are correct, e.g. by reserving a test DOI
    return valid;
  }
}
