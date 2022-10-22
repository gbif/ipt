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
package org.gbif.ipt.action.portal;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

public class ResourceAction extends PortalBaseAction {

  private static final Logger LOG = LogManager.getLogger(ResourceAction.class);

  private VocabulariesManager vocabManager;
  private ExtensionManager extensionManager;
  private List<Resource> resources;
  private Integer page = 1;
  // for conveniently displaying taxonomic coverages in freemarker template
  private List<OrganizedTaxonomicCoverage> organizedCoverages;
  private Map<String, String> roles;
  private Map<String, String> preservationMethods;
  private Map<String, String> languages;
  private Map<String, String> countries;
  private Map<String, String> ranks;
  private DataDir dataDir;
  private Eml eml;
  private Set<Agent> mergedContacts = new HashSet<>();
  private Set<Agent> deduplicatedProjectPersonnel = new HashSet<>();
  private Map<String, Set<String>> contactRoles = new HashMap<>();
  private Map<String, Set<String>> projectPersonnelRoles = new HashMap<>();
  private boolean metadataOnly;
  private boolean preview;
  private Map<String, String> frequencies;
  private Map<String, String> types;
  private int recordsPublishedForVersion;
  private Map<String, Integer> recordsByExtensionForVersion = new HashMap<>();
  private String coreType;
  private String dwcaSizeForVersion;
  private String emlSizeForVersion;
  private String rtfSizeForVersion;

