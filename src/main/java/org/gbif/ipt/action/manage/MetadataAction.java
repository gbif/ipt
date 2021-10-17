/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.action.manage;

import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.DatasetSubtype;
import org.gbif.common.parsers.CountryParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.MetadataSection;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.LangUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.JGTICuratorialUnitType;
import org.gbif.metadata.eml.TemporalCoverageType;
import org.gbif.metadata.eml.UserId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class MetadataAction extends ManagerBaseAction {

  private static final Logger LOG = LogManager.getLogger(MetadataAction.class);


  private final ResourceValidator validatorRes = new ResourceValidator();
  private final EmlValidator emlValidator;
  private final VocabulariesManager vocabManager;
  private static final String LICENSES_PROPFILE_PATH = "/org/gbif/metadata/eml/licenses.properties";
  private static final String LICENSE_NAME_PROPERTY_PREFIX = "license.name.";
  private static final String LICENSE_TEXT_PROPERTY_PREFIX = "license.text.";
  private static final String DIRECTORIES_PROPFILE_PATH = "/org/gbif/metadata/eml/UserDirectories.properties";

  private MetadataSection section = MetadataSection.BASIC_SECTION;
  private MetadataSection next = MetadataSection.GEOGRAPHIC_COVERAGE_SECTION;
  private Map<String, String> languages;
  private Map<String, String> countries;
  private Map<String, String> ranks;
  private Map<String, String> roles;
  private Map<String, String> preservationMethods;
  private Map<String, String> types;
  private Map<String, String> datasetSubtypes;
  private Map<String, String> frequencies;
  private Map<String, String> organisations;

  // to group dataset subtype vocabulary keys
  private List<String> checklistSubtypeKeys;
  private List<String> occurrenceSubtypeKeys;

  private static final CountryParser COUNTRY_PARSER = CountryParser.getInstance();

  private Agent primaryContact;
  private boolean doiReservedOrAssigned = false;
  private ConfigWarnings warnings;
  private static Properties licenseProperties;
  private static Properties directoriesProperties;
  private static Map<String, String> licenses;
  private static Map<String, String> licenseTexts;
  private static Map<String, String> userIdDirectories;

  @Inject
  public MetadataAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager, ConfigWarnings warnings) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
    this.warnings = warnings;
  }

  /**
   * @return a map of countries
   */
  public Map<String, String> getCountries() {
    return countries;
  }

  public String getCurrentSideMenu() {
    return section.getName();
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
   * Called from Basic Metadata page.
   *
   * Determine which license is specified in the intellectual rights. If the intellectual rights contains the name of
   * a license the IPT supports (e.g. CC-BY 4.0), the key corresponding to that license (e.g. ccby) is returned. This
   * is used to pre-select the license drop down when the basic metadata page loads.
   */
  public String getLicenseKeySelected() {
    String licenseText = resource.getEml().getIntellectualRights();
    if (StringUtils.isNotBlank(licenseText)) {
      for (Map.Entry<String, String> entry: licenses.entrySet()) {
        String licenseName = entry.getValue();
        if (StringUtils.isNotBlank(licenseName) && licenseText.contains(licenseName)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  public Map<String, String> getLicenses() {
    return licenses;
  }

  public Map<String, String> getLicenseTexts() {
    return licenseTexts;
  }

  /**
   * Returns a Map containing dataset subtype entries. The entries returned depending on the core type.
   * For example, if the core type is Occurrence, the Map will only contain occurrence dataset subtypes.
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
        } else if (Constants.DWC_ROWTYPE_EVENT.toLowerCase().contains(core)) {
          return getEmptySubtypeMap(); // because there are currently no dataset subtypes for sampling event datasets
        }
      }
    } else {
      if (resource.getCoreType().equalsIgnoreCase(CoreRowType.CHECKLIST.toString())) {
        return getChecklistSubtypesMap();
      } else if (resource.getCoreType().equalsIgnoreCase(CoreRowType.OCCURRENCE.toString())) {
        return getOccurrenceSubtypesMap();
      } else if (resource.getCoreType().equalsIgnoreCase(CoreRowType.SAMPLINGEVENT.toString())) {
        return getEmptySubtypeMap(); // because there are currently no dataset subtypes for sampling event datasets
      } else if (CoreRowType.OTHER.toString().equalsIgnoreCase(resource.getCoreType())) {
        return getEmptySubtypeMap();
      }
    }
    return new LinkedHashMap<>();
  }

  public String getMetadataLanguageIso3() {
    String iso3 = LangUtils.iso3(resource.getEml().getMetadataLanguage());
    if (languages.containsKey(iso3)) {
      return iso3;
    }
    return null;
  }

  public String getNext() {
    return next.getName();
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
    return section.getName();
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
    if (session.get(Constants.SESSION_USER) == null) {
      return;
    }

    // take the section parameter from the requested url
    section = MetadataSection.fromName(StringUtils.substringBetween(req.getRequestURI(), "metadata-", "."));

    switch (section) {
      case BASIC_SECTION:

        // Dataset core type list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
        types = new LinkedHashMap<>();
        types.put("", getText("resource.coreType.selection"));
        types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
        types = MapUtils.getMapWithLowercaseKeys(types);

        // Dataset Subtypes list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
        datasetSubtypes = new LinkedHashMap<>();
        datasetSubtypes.put("", getText("resource.subtype.selection"));
        datasetSubtypes.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, getLocaleLanguage(), false));
        datasetSubtypes = MapUtils.getMapWithLowercaseKeys(datasetSubtypes);

        // group subtypes into Checklist and Occurrence - used for getOccurrenceSubtypeKeys() and getChecklistSubtypeKeys()
        groupDatasetSubtypes();

        // update frequencies list, derived from XML vocabulary, and displayed in drop-down on basic metadata page
        frequencies = new LinkedHashMap<>();
        frequencies.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false));


        // sanitize intellectualRights - pre-v2.2 text was manually entered and may have characters that break js
        if (getEml().getIntellectualRights() != null) {
          getEml().setIntellectualRights(removeNewlineCharacters(getEml().getIntellectualRights()));
        }

        // populate agent vocabularies
        loadAgentVocabularies();

        // load license maps
        try {
          loadLicenseMaps(getText("eml.intellectualRights.nolicenses"));
        } catch (InvalidConfigException e) {
          warnings.addStartupError(e.getMessage(), e);
        }

        // load directories map
        try {
          loadDirectories(getText("eml.contact.noDirectory"));
        } catch (InvalidConfigException e) {
          warnings.addStartupError(e.getMessage(), e);
        }

        // load organisations map
        loadOrganisations();

        // if IPT isn't registered there are no publishing organisations to choose from, so set to "No organisation"
        if (getRegisteredIpt() == null && getDefaultOrganisation() != null) {
          resource.setOrganisation(getDefaultOrganisation());
          addActionWarning(getText("manage.overview.visibility.missing.organisation"));
        }

        if (isHttpPost()) {
          resource.getEml().getDescription().clear();
          resource.getEml().getContacts().clear();
          resource.getEml().getCreators().clear();
          resource.getEml().getMetadataProviders().clear();
          resource.getEml().setIntellectualRights(null);

          // publishing organisation, if provided must match organisation
          String id = getId();
          Organisation organisation = (id == null) ? null : registrationManager.get(id);
          if (organisation != null) {
            // set organisation: note organisation is locked after 1) DOI assigned, or 2) after registration with GBIF
            if (!resource.isAlreadyAssignedDoi() && !resource.isRegistered()) {
              resource.setOrganisation(organisation);
            }
          }
        }
        break;

      case GEOGRAPHIC_COVERAGE_SECTION:
        if (isHttpPost()) {
          resource.getEml().getGeospatialCoverages().clear();
        }
        break;

      case TAXANOMIC_COVERAGE_SECTION:
        // ranks list, derived from XML vocabulary, and displayed on Taxonomic Coverage Page
        ranks = new LinkedHashMap<>();
        ranks.put("", getText("eml.rank.selection"));
        ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));
        if (isHttpPost()) {
          resource.getEml().getTaxonomicCoverages().clear();
        }
        break;

      case TEMPORAL_COVERAGE_SECTION:
        if (isHttpPost()) {
          resource.getEml().getTemporalCoverages().clear();
        }
        break;

      case KEYWORDS_SECTION:
        if (isHttpPost()) {
          resource.getEml().getKeywords().clear();
        }
        break;

      case PARTIES_SECTION:
        // populate agent vocabularies
        loadAgentVocabularies();
        if (isHttpPost()) {
          resource.getEml().getAssociatedParties().clear();
        }
        break;

      case PROJECT_SECTION:
        // populate agent vocabularies
        loadAgentVocabularies();
        if (isHttpPost()) {
          resource.getEml().getProject().getPersonnel().clear();
        }
        break;

      case METHODS_SECTION:
        if (isHttpPost()) {
          resource.getEml().getMethodSteps().clear();
        }
        break;
      case CITATIONS_SECTION:
        if (isHttpPost()) {
          resource.getEml().getBibliographicCitationSet().getBibliographicCitations().clear();
        }
        doiReservedOrAssigned = hasDoiReservedOrAssigned(resource);
        if (doiReservedOrAssigned && !isHttpPost()) {
            addActionMessage(
              "The DOI reserved or registered for this resource is being used as the citation identifier");
        }
        break;

      case COLLECTIONS_SECTION:
        // preservation methods list, derived from XML vocabulary, and displayed in drop-down on Collections Data Page.
        preservationMethods = new LinkedHashMap<>();
        preservationMethods.put("", getText("eml.preservation.methods.selection"));
        preservationMethods.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_PRESERVATION_METHOD, getLocaleLanguage(), false));

        if (isHttpPost()) {
          resource.getEml().getCollections().clear();
          resource.getEml().getSpecimenPreservationMethods().clear();
          resource.getEml().getJgtiCuratorialUnits().clear();
        }
        break;

      case PHYSICAL_SECTION:
        if (isHttpPost()) {
          resource.getEml().getPhysicalData().clear();
        }
        break;

      case ADDITIONAL_SECTION:
        if (isHttpPost()) {
          resource.getEml().getAlternateIdentifiers().clear();
        }
        break;

      default: break;
    }
  }

  @Override
  public String save() throws Exception {
    // before saving, the minimum amount of mandatory metadata must have been provided, and ALL metadata sections must
    // be valid, otherwise an error is displayed
    if (emlValidator.areAllSectionsValid(this, resource)) {
      // Save metadata information (eml.xml)
      resourceManager.saveEml(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[] {getText("submenu." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);
      // progress to next section, since save succeeded
      switch (section) {
        case BASIC_SECTION:
          next = MetadataSection.GEOGRAPHIC_COVERAGE_SECTION;
          break;
        case GEOGRAPHIC_COVERAGE_SECTION:
          next = MetadataSection.TAXANOMIC_COVERAGE_SECTION;
          break;
        case TAXANOMIC_COVERAGE_SECTION:
          next = MetadataSection.TEMPORAL_COVERAGE_SECTION;
          break;
        case TEMPORAL_COVERAGE_SECTION:
          next = MetadataSection.KEYWORDS_SECTION;
          break;
        case KEYWORDS_SECTION:
          next = MetadataSection.PARTIES_SECTION;
          break;
        case PARTIES_SECTION:
          next = MetadataSection.PROJECT_SECTION;
          break;
        case PROJECT_SECTION:
          next = MetadataSection.METHODS_SECTION;
          break;
        case METHODS_SECTION:
          next = MetadataSection.CITATIONS_SECTION;
          break;
        case CITATIONS_SECTION:
          next = MetadataSection.COLLECTIONS_SECTION;
          break;
        case COLLECTIONS_SECTION:
          next = MetadataSection.PHYSICAL_SECTION;
          break;
        case PHYSICAL_SECTION:
          next = MetadataSection.ADDITIONAL_SECTION;
          break;
        case ADDITIONAL_SECTION:
          next = MetadataSection.BASIC_SECTION;
          break;
        default: break;
      }
    } else {
      // stay on the same section, since save failed
      next = section;
    }

    return SUCCESS;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, resource);
    emlValidator.validate(this, resource, section);
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
    Map<String, String> datasetSubtypesCopy = new LinkedHashMap<>(datasetSubtypes);
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
    Map<String, String> datasetSubtypesCopy = new LinkedHashMap<>(datasetSubtypes);
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
    Map<String, String> subtypeMap = new LinkedHashMap<>();
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
    List<String> occurrenceKeys = new LinkedList<>();
    for (DatasetSubtype type : DatasetSubtype.OCCURRENCE_DATASET_SUBTYPES) {
      occurrenceKeys.add(type.name().replaceAll("_", "").toLowerCase());
    }
    occurrenceSubtypeKeys = Collections.unmodifiableList(occurrenceKeys);

    List<String> checklistKeys = new LinkedList<>();
    for (DatasetSubtype type : DatasetSubtype.CHECKLIST_DATASET_SUBTYPES) {
      checklistKeys.add(type.name().replaceAll("_", "").toLowerCase());
    }
    checklistSubtypeKeys = Collections.unmodifiableList(checklistKeys);
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
    return resource.hasCore() ? "true" : "false";
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

  /**
   * @return the very first contact entered, or a new instance of Agent if no contacts exist yet. The first contact
   * entered is considered the primary contact. Other contact forms can be entered by copying from the primary contact.
   */
  public Agent getPrimaryContact() {
    return primaryContact;
  }

  public void setPrimaryContact(Agent primaryContact) {
    this.primaryContact = primaryContact;
  }

  public Map<String, String> getUserIdDirectories() {
    return userIdDirectories;
  }

  /**
   * @return Properties from licenses properties file
   *
   * @throws InvalidConfigException if licenses properties file could not be loaded
   */
  @Singleton
  public static synchronized Properties licenseProperties() throws InvalidConfigException {
    if (licenseProperties == null) {
      Properties p = new Properties();
      try {
        p.load(MetadataAction.class.getResourceAsStream(LICENSES_PROPFILE_PATH));
        LOG.debug("Loaded licenses from " + LICENSES_PROPFILE_PATH);
      } catch (IOException e) {
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE,
          "Failed to load licenses from " + LICENSES_PROPFILE_PATH);
      } finally {
        licenseProperties = p;
      }
    }
    return licenseProperties;
  }

  /**
   * @return Properties from directories properties file
   *
   * @throws InvalidConfigException if directories properties file could not be loaded
   */
  @Singleton
  public static synchronized Properties directoriesProperties() throws InvalidConfigException {
    if (directoriesProperties == null) {
      Properties p = new Properties();
      try {
        p.load(MetadataAction.class.getResourceAsStream(DIRECTORIES_PROPFILE_PATH));
        LOG.debug("Loaded directories from " + DIRECTORIES_PROPFILE_PATH);
      } catch (IOException e) {
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE,
          "Failed to load directories from " + DIRECTORIES_PROPFILE_PATH);
      } finally {
        directoriesProperties = p;
      }
    }
    return directoriesProperties;
  }

  /**
   * Load license maps: #1) license names - used to populate select on Basic Metadata Page
   *                    #2) license texts - used to populate text area on Basic Metadata Page
   *
   * @param firstOption the default select option for the license names (e.g. no license selected)
   *
   * @throws InvalidConfigException if the licenses properties file was badly configured
   */
  @Singleton
  public static synchronized void loadLicenseMaps(String firstOption) throws InvalidConfigException {
    if (licenses == null || licenseTexts == null) {
      licenses = new TreeMap<>();
      licenses.put("", (firstOption == null) ? "-" : firstOption);
      licenseTexts = new TreeMap<>();

      Properties properties = licenseProperties();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String key = StringUtils.trim((String) entry.getKey());
        String value = StringUtils.trim((String) entry.getValue());
        if (key != null && key.startsWith(LICENSE_NAME_PROPERTY_PREFIX) && value != null) {
          String keyMinusPrefix = StringUtils.trimToNull(key.replace(LICENSE_NAME_PROPERTY_PREFIX, ""));
          if (keyMinusPrefix != null) {
            String licenseText =
              StringUtils.trimToNull(properties.getProperty(LICENSE_TEXT_PROPERTY_PREFIX + keyMinusPrefix));
            if (licenseText != null) {
              licenses.put(keyMinusPrefix, value);
              licenseTexts.put(keyMinusPrefix, licenseText);
            }
          } else {
            String error = LICENSES_PROPFILE_PATH + " has been been configured wrong.";
            LOG.error(error);
            throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE, error);
          }
        }
      }
      if ((licenses.size() - 1) == 0) {
        String error = "No licenses could be loaded from " + LICENSES_PROPFILE_PATH + ". Please check configuration.";
        LOG.error(error);
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE, error);
      }
    }
  }

  /**
   * Load directories map used to populate select on Basic Metadata Page
   *
   * @param firstOption the default select option for the directory names (e.g. no directory selected)
   *
   * @throws InvalidConfigException if the directories properties file was badly configured
   */
  @Singleton
  public static synchronized void loadDirectories(String firstOption) throws InvalidConfigException {
    if (userIdDirectories == null) {
      userIdDirectories = new TreeMap<>();
      userIdDirectories.put("", (firstOption == null) ? "-" : firstOption);

      Properties properties = directoriesProperties();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String key = StringUtils.trim((String) entry.getKey());
        String value = StringUtils.trim((String) entry.getValue());
        if (key != null && value != null) {
          userIdDirectories.put(key, value);
        } else {
          String error = DIRECTORIES_PROPFILE_PATH + " has been been configured wrong.";
          LOG.error(error);
          throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE, error);
        }
      }
      if ((userIdDirectories.size() - 1) == 0) {
        String error = "No licenses could be loaded from " + DIRECTORIES_PROPFILE_PATH + ". Please check configuration.";
        LOG.error(error);
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE, error);
      }
    }
  }

  /**
   * Method that loads all vocabularies, and primary contact needed for the agent form.
   */
  private void loadAgentVocabularies() {
    // languages list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage(), true);

    // countries list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    countries = new LinkedHashMap<>();
    countries.put("", getText("eml.country.selection"));
    countries.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_COUNTRY, getLocaleLanguage(), true));

    // roles list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    roles = new LinkedHashMap<>();
    roles.put("", getText("eml.agent.role.selection"));
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));

    if (resource != null && resource.getEml() != null) {

      // create Agent equal to current user
      Agent current = new Agent();
      current.setFirstName(getCurrentUser().getFirstname());
      current.setLastName(getCurrentUser().getLastname());
      current.setEmail(getCurrentUser().getEmail());

      // contacts list
      Agent firstContact = null;
      if (!resource.getEml().getContacts().isEmpty()) {
        for (Agent contact: resource.getEml().getContacts()) {
          // capture first contact, used as primary contact to copy in contact form details
          if (firstContact == null) {
            firstContact = contact;
          }
          String countryValue = contact.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            if (result.isSuccessful()) {
              contact.getAddress().setCountry(result.getPayload().getIso2LetterCode());
            }
          }
        }
      }

      // always populate the primary contact, used to auto-fill parties and project personnel
      if (firstContact == null) {
        firstContact = new Agent();
      }
      if (firstContact.getUserIds().isEmpty()) {
        List<UserId> userIds = new ArrayList<>();
        userIds.add(new UserId());
        firstContact.setUserIds(userIds);
      }
      setPrimaryContact(firstContact);

      // creator list
      if (!resource.getEml().getCreators().isEmpty()) {
        for (Agent creator: resource.getEml().getCreators()) {
          String countryValue = creator.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            creator.getAddress().setCountry(result.getPayload().getIso2LetterCode());
          }
        }
      }
      // metadata provider list
      if (!resource.getEml().getMetadataProviders().isEmpty()) {
        for (Agent metadataProvider: resource.getEml().getMetadataProviders()) {
          String countryValue = metadataProvider.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            metadataProvider.getAddress().setCountry(result.getPayload().getIso2LetterCode());
          }
        }
      } else {
        // add current user to metadataProviders list
        resource.getEml().addMetadataProvider(current);
      }

      // auto populate user with current user if associated parties list is empty, and eml.xml hasn't been written yet
      if (!resourceManager.isEmlExisting(resource.getShortname()) && resource.getEml().getAssociatedParties().isEmpty()) {
        current.setRole("user");
        resource.getEml().getAssociatedParties().add(current);
      }
      // otherwise, ensure associated parties' country value get converted into 2 letter iso code for proper display
      else if (!resource.getEml().getAssociatedParties().isEmpty()) {
        for (Agent party : resource.getEml().getAssociatedParties()) {
          String countryValue = party.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            if (result.isSuccessful()) {
              party.getAddress().setCountry(result.getPayload().getIso2LetterCode());
            }
          }
        }
      }
    }
  }

  /**
   * @return list of organisations associated to IPT that can publish resources
   */
  public Map<String, String> getOrganisations() {
    return organisations;
  }

  /**
   * Determine whether a DOI has been reserved or registered for the resource.
   *
   * @return true if a DOI has been reserved or registered for the resource, or false otherwise
   */
  public boolean hasDoiReservedOrAssigned(Resource resource) {
    return (resource.getDoi() != null && resource.getIdentifierStatus() != IdentifierStatus.UNRESERVED);
  }

  /**
   * Used to activate, deactivate the citation identifier field on the citation metadata page.
   *
   * @return true if a DOI has been reserved or registered for the resource, or false otherwise
   */
  public boolean isDoiReservedOrAssigned() {
    return doiReservedOrAssigned;
  }

  /**
   * Populate organisations dropdown options/list, with placeholder option, followed by list of organisations able to
   * host resources. There must be more than the default organisation "No organisation" in order to include the
   * placeholder option.
   */
  private void loadOrganisations() {
    List<Organisation> associatedOrganisations = registrationManager.list();
    organisations = new LinkedHashMap<>();
    if (!associatedOrganisations.isEmpty()) {

      // add placeholder if there is more than the default organisation "No organisation"
      if (associatedOrganisations.size() > 1) {
        organisations.put("", getText("admin.organisation.name.select"));
      }

      // add default organisation "No organisation" as first option
      Organisation noOrganisation = getDefaultOrganisation();
      if (noOrganisation != null) {
        organisations.put(noOrganisation.getKey().toString(), getText("eml.publishingOrganisation.none"));
      }

      // then add remaining organisations in the order they have been sorted, excluding the default organisation
      for (Organisation o : associatedOrganisations) {
        if (!Constants.DEFAULT_ORG_KEY.equals(o.getKey())) {
          organisations.put(o.getKey().toString(), o.getName());
        }
      }
    }
  }

  /**
   * Remove all newline characters from string. Used to sanitize string for javascript, otherwise an
   * "Unexpected Token ILLEGAL" error may occur.
   */
  protected String removeNewlineCharacters(String s) {
    if (s != null) {
      s = s.replaceAll("\\r\\n|\\r|\\n", " ");
    }
    return s;
  }
}
