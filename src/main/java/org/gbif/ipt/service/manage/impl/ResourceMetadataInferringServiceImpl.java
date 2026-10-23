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
package org.gbif.ipt.service.manage.impl;

import org.gbif.api.util.IsoDateInterval;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.date.TemporalAccessorUtils;
import org.gbif.common.parsers.date.TemporalRangeParser;
import org.gbif.common.parsers.geospatial.CoordinateParseUtils;
import org.gbif.common.parsers.geospatial.LatLng;
import org.gbif.ipt.action.portal.OrganizedTaxonomicCoverage;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTaxonomicCoverage;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.metadata.eml.ipt.model.BBox;
import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;
import org.gbif.metadata.eml.ipt.model.Point;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TemporalCoverage;
import org.gbif.utils.file.ClosableReportingIterator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.CLASS;
import static org.gbif.ipt.config.Constants.DWC_ROWTYPE_EVENT;
import static org.gbif.ipt.config.Constants.DWC_ROWTYPE_OCCURRENCE;
import static org.gbif.ipt.config.Constants.DWC_ROWTYPE_TAXON;
import static org.gbif.ipt.config.Constants.FAMILY;
import static org.gbif.ipt.config.Constants.KINGDOM;
import static org.gbif.ipt.config.Constants.ORDER;
import static org.gbif.ipt.config.Constants.PHYLUM;
import static org.gbif.ipt.config.Constants.VOCAB_CLASS;
import static org.gbif.ipt.config.Constants.VOCAB_DECIMAL_LATITUDE;
import static org.gbif.ipt.config.Constants.VOCAB_DECIMAL_LONGITUDE;
import static org.gbif.ipt.config.Constants.VOCAB_EVENT_DATE;
import static org.gbif.ipt.config.Constants.VOCAB_FAMILY;
import static org.gbif.ipt.config.Constants.VOCAB_KINGDOM;
import static org.gbif.ipt.config.Constants.VOCAB_ORDER;
import static org.gbif.ipt.config.Constants.VOCAB_PHYLUM;

/**
 * Infers geographic, temporal, and taxonomic coverage from the mapped source data of a resource.
 * <p>
 * Values are read the same way they are published: rows rejected by the mapping filter are skipped, translations are
 * applied, and default values are used for missing values.
 */
public class ResourceMetadataInferringServiceImpl implements ResourceMetadataInferringService {

  private static final Logger LOG = LogManager.getLogger(ResourceMetadataInferringServiceImpl.class);

  // DwC-DP tables the EML coverages are inferred from
  private static final String DWC_DP_EVENT = "event";
  private static final Set<String> DWC_DP_TAXA_TABLES = new HashSet<>(Arrays.asList("identification", "identification-taxon"));
  private static final String DWC_DP_DECIMAL_LATITUDE = "decimalLatitude";
  private static final String DWC_DP_DECIMAL_LONGITUDE = "decimalLongitude";
  private static final String DWC_DP_EVENT_DATE = "eventDate";
  private static final String DWC_DP_IDENTIFICATION = "identification";
  private static final String DWC_DP_IS_ACCEPTED_IDENTIFICATION = "isAcceptedIdentification";
  // values of isAcceptedIdentification for superseded identifications, compared ignoring case; blank counts as accepted
  private static final Set<String> NOT_ACCEPTED_VALUES = Set.of("false", "0", "no");

  private static final String CAMTRAP_DEPLOYMENTS = "deployments";
  private static final String CAMTRAP_OBSERVATIONS = "observations";
  public static final String CAMTRAP_OBSERVATIONS_SCIENTIFIC_NAME = "scientificName";
  public static final String CAMTRAP_DEPLOYMENTS_LATITUDE = "latitude";
  public static final String CAMTRAP_DEPLOYMENTS_LONGITUDE = "longitude";
  public static final String CAMTRAP_DEPLOYMENTS_DEPLOYMENT_START = "deploymentStart";
  public static final String CAMTRAP_DEPLOYMENTS_DEPLOYMENT_END = "deploymentEnd";
  // max number of distinct names per rank, ranks exceeding it are left out of the EML taxonomic coverage
  public static final int TAXON_LIMIT = 100;
  // max number of distinct names in the Camtrap DP taxonomic scope, the rest is ignored
  public static final int CAMTRAP_TAXON_LIMIT = 200;
  // errors when a source can't be read or inferring fails unexpectedly: publishing infers again, they can be temporary
  public static final String EML_SERVER_ERROR = "eml.error.serverError";
  public static final String CAMTRAP_SERVER_ERROR = "datapackagemetadata.error.serverError";

  // EML taxonomic coverage ranks and the terms they are inferred from
  private static final Map<String, String> EML_RANK_TERMS = new LinkedHashMap<>();

