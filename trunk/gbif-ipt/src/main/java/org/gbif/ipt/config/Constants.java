package org.gbif.ipt.config;

import java.math.BigDecimal;

import com.google.common.collect.ImmutableSet;

public final class Constants {

  public static final String SESSION_USER = "curr_user";
  public static final String SESSION_RESOURCE = "curr_resource";
  public static final String SESSION_FILE = "file";
  public static final String SESSION_FILE_NAME = "fileName";
  public static final String SESSION_FILE_CONTENT_TYPE = "contentType";
  public static final String REQ_PARAM_RESOURCE = "r";
  public static final String REQ_PARAM_SOURCE = "s";
  public static final String REQ_PARAM_VERSION = "v";
  public static final String REQ_PARAM_PUBLICATION_MODE = "pubMode";
  public static final String REQ_PARAM_PUBLICATION_FREQUENCY = "pubFreq";
  public static final String DWC_ROWTYPE_OCCURRENCE = "http://rs.tdwg.org/dwc/terms/Occurrence";
  public static final String DWC_ROWTYPE_TAXON = "http://rs.tdwg.org/dwc/terms/Taxon";
  public static final String DWC_OCCURRENCE_ID = "http://rs.tdwg.org/dwc/terms/occurrenceID";
  public static final String DWC_TAXON_ID = "http://rs.tdwg.org/dwc/terms/taxonID";
  public static final String DWC_DATASET_ID = "http://rs.tdwg.org/dwc/terms/datasetID";
  public static final String VOCAB_URI_DATASET_TYPE = "http://rs.gbif.org/vocabulary/gbif/datasetType";
  public static final String VOCAB_URI_LANGUAGE = "http://iso.org/639-2";
  public static final String VOCAB_URI_COUNTRY = "http://iso.org/iso3166-1/alpha2";
  public static final String VOCAB_URI_RANKS = "http://rs.gbif.org/vocabulary/gbif/rank";
  public static final String VOCAB_URI_ROLES = "http://rs.gbif.org/vocabulary/gbif/agentRole";
  public static final String VOCAB_URI_UPDATE_FREQUENCIES = "http://rs.gbif.org/vocabulary/eml/updateFrequency";
  public static final String VOCAB_URI_PRESERVATION_METHOD = "http://rs.gbif.org/vocabulary/gbif/preservation_method";
  public static final String VOCAB_URI_DATASET_SUBTYPES = "http://rs.gbif.org/vocabulary/gbif/datasetSubtype";
  public static final String DEPRECATED_VOCAB_URI_RESOURCE_TYPE = "http://rs.gbif.org/vocabulary/gbif/resource_type";
  public static final String DEPRECATED_VOCAB_URL_RESOLVABLE_RESOURCE_TYPE =
    "http_rs_gbif_org_vocabulary_gbif_resource_type_xml.vocab";
  public static final String[] IMAGE_TYPES = {"jpeg", "gif", "png"};
  public static final String GBIF_HOME_PAGE_URL = "http://www.gbif.org";
  public static final String THESAURUS_DATASET_TYPE =
    "GBIF Dataset Type Vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_type.xml";
  public static final String THESAURUS_DATASET_SUBTYPE =
    "GBIF Dataset Subtype Vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_subtype.xml";
  public static final String DATASET_TYPE_METADATA_IDENTIFIER = "metadata";
  public static final String TEST_DOI_PREFIX = "10.5072";
  public static final String EZID_TEST_DOI_SHOULDER = "10.5072/FK2";

  // First published version number
  public static final BigDecimal INITIAL_RESOURCE_VERSION = new BigDecimal("1.0");

  // Set of GBIF supported licenses
  public static final ImmutableSet<String> GBIF_SUPPORTED_LICENSES = ImmutableSet
    .of("http://creativecommons.org/publicdomain/zero/1.0/legalcode",
      "http://creativecommons.org/licenses/by/4.0/legalcode", "http://creativecommons.org/licenses/by-nc/4.0/legalcode",
      "http://www.opendatacommons.org/licenses/by/1.0/", "http://www.opendatacommons.org/licenses/pddl/1.0/");

  private Constants() {
    throw new UnsupportedOperationException("Can't initialize class");
  }
}
