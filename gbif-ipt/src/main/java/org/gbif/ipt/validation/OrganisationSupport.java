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
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.common.base.Strings;
import com.google.inject.Inject;

public class OrganisationSupport {

  private RegistryManager registryManager;

  @Inject
  public OrganisationSupport(RegistryManager registryManager) {
    this.registryManager = registryManager;
  }

  /**
   * Validate the fields entered for a new or edited Organisation. If not valid, an explanatory error message is
   * added to the action.
   *
   * @param action action
   * @param organisation organisation
   */
  public void validate(BaseAction action, Organisation organisation) {

    if (organisation.getKey() == null || organisation.getKey().toString().length() < 1) {
      action.addFieldError("organisation.key", action.getText("validation.organisation.key.required"));
    }

    if (organisation.getPassword() == null || organisation.getPassword().length() < 1) {
      action.addFieldError("organisation.password", action.getText("validation.organisation.password.required"));
    }

    // validate if the key+password combination validates to true
    if (organisation.getKey() != null && organisation.getPassword() != null) {
      if (organisation.getKey().toString().length() > 0 && organisation.getPassword().length() > 0) {
        if (!registryManager.validateOrganisation(organisation.getKey().toString(), organisation.getPassword())) {
          action.addFieldError("organisation.password", action.getText("validation.organisation.password.invalid"));
        }
      }
    }

    // validate that if any DOI registration agency account fields were entered, that they are all present
    if (organisation.getDoiRegistrationAgency() != null ||
        Strings.emptyToNull(organisation.getAgencyAccountUsername()) != null ||
        Strings.emptyToNull(organisation.getAgencyAccountPassword()) != null ||
        Strings.emptyToNull(organisation.getDoiPrefix()) != null ||
        organisation.isAgencyAccountPrimary()) {

      if (organisation.getDoiRegistrationAgency() == null) {
        action.addFieldError("organisation.doiRegistrationAgency", action.getText("validation.organisation.doiRegistrationAgency.required"));
      }

      if (Strings.emptyToNull(organisation.getAgencyAccountUsername()) == null) {
        action.addFieldError("organisation.agencyAccountUsername", action.getText("validation.organisation.agencyAccountUsername.required"));
      }

      if (Strings.emptyToNull(organisation.getAgencyAccountPassword()) == null) {
        action.addFieldError("organisation.agencyAccountPassword", action.getText("validation.organisation.agencyAccountPassword.required"));
      }

      if (Strings.emptyToNull(organisation.getDoiPrefix()) == null) {
        action.addFieldError("organisation.doiPrefix", action.getText("validation.organisation.doiPrefix.required"));
      }
    }

    // TODO validate if the account username and password are correct
  }
}
