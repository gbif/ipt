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

import com.opensymphony.xwork2.validator.validators.EmailValidator;

import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class UserSupport {
  private static Pattern emailPattern = Pattern.compile(EmailValidator.emailAddressPattern);

  public void validate(BaseAction action, User user) {
    if (user != null) {
      if (user.getEmail().length() < 3) {
        action.addFieldError("user.email", action.getText("validation.email.required"));
      } else {
        if (!emailPattern.matcher(user.getEmail()).matches()) {
          action.addFieldError("user.email", action.getText("validation.email.invalid"));
        }
      }
      if (user.getFirstname().length() < 2) {
        action.addFieldError("user.firstname", action.getText("validation.firstname.required"));
      }
      if (user.getLastname().length() < 2) {
        action.addFieldError("user.lastname", action.getText("validation.lastname.required"));
      }
      if (user.getPassword().length() < 4) {
        action.addFieldError("user.password", action.getText("validation.password.required"));
      }
    }
  }
}
