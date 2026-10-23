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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockVocabulariesManager;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.metadata.eml.ipt.model.BBox;
import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TemporalCoverage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Applying the inferred metadata to the EML on publication, for DwC-A and DwC-DP resources.
 */
public class ResourcePublicationManagerImplInferredMetadataTest extends IptBaseTest {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  // event rows: id, latitude, longitude, eventDate, kingdom
  private static final List<String[]> ROWS = Arrays.asList(
      new String[] {"1", "10", "20", "2001-02-03", "Animalia"},
      new String[] {"2", "-10", "-20", "2005", "Plantae"});

  private ResourceManager resourceManager;
  private ResourceMetadataInferringService inferringService;
  private ResourcePublicationManagerImpl manager;

  @BeforeEach
  public void setUp() throws Exception {
    SourceManager sourceManager = mock(SourceManager.class);
    when(sourceManager.rowIterator(any(Source.class))).thenAnswer(invocation -> new InMemoryRowIterator(ROWS));
    inferringService = spy(new ResourceMetadataInferringServiceImpl(sourceManager, new MockVocabulariesManager()));
    resourceManager = mock(ResourceManager.class);
    AppConfig cfg = mock(AppConfig.class);
    when(cfg.getMaxThreads()).thenReturn(1);
    DataDir dataDir = mock(DataDir.class);

    manager = new ResourcePublicationManagerImpl(cfg, dataDir, resourceManager, mock(RegistryManager.class),
        mock(RegistrationManager.class), mock(Eml2Rtf.class), mock(GenerateDwcaFactory.class),
        mock(GenerateDataPackageFactory.class), mock(GenerateDarwinCoreDataPackageFactory.class),
        mock(SimpleTextProvider.class), new InferredMetadataManagerImpl(inferringService, resourceManager, Runnable::run),
        new ResourceDoiServiceImpl(cfg, dataDir, mock(RegistrationManager.class)));
  }

  @AfterEach
  public void tearDown() {
    manager.getExecutor().shutdownNow();
  }

  @DisplayName("DwC-DP: the inferred coverages replace the EML coverages, descriptions are kept")
  @Test
  public void testDwcDp() {
    Resource resource = dwcDpResource();
    inferAll(resource);
    GeospatialCoverage existing = new GeospatialCoverage();
    existing.setDescription("Somewhere");
    resource.getEml().addGeospatialCoverage(existing);

    manager.updateEmlWithInferredMetadata(resource);

    assertInferredCoverages(resource, "Somewhere");
    verify(resourceManager).saveInferredMetadata(resource);
  }

  @DisplayName("DwC-A: the inferred coverages replace the EML coverages")
  @Test
  public void testDwca() {
    Resource resource = dwcaResource();
    inferAll(resource);

    manager.updateEmlWithInferredMetadata(resource);

    assertInferredCoverages(resource, "N/A");
  }

  @DisplayName("Only the coverages inferred automatically are replaced")
  @Test
  public void testOnlyGeographicCoverage() {
    Resource resource = dwcDpResource();
    resource.setInferGeocoverageAutomatically(true);
    TemporalCoverage temporal = new TemporalCoverage();
    resource.getEml().addTemporalCoverage(temporal);

    manager.updateEmlWithInferredMetadata(resource);

    assertAll(
        () -> assertEquals(1, resource.getEml().getGeospatialCoverages().size()),
        () -> assertEquals(1, resource.getEml().getTemporalCoverages().size()),
        () -> assertSame(temporal, resource.getEml().getTemporalCoverages().get(0)),
        () -> assertEquals(Collections.emptyList(), resource.getEml().getTaxonomicCoverages()));
  }

  @DisplayName("Nothing is inferred if inferring automatically is turned off")
  @Test
  public void testInferringTurnedOff() {
    Resource resource = dwcDpResource();

    manager.updateEmlWithInferredMetadata(resource);

    verify(inferringService, never()).inferMetadata(any());
    verify(resourceManager, never()).saveInferredMetadata(any());
    assertEquals(Collections.emptyList(), resource.getEml().getGeospatialCoverages());
  }

