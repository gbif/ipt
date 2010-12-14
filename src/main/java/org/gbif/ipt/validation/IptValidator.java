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
import org.gbif.ipt.model.Ipt;

import com.google.inject.Inject;

/**
 * @author josecuadra
 */
public class IptValidator extends BaseValidator {

  public IptValidator() {
  }

  public void validate(BaseAction action, Ipt ipt) {

    if (ipt.getName() == null || ipt.getName().length() < 1) {
      action.addFieldError("ipt.name", action.getText("validation.ipt.name.required"));
    }
    if (ipt.getName() != null && ipt.getName().length() < 3) {
      action.addFieldError("ipt.name", action.getText("validation.ipt.name.short"));
    }
    if (ipt.getPrimaryContactEmail() == null || ipt.getPrimaryContactEmail().length() < 1) {
      action.addFieldError("ipt.primaryContactEmail", action.getText("validation.ipt.contactEmail.required"));
    }
    if (!isValidEmail(ipt.getPrimaryContactEmail())) {
      action.addFieldError("ipt.primaryContactEmail", action.getText("validation.ipt.contactEmail.invalid"));
    }
    if (ipt.getWsPassword() == null || ipt.getWsPassword().length() < 1) {
      action.addFieldError("ipt.wsPassword", action.getText("validation.ipt.password.required"));
    }
  }
}
