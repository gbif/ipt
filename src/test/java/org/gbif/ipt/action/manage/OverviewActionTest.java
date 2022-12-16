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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.ipt.IptEmlWriter;
import org.gbif.metadata.eml.ipt.model.Citation;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import freemarker.template.TemplateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OverviewActionTest {

  private OverviewAction action;
  private File emlFile;

  @BeforeEach
  public void setup()
    throws IOException, ParserConfigurationException, SAXException, AlreadyExistingException, ImportException {

    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ListValuedMap<String, Date> processFailures = new ArrayListValuedHashMap<>();
    processFailures.put("res1", new Date());
    processFailures.put("res1", new Date());
    when(mockResourceManager.getProcessFailures()).thenReturn(processFailures);

    // mock returning eml-1.0.xml
    emlFile = File.createTempFile("eml-1.0", ".xml");
    AppConfig mockCfg = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.resourceEmlFile(any(), any(BigDecimal.class))).thenReturn(emlFile);
    when(mockCfg.getDataDir()).thenReturn(mockDataDir);

    // mock action
    action =
      new OverviewAction(
          mock(SimpleTextProvider.class),
          mockCfg,
          mock(RegistrationManager.class),
          mockResourceManager,
          mock(UserAccountManager.class),
          mock(ExtensionManager.class),
          mock(GenerateDwcaFactory.class),
          mock(VocabulariesManager.class),
          mock(RegistryManager.class));
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
    action.setDelete("true");
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
    action.setDelete("true");
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
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));
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
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));
    action.setResource(r);
    action.setUndelete("true");
    assertEquals("input", action.undelete());
    assertEquals(1, action.getActionErrors().size());
  }
}
