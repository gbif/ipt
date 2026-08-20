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

import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessSource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapSource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CaptureMethod;
import org.gbif.ipt.model.datapackage.metadata.camtrap.GbifIngestion;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;
import org.gbif.ipt.model.datapackage.metadata.camtrap.ObservationLevel;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Project;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Taxonomic;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Temporal;
import org.gbif.ipt.service.manage.JsonService;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.YamlService;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.utils.file.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetadataReaderImplTest {

  private final JsonService jsonService = new JsonServiceImpl();
  private final YamlService yamlService = new YamlServiceImpl();
  private final MetadataReader reader = new MetadataReaderImpl(jsonService, yamlService);

  @Test
  void testCamtrapSerDeRoundtrip(@TempDir Path tempDir) throws Exception {
    File datapackageJson = FileUtils.getClasspathFile("frictionless/camtrap/datapackage.json");
    DataPackageMetadata metadata = reader.readValue(datapackageJson, MetadataUtils.metadataClassForType("camtrap-dp"));

    assertNotNull(metadata);
    assertInstanceOf(CamtrapMetadata.class, metadata);

    CamtrapMetadata cm = (CamtrapMetadata) metadata;
    verifyCamtrapMetadata(cm);

    Path roundTripFile = tempDir.resolve("datapackage-roundtrip.json");
    reader.writeValue(roundTripFile.toFile(), cm);

    CamtrapMetadata roundtrip = reader.readValue(roundTripFile.toFile(), CamtrapMetadata.class);
    verifyCamtrapMetadata(roundtrip);
  }

  private static void verifyCamtrapMetadata(CamtrapMetadata cm) {
    // Top-level metadata
    assertEquals("dagma-otus_fototrampeotest", cm.getName());
    assertEquals(
        "https://rs.gbif.org/sandbox/data-packages/camtrap-dp/1.0/profile/camtrap-dp-profile.json",
        cm.getProfile()
    );
    assertEquals(
        "Monitoreo de biodiversidad utilizando cámaras trampa en la Estructura Ecológica Principal de Santiago de Cali",
        cm.getTitle()
    );
    assertTrue(cm.getDescription().startsWith(
        "Proyecto para fortalecer la gestión integral de la Biodiversidad en Colombia"
    ));
    // in the datapackage.json formatted as '2026-06-03T09:19:36Z'
    // when Date converted to String formatted as 'Wed Jun 03 11:19:36 CEST 2026'
    assertEquals("Wed Jun 03 11:19:36 CEST 2026", cm.getCreated().toString());
    assertEquals("2", cm.getVersion());
    assertEquals("https://gitlab.com/sib-colombia/logos/-/raw/main/socio-SiB-dagma.png",
        cm.getImage());

    assertEquals(
        " (2025). DAGMA_OTUS. Version 1. Camtrap DP dataset. " +
            "https://ipt.biodiversidad.co/sib/resource?r=dagma-otus_fototrampeo&amp;v=1",
        cm.getBibliographicCitation()
    );

    // Licenses
    List<CamtrapLicense> licenses = cm.getLicenses();
    assertEquals(2, licenses.size());

    assertEquals("CC-BY-4.0", licenses.get(0).getName());
    assertEquals("data", licenses.get(0).getScope().value());

    assertEquals("CC-BY-NC-4.0", licenses.get(1).getName());
    assertEquals("media", licenses.get(1).getScope().value());

    // Sources
    List<CamtrapSource> sources = cm.getSources();
    assertEquals(2, sources.size());

    assertEquals("source 1", sources.get(0).getTitle());
    assertEquals("/source1", sources.get(0).getPath());
    assertEquals("source1@mail", sources.get(0).getEmail());
    assertEquals("1.0", sources.get(0).getVersion());

    assertEquals("source 2", sources.get(1).getTitle());
    assertEquals("source2@mail", sources.get(1).getEmail());
    assertEquals("2.0", sources.get(1).getVersion());
    assertNull(sources.get(1).getPath());

    // Contributors
    List<CamtrapContributor> contributors = cm.getContributors();
    assertEquals(3, contributors.size());

    assertEquals("Catalina Silva", contributors.get(0).getTitle());
    assertEquals("Catalina", contributors.get(0).getFirstName());
    assertEquals("Silva", contributors.get(0).getLastName());
    assertEquals("prediosecosistemas@gmail.com", contributors.get(0).getEmail());
    assertEquals("contact", contributors.get(0).getRole());
    assertEquals(
        "Departamento Administrativo de Gestión del Medio Ambiente-DAGM",
        contributors.get(0).getOrganization()
    );

    assertEquals("Angélica Diaz", contributors.get(1).getTitle());
    assertEquals("Angélica", contributors.get(1).getFirstName());
    assertEquals("Diaz", contributors.get(1).getLastName());
    assertEquals("adiaz@humboldt.org.co", contributors.get(1).getEmail());
    assertEquals("contact", contributors.get(1).getRole());
    assertEquals(
        "Investigación de Recursos biológicos Alexander von Humboldt",
        contributors.get(1).getOrganization()
    );

    assertEquals(" Infraestructura Institucional de Datos",
        contributors.get(2).getTitle());
    assertEquals("i2d@humboldt.org.co", contributors.get(2).getEmail());
    assertEquals("publisher", contributors.get(2).getRole());
    assertEquals(
        "Investigación de Recursos biológicos Alexander von Humboldt",
        contributors.get(2).getOrganization()
    );

    // Keywords
    List<String> keywords = cm.getKeywords();
    assertEquals(1, keywords.size());
    assertEquals("Mamíferos, aves, cámaras trampa", keywords.get(0));

    // Project
    Project project = cm.getProject();

    assertEquals(
        "RED OTUS en la Estructura Ecológica Principal de Santiago de Cali",
        project.getTitle()
    );

    assertTrue(project.getDescription().contains(
        "Proyecto para fortalecer la gestión integral de la Biodiversidad en Colombia"
    ));
    assertTrue(project.getDescription().contains(
        "El diseño de muestreo se estableció con el propósito de registrar principalmente mamíferos y aves"
    ));

    assertEquals("systematicRandom", project.getSamplingDesign().value());

    Set<CaptureMethod> captureMethod = project.getCaptureMethod();
    assertEquals(1, captureMethod.size());
    assertEquals(Set.of(CaptureMethod.ACTIVITY_DETECTION), captureMethod);

    Set<ObservationLevel> observationLevel = project.getObservationLevel();
    assertEquals(1, observationLevel.size());
    assertEquals(Set.of(ObservationLevel.MEDIA), observationLevel);

    assertFalse(project.getIndividualAnimals());

    // Spatial
    Geojson spatial = cm.getSpatial();
    assertEquals(Geojson.Type.POLYGON, spatial.getType());

    List<?> coordinates = spatial.getCoordinates();
    assertEquals(1, coordinates.size());

    List<?> polygon = (List<?>) coordinates.get(0);
    assertEquals(5, polygon.size());

    assertEquals(List.of(-76.66415, 3.3176), polygon.get(0));
    assertEquals(List.of(-76.535, 3.3176), polygon.get(1));
    assertEquals(List.of(-76.535, 3.465278), polygon.get(2));
    assertEquals(List.of(-76.66415, 3.465278), polygon.get(3));
    assertEquals(List.of(-76.66415, 3.3176), polygon.get(4));

    // Temporal
    Temporal temporal = cm.getTemporal();
    assertEquals("2025-10-04", temporal.getStart());
    assertEquals("2025-12-06", temporal.getEnd());

    // Taxonomic
    List<Taxonomic> taxonomic = cm.getTaxonomic();
    assertFalse(taxonomic.isEmpty());
    assertEquals(96, taxonomic.size());
    assertEquals("Eira barbara", taxonomic.get(0).getScientificName());
    assertEquals("Dicotyles tajacu", taxonomic.get(taxonomic.size() - 1).getScientificName());

    // gbifIngestion
    GbifIngestion gbifIngestion = cm.getGbifIngestion();
    assertEquals(ObservationLevel.MEDIA, gbifIngestion.getObservationLevel());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDwcDpSerDeRoundtrip(@TempDir Path tempDir) throws Exception {
    File datapackageJson = FileUtils.getClasspathFile("frictionless/dwc-dp/datapackage.json");
    DataPackageMetadata metadata =
        reader.readValue(datapackageJson, MetadataUtils.metadataClassForType("dwc-dp"));

    assertNotNull(metadata);
    assertInstanceOf(FrictionlessMetadata.class, metadata);

    @SuppressWarnings("rawtypes")
    FrictionlessMetadata<FrictionlessContributor, FrictionlessLicense, FrictionlessSource> fm
        = (FrictionlessMetadata) metadata;

    verifyFrictionlessMetadata(fm);

    Path roundTripFile = tempDir.resolve("datapackage-roundtrip.json");
    reader.writeValue(roundTripFile.toFile(), fm);

    FrictionlessMetadata<FrictionlessContributor, FrictionlessLicense, FrictionlessSource> roundtrip
        = reader.readValue(roundTripFile.toFile(), FrictionlessMetadata.class);
    verifyFrictionlessMetadata(roundtrip);
  }

  private static void verifyFrictionlessMetadata(
      FrictionlessMetadata<FrictionlessContributor, FrictionlessLicense, FrictionlessSource> fm) {
    // Basic metadata
    assertEquals("https://rs.tdwg.org/dwc-dp/1.0/dwc-dp-profile.json", fm.getProfile());
    assertEquals("my-dataset-name", fm.getName());
    assertEquals("My Dataset Title", fm.getTitle());
    assertEquals("test dataset", fm.getDescription());

    // Licenses
    assertEquals(2, fm.getLicenses().size());

    assertEquals("CC-BY-4.0", fm.getLicenses().get(0).getName());
    assertEquals(
        "https://creativecommons.org/licenses/by/4.0/",
        fm.getLicenses().get(0).getPath()
    );
    assertEquals(
        "Creative Commons Attribution 4.0",
        fm.getLicenses().get(0).getTitle()
    );

    assertEquals("CC-BY-NC-4.0", fm.getLicenses().get(1).getName());
    assertEquals(
        "https://creativecommons.org/licenses/by-nc/4.0/",
        fm.getLicenses().get(1).getPath()
    );
    assertEquals(
        "Creative Commons Attribution-NonCommercial 4.0",
        fm.getLicenses().get(1).getTitle()
    );

    // Sources
    assertEquals(2, fm.getSources().size());

    assertEquals("source 1", fm.getSources().get(0).getTitle());
    assertEquals("/source1", fm.getSources().get(0).getPath());
    assertEquals("source1@mail", fm.getSources().get(0).getEmail());

    assertEquals("source 2", fm.getSources().get(1).getTitle());
    assertEquals("source2@mail", fm.getSources().get(1).getEmail());
    assertNull(fm.getSources().get(1).getPath());

    // Created
    // in the datapackage.json formatted as '2026-08-20T09:19:36Z'
    // when Date converted to String formatted as 'Thu Aug 20 11:19:36 CEST 2026'
    assertEquals("Thu Aug 20 11:19:36 CEST 2026", fm.getCreated().toString());

    // Contributors
    assertEquals(3, fm.getContributors().size());

    assertEquals("Esteban Marentes", fm.getContributors().get(0).getTitle());
    assertEquals("/esteban-marentes", fm.getContributors().get(0).getPath());
    assertEquals("emarentes@gbif.org", fm.getContributors().get(0).getEmail());
    assertEquals("contact", fm.getContributors().get(0).getRole());
    assertEquals("GBIF", fm.getContributors().get(0).getOrganization());

    assertEquals("Mikhail Podolskiy", fm.getContributors().get(1).getTitle());
    assertEquals("/mikhail-podolskiy", fm.getContributors().get(1).getPath());
    assertEquals("mpodolskiy@gbif.org", fm.getContributors().get(1).getEmail());
    assertEquals("contact", fm.getContributors().get(1).getRole());
    assertEquals("GBIF", fm.getContributors().get(1).getOrganization());

    assertEquals(" GBIF", fm.getContributors().get(2).getTitle());
    assertEquals("helpdesk@gbif.org", fm.getContributors().get(2).getEmail());
    assertEquals("publisher", fm.getContributors().get(2).getRole());
    assertEquals("GBIF", fm.getContributors().get(2).getOrganization());
    assertNull(fm.getContributors().get(2).getPath());

    // Keywords
    List<String> keywords = fm.getKeywords();
    assertEquals(1, keywords.size());
    assertEquals("dwc-dp", keywords.get(0));

    // Image
    assertEquals("https://www.gbif.org", fm.getImage());
  }

}