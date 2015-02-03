package org.gbif.ipt.action.manage;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OverviewActionTest {

  private OverviewAction action;
  private File emlFile;

  @Before
  public void setup()
    throws IOException, ParserConfigurationException, SAXException, AlreadyExistingException, ImportException {

    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ListMultimap<String, Date> processFailures = ArrayListMultimap.create();
    processFailures.put("res1", new Date());
    processFailures.put("res1", new Date());
    when(mockResourceManager.getProcessFailures()).thenReturn(processFailures);

    // mock returning eml-1.0.xml
    emlFile = File.createTempFile("eml-1.0", ".xml");
    AppConfig mockCfg = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(emlFile);
    when(mockCfg.getDataDir()).thenReturn(mockDataDir);

    // mock action
    action = new OverviewAction(mock(SimpleTextProvider.class), mockCfg,
      mock(RegistrationManager.class), mockResourceManager, mock(UserAccountManager.class),
      mock(ExtensionManager.class), mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));
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
    EmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), user, PublicationStatus.PRIVATE);
    r.addVersionHistory(vh);
    assertTrue(action.isLastPublishedVersionAssignedGBIFSupportedLicense(r));
  }

  @Test
  public void testGetLastPublishedVersionAssignedLicense() throws IOException, TemplateException {
    Resource r = new Resource();
    // CCO
    r.getEml().setIntellectualRights("This work is licensed under <a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">Creative Commons CCZero (CC0) 1.0 License</a>.");
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", r.getEml().parseLicenseUrl());
    assertTrue(r.isAssignedGBIFSupportedLicense());
    EmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), user, PublicationStatus.PRIVATE);
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
    r.getEml().setIntellectualRights("This work is licensed under a <a href=\"http://opendatacommons.org/licenses/odbl/1.0\">Open Data Commons Open Database License (ODbL) 1.0</a>");
    assertEquals("http://opendatacommons.org/licenses/odbl/1.0", r.getEml().parseLicenseUrl());
    assertFalse(r.isAssignedGBIFSupportedLicense());
    EmlWriter.writeEmlFile(emlFile, r.getEml());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory vh = new VersionHistory(new BigDecimal("1.0"), new Date(), user, PublicationStatus.PRIVATE);
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
    // ODbl
    r.getEml().setIntellectualRights("This work is licensed under a <a href=\"http://opendatacommons.org/licenses/odbl/1.0\">Open Data Commons Open Database License (ODbL) 1.0</a>");
    r.setKey(UUID.randomUUID());
    r.setStatus(PublicationStatus.REGISTERED);
    action.setResource(r);
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
    // ODbl
    r.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    r.setStatus(PublicationStatus.PUBLIC);
    action.setResource(r);
    assertNull(action.getOrganisationWithPrimaryDoiAccount());
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
  }
}
