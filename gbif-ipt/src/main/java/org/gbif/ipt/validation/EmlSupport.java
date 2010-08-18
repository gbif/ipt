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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.TaxonomicCoverage;

import com.google.inject.internal.Nullable;

/**
 * @author markus
 * 
 */
public class EmlSupport extends BaseValidator {

  public boolean isValid(Eml eml, @Nullable String part) {
    BaseAction action = new BaseAction(new SimpleTextProvider(), AppConfig.buildMock());
    validate(action, eml, part);
    if (action.hasActionErrors() || action.hasFieldErrors()) {
      return false;
    }
    return true;
  }

  /**
   * Validate an EML document, optionally only a part of it matching the infividual forms on the metadata editor:
   * "basic","parties","geocoverage","taxcoverage","tempcoverage","project","methods","citations","collections",
   * "physical","keywords","additional"
   * 
   * @param action
   * @param eml
   * @param part
   */
  public void validate(BaseAction action, Eml eml, @Nullable String part) {
    if (eml != null) {
      if (part == null || part.equalsIgnoreCase("basic")) {
        if (!exists(eml.getTitle())) {
          action.addFieldError("eml.title", action.getText("validation.required"));
        }
        if (!exists(eml.getDescription(), 5)) {
          action.addFieldError("eml.description", action.getText("validation.required"));
        }
        if (!exists(eml.getLanguage(), 2)) {
          action.addFieldError("eml.language", action.getText("validation.required"));
        }
        if (!exists(eml.getContact().getLastName())) {
          action.addFieldError("eml.contact.lastName", action.getText("validation.required"));
        }
        if (!isValidEmail(eml.getContact().getEmail())) {
          action.addFieldError("eml.contact.email", action.getText("validation.invalid"));
        }
      } else if (part == null || part.equalsIgnoreCase("parties")) {
      } else if (part == null || part.equalsIgnoreCase("geocoverage")) {	     		  
      } else if (part == null || part.equalsIgnoreCase("taxcoverage")) {
    	  int cont = 0;
    	  for(TaxonomicCoverage tc : eml.getTaxonomicCoverages()) {    		 
    		  if(!exists(tc.getDescription(), 5)) {    			  
    			  action.addFieldError("eml.taxonomicCoverages["+cont+"].description", action.getText("validation.required"));
    		  }
    		  if(!exists(tc.getTaxonKeyword().getScientificName())) {
    			  action.addFieldError("eml.taxonomicCoverages["+cont+"].taxonKeyword.scientificName", action.getText("validation.required"));
    		  }
    		  if(!exists(tc.getTaxonKeyword().getCommonName())) {
    			  action.addFieldError("eml.taxonomicCoverages["+cont+"].taxonKeyword.commonName", action.getText("validation.required"));
    		  }    		  
    		  cont++;
    	  }
      } else if (part == null || part.equalsIgnoreCase("tempcoverage")) {
      } else if (part == null || part.equalsIgnoreCase("project")) {
      } else if (part == null || part.equalsIgnoreCase("methods")) {
      } else if (part == null || part.equalsIgnoreCase("citations")) {
      } else if (part == null || part.equalsIgnoreCase("collections")) {
      } else if (part == null || part.equalsIgnoreCase("physical")) {
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
      } else if (part == null || part.equalsIgnoreCase("additional")) {
      }
    }
  }

}