  @DisplayName("Coverages are kept if the resource has no mapped data")
  @Test
  public void testNoMappedData() {
    Resource resource = new Resource();
    resource.setShortname("dwc-dp-empty");
    resource.setCoreType(Constants.DWC_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER);
    inferAll(resource);
    GeospatialCoverage existing = new GeospatialCoverage();
    resource.getEml().addGeospatialCoverage(existing);

    manager.updateEmlWithInferredMetadata(resource);

    assertEquals(1, resource.getEml().getGeospatialCoverages().size());
    assertSame(existing, resource.getEml().getGeospatialCoverages().get(0));
  }

  private static void assertInferredCoverages(Resource resource, String geographicDescription) {
    List<GeospatialCoverage> geographic = resource.getEml().getGeospatialCoverages();
    List<TemporalCoverage> temporal = resource.getEml().getTemporalCoverages();
    List<TaxonomicCoverage> taxonomic = resource.getEml().getTaxonomicCoverages();
    assertEquals(1, geographic.size());
    assertEquals(1, temporal.size());
    assertEquals(1, taxonomic.size());
    BBox bbox = geographic.get(0).getBoundingCoordinates();

    assertAll(
        () -> assertEquals(geographicDescription, geographic.get(0).getDescription()),
        () -> assertEquals(-10.0, bbox.getMin().getLatitude()),
        () -> assertEquals(-20.0, bbox.getMin().getLongitude()),
        () -> assertEquals(10.0, bbox.getMax().getLatitude()),
        () -> assertEquals(20.0, bbox.getMax().getLongitude()),
        () -> assertEquals("2001-02-03", format(temporal.get(0).getStartDate())),
        () -> assertEquals("2005-12-31", format(temporal.get(0).getEndDate())),
        () -> assertEquals(Arrays.asList("Animalia", "Plantae"), taxonomic.get(0).getTaxonKeywords().stream()
            .map(TaxonKeyword::getScientificName)
            .collect(Collectors.toList())));
  }

  private static void inferAll(Resource resource) {
    resource.setInferGeocoverageAutomatically(true);
    resource.setInferTemporalCoverageAutomatically(true);
    resource.setInferTaxonomicCoverageAutomatically(true);
  }

  private static Resource dwcDpResource() {
    Resource resource = new Resource();
    resource.setShortname("dwc-dp");
    resource.setCoreType(Constants.DWC_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER);
    resource.addDataPackageMapping(dwcDpMapping("event",
        dwcDpField("eventID", 0), dwcDpField("decimalLatitude", 1), dwcDpField("decimalLongitude", 2),
        dwcDpField("eventDate", 3)));
    resource.addDataPackageMapping(dwcDpMapping("identification", dwcDpField("kingdom", 4)));
    return resource;
  }

  private static Resource dwcaResource() {
    Extension occurrence = new Extension();
    occurrence.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(occurrence);
    mapping.setSource(source());
    mapping.setFields(new HashSet<>(Arrays.asList(
        field(DwcTerm.occurrenceID, 0), field(DwcTerm.decimalLatitude, 1), field(DwcTerm.decimalLongitude, 2),
        field(DwcTerm.eventDate, 3), field(DwcTerm.kingdom, 4))));

    Resource resource = new Resource();
    resource.setShortname("dwca");
    resource.setCoreType("occurrence");
    resource.addMapping(mapping);
    return resource;
  }

  private static DataPackageMapping dwcDpMapping(String table, DataPackageFieldMapping... fields) {
    return DataPackageMapping.builder()
        .dataPackageSchema(
            DataPackageSchema.builder()
                .name(Constants.DWC_DP)
                .identifier(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER)
                .build())
        .dataPackageTableSchemaName(new DataPackageTableSchemaName(table))
        .source(source())
        .fields(Arrays.asList(fields))
        .build();
  }

  private static DataPackageFieldMapping dwcDpField(String name, int index) {
    return DataPackageFieldMapping.builder()
        .index(index)
        .field(DataPackageField.builder().name(name).type("string").build())
        .build();
  }

  private static PropertyMapping field(Term term, int index) {
    PropertyMapping field = new PropertyMapping();
    field.setTerm(term);
    field.setIndex(index);
    return field;
  }

  private static Source source() {
    TextFileSource source = new TextFileSource();
    source.setName("events");
    return source;
  }

  private static String format(Date date) {
    return DATE_FORMAT.format(date);
  }
}
