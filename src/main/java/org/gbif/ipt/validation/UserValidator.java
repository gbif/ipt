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
import org.gbif.ipt.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserValidator extends BaseValidator {

  private static final Map<String, String> ERROR_TRANSLATIONS = new HashMap<>();

  static {
    ERROR_TRANSLATIONS.put("Missing final '@domain'", "validation.email.error.domain.missingFinal");
    ERROR_TRANSLATIONS.put("Missing domain", "validation.email.error.domain.missing");
    ERROR_TRANSLATIONS.put("Domain starts with dot", "validation.email.error.domain.startsWithDot");
    ERROR_TRANSLATIONS.put("Domain contains control or whitespace", "validation.email.error.domain.containsControl");
    ERROR_TRANSLATIONS.put("Domain contains illegal character", "validation.email.error.domain.containsIllegal");
    ERROR_TRANSLATIONS.put("Domain contains dot-dot", "validation.email.error.domain.containsDotDot");
    ERROR_TRANSLATIONS.put("Domain ends with dot", "validation.email.error.domain.endsWithDot");
    ERROR_TRANSLATIONS.put("Missing local name", "validation.email.error.localAddress.missing");
    ERROR_TRANSLATIONS.put("Local address contains control or whitespace", "validation.email.error.localAddress.containsControl");
    ERROR_TRANSLATIONS.put("Local address contains illegal character", "validation.email.error.localAddress.containsIllegal");
  }

  public boolean validate(BaseAction action, User user) {
    return validate(action, user, true);
  }

  public boolean validate(BaseAction action, User user, boolean validatePassword) {
    boolean valid = true;
    if (user != null) {
      if (exists(user.getEmail())) {
        ValidationResult result = checkEmailValid(user.getEmail());
        if (!result.isValid()) {
          valid = false;
          action.addFieldError(
                  "user.email",
                  action.getText(ERROR_TRANSLATIONS.getOrDefault(result.getMessage(), "validation.email.invalid"))
          );
        }
      } else {
        action.addFieldError("user.email", action.getText("validation.email.required"));
        valid = false;
      }
      if (!exists(user.getFirstname(), 1)) {
        valid = false;
        action.addFieldError("user.firstname", action.getText("validation.firstname.required"));
      }
      if (!exists(user.getLastname(), 1)) {
        valid = false;
        action.addFieldError("user.lastname", action.getText("validation.lastname.required"));
      }
      if (validatePassword && !exists(user.getPassword(), 4)) {
        valid = false;
        action.addFieldError("newPassword", action.getText("validation.password.required"));
        action.addFieldError("user.password", action.getText("validation.password.required"));
      }

    }
    return valid;
  }

  public boolean validatePassword(BaseAction action, String password) {
    boolean valid = true;
    if (!exists(password, 4)) {
      valid = false;
      action.addFieldError("newPassword", action.getText("validation.password.required"));
      action.addFieldError("user.password", action.getText("validation.password.required"));
    }
    return valid;
  }
}