  static {
    EML_RANK_TERMS.put(KINGDOM, VOCAB_KINGDOM);
    EML_RANK_TERMS.put(PHYLUM, VOCAB_PHYLUM);
    EML_RANK_TERMS.put(CLASS, VOCAB_CLASS);
    EML_RANK_TERMS.put(ORDER, VOCAB_ORDER);
    EML_RANK_TERMS.put(FAMILY, VOCAB_FAMILY);
  }

  private final SourceManager sourceManager;
  private final VocabulariesManager vocabManager;

  public ResourceMetadataInferringServiceImpl(SourceManager sourceManager, VocabulariesManager vocabManager) {
    this.sourceManager = sourceManager;
    this.vocabManager = vocabManager;
  }

  @Override
  public InferredMetadata inferMetadata(Resource resource) {
    if (!resource.isDataPackage() || resource.isDwcDp()) {
      return inferEmlMetadata(resource);
    } else if (CAMTRAP_DP.equals(resource.getCoreType())) {
      return inferCamtrapMetadata(resource);
    } else {
      LOG.error("Metadata inferring is not supported for the type {}", resource.getCoreType());
      return null;
    }
  }

  /**
   * Each mapping is read only for the coverages it has the fields for: e.g. an event core for the geographic and
   * temporal coverages, an occurrence extension for the taxonomic coverage.
   */
  private InferredEmlMetadata inferEmlMetadata(Resource resource) {
    // the time inferring started: mappings changed while reading the sources make the result outdated
    Date started = new Date();
    List<EmlMapping> mappings = emlMappings(resource);

    GeographicCoverageInference geographic = new GeographicCoverageInference();
    TemporalCoverageInference temporal = new TemporalCoverageInference();
    TaxonomicCoverageInference taxonomic = new TaxonomicCoverageInference();
    List<EmlCoverageInference> coverages = List.of(geographic, temporal, taxonomic);

    for (EmlMapping mapping : mappings) {
      List<EmlCoverageInference> coveragesOfMapping = coverages.stream()
          .filter(coverage -> coverage.hasFields(mapping))
          .toList();
      if (coveragesOfMapping.isEmpty()) {
        continue;
      }
      coveragesOfMapping.forEach(EmlCoverageInference::markFieldsMapped);

      boolean read = readRows(mapping.source, mapping.filter, mapping.fields, row -> {
        coveragesOfMapping.forEach(coverage -> coverage.add(mapping, row));
        return true;
      });

      if (!read) {
        coveragesOfMapping.forEach(EmlCoverageInference::markReadFailed);
      }
    }

    boolean mappingsExist = !mappings.isEmpty();
    InferredEmlMetadata inferredMetadata = new InferredEmlMetadata();
    inferredMetadata.setInferredGeographicCoverage(geographic.result(mappingsExist));
    inferredMetadata.setInferredTemporalCoverage(temporal.result(mappingsExist));
    inferredMetadata.setInferredTaxonomicCoverage(taxonomic.result(mappingsExist, this::organizeTaxonomicCoverage));
    inferredMetadata.setLastModified(started);

    return inferredMetadata;
  }

  @Override
  public OptionalLong countRowsToRead(Resource resource) {
    List<Source> sourcesToRead = new ArrayList<>();

    if (!resource.isDataPackage() || resource.isDwcDp()) {
      List<EmlCoverageInference> coverages =
          List.of(new GeographicCoverageInference(), new TemporalCoverageInference(), new TaxonomicCoverageInference());
      for (EmlMapping mapping : emlMappings(resource)) {
        if (coverages.stream().anyMatch(coverage -> coverage.hasFields(mapping))) {
          sourcesToRead.add(mapping.source);
        }
      }
    } else if (CAMTRAP_DP.equals(resource.getCoreType())) {
      for (DataPackageMapping mapping : resource.getDataPackageMappings()) {
        if (hasCamtrapScopeFields(mapping)) {
          sourcesToRead.add(mapping.getSource());
        }
      }
    }

    long rows = 0;
    for (Source source : sourcesToRead) {
      if (source == null) {
        continue;
      }
      // a source mapped several times is read several times
      if (!(source instanceof FileSource fileSource)) {
        return OptionalLong.empty();
      }
      rows += fileSource.getRows();
    }
    return OptionalLong.of(rows);
  }

  private static List<EmlMapping> emlMappings(Resource resource) {
    return resource.isDwcDp()
        ? resource.getDataPackageMappings().stream().map(ResourceMetadataInferringServiceImpl::emlMapping).toList()
        : resource.getMappings().stream().map(ResourceMetadataInferringServiceImpl::emlMapping).toList();
  }

