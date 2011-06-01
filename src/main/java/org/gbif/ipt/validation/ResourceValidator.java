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
import org.gbif.ipt.model.Resource;

import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class ResourceValidator {
  private static Pattern shortnamePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");

  public void validate(BaseAction action, Resource resource) {
    if (resource != null) {
      validateShortname(action, resource.getShortname());

      /*
       * This is not needed anymore. Title and description attribute belong to the Eml class,
       * and its validation is in the corresponding EmlSupport method.
       * if (resource.getTitle() == null || resource.getTitle().length() < 3) {
       * action.addFieldError("resource.title", action.getText("validation.resource.title.required"));
       * }
       */
    }
  }

  public void validateShortname(BaseAction action, String shortname) {
    if (shortname == null) {
      action.addFieldError("resource.shortname", action.getText("validation.resource.shortname.required"));
    } else {
      if (shortname.length() < 3) {
        action.addFieldError("resource.shortname", action.getText("validation.resource.shortname.invalid"));
      } else if (!shortnamePattern.matcher(shortname).matches()) {
        action.addFieldError("resource.shortname", action.getText("validation.resource.shortname.invalid"));
      }
    }
  }
}
