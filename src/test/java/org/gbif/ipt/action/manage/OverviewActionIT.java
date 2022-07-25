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
import org.gbif.datacite.rest.client.configuration.ClientConfiguration;
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
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.file.properties.PropertiesUtil;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WARNING! This requires live DataCite service.
 */
public class OverviewActionIT {

  private static final Logger LOG = LogManager.getLogger(OverviewActionIT.class);
  private static final UUID ORGANISATION_KEY = UUID.fromString("dce7a3c9-ea78-4be7-9abc-e3838de70dc5");

  private Resource r;

  public static Stream<Arguments> data() throws Exception {
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

    Properties p = PropertiesUtil.loadProperties("datacite.properties");
    ClientConfiguration cfg = ClientConfiguration.builder()
      .withBaseApiUrl(p.getProperty("baseApiUrl"))
      .withTimeOut(Long.valueOf(p.getProperty("timeOut")))
      .withFileCacheMaxSizeMb(Long.valueOf(p.getProperty("fileCacheMaxSizeMb")))
      .withUser(p.getProperty("user"))
      .withPassword(p.getProperty("password"))
      .build();

    //LOG.info("DataCite password (read from Maven property datacite.password)= " + dcCfg.getPassword());

    Organisation oDataCite = new Organisation();
    oDataCite.setKey(ORGANISATION_KEY.toString());
    oDataCite.setAgencyAccountPrimary(true);
    oDataCite.setName("GBIF");
    oDataCite.setDoiPrefix(Constants.TEST_DOI_PREFIX);
    oDataCite.setCanHost(true);
    oDataCite.setAgencyAccountUsername(cfg.getUser());
    oDataCite.setAgencyAccountPassword(cfg.getPassword());
    oDataCite.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCite);

    // mock RegistrationManager returning organisation by key
    when(mockRegistrationManagerDataCite.get(any(UUID.class))).thenReturn(oDataCite);

    // mock returning DataCite service
    DoiService dataCiteService = new RestJsonApiDataCiteService(cfg.getBaseApiUrl(), cfg.getUser(), cfg.getPassword());
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock action for DataCite
    OverviewAction actionDataCite =
        new OverviewAction(
            mock(SimpleTextProvider.class),
            mockAppConfig,
            mockRegistrationManagerDataCite,
            mock(ResourceManager.class),
            mock(UserAccountManager.class),
            mock(ExtensionManager.class),
            mock(GenerateDwcaFactory.class),
            mock(GenerateDataPackageFactory.class),
            mock(VocabulariesManager.class),
            mock(RegistryManager.class),
            mock(DataSchemaManager.class));

