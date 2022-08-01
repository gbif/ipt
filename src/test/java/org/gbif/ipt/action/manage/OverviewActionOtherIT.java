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
import org.gbif.api.model.common.DoiData;
import org.gbif.api.model.common.DoiStatus;
import org.gbif.datacite.rest.client.configuration.ClientConfiguration;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.datacite.RestJsonApiDataCiteService;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.file.properties.PropertiesUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Other ITs for OverviewAction that can't run as parameterized tests for example or that
 * need a special DOI account.
 */
@Disabled
public class OverviewActionOtherIT {

  private static final Logger LOG = LogManager.getLogger(OverviewActionIT.class);
  private static final UUID ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5");
  private static ClientConfiguration cfg;
  private static DoiService dataCiteService;
  private Resource r;
  private OverviewAction action;

  @BeforeAll
  public static void setup() throws IOException {
    // load DataCite account username and password from the properties file
    Properties p = PropertiesUtil.loadProperties("datacite.properties");
    ClientConfiguration cfg = ClientConfiguration.builder()
      .withBaseApiUrl(p.getProperty("baseApiUrl"))
      .withTimeOut(Long.valueOf(p.getProperty("timeOut")))
      .withFileCacheMaxSizeMb(Long.valueOf(p.getProperty("fileCacheMaxSizeMb")))
      .withUser(p.getProperty("user"))
      .withPassword(p.getProperty("password"))
      .build();
    // configure a DataCite service for reuse in various Datacite related tests
    dataCiteService = new RestJsonApiDataCiteService(cfg.getBaseApiUrl(), cfg.getUser(), cfg.getPassword());
  }

  /**
   * Generate a new test resource for each test.
   */
  @BeforeEach
  public void before() {
    r = new Resource();
    Eml eml = new Eml();
    r.setEml(eml);

    // mandatory elements
    r.setCoreType("Occurrence");
    r.setTitle("Ants");
    r.setShortname("ants");
    eml.setTitle("Ants");

    Citation citation = new Citation();
    citation.setCitation("Smith J (2013). Ants. GBIF. Dataset");
    r.getEml().setCitation(citation);

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
    o.setKey(UUID.randomUUID().toString());
    r.setOrganisation(o);
  }

  /**
   * Test reserving an existing registered DOI (reusing an existing registered DOI) using GBIF's test DataCite account.
   * </b>
   * But, make it fail by making the resource private.
   */
  @Test
  public void testReuseAndReserveExistingRegisteredDoiFailsNotPublic() throws Exception {
    // common mock AppConfig
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);

