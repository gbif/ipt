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

import com.opensymphony.xwork2.validator.validators.EmailValidator;

import java.util.regex.Pattern;

/**
 * @author markus
 * 
 */
public class ResourceSupport {
  private static Pattern emailPattern = Pattern.compile(EmailValidator.emailAddressPattern);

  public void validate(BaseAction action, Resource resource) {
    if (resource != null) {
      if (resource.getShortname().length() < 3) {
        action.addFieldError("resource.shortname", action.getText("validation.shortname.required"));
      }
      if (resource.getTitle().length() < 3) {
        action.addFieldError("resource.title", action.getText("validation.title.required"));
      }
    }
  }
}
