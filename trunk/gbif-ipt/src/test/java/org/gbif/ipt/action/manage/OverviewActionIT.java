package org.gbif.ipt.action.manage;


import org.gbif.api.model.common.DOI;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.ServiceConfig;
import org.gbif.doi.service.datacite.DataCiteService;
import org.gbif.doi.service.ezid.EzidService;
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
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class OverviewActionIT {


  private static final Logger LOG = Logger.getLogger(OverviewActionIT.class);

  private Resource r;
  private OverviewAction action;
  private DOIRegistrationAgency type;

  public OverviewActionIT(OverviewAction action, DOIRegistrationAgency type) {
    this.action = action;
    this.type = type;
  }

  @Parameterized.Parameters
  public static Iterable data() throws IOException, DeletionNotAllowedException, TemplateException, URISyntaxException {
    // common mock AppConfig
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);

    // mock returning versioned eml file (e.g. eml-#.0.xml) - constructed from Eml object populated with mandatory stuff
    Eml eml = new Eml();
    eml.setTitle("ants");
    eml.setPubDate(new Date());
    Agent creator = new Agent();
    creator.setFirstName("John");
    creator.setLastName("Smith");
    eml.addCreator(creator);
    File tmpVersionedEmlFile = File.createTempFile("eml-#.0", ".xml");
    EmlWriter.writeEmlFile(tmpVersionedEmlFile, eml);
    when(mockDataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(tmpVersionedEmlFile);
    when(mockAppConfig.getDataDir()).thenReturn(mockDataDir);
    // mock returning target URLs
    when(mockAppConfig.getResourceUri(anyString())).thenReturn(new URI("http://www.gbif-uat.org/ipt/resource?r=ants"));
    when(mockAppConfig.getResourceVersionUri(anyString(), any(BigDecimal.class)))
      .thenReturn(new URI("http://www.gbif-uat.org/ipt/resource?r=ants&v=#.0"));

    // DataCite parameters..
    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    InputStream dc = FileUtils.classpathStream("datacite.yaml");
    ServiceConfig dcCfg = mapper.readValue(dc, ServiceConfig.class);
    LOG.info("DataCite password (read from Maven property datacite.password)= " + dcCfg.getPassword());

    Organisation oDataCite = new Organisation();
    oDataCite.setAgencyAccountPrimary(true);
    oDataCite.setName("GBIF");
    oDataCite.setDoiPrefix(Constants.TEST_DOI_PREFIX);
    oDataCite.setCanHost(true);
    oDataCite.setAgencyAccountUsername(dcCfg.getUsername());
    oDataCite.setAgencyAccountPassword(dcCfg.getPassword());
    oDataCite.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCite);

    // mock RegistrationManager returning organisation by key
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCite);

    // mock returning DataCite service
    DoiService dataCiteService = new DataCiteService(HttpUtil.newMultithreadedClient(10000, 3, 2), dcCfg);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    OverviewAction actionDataCite =
      new OverviewAction(mock(SimpleTextProvider.class), mockAppConfig, mockRegistrationManagerDataCite,
        mock(ResourceManager.class), mock(UserAccountManager.class), mock(ExtensionManager.class),
        mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));

    // EZID parameters..
    RegistrationManager mockRegistrationManagerEZID = mock(RegistrationManager.class);

    Organisation oEZID = new Organisation();
    oEZID.setAgencyAccountPrimary(true);
    oEZID.setName("GBIF");
    oEZID.setDoiPrefix(Constants.EZID_TEST_DOI_SHOULDER);
    oEZID.setCanHost(true);
    oEZID.setAgencyAccountUsername("apitest");
    oEZID.setAgencyAccountPassword("apitest");
    oEZID.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerEZID.findPrimaryDoiAgencyAccount()).thenReturn(oEZID);

    // mock RegistrationManager returning organisation by key
    when(mockRegistrationManagerEZID.get(any(UUID.class))).thenReturn(oEZID);

    // mock returning EZID service
    ServiceConfig cfgEZID = new ServiceConfig("apitest", "apitest");
    EzidService ezidService = new EzidService(HttpUtil.newMultithreadedClient(10000, 2, 2), cfgEZID);
    when(mockRegistrationManagerEZID.getDoiService()).thenReturn(ezidService);

    // mock action for EZID
    OverviewAction actionEZID =
      new OverviewAction(mock(SimpleTextProvider.class), mockAppConfig, mockRegistrationManagerEZID,
        mock(ResourceManager.class), mock(UserAccountManager.class), mock(ExtensionManager.class),
        mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));

    return Arrays.asList(
      new Object[][] {{actionDataCite, DOIRegistrationAgency.DATACITE}
        //TODO undelete when EZID API working again
//        , {actionEZID, DOIRegistrationAgency.EZID}
      });
  }

  /**
   * Generate a new test resource for each test.
   */
  @Before
  public void before() {
    r = new Resource();
    Eml eml = new Eml();
    r.setEml(eml);

    // mandatory elements
    r.setTitle("Ants");
    r.setShortname("ants");
    eml.setTitle("Ants");

    // publication date
    Calendar cal = Calendar.getInstance();
    cal.set(2013, Calendar.JANUARY, 9);
    Date date = cal.getTime();
    eml.setDateStamp(date);

    // creator
    Agent creator = new Agent();
    creator.setFirstName("John");
    creator.setLastName("Smith");
    eml.addCreator(creator);

    // publisher
    Organisation o = new Organisation();
    o.setName("GBIF");
    r.setOrganisation(o);

    // resource must be publicly available
    r.setStatus(PublicationStatus.PUBLIC);

    action.setResource(r);
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
  }

  /**
   * Reserve DOI for resource that has never been assigned a DOI.
   */
  @Test
  public void testReserveDoi() throws Exception {
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());
  }

  /**
   * Test reserving existing DOI, making sure the DOI is preserved (user wants to reuse existing DOI).
   */
  @Test
  public void testReuseAndReserveExistingDoi() throws Exception {
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());

    DOI existingDOI = new DOI(r.getDoi().toString());
    Citation citation = new Citation("Mock Citation", existingDOI.toString());
    r.getEml().setCitation(citation);
    // reset DOI
    r.setDoi(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);

    action.reserveDoi();
    // make sure the existing DOI was reused
    assertEquals(existingDOI.getDoiName(), r.getDoi().getDoiName());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("Existing DOI was reused successfully, DOI=" + existingDOI.getDoiName());
  }

  /**
   * Test deleting reserved DOI, when the resource was never assigned a DOI before.
   */
  @Test
  public void testDeleteReservedDoi() throws Exception {
    LOG.info("Testing " + type + "...");
    action.setDeleteDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertFalse(r.isAlreadyAssignedDoi());
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());

    action.deleteDoi();
    // make sure the reserved DOI was deleted
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertFalse(r.isAlreadyAssignedDoi());
    LOG.info("Existing DOI was deleted successfully");
  }

  /**
   * Test deleting reserved DOI, when the resource was previously assigned a DOI.
   */
  @Test
  public void testDeleteReservedDoiWhenPreviousDoiExists() throws Exception {
    LOG.info("Testing " + type + "...");
    action.setDeleteDoi("true");

    // mock resource being assigned DOI
    DOI assignedDoi = new DOI("10.5072/bclona1");
    r.setDoi(assignedDoi);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC);
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory history = new VersionHistory(new BigDecimal("1.0"), new Date(), user, PublicationStatus.PUBLIC);
    history.setDoi(assignedDoi);
    history.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history);
    assertTrue(r.isAlreadyAssignedDoi());

    // reserve new DOI for resource
    action.reserveDoi();
    DOI reserved = r.getDoi();
    assertNotNull(reserved);
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("DOI was reserved successfully, DOI=" + reserved.toString());

    action.deleteDoi();
    // make sure the reserved DOI was deleted, and previous DOI reassigned
    assertEquals(assignedDoi.getDoiName(), r.getDoi().getDoiName());
    assertEquals(IdentifierStatus.PUBLIC, r.getIdentifierStatus());
    assertTrue(r.isAlreadyAssignedDoi());
    LOG.info("Existing DOI was deleted successfully");

    // for fun, try to publish resource having this deleted DOI - should not be possible!!
    r.setDoi(reserved);
    assertTrue(r.getDoi() != null && r.isPubliclyAvailable());
    // reset action errors, .clear() doesn't work
    List<String> collection = Lists.newArrayList();
    action.setActionErrors(collection);

    action.setPublish("true");
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
    LOG.info("Publishing resource with deleted DOI failed as expected");
  }

  /**
   * Ensure publishing fails (is prevented from starting) if DOI reserved cannot be resolved.
   */
  @Test
  public void testPublishFailsBecauseDOICannotBeResolved() throws Exception {
    LOG.info("Testing " + type + "...");
    // mock resource having DOI reserved that doesn't exist!
    DOI assignedDoi = DOIUtils.mintDOI(type,
      (type.equals(DOIRegistrationAgency.EZID) ? Constants.EZID_TEST_DOI_SHOULDER : Constants.TEST_DOI_PREFIX));
    r.setDoi(assignedDoi);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertTrue(r.getDoi() != null && r.isPubliclyAvailable());
    // reset action errors, .clear() doesn't work
    List<String> collection = Lists.newArrayList();
    action.setActionErrors(collection);

    action.setPublish("true");
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
    LOG.info("Publishing resource with DOI that cannot be resolved failed as expected");
  }

  /**
   * Test deleting resource that has been assigned multiple DOIs, and ensure that deletion deletes all reserved DOIs,
   * and deactivates all registered DOIs.
   * </br>
   * Then test undeleting the same resource, and ensure that all registered DOIs are reactivated.
   */
  @Test
  public void testDeleteAndUndeleteResourceAssignedMultipleDOIs() throws Exception {
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");

    action.reserveDoi();
    assertNotNull(r.getDoi());
    DOI reserved1 = new DOI(r.getDoi().toString());

    // reset
    r.setDoi(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);

    action.reserveDoi();
    DOI reserved2 = new DOI(r.getDoi().toString());

    // reset
    r.setDoi(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);

    action.reserveDoi();
    DOI reserved3 = new DOI(r.getDoi().toString());

    assertTrue(!reserved1.toString().equals(reserved2.toString()) && !reserved1.toString().equals(reserved3.toString())
               && !reserved2.toString().equals(reserved3.toString()));

    // mock VersionHistory: version 1.0 and 1.1 share same registered DOI, version 2.0 has different registered DOI
    VersionHistory history1 =
      new VersionHistory(new BigDecimal("1.0"), new Date(), new User(), PublicationStatus.PUBLIC);
    history1.setDoi(reserved1);
    history1.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history1);

    VersionHistory history11 =
      new VersionHistory(new BigDecimal("1.1"), new Date(), new User(), PublicationStatus.PUBLIC);
    history11.setDoi(reserved1);
    history11.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history11);

    VersionHistory history2 =
      new VersionHistory(new BigDecimal("2.0"), new Date(), new User(), PublicationStatus.PUBLIC);
    history2.setDoi(reserved2);
    history2.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history2);

    assertEquals(3, r.getVersionHistory().size());

    // ensure resource has reserved DOI
    assertTrue(r.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION));
    assertTrue(r.isAlreadyAssignedDoi());
    assertTrue(r.isPubliclyAvailable());

    // delete!
    action.setDelete("true");
    assertEquals("home", action.delete());
    assertEquals(PublicationStatus.DELETED, r.getStatus());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());

    // undelete!
    action.setUndelete("true");
    assertEquals("success", action.undelete());
    assertEquals(PublicationStatus.PUBLIC, r.getStatus());
    assertTrue(r.getIdentifierStatus().equals(IdentifierStatus.PUBLIC));
  }
}