  /**
   * Whether a Camtrap DP mapping is read: deployments with both coordinates or both dates, observations with
   * scientific names.
   */
  private static boolean hasCamtrapScopeFields(DataPackageMapping mapping) {
    String tableSchema = Optional.ofNullable(mapping.getDataPackageTableSchemaName())
        .map(DataPackageTableSchemaName::getName)
        .orElse(null);

    if (CAMTRAP_DEPLOYMENTS.equals(tableSchema)) {
      boolean coordinatesMapped = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_LATITUDE)).isMapped()
          && MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_LONGITUDE)).isMapped();
      boolean datesMapped = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_DEPLOYMENT_START)).isMapped()
          && MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_DEPLOYMENT_END)).isMapped();
      return coordinatesMapped || datesMapped;
    } else if (CAMTRAP_OBSERVATIONS.equals(tableSchema)) {
      return MappedField.of(mapping.getField(CAMTRAP_OBSERVATIONS_SCIENTIFIC_NAME)).isMapped();
    }
    return false;
  }

  /**
   * DwC-A: place and time from occurrences and events, taxa from occurrences and taxa.
   */
  private static EmlMapping emlMapping(ExtensionMapping mapping) {
    String rowType = mapping.getExtension() != null ? mapping.getExtension().getRowType() : null;
    boolean hasPlaceAndTime = DWC_ROWTYPE_OCCURRENCE.equals(rowType) || DWC_ROWTYPE_EVENT.equals(rowType);
    boolean hasTaxa = DWC_ROWTYPE_OCCURRENCE.equals(rowType) || DWC_ROWTYPE_TAXON.equals(rowType);
    // mapped fields by term, e.g. "http://rs.tdwg.org/dwc/terms/eventDate" -> its index, default value and translation
    Map<String, MappedField> fields = new HashMap<>();
    for (PropertyMapping field : mapping.getFields()) {
      if (field.getTerm() != null) {
        // a term mapped twice: keep the first one
        fields.putIfAbsent(field.getTerm().qualifiedName(), MappedField.of(field));
      }
    }

    EmlMapping result = new EmlMapping(mapping.getSource(), mapping.getFilter(), fields.values());
    if (hasPlaceAndTime) {
      result.latitude = fields.getOrDefault(VOCAB_DECIMAL_LATITUDE, MappedField.UNMAPPED);
      result.longitude = fields.getOrDefault(VOCAB_DECIMAL_LONGITUDE, MappedField.UNMAPPED);
      result.eventDate = fields.getOrDefault(VOCAB_EVENT_DATE, MappedField.UNMAPPED);
    }
    if (hasTaxa) {
      EML_RANK_TERMS.forEach((rank, term) -> result.addRank(rank, fields.getOrDefault(term, MappedField.UNMAPPED)));
    }
    return result;
  }

  /**
   * DwC-DP: place and time from events, taxa from identifications. Fields are named after the ranks.
   */
  private static EmlMapping emlMapping(DataPackageMapping mapping) {
    String table = Optional.ofNullable(mapping.getDataPackageTableSchemaName())
        .map(DataPackageTableSchemaName::getName)
        .orElse(null);
    List<MappedField> fields = mapping.getFields() == null
        ? Collections.emptyList()
        : mapping.getFields().stream().map(MappedField::of).collect(Collectors.toList());

    EmlMapping result = new EmlMapping(mapping.getSource(), mapping.getFilter(), fields);
    if (DWC_DP_EVENT.equals(table)) {
      result.latitude = MappedField.of(mapping.getField(DWC_DP_DECIMAL_LATITUDE));
      result.longitude = MappedField.of(mapping.getField(DWC_DP_DECIMAL_LONGITUDE));
      result.eventDate = MappedField.of(mapping.getField(DWC_DP_EVENT_DATE));
    }
    if (DWC_DP_TAXA_TABLES.contains(table)) {
      EML_RANK_TERMS.keySet().forEach(rank -> result.addRank(rank, MappedField.of(mapping.getField(rank))));
    }
    if (DWC_DP_IDENTIFICATION.equals(table)) {
      result.acceptedIdentification = MappedField.of(mapping.getField(DWC_DP_IS_ACCEPTED_IDENTIFICATION));
    }
    return result;
  }

  /**
   * Groups the taxon keywords by rank for the UI, in the order of the ranks vocabulary.
   */
  private OrganizedTaxonomicCoverage organizeTaxonomicCoverage(TaxonomicCoverage coverage) {
    Map<String, List<String>> namesByRank = coverage.getTaxonKeywords().stream()
        .collect(Collectors.groupingBy(
            k -> StringUtils.lowerCase(k.getRank(), Locale.ENGLISH),
            Collectors.mapping(TaxonKeyword::getScientificName, Collectors.toList())));

    List<OrganizedTaxonomicKeywords> organizedKeywords = new ArrayList<>();
    for (String rank : vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.ENGLISH.getLanguage(), false).keySet()) {
      OrganizedTaxonomicKeywords keywords = new OrganizedTaxonomicKeywords();
      keywords.setRank(rank);
      keywords.setDisplayNames(new ArrayList<>(namesByRank.getOrDefault(rank.toLowerCase(Locale.ENGLISH), Collections.emptyList())));
      organizedKeywords.add(keywords);
    }

    OrganizedTaxonomicCoverage organizedCoverage = new OrganizedTaxonomicCoverage();
    organizedCoverage.setDescription(coverage.getDescription());
    organizedCoverage.setKeywords(organizedKeywords);
    return organizedCoverage;
  }

  private InferredCamtrapMetadata inferCamtrapMetadata(Resource resource) {
    // the time inferring started: mappings changed while reading the sources make the result outdated
    Date started = new Date();
    DateRangeParser dateParser = new DateRangeParser();
    CoordinateBounds coordinates = new CoordinateBounds();
    DateBounds dates = new DateBounds();
    Set<String> taxa = new TreeSet<>();
    SectionState geographic = new SectionState();
    SectionState temporal = new SectionState();
    SectionState taxonomic = new SectionState();
    // whether a field is mapped in any deployments mapping, to tell which one is missing
    boolean latitudeMapped = false;
    boolean longitudeMapped = false;
    boolean deploymentStartMapped = false;
    boolean deploymentEndMapped = false;
    boolean deploymentsMapped = false;
    boolean observationsMapped = false;

    for (DataPackageMapping mapping : resource.getDataPackageMappings()) {
      String tableSchema = Optional.ofNullable(mapping.getDataPackageTableSchemaName())
          .map(DataPackageTableSchemaName::getName)
          .orElse(null);
      List<MappedField> allFields = mapping.getFields() == null
          ? Collections.emptyList()
          : mapping.getFields().stream().map(MappedField::of).collect(Collectors.toList());

      if (CAMTRAP_DEPLOYMENTS.equals(tableSchema)) {
        deploymentsMapped = true;
        MappedField latitude = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_LATITUDE));
        MappedField longitude = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_LONGITUDE));
        MappedField deploymentStart = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_DEPLOYMENT_START));
        MappedField deploymentEnd = MappedField.of(mapping.getField(CAMTRAP_DEPLOYMENTS_DEPLOYMENT_END));
        latitudeMapped |= latitude.isMapped();
        longitudeMapped |= longitude.isMapped();
        deploymentStartMapped |= deploymentStart.isMapped();
        deploymentEndMapped |= deploymentEnd.isMapped();

        boolean geographicMapped = latitude.isMapped() && longitude.isMapped();
        boolean temporalMapped = deploymentStart.isMapped() && deploymentEnd.isMapped();
        geographic.mapped |= geographicMapped;
        temporal.mapped |= temporalMapped;

        if (!geographicMapped && !temporalMapped) {
          continue;
        }

        boolean read = readRows(mapping.getSource(), mapping.getFilter(), allFields, row -> {
          if (geographicMapped) {
            coordinates.add(latitude.value(row), longitude.value(row));
          }
          if (temporalMapped) {
            // only deployments with both dates valid
            Optional<DateRange> start = dateParser.parse(deploymentStart.value(row));
            Optional<DateRange> end = dateParser.parse(deploymentEnd.value(row));
            if (start.isPresent() && end.isPresent()) {
              dates.add(start.get().first(), end.get().last());
            }
          }
          return true;
        });

        if (!read) {
          geographic.failed |= geographicMapped;
          temporal.failed |= temporalMapped;
        }
      } else if (CAMTRAP_OBSERVATIONS.equals(tableSchema)) {
        observationsMapped = true;
        MappedField scientificName = MappedField.of(mapping.getField(CAMTRAP_OBSERVATIONS_SCIENTIFIC_NAME));
        taxonomic.mapped |= scientificName.isMapped();

        if (!scientificName.isMapped() || taxa.size() >= CAMTRAP_TAXON_LIMIT) {
          continue;
        }

        boolean read = readRows(mapping.getSource(), mapping.getFilter(), allFields, row -> {
          Optional.ofNullable(scientificName.value(row)).ifPresent(taxa::add);
          return taxa.size() < CAMTRAP_TAXON_LIMIT;
        });

        if (!read) {
          taxonomic.failed = true;
        }
      }
    }

    if (taxa.size() >= CAMTRAP_TAXON_LIMIT) {
      LOG.debug("Inferred taxonomic scope of resource {} is limited to {} taxa", resource.getShortname(), CAMTRAP_TAXON_LIMIT);
    }

    InferredCamtrapGeographicScope geographicScope = new InferredCamtrapGeographicScope();
    if (geographic.failed) {
      geographicScope.addError(CAMTRAP_SERVER_ERROR);
    } else if (!deploymentsMapped) {
      geographicScope.addError("datapackagemetadata.error.noDeploymentsMapped");
    } else if (!latitudeMapped && longitudeMapped) {
      geographicScope.addError("datapackagemetadata.geographic.error.latitudeNotMapped");
    } else if (latitudeMapped && !longitudeMapped) {
      geographicScope.addError("datapackagemetadata.geographic.error.longitudeNotMapped");
    } else if (!geographic.mapped) {
      geographicScope.addError("datapackagemetadata.geographic.error.fieldsNotMapped");
    } else if (!coordinates.hasData()) {
      geographicScope.addError("datapackagemetadata.error.noValidData");
    } else {
      geographicScope.setMinLatitude(coordinates.minLatitude);
      geographicScope.setMinLongitude(coordinates.minLongitude);
      geographicScope.setMaxLatitude(coordinates.maxLatitude);
      geographicScope.setMaxLongitude(coordinates.maxLongitude);
      geographicScope.setInferred(true);
    }

    InferredCamtrapTemporalScope temporalScope = new InferredCamtrapTemporalScope();
    if (temporal.failed) {
      temporalScope.addError(CAMTRAP_SERVER_ERROR);
    } else if (!deploymentsMapped) {
      temporalScope.addError("datapackagemetadata.error.noDeploymentsMapped");
    } else if (!deploymentStartMapped && deploymentEndMapped) {
      temporalScope.addError("datapackagemetadata.temporal.error.deploymentStartNotMapped");
    } else if (deploymentStartMapped && !deploymentEndMapped) {
      temporalScope.addError("datapackagemetadata.temporal.error.deploymentEndNotMapped");
    } else if (!temporal.mapped) {
      temporalScope.addError("datapackagemetadata.temporal.error.fieldsNotMapped");
    } else if (!dates.hasData()) {
      temporalScope.addError("datapackagemetadata.error.noValidData");
    } else {
      temporalScope.setStartDate(toDate(dates.earliest));
      temporalScope.setEndDate(toDate(dates.latest));
      temporalScope.setInferred(true);
    }

    InferredCamtrapTaxonomicScope taxonomicScope = new InferredCamtrapTaxonomicScope();
    if (taxonomic.failed) {
      taxonomicScope.addError(CAMTRAP_SERVER_ERROR);
    } else if (!observationsMapped) {
      taxonomicScope.addError("datapackagemetadata.error.noObservationsMapped");
    } else if (!taxonomic.mapped) {
      taxonomicScope.addError("datapackagemetadata.taxonomic.error.scientificNameNotMapped");
    } else if (taxa.isEmpty()) {
      taxonomicScope.addError("datapackagemetadata.error.noValidData");
    } else {
      taxonomicScope.setData(taxa.stream().map(name -> {
        Taxonomic t = new Taxonomic();
        t.setScientificName(name);
        return t;
      }).collect(Collectors.toList()));
      taxonomicScope.setInferred(true);
    }

    InferredCamtrapMetadata inferredMetadata = new InferredCamtrapMetadata();
    inferredMetadata.setInferredGeographicScope(geographicScope);
    inferredMetadata.setInferredTemporalScope(temporalScope);
    inferredMetadata.setInferredTaxonomicScope(taxonomicScope);
    inferredMetadata.setLastModified(started);

    return inferredMetadata;
  }

  /**
   * Passes the rows of the source that match the mapping filter to the row handler, until it returns false.
   *
   * @return false if the source could not be read
   */
  private boolean readRows(Source source, RecordFilter filter, Collection<MappedField> fields,
                           Predicate<String[]> rowHandler) {
    try (ClosableReportingIterator<String[]> iter = sourceManager.rowIterator(source)) {
      while (iter.hasNext()) {
        String[] row = iter.next();
        if (row == null || row.length == 0 || iter.hasRowError() || !matchesFilter(filter, row, fields)) {
          continue;
        }
        if (!rowHandler.test(row)) {
          break;
        }
      }
      return true;
      // Catch ParseException, occurs for Excel files. Find out why
    } catch (com.github.pjfanning.xlsx.exceptions.ParseException e) {
      LOG.warn("Error while reading source {} to infer metadata, using the rows read so far: {}",
          source != null ? source.getName() : null, e.getMessage());
      return true;
    } catch (Exception e) {
      LOG.error("Error while trying to infer metadata from source {}", source != null ? source.getName() : null, e);
      return false;
    }
  }

  /**
   * Same filtering as during the publication.
   */
  private static boolean matchesFilter(RecordFilter filter, String[] row, Collection<MappedField> fields) {
    if (filter == null || filter.getColumn() == null || filter.getComparator() == null || filter.getParam() == null) {
      return true;
    }

    if (filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation) {
      String[] translated = row.clone();
      fields.forEach(field -> field.translate(translated));
      return filter.matches(translated);
    }

    return filter.matches(row);
  }

  private static LocalDate toLocalDate(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.toLocalDate() : null;
  }

  /**
   * Midnight in the default time zone. Uses the Julian-Gregorian calendar of java.util.Date, so dates before 1582 are
   * formatted back unchanged.
   */
  private static Date toDate(LocalDate date) {
    Calendar calendar = new GregorianCalendar();
    calendar.clear();
    calendar.set(Calendar.YEAR, date.getYear());
    calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
    calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
    return calendar.getTime();
  }

  /**
   * The fields of a DwC-A or DwC-DP mapping that EML coverages are inferred from.
   */
  private static final class EmlMapping {
    private final Source source;
    private final RecordFilter filter;
    // all fields, for translating rows before filtering
    private final Collection<MappedField> fields;
    private MappedField latitude = MappedField.UNMAPPED;
    private MappedField longitude = MappedField.UNMAPPED;
    private MappedField eventDate = MappedField.UNMAPPED;
    private final Map<String, MappedField> ranks = new LinkedHashMap<>();
    // DwC-DP identifications: superseded ones are left out of the taxonomic coverage
    private MappedField acceptedIdentification = MappedField.UNMAPPED;

    private EmlMapping(Source source, RecordFilter filter, Collection<MappedField> fields) {
      this.source = source;
      this.filter = filter;
      this.fields = fields;
    }

    private void addRank(String rank, MappedField field) {
      if (field.isMapped()) {
        ranks.put(rank, field);
      }
    }

    private boolean isSupersededIdentification(String[] row) {
      String accepted = acceptedIdentification.value(row);
      return accepted != null && NOT_ACCEPTED_VALUES.contains(accepted.toLowerCase(Locale.ENGLISH));
    }
  }

  /**
   * An EML coverage, inferred from the mappings that have its fields.
   */
  private abstract static class EmlCoverageInference {
    // at least one mapping has the fields of the coverage
    private boolean fieldsMapped;
    // reading the source of one of these mappings failed
    private boolean readFailed;

    /**
     * @return whether the mapping has the fields the coverage is inferred from
     */
    abstract boolean hasFields(EmlMapping mapping);

    /**
     * Adds the values of a source row, of a mapping that has the fields.
     */
    abstract void add(EmlMapping mapping, String[] row);

    /**
     * @return whether at least one valid value was added
     */
    abstract boolean hasData();

    /**
     * @return the error when no mapping has the fields
     */
    abstract String fieldsNotMappedError();

    void markFieldsMapped() {
      fieldsMapped = true;
    }

    void markReadFailed() {
      readFailed = true;
    }

    /**
     * @return the error explaining why the coverage could not be inferred, or null if it was inferred
     */
    String error(boolean mappingsExist) {
      if (readFailed) {
        return EML_SERVER_ERROR;
      } else if (!mappingsExist) {
        return "eml.error.noMappings";
      } else if (!fieldsMapped) {
        return fieldsNotMappedError();
      } else if (!hasData()) {
        return "eml.error.noValidData";
      }
      return null;
    }
  }

  /**
   * Bounding box of the decimal coordinates.
   */
  private static final class GeographicCoverageInference extends EmlCoverageInference {
    private final CoordinateBounds coordinates = new CoordinateBounds();

    @Override
    boolean hasFields(EmlMapping mapping) {
      return mapping.latitude.isMapped() && mapping.longitude.isMapped();
    }

    @Override
    void add(EmlMapping mapping, String[] row) {
      coordinates.add(mapping.latitude.value(row), mapping.longitude.value(row));
    }

    @Override
    boolean hasData() {
      return coordinates.hasData();
    }

    @Override
    String fieldsNotMappedError() {
      return "eml.geospatialCoverages.error.fieldsNotMapped";
    }

    private InferredEmlGeographicCoverage result(boolean mappingsExist) {
      InferredEmlGeographicCoverage result = new InferredEmlGeographicCoverage();
      String error = error(mappingsExist);

      if (error != null) {
        result.addError(error);
      } else {
        GeospatialCoverage geospatialCoverage = new GeospatialCoverage();
        geospatialCoverage.setBoundingCoordinates(
            new BBox(
                new Point(coordinates.minLatitude, coordinates.minLongitude),
                new Point(coordinates.maxLatitude, coordinates.maxLongitude)));
        result.setData(geospatialCoverage);
        result.setInferred(true);
      }

      return result;
    }
  }

  /**
   * Earliest and latest event date.
   */
  private static final class TemporalCoverageInference extends EmlCoverageInference {
    private final DateRangeParser dateParser = new DateRangeParser();
    private final DateBounds dates = new DateBounds();

    @Override
    boolean hasFields(EmlMapping mapping) {
      return mapping.eventDate.isMapped();
    }

    @Override
    void add(EmlMapping mapping, String[] row) {
      dateParser.parse(mapping.eventDate.value(row)).ifPresent(range -> dates.add(range.first(), range.last()));
    }

    @Override
    boolean hasData() {
      return dates.hasData();
    }

    @Override
    String fieldsNotMappedError() {
      return "eml.temporalCoverages.error.fieldsNotMapped";
    }

    private InferredEmlTemporalCoverage result(boolean mappingsExist) {
      InferredEmlTemporalCoverage result = new InferredEmlTemporalCoverage();
      String error = error(mappingsExist);

      if (error != null) {
        result.addError(error);
      } else {
        TemporalCoverage temporalCoverage = new TemporalCoverage();
        temporalCoverage.setStartDate(toDate(dates.earliest));
        temporalCoverage.setEndDate(toDate(dates.latest));
        result.setData(temporalCoverage);
        result.setInferred(true);
      }

      return result;
    }
  }

  /**
   * Distinct names of each rank, kingdom to family.
   */
  private static final class TaxonomicCoverageInference extends EmlCoverageInference {
    private final Map<String, RankTaxa> taxaByRank = new LinkedHashMap<>();

    private TaxonomicCoverageInference() {
      EML_RANK_TERMS.keySet().forEach(rank -> taxaByRank.put(rank, new RankTaxa()));
    }

    @Override
    boolean hasFields(EmlMapping mapping) {
      return !mapping.ranks.isEmpty();
    }

    @Override
    void add(EmlMapping mapping, String[] row) {
      if (mapping.isSupersededIdentification(row)) {
        return;
      }
      mapping.ranks.forEach((rank, field) -> taxaByRank.get(rank).add(field.value(row)));
    }

    @Override
    boolean hasData() {
      return taxaByRank.values().stream().anyMatch(taxa -> taxa.limitExceeded || !taxa.names.isEmpty());
    }

    @Override
    String fieldsNotMappedError() {
      return "eml.taxonomicCoverages.error.fieldsNotMapped";
    }

    private InferredEmlTaxonomicCoverage result(boolean mappingsExist,
                                                Function<TaxonomicCoverage, OrganizedTaxonomicCoverage> organizer) {
      InferredEmlTaxonomicCoverage result = new InferredEmlTaxonomicCoverage();
      String error = error(mappingsExist);

      taxaByRank.forEach((rank, taxa) -> {
        if (taxa.limitExceeded) {
          result.addRankWarning(rank, "eml.warning.limitExceeded");
        }
      });

      if (error != null) {
        result.addError(error);
      } else {
        List<TaxonKeyword> keywords = new ArrayList<>();
        taxaByRank.forEach((rank, taxa) -> taxa.names.forEach(name -> keywords.add(new TaxonKeyword(name, rank, null))));
        TaxonomicCoverage taxonomicCoverage = new TaxonomicCoverage();
        taxonomicCoverage.setTaxonKeywords(keywords);
        result.setData(taxonomicCoverage);
        result.setOrganizedData(organizer.apply(taxonomicCoverage));
        result.setInferred(true);
      }

      return result;
    }
  }

  /**
   * Whether an inferred section is mapped in at least one mapping, and whether reading one of them failed.
   */
  private static final class SectionState {
    private boolean mapped;
    private boolean failed;
  }

  /**
   * A mapped field reads its value from a source row the same way it is published.
   */
  private record MappedField(Integer index, String defaultValue, Map<String, String> translation) {
    private static final MappedField UNMAPPED = new MappedField(null, null, null);

    private static MappedField of(PropertyMapping mapping) {
      return new MappedField(mapping.getIndex(), mapping.getDefaultValue(), mapping.getTranslation());
    }

    private static MappedField of(DataPackageFieldMapping mapping) {
      return mapping == null
          ? UNMAPPED
          : new MappedField(mapping.getIndex(), mapping.getDefaultValue(), mapping.getTranslation());
    }

    // same as DataPackageFieldMapping#isMapped, a blank default value doesn't count
    private boolean isMapped() {
      return index != null || StringUtils.isNotBlank(defaultValue);
    }

    /**
     * @return the translated value, the default value if the value is missing, or null if blank
     */
    private String value(String[] row) {
      String value = null;
      if (index != null && index >= 0 && index < row.length) {
        value = row[index];
        // translations are TreeMaps, which reject null keys
        if (value != null && translation != null && translation.containsKey(value)) {
          value = translation.get(value);
        }
      }
      if (value == null) {
        value = defaultValue;
      }
      return StringUtils.trimToNull(value);
    }

    /**
     * Replaces the source value by its translation in the row.
     */
    private void translate(String[] row) {
      if (translation != null && index != null && index >= 0 && index < row.length
          && row[index] != null && translation.containsKey(row[index])) {
        row[index] = translation.get(row[index]);
      }
    }
  }

  /**
   * Bounding box of all valid coordinates.
   */
  private static final class CoordinateBounds {
    private double minLatitude = Double.POSITIVE_INFINITY;
    private double maxLatitude = Double.NEGATIVE_INFINITY;
    private double minLongitude = Double.POSITIVE_INFINITY;
    private double maxLongitude = Double.NEGATIVE_INFINITY;

    private void add(String latitude, String longitude) {
      // the parser rounds NaN to 0 and reports it as valid
      if (latitude == null || longitude == null || isNaN(latitude) || isNaN(longitude)) {
        return;
      }

      OccurrenceParseResult<LatLng> result = CoordinateParseUtils.parseLatLng(latitude, longitude);
      LatLng latLng = result.getPayload();
      if (!result.isSuccessful() || latLng == null || latLng.getLat() == null || latLng.getLng() == null) {
        return;
      }

      minLatitude = Math.min(minLatitude, latLng.getLat());
      maxLatitude = Math.max(maxLatitude, latLng.getLat());
      minLongitude = Math.min(minLongitude, latLng.getLng());
      maxLongitude = Math.max(maxLongitude, latLng.getLng());
    }

    private boolean hasData() {
      return minLatitude <= maxLatitude;
    }

    private static boolean isNaN(String value) {
      try {
        return Double.isNaN(Double.parseDouble(value));
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }

  /**
   * First and last day of a date or date range, e.g. 2020-01-01 and 2020-12-31 for "2020".
   */
  private record DateRange(LocalDate first, LocalDate last) {
  }

  /**
   * Parses dates and date ranges, remembering the most recent distinct values: in big datasets many records share
   * the same dates, and parsing is by far the slowest part of inferring.
   * <p>
   * Not thread-safe, one per inferring run.
   */
  private static final class DateRangeParser {
    private static final int CACHE_SIZE = 10_000;

    private TemporalRangeParser parser;
    private final Map<String, Optional<DateRange>> cache = new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Optional<DateRange>> eldest) {
        return size() > CACHE_SIZE;
      }
    };

    private Optional<DateRange> parse(String value) {
      if (value == null) {
        return Optional.empty();
      }
      return cache.computeIfAbsent(value, this::parseUncached);
    }

    private Optional<DateRange> parseUncached(String value) {
      if (parser == null) {
        parser = TemporalRangeParser.builder().create();
      }

      OccurrenceParseResult<IsoDateInterval> result = parser.parse(value);
      IsoDateInterval interval = result.isSuccessful() ? result.getPayload() : null;
      if (interval == null || interval.getFrom() == null) {
        return Optional.empty();
      }

      // offsets are ignored, the local date is used
      Temporal to = interval.getTo() != null ? interval.getTo() : interval.getFrom();
      LocalDate first = toLocalDate(TemporalAccessorUtils.toEarliestLocalDateTime(interval.getFrom(), true));
      LocalDate last = toLocalDate(TemporalAccessorUtils.toLatestLocalDateTime(to, true));
      return first == null || last == null ? Optional.empty() : Optional.of(new DateRange(first, last));
    }
  }

  /**
   * Earliest and latest of all valid dates.
   */
  private static final class DateBounds {
    private LocalDate earliest;
    private LocalDate latest;

    private void add(LocalDate start, LocalDate end) {
      if (start == null || end == null) {
        return;
      }
      // e.g. a deployment ending before it starts
      if (end.isBefore(start)) {
        LocalDate swap = start;
        start = end;
        end = swap;
      }

      if (earliest == null || start.isBefore(earliest)) {
        earliest = start;
      }
      if (latest == null || end.isAfter(latest)) {
        latest = end;
      }
    }

    private boolean hasData() {
      return earliest != null;
    }
  }

  /**
   * Distinct names of a rank, dropped once there are more than {@link #TAXON_LIMIT}.
   */
  private static final class RankTaxa {
    private final Set<String> names = new TreeSet<>();
    private boolean limitExceeded;

    private void add(String name) {
      if (name == null || limitExceeded) {
        return;
      }

      names.add(name);
      if (names.size() > TAXON_LIMIT) {
        names.clear();
        limitExceeded = true;
      }
    }
  }
}
