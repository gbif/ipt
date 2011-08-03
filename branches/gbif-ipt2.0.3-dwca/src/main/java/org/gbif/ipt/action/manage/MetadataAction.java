/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.utils.CountryUtils;
import org.gbif.ipt.utils.LangUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.TemporalCoverageType;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author markus
 */
public class MetadataAction extends ManagerBaseAction {
  private ResourceValidator validatorRes = new ResourceValidator();
  private EmlValidator validatorEml = new EmlValidator();
  private String section = "basic";
  private String next = "geocoverage";
  private Map<String, String> resourceTypes;
  private Map<String, String> languages;
  private Map<String, String> countries;
  private Map<String, String> ranks;
  private Map<String, String> roles;
  private Map<String, String> preservationMethods;

  private static final List<String> sections = Arrays.asList("basic", "geocoverage", "taxcoverage", "tempcoverage",
      "keywords", "parties", "project", "methods", "citations", "collections", "physical", "additional");

  @Inject
  private VocabulariesManager vocabManager;

  /**
   * @return a map of countries
   */
  public Map<String, String> getCountries() {
    return countries;
  }

  public String getCurrentSideMenu() {
    return section;
  }

  public Eml getEml() {
    return resource.getEml();
  }

  public Map<String, String> getJGTICuratorialUnitTypeOptions() {
    return JGTICuratorialUnitType.htmlSelectMap;
  }

  public String getLanguageIso3() {
    String iso3 = LangUtils.iso3(resource.getEml().getLanguage());
    if (languages.containsKey(iso3)) {
      return iso3;
    }
    return null;
  }

  public Map<String, String> getLanguages() {
    return languages;
  }

  public String getMetadataLanguageIso3() {
    String iso3 = LangUtils.iso3(resource.getEml().getMetadataLanguage());
    if (languages.containsKey(iso3)) {
      return iso3;
    }
    return null;
  }

  public String getNext() {
    return next;
  }

  /**
   * @return a map of preservation methods
   */
  public Map<String, String> getPreservationMethods() {
    return preservationMethods;
  }

  /**
   * @return a map of Ranks
   */
  public Map<String, String> getRanks() {
    return ranks;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  public Map<String, String> getResourceTypes() {
    return resourceTypes;
  }

  public Map<String, String> getRoles() {
    return roles;
  }

  public String getSection() {
    return section;
  }

  public Map<String, String> getTempTypes() {
    return TemporalCoverageType.htmlSelectMap;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    // somehow the action params in struts.xml dont seem to work right
    // we therefore take the section parameter from the requested url
    section = StringUtils.substringBetween(req.getRequestURI(), "metadata-", ".");
    int idx = sections.indexOf(section);
    if (idx < 0 || idx == sections.size()) {
      idx = 0;
    }
    if (idx + 1 < sections.size()) {
      next = sections.get(idx + 1);
    } else {
      next = sections.get(0);
    }
    resourceTypes = vocabManager.getI18nVocab(Constants.VOCAB_URI_RESOURCE_TYPE, getLocaleLanguage(), true);
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage(), true);
    countries = new LinkedHashMap<String, String>();
    countries.put("", getText("eml.country.selection"));
    countries.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_COUNTRY, getLocaleLanguage(), true));
    ranks = new LinkedHashMap<String, String>();
    ranks.put("", getText("eml.rank.selection"));
    ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));
    roles = new LinkedHashMap<String, String>();
    roles.put("", getText("eml.agent.role.selection"));
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));
    preservationMethods = new LinkedHashMap<String, String>();
    preservationMethods.put("", getText("eml.preservation.methods.selection"));
    preservationMethods.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_PRESERVATION_METHOD, getLocaleLanguage(),
        false));

    if (resource.getEml().getContact().getAddress().getCountry() != null) {
      resource.getEml().getContact().getAddress().setCountry(
          CountryUtils.iso2(resource.getEml().getContact().getAddress().getCountry()));
    }
    if (resource.getEml().resourceCreator().getAddress().getCountry() != null) {
      resource.getEml().resourceCreator().getAddress().setCountry(
          CountryUtils.iso2(resource.getEml().resourceCreator().getAddress().getCountry()));
    }
    if (resource.getEml().getMetadataProvider().getAddress().getCountry() != null) {
      resource.getEml().getMetadataProvider().getAddress().setCountry(
          CountryUtils.iso2(resource.getEml().getMetadataProvider().getAddress().getCountry()));
    }

    if (resource.getEml().getMetadataProvider() != null && resource.getEml().getMetadataProvider().isEmpty()) {
      Agent current = new Agent();
      current.setFirstName(getCurrentUser().getFirstname());
      current.setLastName(getCurrentUser().getLastname());
      current.setEmail(getCurrentUser().getEmail());
      resource.getEml().setMetadataProvider(current);
    }

    // if it is a submission of the taxonomic coverage, clear the session list
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
      if (section.equals("collections")) {
        resource.getEml().getJgtiCuratorialUnits().clear();
      }
      if (section.equals("additional")) {
        resource.getEml().getAlternateIdentifiers().clear();
      }

    }

  }

  @Override
  public String save() throws Exception {
    resourceManager.saveEml(resource);
    if (section.equals("basic")) {
      resourceManager.save(resource);
    }
    addActionMessage(getText("manage.success", new String[]{getText("submenu." + section)}));
    return SUCCESS;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, resource);
    validatorEml.validate(this, resource.getEml(), section);
  }
}
