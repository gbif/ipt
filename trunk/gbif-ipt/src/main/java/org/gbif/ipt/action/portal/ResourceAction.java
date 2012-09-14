package org.gbif.ipt.action.portal;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.commons.lang.xwork.StringUtils;

public class ResourceAction extends PortalBaseAction {

  private VocabulariesManager vocabManager;
  private List<Resource> resources;
  private Integer page = 1;
  // for conveniently displaying taxonomic coverages in freemarker template
  private List<OrganizedTaxonomicCoverage> organizedCoverages;
  private Map<String, String> roles;

  @Inject
  public ResourceAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.vocabManager = vocabManager;
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

  public Eml getEml() {
    return resource.getEml();
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

  public Map<String, String> getRanks() {
    return vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.getDefault().getLanguage(), false);
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

  public String rss() {
    resources = resourceManager.latest(page, 25);
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    // now prepare organized taxonomic coverages, facilitating UI display
    if (resource != null && resource.getEml() != null && resource.getEml().getTaxonomicCoverages() != null) {
      organizedCoverages = constructOrganizedTaxonomicCoverages(resource.getEml().getTaxonomicCoverages());
    }
    // roles list, derived from XML vocabulary, and displayed in drop-down where new contacts are created
    roles = new LinkedHashMap<String, String>();
    roles.putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_ROLES, getLocaleLanguage(), false));
  }

  /**
   * Takes a list of the resource's TaxonomicCoverages, and for each one, creates a new OrganizedTaxonomicCoverage
   * that gets added to the class' list of OrganizedTaxonomicCoverage.
   *
   * @param coverages list of resource's TaxonomicCoverage
   */
  List<OrganizedTaxonomicCoverage> constructOrganizedTaxonomicCoverages(List<TaxonomicCoverage> coverages) {
    List<OrganizedTaxonomicCoverage> organizedCoverages = new ArrayList<OrganizedTaxonomicCoverage>();
    for (TaxonomicCoverage coverage : coverages) {
      OrganizedTaxonomicCoverage organizedCoverage = new OrganizedTaxonomicCoverage();
      organizedCoverage.setDescription(coverage.getDescription());
      organizedCoverage.setKeywords(setOrganizedTaxonomicKeywords(coverage.getTaxonKeywords()));
      organizedCoverages.add(organizedCoverage);
    }
    return organizedCoverages;
  }

  /**
   * For each unique rank, this method constructs a new OrganizedTaxonomicKeywords that contains a list of display
   * names
   * coming from a resource's TaxonomicCoverage's TaxonKeywords corresponding to that rank. The display nane consists
   * of
   * "scientific name (common name)". Another OrganizedTaxonomicKeywords is also created for the unknown rank, that is
   * a TaxonomicKeyword not having a rank.
   *
   * @param keywords list of resource's TaxonomicCoverage's TaxonKeywords
   *
   * @return list of OrganizedTaxonomicKeywords (one for each rank + unknown rank), or an empty list if none were added
   */
  private List<OrganizedTaxonomicKeywords> setOrganizedTaxonomicKeywords(List<TaxonKeyword> keywords) {
    List<OrganizedTaxonomicKeywords> organizedTaxonomicKeywordsList = new ArrayList<OrganizedTaxonomicKeywords>();

    // also we want a unique set of names corresponding to empty rank
    Set<String> uniqueNamesForEmptyRank = new HashSet<String>();

    for (String rank : getRanks().keySet()) {
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
    if (uniqueNamesForEmptyRank.size() > 0) {
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
}
