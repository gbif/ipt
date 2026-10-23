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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.FileUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceCamtrapMetadataInferringTest extends IptBaseTest {

  @Mock
  private SourceManager sourceManagerMock;

  @InjectMocks
  private ResourceMetadataInferringServiceImpl metadataInferringService;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @DisplayName("Test metadata inferring for the resource without mappings")
  @Test
  public void testInferMetadataResourceWithoutMappings() {
    Resource resource = getCamtrapResource("resource-without-mappings");

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    InferredCamtrapGeographicScope geographic = inferredMetadata.getInferredGeographicScope();
    InferredCamtrapTemporalScope temporal = inferredMetadata.getInferredTemporalScope();
    InferredCamtrapTaxonomicScope taxonomic = inferredMetadata.getInferredTaxonomicScope();

    assertAll(
        "Inferred geographic metadata must contain an error message about missing deployments mapping",
        () -> assertFalse(geographic.isInferred()),
        () -> assertFalse(geographic.getErrors().isEmpty()),
        () -> assertEquals(1, geographic.getErrors().size()),
        () -> assertTrue(geographic.getErrors().contains("datapackagemetadata.error.noDeploymentsMapped"))
    );

    assertAll(
        "Inferred taxonomic metadata must contain an error message about missing observation mapping",
        () -> assertFalse(taxonomic.isInferred()),
        () -> assertFalse(taxonomic.getErrors().isEmpty()),
        () -> assertEquals(1, taxonomic.getErrors().size()),
        () -> assertTrue(taxonomic.getErrors().contains("datapackagemetadata.error.noObservationsMapped"))
    );

    assertAll(
        "Inferred temporal metadata must contain an error message about missing deployments mapping",
        () -> assertFalse(temporal.isInferred()),
        () -> assertFalse(temporal.getErrors().isEmpty()),
        () -> assertEquals(1, temporal.getErrors().size()),
        () -> assertTrue(temporal.getErrors().contains("datapackagemetadata.error.noDeploymentsMapped"))
    );
  }

  @DisplayName("Test metadata inferring for the resource with multiple valid mappings")
  @Test
  public void testMetadataInferring() throws Exception {
    Resource resource = getCamtrapResource("regular-resource");
    TextFileSource deploymentsFileSource = getTextFileSource("deployments", "data/deployments.txt");
    TextFileSource deploymentsFileSource2 = getTextFileSource("deployments2", "data/deployments-additional.txt");
    TextFileSource observationsFileSource = getTextFileSource("observations", "data/observations.txt");

    when(sourceManagerMock.rowIterator(any(FileSource.class))).thenAnswer(invocation -> {
      FileSource source = invocation.getArgument(0);

      if ("deployments".equals(source.getName())) {
        return deploymentsFileSource.rowIterator();
      } else if ("deployments2".equals(source.getName())) {
        return deploymentsFileSource2.rowIterator();
      } else if ("observations".equals(source.getName())) {
        return observationsFileSource.rowIterator();
      } else {
        return null;
      }
    });

    DataPackageMapping deploymentsMapping = getDeploymentsMapping(deploymentsFileSource);
    DataPackageMapping deploymentsMapping2 = getDeploymentsMapping(deploymentsFileSource2);
    DataPackageMapping observationsMapping = getObservationsMapping(observationsFileSource);
    resource.addDataPackageMapping(deploymentsMapping);
    resource.addDataPackageMapping(observationsMapping);
    resource.addDataPackageMapping(deploymentsMapping2);

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    InferredCamtrapGeographicScope geographic = inferredMetadata.getInferredGeographicScope();
    InferredCamtrapTemporalScope temporal = inferredMetadata.getInferredTemporalScope();
    InferredCamtrapTaxonomicScope taxonomic = inferredMetadata.getInferredTaxonomicScope();

    assertAll(
        "Inferred geographic metadata must be present and valid",
        () -> assertTrue(geographic.isInferred()),
        () -> assertTrue(geographic.getErrors().isEmpty()),
        () -> assertEquals(49.4781, geographic.getMinLatitude()),
        () -> assertEquals(2.4328, geographic.getMinLongitude()),
        () -> assertEquals(53.40744, geographic.getMaxLatitude()),
        () -> assertEquals(8.32994, geographic.getMaxLongitude())
    );

    assertAll(
        "Inferred temporal metadata must be present and valid",
        () -> assertTrue(temporal.isInferred()),
        () -> assertTrue(temporal.getErrors().isEmpty()),
        () -> assertEquals("2019-09-18", formatDate(temporal.getStartDate()), "Start date does not match"),
        () -> assertEquals("2023-05-02", formatDate(temporal.getEndDate()), "End date does not match")
    );

    assertAll(
        "Inferred taxonomic metadata must be present and valid",
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertTrue(taxonomic.getErrors().isEmpty()),
        () -> assertEquals(10, taxonomic.getData().size(), "Expected 10 taxa"),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Ardea cinerea")), "Taxa must contain \"Ardea cinerea\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Rattus norvegicus")), "Taxa must contain \"Rattus norvegicus\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Homo sapiens")), "Taxa must contain \"Homo sapiens\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Anas platyrhynchos")), "Taxa must contain \"Anas platyrhynchos\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Anas strepera")), "Taxa must contain \"Anas strepera\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Mustela putorius")), "Taxa must contain \"Mustela putorius\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Vulpes vulpes")), "Taxa must contain \"Vulpes vulpes\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Martes foina")), "Taxa must contain \"Martes foina\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Aves")), "Taxa must contain \"Aves\". Values: " + toString(taxonomic)),
        () -> assertTrue(taxonomic.getData().contains(taxonomic("Ardea")), "Taxa must contain \"Ardea\". Values: " + toString(taxonomic))
    );
  }

  @DisplayName("Test errors for partially mapped deployments and observations")
  @Test
  public void testInferMetadataPartialMappings() {
    Resource resource = getCamtrapResource("partial-mappings");
    TextFileSource source = getTextFileSource("deployments", "data/deployments.txt");
    resource.addDataPackageMapping(mapping("deployments", source,
        fieldMapping("deploymentID", 0), fieldMapping("latitude", 1), fieldMapping("deploymentStart", 2)));
    resource.addDataPackageMapping(mapping("observations", source, fieldMapping("observationID", 0)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    // nothing to infer, so the sources are not read
    assertAll(
        () -> assertEquals(Collections.singleton("datapackagemetadata.geographic.error.longitudeNotMapped"),
            inferredMetadata.getInferredGeographicScope().getErrors()),
        () -> assertEquals(Collections.singleton("datapackagemetadata.temporal.error.deploymentEndNotMapped"),
            inferredMetadata.getInferredTemporalScope().getErrors()),
        () -> assertEquals(Collections.singleton("datapackagemetadata.taxonomic.error.scientificNameNotMapped"),
            inferredMetadata.getInferredTaxonomicScope().getErrors()));
  }

  @DisplayName("Test the taxonomic scope is limited, and sources are not read further once the limit is reached")
  @Test
  public void testInferMetadataTaxonLimit() throws Exception {
    List<String[]> rows = new ArrayList<>();
    for (int i = 0; i < ResourceMetadataInferringServiceImpl.CAMTRAP_TAXON_LIMIT + 10; i++) {
      rows.add(new String[] {"o" + i, String.format("Taxon %03d", i)});
    }
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(rows));
    Resource resource = getCamtrapResource("taxon-limit");
    TextFileSource source = getTextFileSource("observations", "data/observations.txt");
    resource.addDataPackageMapping(mapping("observations", source, fieldMapping("scientificName", 1)));
    resource.addDataPackageMapping(mapping("observations", source, fieldMapping("scientificName", 1)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    InferredCamtrapTaxonomicScope taxonomic = inferredMetadata.getInferredTaxonomicScope();
    assertAll(
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertEquals(ResourceMetadataInferringServiceImpl.CAMTRAP_TAXON_LIMIT, taxonomic.getData().size()),
        () -> assertEquals("Taxon 000", taxonomic.getData().get(0).getScientificName()),
        () -> verify(sourceManagerMock, times(1)).rowIterator(any(Source.class)));
  }

  @DisplayName("Test deployments with reversed or incomplete dates, mixed offsets and invalid coordinates")
  @Test
  public void testInferMetadataDeploymentEdgeCases() throws Exception {
    List<String[]> rows = Arrays.asList(
        // end before start
        new String[] {"d1", "10", "10", "2021-06-01T00:00:00Z", "2021-05-01T00:00:00Z"},
        // only one valid date, skipped for the temporal scope
        new String[] {"d2", "20", "20", "2019-01-01T00:00:00Z", "not a date"},
        // local dates are used, not UTC
        new String[] {"d3", "NaN", "30", "2020-12-31T23:30:00-05:00", "2022-01-01T00:30:00+02:00"},
        new String[] {"d4", null, null, null, null});
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(rows));
    Resource resource = getCamtrapResource("deployment-edge-cases");
    resource.addDataPackageMapping(mapping("deployments", getTextFileSource("deployments", "data/deployments.txt"),
        fieldMapping("deploymentID", 0), fieldMapping("latitude", 1), fieldMapping("longitude", 2),
        fieldMapping("deploymentStart", 3), fieldMapping("deploymentEnd", 4)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    InferredCamtrapGeographicScope geographic = inferredMetadata.getInferredGeographicScope();
    InferredCamtrapTemporalScope temporal = inferredMetadata.getInferredTemporalScope();
    assertAll(
        () -> assertEquals(10.0, geographic.getMinLatitude()),
        () -> assertEquals(20.0, geographic.getMaxLatitude()),
        () -> assertEquals(20.0, geographic.getMaxLongitude()),
        () -> assertEquals("2020-12-31", formatDate(temporal.getStartDate())),
        () -> assertEquals("2022-01-01", formatDate(temporal.getEndDate())));
  }

  @DisplayName("Test a deployment ending before it starts gives a start before the end")
  @Test
  public void testInferMetadataReversedDeployment() throws Exception {
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(
        Collections.singletonList(new String[] {"d1", "2021-06-01T00:00:00Z", "2021-05-01T00:00:00Z"})));
    Resource resource = getCamtrapResource("reversed-deployment");
    resource.addDataPackageMapping(mapping("deployments", getTextFileSource("deployments", "data/deployments.txt"),
        fieldMapping("deploymentID", 0), fieldMapping("deploymentStart", 1), fieldMapping("deploymentEnd", 2)));

    InferredCamtrapTemporalScope temporal =
        ((InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource)).getInferredTemporalScope();

    assertAll(
        () -> assertTrue(temporal.isInferred()),
        () -> assertEquals("2021-05-01", formatDate(temporal.getStartDate())),
        () -> assertEquals("2021-06-01", formatDate(temporal.getEndDate())));
  }

  @DisplayName("Test default values, translations and filters are applied like during the publication")
  @Test
  public void testInferMetadataDefaultsTranslationsAndFilter() throws Exception {
    List<String[]> rows = Arrays.asList(
        new String[] {"o1", "Vulpes vulpes", "animal"},
        new String[] {"o2", "VV", "animal"},
        new String[] {"o3", null, "animal"},
        new String[] {"o4", "Homo sapiens", "human"});
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(rows));
    DataPackageFieldMapping scientificName = fieldMapping("scientificName", 1);
    scientificName.setDefaultValue("Animalia");
    scientificName.setTranslation(new TreeMap<>(Collections.singletonMap("VV", "Vulpes vulpes")));
    DataPackageMapping observations = mapping("observations", getTextFileSource("observations", "data/observations.txt"),
        fieldMapping("observationID", 0), scientificName, fieldMapping("observationType", 2));
    RecordFilter filter = new RecordFilter();
    filter.setColumn(2);
    filter.setComparator(RecordFilter.Comparator.Equals);
    filter.setParam("animal");
    filter.setFilterTime(RecordFilter.FilterTime.BeforeTranslation);
    observations.setFilter(filter);
    Resource resource = getCamtrapResource("defaults-translations-filter");
    resource.addDataPackageMapping(observations);

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    assertEquals(Arrays.asList("Animalia", "Vulpes vulpes"), inferredMetadata.getInferredTaxonomicScope().getData().stream()
        .map(Taxonomic::getScientificName)
        .collect(Collectors.toList()));
  }

  @DisplayName("Test coordinates and dates mapped in different deployments mappings, and an unreadable observations source")
  @Test
  public void testInferMetadataFieldsInDifferentMappingsAndUnreadableSource() throws Exception {
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(
        Collections.singletonList(new String[] {"o1", "Vulpes vulpes"})).failingAt(0, new IllegalStateException("broken")));
    TextFileSource source = getTextFileSource("deployments", "data/deployments.txt");
    Resource resource = getCamtrapResource("different-mappings");
    resource.addDataPackageMapping(mapping("deployments", source, fieldMapping("latitude", 1), fieldMapping("deploymentStart", 3)));
    resource.addDataPackageMapping(mapping("deployments", source, fieldMapping("longitude", 2), fieldMapping("deploymentEnd", 4)));
    resource.addDataPackageMapping(mapping("observations", source, fieldMapping("scientificName", 1)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    assertAll(
        () -> assertEquals(Collections.singleton("datapackagemetadata.geographic.error.fieldsNotMapped"),
            inferredMetadata.getInferredGeographicScope().getErrors()),
        () -> assertEquals(Collections.singleton("datapackagemetadata.temporal.error.fieldsNotMapped"),
            inferredMetadata.getInferredTemporalScope().getErrors()),
        () -> assertEquals(Collections.singleton("datapackagemetadata.error.serverError"),
            inferredMetadata.getInferredTaxonomicScope().getErrors()));
  }

  @DisplayName("Test an unreadable observations source fails only the taxonomic scope")
  @Test
  public void testInferMetadataUnreadableObservations() throws Exception {
    TextFileSource deployments = getTextFileSource("deployments", "data/deployments.txt");
    TextFileSource observations = getTextFileSource("observations", "data/observations.txt");
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> {
      Source source = invocation.getArgument(0);
      if ("observations".equals(source.getName())) {
        throw new SourceException("can't read observations");
      }
      return new InMemoryRowIterator(Collections.singletonList(
          new String[] {"d1", "10", "20", "2021-01-01T00:00:00Z", "2021-02-01T00:00:00Z"}));
    });
    Resource resource = getCamtrapResource("unreadable-observations");
    resource.addDataPackageMapping(mapping("deployments", deployments,
        fieldMapping("latitude", 1), fieldMapping("longitude", 2), fieldMapping("deploymentStart", 3),
        fieldMapping("deploymentEnd", 4)));
    resource.addDataPackageMapping(mapping("observations", observations, fieldMapping("scientificName", 1)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    assertAll(
        () -> assertTrue(inferredMetadata.getInferredGeographicScope().isInferred()),
        () -> assertTrue(inferredMetadata.getInferredTemporalScope().isInferred()),
        () -> assertEquals(Collections.singleton("datapackagemetadata.error.serverError"),
            inferredMetadata.getInferredTaxonomicScope().getErrors()));
  }

  @DisplayName("Test scientific names are trimmed, blank and duplicate names are skipped, taxa are sorted")
  @Test
  public void testInferMetadataScientificNames() throws Exception {
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(Arrays.asList(
        new String[] {"o1", " Vulpes vulpes "},
        new String[] {"o2", "   "},
        new String[] {"o3", "Vulpes vulpes"},
        new String[] {"o4", "Ardea cinerea"},
        new String[] {"o5", ""})));
    Resource resource = getCamtrapResource("scientific-names");
    resource.addDataPackageMapping(mapping("observations", getTextFileSource("observations", "data/observations.txt"),
        fieldMapping("scientificName", 1)));

    InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) metadataInferringService.inferMetadata(resource);

    assertEquals(Arrays.asList("Ardea cinerea", "Vulpes vulpes"), inferredMetadata.getInferredTaxonomicScope().getData().stream()
        .map(Taxonomic::getScientificName)
        .collect(Collectors.toList()));
  }

  @DisplayName("Test mappings changed while the sources are read make the result outdated")
  @Test
  public void testInferMetadataMappingsChangedWhileInferring() throws Exception {
    Resource resource = getCamtrapResource("mappings-changed");
    when(sourceManagerMock.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(
        Collections.singletonList(new String[] {"o1", "Vulpes vulpes"})) {
      @Override
      public String[] next() {
        ResourceEmlMetadataInferringTest.changeMappingsLater(resource);
        return super.next();
      }
    });
    resource.addDataPackageMapping(mapping("observations", getTextFileSource("observations", "data/observations.txt"),
        fieldMapping("scientificName", 1)));

    resource.setInferredMetadata(metadataInferringService.inferMetadata(resource));

    assertTrue(resource.isInferredMetadataOutdated(InferredCamtrapMetadata.class));
  }

  private static DataPackageMapping mapping(String tableSchema, Source source, DataPackageFieldMapping... fields) {
    return DataPackageMapping.builder()
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchema))
        .source(source)
        .fields(Arrays.asList(fields))
        .build();
  }

  private static DataPackageFieldMapping fieldMapping(String name, int index) {
    return DataPackageFieldMapping.builder()
        .index(index)
        .field(DataPackageField.builder().name(name).type("string").build())
        .build();
  }

  private static String toString(InferredCamtrapTaxonomicScope taxonomic) {
    return taxonomic.getData().stream()
        .map(Taxonomic::getScientificName)
        .collect(Collectors.joining(", ", "[", "]"));
  }

  private static Taxonomic taxonomic(String scientificName) {
    Taxonomic taxonomic = new Taxonomic();
    taxonomic.setScientificName(scientificName);

    return taxonomic;
  }

  private static Resource getCamtrapResource(String shortname) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    resource.setCoreType("camtrap-dp");
    resource.setDataPackageIdentifier("http://rs.gbif.org/schemas/camtrap-dp");

    return resource;
  }

  private static TextFileSource getTextFileSource(String name, String filePath) {
    TextFileSource source = new TextFileSource();
    source.setName(name);
    source.setFile(FileUtils.getClasspathFile(filePath));
    source.setEncoding("UTF-8");
    source.setReadable(true);
    source.setFieldsTerminatedBy(",");
    source.setIgnoreHeaderLines(1);

    return source;
  }

  private static DataPackageMapping getDeploymentsMapping(Source source) {
    return DataPackageMapping.builder()
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataPackageTableSchemaName(new DataPackageTableSchemaName("deployments"))
        .source(source)
        .fields(
            Arrays.asList(
                DataPackageFieldMapping.builder()
                    .index(0)
                    .field(DataPackageField.builder()
                        .name("deploymentID")
                        .type("string")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(3)
                    .field(DataPackageField.builder()
                        .name("latitude")
                        .type("number")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(4)
                    .field(DataPackageField.builder()
                        .name("longitude")
                        .type("number")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(6)
                    .field(DataPackageField.builder()
                        .name("deploymentStart")
                        .type("datetime")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(7)
                    .field(DataPackageField.builder()
                        .name("deploymentEnd")
                        .type("datetime")
                        .build())
                    .build()))
        .build();
  }

  private static DataPackageMapping getObservationsMapping(Source source) {
    return DataPackageMapping.builder()
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataPackageTableSchemaName(new DataPackageTableSchemaName("observations"))
        .source(source)
        .fields(
            Arrays.asList(
                DataPackageFieldMapping.builder()
                    .index(0)
                    .field(DataPackageField.builder()
                        .name("observationID")
                        .type("string")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(1)
                    .field(DataPackageField.builder()
                        .name("deploymentID")
                        .type("string")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(4)
                    .field(DataPackageField.builder()
                        .name("eventStart")
                        .type("datetime")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(5)
                    .field(DataPackageField.builder()
                        .name("eventEnd")
                        .type("datetime")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(6)
                    .field(DataPackageField.builder()
                        .name("observationLevel")
                        .type("string")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(7)
                    .field(DataPackageField.builder()
                        .name("observationType")
                        .type("string")
                        .build())
                    .build(),
                DataPackageFieldMapping.builder()
                    .index(9)
                    .field(DataPackageField.builder()
                        .name("scientificName")
                        .type("string")
                        .build())
                    .build()))
        .build();
  }

  private static String formatDate(Date date) {
    return DATE_FORMAT.format(date);
  }
}
