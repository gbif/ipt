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

import com.google.inject.Inject;

/**
 * @author josecuadra
 */
public class OrganisationSupport {
  @Inject
  private RegistryManager registryManager;

  public OrganisationSupport() {
  }

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
  }
}
