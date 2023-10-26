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

import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSchemaFieldMapping;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.FileUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceCamtrapMetadataInferringTest {

  @Mock
  private SourceManager sourceManagerMock;

  @InjectMocks
  private ResourceMetadataInferringServiceImpl metadataInferringService;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

  @BeforeAll
  static void beforeAll() {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

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

    DataSchemaMapping deploymentsMapping = getDeploymentsMapping(deploymentsFileSource);
    DataSchemaMapping deploymentsMapping2 = getDeploymentsMapping(deploymentsFileSource2);
    DataSchemaMapping observationsMapping = getObservationsMapping(observationsFileSource);
    resource.addDataSchemaMapping(deploymentsMapping);
    resource.addDataSchemaMapping(observationsMapping);
    resource.addDataSchemaMapping(deploymentsMapping2);

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
        () -> assertEquals(toDate("Wed May 04 00:00:00 CEST 2022"), temporal.getStartDate()),
        () -> assertEquals(toDate("Wed Jun 01 00:00:00 CEST 2022"), temporal.getEndDate())
    );

    assertAll(
        "Inferred taxonomic metadata must be present and valid",
        () -> assertTrue(taxonomic.isInferred()),
        () -> assertTrue(taxonomic.getErrors().isEmpty()),
        () -> assertEquals(4, taxonomic.getData().size()),
        () -> assertIterableEquals(
            Arrays.asList(
                taxonomic("49JSC", "Ondatra zibethicus"),
                taxonomic("4RM67", "Rattus norvegicus"),
                taxonomic("DGP6", "Anas platyrhynchos"),
                taxonomic("3F6VX", "Gallinula chloropus")),
            taxonomic.getData()
        )
    );
  }

  private static Taxonomic taxonomic(String taxonID, String scientificName) {
    Taxonomic taxonomic = new Taxonomic();
    taxonomic.setTaxonID(taxonID);
    taxonomic.setScientificName(scientificName);

    return taxonomic;
  }

  private static Resource getCamtrapResource(String shortname) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    resource.setCoreType("camtrap-dp");
    resource.setSchemaIdentifier("http://rs.gbif.org/schemas/camtrap-dp");

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

  private static DataSchemaMapping getDeploymentsMapping(Source source) {
    return DataSchemaMapping.builder()
        .dataSchema(
            DataSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataSchemaFile("deployments")
        .source(source)
        .fields(
            Arrays.asList(
                DataSchemaFieldMapping.builder()
                    .index(0)
                    .field(DataSchemaField.builder()
                        .name("deploymentID")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(3)
                    .field(DataSchemaField.builder()
                        .name("latitude")
                        .type("number")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(4)
                    .field(DataSchemaField.builder()
                        .name("longitude")
                        .type("number")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(6)
                    .field(DataSchemaField.builder()
                        .name("deploymentStart")
                        .type("datetime")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(7)
                    .field(DataSchemaField.builder()
                        .name("deploymentEnd")
                        .type("datetime")
                        .build())
                    .build()))
        .build();
  }

  private static DataSchemaMapping getObservationsMapping(Source source) {
    return DataSchemaMapping.builder()
        .dataSchema(
            DataSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataSchemaFile("observations")
        .source(source)
        .fields(
            Arrays.asList(
                DataSchemaFieldMapping.builder()
                    .index(0)
                    .field(DataSchemaField.builder()
                        .name("observationID")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(1)
                    .field(DataSchemaField.builder()
                        .name("deploymentID")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(4)
                    .field(DataSchemaField.builder()
                        .name("eventStart")
                        .type("datetime")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(5)
                    .field(DataSchemaField.builder()
                        .name("eventEnd")
                        .type("datetime")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(6)
                    .field(DataSchemaField.builder()
                        .name("observationLevel")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(7)
                    .field(DataSchemaField.builder()
                        .name("observationType")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(9)
                    .field(DataSchemaField.builder()
                        .name("taxonID")
                        .type("string")
                        .build())
                    .build(),
                DataSchemaFieldMapping.builder()
                    .index(10)
                    .field(DataSchemaField.builder()
                        .name("scientificName")
                        .type("string")
                        .build())
                    .build()))
        .build();
  }

  private static Date toDate(String strDate) throws Exception {
    return DATE_FORMAT.parse(strDate);
  }
}
