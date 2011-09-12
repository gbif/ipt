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
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.utils.CountryUtils;
import org.gbif.ipt.utils.LangUtils;
import org.gbif.ipt.utils.SubtypeUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.TemporalCoverageType;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;

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
  private Map<String, String> licenses;
  private Map<String, String> preservationMethods;
  private Map<String, String> types;
  @Inject
  private ExtensionManager extensionManager;
  private PropertyMapping mappingCoreid;
  private ExtensionMapping mapping;

  private static final List<String> sections = Arrays.asList("basic", "geocoverage", "taxcoverage", "tempcoverage",
    "keywords", "parties", "project", "methods", "citations", "collections", "physical", "additional");

  @Inject
  private VocabulariesManager vocabManager;

  // Return the static list from SubtypeUtils class
  public Map<String, String> getChecklistSubtypes() {
    return SubtypeUtils.checklistSubtypeList();
  }

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

  /*
   * Return the text of the license through the value of the map
   */
  public String getLicenseName() {
    String licenseText = resource.getEml().getIntellectualRights();
    if (licenseText != null) {
      Set<String> keys = licenses.keySet();
      Iterator<String> it = keys.iterator();
      licenseText = licenseText.trim().toLowerCase();
      while (it.hasNext()) {
        String licenseName = it.next();
        if (licenses.get(licenseName).trim().toLowerCase().equals(licenseText)) {
          return licenseName;
        }
      }
    }
    return null;
  }

  public Map<String, String> getLicenses() {
    return licenses;
  }

  // Return the static list from SubtypeUtils class
  public Map<String, String> getListSubtypes() {
    if (resource.getCoreType() != null) {
      if (resource.getCoreType().toLowerCase().equals(CoreRowType.CHECKLIST.toString().toLowerCase())) {
        return SubtypeUtils.checklistSubtypeList();
      } else if (resource.getCoreType().toLowerCase().equals(CoreRowType.OCCURRENCE.toString().toLowerCase())) {
        return SubtypeUtils.occurrenceSubtypeList();
      } else if (resource.getCoreType().toLowerCase().equals("other")) {
        return SubtypeUtils.noSubtypeList();
      }
    } else if (resource.getCoreTypeTerm() != null) {
      String core = resource.getCoreTypeTerm().simpleName().toLowerCase();
      if (Constants.DWC_ROWTYPE_TAXON.toLowerCase().contains(core)) {
        return SubtypeUtils.checklistSubtypeList();
      } else if (Constants.DWC_ROWTYPE_OCCURRENCE.toLowerCase().contains(core)) {
        return SubtypeUtils.occurrenceSubtypeList();
      }
    }
    return new LinkedHashMap<String, String>();
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

  // Return the static list from SubtypeUtils class
  public Map<String, String> getOccurrenceSubtypes() {
    return SubtypeUtils.occurrenceSubtypeList();
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

  public Map<String, String> getTypes() {
    return types;
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
    types = new LinkedHashMap<String, String>();
    types.put("", "Select a type");
    types.put(StringUtils.capitalize((CoreRowType.CHECKLIST).toString().toLowerCase()),
      StringUtils.capitalize((Resource.CoreRowType.CHECKLIST).toString().toLowerCase()));
    types.put(StringUtils.capitalize((CoreRowType.OCCURRENCE).toString().toLowerCase()),
      StringUtils.capitalize((CoreRowType.OCCURRENCE).toString().toLowerCase()));
    types.put("Other", "Other");
    licenses = new LinkedHashMap<String, String>();
    licenses.put(getText("eml.intellectualRights.nolicenses"), "");
    licenses.put(getText("eml.intellectualRights.license.cczero"),
      getText("eml.intellectualRights.license.cczero.text"));
    licenses.put(getText("eml.intellectualRights.license.pddl"), getText("eml.intellectualRights.license.pddl.text"));
    licenses.put(getText("eml.intellectualRights.license.odcby"), getText("eml.intellectualRights.license.odcby.text"));
    licenses.put(getText("eml.intellectualRights.license.odbl"), getText("eml.intellectualRights.license.odbl.text"));
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
      resource.getEml().getContact().getAddress()
        .setCountry(CountryUtils.iso2(resource.getEml().getContact().getAddress().getCountry()));
    }
    if (resource.getEml().resourceCreator().getAddress().getCountry() != null) {
      resource.getEml().resourceCreator().getAddress()
        .setCountry(CountryUtils.iso2(resource.getEml().resourceCreator().getAddress().getCountry()));
    }
    if (resource.getEml().getMetadataProvider().getAddress().getCountry() != null) {
      resource.getEml().getMetadataProvider().getAddress()
        .setCountry(CountryUtils.iso2(resource.getEml().getMetadataProvider().getAddress().getCountry()));
    }

    if (resource.getEml().getMetadataProvider() != null && resource.getEml().getMetadataProvider().isEmpty()) {
      Agent current = new Agent();
      current.setFirstName(getCurrentUser().getFirstname());
      current.setLastName(getCurrentUser().getLastname());
      current.setEmail(getCurrentUser().getEmail());
      resource.getEml().setMetadataProvider(current);
    }

    // Save the coreType to the resource when it is null
    if (resource.getCoreType() == null) {
      if (resource.getCoreTypeTerm() != null) {
        String core = resource.getCoreTypeTerm().simpleName().toLowerCase();
        if (Constants.DWC_ROWTYPE_TAXON.toLowerCase().contains(core)) {
          resource.setCoreType(StringUtils.capitalize((CoreRowType.OCCURRENCE).toString().toLowerCase()));
        } else if (Constants.DWC_ROWTYPE_OCCURRENCE.toLowerCase().contains(core)) {
          resource.setCoreType(StringUtils.capitalize((CoreRowType.CHECKLIST).toString().toLowerCase()));
        }
      }
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
    // Save metadata information (eml.xml)
    resourceManager.saveEml(resource);
    // Save resource information (resource.xml)
    resourceManager.save(resource);
    // Set resource modified date
    resource.setModified(new Date());
    addActionMessage(getText("manage.success", new String[] {getText("submenu." + section)}));
    return SUCCESS;
  }

  public void setLicenses(Map<String, String> licenses) {
    this.licenses = licenses;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, resource);
    validatorEml.validate(this, resource.getEml(), section);
  }
}
