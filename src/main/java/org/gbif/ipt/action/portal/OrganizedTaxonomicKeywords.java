package org.gbif.ipt.action.portal;

import java.util.ArrayList;
import java.util.List;

/**
 * Class similar to TaxonomicCoverage, but the TaxonomicKeywords are OrganizedTaxonomicKeywords. This conveniently
 * stores all scientific names and common names for a rank together. Each display name is simply the concatenation
 * of the scientific name, and the common name in parentheses. E.g. Plantae (plants).
 *
 * @see org.gbif.metadata.eml.TaxonKeyword in project gbif-metadata-profile
 */
public class OrganizedTaxonomicKeywords {

  private String rank;
  private List<String> displayNames = new ArrayList<String>();

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public List<String> getDisplayNames() {
    return displayNames;
  }

  public void setDisplayNames(List<String> displayNames) {
    this.displayNames = displayNames;
  }
}
