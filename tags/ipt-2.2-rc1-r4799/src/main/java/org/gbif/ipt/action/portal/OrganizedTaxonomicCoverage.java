package org.gbif.ipt.action.portal;

import java.util.List;

/**
 * Class similar to TaxonomicCoverage, but the TaxonomicKeywords are OrganizedTaxonomicKeywords.
 *
 *  * @see org.gbif.metadata.eml.TaxonomicCoverage in project gbif-metadata-profile
 */
public class OrganizedTaxonomicCoverage {

  private List<OrganizedTaxonomicKeywords> keywords;
  private String description;

  public List<OrganizedTaxonomicKeywords> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<OrganizedTaxonomicKeywords> keywords) {
    this.keywords = keywords;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
