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

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.DoiData;
import org.gbif.api.model.common.DoiStatus;
import org.gbif.datacite.rest.client.configuration.ClientConfiguration;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.datacite.RestJsonApiDataCiteService;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataSchemaIdentifierConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.file.properties.PropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceManagerImplIT {

  private static final Logger LOG = LogManager.getLogger(ResourceManagerImplIT.class);
  private static DataDir MOCK_DATA_DIR = mock(DataDir.class);
  private static File TMP_EML_FILE;

  private Resource resource;

  public static Stream<Arguments> data() throws IOException {
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
    DataSchemaManager mockSchemaManager = mock(DataSchemaManager.class);
    JdbcInfoConverter mockJdbcConverter = mock(JdbcInfoConverter.class);
    SourceManager mockSourceManager = mock(SourceManager.class);
    RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
    GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
    PasswordEncrypter mockPasswordEncrypter = mock(PasswordEncrypter.class);
    Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
    VocabulariesManager mockVocabulariesManager = mock(VocabulariesManager.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    ConceptTermConverter mockConceptTermConverter = mock(ConceptTermConverter.class);

    // persist eml file for version 1.0 (contents written later)
    TMP_EML_FILE = File.createTempFile("eml-1.0", ".xml");
    when(MOCK_DATA_DIR.resourceEmlFile("ants", new BigDecimal("1.1"))).thenReturn(TMP_EML_FILE);

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
    oDataCite.setAgencyAccountPrimary(true);
    oDataCite.setName("GBIF");
    oDataCite.setDoiPrefix(Constants.TEST_DOI_PREFIX);
    oDataCite.setCanHost(true);
    oDataCite.setAgencyAccountUsername(cfg.getUser());
    oDataCite.setAgencyAccountPassword(cfg.getPassword());
    oDataCite.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);

    // mock returning primary DOI agency account
    when(mockRegistrationManagerDataCite.findPrimaryDoiAgencyAccount()).thenReturn(oDataCite);

    // mock returning DataCite service
    DoiService dataCiteService = new RestJsonApiDataCiteService(cfg.getBaseApiUrl(), cfg.getUser(), cfg.getPassword());
    when(mockRegistrationManagerDataCite.getDoiService()).thenReturn(dataCiteService);

    // mock ResourceManagerImpl for DataCite
    ResourceManagerImpl managerDataCite = new ResourceManagerImpl(
        mockAppConfig,
        MOCK_DATA_DIR,
        mockEmailConverter,
        new OrganisationKeyConverter(mockRegistrationManagerDataCite),
        mockExtensionRowTypeConverter,
        mock(DataSchemaIdentifierConverter.class),
        mockJdbcConverter,
        mockSourceManager,
        mockExtensionManager,
        mockSchemaManager,
        mockRegistryManager,
        mockConceptTermConverter,
        mockDwcaFactory,
        mock(GenerateDataPackageFactory.class),
        mockPasswordEncrypter,
        mockEml2Rtf,
        mockVocabulariesManager,
        mockSimpleTextProvider,
        mockRegistrationManagerDataCite);

    return Stream.of(
        Arguments.of(managerDataCite, DOIRegistrationAgency.DATACITE, DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX), mockRegistrationManagerDataCite)
    );
  }

  /**
   * Generate a brand new (unpublished) test resource for each test.
   */
  @BeforeEach
  public void before() {
    resource = new Resource();
    Eml eml = new Eml();
    resource.setEml(eml);

    // mandatory elements
    resource.setCoreType("Occurrence");
    resource.setTitle("Ants");
    resource.setShortname("ants");
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
    o.setKey(UUID.randomUUID().toString());
    resource.setOrganisation(o);

    resource.setMetadataVersion(Constants.INITIAL_RESOURCE_VERSION);
    resource.setStatus(PublicationStatus.PUBLIC);
    assertNull(resource.getLastPublished());
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
  @ParameterizedTest
  @MethodSource("data")
  public void testRegisterDoiWorkflow(ResourceManagerImpl manager,
                                      DOIRegistrationAgency type,
                                      DOI doi,
                                      RegistrationManager registrationManager) throws Exception {
    LOG.info("Testing " + type + "...");
    String expectedDataciteUrl = URI.create("http://www.gbif-uat.org:7001/ipt?r=ants").toString();

    // reserve DOI to begin with
    assertNotNull(doi);
    assertEquals(PublicationStatus.PUBLIC, resource.getStatus());
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    assertEquals(Constants.INITIAL_RESOURCE_VERSION.toPlainString(), resource.getEmlVersion().toPlainString());
    DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
    registrationManager.getDoiService().reserve(doi, dataCiteMetadata);

    // check DOI is reserved, and its target is null
    DoiData doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.RESERVED, doiData.getStatus());
    if (type == DOIRegistrationAgency.DATACITE) {
      assertNull(doiData.getTarget());
    }

    // register DOI
    manager.doRegisterDoi(resource, null);
    assertEquals(IdentifierStatus.PUBLIC, resource.getIdentifierStatus());
    LOG.info("DOI was registered successfully, DOI=" + doi.getDoiName());

    // check DOI was registered, and its target is correct
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertTrue(doiData.getStatus().isRegistered());
    assertNotNull(doiData.getTarget());

    if (type == DOIRegistrationAgency.DATACITE) {
      assertEquals(expectedDataciteUrl, decodeUrl(doiData.getTarget().toString()));
    }

    // mock version 1.0 having been published by setting last published, and adding new VersionHistory
    resource.setLastPublished(new Date());
    User user = new User();
    user.setEmail("jsmith@gbif.org");
    VersionHistory history = new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC);
    history.setModifiedBy(user);
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setDoi(doi);
    resource.addVersionHistory(history);

    // persist eml file for version 1.0
    EmlWriter.writeEmlFile(TMP_EML_FILE, resource.getEml());

    // update DOI for next published version
    BigDecimal nextVersion = resource.getNextVersion();
    resource.setMetadataVersion(nextVersion);
    assertEquals("1.1", resource.getEmlVersion().toPlainString());
    assertEquals("1.0", resource.getReplacedMetadataVersion().toPlainString());
    manager.doUpdateDoi(resource);
    LOG.info("DOI was updated successfully, DOI=" + doi.getDoiName());

    // check DOI remains registered, and its target is the same
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertTrue(doiData.getStatus().isRegistered());
    assertNotNull(doiData.getTarget());

    if (type == DOIRegistrationAgency.DATACITE) {
      assertEquals(expectedDataciteUrl, decodeUrl(doiData.getTarget().toString()));
    }

    // mock version 1.1 having been published by setting last published, and adding new VersionHistory
    resource.setLastPublished(new Date());
    VersionHistory history2 = new VersionHistory(new BigDecimal("1.1"), new Date(), PublicationStatus.PUBLIC);
    history.setModifiedBy(user);
    history2.setDoi(doi);
    history2.setStatus(IdentifierStatus.PUBLIC);
    resource.addVersionHistory(history2);

    // reserve another new DOI
    DOI newDoi = DOIUtils.mintDOI(type, registrationManager.findPrimaryDoiAgencyAccount().getDoiPrefix());
    resource.setDoi(newDoi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(newDoi, resource);
    registrationManager.getDoiService().reserve(newDoi, dataCiteMetadata);

    // check DOI is reserved, and its target is null
    doiData = registrationManager.getDoiService().resolve(newDoi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.RESERVED, doiData.getStatus());
    if (type == DOIRegistrationAgency.DATACITE) {
      assertNull(doiData.getTarget());
    }

    // replace DOI with new DOI, and publish version 2.0
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());
    nextVersion = resource.getNextVersion(); // new major version
    resource.setMetadataVersion(nextVersion);
    assertEquals("2.0", resource.getEmlVersion().toPlainString());
    assertNotNull(resource.getAssignedDoi());
    assertEquals("1.1", resource.getReplacedMetadataVersion().toPlainString());
    manager.doReplaceDoi(resource, resource.getEmlVersion(), resource.getReplacedMetadataVersion());

    // check new DOI is registered now, and its target is equal to resource URI
    doiData = registrationManager.getDoiService().resolve(newDoi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.REGISTERED, doiData.getStatus());
    assertNotNull(doiData.getTarget());

    if (type == DOIRegistrationAgency.DATACITE) {
      assertEquals(expectedDataciteUrl, decodeUrl(doiData.getTarget().toString()));
    }

    // check replaced DOI is still registered, and its target is equal to resource version URI
    doiData = registrationManager.getDoiService().resolve(doi);
    assertNotNull(doiData.getStatus());
    assertEquals(DoiStatus.REGISTERED, doiData.getStatus());
    assertNotNull(doiData.getTarget());

    if (type == DOIRegistrationAgency.DATACITE) {
      assertEquals(URI.create("http://www.gbif-uat.org:7001/ipt?r=ants&v=1.1").toString(), decodeUrl(doiData.getTarget().toString()));
    }
  }

  private String decodeUrl(String url) throws UnsupportedEncodingException {
    return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
  }
}
