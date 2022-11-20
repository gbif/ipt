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
package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Ipt;

public class IptValidator extends BaseValidator {

  public void validate(BaseAction action, Ipt ipt) {
    validateBasic(action, ipt, "ipt");
    if (ipt.getWsPassword() == null || ipt.getWsPassword().length() < 1) {
      action.addFieldError("ipt.wsPassword", action.getText("validation.ipt.password.required"));
    }
    if (ipt.getWsPassword() != null && ipt.getWsPassword().length() < 6) {
      action.addFieldError("ipt.wsPassword", action.getText("validation.ipt.password.short"));
    }
  }

  public void validateIptPassword(BaseAction action, String password) {
    if (password != null && password.length() < 6) {
      action.addFieldError("registeredIptPassword", action.getText("validation.ipt.password.short"));
    }
  }

  public void validateUpdate(BaseAction action, Ipt ipt) {
    validateBasic(action, ipt, "registeredIpt");
  }


  private void validateBasic(BaseAction action, Ipt ipt, String fieldPrefix) {
    if (ipt.getName() == null || ipt.getName().length() < 1) {
      action.addFieldError(fieldPrefix + ".name", action.getText("validation.ipt.name.required"));
    }
    if (ipt.getName() != null && ipt.getName().length() < 3) {
      action.addFieldError(fieldPrefix + ".name", action.getText("validation.ipt.name.short"));
    }
    if (ipt.getDescription() == null || ipt.getDescription().length() < 1) {
      action.addFieldError(fieldPrefix + ".description",
        action.getText("validation.required", new String[] {action.getText("basic.description")}));
    }
    if (ipt.getPrimaryContactName() == null || ipt.getPrimaryContactName().length() < 1) {
      action.addFieldError(fieldPrefix + ".primaryContactName", action.getText("validation.ipt.contactName.required"));
    }
    if (ipt.getPrimaryContactEmail() == null || ipt.getPrimaryContactEmail().length() < 1) {
      action.addFieldError(fieldPrefix + ".primaryContactEmail", action.getText("validation.ipt.contactEmail.required"));
    }
    if (ipt.getPrimaryContactEmail() != null && ipt.getPrimaryContactEmail().length() < 6) {
      action.addFieldError(fieldPrefix + ".primaryContactEmail", action.getText("validation.ipt.contactEmail.short"));
    }
    if (!isValidEmail(ipt.getPrimaryContactEmail())) {
      action.addFieldError(fieldPrefix + ".primaryContactEmail", action.getText("validation.ipt.contactEmail.invalid"));
    }
  }
}
