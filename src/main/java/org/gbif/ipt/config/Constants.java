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
package org.gbif.ipt.config;

import org.gbif.dwc.terms.DwcTerm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Constants {

  public static final String CANCEL = "cancel";
  public static final String SESSION_USER = "curr_user";
  public static final String SESSION_RESOURCE = "curr_resource";
  public static final String SESSION_URL = "url";
  public static final String SESSION_SOURCE_NAME = "sourceName";
  public static final String SESSION_SOURCE_OVERWRITE_MESSAGE = "sourceOverwriteMessage";
  public static final String SESSION_FILE = "file";
  public static final String SESSION_FILE_NAME = "fileName";
  public static final String SESSION_REFERER = "referer";
  public static final String SESSION_FILE_CONTENT_TYPE = "contentType";
  public static final String SESSION_FILE_NUMBER_COLUMNS = "numberColumns";
  public static final String REQ_PATH_RESOURCE = "resource";
  public static final String REQ_PATH_EML = "eml.do";
  public static final String REQ_PATH_METADATA = "metadata.do";
  public static final String REQ_PATH_ARCHIVE = "archive.do";
  public static final String REQ_PATH_LOGO = "logo.do";
  public static final String REQ_PARAM_RESOURCE = "r";
  public static final String REQ_PARAM_REINFER_METADATA = "reinferMetadata";
  public static final String REQ_PARAM_ID = "id";
  public static final String REQ_PARAM_SOURCE = "s";
  public static final String REQ_PARAM_VERSION = "v";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY = "updateFrequency";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MONTH = "updateFrequencyMonth";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_BIMONTH = "updateFrequencyBiMonth";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAY = "updateFrequencyDay";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_DAYOFWEEK = "updateFrequencyDayOfWeek";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_HOUR = "updateFrequencyHour";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_MINUTE = "updateFrequencyMinute";
  public static final String REQ_PARAM_AUTO_PUBLISH_FREQUENCY_TIME = "updateFrequencyTime";
  public static final String DWC_ROWTYPE_OCCURRENCE = DwcTerm.Occurrence.qualifiedName();
  public static final String DWC_ROWTYPE_TAXON = DwcTerm.Taxon.qualifiedName();
  public static final String DWC_ROWTYPE_EVENT = DwcTerm.Event.qualifiedName();
  public static final String DWC_ROWTYPE_MATERIAL_ENTITY = "http://rs.tdwg.org/dwc/terms/MaterialEntity";
  public static final String DWC_OCCURRENCE_ID = "http://rs.tdwg.org/dwc/terms/occurrenceID";
  public static final String DWC_TAXON_ID = "http://rs.tdwg.org/dwc/terms/taxonID";
  public static final String DWC_EVENT_ID = "http://rs.tdwg.org/dwc/terms/eventID";
  public static final String DWC_MATERIAL_ENTITY_ID = "http://rs.tdwg.org/dwc/terms/materialEntityID";
  public static final String DWC_BASIS_OF_RECORD = "http://rs.tdwg.org/dwc/terms/basisOfRecord";
  public static final String DWC_DATASET_ID = "http://rs.tdwg.org/dwc/terms/datasetID";
  public static final String DATA_PACKAGE_CAMTRAP_DP_IDENTIFIER = "http://rs.gbif.org/data-packages/camtrap-dp";
  public static final String DATA_PACKAGE_COLDP_IDENTIFIER = "http://rs.gbif.org/data-packages/coldp";
  public static final String VOCAB_URI_DATASET_TYPE = "http://rs.gbif.org/vocabulary/gbif/datasetType";
  public static final String VOCAB_URI_LANGUAGE = "http://iso.org/639-2";
  public static final String VOCAB_URI_COUNTRY = "http://iso.org/iso3166-1/alpha2";
  public static final String VOCAB_URI_RANKS = "http://rs.gbif.org/vocabulary/gbif/rank";
  public static final String VOCAB_URI_ROLES = "http://rs.gbif.org/vocabulary/gbif/agentRole";
  public static final String VOCAB_URI_UPDATE_FREQUENCIES = "http://rs.gbif.org/vocabulary/eml/updateFrequency";
  public static final String VOCAB_URI_PRESERVATION_METHOD = "http://rs.gbif.org/vocabulary/gbif/preservation_method";
  public static final String VOCAB_URI_DATASET_SUBTYPES = "http://rs.gbif.org/vocabulary/gbif/datasetSubtype";
  public static final String VOCAB_URI_BASIS_OF_RECORDS = "http://rs.tdwg.org/dwc/dwctype/";
  public static final String VOCAB_DECIMAL_LONGITUDE = "http://rs.tdwg.org/dwc/terms/decimalLongitude";
  public static final String VOCAB_DECIMAL_LATITUDE = "http://rs.tdwg.org/dwc/terms/decimalLatitude";
  public static final String LONGITUDE = "decimalLongitude";
  public static final String LATITUDE = "decimalLatitude";
  public static final String VOCAB_SCIENTIFIC_NAME = "http://rs.tdwg.org/dwc/terms/scientificName";
  public static final String VOCAB_KINGDOM = "http://rs.tdwg.org/dwc/terms/kingdom";
  public static final String VOCAB_PHYLUM = "http://rs.tdwg.org/dwc/terms/phylum";
  public static final String VOCAB_CLASS = "http://rs.tdwg.org/dwc/terms/class";
  public static final String VOCAB_ORDER = "http://rs.tdwg.org/dwc/terms/order";
  public static final String VOCAB_FAMILY = "http://rs.tdwg.org/dwc/terms/family";
  public static final String KINGDOM = "kingdom";
  public static final String PHYLUM = "phylum";
  public static final String CLASS = "class";
  public static final String ORDER = "order";
  public static final String FAMILY = "family";
  public static final String VOCAB_EVENT_DATE = "http://rs.tdwg.org/dwc/terms/eventDate";
  public static final String EVENT_DATE = "eventDate";
  public static final String[] IMAGE_TYPES = {"jpeg", "gif", "png"};
  public static final String GBIF_HOME_PAGE_URL = "https://www.gbif.org";
  public static final String THESAURUS_DATASET_TYPE =
    "GBIF Dataset Type Vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_type_2015-07-10.xml";
  public static final String THESAURUS_DATASET_SUBTYPE =
    "GBIF Dataset Subtype Vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_subtype.xml";
  public static final String DATASET_TYPE_METADATA_IDENTIFIER = "metadata";
  public static final String TEST_DOI_PREFIX = "10.21373";

  public static final String DATATABLE_ORDER_COLUMN_PARAM = "order[0][column]";
  public static final String DATATABLE_ORDER_DIRECTORY_PARAM = "order[0][dir]";
  public static final String DATATABLE_LENGTH_PARAM = "length"; // limit
  public static final String DATATABLE_START_PARAM = "start"; // offset
  public static final String DATATABLE_SEARCH_PARAM = "search[value]";

  public static final UUID DEFAULT_ORG_KEY = UUID.fromString("625a5522-1886-4998-be46-52c66dd566c9");

  // First published version number
  public static final BigDecimal INITIAL_RESOURCE_VERSION = new BigDecimal("1.0");
  public static final BigDecimal INITIAL_RESOURCE_VERSION_DATA_PACKAGE = new BigDecimal("1");

  public static final String CAMTRAP_DP = "camtrap-dp";
  public static final String COL_DP = "coldp";
  public static final String CAMTRAP_DP_OBSERVATIONS = "observations";
  public static final String DWC_ARCHIVE_NAME = "dwca";
  public static final String DWC_ARCHIVE_EXTENSION = ".zip";
  public static final String DATA_PACKAGE_NAME = "datapackage";
  public static final String DATA_PACKAGE_EXTENSION = ".zip";

  // Set of GBIF supported licenses
  public static final Set<String> GBIF_SUPPORTED_LICENSES;
  public static final Set<String> GBIF_SUPPORTED_LICENSES_CODES;
  public static final Map<String, String> COLDP_LICENSES_CODES_TO_GBIF = new HashMap<>();

  public static final String EML_2_1_1_SCHEMA = "eml://ecoinformatics.org/eml-2.1.1";
  public static final String EML_2_2_0_SCHEMA = "https://eml.ecoinformatics.org/eml-2.2.0";

  static {
    Set<String> licencesInternal = new HashSet<>();
    licencesInternal.add("http://creativecommons.org/publicdomain/zero/1.0/legalcode");
    licencesInternal.add("https://creativecommons.org/publicdomain/zero/1.0/legalcode");
    licencesInternal.add("http://creativecommons.org/licenses/by/4.0/legalcode");
    licencesInternal.add("https://creativecommons.org/licenses/by/4.0/legalcode");
    licencesInternal.add("http://creativecommons.org/licenses/by-nc/4.0/legalcode");
    licencesInternal.add("https://creativecommons.org/licenses/by-nc/4.0/legalcode");
    licencesInternal.add("http://www.opendatacommons.org/licenses/by/1.0/");
    licencesInternal.add("https://www.opendatacommons.org/licenses/by/1.0/");
    licencesInternal.add("http://www.opendatacommons.org/licenses/pddl/1.0/");
    licencesInternal.add("https://www.opendatacommons.org/licenses/pddl/1.0/");
    GBIF_SUPPORTED_LICENSES = Collections.unmodifiableSet(licencesInternal);

    Set<String> licenseCodesInternal = new HashSet<>();
    licenseCodesInternal.add("CC0-1.0");
    licenseCodesInternal.add("CC-BY-4.0");
    licenseCodesInternal.add("CC-BY-NC-4.0");
    GBIF_SUPPORTED_LICENSES_CODES = Collections.unmodifiableSet(licenseCodesInternal);

    COLDP_LICENSES_CODES_TO_GBIF.put("cc by nc", "CC-BY-NC-4.0");
    COLDP_LICENSES_CODES_TO_GBIF.put("cc by", "CC-BY-4.0");
    COLDP_LICENSES_CODES_TO_GBIF.put("cc0", "CC0-1.0");
  }

  private Constants() {
    throw new UnsupportedOperationException("Can't initialize class");
  }
}
