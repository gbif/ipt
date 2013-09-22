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

import org.gbif.api.model.vocabulary.DatasetSubtype;
import org.gbif.api.model.vocabulary.DatasetType;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.CountryUtils;
import org.gbif.ipt.utils.LangUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.TemporalCoverageType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class MetadataAction extends ManagerBaseAction {

  private final ResourceValidator validatorRes = new ResourceValidator();
  private EmlValidator emlValidator;
  private String section = "basic";
  private String next = "geocoverage";
  private Map<String, String> languages;
  private Map<String, String> countries;
  private Map<String, String> ranks;
  private Map<String, String> roles;
  private Map<String, String> licenses;
  private Map<String, String> preservationMethods;
  private Map<String, String> types;
  private Map<String, String> datasetSubtypes;
  private VocabulariesManager vocabManager;
  private String resourceHasCore;
  private Map<String, String> frequencies;

  // to group dataset subtype vocabulary keys
  private List<String> checklistSubtypeKeys;
  private List<String> occurrenceSubtypeKeys;

  private static final List<String> SECTIONS = Arrays
    .asList("basic", "geocoverage", "taxcoverage", "tempcoverage", "keywords", "parties", "project", "methods",
      "citations", "collections", "physical", "additional");

  @Inject
  public MetadataAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
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
    return JGTICuratorialUnitType.HTML_SELECT_MAP;
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
   * Return the text of the license through the value of the map.
   */
  public String getLicenseName() {
    String licenseText = resource.getEml().getIntellectualRights();
    if (licenseText != null) {
      Set<String> keys = licenses.keySet();
      Iterator<String> it = keys.iterator();
      licenseText = licenseText.trim().toLowerCase();
      while (it.hasNext()) {
        String licenseName = it.next();
        if (licenses.get(licenseName).equalsIgnoreCase(licenseText)) {
          return licenseName;
        }
      }
    }
    return null;
  }

  public Map<String, String> getLicenses() {
    return licenses;
  }

  /**
   * Returns a Map containing dataset subtype entries. The entries returned depending on the core type.
   * For exmaple, if the core type is Occurrence, the Map will only contain occurrence dataset subtypes.
   * This method is called by Struts.
   *
   * @return Map of dataset subtypes
   */
  public Map<String, String> getListSubtypes() {
    if (resource.getCoreType() == null) {
      if (resource.getCoreTypeTerm() != null) {
        String core = resource.getCoreTypeTerm().simpleName().toLowerCase();
        if (Constants.DWC_ROWTYPE_TAXON.toLowerCase().contains(core)) {
          return getChecklistSubtypesMap();
        } else if (Constants.DWC_ROWTYPE_OCCURRENCE.toLowerCase().contains(core)) {
          return getOccurrenceSubtypesMap();
        }
      }
    } else {
      if (resource.getCoreType().equalsIgnoreCase(CoreRowType.CHECKLIST.toString())) {
        return getChecklistSubtypesMap();
      } else if (resource.getCoreType().equalsIgnoreCase(CoreRowType.OCCURRENCE.toString())) {
        return getOccurrenceSubtypesMap();
      } else if (CoreRowType.OTHER.toString().equalsIgnoreCase(resource.getCoreType())) {
        return getEmptySubtypeMap();
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

  public Map<String, String> getRoles() {
    return roles;
  }

  public String getSection() {
    return section;
  }

  public Map<String, String> getTempTypes() {
    return TemporalCoverageType.HTML_SELECT_MAP;
  }

  public Map<String, String> getTypes() {
    return types;
  }

  @Override
  public void prepare() {
    super.prepare();
    // somehow the action params in struts.xml dont seem to work right
    // we therefore take the section parameter from the requested url
    section = StringUtils.substringBetween(req.getRequestURI(), "metadata-", ".");
    int idx = SECTIONS.indexOf(section);
    if (idx < 0 || idx == SECTIONS.size()) {
      idx = 0;
    }
    next = idx + 1 < SECTIONS.size() ? SECTIONS.get(idx + 1) : SECTIONS.get(0);

    // licenses - Additional Metadata Page
    licenses = new LinkedHashMap<String, String>();
    licenses.put(getText("eml.intellectualRights.nolicenses"), "");
    licenses
      .put(getText("eml.intellectualRights.license.cczero"), getText("eml.intellectualRights.license.cczero.text"));
    licenses.put(getText("eml.intellectualRights.license.pddl"), getText("eml.intellectualRights.license.pddl.text"));
    licenses.put(getText("eml.intellectualRights.license.odcby"), getText("eml.intellectualRights.license.odcby.text"));
    licenses.put(getText("eml.intellectualRights.license.odbl"), getText("eml.intellectualRights.license.odbl.text"));

    // Dataset core type list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    types = new LinkedHashMap<String, String>();
    types.put("", getText("resource.coreType.selection"));
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    // convert all keys in Map to lowercase, in order to standardize keys across different versions of the IPT, as well
    // as to facilitate grouping of subtypes, please see groupDatasetSubtypes()
    types = MapUtils.getMapWithLowercaseKeys(types);

    // languages list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage(), true);

    // countries list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    countries = new LinkedHashMap<String, String>();
    countries.put("", getText("eml.country.selection"));
    countries.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_COUNTRY, getLocaleLanguage(), true));

    // ranks list, derived from XML vocabulary, and displayed on Taxonomic Coverage Page
    ranks = new LinkedHashMap<String, String>();
    ranks.put("", getText("eml.rank.selection"));
    ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));

    // roles list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    roles = new LinkedHashMap<String, String>();
    roles.put("", getText("eml.agent.role.selection"));
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));

    // Dataset Subtypes list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    datasetSubtypes = new LinkedHashMap<String, String>();
    datasetSubtypes.put("", getText("resource.subtype.selection"));
    datasetSubtypes.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, getLocaleLanguage(), false));
    // convert all keys in Map to lowercase, in order to standardize keys across different versions of the IPT, as well
    // as to facilitate grouping of subtypes, please see groupDatasetSubtypes()
    datasetSubtypes = MapUtils.getMapWithLowercaseKeys(datasetSubtypes);
    // group subtypes into Checklist and Occurrence - used for getOccurrenceSubtypeKeys() and getChecklistSubtypeKeys()
    groupDatasetSubtypes();

    // preservation methods list, derived from XML vocabulary, and displayed in drop-down on Collections Data Page.
    preservationMethods = new LinkedHashMap<String, String>();
    preservationMethods.put("", getText("eml.preservation.methods.selection"));
    preservationMethods
      .putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_PRESERVATION_METHOD, getLocaleLanguage(), false));

    // update frequencies list, derived from XML vocabulary, and displayed in drop-down on basic metadata page
    frequencies = new LinkedHashMap<String, String>();
    frequencies.put("", getText("resource.updateFrequency.selection"));
    frequencies.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false));

    if (resource != null && resource.getEml() != null) {
      // contact
      Agent contact = resource.getEml().getContact();
      if (contact != null) {
        if (contact.getAddress().getCountry() != null) {
          contact.getAddress().setCountry(CountryUtils.iso2(contact.getAddress().getCountry()));
        }
      }
      // creator
      Agent creator = resource.getEml().resourceCreator();
      if (creator != null) {
        if (creator.getAddress().getCountry() != null) {
          creator.getAddress().setCountry(CountryUtils.iso2(creator.getAddress().getCountry()));
        }
      }

      // metadata provider
      Agent metadataProvider = resource.getEml().getMetadataProvider();

      // create Agent equal to current user
      Agent current = new Agent();
      current.setFirstName(getCurrentUser().getFirstname());
      current.setLastName(getCurrentUser().getLastname());
      current.setEmail(getCurrentUser().getEmail());

      if (!isAgentWithoutRoleEmpty(metadataProvider)) {
        if (metadataProvider.getAddress().getCountry() != null) {
          metadataProvider.getAddress().setCountry(CountryUtils.iso2(metadataProvider.getAddress().getCountry()));
        }
      } else {
        // auto populate with current user
        resource.getEml().setMetadataProvider(current);
      }

      // auto populate user with current user
      if (!resourceManager.isEmlExisting(resource.getShortname()) && resource.getEml().getAssociatedParties().isEmpty()) {
        current.setRole("user");
        resource.getEml().getAssociatedParties().add(current);
      }

      // if it is a submission of the taxonomic coverage, clear the session list
      if (isHttpPost()) {
        if ("parties".equals(section)) {
          resource.getEml().getAssociatedParties().clear();
        }
        if ("geocoverage".equals(section)) {
          resource.getEml().getGeospatialCoverages().clear();
        }
        if ("taxcoverage".equals(section)) {
          resource.getEml().getTaxonomicCoverages().clear();
        }
        if ("tempcoverage".equals(section)) {
          resource.getEml().getTemporalCoverages().clear();
        }
        if ("methods".equals(section)) {
          resource.getEml().getMethodSteps().clear();
        }
        if ("citations".equals(section)) {
          resource.getEml().getBibliographicCitationSet().getBibliographicCitations().clear();
        }
        if ("physical".equals(section)) {
          resource.getEml().getPhysicalData().clear();
        }
        if ("keywords".equals(section)) {
          resource.getEml().getKeywords().clear();
        }
        if ("collections".equals(section)) {
          resource.getEml().getJgtiCuratorialUnits().clear();
        }
        if ("additional".equals(section)) {
          resource.getEml().getAlternateIdentifiers().clear();
        }
      }
    }
  }

  /**
   * Determine if all fields for an agent of type agentType (not agentWithRoleType) are empty.
   *
   * @param agent Agent
   *
   * @return if agent is empty or not
   */
  private boolean isAgentWithoutRoleEmpty(Agent agent) {
    if (agent != null) {
      return Strings.nullToEmpty(agent.getFirstName()).trim().isEmpty() && Strings.nullToEmpty(agent.getLastName())
        .trim().isEmpty() && Strings.nullToEmpty(agent.getOrganisation()).trim().isEmpty() && Strings
        .nullToEmpty(agent.getPosition()).trim().isEmpty() && agent.getAddress().isEmpty() && Strings
        .nullToEmpty(agent.getPhone()).trim().isEmpty() && Strings.nullToEmpty(agent.getEmail()).trim().isEmpty()
             && Strings.nullToEmpty(agent.getHomepage()).trim().isEmpty();
    }
    return true;
  }

  @Override
  public String save() throws Exception {
    //before saving, ALL metadata sections must be valid, otherwise an error is displayed
    if (emlValidator.areAllSectionsValid(this, resource.getEml())) {
      // Save metadata information (eml.xml)
      resourceManager.saveEml(resource);
      // Save resource information (resource.xml)
      resourceManager.save(resource);
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[] {getText("submenu." + section)}));
    }
    return SUCCESS;
  }

  public void setLicenses(Map<String, String> licenses) {
    this.licenses = licenses;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, resource);
    emlValidator.validate(this, resource.getEml(), section);
  }

  /**
   * A list of dataset subtypes used to populate the dataset subtype dropdown on the Basic Metadata page.
   *
   * @return list of dataset subtypes
   */
  public Map<String, String> getDatasetSubtypes() {
    return datasetSubtypes;
  }

  /**
   * Exclude all known Checklist subtypes from the complete Map of Occurrence dataset subtypes, and return it. To
   * exclude a newly added Checklist subtype, just extend the static list above. Called from Struts, so must be public.
   *
   * @return Occurrence subtypes Map
   */
  public Map<String, String> getOccurrenceSubtypesMap() {
    // exclude subtypes known to relate to Checklist type
    Map<String, String> datasetSubtypesCopy = new LinkedHashMap<String, String>(datasetSubtypes);
    for (String key : checklistSubtypeKeys) {
      if (datasetSubtypesCopy.containsKey(key)) {
        datasetSubtypesCopy.remove(key);
      }
    }
    return datasetSubtypesCopy;
  }

  /**
   * Exclude all known Occurrence subtypes from the complete Map of Checklist dataset subtypes, and return it. To
   * exclude a newly added Occurrence subtype, just extend the static list above. Called from Struts, so must be
   * public.
   *
   * @return Checklist subtypes Map
   */
  public Map<String, String> getChecklistSubtypesMap() {
    // exclude subtypes known to relate to Checklist type
    Map<String, String> datasetSubtypesCopy = new LinkedHashMap<String, String>(datasetSubtypes);
    for (String key : occurrenceSubtypeKeys) {
      if (datasetSubtypesCopy.containsKey(key)) {
        datasetSubtypesCopy.remove(key);
      }
    }
    return datasetSubtypesCopy;
  }

  /**
   * Returns a Map representing with only a single entry indicating that there is no subtype to choose from. Called
   * from Struts, so must be public.
   *
   * @return a Map representing an empty set of dataset subtypes.
   */
  public Map<String, String> getEmptySubtypeMap() {
    Map<String, String> subtypeMap = new LinkedHashMap<String, String>();
    subtypeMap.put("", getText("resource.subtype.none"));
    return subtypeMap;
  }

  /**
   * Group dataset subtypes into 2 lists: one for checklist subtypes and the other for occurrence subtype keys.
   * The way this grouping is done, is that DatasetSubtype Enum.name gets converted into vocabulary.identifier,
   * for ex: TAXONOMIC_IDENTIFIER -> taxonomiciIdentifier
   * As long as the DatasetSubtype is extended properly in accordance with the DatasetSubtype vocabulary, simply
   * updating the version of gbif-common-api dependency is the only change needed to update the subtype list.
   * This is a workaround to the limitation we currently have with our XML vocabularies, in that concepts can't be
   * grouped.
   */
  void groupDatasetSubtypes() {
    List<String> checklistKeys = new LinkedList<String>();
    List<String> occurrenceKeys = new LinkedList<String>();
    for (DatasetSubtype type : DatasetSubtype.values()) {
      if (type.getType().compareTo(DatasetType.CHECKLIST) == 0) {
        checklistKeys.add(type.name().replaceAll("_", "").toLowerCase());
      } else if (type.getType().compareTo(DatasetType.OCCURRENCE) == 0) {
        occurrenceKeys.add(type.name().replaceAll("_", "").toLowerCase());
      }
    }
    checklistSubtypeKeys = Collections.unmodifiableList(checklistKeys);
    occurrenceSubtypeKeys = Collections.unmodifiableList(occurrenceKeys);
  }

  void setDatasetSubtypes(Map<String, String> datasetSubtypes) {
    this.datasetSubtypes = datasetSubtypes;
  }

  List<String> getChecklistSubtypeKeys() {
    return checklistSubtypeKeys;
  }

  List<String> getOccurrenceSubtypeKeys() {
    return occurrenceSubtypeKeys;
  }

  /**
   * On the basic metadata page, this variable determines whether the core type dropdown is
   * disabled or not.
   *
   * @return "true" or "false" - does the resource have a core mapping yet?
   */
  public String getResourceHasCore() {
    return (resource.hasCore()) ? "true" : "false";
  }

  /**
   * On the basic metadata page, this map populates the update frequencies dropdown. The map is derived from the
   * vocabulary {@link -linkoffline http://rs.gbif.org/vocabulary/eml/update_frequency.xml}.
   *
   * @return update frequencies map
   */
  public Map<String, String> getFrequencies() {
    return frequencies;
  }
}