    return Stream.of(Arguments.of(actionDataCite, DOIRegistrationAgency.DATACITE));
  }

  /**
   * Generate a new test resource for each test.
   */
  public void before(OverviewAction action, DOIRegistrationAgency type) {
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

    // resource must be publicly available
    r.setStatus(PublicationStatus.PUBLIC);

    action.setResource(r);
    assertNull(r.getDoi());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertNotNull(r.getEml().getCitation());
    assertNull(r.getEml().getCitation().getIdentifier());

    LOG.warn("This test requires live " + type + " services");
  }

  /**
   * Reserve DOI for resource that has never been assigned a DOI.
   */
  @ParameterizedTest
  @MethodSource("data")
  public void testReserveDoi(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertNotNull(r.getDoiOrganisationKey());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier()); // doi set as citation id
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());
  }

  /**
   * Test reserving existing DOI, making sure the DOI is preserved (user wants to reuse existing DOI).
   */
  @ParameterizedTest
  @MethodSource("data")
  public void testReuseAndReserveExistingDoi(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());

    DOI existingDOI = new DOI(r.getDoi().toString());

    // reset DOI
    r.setDoi(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);
    r.setDoiOrganisationKey(null);
    r.getEml().getAlternateIdentifiers().clear();
    r.getEml().setCitation(null);

    // set citation identifier equal to DOI - this should get reused next time we reserve a DOI
    r.setCitationAutoGenerated(true);
    r.getEml().setCitation(new Citation("Replaced by auto-generated citation", existingDOI.toString()));

    action.reserveDoi();
    // make sure the existing DOI was reused
    assertEquals(existingDOI.getDoiName(), r.getDoi().getDoiName());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier()); // doi set as citation id
    LOG.info("Existing DOI was reused successfully, DOI=" + existingDOI.getDoiName());
  }

  /**
   * Test deleting reserved DOI, when the resource was never assigned a DOI before.
   */
  @ParameterizedTest
  @MethodSource("data")
  public void testDeleteReservedDoi(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    action.setDeleteDoi("true");
    action.setReserveDoi("true");
    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier()); // doi set as citation id
    assertFalse(r.isAlreadyAssignedDoi());
    LOG.info("DOI was reserved successfully, DOI=" + r.getDoi());

    action.deleteDoi();
    // make sure the reserved DOI was deleted
    assertNull(r.getDoi());
    assertNull(r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertEquals(0, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertNull(r.getEml().getCitation().getIdentifier()); // doi set as citation id
    assertFalse(r.isAlreadyAssignedDoi());
    LOG.info("Existing DOI was deleted successfully");
  }

  /**
   * Test deleting reserved DOI, when the resource was previously assigned a DOI.
   */
  @ParameterizedTest
  @MethodSource("data")
  public void testDeleteReservedDoiWhenPreviousDoiExists(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    action.setDeleteDoi("true");
    action.setReserveDoi("true");

    // mock resource being assigned DOI
    DOI assignedDoi = new DOI("10.5072/bclona1");
    r.setDoi(assignedDoi);
    r.setDoiOrganisationKey(ORGANISATION_KEY);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC);
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory history = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC);
    history.setModifiedBy(user);
    history.setDoi(assignedDoi);
    history.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history);
    assertNotNull(r.getDoi());
    r.getEml().getCitation().setIdentifier(r.getDoi().getUrl().toString());
    assertTrue(r.isAlreadyAssignedDoi());

    // reserve new DOI for resource
    action.reserveDoi();
    DOI reserved = r.getDoi();
    assertNotNull(reserved);
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, r.getIdentifierStatus());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals(reserved.toString(), r.getEml().getAlternateIdentifiers().get(0));
    assertEquals(reserved.getUrl().toString(), r.getEml().getCitation().getIdentifier()); // new DOI set as citation id
    LOG.info("DOI was reserved successfully, DOI=" + reserved);

    action.deleteDoi();
    // make sure the reserved DOI was deleted, and previous DOI reassigned
    assertEquals(assignedDoi.getDoiName(), r.getDoi().getDoiName());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(IdentifierStatus.PUBLIC, r.getIdentifierStatus());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size()); // alternate ids updated
    assertEquals("10.5072/bclona1", r.getEml().getAlternateIdentifiers().get(0));
    assertEquals("https://doi.org/10.5072/bclona1", r.getEml().getCitation().getIdentifier()); // citation id reset to previous DOI
    assertTrue(r.isAlreadyAssignedDoi());
    LOG.info("Existing DOI was deleted successfully");

    // for fun, try to publish resource having this deleted DOI - should not be possible!!
    r.setDoi(reserved);
    assertTrue(r.getDoi() != null && r.isPubliclyAvailable());
    // reset action errors, .clear() doesn't work
    List<String> collection = new ArrayList<>();
    action.setActionErrors(collection);

    action.setPublish("true");
    assertEquals("input", action.publish());
    assertEquals(1, action.getActionErrors().size());
    LOG.info("Publishing resource with deleted DOI failed as expected");
  }

  /**
   * Ensure publishing fails (is prevented from starting) if DOI reserved cannot be resolved.
   */
  @ParameterizedTest
  @MethodSource("data")
  public void testPublishFailsBecauseDOICannotBeResolved(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    // mock resource having DOI reserved that doesn't exist!
    DOI assignedDoi = DOIUtils.mintDOI(type, Constants.TEST_DOI_PREFIX);
    r.setDoi(assignedDoi);
    r.setDoiOrganisationKey(ORGANISATION_KEY);
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertTrue(r.getDoi() != null && r.isPubliclyAvailable());
    // reset action errors, .clear() doesn't work
    List<String> collection = new ArrayList<>();
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
  @ParameterizedTest
  @MethodSource("data")
  public void testDeleteAndUndeleteResourceAssignedMultipleDOIs(OverviewAction action, DOIRegistrationAgency type) throws Exception {
    before(action, type);
    LOG.info("Testing " + type + "...");
    action.setReserveDoi("true");

    action.reserveDoi();
    assertNotNull(r.getDoi());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
    assertEquals(1, r.getEml().getAlternateIdentifiers().size());
    assertNotNull(r.getEml().getCitation());
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier());
    assertEquals(r.getDoi().getUrl().toString(), r.getEml().getCitation().getIdentifier());

    DOI reserved1 = new DOI(r.getDoi().toString());

    // reset
    r.setDoi(null);
    r.setDoiOrganisationKey(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);
    r.getEml().getAlternateIdentifiers().clear();
    r.getEml().setCitation(null);

    action.reserveDoi();
    DOI reserved2 = new DOI(r.getDoi().toString());

    // reset
    r.setDoi(null);
    r.setDoiOrganisationKey(null);
    r.setIdentifierStatus(IdentifierStatus.UNRESERVED);
    r.getEml().getAlternateIdentifiers().clear();
    r.getEml().setCitation(null);

    action.reserveDoi();
    DOI reserved3 = new DOI(r.getDoi().toString());

    assertTrue(!reserved1.toString().equals(reserved2.toString()) && !reserved1.toString().equals(reserved3.toString())
        && !reserved2.toString().equals(reserved3.toString()));

    // mock VersionHistory: version 1.0 and 1.1 share same registered DOI, version 2.0 has different registered DOI
    VersionHistory history1 =
        new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC);
    history1.setDoi(reserved1);
    history1.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history1);


    VersionHistory history11 =
        new VersionHistory(new BigDecimal("1.1"), new Date(), PublicationStatus.PUBLIC);
    history11.setDoi(reserved1);
    history11.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history11);

    VersionHistory history2 =
        new VersionHistory(new BigDecimal("2.0"), new Date(), PublicationStatus.PUBLIC);
    history2.setDoi(reserved2);
    history2.setStatus(IdentifierStatus.PUBLIC);
    r.addVersionHistory(history2);

    assertEquals(3, r.getVersionHistory().size());

    // ensure resource has reserved DOI
    assertEquals(r.getIdentifierStatus(), IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertTrue(r.isAlreadyAssignedDoi());
    assertTrue(r.isPubliclyAvailable());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());

    // mock resource having 3 alternate identifier DOIs
    r.getEml().getAlternateIdentifiers().clear();
    r.getEml().getAlternateIdentifiers().add(reserved1.toString());
    r.getEml().getAlternateIdentifiers().add(reserved2.toString());
    r.getEml().getAlternateIdentifiers().add(reserved3.toString());
    assertEquals(3, r.getEml().getAlternateIdentifiers().size());

    // check citation identifier properly set
    assertNotNull(r.getEml().getCitation().getIdentifier());
    assertEquals(reserved3.getUrl().toString(), r.getEml().getCitation().getIdentifier());

    // delete!
    action.setDelete("true");
    assertEquals("home", action.delete());
    assertEquals(PublicationStatus.DELETED, r.getStatus());
    assertEquals(IdentifierStatus.UNRESERVED, r.getIdentifierStatus());
    assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());

    // should still be 2 alternate identifiers left (only the reserved DOI gets deleted from alternate identifiers list)
    assertEquals(2, r.getEml().getAlternateIdentifiers().size());

    // undelete!
    action.setUndelete("true");

    // since this integration tests undeletes a reserved DOI, this only works in DataCite
    if (type.equals(DOIRegistrationAgency.DATACITE)) {
      assertEquals("success", action.undelete());
      assertEquals(PublicationStatus.PUBLIC, r.getStatus());
      assertEquals(r.getIdentifierStatus(), IdentifierStatus.PUBLIC);
      assertEquals(ORGANISATION_KEY, r.getDoiOrganisationKey());
      assertEquals(2, r.getEml().getAlternateIdentifiers().size());
      // DOI of last published version should be used as citation identifier
      assertEquals(reserved2.getUrl().toString(), r.getEml().getCitation().getIdentifier());
    }
  }
}
