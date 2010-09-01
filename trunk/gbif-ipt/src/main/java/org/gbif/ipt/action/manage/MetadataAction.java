/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.Rank;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.Role;
import org.gbif.metadata.eml.TemporalCoverageType;

import com.google.inject.Inject;

/**
 * @author markus
 * 
 */
public class MetadataAction extends ManagerBaseAction {
  private ResourceValidator validatorRes = new ResourceValidator();
  private EmlValidator validatorEml = new EmlValidator();
  private String section = "basic";
  private String next = "parties";
  private Map<String, String> resourceTypes;
  private Map<String, String> languages;

  private static final List<String> sections = Arrays.asList("basic", "parties", "geocoverage", "taxcoverage",
      "tempcoverage", "project", "methods", "citations", "collections", "physical", "keywords", "additional");

  @Inject
  private VocabulariesManager vocabManager;

  public String getCurrentSideMenu() {
    return section;
  }

  public Eml getEml() {
    return resource.getEml();
  }

  public Map<String, String> getLanguages() {
    return languages;
  }

  public String getNext() {
    return next;
  }

  public Resource getResource() {
    return resource;
  }

  public Map<String, String> getResourceTypes() {
    return resourceTypes;
  }

  public Map<String, String> getRoleOptions() {
    return Role.htmlSelectMap;
  }
  
  public Map<String, String> getJGTICuratorialUnitTypeOptions() {
    return JGTICuratorialUnitType.htmlSelectMap;
  }
  
  public String getSection() {
    return section;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();    
    // somehow the action params in struts.xml dont seem to work right
    // we therefore take the section parameter from the requested url
    section = StringUtils.substringBetween(req.getRequestURI(), "-", ".");
    int idx = sections.indexOf(section);
    if (idx < 0 || idx == sections.size()) {
      idx = 0;
    }
    if(idx + 1 < sections.size()){
    	next = sections.get(idx + 1);
    }else{
    	next = sections.get(0);
    }
    resourceTypes = vocabManager.getI18nVocab(Constants.VOCAB_URI_RESOURCE_TYPE, getLocaleLanguage());
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage());
    
    // if it is a submission of the taxonomic coverage, clear the session list
    // TODO: Ask Markus if this is the preferred method of handling deletions 
    // of elements in a list
    if (isHttpPost()) {    	
    	if (section.equals("parties")) {
    		resource.getEml().getAssociatedParties().clear();
    	}
    	if (section.equals("taxcoverage")) {
    		resource.getEml().getTaxonomicCoverages().clear();
    	}
    	if (section.equals("tempcoverage")) {
    		resource.getEml().getTemporalCoverages().clear();
    	}
    	if (section.equals("methods")) {
    		resource.getEml().getMethodSteps().clear();
    	}
    	if (section.equals("citations")) {
    		resource.getEml().getBibliographicCitationSet().getBibliographicCitations().clear();
    	}
    	if (section.equals("physical")) {
    		resource.getEml().getPhysicalData().clear();
    	}
    	if (section.equals("keywords")) {
    		resource.getEml().getKeywords().clear();
    	}
    	 
    }
    
  }

  @Override
  public String save() throws Exception {
	Agent current = new Agent();
	current.setFirstName(getCurrentUser().getFirstname());
	current.setLastName(getCurrentUser().getLastname());
	current.setEmail(getCurrentUser().getEmail());
	resource.getEml().setMetadataProvider(current);
	resourceManager.saveEml(resource);
    return SUCCESS;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, resource);
    validatorEml.validate(this, resource.getEml(), section);
  }

  /** 
   * @return a map of Ranks
   */
  public Map<String, String> getRanks() {
	 Map<String, String> map = new LinkedHashMap<String, String>();
	 List<Rank> ranks = Rank.DARWIN_CORE_HIGHER_RANKS;
	 for(Rank r : ranks) {
		 map.put(r.name(), getText("rank."+r.name().toLowerCase()));
	 }
	 return map;
  }
  
  public Map<String, String> getTempTypes() {
	  return TemporalCoverageType.htmlSelectMap;
  }
  
  /*
   * TODO This method should be implemented different.
   * This is only a test version for the basic page.
   * The list of countries should be gotten using a different way. 
   */
  public Map<String, String> getCountryList() {
	  Map<String, String> countryList = new TreeMap<String, String>();
	  String c;
	  for(Locale locale : Locale.getAvailableLocales()) {
		  c = locale.getDisplayCountry();
		  if(c.length() > 0) {
			  countryList.put(locale.getISO3Country(), c);
		  }
	  }	  
	  return countryList;
  }  
  
}
