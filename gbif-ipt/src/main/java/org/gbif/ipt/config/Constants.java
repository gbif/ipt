package org.gbif.ipt.config;

public class Constants {
  public static final String SESSION_USER = "curr_user";
  public static final String SESSION_RESOURCE = "curr_resource";
  public static final String REQ_PARAM_RESOURCE = "r";
  public static final String REQ_PARAM_SOURCE = "s";
  public static final String DWC_ROWTYPE_OCCURRENCE = "http://rs.tdwg.org/dwc/terms/Occurrence";
  public static final String DWC_ROWTYPE_TAXON = "http://rs.tdwg.org/dwc/terms/Taxon";
  public static final String DWC_OCCURRENCE_ID = "http://rs.tdwg.org/dwc/terms/OccurrenceID";
  public static final String DWC_TAXON_ID = "http://rs.tdwg.org/dwc/terms/TaxonID";
  public static final String VOCAB_URI_RESOURCE_TYPE = "http://rs.gbif.org/vocabulary/gbif/resource_type";
  public static final String VOCAB_URI_LANGUAGE = "http://iso.org/639-2";
  public static final String VOCAB_URI_COUNTRY = "http://iso.org/iso3166-1/alpha2";
  public static final String VOCAB_URI_RANKS = "http://rs.gbif.org/vocabulary/gbif/rank";
  public static final String VOCAB_URI_ROLES = "http://rs.gbif.org/vocabulary/gbif/agentRole";
  public static final String VOCAB_URI_PRESERVATION_METHOD = "http://rs.gbif.org/vocabulary/gbif/preservation_method";
  public static final String[] IMAGE_TYPES = new String[3];
  static {
    IMAGE_TYPES[0] = "jpeg";
    IMAGE_TYPES[1] = "gif";
    IMAGE_TYPES[2] = "png";
  }

}
