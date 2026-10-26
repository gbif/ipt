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
package org.gbif.ipt.action.manage;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.datacite.RestJsonApiDataCiteService;
import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapLicense;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.service.manage.impl.JsonServiceImpl;
import org.gbif.ipt.service.manage.impl.MetadataReaderImpl;
import org.gbif.ipt.service.manage.impl.YamlServiceImpl;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.ipt.IptEmlWriter;
import org.gbif.metadata.eml.ipt.model.Citation;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import freemarker.template.TemplateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OverviewActionTest extends IptBaseTest {

  private OverviewAction action;
  private File emlFile;
  @TempDir
  private File publishedMetadataDir;
  private DataPackageSchemaManager mockSchemaManager;
  private final MetadataReader metadataReader = new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl());

  @BeforeEach
  public void setup()
    throws IOException, ParserConfigurationException, SAXException, AlreadyExistingException, ImportException {

    ResourcePublicationManager mockResourcePublicationManager = mock(ResourcePublicationManager.class);
    Map<String, List<Date>> processFailures = new ConcurrentHashMap<>();
    processFailures
        .computeIfAbsent("res1", k -> new CopyOnWriteArrayList<>())
        .add(new Date());
    processFailures
        .computeIfAbsent("res1", k -> new CopyOnWriteArrayList<>())
        .add(new Date());
    when(mockResourcePublicationManager.getProcessFailures()).thenReturn(processFailures);

    // mock returning eml-1.0.xml
    emlFile = File.createTempFile("eml-1.0", ".xml");
    AppConfig mockCfg = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.resourceEmlFile(any(), any(BigDecimal.class))).thenReturn(emlFile);
    // the metadata a data package was published with
    when(mockDataDir.resourceDatapackageMetadataFile(any(), any(), any(BigDecimal.class))).thenAnswer(invocation ->
        new File(publishedMetadataDir, Constants.COL_DP.equals(invocation.getArgument(1))
            ? "metadata-" + invocation.getArgument(2, BigDecimal.class).toPlainString() + ".yaml"
            : "datapackage-" + invocation.getArgument(2, BigDecimal.class).toPlainString() + ".json"));
    when(mockCfg.getDataDir()).thenReturn(mockDataDir);

    mockSchemaManager = mock(DataPackageSchemaManager.class);

    // mock action
    action =
      new OverviewAction(
          mock(SimpleTextProvider.class),
          mockCfg,
          mock(RegistrationManager.class),
          mock(ResourceManager.class),
          mock(UserAccountManager.class),
          mock(ExtensionManager.class),
          mock(GenerateDwcaFactory.class),
          mock(GenerateDataPackageFactory.class),
          mock(VocabulariesManager.class),
          mock(RegistryManager.class),
          mockSchemaManager,
          mockResourcePublicationManager,
          metadataReader);
  }

  @Test
  public void testLoadDataPackageSchemaVersions() {
    DataPackageSchema schema = DataPackageSchema.builder().identifier("dwc-dp").name("dwc-dp").version("1.0").build();
    when(mockSchemaManager.get("dwc-dp")).thenReturn(schema);
    action.setResource(dataPackageResource(schema, "1.0", "1.0"));

    action.loadDataPackageSchemaVersions();

    assertEquals("1.0", action.getInstalledSchemaVersion());
    assertEquals("dwc-dp", action.getInstalledSchemaName());
    assertFalse(action.isDataPackageSchemaChanged());
    assertNull(action.getOutdatedMappingVersions());
  }

  @Test
  public void testLoadDataPackageSchemaVersionsWithOutdatedMappings() {
    DataPackageSchema schema = DataPackageSchema.builder().identifier("dwc-dp").name("dwc-dp").version("1.0").build();
    when(mockSchemaManager.get("dwc-dp")).thenReturn(schema);
    action.setResource(dataPackageResource(schema, "1.0", "0.1", null));

    action.loadDataPackageSchemaVersions();

    assertEquals("1.0", action.getInstalledSchemaVersion());
    assertTrue(action.isDataPackageSchemaChanged());
    assertEquals("0.1, v?", action.getOutdatedMappingVersions());
  }

  @Test
  public void testLoadDataPackageSchemaVersionsWithoutInstalledSchema() {
    DataPackageSchema schema = DataPackageSchema.builder().identifier("dwc-dp").name("dwc-dp").version("1.0").build();
    action.setResource(dataPackageResource(schema, "0.1"));

    action.loadDataPackageSchemaVersions();

    assertNull(action.getInstalledSchemaVersion());
    assertFalse(action.isDataPackageSchemaChanged());
  }

  private static Resource dataPackageResource(DataPackageSchema schema, String... mappingVersions) {
    Resource resource = new Resource();
    resource.setDataPackageIdentifier(schema.getIdentifier());
    for (String version : mappingVersions) {
      DataPackageMapping mapping = new DataPackageMapping();
      mapping.setDataPackageSchema(schema);
      mapping.setVersion(version);
      resource.addDataPackageMapping(mapping);
    }
    return resource;
  }

  @Test
  public void testLogProcessFailures() {
    Resource resource = new Resource();
    resource.setShortname("res1");
    resource.setTitle("Mammals");
    action.logProcessFailures(resource);
    resource.setShortname("res2");
    action.logProcessFailures(resource);
  }

  @Test
  public void testFindExistingDoi() {
    Resource resource = new Resource();
    Eml eml = new Eml();
    resource.setEml(eml);

    Citation citation1 = new Citation("Text", "doi:10.1594/J67TGE");
    eml.setCitation(citation1);

    DOI identifier = action.findExistingDoi(resource);
    assertEquals("10.1594/j67tge", identifier.getDoiName());

    Citation citation2 = new Citation("Text", "http://dx.doi.org/10.8894/887TGE");
    eml.setCitation(citation2);

    identifier = action.findExistingDoi(resource);
    assertEquals("10.8894/887tge", identifier.getDoiName());
  }

  /**
   * CC0 is a GBIF-supported license.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicense() throws IOException, TemplateException {
    Resource r = new Resource();
    // CCO
    r.getEml().setIntellectualRights(
      "This work is licensed under <a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">Creative Commons CCZero (CC0) 1.0 License</a>.");
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", r.getEml().parseLicenseUrl());
    assertTrue(r.isAssignedGBIFSupportedLicense());
    IptEmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PRIVATE);
    r.addVersionHistory(vh);
    assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));
  }

  /**
   * A license GBIF doesn't support, e.g. CC BY-SA, in the last published version prevents registration. A supported
   * one without legalcode doesn't.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicenseUnsupported() throws IOException, TemplateException {
    Resource r = new Resource();
    r.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PRIVATE));

    r.getEml().setIntellectualRights(
      "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by-sa/4.0/legalcode\">Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License</a>.");
    IptEmlWriter.writeEmlFile(emlFile, r.getEml());
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));

    r.getEml().setIntellectualRights(
      "This work is licensed under a <a href=\"https://creativecommons.org/licenses/by/4.0/\">Creative Commons Attribution (CC-BY) 4.0 License</a>.");
    IptEmlWriter.writeEmlFile(emlFile, r.getEml());
    assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));
  }

  /**
   * A ColDP license can be written in many ways, e.g. Plazi's CC0.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicenseColDp() throws IOException {
    Resource r = colDpResource(null);

    for (String license : List.of("CC0", "cc0", "CC0-1.0", "CC BY 4.0", "CC-BY-NC-4.0")) {
      publishColDp(license);
      assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r), license);
    }
    publishColDp("CC BY-SA 4.0");
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));
    publishColDp(null);
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));
  }

  /**
   * A ColDP's last published version is registered, so its license is checked, not the one it has been edited to
   * since.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicenseColDpNotEdited() throws IOException {
    publishColDp("CC0");
    assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(colDpResource("CC BY-SA 4.0")));

    publishColDp("CC BY-SA 4.0");
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(colDpResource("CC0")));
  }

  /**
   * A Camtrap DP's last published version is registered, so its data license is checked, not the one it has been
   * edited to since.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicenseCamtrapDpNotEdited() throws IOException {
    metadataReader.writeValue(new File(publishedMetadataDir, "datapackage-1.json"), camtrapMetadata("CC0-1.0"));
    assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(camtrapDpResource("CC-BY-SA-4.0")));

    metadataReader.writeValue(new File(publishedMetadataDir, "datapackage-1.json"), camtrapMetadata("CC-BY-SA-4.0"));
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(camtrapDpResource("CC0-1.0")));
  }

  /**
   * Without the metadata it was published with, a data package's license can't be checked.
   */
  @Test
  public void testIsLastPublishedVersionAssignedGBIFSupportedLicenseWithoutPublishedMetadata() {
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(colDpResource("CC0")));
    assertFalse(action.isLastPublishedVersionAssignedGBIFSupportedLicense(camtrapDpResource("CC0-1.0")));
  }

  /**
   * A ColDP resource published as version 1, with its current license.
   */
  private static Resource colDpResource(String license) {
    Resource r = new Resource();
    r.setCoreType(Constants.COL_DP);
    r.setDataPackageIdentifier(Constants.DATA_PACKAGE_COLDP_IDENTIFIER);
    ColMetadata metadata = new ColMetadata();
    metadata.setLicense(license);
    r.setDataPackageMetadata(metadata);
    r.addVersionHistory(new VersionHistory(new BigDecimal("1"), new Date(), PublicationStatus.PRIVATE));
    return r;
  }

  private void publishColDp(String license) throws IOException {
    ColMetadata published = new ColMetadata();
    published.setLicense(license);
    metadataReader.writeValue(new File(publishedMetadataDir, "metadata-1.yaml"), published);
  }

  /**
   * A Camtrap DP resource published as version 1, with its current data license.
   */
  private static Resource camtrapDpResource(String dataLicense) {
    Resource r = new Resource();
    r.setCoreType(Constants.CAMTRAP_DP);
    r.setDataPackageIdentifier(Constants.DATA_PACKAGE_CAMTRAP_DP_IDENTIFIER);
    r.setDataPackageMetadata(camtrapMetadata(dataLicense));
    r.addVersionHistory(new VersionHistory(new BigDecimal("1"), new Date(), PublicationStatus.PRIVATE));
    return r;
  }

  private static CamtrapMetadata camtrapMetadata(String dataLicense) {
    CamtrapLicense license = new CamtrapLicense();
    license.setName(dataLicense);
    license.setScope(CamtrapLicense.Scope.DATA);
    CamtrapMetadata metadata = new CamtrapMetadata();
    metadata.setLicenses(new ArrayList<>(List.of(license)));
    return metadata;
  }

  @Test
  public void testGetLastPublishedVersionAssignedLicense() throws IOException, TemplateException {
    Resource r = new Resource();
    // CCO
    r.getEml().setIntellectualRights(
      "This work is licensed under <a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">Creative Commons CCZero (CC0) 1.0 License</a>.");
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", r.getEml().parseLicenseUrl());
    assertTrue(r.isAssignedGBIFSupportedLicense());
    IptEmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PRIVATE);
    r.addVersionHistory(vh);
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode",
      action.getLastPublishedVersionAssignedLicense(r));
  }

  /**
   * ODbl is not a GBIF-supported license - test registration fails when last published version of resource is assigned
   * ODbl.
   */
  @Test
  public void testRegisterResourceNotGBIFSupportedLicense() throws Exception {
    Resource r = new Resource();
    // ODbl
    r.getEml().setIntellectualRights(
      "This work is licensed under a <a href=\"http://opendatacommons.org/licenses/odbl/1.0\">Open Data Commons Open Database License (ODbL) 1.0</a>");
    assertEquals("http://opendatacommons.org/licenses/odbl/1.0", r.getEml().parseLicenseUrl());
    assertFalse(r.isAssignedGBIFSupportedLicense());
    IptEmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PRIVATE);
    r.addVersionHistory(vh);
    action.setResource(r);
    assertEquals("input", action.registerResource());
    assertEquals(1, action.getActionErrors().size());
  }

  /**
   * ODbl is not a GBIF-supported license - test publishing fails when last current version of resource is assigned
   * ODbl.
   */
  @Test
  public void testPublishResourceNotGBIFSupportedLicense() throws Exception {
    Resource r = new Resource();
    r.getEml().setIntellectualRights(
      "This work is licensed under a <a href=\"http://opendatacommons.org/licenses/odbl/1.0\">Open Data Commons Open Database License (ODbL) 1.0</a>");
    r.setKey(UUID.randomUUID());
    r.setStatus(PublicationStatus.REGISTERED);
    action.setResource(r);
    action.setPublish("true");
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
  }

  /**
   * If a public resource has a DOI - publishing will trigger a DOI operation (register or update). This test ensures
   * that if a DOI service is not configured, publishing fails.
   */
  @Test
  public void testPublishPublicResourceWithDOIButNoDOIService() throws Exception {
    Resource r = new Resource();
    r.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    r.setStatus(PublicationStatus.PUBLIC);
    action.setResource(r);
    action.setPublish("true");
    assertNull(action.getOrganisationWithPrimaryDoiAccount());
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
  }

  /**
   * Test trying to delete a resource that is already deleted - redirects back to manage page showing warning.
   */
  @Test
  public void testDeleteDeletedResource() throws Exception {
    Resource r = new Resource();
    r.setStatus(PublicationStatus.DELETED);
    action.setResource(r);
    action.setDeleteFlag("true");
    assertEquals("input", action.delete());
    assertEquals(1, action.getActionWarnings().size());
  }

  /**
   * If a public resource has a DOI - deleting will trigger a DOI operation (delete reserved DOI or deactivate
   * registered DOI. This test ensures that if a DOI service is not configured, deletion fails.
   */
  @Test
  public void testDeletePublicResourceWithDOIButNoDOIService() throws Exception {
    Resource r = new Resource();
    r.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    r.setStatus(PublicationStatus.PUBLIC);
    action.setResource(r);
    action.setDeleteFlag("true");
    assertNull(action.getOrganisationWithPrimaryDoiAccount());
    assertEquals("input", action.delete());
    assertEquals(1, action.getActionErrors().size());
  }

  @Test
  public void testUndeleteNonDeletedResource() {
    Resource r = new Resource();
    r.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    r.setStatus(PublicationStatus.PUBLIC);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionWarnings().size());
  }

  @Test
  public void testUndeleteButNoDOIAssigned() {
    Resource r = new Resource();
    r.setStatus(PublicationStatus.DELETED);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionWarnings().size());
  }

  @Test
  public void testUndeleteButNoDOIService() {
    Resource r = new Resource();
    DOI doiToUndelete = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    BigDecimal versionToUndelete = new BigDecimal("1.0");
    VersionHistory vh = new VersionHistory(versionToUndelete, new Date(), PublicationStatus.PUBLIC);
    vh.setDoi(doiToUndelete);
    vh.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(vh);
    // resource deleted!
    r.setStatus(PublicationStatus.DELETED);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionErrors().size());
  }

  @Test
  public void testUndeleteButResourceHasNoOrganisation() {
    Resource r = new Resource();
    DOI doiToUndelete = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    BigDecimal versionToUndelete = new BigDecimal("1.0");
    VersionHistory vh = new VersionHistory(versionToUndelete, new Date(), PublicationStatus.PUBLIC);
    vh.setDoi(doiToUndelete);
    vh.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(vh);
    // resource deleted!
    r.setStatus(PublicationStatus.DELETED);
    // mock organisation missing
    r.setOrganisation(null);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionErrors().size());
  }

  @Test
  public void testUndeleteButResourceOrganisationNoLongerInIPT() {
    Resource r = new Resource();
    DOI doiToUndelete = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    BigDecimal versionToUndelete = new BigDecimal("1.0");
    VersionHistory vh = new VersionHistory(versionToUndelete, new Date(), PublicationStatus.PUBLIC);
    vh.setDoi(doiToUndelete);
    vh.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(vh);
    // resource deleted!
    r.setStatus(PublicationStatus.DELETED);
    // mock organisation that is not associated to IPT
    Organisation org = new Organisation();
    org.setKey(UUID.randomUUID().toString());
    r.setOrganisation(org);

    // mock RegistrationManager returning mock DoiService
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    DoiService mockDataCiteService = mock(RestJsonApiDataCiteService.class);
    when(mockRegistrationManager.getDoiService()).thenReturn(mockDataCiteService);
    // mock action
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mock(AppConfig.class),
        mockRegistrationManager,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(GenerateDataPackageFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class),
        mock(DataPackageSchemaManager.class),
        mock(ResourcePublicationManager.class),
        metadataReader);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionErrors().size());
  }

  @Test
  public void testUndeleteButResourceDOIPrefixNotMatchingDOIAccountActivatedInIPT() {
    Resource r = new Resource();
    DOI doiToUndelete = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    BigDecimal versionToUndelete = new BigDecimal("1.0");
    VersionHistory vh = new VersionHistory(versionToUndelete, new Date(), PublicationStatus.PUBLIC);
    vh.setDoi(doiToUndelete);
    vh.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(vh);
    // resource deleted!
    r.setStatus(PublicationStatus.DELETED);
    // mock organisation that is not associated to IPT
    Organisation resourceOrganisation = new Organisation();
    resourceOrganisation.setKey(UUID.randomUUID().toString());
    r.setOrganisation(resourceOrganisation);

    // mock RegistrationManager returning mock DoiService
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    DoiService mockDataCiteService = mock(RestJsonApiDataCiteService.class);
    when(mockRegistrationManager.getDoiService()).thenReturn(mockDataCiteService);

    // mock RegistrationManager returning resource organisation
    when(mockRegistrationManager.get(any(UUID.class))).thenReturn(resourceOrganisation);

    // mock RegistrationManager returning organisation with DOI agency account activated
    Organisation doiAccoutActivated = new Organisation();
    doiAccoutActivated.setKey(UUID.randomUUID().toString());
    doiAccoutActivated.setDoiPrefix("10.5000"); // different to resource doi that has 10.5072 (DataCite test prefix)
    when(mockRegistrationManager.findPrimaryDoiAgencyAccount()).thenReturn(doiAccoutActivated);

    // mock action
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mock(AppConfig.class),
        mockRegistrationManager,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(GenerateDataPackageFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class),
        mock(DataPackageSchemaManager.class),
        mock(ResourcePublicationManager.class),
        metadataReader);
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionErrors().size());
  }
}