    when(mockAppConfig.getDataDir()).thenReturn(mockDataDir);
    // mock returning target URLs
    when(mockAppConfig.getResourceUri(anyString())).thenReturn(new URI("http://ipt.gbif-uat.org/resource?r=migra3"));
    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    Organisation oDataCiteGBIF = new Organisation();
    oDataCiteGBIF.setKey(ORGANISATION_KEY.toString());
    oDataCiteGBIF.setAgencyAccountPrimary(true);
    oDataCiteGBIF.setName("GBIF");
    oDataCiteGBIF.setDoiPrefix("10.21373"); // TODO: 2019-06-19 wrong prefix
    oDataCiteGBIF.setCanHost(true);
    oDataCiteGBIF.setAgencyAccountUsername(cfg.getUser());
    oDataCiteGBIF.setAgencyAccountPassword(cfg.getPassword());
    oDataCiteGBIF.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mockAppConfig,
        mockRegistrationManagerDataCite,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));

    LOG.info("Testing DataCite with GBIF test Prefix...");
    action.setReserveDoi("true");

    action.setResource(r);
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertNotNull(r.getEml().getCitation());
    assertNull(r.getEml().getCitation().getIdentifier());

    DOI existingDOI = new DOI("doi:10.21373/xhav6t");
    // set citation identifier equal to existing registered DOI (doi:10.21373/xhav6t) - this should get reused
    r.setCitationAutoGenerated(true);
    r.getEml().setCitation(new Citation("Replaced by auto-generated citation", existingDOI.toString()));

    // start with private resource - error: resource must be public
    r.setStatus(PublicationStatus.PRIVATE);
    action.reserveDoi();

    // 1 error is expected for the resource not being public
    assertEquals(1, action.getActionErrors().size());

    // make sure the existing DOI was NOT reused
    assertNull(r.getDoi());
    assertNull(r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertEquals(0, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    LOG.info("Existing DOI was NOT reused because resource was private and had wrong target URI");
  }


  /**
   * Test reserving an existing registered DOI (reusing an existing registered DOI) using GBIF's test DataCite account.
   * </b>
   * But, make it fail by making it use the wrong target URI.
   */
  @Test
  public void testReuseAndReserveExistingRegisteredDoiFailsWrongTarget() throws Exception {
    // common mock AppConfig
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);

    when(mockAppConfig.getDataDir()).thenReturn(mockDataDir);
    // mock returning target URLs
    when(mockAppConfig.getResourceUri(anyString())).thenReturn(new URI("http://ipt.gbif-uat.org/resource?r=wrong"));
    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    Organisation oDataCiteGBIF = new Organisation();
    oDataCiteGBIF.setKey(ORGANISATION_KEY.toString());
    oDataCiteGBIF.setAgencyAccountPrimary(true);
    oDataCiteGBIF.setName("GBIF");
    oDataCiteGBIF.setDoiPrefix("10.21373");
    oDataCiteGBIF.setCanHost(true);
    oDataCiteGBIF.setAgencyAccountUsername(cfg.getUser());
    oDataCiteGBIF.setAgencyAccountPassword(cfg.getPassword());
    oDataCiteGBIF.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mockAppConfig,
        mockRegistrationManagerDataCite,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));

    LOG.info("Testing DataCite with GBIF test Prefix...");
    action.setReserveDoi("true");

    action.setResource(r);
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertNotNull(r.getEml().getCitation());
    assertNull(r.getEml().getCitation().getIdentifier());

    DOI existingDOI = new DOI("doi:10.15469/xhav6t");
    // set citation identifier equal to existing registered DOI (doi:10.15469/xhav6t) - this should get reused
    r.setCitationAutoGenerated(true);
    r.getEml().setCitation(new Citation("Replaced by auto-generated citation", existingDOI.toString()));

    // start with private resource - error: resource must be public
    r.setStatus(PublicationStatus.PUBLIC);
    action.reserveDoi();

    // 1 error is expected for the resource having wrong target URI
    assertEquals(1, action.getActionErrors().size());

    // make sure the existing DOI was NOT reused
    assertNull(r.getDoi());
    assertNull(r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertEquals(0, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    LOG.info("Existing DOI was NOT reused because resource was private and had wrong target URI");
  }

  /**
   * Test reserving an existing registered DOI (reusing an existing registered DOI) using GBIF's test DataCite account.
   * </b>
   * Make it succeed by making the resource public and using the correct target URI.
   */
  @Test
  public void testReuseAndReserveExistingRegisteredDoi() throws Exception {
    // common mock AppConfig
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);

    when(mockAppConfig.getDataDir()).thenReturn(mockDataDir);
    // mock returning target URLs
    when(mockAppConfig.getResourceUri(anyString())).thenReturn(new URI("http://ipt.gbif-uat.org/resource?r=migra3"));
    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    Organisation oDataCiteGBIF = new Organisation();
    oDataCiteGBIF.setKey(ORGANISATION_KEY.toString());
    oDataCiteGBIF.setAgencyAccountPrimary(true);
    oDataCiteGBIF.setName("GBIF");
    oDataCiteGBIF.setDoiPrefix("10.21373");
    oDataCiteGBIF.setCanHost(true);
    oDataCiteGBIF.setAgencyAccountUsername(cfg.getUser());
    oDataCiteGBIF.setAgencyAccountPassword(cfg.getPassword());
    oDataCiteGBIF.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mockAppConfig,
        mockRegistrationManagerDataCite,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));

    LOG.info("Testing DataCite with GBIF test Prefix...");
    action.setReserveDoi("true");

    action.setResource(r);
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertNotNull(r.getEml().getCitation());
    assertNull(r.getEml().getCitation().getIdentifier());

    DOI existingDOI = new DOI("doi:10.21373/xhav6t");
    // set citation identifier equal to existing registered DOI (doi:10.15469/xhav6t) - this should get reused
    r.setCitationAutoGenerated(true);
    r.getEml().setCitation(new Citation("Replaced by auto-generated citation", existingDOI.toString()));

    // resource must be public
    r.setStatus(PublicationStatus.PUBLIC);

    action.reserveDoi();

    // 0 errors are expected
    assertEquals(0, action.getActionErrors().size());

    // make sure the existing DOI was NOT reused
    assertNotNull(r.getDoi());
    assertEquals(existingDOI.getDoiName(), r.getDoi().getDoiName());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier()); // doi set as citation id
    LOG.info("Existing registered DOI was reused successfully, DOI=" + existingDOI.getDoiName());
  }

  /**
   * Test deleting a reserved existing DOI registered with DataCite.
   */
  @Test
  public void testDeleteReservedExistingRegisteredDoiDataCite() throws Exception {
    // common mock AppConfig
    AppConfig mockAppConfig = mock(AppConfig.class);

    RegistrationManager mockRegistrationManagerDataCite = mock(RegistrationManager.class);

    Organisation oDataCiteGBIF = new Organisation();
    oDataCiteGBIF.setKey(ORGANISATION_KEY.toString());
    oDataCiteGBIF.setAgencyAccountPrimary(true);
    oDataCiteGBIF.setName("GBIF");
    oDataCiteGBIF.setDoiPrefix("10.21373"); // TODO: 2019-06-19 move to some property
    oDataCiteGBIF.setCanHost(true);
    oDataCiteGBIF.setAgencyAccountUsername(cfg.getUser());
    oDataCiteGBIF.setAgencyAccountPassword(cfg.getPassword());
    oDataCiteGBIF.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCiteGBIF);
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    action = new OverviewAction(
        mock(SimpleTextProvider.class),
        mockAppConfig,
        mockRegistrationManagerDataCite,
        mock(ResourceManager.class),
        mock(UserAccountManager.class),
        mock(ExtensionManager.class),
        mock(GenerateDwcaFactory.class),
        mock(VocabulariesManager.class),
        mock(RegistryManager.class));

    LOG.info("Testing DataCite with test Prefix...");
    action.setDeleteDoi("true");

    // mock resource having DOI
    DOI existingDOI = new DOI("doi:10.21373/xhav6t");
    // set citation identifier equal to existing registered DOI - this should get reused
    r.setCitationAutoGenerated(true);
    r.getEml().setCitation(new Citation("Replaced by auto-generated citation", existingDOI.toString()));
    r.setDoi(existingDOI);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);

    action.setResource(r);
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertNotNull(r.getEml().getCitation());
    assertNotNull(r.getEml().getCitation().getIdentifier());

    final DoiData doiDataExistingDoi = dataCiteService.resolve(existingDOI);
    if (doiDataExistingDoi.getStatus() == DoiStatus.NEW && doiDataExistingDoi.getStatus() == DoiStatus.FAILED) {
      final DataCiteMetadata metadata = DataCiteMetadataBuilder.createDataCiteMetadata(existingDOI, r);
      dataCiteService.register(existingDOI, URI.create("https://www.gbif-dev.org/"), metadata);
    }
    action.deleteDoi();

    DoiData doiData = dataCiteService.resolve(existingDOI);
    assertNotNull(doiData);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.REGISTERED, doiData.getStatus());

    assertNull(r.getDoi());
    assertNull(r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertEquals(0, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertNull(r.getEml().getCitation().getIdentifier());
    LOG.info("Existing reserved registered DOI was deleted successfully without making it unavailable, DOI=" + existingDOI.getDoiName());
  }
}
