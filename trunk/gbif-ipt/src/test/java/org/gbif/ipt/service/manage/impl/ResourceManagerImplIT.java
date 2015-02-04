package org.gbif.ipt.service.manage.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.DoiData;
import org.gbif.api.model.common.DoiStatus;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.ServiceConfig;
import org.gbif.doi.service.datacite.DataCiteService;
import org.gbif.doi.service.ezid.EzidService;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.manage.OverviewAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.ws.rs.core.UriBuilder;

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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class ResourceManagerImplIT {

  private static final Logger LOG = Logger.getLogger(ResourceManagerImplIT.class);
  private static DataDir MOCK_DATA_DIR = mock(DataDir.class);
  private static File TMP_EML_FILE;

  private Resource r;
  private ResourceManagerImpl manager;
  private DOIRegistrationAgency type;
  private DOI doi;
  private RegistrationManager registrationManager;

  public ResourceManagerImplIT(ResourceManagerImpl action, DOIRegistrationAgency type, DOI doi,
    RegistrationManager registrationManager) {
    this.manager = action;
    this.type = type;
    this.doi = doi;
    this.registrationManager = registrationManager;
  }

  @Parameterized.Parameters
  public static Iterable data() throws IOException {
    // Mock classes
    AppConfig mockAppConfig = mock(AppConfig.class);

    // mock returning resource URI in gbif-uat belong
    when(mockAppConfig.getResourceUri(anyString()))
      .thenReturn(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt?r=ants").build());
    when(mockAppConfig.getResourceVersionUri("ants", new BigDecimal("1.1")))
      .thenReturn(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt?r=ants&v=1.1").build());
    when(mockAppConfig.getMaxThreads()).thenReturn(3);

    UserAccountManager mockUserAccountManager = mock(UserAccountManager.class);
    UserEmailConverter mockEmailConverter = new UserEmailConverter(mockUserAccountManager);
    ExtensionRowTypeConverter mockExtensionRowTypeConverter = mock(ExtensionRowTypeConverter.class);
    ExtensionManager mockExtensionManager = mock(ExtensionManager.class);
    JdbcInfoConverter mockJdbcConverter = mock(JdbcInfoConverter.class);
    SourceManager mockSourceManager = mock(SourceManager.class);
    RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
    GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
    PasswordConverter mockPasswordConverter = mock(PasswordConverter.class);
    Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
    VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    ConceptTermConverter mockConceptTermConverter = mock(ConceptTermConverter.class);

    // persist eml file for version 1.0 (contents written later)
    TMP_EML_FILE = File.createTempFile("eml-1.0", ".xml");
    when(MOCK_DATA_DIR.resourceEmlFile("ants", new BigDecimal("1.1"))).thenReturn(TMP_EML_FILE);

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

    // mock returning DataCite service
    DoiService dataCiteService = new DataCiteService(HttpUtil.newMultithreadedClient(10000, 3, 2), dcCfg);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock ResourceManagerImpl for DataCite
    ResourceManagerImpl managerDataCite = new ResourceManagerImpl(mockAppConfig, MOCK_DATA_DIR, mockEmailConverter,
      new OrganisationKeyConverter(mockRegistrationManagerDataCite), mockExtensionRowTypeConverter, mockJdbcConverter,
      mockSourceManager, mockExtensionManager, mockRegistryManager, mockConceptTermConverter, mockDwcaFactory,
      mockPasswordConverter, mockEml2Rtf, mockVocabulariesManager, mockSimpleTextProvider,
      mockRegistrationManagerDataCite);


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

    // mock returning EZID service
    ServiceConfig cfgEZID = new ServiceConfig("apitest", "apitest");
    EzidService ezidService = new EzidService(HttpUtil.newMultithreadedClient(10000, 2, 2), cfgEZID);
    when(mockRegistrationManagerEZID.getDoiService()).thenReturn(ezidService);

    // mock action for EZID
    OverviewAction actionEZID =
      new OverviewAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mockRegistrationManagerEZID,
        mock(ResourceManager.class), mock(UserAccountManager.class), mock(ExtensionManager.class),
        mock(VocabulariesManager.class), mock(GenerateDwcaFactory.class));

    // mock ResourceManagerImpl for EZID
    ResourceManagerImpl managerEZID = new ResourceManagerImpl(mockAppConfig, MOCK_DATA_DIR, mockEmailConverter,
      new OrganisationKeyConverter(mockRegistrationManagerEZID), mockExtensionRowTypeConverter, mockJdbcConverter,
      mockSourceManager, mockExtensionManager, mockRegistryManager, mockConceptTermConverter, mockDwcaFactory,
      mockPasswordConverter, mockEml2Rtf, mockVocabulariesManager, mockSimpleTextProvider, mockRegistrationManagerEZID);

    return Arrays.asList(new Object[][] {{managerDataCite, DOIRegistrationAgency.DATACITE,
      DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX), mockRegistrationManagerDataCite}
      // TODO undelete when EZID is back up
//      ,{managerEZID, DOIRegistrationAgency.EZID,DOIUtils.mintDOI(DOIRegistrationAgency.EZID, Constants.EZID_TEST_DOI_SHOULDER), mockRegistrationManagerEZID}
    });
  }

  /**
   * Generate a brand new (unpublished) test resource for each test.
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

    r.setEmlVersion(Constants.INITIAL_RESOURCE_VERSION);
    r.setStatus(PublicationStatus.PUBLIC);
    assertNull(r.getLastPublished());
  }

  /**
   * Reserve and then register DOI for public resource.
   * </br>
   * Then test updating the DOI with a new version number.
   * </br>
   * Lastly test replacing this DOI with a new DOI, by reserving and registering a new DOI for the resource. The
   * replaced DOI should still be registered, but its metadata should reflect the fact it has been replaced by the new
   * version, and its target URI should point to that version of the resource.
   */
  @Test
  public void testRegisterDoiWorkflow() throws Exception {
    LOG.info("Testing " + type + "...");

    // reserve DOI to begin with
    assertNotNull(doi);
    assertEquals(PublicationStatus.PUBLIC, r.getStatus());
    r.setDoi(doi);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertEquals(Constants.INITIAL_RESOURCE_VERSION.toPlainString(), r.getEmlVersion().toPlainString());
    DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, r);
    registrationManager.getDoiService().reserve(doi, dataCiteMetadata);

    // check DOI is reserved, and its target is null
    DoiData doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.RESERVED, doiData.getStatus());
    assertNull(doiData.getTarget());

    // register DOI
    manager.doRegisterDoi(r, null);
    assertEquals(IdentifierStatus.PUBLIC, r.getIdentifierStatus());
    LOG.info("DOI was registered successfully, DOI=" + doi.getDoiName());

    // check DOI was registered, and its target is correct
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertTrue(doiData.getStatus().isRegistered());
    assertNotNull(doiData.getTarget());
    assertEquals(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt%3Fr=ants").build().toString(),
      doiData.getTarget().toString());

    // mock version 1.0 having been published by setting last published, and adding new VersionHistory
    r.setLastPublished(new Date());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory history = new VersionHistory(new BigDecimal("1.0"), new Date(), user, PublicationStatus.PUBLIC);
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setDoi(doi);
    r.addVersionHistory(history);

    // persist eml file for version 1.0
    EmlWriter.writeEmlFile(TMP_EML_FILE, r.getEml());

    // update DOI for next published version
    BigDecimal nextVersion = r.getNextVersion();
    r.setEmlVersion(nextVersion);
    assertEquals("1.1", r.getEmlVersion().toPlainString());
    assertEquals("1.0", r.getReplacedEmlVersion().toPlainString());
    manager.doUpdateDoi(r);
    LOG.info("DOI was updated successfully, DOI=" + doi.getDoiName());

    // check DOI remains registered, and its target is the same
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertTrue(doiData.getStatus().isRegistered());
    assertNotNull(doiData.getTarget());
    assertEquals(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt%3Fr=ants").build().toString(),
      doiData.getTarget().toString());

    // mock version 1.1 having been published by setting last published, and adding new VersionHistory
    r.setLastPublished(new Date());
    VersionHistory history2 = new VersionHistory(new BigDecimal("1.1"), new Date(), user, PublicationStatus.PUBLIC);
    history2.setDoi(doi);
    history2.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history2);

    // reserve another new DOI
    DOI newDoi = DOIUtils.mintDOI(type, registrationManager.findPrimaryDoiAgencyAccount().getDoiPrefix());
    r.setDoi(newDoi);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(newDoi, r);
    registrationManager.getDoiService().reserve(newDoi, dataCiteMetadata);

    // check DOI is reserved, and its target is null
    doiData = registrationManager.getDoiService().resolve(newDoi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.RESERVED, doiData.getStatus());
    assertNull(doiData.getTarget());

    // replace DOI with new DOI, and publish version 2.0
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    nextVersion = r.getNextVersion(); // new major version
    r.setEmlVersion(nextVersion);
    assertEquals("2.0", r.getEmlVersion().toPlainString());
    assertNotNull(r.getAssignedDoi());
    assertEquals("1.1", r.getReplacedEmlVersion().toPlainString());
    manager.doReplaceDoi(r, r.getEmlVersion(), r.getReplacedEmlVersion());

    // check new DOI is registered now, and its target is equal to resource URI
    doiData = registrationManager.getDoiService().resolve(newDoi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.REGISTERED, doiData.getStatus());
    assertNotNull(doiData.getTarget());
    assertEquals(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt%3Fr=ants").build().toString(),
      doiData.getTarget().toString());

    // check replaced DOI is still registered, and its target is equal to resource version URI
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.REGISTERED, doiData.getStatus());
    assertNotNull(doiData.getTarget());
    assertEquals(UriBuilder.fromPath("http://www.gbif-uat.org:7001/ipt%3Fr=ants&v=1.1").build().toString(),
      doiData.getTarget().toString());
  }
}
