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
package org.gbif.ipt.action.manage;

import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.DatasetSubtype;
import org.gbif.common.parsers.CountryParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.MetadataSection;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.LangUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.validation.ActionErrorCollector;
import org.gbif.ipt.validation.EmlValidator;
import org.gbif.ipt.validation.ResourceValidator;
import org.gbif.ipt.i18n.StrutsI18n;
import org.gbif.metadata.eml.ipt.model.Address;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.JGTICuratorialUnitType;
import org.gbif.metadata.eml.ipt.model.TemporalCoverageType;
import org.gbif.metadata.eml.ipt.model.UserId;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.Parameter;
import org.apache.struts2.ActionContext;

import lombok.Getter;
import lombok.Setter;

import static org.gbif.ipt.config.Constants.DATASET_TYPE_METADATA_IDENTIFIER;

public class MetadataAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 3996112876438145205L;

  private static final Logger LOG = LogManager.getLogger(MetadataAction.class);

  private final ResourceValidator validatorRes = new ResourceValidator();
  private final EmlValidator emlValidator;
  private final VocabulariesManager vocabManager;
  private final ResourceMetadataInferringService resourceMetadataInferringService;
  private static final String LICENSES_PROPFILE_PATH = "/org/gbif/metadata/eml/licenses.properties";
  private static final String LICENSE_NAME_PROPERTY_PREFIX = "license.name.";
  private static final String LICENSE_TEXT_PROPERTY_PREFIX = "license.text.";
  private static final String LICENSE_URL_PROPERTY_PREFIX = "license.url.";
  private static final String DIRECTORIES_PROPFILE_PATH = "/org/gbif/metadata/eml/UserDirectories.properties";

  private MetadataSection section = MetadataSection.BASIC_SECTION;
  private MetadataSection next = MetadataSection.GEOGRAPHIC_COVERAGE_SECTION;
  @Getter
  private Map<String, String> languages;
  // A map of countries
  @Getter
  private Map<String, String> countries;
  // A map of Ranks
  @Getter
  private Map<String, String> ranks;
  @Getter
  private Map<String, String> roles;
  // A map of preservation methods
  @Getter
  private Map<String, String> preservationMethods;
  @Getter
  private Map<String, String> types;
  // A list of dataset subtypes used to populate the dataset subtype dropdown on the Basic Metadata page.
  @Getter
  private Map<String, String> datasetSubtypes;
  // On the basic metadata page, this map populates the update frequencies dropdown.
  // The map is derived from the vocabulary http://rs.gbif.org/vocabulary/eml/update_frequency.xml .
  @Getter
  private Map<String, String> frequencies;

  // to group dataset subtype vocabulary keys
  private List<String> checklistSubtypeKeys;
  private List<String> occurrenceSubtypeKeys;
  private List<String> samplingEventSubtypeKeys;

  private static final CountryParser COUNTRY_PARSER = CountryParser.getInstance();

  // The very first contact entered, or a new instance of Agent if no contacts exist yet.
  // The first contact entered is considered the primary contact.
  // Other contact forms can be entered by copying from the primary contact.
  @Setter
  @Getter
  private Agent primaryContact;
  private boolean doiReservedOrAssigned = false;
  @Getter
  private InferredEmlMetadata inferredMetadata;
  private final ConfigWarnings configWarnings;
  private static Properties licenseProperties;
  private static Properties directoriesProperties;
  private static Map<String, String> licenses;
  private static Map<String, String> licenseTexts;
  private static Map<String, String> licenseUrls;
  private static Map<String, String> userIdDirectories;

  private final DataDir dataDir;
  private File file;

  @Inject
  public MetadataAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      VocabulariesManager vocabManager,
      ResourceMetadataInferringService resourceMetadataInferringService,
      ConfigWarnings configWarnings,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.resourceMetadataInferringService = resourceMetadataInferringService;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
    this.configWarnings = configWarnings;
    this.dataDir = dataDir;
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

  /*
   * Called from Basic Metadata page.
   *
   * Determine which license is specified in the intellectual rights. If the intellectual rights contain the name of
   * a license the IPT supports (e.g. CC-BY 4.0), the key corresponding to that license (e.g. ccby) is returned. This
   * is used to pre-select the license dropdown when the basic metadata page loads.
   */
  public String getLicenseKeySelected() {
    String licenseText = resource.getEml().getIntellectualRights();
    if (StringUtils.isNotBlank(licenseText)) {
      // can be old-fashioned license, with version outside parenthesis
      String licenseTextUpdated = licenseText.replace(") 4.0", " 4.0)");
      for (Map.Entry<String, String> entry : licenses.entrySet()) {
        String licenseName = entry.getValue();
        if (StringUtils.isNotBlank(licenseName) && licenseTextUpdated.contains(licenseName)) {
          return entry.getKey();
        }
      }

      // Try URL instead (MDT style)
      for (Map.Entry<String, String> entry : licenseUrls.entrySet()) {
        String licenseUrl = entry.getValue();
        if (StringUtils.isNotBlank(licenseUrl) && licenseTextUpdated.contains(licenseUrl)) {
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

  public Map<String, String> getLicenseUrls() {
    return licenseUrls;
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
        return getSamplingEventSubtypesMap();
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

  @Override
  public Resource getResource() {
    return resource;
  }

  public String getSection() {
    return section.getName();
  }

  public Map<String, String> getTempTypes() {
    return TemporalCoverageType.HTML_SELECT_MAP;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (session.get(Constants.SESSION_USER) == null) {
      return;
    }

    // take the section parameter from the requested url
    String requestURI = req.getRequestURI();
    section = MetadataSection.fromName(StringUtils.substringBetween(req.getRequestURI(), "metadata-", "."));

    // uploadlogo - redirect to additional metadata section
    if (requestURI.contains("uploadlogo")) {
      section = MetadataSection.ADDITIONAL_SECTION;
    }

    boolean reinferMetadata = Boolean.parseBoolean(StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_REINFER_METADATA)));

    boolean mappingsChangedAfterLastTry =
        !DATASET_TYPE_METADATA_IDENTIFIER.equals(resource.getCoreType())
            && resource.getInferredMetadata() != null
            && resource.getInferredMetadata().getLastModified() != null
            && resource.getMappingsModified() != null
            && resource.getMappingsModified().after(resource.getInferredMetadata().getLastModified());

    // infer metadata if:
    // 1) It was requested
    // 2) It is absent
    // 3) Mappings were changed
    if (reinferMetadata || resource.getInferredMetadata() == null || mappingsChangedAfterLastTry) {
      InferredMetadata inferredMetadataRaw = resourceMetadataInferringService.inferMetadata(resource);

      if (inferredMetadataRaw instanceof InferredEmlMetadata) {
        inferredMetadata = (InferredEmlMetadata) inferredMetadataRaw;
      } else {
        LOG.error("Wrong type of the inferred metadata class, expected {} got {}",
            InferredEmlMetadata.class.getSimpleName(), inferredMetadataRaw.getClass().getSimpleName());
        inferredMetadata = new InferredEmlMetadata();
      }
      resource.setInferredMetadata(inferredMetadata);
      resourceManager.saveInferredMetadata(resource);
    } else {
      if (resource.getInferredMetadata() instanceof InferredEmlMetadata) {
        inferredMetadata = (InferredEmlMetadata) resource.getInferredMetadata();
      } else {
        LOG.error("Wrong type of the stored inferred metadata class, expected {} got {}",
            InferredEmlMetadata.class.getSimpleName(), resource.getInferredMetadata().getClass().getSimpleName());
        inferredMetadata = new InferredEmlMetadata();
      }
    }

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
        // temporary fix: remove "unkown" from vocabulary
        vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false)
            .entrySet()
            .stream()
            .filter(p -> !"unkown".equals(p.getKey()))
            .forEach(p -> frequencies.put(p.getKey(), p.getValue()));

        // sanitize intellectualRights - pre-v2.2 text was manually entered and may have characters that break js
        if (getEml().getIntellectualRights() != null) {
          getEml().setIntellectualRights(removeNewlineCharacters(getEml().getIntellectualRights()));
        }

        // Public, published occurrence resources have a distribution download URL
        if (CoreRowType.OCCURRENCE.toString().equalsIgnoreCase(resource.getCoreType())
            && resource.isPublished()
            && resource.isPubliclyAvailable()) {
          resource.getEml().setDistributionDownloadUrl(cfg.getBaseUrl() + "/archive.do?r=" + resource.getShortname());
        }

        // populate agent vocabularies
        loadAgentVocabularies();

        // load license maps
        try {
          loadLicenseMaps(getText("eml.intellectualRights.nolicenses"));
        } catch (InvalidConfigException e) {
          configWarnings.addStartupError(e.getMessage(), e);
        }

        // load directories map
        try {
          loadDirectories(getText("eml.contact.noDirectory"));
        } catch (InvalidConfigException e) {
          configWarnings.addStartupError(e.getMessage(), e);
        }

        // if IPT isn't registered, there are no publishing organisations to choose from, so set to "No organisation"
        if (getRegisteredIpt() == null && getDefaultOrganisation() != null) {
          resource.setOrganisation(getDefaultOrganisation());
          addActionWarning(getText("manage.overview.visibility.missing.organisation"));
        }

        if (isHttpPost()) {
          resource.getEml().setIntellectualRights(null);
        }
        break;

      case CONTACTS_SECTION:
        // populate agent vocabularies
        loadAgentVocabularies();
        if (isHttpPost()) {
          resource.getEml().getContacts().clear();
          resource.getEml().getCreators().clear();
          resource.getEml().getMetadataProviders().clear();
          resource.getEml().getAssociatedParties().clear();
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

      case PROJECT_SECTION:
        // populate agent vocabularies
        loadAgentVocabularies();
        if (isHttpPost()) {
          resource.getEml().getProject().getPersonnel().clear();
          resource.getEml().getProject().getAwards().clear();
          resource.getEml().getProject().getRelatedProjects().clear();
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

      default:
        break;
    }
  }

  @Override
  public String save() throws Exception {
    // before saving, the minimum amount of mandatory metadata must have been provided, and the current metadata section
    // must be valid, otherwise an error is displayed
    if (emlValidator.isSectionValid(resource, section, new ActionErrorCollector(this), new StrutsI18n(this))) {
      // Save metadata information (eml.xml)
      resourceManager.saveEml(resource);
      // save date metadata was last modified
      resource.setMetadataModified(new Date());
      // Alert user of successful save
      addActionMessage(getText("manage.success", new String[]{getText("submenu." + section.getName())}));
      // Save resource information (resource.xml)
      resourceManager.save(resource);
      // progress to next section, since save succeeded
      switch (section) {
        case BASIC_SECTION:
          next = MetadataSection.CONTACTS_SECTION;
          break;
        case CONTACTS_SECTION:
          next = MetadataSection.ACKNOWLEDGEMENTS_SECTION;
          break;
        case ACKNOWLEDGEMENTS_SECTION:
          next = MetadataSection.GEOGRAPHIC_COVERAGE_SECTION;
          break;
        case GEOGRAPHIC_COVERAGE_SECTION:
          next = MetadataSection.TAXANOMIC_COVERAGE_SECTION;
          break;
        case TAXANOMIC_COVERAGE_SECTION:
          next = MetadataSection.TEMPORAL_COVERAGE_SECTION;
          break;
        case TEMPORAL_COVERAGE_SECTION:
          next = MetadataSection.ADDITIONAL_DESCRIPTION_SECTION;
          break;
        case ADDITIONAL_DESCRIPTION_SECTION:
          next = MetadataSection.KEYWORDS_SECTION;
          break;
        case KEYWORDS_SECTION:
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
        default:
          break;
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
    emlValidator.validate(resource, section, new ActionErrorCollector(this), new StrutsI18n(this));
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
      datasetSubtypesCopy.remove(key);
    }
    return datasetSubtypesCopy;
  }

  public Map<String, String> getSamplingEventSubtypesMap() {
    // exclude subtypes known to relate to Checklist type
    Map<String, String> datasetSubtypesCopy = new LinkedHashMap<>(datasetSubtypes);
    for (String key : checklistSubtypeKeys) {
      datasetSubtypesCopy.remove(key);
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
      datasetSubtypesCopy.remove(key);
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
   * The way this grouping is done is that DatasetSubtype Enum.name gets converted into vocabulary.identifier,
   * for ex: TAXONOMIC_IDENTIFIER -> taxonomiciIdentifier
   * As long as the DatasetSubtype is extended properly in accordance with the DatasetSubtype vocabulary, simply
   * updating the version of gbif-common-api dependency is the only change needed to update the subtype list.
   * This is a workaround to the limitation we currently have with our XML vocabularies, in that concepts can't be
   * grouped.
   */
  void groupDatasetSubtypes() {
    List<String> occurrenceKeys = new ArrayList<>();
    for (DatasetSubtype type : DatasetSubtype.OCCURRENCE_DATASET_SUBTYPES) {
      occurrenceKeys.add(type.name().replaceAll("_", "").toLowerCase());
    }
    occurrenceSubtypeKeys = Collections.unmodifiableList(occurrenceKeys);

    List<String> checklistKeys = new ArrayList<>();
    for (DatasetSubtype type : DatasetSubtype.CHECKLIST_DATASET_SUBTYPES) {
      checklistKeys.add(type.name().replaceAll("_", "").toLowerCase());
    }
    checklistSubtypeKeys = Collections.unmodifiableList(checklistKeys);

    List<String> samplingEventKeys = new ArrayList<>();
    for (DatasetSubtype type : DatasetSubtype.SAMPLING_EVENT_DATASET_SUBTYPES) {
      samplingEventKeys.add(type.name().replaceAll("_", "").toLowerCase());
    }
    samplingEventSubtypeKeys = Collections.unmodifiableList(samplingEventKeys);
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

  List<String> getSamplingEventSubtypeKeys() {
    return samplingEventSubtypeKeys;
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

  public Map<String, String> getUserIdDirectories() {
    if (userIdDirectories == null) {
      loadDirectories(getText("eml.contact.noDirectory"));
    }
    return userIdDirectories;
  }

  /**
   * @return Properties from licenses properties file
   * @throws InvalidConfigException if the licenses properties file could not be loaded
   */
  public static synchronized Properties licenseProperties() throws InvalidConfigException {
    if (licenseProperties == null) {
      Properties p = new Properties();
      try {
        p.load(MetadataAction.class.getResourceAsStream(LICENSES_PROPFILE_PATH));
        LOG.debug("Loaded licenses from {}", LICENSES_PROPFILE_PATH);
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
   * @throws InvalidConfigException if directories properties file could not be loaded
   */
  public static synchronized Properties directoriesProperties() throws InvalidConfigException {
    if (directoriesProperties == null) {
      Properties p = new Properties();
      try {
        p.load(MetadataAction.class.getResourceAsStream(DIRECTORIES_PROPFILE_PATH));
        LOG.debug("Loaded directories from {}", DIRECTORIES_PROPFILE_PATH);
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
   * #2) license texts - used to populate text area on Basic Metadata Page
   * #3) license URLs
   *
   * @param firstOption the default select option for the license names (e.g. no license selected)
   * @throws InvalidConfigException if the licenses properties file was badly configured
   */
  public static synchronized void loadLicenseMaps(String firstOption) throws InvalidConfigException {
    if (licenses == null || licenseTexts == null || licenseUrls == null) {
      licenses = new TreeMap<>(new LicenceComparator());
      licenses.put("", (firstOption == null) ? "-" : firstOption);
      licenseTexts = new TreeMap<>();
      licenseUrls = new TreeMap<>();

      Properties properties = licenseProperties();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String key = StringUtils.trim((String) entry.getKey());
        String value = StringUtils.trim((String) entry.getValue());

        if (key != null && key.startsWith(LICENSE_NAME_PROPERTY_PREFIX) && value != null) {
          String keyMinusPrefix = StringUtils.trimToNull(key.replace(LICENSE_NAME_PROPERTY_PREFIX, ""));

          if (keyMinusPrefix != null) {
            String licenseText =
                StringUtils.trimToNull(properties.getProperty(LICENSE_TEXT_PROPERTY_PREFIX + keyMinusPrefix));
            String licenseUrl =
                StringUtils.trimToNull(properties.getProperty(LICENSE_URL_PROPERTY_PREFIX + keyMinusPrefix));

            if (licenseText != null) {
              licenses.put(keyMinusPrefix, value);
              licenseTexts.put(keyMinusPrefix, licenseText);
              licenseUrls.put(keyMinusPrefix, licenseUrl);
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
   * @throws InvalidConfigException if the directories properties file was badly configured
   */
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
      // contacts list
      Agent firstContact = null;
      if (!resource.getEml().getContacts().isEmpty()) {
        for (Agent contact : resource.getEml().getContacts()) {
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

      // always populate the primary contact, used to autofill parties and project personnel
      if (firstContact == null) {
        firstContact = new Agent();
      }
      if (firstContact.getUserIds().isEmpty()) {
        List<UserId> userIds = new ArrayList<>();
        firstContact.setUserIds(userIds);
      }
      setPrimaryContact(firstContact);

      // creator list
      if (!resource.getEml().getCreators().isEmpty()) {
        for (Agent creator : resource.getEml().getCreators()) {
          String countryValue = creator.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            creator.getAddress().setCountry(result.getPayload().getIso2LetterCode());
          }
        }
      }
      // metadata provider list
      if (!resource.getEml().getMetadataProviders().isEmpty()) {
        for (Agent metadataProvider : resource.getEml().getMetadataProviders()) {
          String countryValue = metadataProvider.getAddress().getCountry();
          if (countryValue != null) {
            ParseResult<Country> result = COUNTRY_PARSER.parse(countryValue);
            metadataProvider.getAddress().setCountry(result.getPayload().getIso2LetterCode());
          }
        }
      }

      // otherwise, ensure associated parties' country value get converted into 2-letter iso code for proper display
      else if (!resource.getEml().getAssociatedParties().isEmpty()) {
        for (Agent party : resource.getEml().getAssociatedParties()) {
          String countryValue = Optional.ofNullable(party)
              .map(Agent::getAddress)
              .map(Address::getCountry)
              .orElse(null);
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
   * Remove all newline characters from string.
   * Used to sanitize string for JavaScript, otherwise an
   * "Unexpected Token ILLEGAL" error may occur.
   */
  protected String removeNewlineCharacters(String s) {
    if (s != null) {
      s = s.replaceAll("\\r\\n|\\r|\\n", " ");
    }
    return s;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String uploadLogo() {
    if (file != null) {
      // remove any previous logo file
      for (String suffix : Constants.IMAGE_TYPES) {
        FileUtils.deleteQuietly(dataDir.resourceLogoFile(resource.getShortname(), suffix));
      }
      // inspect file type
      String type = "jpeg";
      String fileContentType = Optional.ofNullable(ActionContext.getContext())
          .map(ActionContext::getParameters)
          .map(p -> p.get("fileContentType"))
          .map(Parameter::getValue)
          .orElse(null);

      if (fileContentType != null) {
        type = StringUtils.substringAfterLast(fileContentType, "/");
      }
      File logoFile = dataDir.resourceLogoFile(resource.getShortname(), type);
      try {
        FileUtils.copyFile(file, logoFile);
      } catch (IOException e) {
        LOG.warn(e.getMessage());
      }
      // resource.getEml().setLogoUrl(cfg.getResourceLogoUrl(resource.getShortname()));
    }
    return INPUT;
  }

  /**
   * Custom license comparator. This is needed because java Properties class is based on Hashtable and does not
   * respect the order in the licences.properties file. This comparator will sort license from less to more restricted.
   * Result: "" (empty), cczero, ccby, ccbync
   */
  private static class LicenceComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
      if (StringUtils.equals(o1, o2)) {
        return 0;
      } else if (!StringUtils.equalsAny("cczero", o1, o2)) {
        // if not cczero - just compare them
        return o1.compareTo(o2);
      } else if ("cczero".equals(o1) && !o2.isEmpty()) {
        // cczero should be right after empty license
        return -1;
      } else if ("cczero".equals(o2) && !o1.isEmpty()) {
        // cczero should be right after empty license
        return 1;
      } else if (o1.isEmpty()) {
        return -1;
      } else if (o2.isEmpty()) {
        return 1;
      }

      return o1.compareTo(o2);
    }
  }
}
