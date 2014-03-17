package org.gbif.ipt.action.portal;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class ResourceAction extends PortalBaseAction {

  private static final Logger LOG = Logger.getLogger(ResourceAction.class);

  private VocabulariesManager vocabManager;
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
  private boolean metadataOnly;
  private int recordsPublishedForVersion;
  private Map<String, String> frequencies;

  @Inject
  public ResourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager, DataDir dataDir) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
    this.dataDir = dataDir;
  }

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Return the size of the DwC-A file.
   */
  public String getDwcaFormattedSize() {
    return FileUtils.formatSize(resourceManager.getDwcaSize(resource), 0);
  }

  /**
   * Loads a specific version of a resource's metadata from its eml-v.xml file located inside its resource directory.
   * If no eml-v.xml file was found (there have been no published versions yet), the resource is loaded from the
   * default eml.xml file. If no eml.xml file exists yet, an empty EML instance is loaded.
   * 
   * @param shortname resource shortname
   * @param version resource version (eml version)
   * @return EML object loaded from eml.xml file with specific version or a new EML instance if none found
   * @throws IOException if problem occurred loading eml file (e.g. it doesn't exist)
   * @throws SAXException if problem occurred parsing eml file
   */
  private Eml loadEmlFromFile(String shortname, @Nullable Integer version) throws IOException, SAXException {
    File emlFile = dataDir.resourceEmlFile(shortname, version);
    LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
    InputStream in = new FileInputStream(emlFile);
    return EmlFactory.build(in);
  }

  /**
   * Return the size of the EML file.
   */
  public String getEmlFormattedSize() {
    return FileUtils.formatSize(resourceManager.getEmlSize(resource), 0);
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
   * Return the RTF size file format.
   */
  public String getRtfFormattedSize() {
    return FileUtils.formatSize(resourceManager.getRtfSize(resource), 0);
  }

  public boolean isRtfFileExisting() {
    return resourceManager.isRtfExisting(resource.getShortname());
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
   * Handle everything needed to load resource detail (public portal) page.
   * 
   * @return Struts2 result string
   */
  public String detail() {
    if (resource == null) {
      return NOT_FOUND;
    }
    // load EML instance for version requested
    String name = resource.getShortname();
    try {
      eml = loadEmlFromFile(name, version);
    } catch (FileNotFoundException e) {
      LOG.error("EML file version #" + getStringVersion() + " for resource " + name + " not found");
      return NOT_FOUND;
    } catch (IOException e) {
      String msg = getText("portal.resource.eml.error.load", new String[] {getStringVersion(), name});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    } catch (SAXException e) {
      String msg = getText("portal.resource.eml.error.parse", new String[] {getStringVersion(), name});
      LOG.error(msg);
      addActionError(msg);
      return ERROR;
    }

    // determine whether version of resource requested is metadata-only or not (has published DwC-A or not)
    if (version == null) {
      setMetadataOnly(resource.getRecordsPublished() > 0);
    } else {
      File dwcaFile = dataDir.resourceDwcaFile(name, version);
      if (dwcaFile.exists()) {
        setMetadataOnly(true);
        // determine record count for the version being requested (read from hidden file .recordspublished-version)
        setRecordsPublishedForVersion(resource.getShortname(), version);
      }
    }

    // now prepare organized taxonomic coverages, facilitating UI display
    if (resource != null && eml != null && eml.getTaxonomicCoverages() != null) {
      organizedCoverages = constructOrganizedTaxonomicCoverages(eml.getTaxonomicCoverages());
    }
    // roles list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    roles = new LinkedHashMap<String, String>();
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));

    // preservation methods list, derived from XML vocabulary, and displayed in drop-down on Collections Data Page.
    preservationMethods = new LinkedHashMap<String, String>();
    preservationMethods
      .putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_PRESERVATION_METHOD, getLocaleLanguage(), false));

    // languages list, derived from XML vocabulary, and displayed in drop-down on Basic Metadata page
    languages = vocabManager.getI18nVocab(Constants.VOCAB_URI_LANGUAGE, getLocaleLanguage(), true);

    // countries list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    countries = new LinkedHashMap<String, String>();
    countries.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_COUNTRY, getLocaleLanguage(), true));

    // ranks list, derived from XML vocabulary, and displayed on Taxonomic Coverage Page
    ranks = new LinkedHashMap<String, String>();
    ranks.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, getLocaleLanguage(), false));

    // update frequencies list, derived from XML vocabulary, and displayed on Basic Metadata Page
    frequencies = new LinkedHashMap<String, String>();
    frequencies.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_UPDATE_FREQUENCIES, getLocaleLanguage(), false));

    return SUCCESS;
  }

  /**
   * Takes a list of the resource's TaxonomicCoverages, and for each one, creates a new OrganizedTaxonomicCoverage
   * that gets added to the class' list of OrganizedTaxonomicCoverage.
   * 
   * @param coverages list of resource's TaxonomicCoverage
   */
  List<OrganizedTaxonomicCoverage> constructOrganizedTaxonomicCoverages(List<TaxonomicCoverage> coverages) {
    List<OrganizedTaxonomicCoverage> organizedTaxonomicCoverages = new ArrayList<OrganizedTaxonomicCoverage>();
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
   * names coming from a resource's TaxonomicCoverage's TaxonKeywords corresponding to that rank. The display nane
   * consists of "scientific name (common name)". Another OrganizedTaxonomicKeywords is also created for the unknown
   * rank, that is a TaxonomicKeyword not having a rank.
   * 
   * @param keywords list of resource's TaxonomicCoverage's TaxonKeywords
   * @return list of OrganizedTaxonomicKeywords (one for each rank + unknown rank), or an empty list if none were added
   */
  private List<OrganizedTaxonomicKeywords> setOrganizedTaxonomicKeywords(List<TaxonKeyword> keywords) {
    List<OrganizedTaxonomicKeywords> organizedTaxonomicKeywordsList = new ArrayList<OrganizedTaxonomicKeywords>();

    // also we want a unique set of names corresponding to empty rank
    Set<String> uniqueNamesForEmptyRank = new HashSet<String>();

    ranks = new LinkedHashMap<String, String>();
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
      emptyRankKeywords.setDisplayNames(new ArrayList<String>(uniqueNamesForEmptyRank));
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
   * Look for .recordspublished-version file, and parse its contents for published record count.
   * 
   * @param shortname resource shortname
   * @param version resource version to retrieve published record count for
   * @return published record count for version or null if file not found or could not be parsed
   */
  public int setRecordsPublishedForVersion(String shortname, int version) {
    recordsPublishedForVersion = 0;
    File file = dataDir.resourceCountFile(shortname, version);
    if (file != null && file.exists()) {
      try {
        String countAsString = StringUtils.trimToNull(org.apache.commons.io.FileUtils.readFileToString(file));
        recordsPublishedForVersion = Integer.valueOf(countAsString);
      } catch (IOException e) {
        LOG.error("Cannot read file: " + file.getAbsolutePath(), e);
      } catch (NumberFormatException e) {
        LOG.error("Number read from file not valid: " + file.getAbsolutePath());
      }
    } else {
      LOG.warn(".recordspublished-version file not existing for version: " + version);
    }
    return recordsPublishedForVersion;
  }

  /**
   * @return the number of records published for version of resource requested.
   */
  public int getRecordsPublishedForVersion() {
    return recordsPublishedForVersion;
  }
}
