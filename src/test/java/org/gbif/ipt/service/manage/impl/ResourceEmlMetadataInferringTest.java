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

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.mock.MockVocabulariesManager;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTaxonomicCoverage;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.metadata.eml.ipt.model.BBox;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceEmlMetadataInferringTest extends IptBaseTest {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  // source columns
  private static final int ID = 0;
  private static final int LATITUDE = 1;
  private static final int LONGITUDE = 2;
  private static final int EVENT_DATE = 3;
  private static final int KINGDOM = 4;
  private static final int PHYLUM = 5;
  private static final int CLASS = 6;
  private static final int ORDER = 7;
  private static final int FAMILY = 8;

  private final Map<String, Supplier<InMemoryRowIterator>> sources = new HashMap<>();
  private ResourceMetadataInferringServiceImpl metadataInferringService;

  @BeforeEach
  public void setUp() throws Exception {
    SourceManager sourceManager = mock(SourceManager.class);
    when(sourceManager.rowIterator(any(Source.class))).thenAnswer(invocation -> {
      Source source = invocation.getArgument(0);
      Supplier<InMemoryRowIterator> rows = sources.get(source.getName());
      if (rows == null) {
        throw new SourceException("Source " + source.getName() + " can't be read");
      }
      return rows.get();
    });
    metadataInferringService = new ResourceMetadataInferringServiceImpl(sourceManager, new MockVocabulariesManager());
  }

  @DisplayName("Resource without mappings")
  @Test
  public void testNoMappings() {
    InferredEmlMetadata metadata = infer(new Resource());

    assertOnlyError("eml.error.noMappings", metadata.getInferredGeographicCoverage().getErrors());
    assertOnlyError("eml.error.noMappings", metadata.getInferredTemporalCoverage().getErrors());
    assertOnlyError("eml.error.noMappings", metadata.getInferredTaxonomicCoverage().getErrors());
    assertFalse(metadata.getInferredGeographicCoverage().isInferred());
    assertFalse(metadata.getInferredTemporalCoverage().isInferred());
    assertFalse(metadata.getInferredTaxonomicCoverage().isInferred());
    assertNotNull(metadata.getLastModified());
  }

  @DisplayName("Occurrences with all fields mapped, invalid values are skipped")
  @Test
  public void testOccurrences() {
    addSource("occurrences",
        row("1", "10.5", "20.5", "2005", "Animalia", "Chordata", "Aves", "Passeriformes", "Corvidae"),
        row("2", "-5", "-30", "2010-06", "Plantae", "Tracheophyta", "Magnoliopsida", "Rosales", "Rosaceae"),
        row("3", "45", "100", "2009-03-04T23:30:00-05:00", "Animalia", "Chordata", "Mammalia", "Carnivora", "Canidae"),
        row("4", "95", "200", "not a date", "", "", "", "", ""),
        row("5", "1", "2", "2000-02-01/2000-03-15", " Animalia ", "Chordata", "Aves", "Passeriformes", "Corvidae"));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", allFields()));

    InferredEmlMetadata metadata = infer(resource);

    InferredEmlGeographicCoverage geographic = metadata.getInferredGeographicCoverage();
    BBox bbox = geographic.getData().getBoundingCoordinates();
    assertAll("geographic",
        () -> assertTrue(geographic.isInferred()),
        () -> assertTrue(geographic.getErrors().isEmpty()),
        () -> assertEquals(-5.0, bbox.getMin().getLatitude()),
        () -> assertEquals(-30.0, bbox.getMin().getLongitude()),
        () -> assertEquals(45.0, bbox.getMax().getLatitude()),
        () -> assertEquals(100.0, bbox.getMax().getLongitude()));

    InferredEmlTemporalCoverage temporal = metadata.getInferredTemporalCoverage();
    assertAll("temporal",
        () -> assertTrue(temporal.isInferred()),
        () -> assertTrue(temporal.getErrors().isEmpty()),
        // range start
        () -> assertEquals("2000-02-01", format(temporal.getData().getStartDate())),
        // end of the month
        () -> assertEquals("2010-06-30", format(temporal.getData().getEndDate())));

    InferredEmlTaxonomicCoverage taxonomic = metadata.getInferredTaxonomicCoverage();
    assertAll("taxonomic",
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertTrue(taxonomic.getErrors().isEmpty()),
        () -> assertTrue(taxonomic.getRankWarnings().isEmpty()),
        // by rank, then by name
        () -> assertEquals(
            Arrays.asList("Animalia", "Plantae", "Chordata", "Tracheophyta", "Aves", "Magnoliopsida", "Mammalia",
                "Carnivora", "Passeriformes", "Rosales", "Canidae", "Corvidae", "Rosaceae"),
            taxonomic.getData().getTaxonKeywords().stream().map(TaxonKeyword::getScientificName).collect(Collectors.toList())),
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Arrays.asList("Chordata", "Tracheophyta"), names(taxonomic, Constants.PHYLUM)),
        () -> assertEquals(Arrays.asList("Aves", "Magnoliopsida", "Mammalia"), names(taxonomic, Constants.CLASS)),
        () -> assertEquals(Arrays.asList("Carnivora", "Passeriformes", "Rosales"), names(taxonomic, Constants.ORDER)),
        () -> assertEquals(Arrays.asList("Canidae", "Corvidae", "Rosaceae"), names(taxonomic, Constants.FAMILY)),
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), organizedNames(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Arrays.asList("Canidae", "Corvidae", "Rosaceae"), organizedNames(taxonomic, Constants.FAMILY)),
        () -> assertEquals(Collections.emptyList(), organizedNames(taxonomic, "genus")));
  }

  @DisplayName("Temporal coverage covers whole years, and uses local dates of date times")
  @Test
  public void testTemporalCoverageOfYearsAndDateTimes() {
    // the extremes are never in the first row
    addSource("years", row("1", null, null, "2000"), row("2", null, null, "2001"), row("3", null, null, "1999"));
    addSource("datetimes",
        row("1", null, null, "2002-12-31T23:30:00-05:00"),
        row("2", null, null, "2003-01-01T00:30:00+05:00"),
        row("3", null, null, "2002-06-15T12:00:00"));
    PropertyMapping eventDate = field(DwcTerm.eventDate, EVENT_DATE, null);

    InferredEmlTemporalCoverage years =
        infer(resource(mapping(Constants.DWC_ROWTYPE_EVENT, "years", eventDate))).getInferredTemporalCoverage();
    InferredEmlTemporalCoverage dateTimes =
        infer(resource(mapping(Constants.DWC_ROWTYPE_EVENT, "datetimes", eventDate))).getInferredTemporalCoverage();

    assertAll(
        () -> assertEquals("1999-01-01", format(years.getData().getStartDate())),
        () -> assertEquals("2001-12-31", format(years.getData().getEndDate())),
        () -> assertEquals("2002-06-15", format(dateTimes.getData().getStartDate())),
        () -> assertEquals("2003-01-01", format(dateTimes.getData().getEndDate())));
  }

  @DisplayName("Dates without a day: year-month first, year ranges, a year before full dates (issue #2938)")
  @Test
  public void testDatesWithoutDay() {
    addSource("year-month", row("1", null, null, "1951-07"), row("2", null, null, "1951-05-20"));
    addSource("year-range", row("1", null, null, "2006/2007"));
    addSource("year-first",
        row("1", null, null, "2023"),
        row("2", null, null, "2004-03-15"),
        row("3", null, null, "2025-02-01"),
        row("4", null, null, "2010-10-10"));
    PropertyMapping eventDate = field(DwcTerm.eventDate, EVENT_DATE, null);

    InferredEmlTemporalCoverage yearMonth =
        infer(resource(mapping(Constants.DWC_ROWTYPE_EVENT, "year-month", eventDate))).getInferredTemporalCoverage();
    InferredEmlTemporalCoverage yearRange =
        infer(resource(mapping(Constants.DWC_ROWTYPE_EVENT, "year-range", eventDate))).getInferredTemporalCoverage();
    InferredEmlTemporalCoverage yearFirst =
        infer(resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "year-first", eventDate))).getInferredTemporalCoverage();

    assertAll(
        () -> assertTrue(yearMonth.getErrors().isEmpty(), "Errors: " + yearMonth.getErrors()),
        () -> assertEquals("1951-05-20", format(yearMonth.getData().getStartDate())),
        () -> assertEquals("1951-07-31", format(yearMonth.getData().getEndDate())),
        () -> assertEquals("2006-01-01", format(yearRange.getData().getStartDate())),
        () -> assertEquals("2007-12-31", format(yearRange.getData().getEndDate())),
        () -> assertEquals("2004-03-15", format(yearFirst.getData().getStartDate())),
        () -> assertEquals("2025-02-01", format(yearFirst.getData().getEndDate())));
  }

  @DisplayName("Default values and translations are applied like during the publication")
  @Test
  public void testDefaultValuesAndTranslations() {
    addSource("occurrences",
        row("1", "10", null, "2020-01-01", "A"),
        row("2", "20", null, "2020-01-02", "P"),
        row("3", "30", null, "2020-01-03", null));
    PropertyMapping kingdom = field(DwcTerm.kingdom, KINGDOM, "Fungi");
    Map<String, String> translation = new HashMap<>();
    translation.put("A", "Animalia");
    translation.put("P", "Plantae");
    kingdom.setTranslation(translation);
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        // all longitudes come from the default value
        field(DwcTerm.decimalLongitude, null, "5.5"),
        field(DwcTerm.eventDate, EVENT_DATE, null),
        kingdom,
        // all classes come from the default value
        field(DwcTerm.class_, null, "Insecta")));

    InferredEmlMetadata metadata = infer(resource);

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    InferredEmlTaxonomicCoverage taxonomic = metadata.getInferredTaxonomicCoverage();
    assertAll(
        () -> assertEquals(10.0, bbox.getMin().getLatitude()),
        () -> assertEquals(30.0, bbox.getMax().getLatitude()),
        () -> assertEquals(5.5, bbox.getMin().getLongitude()),
        () -> assertEquals(5.5, bbox.getMax().getLongitude()),
        () -> assertEquals(Arrays.asList("Animalia", "Fungi", "Plantae"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Collections.singletonList("Insecta"), names(taxonomic, Constants.CLASS)));
  }

  @DisplayName("Coordinates from default values in a mapping that isn't the last one")
  @Test
  public void testDefaultValuesInEarlierMappings() {
    addSource("occurrences", row("1", "10", "20"), row("2", "-10", "-20"));
    addSource("events", row("e1", null, null, "2020-01-01"));
    // the last place and time mapping has no coordinates
    PropertyMapping eventDate = field(DwcTerm.eventDate, EVENT_DATE, null);
    Resource longitudeDefault = resource(
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
            field(DwcTerm.decimalLatitude, LATITUDE, null), field(DwcTerm.decimalLongitude, null, "5")),
        mapping(Constants.DWC_ROWTYPE_EVENT, "events", eventDate));
    Resource latitudeDefault = resource(
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
            field(DwcTerm.decimalLatitude, null, "-40"), field(DwcTerm.decimalLongitude, LONGITUDE, null)),
        mapping(Constants.DWC_ROWTYPE_EVENT, "events", eventDate));

    InferredEmlGeographicCoverage fromLongitudeDefault = infer(longitudeDefault).getInferredGeographicCoverage();
    InferredEmlGeographicCoverage fromLatitudeDefault = infer(latitudeDefault).getInferredGeographicCoverage();

    assertTrue(fromLongitudeDefault.isInferred(), "Errors: " + fromLongitudeDefault.getErrors());
    assertTrue(fromLatitudeDefault.isInferred(), "Errors: " + fromLatitudeDefault.getErrors());
    BBox longitudeBox = fromLongitudeDefault.getData().getBoundingCoordinates();
    BBox latitudeBox = fromLatitudeDefault.getData().getBoundingCoordinates();
    assertAll(
        () -> assertEquals(-10.0, longitudeBox.getMin().getLatitude()),
        () -> assertEquals(10.0, longitudeBox.getMax().getLatitude()),
        () -> assertEquals(5.0, longitudeBox.getMin().getLongitude()),
        () -> assertEquals(5.0, longitudeBox.getMax().getLongitude()),
        () -> assertEquals(-40.0, latitudeBox.getMin().getLatitude()),
        () -> assertEquals(-40.0, latitudeBox.getMax().getLatitude()),
        () -> assertEquals(-20.0, latitudeBox.getMin().getLongitude()),
        () -> assertEquals(20.0, latitudeBox.getMax().getLongitude()));
  }

  @DisplayName("A blank default value doesn't count as mapped")
  @Test
  public void testBlankDefaultValue() {
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        field(DwcTerm.decimalLatitude, null, " "),
        field(DwcTerm.decimalLongitude, null, ""),
        field(DwcTerm.eventDate, null, ""),
        field(DwcTerm.kingdom, null, " ")));

    InferredEmlMetadata metadata = infer(resource);

    assertOnlyError("eml.geospatialCoverages.error.fieldsNotMapped", metadata.getInferredGeographicCoverage().getErrors());
    assertOnlyError("eml.temporalCoverages.error.fieldsNotMapped", metadata.getInferredTemporalCoverage().getErrors());
    assertOnlyError("eml.taxonomicCoverages.error.fieldsNotMapped", metadata.getInferredTaxonomicCoverage().getErrors());
  }

  @DisplayName("Rows rejected by the mapping filter are skipped")
  @Test
  public void testFilter() {
    addSource("occurrences",
        row("1", "10", "10", "2020-01-01", "A"),
        row("2", "80", "80", "2030-01-01", "P"));
    PropertyMapping kingdom = field(DwcTerm.kingdom, KINGDOM, null);
    kingdom.setTranslation(Collections.singletonMap("A", "Animalia"));
    ExtensionMapping mapping = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        field(DwcTerm.decimalLongitude, LONGITUDE, null),
        field(DwcTerm.eventDate, EVENT_DATE, null),
        kingdom);
    RecordFilter filter = new RecordFilter();
    filter.setColumn(KINGDOM);
    filter.setComparator(RecordFilter.Comparator.Equals);
    filter.setParam("Animalia");
    filter.setFilterTime(RecordFilter.FilterTime.AfterTranslation);
    mapping.setFilter(filter);

    InferredEmlMetadata metadata = infer(resource(mapping));

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    InferredEmlTemporalCoverage temporal = metadata.getInferredTemporalCoverage();
    assertAll(
        () -> assertEquals(10.0, bbox.getMax().getLatitude()),
        () -> assertEquals("2020-01-01", format(temporal.getData().getEndDate())),
        () -> assertEquals(Collections.singletonList("Animalia"), names(metadata.getInferredTaxonomicCoverage(), Constants.KINGDOM)));

    // before translation no row matches
    filter.setFilterTime(RecordFilter.FilterTime.BeforeTranslation);
    InferredEmlMetadata noRows = infer(resource(mapping));
    assertOnlyError("eml.error.noValidData", noRows.getInferredGeographicCoverage().getErrors());
  }

  @DisplayName("Errors for unmapped fields and for missing valid data")
  @Test
  public void testFieldsNotMappedAndNoValidData() {
    addSource("occurrences", row("1", "not a latitude", "10", "not a date", "   "));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        field(DwcTerm.decimalLongitude, LONGITUDE, null),
        field(DwcTerm.eventDate, EVENT_DATE, null),
        field(DwcTerm.kingdom, KINGDOM, null)));
    Resource resourceWithoutFields = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        // latitude alone is not enough
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        field(DwcTerm.scientificName, KINGDOM, null)));

    InferredEmlMetadata noValidData = infer(resource);
    InferredEmlMetadata notMapped = infer(resourceWithoutFields);

    assertOnlyError("eml.error.noValidData", noValidData.getInferredGeographicCoverage().getErrors());
    assertOnlyError("eml.error.noValidData", noValidData.getInferredTemporalCoverage().getErrors());
    assertOnlyError("eml.error.noValidData", noValidData.getInferredTaxonomicCoverage().getErrors());
    assertOnlyError("eml.geospatialCoverages.error.fieldsNotMapped", notMapped.getInferredGeographicCoverage().getErrors());
    assertOnlyError("eml.temporalCoverages.error.fieldsNotMapped", notMapped.getInferredTemporalCoverage().getErrors());
    assertOnlyError("eml.taxonomicCoverages.error.fieldsNotMapped", notMapped.getInferredTaxonomicCoverage().getErrors());
  }

  @DisplayName("Event core provides place and time, the occurrence extension taxa, other extensions are ignored")
  @Test
  public void testEventCoreWithExtensions() {
    addSource("events", row("e1", "10", "10", "2020-01-01", "Ignored"));
    addSource("occurrences", row("o1", "50", "50", "2030-01-01", "Animalia"));
    addSource("taxa", row("t1", null, null, null, "Plantae"));
    addSource("other", row("x1", "60", "60", "2040-01-01", "Fungi"));
    Resource resource = resource(
        mapping(Constants.DWC_ROWTYPE_EVENT, "events", allFields()),
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", field(DwcTerm.kingdom, KINGDOM, null)),
        mapping(Constants.DWC_ROWTYPE_TAXON, "taxa", field(DwcTerm.kingdom, KINGDOM, null)),
        mapping("http://rs.gbif.org/terms/1.0/Other", "other", allFields()));

    InferredEmlMetadata metadata = infer(resource);

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    assertAll(
        () -> assertEquals(10.0, bbox.getMax().getLatitude()),
        () -> assertEquals("2020-01-01", format(metadata.getInferredTemporalCoverage().getData().getEndDate())),
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), names(metadata.getInferredTaxonomicCoverage(), Constants.KINGDOM)));
  }

  @DisplayName("Ranks with too many names are dropped with a warning")
  @Test
  public void testTaxonLimit() {
    List<String[]> rows = new ArrayList<>();
    for (int i = 0; i <= ResourceMetadataInferringServiceImpl.TAXON_LIMIT; i++) {
      // exactly TAXON_LIMIT orders, one more family
      rows.add(row(String.valueOf(i), null, null, null, "Animalia", null, null,
          "Order" + (i % ResourceMetadataInferringServiceImpl.TAXON_LIMIT), "Family" + i));
    }
    sources.put("taxa", () -> new InMemoryRowIterator(rows));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_TAXON, "taxa",
        field(DwcTerm.kingdom, KINGDOM, null),
        field(DwcTerm.order, ORDER, null),
        field(DwcTerm.family, FAMILY, null)));

    InferredEmlTaxonomicCoverage taxonomic = infer(resource).getInferredTaxonomicCoverage();

    assertAll(
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertEquals(Collections.singletonList("Animalia"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(ResourceMetadataInferringServiceImpl.TAXON_LIMIT, names(taxonomic, Constants.ORDER).size()),
        () -> assertNull(taxonomic.getRankWarnings().get(Constants.ORDER)),
        () -> assertEquals(Collections.emptyList(), names(taxonomic, Constants.FAMILY)),
        () -> assertEquals("eml.warning.limitExceeded", taxonomic.getRankWarnings().get(Constants.FAMILY)),
        () -> assertNull(taxonomic.getRankWarnings().get(Constants.KINGDOM)));
  }

  @DisplayName("A source that can't be read fails only the coverages it is mapped for")
  @Test
  public void testUnreadableSource() {
    addSource("events", row("e1", "10", "10", "2020-01-01"));
    Resource resource = resource(
        mapping(Constants.DWC_ROWTYPE_EVENT, "events", allFields()),
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "missing", field(DwcTerm.kingdom, KINGDOM, null)));

    InferredEmlMetadata metadata = infer(resource);

    assertTrue(metadata.getInferredGeographicCoverage().isInferred());
    assertTrue(metadata.getInferredTemporalCoverage().isInferred());
    assertOnlyError("eml.error.serverError", metadata.getInferredTaxonomicCoverage().getErrors());
  }

  @DisplayName("Data packages other than DwC-DP and Camtrap DP are not supported")
  @Test
  public void testUnsupportedDataPackage() {
    Resource resource = new Resource();
    resource.setCoreType(Constants.COL_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_COLDP_IDENTIFIER);

    assertNull(metadataInferringService.inferMetadata(resource));
  }

  @DisplayName("DwC-DP: place and time from events, taxa from identifications, other tables are ignored")
  @Test
  public void testDwcDp() {
    addSource("events", row("e1", "10", "-10", "2020-05-01"), row("e2", "-20", "30", "2019"));
    addSource("identifications", row("i1", "Animalia", "Aves", "Corvidae"), row("i2", "Plantae", null, null));
    addSource("identification-taxa", row("t1", "Chordata"));
    addSource("occurrences", row("o1", "Fungi", "80", "80", "1900-01-01"));
    Resource resource = dwcDpResource(
        dwcDpMapping("event", "events",
            dwcDpField("eventID", 0), dwcDpField("decimalLatitude", 1), dwcDpField("decimalLongitude", 2), dwcDpField("eventDate", 3)),
        dwcDpMapping("identification", "identifications",
            dwcDpField("identificationID", 0), dwcDpField("kingdom", 1), dwcDpField("class", 2), dwcDpField("family", 3)),
        dwcDpMapping("identification-taxon", "identification-taxa", dwcDpField("phylum", 1)),
        // same field names in a table that isn't used
        dwcDpMapping("occurrence", "occurrences",
            dwcDpField("kingdom", 1), dwcDpField("decimalLatitude", 2), dwcDpField("decimalLongitude", 3), dwcDpField("eventDate", 4)));

    InferredEmlMetadata metadata = infer(resource);

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    InferredEmlTemporalCoverage temporal = metadata.getInferredTemporalCoverage();
    InferredEmlTaxonomicCoverage taxonomic = metadata.getInferredTaxonomicCoverage();
    assertAll(
        () -> assertEquals(-20.0, bbox.getMin().getLatitude()),
        () -> assertEquals(-10.0, bbox.getMin().getLongitude()),
        () -> assertEquals(10.0, bbox.getMax().getLatitude()),
        () -> assertEquals(30.0, bbox.getMax().getLongitude()),
        () -> assertEquals("2019-01-01", format(temporal.getData().getStartDate())),
        () -> assertEquals("2020-05-01", format(temporal.getData().getEndDate())),
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Collections.singletonList("Chordata"), names(taxonomic, Constants.PHYLUM)),
        () -> assertEquals(Collections.singletonList("Aves"), names(taxonomic, Constants.CLASS)),
        () -> assertEquals(Collections.singletonList("Corvidae"), names(taxonomic, Constants.FAMILY)));
  }

  @DisplayName("DwC-DP: filters, translations and default values are applied")
  @Test
  public void testDwcDpFilterTranslationsAndDefaults() {
    addSource("events",
        row("e1", "10", "2020-01-01", "keep"),
        row("e2", "80", "1900-01-01", "skip"));
    addSource("identifications", row("i1", "A"), row("i2", null), row("i3", "  "));
    DataPackageFieldMapping kingdom = dwcDpField("kingdom", 1);
    kingdom.setTranslation(new TreeMap<>(Collections.singletonMap("A", "Animalia")));
    kingdom.setDefaultValue("Fungi");
    DataPackageFieldMapping longitude = DataPackageFieldMapping.builder()
        .defaultValue("7.5")
        .field(DataPackageField.builder().name("decimalLongitude").type("number").build())
        .build();
    DataPackageMapping events = dwcDpMapping("event", "events",
        dwcDpField("eventID", 0), dwcDpField("decimalLatitude", 1), longitude, dwcDpField("eventDate", 2));
    RecordFilter filter = new RecordFilter();
    filter.setColumn(3);
    filter.setComparator(RecordFilter.Comparator.Equals);
    filter.setParam("keep");
    filter.setFilterTime(RecordFilter.FilterTime.BeforeTranslation);
    events.setFilter(filter);
    Resource resource = dwcDpResource(events, dwcDpMapping("identification", "identifications", kingdom));

    InferredEmlMetadata metadata = infer(resource);

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    InferredEmlTemporalCoverage temporal = metadata.getInferredTemporalCoverage();
    assertAll(
        () -> assertEquals(10.0, bbox.getMax().getLatitude()),
        () -> assertEquals(7.5, bbox.getMax().getLongitude()),
        () -> assertEquals("2020-01-01", format(temporal.getData().getStartDate())),
        // the blank value is published as it is, not replaced by the default value
        () -> assertEquals(Arrays.asList("Animalia", "Fungi"), names(metadata.getInferredTaxonomicCoverage(), Constants.KINGDOM)));
  }

  @DisplayName("DwC-DP: superseded identifications are left out of the taxonomic coverage, blank counts as accepted")
  @Test
  public void testDwcDpSupersededIdentifications() {
    addSource("identifications",
        row("i1", "Animalia", "true"),
        row("i2", "Plantae", "FALSE"),
        row("i3", "Fungi", "0"),
        row("i4", "Bacteria", "No"),
        row("i5", "Chromista", null),
        row("i6", "Protozoa", "1"));
    addSource("identification-taxa", row("t1", "Archaea"));
    Resource resource = dwcDpResource(
        dwcDpMapping("identification", "identifications",
            dwcDpField("identificationID", 0), dwcDpField("kingdom", 1), dwcDpField("isAcceptedIdentification", 2)),
        // no such field: all rows are read
        dwcDpMapping("identification-taxon", "identification-taxa", dwcDpField("kingdom", 1)));

    InferredEmlMetadata metadata = infer(resource);

    assertEquals(Arrays.asList("Animalia", "Archaea", "Chromista", "Protozoa"),
        names(metadata.getInferredTaxonomicCoverage(), Constants.KINGDOM));
  }

  @DisplayName("DwC-DP: errors without mappings and without the coverage fields mapped")
  @Test
  public void testDwcDpErrors() {
    InferredEmlMetadata noMappings = infer(dwcDpResource());
    InferredEmlMetadata notMapped = infer(dwcDpResource(
        dwcDpMapping("event", "events", dwcDpField("eventID", 0), dwcDpField("decimalLatitude", 1)),
        dwcDpMapping("occurrence", "occurrences", dwcDpField("scientificName", 1))));

    assertAll(
        () -> assertOnlyError("eml.error.noMappings", noMappings.getInferredGeographicCoverage().getErrors()),
        () -> assertOnlyError("eml.error.noMappings", noMappings.getInferredTemporalCoverage().getErrors()),
        () -> assertOnlyError("eml.error.noMappings", noMappings.getInferredTaxonomicCoverage().getErrors()),
        () -> assertOnlyError("eml.geospatialCoverages.error.fieldsNotMapped", notMapped.getInferredGeographicCoverage().getErrors()),
        () -> assertOnlyError("eml.temporalCoverages.error.fieldsNotMapped", notMapped.getInferredTemporalCoverage().getErrors()),
        () -> assertOnlyError("eml.taxonomicCoverages.error.fieldsNotMapped", notMapped.getInferredTaxonomicCoverage().getErrors()));
  }

  @DisplayName("Reversed date ranges still give a start before the end")
  @Test
  public void testReversedDateRange() {
    addSource("events", row("1", null, null, "2020-03-01/2020-01-01"));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.eventDate, EVENT_DATE, null)));

    InferredEmlTemporalCoverage temporal = infer(resource).getInferredTemporalCoverage();

    assertTrue(temporal.isInferred());
    assertAll(
        () -> assertEquals("2020-01-01", format(temporal.getData().getStartDate())),
        () -> assertEquals("2020-03-01", format(temporal.getData().getEndDate())));
  }

  @DisplayName("Unlikely dates and non-numeric coordinates are ignored")
  @Test
  public void testUnlikelyValuesIgnored() {
    addSource("events",
        // NaN would be parsed as 0
        row("1", "NaN", "100", "1499-12-31"),
        row("2", "-30", "NaN", "2999-01-01"),
        row("6", "10", "Infinity", null),
        row("3", "", "", ""),
        row("4", "-20", "-40", "2001-05-05"),
        row("5", "20", "40", "1500-01-01"));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_EVENT, "events", allFields()));

    InferredEmlMetadata metadata = infer(resource);

    BBox bbox = metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates();
    InferredEmlTemporalCoverage temporal = metadata.getInferredTemporalCoverage();
    assertAll(
        () -> assertEquals(-20.0, bbox.getMin().getLatitude()),
        () -> assertEquals(-40.0, bbox.getMin().getLongitude()),
        () -> assertEquals(20.0, bbox.getMax().getLatitude()),
        () -> assertEquals(40.0, bbox.getMax().getLongitude()),
        () -> assertEquals("1500-01-01", format(temporal.getData().getStartDate())),
        () -> assertEquals("2001-05-05", format(temporal.getData().getEndDate())));
  }

  @DisplayName("Missing cells, short rows and out of range indexes are treated as missing values")
  @Test
  public void testMissingValues() {
    addSource("occurrences",
        // missing cell with a translation, translations are stored in a TreeMap that rejects null keys
        row("1", "10", "10", "2020-01-01", null),
        // shorter than the mapping, kingdom falls back to the default value
        row("2", "20", "20", "2020-01-02"),
        row("3", "30", "30", "2020-01-03", "A"));
    PropertyMapping kingdom = field(DwcTerm.kingdom, KINGDOM, "Fungi");
    kingdom.setTranslation(new TreeMap<>(Collections.singletonMap("A", "Animalia")));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences",
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        field(DwcTerm.decimalLongitude, LONGITUDE, null),
        field(DwcTerm.eventDate, EVENT_DATE, null),
        kingdom,
        // beyond every row
        field(DwcTerm.family, 50, null)));

    InferredEmlMetadata metadata = infer(resource);

    InferredEmlTaxonomicCoverage taxonomic = metadata.getInferredTaxonomicCoverage();
    assertAll(
        () -> assertTrue(metadata.getInferredGeographicCoverage().isInferred()),
        () -> assertEquals(30.0, metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates().getMax().getLatitude()),
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertEquals(Arrays.asList("Animalia", "Fungi"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Collections.emptyList(), names(taxonomic, Constants.FAMILY)));
  }

  @DisplayName("Rows with reading errors, null and empty rows are skipped")
  @Test
  public void testUnreadableRowsSkipped() {
    List<String[]> rows = Arrays.asList(
        row("1", "80", "80", "1900-01-01", "Broken"),
        null,
        new String[0],
        row("4", "10", "10", "2020-01-01", "Animalia"));
    sources.put("occurrences", () -> new InMemoryRowIterator(rows).withRowErrors(0));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", allFields()));

    InferredEmlMetadata metadata = infer(resource);

    assertAll(
        () -> assertEquals(10.0, metadata.getInferredGeographicCoverage().getData().getBoundingCoordinates().getMax().getLatitude()),
        () -> assertEquals("2020-01-01", format(metadata.getInferredTemporalCoverage().getData().getStartDate())),
        () -> assertEquals(Collections.singletonList("Animalia"), names(metadata.getInferredTaxonomicCoverage(), Constants.KINGDOM)));
  }

  @DisplayName("Excel parse errors keep the rows read so far, other reading errors fail the coverages")
  @Test
  public void testReadingFailures() {
    List<String[]> rows = Arrays.asList(row("1", "10", "10", "2020-01-01", "Animalia"), row("2", "20", "20", "2021-01-01", "Plantae"));
    sources.put("excel", () -> new InMemoryRowIterator(rows)
        .failingAt(1, new com.github.pjfanning.xlsx.exceptions.ParseException("broken cell")));
    sources.put("failing", () -> new InMemoryRowIterator(rows).failingAt(1, new IllegalStateException("broken source")));
    sources.put("failing-on-close", () -> new InMemoryRowIterator(rows).failingOnClose(new IllegalStateException("can't close")));

    InferredEmlMetadata excel = infer(resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "excel", allFields())));
    InferredEmlMetadata failing = infer(resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "failing", allFields())));
    InferredEmlMetadata failingOnClose = infer(resource(mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "failing-on-close", allFields())));

    assertAll(
        () -> assertEquals(10.0, excel.getInferredGeographicCoverage().getData().getBoundingCoordinates().getMax().getLatitude()),
        () -> assertEquals(Collections.singletonList("Animalia"), names(excel.getInferredTaxonomicCoverage(), Constants.KINGDOM)),
        () -> assertOnlyError("eml.error.serverError", failing.getInferredGeographicCoverage().getErrors()),
        () -> assertOnlyError("eml.error.serverError", failing.getInferredTemporalCoverage().getErrors()),
        () -> assertOnlyError("eml.error.serverError", failing.getInferredTaxonomicCoverage().getErrors()),
        () -> assertOnlyError("eml.error.serverError", failingOnClose.getInferredGeographicCoverage().getErrors()));
  }

  @DisplayName("Latitude and longitude must be mapped in the same mapping")
  @Test
  public void testCoordinatesInDifferentMappings() {
    Resource resource = resource(
        mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.decimalLatitude, LATITUDE, null)),
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", field(DwcTerm.decimalLongitude, LONGITUDE, null)));

    InferredEmlMetadata metadata = infer(resource);

    assertOnlyError("eml.geospatialCoverages.error.fieldsNotMapped", metadata.getInferredGeographicCoverage().getErrors());
  }

  @DisplayName("Taxa of all mappings are merged, the limit counts distinct names across mappings")
  @Test
  public void testTaxaMergedAcrossMappings() {
    List<String[]> first = new ArrayList<>();
    List<String[]> second = new ArrayList<>();
    for (int i = 0; i < ResourceMetadataInferringServiceImpl.TAXON_LIMIT; i++) {
      first.add(row(String.valueOf(i), null, null, null, "Animalia", null, null, null, "Family" + i));
      // same names again, and one new
      second.add(row(String.valueOf(i), null, null, null, "Plantae", null, null, null, "Family" + (i + 1)));
    }
    sources.put("taxa", () -> new InMemoryRowIterator(first));
    sources.put("occurrences", () -> new InMemoryRowIterator(second));
    Resource resource = resource(
        mapping(Constants.DWC_ROWTYPE_TAXON, "taxa", field(DwcTerm.kingdom, KINGDOM, null), field(DwcTerm.family, FAMILY, null)),
        mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", field(DwcTerm.kingdom, KINGDOM, null), field(DwcTerm.family, FAMILY, null)));

    InferredEmlTaxonomicCoverage taxonomic = infer(resource).getInferredTaxonomicCoverage();

    assertAll(
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), names(taxonomic, Constants.KINGDOM)),
        () -> assertEquals(Collections.emptyList(), names(taxonomic, Constants.FAMILY)),
        () -> assertEquals("eml.warning.limitExceeded", taxonomic.getRankWarnings().get(Constants.FAMILY)));
  }

  @DisplayName("If every mapped rank exceeds the limit, nothing is inferred: an error, and the warnings")
  @Test
  public void testAllRanksExceedLimit() {
    List<String[]> rows = new ArrayList<>();
    for (int i = 0; i <= ResourceMetadataInferringServiceImpl.TAXON_LIMIT; i++) {
      rows.add(row(String.valueOf(i), null, null, null, null, null, null, null, "Family" + i));
    }
    sources.put("taxa", () -> new InMemoryRowIterator(rows));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_TAXON, "taxa", field(DwcTerm.family, FAMILY, null)));

    InferredEmlTaxonomicCoverage taxonomic = infer(resource).getInferredTaxonomicCoverage();

    assertAll(
        () -> assertFalse(taxonomic.isInferred()),
        () -> assertEquals(Collections.singleton("eml.taxonomicCoverages.error.limitExceeded"), taxonomic.getErrors()),
        () -> assertNull(taxonomic.getData()),
        () -> assertEquals("eml.warning.limitExceeded", taxonomic.getRankWarnings().get(Constants.FAMILY)));
  }

  @DisplayName("Mappings changed while the sources are read: the result is outdated")
  @Test
  public void testMappingsChangedWhileInferring() {
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.eventDate, EVENT_DATE, null)));
    sources.put("events", () -> new InMemoryRowIterator(Collections.singletonList(row("1", null, null, "2020-01-01"))) {
      @Override
      public String[] next() {
        changeMappingsLater(resource);
        return super.next();
      }
    });

    resource.setInferredMetadata(infer(resource));

    assertTrue(resource.isInferredMetadataOutdated(InferredEmlMetadata.class));
  }

  /**
   * Changes the mappings a moment later than now, so that it is surely after inferring started.
   */
  static void changeMappingsLater(Resource resource) {
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    resource.setMappingsModified(new Date());
  }

  @DisplayName("Rows to read: the rows of the sources of mappings with coverage fields")
  @Test
  public void testCountRowsToRead() {
    ExtensionMapping events = mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.eventDate, EVENT_DATE, null));
    rows(events.getSource(), 1000);
    ExtensionMapping occurrences = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "occurrences", field(DwcTerm.kingdom, KINGDOM, null));
    rows(occurrences.getSource(), 500);
    // no coverage fields: not read
    ExtensionMapping withoutCoverage = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "other", field(DwcTerm.scientificName, 1, null));
    rows(withoutCoverage.getSource(), 9999);
    // the same source in another mapping is read again
    ExtensionMapping eventsAgain = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "events", field(DwcTerm.kingdom, KINGDOM, null));
    eventsAgain.setSource(events.getSource());

    assertAll(
        () -> assertEquals(OptionalLong.of(1500), metadataInferringService.countRowsToRead(resource(events, occurrences, withoutCoverage))),
        () -> assertEquals(OptionalLong.of(2500), metadataInferringService.countRowsToRead(resource(events, occurrences, eventsAgain))),
        () -> assertEquals(OptionalLong.of(0), metadataInferringService.countRowsToRead(new Resource())));
  }

  @DisplayName("Rows to read: unknown for sources read remotely, SQL and URL")
  @Test
  public void testCountRowsToReadRemoteSources() {
    ExtensionMapping events = mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.eventDate, EVENT_DATE, null));
    rows(events.getSource(), 1000);
    ExtensionMapping fromSql = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "sql", field(DwcTerm.kingdom, KINGDOM, null));
    SqlSource sql = new SqlSource();
    sql.setName("sql");
    fromSql.setSource(sql);
    ExtensionMapping fromUrl = mapping(Constants.DWC_ROWTYPE_OCCURRENCE, "url", field(DwcTerm.kingdom, KINGDOM, null));
    UrlSource url = new UrlSource();
    url.setName("url");
    // the row count of the last analysis, the data at the URL can have changed since
    url.setRows(10);
    fromUrl.setSource(url);

    assertAll(
        () -> assertEquals(OptionalLong.empty(), metadataInferringService.countRowsToRead(resource(events, fromSql))),
        () -> assertEquals(OptionalLong.empty(), metadataInferringService.countRowsToRead(resource(events, fromUrl))));
  }

  @DisplayName("DwC-DP rows to read: the event and identification tables")
  @Test
  public void testCountRowsToReadDwcDp() {
    DataPackageMapping events = dwcDpMapping("event", "events", dwcDpField("eventDate", 3));
    rows(events.getSource(), 100);
    DataPackageMapping identifications = dwcDpMapping("identification", "identifications", dwcDpField("kingdom", 1));
    rows(identifications.getSource(), 20);
    DataPackageMapping occurrences = dwcDpMapping("occurrence", "occurrences", dwcDpField("kingdom", 1));
    rows(occurrences.getSource(), 9999);

    assertEquals(OptionalLong.of(120), metadataInferringService.countRowsToRead(dwcDpResource(events, identifications, occurrences)));
  }

  @DisplayName("More distinct dates than the parser remembers: all of them still count")
  @Test
  public void testManyDistinctDates() {
    List<String[]> rows = new ArrayList<>();
    LocalDate first = LocalDate.of(1980, 1, 1);
    // more than the 10,000 dates the parser remembers, then the first ones again
    for (int i = 0; i < 12_000; i++) {
      rows.add(row(String.valueOf(i), null, null, first.plusDays(i).toString()));
    }
    for (int i = 0; i < 100; i++) {
      rows.add(row("again" + i, null, null, first.plusDays(i).toString()));
    }
    sources.put("events", () -> new InMemoryRowIterator(rows));
    Resource resource = resource(mapping(Constants.DWC_ROWTYPE_EVENT, "events", field(DwcTerm.eventDate, EVENT_DATE, null)));

    InferredEmlTemporalCoverage temporal = infer(resource).getInferredTemporalCoverage();

    assertAll(
        () -> assertEquals("1980-01-01", format(temporal.getData().getStartDate())),
        () -> assertEquals(first.plusDays(11_999).toString(), format(temporal.getData().getEndDate())));
  }

  private static void rows(Source source, int rows) {
    ((TextFileSource) source).setRows(rows);
  }

  private InferredEmlMetadata infer(Resource resource) {
    return (InferredEmlMetadata) metadataInferringService.inferMetadata(resource);
  }

  private void addSource(String name, String[]... rows) {
    sources.put(name, () -> new InMemoryRowIterator(Arrays.asList(rows)));
  }

  private static String[] row(String... values) {
    return values;
  }

  private static Resource resource(ExtensionMapping... mappings) {
    Resource resource = new Resource();
    resource.setShortname("res");
    resource.setMappings(new ArrayList<>(Arrays.asList(mappings)));
    return resource;
  }

  private static ExtensionMapping mapping(String rowType, String sourceName, PropertyMapping... fields) {
    Extension extension = new Extension();
    extension.setRowType(rowType);
    TextFileSource source = new TextFileSource();
    source.setName(sourceName);

    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(extension);
    mapping.setSource(source);
    mapping.setFields(new HashSet<>(Arrays.asList(fields)));
    return mapping;
  }

  private static Resource dwcDpResource(DataPackageMapping... mappings) {
    Resource resource = new Resource();
    resource.setShortname("dwc-dp-res");
    resource.setCoreType(Constants.DWC_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER);
    for (DataPackageMapping mapping : mappings) {
      resource.addDataPackageMapping(mapping);
    }
    return resource;
  }

  private static DataPackageMapping dwcDpMapping(String table, String sourceName, DataPackageFieldMapping... fields) {
    TextFileSource source = new TextFileSource();
    source.setName(sourceName);

    return DataPackageMapping.builder()
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name(Constants.DWC_DP)
                .identifier(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER)
                .build())
        .dataPackageTableSchemaName(new DataPackageTableSchemaName(table))
        .source(source)
        .fields(Arrays.asList(fields))
        .build();
  }

  private static DataPackageFieldMapping dwcDpField(String name, int index) {
    return DataPackageFieldMapping.builder()
        .index(index)
        .field(DataPackageField.builder().name(name).type("string").build())
        .build();
  }

  private static PropertyMapping[] allFields() {
    return new PropertyMapping[] {
        field(DwcTerm.occurrenceID, ID, null),
        field(DwcTerm.decimalLatitude, LATITUDE, null),
        field(DwcTerm.decimalLongitude, LONGITUDE, null),
        field(DwcTerm.eventDate, EVENT_DATE, null),
        field(DwcTerm.kingdom, KINGDOM, null),
        field(DwcTerm.phylum, PHYLUM, null),
        field(DwcTerm.class_, CLASS, null),
        field(DwcTerm.order, ORDER, null),
        field(DwcTerm.family, FAMILY, null)
    };
  }

  private static PropertyMapping field(Term term, Integer index, String defaultValue) {
    PropertyMapping field = new PropertyMapping();
    field.setTerm(term);
    field.setIndex(index);
    field.setDefaultValue(defaultValue);
    return field;
  }

  private static List<String> names(InferredEmlTaxonomicCoverage coverage, String rank) {
    return coverage.getData().getTaxonKeywords().stream()
        .filter(k -> rank.equals(k.getRank()))
        .map(TaxonKeyword::getScientificName)
        .collect(Collectors.toList());
  }

  private static List<String> organizedNames(InferredEmlTaxonomicCoverage coverage, String rank) {
    return coverage.getOrganizedData().getKeywords().stream()
        .filter(k -> rank.equals(k.getRank()))
        .findFirst()
        .map(OrganizedTaxonomicKeywords::getDisplayNames)
        .orElse(null);
  }

  private static void assertOnlyError(String expected, Set<String> errors) {
    assertEquals(Collections.singleton(expected), errors);
  }

  private static String format(Date date) {
    return DATE_FORMAT.format(date);
  }
}
