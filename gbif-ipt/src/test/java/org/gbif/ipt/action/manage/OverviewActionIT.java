package org.gbif.ipt.action.manage;


import org.gbif.api.model.common.DOI;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.ServiceConfig;
import org.gbif.doi.service.datacite.DataCiteService;
import org.gbif.doi.service.ezid.EzidService;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
  public static Iterable data() throws IOException {

    // DataCite parameters..
    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    InputStream dc = FileUtils.classpathStream("resources/datacite.yaml");
    ServiceConfig dcCfg = mapper.readValue(dc, ServiceConfig.class);
    LOG.info(dcCfg);

    Organisation oDataCite = new Organisation();
    oDataCite.setAgencyAccountPrimary(true);
    oDataCite.setName("GBIF");
    oDataCite.setDoiPrefix("10.5072");
    oDataCite.setCanHost(true);
    oDataCite.setAgencyAccountUsername(dcCfg.getUsername());
    oDataCite.setAgencyAccountPassword(dcCfg.getPassword());
    oDataCite.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCite);

    // mock returning DataCite service
    DoiService dataCiteService = new DataCiteService(HttpUtil.newMultithreadedClient(10000, 3, 2), dcCfg);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    OverviewAction actionDataCite =
      new OverviewAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mockRegistrationManagerDataCite,
        mock(ResourceManager.class), mock(UserAccountManager.class), mock(ExtensionManager.class),
        mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));
    actionDataCite.setReserveDoi("true");

    // EZID parameters..
    RegistrationManager mockRegistrationManagerEZID = mock(RegistrationManager.class);

    Organisation oEZID = new Organisation();
    oEZID.setAgencyAccountPrimary(true);
    oEZID.setName("GBIF");
    oEZID.setDoiPrefix("10.5072");
    oEZID.setCanHost(true);
    oEZID.setAgencyAccountUsername("apitest");
    oEZID.setAgencyAccountPassword("apitest");
    oEZID.setDoiRegistrationAgency(DOIRegistrationAgency.EZID);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerEZID.findPrimaryDoiAgencyAccount()).thenReturn(oEZID);

    // mock returning EZID service
    ServiceConfig cfgEZID = new ServiceConfig("apitest", "apitest");
    EzidService ezidService = new EzidService(HttpUtil.newMultithreadedClient(10000, 2, 2), cfgEZID);
    when(mockRegistrationManagerEZID.getDoiService()).thenReturn(ezidService);

    // mock action for EZID
    OverviewAction actionEZID =
      new OverviewAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mockRegistrationManagerEZID,
        mock(ResourceManager.class), mock(UserAccountManager.class), mock(ExtensionManager.class),
        mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));
    actionEZID.setReserveDoi("true");

    return Arrays.asList(
      new Object[][] {{actionDataCite, DOIRegistrationAgency.DATACITE}, {actionEZID, DOIRegistrationAgency.EZID}});
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
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());

    DOI existingDOI = new DOI(r.getDoi());
    Citation citation = new Citation("Mock Citation", existingDOI.toString());
    r.getEml().setCitation(citation);
    // reset DOI
    r.setDoi(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);

    action.reserveDoi();
    // make sure the existing DOI was reused
    assertEquals(existingDOI.getDoiName(), r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    LOG.info("Existing DOI was reused successfully, DOI=" + existingDOI.getDoiName());
  }

}
