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

import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSchemaFieldMapping;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.DataSubschemaName;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceCamtrapMetadataInferringTest {

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
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataSchemaFile(new DataSubschemaName("deployments"))
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
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name("camtrap-dp")
                .identifier("http://rs.gbif.org/schemas/camtrap-dp")
                .build())
        .dataSchemaFile(new DataSubschemaName("observations"))
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