  @Inject
  public ResourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager, DataDir dataDir,
    ExtensionManager extensionManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.dataDir = dataDir;
    this.extensionManager = extensionManager;
  }

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Loads a specific version of a resource's metadata from its eml-v.xml file located inside its resource directory.
   * </br>
   * If no specific version is requested, the latest published version is loaded.
   * </br>
   * If there have been no published versions yet, the resource is loaded from the default eml.xml file.
   * </br>
   * If no eml.xml file exists yet, an empty EML instance is loaded.
   *
   * @param shortname resource shortname
   * @param version   resource version (eml version)
   *
   * @return EML object loaded from eml.xml file with specific version or a new EML instance if none found
   *
   * @throws IOException                                    if problem occurred loading eml file (e.g. it doesn't
   *                                                        exist)
   * @throws SAXException                                   if problem occurred parsing eml file
   * @throws javax.xml.parsers.ParserConfigurationException if problem occurred parsing eml file
   */
  private Eml loadEmlFromFile(String shortname, @NotNull BigDecimal version)
    throws IOException, SAXException, ParserConfigurationException {
    Objects.requireNonNull(version);
    File emlFile = dataDir.resourceEmlFile(shortname, version);
    LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
    InputStream in = new FileInputStream(emlFile);
    return EmlFactory.build(in);
  }

  /**
   * Return the latest published public (or registered) version, or null if the last published version was private or
   * deleted.
   * </br>
   * Important, VersionHistory goes from the latest published (first index) to the earliest published (last index).
   *
   * @param resource resource
   *
   * @return latest published public (or registered) version, or null if none found
   */
  private BigDecimal findLatestPublishedPublicVersion(Resource resource) {
    if (resource != null) {
      List<VersionHistory> history = resource.getVersionHistory();
      if (!history.isEmpty()) {
        VersionHistory latestVersion = history.get(0);
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) && !latestVersion
          .getPublicationStatus().equals(PublicationStatus.PRIVATE)) {
          return new BigDecimal(latestVersion.getVersion());
        }
      } else if (resource.isRegistered()) {
        return resource.getMetadataVersion();
      }
    }
    return null;
  }

  /**
   * Return the latest published version regardless of its visibility (e.g. public or private).
   * </br>
   * Important, VersionHistory goes from the latest published (first index) to the earliest published (last index).
   *
   * @param resource resource
   *
   * @return latest published public (or registered) version, or null if VersionHistory list was null or empty
   */
  private BigDecimal findLatestPublishedVersion(Resource resource) {
    if (resource != null) {
      List<VersionHistory> history = resource.getVersionHistory();
      if (!history.isEmpty()) {
        return new BigDecimal(history.get(0).getVersion());
      } else {
        return resource.getMetadataVersion();
      }
    }
    return null;
  }

  /**
   * @return publication status of specific published version, defaulting to status=private if it is not definitively
   * known
   */
  private PublicationStatus getPublishedVersionsPublicationStatus(Resource resource, BigDecimal version) {
    Objects.requireNonNull(version);
    List<VersionHistory> history = resource.getVersionHistory();
    if (!history.isEmpty()) {
      for (VersionHistory vh : history) {
        BigDecimal vhVersion = new BigDecimal(vh.getVersion());
        if (vhVersion.compareTo(version) == 0) {
          return vh.getPublicationStatus();
        }
      }
    } else if (resource.getMetadataVersion().compareTo(version) == 0) {
      return resource.getStatus();
    }
    return PublicationStatus.PRIVATE;
  }

  /**
   * Return the DOI for version requested, or null if no DOI was assigned or the resource's VersionHistory list is
   * null or empty. Important, VersionHistory goes from the latest published (first index) to the earliest published (last
   * index).
   *
   * @return DOI for published version, or null if no DOI was assigned or VersionHistory list was null or empty
   */
  public DOI findDoiAssignedToPublishedVersion() {
    if (resource != null) {
      BigDecimal versionRequested = (getVersion() == null) ? resource.getMetadataVersion() : getVersion();
      for (VersionHistory history : resource.getVersionHistory()) {
        if (history.getVersion().equalsIgnoreCase(versionRequested.toPlainString())) {
          // To be officially assigned, DOI must be public
          if (history.getStatus() == IdentifierStatus.PUBLIC) {
            return history.getDoi();
          }
        }
      }
    }
    return null;
  }

  public Ipt getIpt() {
    if (registrationManager.getIpt() == null) {
      return new Ipt();
    }
    return registrationManager.getIpt();
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Return the list of Agent Roles specific to the current locale.
   *
   * @return the list of Agent Roles specific to the current locale
   */
  public Map<String, String> getRoles() {
    return roles;
  }

  /**
   * Return the list of Preservation Methods specific to the current locale.
   *
   * @return the list of Preservation Methods specific to the current locale
   */
  public Map<String, String> getPreservationMethods() {
    return preservationMethods;
  }

  /**
   * Return the list of ISO 3 letter language codes specific to the current locale.
   *
   * @return the list of ISO 3 letter language codes specific to the current locale
   */
  public Map<String, String> getLanguages() {
    return languages;
  }

  /**
   * Return the list of 2-letter country codes specific to the current locale.
   *
   * @return the list of 2-letter country codes specific to the current locale
   */
  public Map<String, String> getCountries() {
    return countries;
  }

  /**
   * Return the list Ranks specific to the current locale.
   *
   * @return the list of Ranks specific to the current locale
   */
  public Map<String, String> getRanks() {
    return ranks;
  }

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }

  /**
   * Finish loading all details shown on resource homepage.
   *
   * @param resource resource
   * @param eml      Eml instance
   * @param version  resource version (eml version) to load
   */
  public void finishLoadingDetail(@NotNull Resource resource, @NotNull Eml eml, @NotNull BigDecimal version) {
    // determine whether version of resource requested is metadata-only or not (has published DwC-A or not)
    String name = resource.getShortname();
    File dwcaFile = dataDir.resourceDwcaFile(name, version);
    if (dwcaFile.exists()) {
      dwcaSizeForVersion = FileUtils.formatSize(dwcaFile.length(), 0);
    } else {
      metadataOnly = true;
    }

    // determine EML file size
    File emlFile = dataDir.resourceEmlFile(name, version);
    emlSizeForVersion = FileUtils.formatSize(emlFile.length(), 0);

    // determine RTF file size
    File rtfFile = dataDir.resourceRtfFile(name, version);
    rtfSizeForVersion = FileUtils.formatSize(rtfFile.length(), 0);

    // find record counts for published version
    for (VersionHistory history : resource.getVersionHistory()) {
      if (version.compareTo(new BigDecimal(history.getVersion())) == 0) {
        recordsPublishedForVersion = history.getRecordsPublished();
        setRecordsByExtensionForVersion(history.getRecordsByExtension());
      }
    }

    // ensure record counts by extension always exists, defaulting to empty map
    if (recordsByExtensionForVersion == null) {
      Map<String, Integer> m = new HashMap<>();
      setRecordsByExtensionForVersion(m);
    }

    // if the record count for this published version is greater than 0, but no dwca was found, it must have been
    // deleted which means that archival mode was not turned on when the proceeding version was published
    if (metadataOnly && recordsPublishedForVersion > 0) {
      addActionWarning(getText("portal.resource.version.notArchived.count",
        new String[] {String.valueOf(recordsPublishedForVersion)}));
    }

    // now prepare organized taxonomic coverages, facilitating UI display
    if (eml.getTaxonomicCoverages() != null) {
      organizedCoverages = constructOrganizedTaxonomicCoverages(eml.getTaxonomicCoverages());
    }
    // roles list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    roles = new LinkedHashMap<>();
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));

    // preservation methods list, derived from XML vocabulary, and displayed in drop-down on Collections Data Page.
    preservationMethods = new LinkedHashMap<>();
    preservationMethods
      .putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_PRESERVATION_METHOD, getLocaleLanguage(), false));

    // languages list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage(), true);

    // countries list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    countries = new LinkedHashMap<>();
    countries.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_COUNTRY, getLocaleLanguage(), true));

    // ranks list, derived from XML vocabulary, and displayed on Taxonomic Coverage Page
    ranks = new LinkedHashMap<>();
    ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));

    // update frequencies list, derived from XML vocabulary, and displayed on Basic Metadata Page
    frequencies = new LinkedHashMap<>();
    frequencies.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false));

    // Dataset core type list, derived from XML vocabulary
    types = new LinkedHashMap<>();
    types.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_TYPE, getLocaleLanguage(), false));
    types = MapUtils.getMapWithLowercaseKeys(types);
    coreType = (resource.getCoreType() != null && types.containsKey(resource.getCoreType().toLowerCase()))
        ? types.get(resource.getCoreType().toLowerCase())
        : types.get(Resource.CoreRowType.OTHER.toString().toLowerCase());
  }

  /**
   * Preview the next published version of the resource page.
   *
   * @return Struts2 result string
   */
  public String preview() {
    // retrieve unpublished eml.xml, using version = null
    String shortname = resource.getShortname();
    try {
      File emlFile = dataDir.resourceEmlFile(shortname);
      LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
      InputStream in = new FileInputStream(emlFile);
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      LOG.error("EML file version #" + getStringVersion() + " for resource " + shortname + " not found");
      return NOT_FOUND;
    } catch (IOException e) {
      String msg = getText("portal.resource.eml.error.load", new String[] {getStringVersion(), shortname});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    } catch (SAXException | ParserConfigurationException e) {
      String msg = getText("portal.resource.eml.error.parse", new String[] {getStringVersion(), shortname});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    }

    BigDecimal nextVersion = resource.getNextVersion();
    resource = generatePreviewResource(resource, eml, nextVersion);
    finishLoadingDetail(resource, eml, nextVersion);
    setPreview(true);

    return SUCCESS;
  }

  /**
   * Generate a copy of the resource, previewing what the next publication of the resource will look like.
   * This involves copying over certain fields not in EML, and then setting the version equal to next published
   * version,
   * and setting a new pubDate.
   *
   * @param resource    resource
   * @param eml         Eml
   * @param nextVersion next published version
   *
   * @return copy of the resource, previewing what the next publication of the resource will look like
   */
  private Resource generatePreviewResource(Resource resource, Eml eml, BigDecimal nextVersion) {
    Resource copy = new Resource();
    copy.setShortname(resource.getShortname());
    copy.setTitle(resource.getTitle());
    copy.setLastPublished(resource.getLastPublished());
    copy.setStatus(resource.getStatus());
    copy.setOrganisation(resource.getOrganisation());
    copy.setKey(resource.getKey());

    // update all version number and pubDate
    copy.setMetadataVersion(nextVersion);

    // update citation, if auto-generation turned on
    if (resource.isCitationAutoGenerated()) {
      Citation citation = new Citation();
      URI homepage = cfg.getResourceVersionUri(resource.getShortname(), nextVersion);
      citation.setCitation(resource.generateResourceCitation(nextVersion, homepage));
      eml.setCitation(citation);
    }

    Date releaseDate = new Date();
    copy.setLastPublished(releaseDate);
    eml.setPubDate(releaseDate);
    copy.setEml(eml);

    // create new VersionHistory
    List<VersionHistory> histories = new ArrayList<>(resource.getVersionHistory());
    copy.setVersionHistory(histories);
    VersionHistory history = new VersionHistory(nextVersion, releaseDate, PublicationStatus.PUBLIC);

    // modifiedBy
    User modifiedBy = getCurrentUser();
    if (modifiedBy != null) {
      history.setModifiedBy(modifiedBy);
    }

    // show DOI if it will go public on next publication
    if (resource.getDoi() != null && (resource.getIdentifierStatus() == IdentifierStatus.PUBLIC_PENDING_PUBLICATION
                                      || resource.getIdentifierStatus() == IdentifierStatus.PUBLIC)) {
      copy.setDoi(resource.getDoi());
      copy.setIdentifierStatus(IdentifierStatus.PUBLIC);
      history.setDoi(resource.getDoi());
      history.setStatus(IdentifierStatus.PUBLIC);
    }
    copy.addVersionHistory(history);

    return copy;
  }

  /**
   * Load resource detail (public portal) page. If no resource version is specified, the last public published version
   * is returned.
   * </br>
   * Admin users and resource managers can see all published versions of a resource.
   * </br>
   * Non authorized users can only see published versions that were publicly available at the time of publishing, or
   * that are registered (needed for resource published prior to IPT v2.2).
   * </br>
   * Please note that changing a resource's visibility from private to public does not change the visibility of the
   * last published version.
   *
   * @return Struts2 result string
   */
  public String detail() {
    if (resource == null) {
      return NOT_FOUND;
    }
    String name = resource.getShortname();
    try {
      // resource managers can see all published versions regardless of whether they were public or private
      if (getCurrentUser() != null && RequireManagerInterceptor.isAuthorized(getCurrentUser(), resource)) {
        version = (version == null) ? findLatestPublishedVersion(resource) : version;
        // warn user in case this version isn't publicly available
        PublicationStatus versionStatus = getPublishedVersionsPublicationStatus(resource, version);
        if (versionStatus.equals(PublicationStatus.PRIVATE)) {
          String status = getText("resource.status.private");
          addActionWarning(getText("portal.resource.warning.notPublic", new String[] {status.toLowerCase()}));
        } else if (resource.getStatus().equals(PublicationStatus.DELETED)) {
          String status = getText("resource.status.deleted");
          addActionWarning(getText("portal.resource.warning.notPublic", new String[] {status.toLowerCase()}));
        }
      }
      // otherwise, only published public versions can be shown
      else {
        // if no specific version was requested, find the latest published public version
        if (version == null) {
          version = findLatestPublishedPublicVersion(resource);
          if (version == null) {
            return NOT_ALLOWED;
          }
        }
        // if a specific version was requested, ensure it was public (or registered)
        else {
          VersionHistory history = resource.findVersionHistory(version);
          if (history == null || history.getPublicationStatus().equals(PublicationStatus.PRIVATE) || history
            .getPublicationStatus().equals(PublicationStatus.DELETED)) {
            return NOT_ALLOWED;
          }
        }
      }

      // if the specific version requested is not the latest published version, warn user
      if (resource.getLastPublishedVersionsVersion() != null
          && version.compareTo(resource.getLastPublishedVersionsVersion()) != 0) {
        addActionWarning(getText("portal.resource.warning.notLatest"));
      }

      // load EML instance for version requested
      eml = loadEmlFromFile(name, version);
    } catch (FileNotFoundException e) {
      LOG.error("EML file version #" + getStringVersion() + " for resource " + name + " not found");
      return NOT_FOUND;
    } catch (IOException e) {
      String msg = getText("portal.resource.eml.error.load", new String[] {getStringVersion(), name});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    } catch (SAXException | ParserConfigurationException e) {
      String msg = getText("portal.resource.eml.error.parse", new String[] {getStringVersion(), name});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    }

    finishLoadingDetail(resource, eml, version);

    return SUCCESS;
  }

  /**
   * Takes a list of the resource's TaxonomicCoverages, and for each one, creates a new OrganizedTaxonomicCoverage
   * that gets added to the class' list of OrganizedTaxonomicCoverage.
   *
   * @param coverages list of resource's TaxonomicCoverage
   */
  List<OrganizedTaxonomicCoverage> constructOrganizedTaxonomicCoverages(List<TaxonomicCoverage> coverages) {
    List<OrganizedTaxonomicCoverage> organizedTaxonomicCoverages = new ArrayList<>();
    for (TaxonomicCoverage coverage : coverages) {
      OrganizedTaxonomicCoverage organizedCoverage = new OrganizedTaxonomicCoverage();
      organizedCoverage.setDescription(coverage.getDescription());
      organizedCoverage.setKeywords(setOrganizedTaxonomicKeywords(coverage.getTaxonKeywords()));
      organizedTaxonomicCoverages.add(organizedCoverage);
    }
    return organizedTaxonomicCoverages;
  }

  /**
   * For each unique rank, this method constructs a new OrganizedTaxonomicKeywords that contains a list of display
   * names coming from a resource's TaxonomicCoverage's TaxonKeywords corresponding to that rank. The display name
   * consists of "scientific name (common name)". Another OrganizedTaxonomicKeywords is also created for the unknown
   * rank, that is a TaxonomicKeyword not having a rank.
   *
   * @param keywords list of resource's TaxonomicCoverage's TaxonKeywords
   *
   * @return list of OrganizedTaxonomicKeywords (one for each rank + unknown rank), or an empty list if none were added
   */
  private List<OrganizedTaxonomicKeywords> setOrganizedTaxonomicKeywords(List<TaxonKeyword> keywords) {
    List<OrganizedTaxonomicKeywords> organizedTaxonomicKeywordsList = new ArrayList<>();

    // also, we want a unique set of names corresponding to empty rank
    Set<String> uniqueNamesForEmptyRank = new HashSet<>();

    ranks = new LinkedHashMap<>();
    ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));

    for (String rank : ranks.keySet()) {
      OrganizedTaxonomicKeywords organizedKeywords = new OrganizedTaxonomicKeywords();
      // set rank
      organizedKeywords.setRank(rank);
      // construct display name for each TaxonKeyword, and add display name to organized keywords list
      for (TaxonKeyword keyword : keywords) {
        // add display name to appropriate list if it isn't null
        String displayName = createKeywordDisplayName(keyword);
        if (displayName != null) {
          if (rank.equalsIgnoreCase(keyword.getRank())) {
            organizedKeywords.getDisplayNames().add(displayName);
          } else if (StringUtils.trimToNull(keyword.getRank()) == null) {
            uniqueNamesForEmptyRank.add(displayName);
          }
        }
      }
      // add to list
      organizedTaxonomicKeywordsList.add(organizedKeywords);
    }
    // if there were actually some names with empty ranks, add the special OrganizedTaxonomicKeywords for empty rank
    if (!uniqueNamesForEmptyRank.isEmpty()) {
      // create special OrganizedTaxonomicKeywords for empty rank
      OrganizedTaxonomicKeywords emptyRankKeywords = new OrganizedTaxonomicKeywords();
      emptyRankKeywords.setRank("Unranked");
      emptyRankKeywords.setDisplayNames(new ArrayList<>(uniqueNamesForEmptyRank));
      organizedTaxonomicKeywordsList.add(emptyRankKeywords);
    }
    // return list
    return organizedTaxonomicKeywordsList;
  }

  /**
   * Construct display name from TaxonKeyword's scientific name and common name properties. It will look like:
   * scientific name (common name) provided both properties are not null.
   *
   * @return constructed display name or an empty string if none could be constructed
   */
  private String createKeywordDisplayName(TaxonKeyword keyword) {
    String combined = null;
    if (keyword != null) {
      String scientificName = StringUtils.trimToNull(keyword.getScientificName());
      String commonName = StringUtils.trimToNull(keyword.getCommonName());
      if (scientificName != null && commonName != null) {
        combined = scientificName + " (" + commonName + ")";
      } else if (scientificName != null) {
        combined = scientificName;
      } else if (commonName != null) {
        combined = commonName;
      }
    }
    return combined;
  }

  /**
   * Returns a list of OrganizedTaxonomicCoverage that facilitate the display of the resource's TaxonomicCoverage on
   * the UI.
   *
   * @return list of OrganizedTaxonomicCoverage or an empty list if none were added
   */
  public List<OrganizedTaxonomicCoverage> getOrganizedCoverages() {
    return organizedCoverages;
  }

  /**
   * Get the EML instance to display on Resource Portal page.
   *
   * @return EML instance
   */
  public Eml getEml() {
    return eml;
  }

  /**
   * Returns merged contacts. Populates collection if it's empty.
   * Puts all creators, contact, metadataProviders and associatedParties together removing duplicates.
   *
   * @return set og agents (contacts) without duplicates
   */
  public Set<Agent> getMergedContacts() {
    if (mergedContacts.isEmpty()) {
      Stream.of(eml.getCreators(), eml.getContacts(), eml.getMetadataProviders(), eml.getAssociatedParties())
          .flatMap(Collection::stream)
          .filter(Objects::nonNull)
          .filter(this::isValidAgent)
          .filter(agent -> mergedContacts.stream().filter(Objects::nonNull).noneMatch(a -> agentsMatch(a, agent)))
          .forEach(mergedContacts::add);
    }

    if (contactRoles.isEmpty()) {
      initializeContactRoles();
    }

    return mergedContacts;
  }

  /**
   * Returns deduplicated personnel. Populates collection if it's empty.
   *
   * @return set og agents (project's personnel) without duplicates
   */
  public Set<Agent> getDeduplicatedProjectPersonnel() {
    if (eml.getProject() != null && eml.getProject().getPersonnel() != null && deduplicatedProjectPersonnel.isEmpty()) {
      eml.getProject().getPersonnel()
          .stream()
          .filter(Objects::nonNull)
          .filter(agent -> deduplicatedProjectPersonnel.stream().filter(Objects::nonNull).noneMatch(a -> agentsMatch(a, agent)))
          .forEach(deduplicatedProjectPersonnel::add);
    }

    if (projectPersonnelRoles.isEmpty()) {
      initializeProjectPersonnelRoles();
    }

    return deduplicatedProjectPersonnel;
  }

  private boolean isValidAgent(Agent agent) {
    boolean isEmpty = StringUtils.isEmpty(agent.getFullName());
    if (isEmpty) {
      LOG.error("Invalid contact: fullname not present. Check the metadata");
    }
    return !isEmpty;
  }

  /**
   * Check two EML agents match.
   * Compares only essential fields:
   * <ol>
   *  <li>Full names must match</li>
   *  <li>If both emails are non-nulls, they must match</li>
   *  <li>If both position are non-nulls, they must match</li>
   * </ol>
   *
   * @param agent1 first agent
   * @param agent2 second agent
   * @return comparison result
   */
  private boolean agentsMatch(Agent agent1, Agent agent2) {
    boolean namesMatch = false;
    boolean emailsMatch;
    boolean positionsMatch;

    if (agent1.getFullName() != null && agent2.getFullName() != null) {
      namesMatch = agent1.getFullName().equals(agent2.getFullName());
    }

    if (agent1.getEmail() != null && agent2.getEmail() != null) {
      emailsMatch = agent1.getEmail().equals(agent2.getEmail());
    } else {
      emailsMatch = true;
    }

    if (agent1.getPosition() != null && agent2.getPosition() != null) {
      positionsMatch = agent1.getPosition().equals(agent2.getPosition());
    } else {
      positionsMatch = true;
    }

    return namesMatch && emailsMatch && positionsMatch;
  }

  /**
   * Initializes contacts' roles.
   * After deduplication contact may have several roles.
   */
  private void initializeContactRoles() {
    for (Agent agent : mergedContacts) {
      Set<String> agentRoles = new HashSet<>();
      if (eml.getCreators().stream().filter(Objects::nonNull).anyMatch(a -> agentsMatch(a, agent))) {
        agentRoles.add("originator");
      }
      if (eml.getContacts().stream().filter(Objects::nonNull).anyMatch(a -> agentsMatch(a, agent))) {
        agentRoles.add("pointOfContact");
      }
      if (eml.getMetadataProviders().stream().filter(Objects::nonNull).anyMatch(a -> agentsMatch(a, agent))) {
        agentRoles.add("metadataProvider");
      }
      eml.getAssociatedParties()
          .stream()
          .filter(Objects::nonNull)
          .filter(a -> a.getRole() != null)
          .filter(a -> agentsMatch(a, agent))
          .forEach(a -> agentRoles.add(a.getRole()));

      contactRoles.put(agent.getFullName(), agentRoles);
    }
  }

  /**
   * Initializes project personnel's roles.
   * After deduplication contact may have several roles.
   */
  private void initializeProjectPersonnelRoles() {
    if (eml.getProject() != null && eml.getProject().getPersonnel() != null) {
      for (Agent agent : eml.getProject().getPersonnel()) {
        // agent already present, just add role if not null
        if (projectPersonnelRoles.containsKey(agent.getFullName()) && StringUtils.isNotEmpty(agent.getRole())) {
          projectPersonnelRoles.get(agent.getFullName()).add(agent.getRole());
        }
        // agent not present, add agent and its role (or just empty set if role is null)
        else if (!projectPersonnelRoles.containsKey(agent.getFullName())) {
          Set<String> agentRoles = new HashSet<>();
          if (StringUtils.isNotEmpty(agent.getRole())) {
            agentRoles.add(agent.getRole());
          }
          projectPersonnelRoles.put(agent.getFullName(), agentRoles);
        }
      }
    }
  }

  public Map<String, Set<String>> getContactRoles() {
    return contactRoles;
  }

  public Map<String, Set<String>> getProjectPersonnelRoles() {
    return projectPersonnelRoles;
  }

  /**
   * Set the EML instance to display on Resource Portal page.
   *
   * @param eml EML instance
   */
  public void setEml(Eml eml) {
    this.eml = eml;
  }

  /**
   * Returns whether the version of the resource is a metadata-only resource. This is determined by the existence of
   * a DwC-A. This method is only really of importance for versions of the resource that are not the latest. For the
   * latest published version of the resource, one can just call resource.recordsPublished() and see if it's > 0.
   *
   * @return true if resource is metadata-only
   */
  public boolean isMetadataOnly() {
    return metadataOnly;
  }

  /**
   * Set whether resource is metadata-only or not.
   *
   * @param metadataOnly is the resource metadata-only
   */
  public void setMetadataOnly(boolean metadataOnly) {
    this.metadataOnly = metadataOnly;
  }

  /**
   * This map populates the update frequencies. The map is derived from the vocabulary {@link -linkoffline
   * http://rs.gbif.org/vocabulary/eml/update_frequency.xml}.
   *
   * @return update frequencies map
   */
  public Map<String, String> getFrequencies() {
    return frequencies;
  }

  /**
   * @return record count for published version (specified from version parameter)
   */
  public int getRecordsPublishedForVersion() {
    return recordsPublishedForVersion;
  }

  /**
   * @return formatted size of DwC-A for published version
   */
  public String getDwcaSizeForVersion() {
    return dwcaSizeForVersion;
  }

  /**
   * @return formatted size of EML for published version
   */
  public String getEmlSizeForVersion() {
    return emlSizeForVersion;
  }

  /**
   * @return formatted size of RTF for published version
   */
  public String getRtfSizeForVersion() {
    return rtfSizeForVersion;
  }

  /**
   * @return true if the page rendered is a preview of the next release
   */
  public boolean isPreview() {
    return preview;
  }

  /**
   * @param preview true if the page rendered is a preview of the next release, false otherwise
   */
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  /**
   * A map of dataset types keys to internationalized values.
   *
   * @return map of dataset subtypes
   */
  public Map<String, String> getTypes() {
    return types;
  }

  /**
   * @return ExtensionManager, to retrieve Extension by rowType in template
   */
  public ExtensionManager getExtensionManager() {
    return extensionManager;
  }

  /**
   * @param recordsByExtensionForVersion map of record counts by extension for published version (specified from version
   *                                     parameter)
   */
  public void setRecordsByExtensionForVersion(Map<String, Integer> recordsByExtensionForVersion) {
    this.recordsByExtensionForVersion = recordsByExtensionForVersion;
  }

  /**
   * @return the largest number of records found in any extension, including the core extension
   */
  public int getMaxRecordsInExtension() {
    int count = 0;
    if (!recordsByExtensionForVersion.isEmpty()) {
      for (String rowType : recordsByExtensionForVersion.keySet()) {
        int extensionCount = recordsByExtensionForVersion.get(rowType);
        count = Math.max(extensionCount, count);
      }
    }
    return count;
  }

  /**
   * @return map of record counts by extension for published version (specified from version parameter), sorted by
   * count then by extension name for uniqueness (as two extensions can have the same count)
   */
  public LinkedHashMap<String, Integer> getRecordsByExtensionOrdered() {
    LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
    recordsByExtensionForVersion
        .entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
        .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

    return result;
  }

  /**
   * @return the core type of the resource
   */
  public String getCoreType() {
    return coreType;
  }
}
