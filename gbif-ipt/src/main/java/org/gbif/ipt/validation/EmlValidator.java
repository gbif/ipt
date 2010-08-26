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
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnit;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;

import com.google.inject.internal.Nullable;

/**
 * @author markus
 * 
 */
public class EmlValidator extends BaseValidator {

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
    	  for(int index=0;index<eml.getAssociatedParties().size();index++) {
    		  if(!exists(eml.getAssociatedParties().get(index).getFirstName())) {
    			  action.addFieldError("eml.associatedParties["+index+"].firstName", action.getText("validation.required"));
    		  }
    		  if(!exists(eml.getAssociatedParties().get(index).getLastName())) {
    			  action.addFieldError("eml.associatedParties["+index+"].lastName", action.getText("validation.required"));
    		  }
    	  }
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
    	  int index = 0;
    	  for(TemporalCoverage tc : eml.getTemporalCoverages()) {
    		  
    		  if(tc.getType().equals(TemporalCoverageType.SINGLE_DATE)) {
    			  if(!exists(tc.getStartDate())) {
    				  action.addFieldError("eml.temporalCoverages["+index+"].startDate", action.getText("validation.required"));
    			  }
    		  }
    		  if(tc.getType().equals(TemporalCoverageType.DATE_RANGE)) {
    			  if(!exists(tc.getStartDate())) {
    				  action.addFieldError("eml.temporalCoverages["+index+"].startDate", action.getText("validation.required"));
    			  }
    			  if(!exists(tc.getEndDate())) {
    				  action.addFieldError("eml.temporalCoverages["+index+"].endDate", action.getText("validation.required"));    				  
    			  }
    		  }
    		  if(tc.getType().equals(TemporalCoverageType.FORMATION_PERIOD)) {
    			  if(!exists(tc.getFormationPeriod())) {
    				  action.addFieldError("eml.temporalCoverages["+index+"].formationPeriod", action.getText("validation.required"));
    			  }
    		  }
    		  if(tc.getType().equals(TemporalCoverageType.LIVING_TIME_PERIOD)) {
    			  if(!exists(tc.getLivingTimePeriod())) {
    				  action.addFieldError("eml.temporalCoverages["+index+"].livingTimePeriod", action.getText("validation.required"));
    			  }
    		  }    		  
    		  index++;
    	  }
      } else if (part == null || part.equalsIgnoreCase("project")) {
    	  if(!exists(eml.getProject().getPersonnel().getFirstName())) {
    		  action.addFieldError("eml.project.personnel.firstName", action.getText("validation.required"));
    	  }
    	  if(!exists(eml.getProject().getPersonnel().getLastName())) {
    		  action.addFieldError("eml.project.personnel.lastName", action.getText("validation.required"));
    	  }
    	  
      } else if (part == null || part.equalsIgnoreCase("methods")) {
    	  for(int index=1;index<eml.getSamplingMethods().size()-2;index++) {
    		  if(!exists(eml.getSamplingMethods().get(index).getStepDescription(), 5)) {
    			  action.addFieldError("eml.samplingMethods["+index+"].stepDescription", action.getText("validation.required"));
    		  }
    	  }
      } else if (part == null || part.equalsIgnoreCase("citations")) {
    	  if(!exists(eml.getCitation())) {
    		  action.addFieldError("eml.citation", action.getText("validation.required"));
    	  }
    	  for(int index = 0; index < eml.getBibliographicCitations().size(); index++) {
    		  if(!exists(eml.getBibliographicCitations().get(index))) {
    			  action.addFieldError("eml.bibliographicCitationSet.bibliographicCitations["+index+"]", action.getText("validation.required"));
    		  }
    	  }
      } else if (part == null || part.equalsIgnoreCase("collections")) {
    	  int index = 0;
    	  for(JGTICuratorialUnit jcu : eml.getJgtiCuratorialUnits()) {
    		  if(jcu.getType().equals(JGTICuratorialUnitType.COUNT_RANGE)) {    			  
    			  if(!exists(jcu.getRangeStart())) {
    				  action.addFieldError("eml.jgtiCuratorialUnits["+index+"].rangeStart", action.getText("validation.required"));
    			  }
    			  if(!exists(jcu.getRangeEnd().toString())) {
    				  action.addFieldError("eml.jgtiCuratorialUnits["+index+"].rangeEnd", action.getText("validation.required"));
    			  }
    		  }
    		  if(jcu.getType().equals(JGTICuratorialUnitType.COUNT_WITH_UNCERTAINTY)) {    			  
    			  if(!exists(jcu.getRangeMean())) {
    				  action.addFieldError("eml.jgtiCuratorialUnits["+index+"].rangeMean", action.getText("validation.required"));
    			  }
    			  if(!exists(jcu.getUncertaintyMeasure())) {
    				  action.addFieldError("eml.jgtiCuratorialUnits["+index+"].uncertaintyMeasure", action.getText("validation.required"));
    			  }
    		  }
    		  if(!exists(jcu.getUnitType())) {
				  action.addFieldError("eml.jgtiCuratorialUnits["+index+"].unitType", action.getText("validation.required"));
			  }
    		  index++;
    	  }
      } else if (part == null || part.equalsIgnoreCase("physical")) {
    	  int index = 0;
    	  for(PhysicalData pd : eml.getPhysicalData()) {
    		  if(!exists(pd.getName())) {
    			  action.addFieldError("eml.physicalData["+index+"].name", action.getText("validation.required"));
    		  }
    		  index++;
    	  }
      } else if (part == null || part.equalsIgnoreCase("keywords")) {
    	  int index = 0;
    	  for(KeywordSet ks : eml.getKeywords()) {
    		  if(!exists(ks.getKeywordThesaurus())) {
    			  action.addFieldError("eml.keywords["+index+"].keywordThesaurus", action.getText("validation.required"));
    		  }
    		  if(!exists(ks.getKeywordsString())) {
    			  action.addFieldError("eml.keywords["+index+"].keywordsString", action.getText("validation.required"));
    		  }
    		  index++;
    	  }
      } else if (part == null || part.equalsIgnoreCase("additional")) {
    	  if(!exists(eml.getDistributionUrl(), 5)) {
    		  if(!exists(eml.getDistributionUrl())) {
    			  action.addFieldError("eml.distributionUrl", action.getText("validation.required"));    			  
    		  } else {
    			  action.addFieldError("eml.distributionUrl", action.getText("validation.short"));
    		  }
    	  }
      }
    }
  }

}

