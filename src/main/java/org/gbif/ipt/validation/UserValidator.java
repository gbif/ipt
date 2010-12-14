/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.User;

/**
 * @author markus
 * 
 */
public class UserValidator extends BaseValidator {
  public boolean validate(BaseAction action, User user) {
    boolean valid = true;
    if (user != null) {
      if (!exists(user.getEmail())) {
        action.addFieldError("user.email", action.getText("validation.email.required"));
        valid = false;
      } else {
        if (!isValidEmail(user.getEmail())) {
          valid = false;
          action.addFieldError("user.email", action.getText("validation.email.invalid"));
        }
      }
      if (!exists(user.getFirstname(), 1)) {
        valid = false;
        action.addFieldError("user.firstname", action.getText("validation.firstname.required"));
      }
      if (!exists(user.getLastname(), 1)) {
        valid = false;
        action.addFieldError("user.lastname", action.getText("validation.lastname.required"));
      }
      if (!exists(user.getPassword(), 4)) {
        valid = false;
        action.addFieldError("user.password", action.getText("validation.password.required"));
      }
    }
    return valid;
  }
}
