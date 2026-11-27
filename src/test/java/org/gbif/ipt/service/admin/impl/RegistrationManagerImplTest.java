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
package org.gbif.ipt.service.admin.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.ipt.utils.PBEEncrypt;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.converters.ConversionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationManagerImplTest extends IptMockBaseTest {

  // Key of Academy of Natural Sciences - Organisation IPT is associated to
  private static final String HOSTING_ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
  // Key of Academy of Natural Sciences - Organisation that registered resource #1 is associated to
  private static final String RESOURCE1_ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
  // Key of Zoological Museum Denmark - Organisation that resource #2's registered DOI is associated to
  private static final String RESOURCE2_ORGANISATION_KEY = "456058db-f70b-4005-97ad-e08570cf0c56";

  // the hosting organisation's token in its associated organisation entry of registration2.xml
  private static final String ASSOCIATED_TOKEN = "PsQfBTu3GJfy00mWe2MyOg=";

  private RegistrationManager registrationManager;
  private DataDir mockDataDir;

  private List<Resource> resourcesList = new ArrayList<>();

  // RegistrationManagerImpl deletes registration.xml and overwrites registration2.xml, so tests work on copies
  @TempDir
  Path tempDir;

  private RegistryManager registryManager;
  private HttpClient httpClient;
  private File registrationXml2;

  @BeforeEach
  public void setup() throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock instances
    AppConfig mockAppConfig = mock(AppConfig.class);
    mockDataDir = mock(DataDir.class);
    SAXParserFactory mockSAXParserFactory = mock(SAXParserFactory.class);
    ConfigWarnings mockConfigWarnings = mock(ConfigWarnings.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    HttpClient mockHttpClient;
    HttpResponse mockResponse;
    ExtendedResponse extResponse;

    // create Resource associated to Organisation
    Resource r1 = new Resource();
    r1.setShortname("Test Resource");
    Organisation r1Org = new Organisation();
    r1Org.setName("Old Name Academy of Natural Sciences");
    r1Org.setKey(RESOURCE1_ORGANISATION_KEY);
    r1.setStatus(PublicationStatus.REGISTERED);
    r1.setOrganisation(r1Org);

    // create Resource whose registered DOI is associated to Organisation
    Resource r2 = new Resource();
    r2.setShortname("Another Test Resource");
    r2.setDoiOrganisationKey(UUID.fromString(RESOURCE2_ORGANISATION_KEY));
    r2.setStatus(PublicationStatus.PUBLIC);
    r2.setIdentifierStatus(IdentifierStatus.PUBLIC);
    r2.setDoi(new DOI("doi:10.1594/PANGAEA.726855"));

    // mock list() to return list with the mocked Resource - notable its organisation name is the old version
    resourcesList = new ArrayList<>();
    resourcesList.add(r1);
    resourcesList.add(r2);

    // mock returning registration.xml file
    File registrationXML = copyToTempDir("config/registration.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V1)).thenReturn(registrationXML);

    // mock returning registration2.xml file
    registrationXml2 = copyToTempDir("config/registration2.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V2)).thenReturn(registrationXml2);

    // mock returning list of registered Organisation with local test resource
    mockHttpClient = mock(HttpClient.class);
    mockResponse = mock(HttpResponse.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
    extResponse = new ExtendedResponse(mockResponse);
    extResponse.setContent(
      IOUtils.toString(
          Objects.requireNonNull(RegistrationManagerImplTest.class.getResourceAsStream("/responses/organisation.json")),
          StandardCharsets.UTF_8));
    when(mockHttpClient.get(anyString())).thenReturn(extResponse);
    httpClient = mockHttpClient;

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpClient, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider);

    // make sure the list of organisations is fully populated
    assertNotNull(mockRegistryManager.getRegisteredOrganisation(RESOURCE1_ORGANISATION_KEY));
    registryManager = mockRegistryManager;

    // create instance of manager
    registrationManager =
      new RegistrationManagerImpl(mockAppConfig, mockDataDir, mockRegistryManager,
        mock(PasswordEncrypter.class));

    // load associatedOrganisations, hostingOrganisation, etc
    registrationManager.load();
  }

  /**
   * Copies a classpath file into the temporary directory, so the original test resource is never modified.
   */
  private File copyToTempDir(String classpathFile) throws IOException {
    File source = FileUtils.getClasspathFile(classpathFile);
    Path target = tempDir.resolve(source.getName());
    Files.copy(source.toPath(), target);
    return target.toFile();
  }

  @Test
  public void testDeleteOrganizationAssociatedToResource() {
    // try deleting the Academy of Natural Sciences - it will throw a DeletionNotAllowedException since there is a
    // resource associated to it
    try {
      registrationManager.delete(RESOURCE1_ORGANISATION_KEY, resourcesList);
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.RESOURCE_REGISTERED_WITH_ORGANISATION, e.getReason());
    }
  }

  @Test
  public void updateOrganisationNamesOnLoad() {
    // Hosting organisation name: changed from "Old Name Academy of Natural Sciences" as per latest registry response
    // mocked from organisation/<key>.json
    Organisation host = registrationManager.getHostingOrganisation();
    assertEquals(HOSTING_ORGANISATION_KEY, host.getKey().toString());
    assertEquals("New Name Academy of Natural Sciences", host.getName());
    assertEquals(HOSTING_ORGANISATION_KEY, host.getKey().toString());

    // Associated organisation name: changed from "Old Name Academy of Natural Sciences" as per latest registry
    // response mocked from organisation/<key>.json
    Organisation associated = registrationManager.get(RESOURCE1_ORGANISATION_KEY);
    assertEquals("New Name Academy of Natural Sciences", associated.getName());
    assertEquals(RESOURCE1_ORGANISATION_KEY, associated.getKey().toString());
  }

  /**
   * The mocked Registry answers every request with the Academy of Natural Sciences, so the other associated
   * organisations come back with the wrong key and are reported as not updated. The hosting organisation is the
   * Academy itself, so it's updated and not reported.
   */
  @Test
  public void updateAssociatedOrganisationsMetadataReportsNotUpdated() throws IOException {
    List<String> notUpdated = registrationManager.updateAssociatedOrganisationsMetadata().stream()
      .map(o -> o.getKey().toString())
      .sorted()
      .collect(Collectors.toList());

    List<String> expected = registrationManager.list().stream()
      .map(o -> o.getKey().toString())
      .filter(key -> !key.equals(HOSTING_ORGANISATION_KEY) && !key.equals(Constants.DEFAULT_ORG_KEY.toString()))
      .sorted()
      .collect(Collectors.toList());

    assertFalse(expected.isEmpty());
    assertEquals(expected, notUpdated);
  }

  /**
   * The mocked Registry answers every request with the Academy of Natural Sciences: the Academy is updated (it's
   * also the hosting organisation), any other organisation gets the wrong key back and keeps its details.
   */
  @Test
  public void updateOrganisationFromRegistry() throws Exception {
    registrationManager.get(HOSTING_ORGANISATION_KEY).setName("Stale name");

    assertTrue(registrationManager.updateOrganisationFromRegistry(HOSTING_ORGANISATION_KEY));
    assertEquals("New Name Academy of Natural Sciences", registrationManager.get(HOSTING_ORGANISATION_KEY).getName());
    assertEquals("New Name Academy of Natural Sciences", registrationManager.getHostingOrganisation().getName());

    Organisation other = registrationManager.get(RESOURCE2_ORGANISATION_KEY);
    String otherName = other.getName();
    assertFalse(registrationManager.updateOrganisationFromRegistry(RESOURCE2_ORGANISATION_KEY));
    assertEquals(otherName, registrationManager.get(RESOURCE2_ORGANISATION_KEY).getName());

    assertThrows(IllegalArgumentException.class,
      () -> registrationManager.updateOrganisationFromRegistry("00000000-0000-0000-0000-000000000001"));
  }

  /**
   * Test migrating former registration configuration (registration.xml) to encrypted registration configuration
   * (registration2.xml) but on converting a LegacyOrganization, the method fails because the LegacyOrganization is
   * missing its mandatory key.
   */
  @Test
  public void testEncryptRegistrationFailsOnOrganisationMissingKey() throws IOException {
    // mock returning registration.xml file, with organization missing key which is NotNull field
    File registrationXML = copyToTempDir("config/registration_invalid.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V1)).thenReturn(registrationXML);

    assertThrows(ConversionException.class, () -> registrationManager.encryptRegistration());
  }

  /**
   * Try deleting the organisation - it will throw a DeletionNotAllowedException since there is a resource with
   * registered DOI associated to it.
   */
  @Test
  public void testDeleteOrganizationAssociatedToResourceDoi() {
    try {
      registrationManager.delete(RESOURCE2_ORGANISATION_KEY, Collections.emptyList());
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.RESOURCE_DOI_REGISTERED_WITH_ORGANISATION, e.getReason());
    }
  }

  /**
   * The hosting organisation is the associated organisation's object, so a change on either (e.g. its token) is seen
   * on both, also after the associated organisation is replaced by a saved copy (#3352).
   */
  @Test
  public void hostingOrganisationIsTheAssociatedOne() throws Exception {
    Organisation associated = registrationManager.get(HOSTING_ORGANISATION_KEY);
    assertSame(associated, registrationManager.getHostingOrganisation());

    registrationManager.getHostingOrganisation().setPassword("changed on Registration");
    assertEquals("changed on Registration", registrationManager.get(HOSTING_ORGANISATION_KEY).getPassword());

    Organisation saved = new Organisation(associated);
    saved.setPassword("changed on Organizations");
    registrationManager.addAssociatedOrganisation(saved);
    assertSame(saved, registrationManager.getHostingOrganisation());
    assertEquals("changed on Organizations", registrationManager.getHostingOrganisation().getPassword());

    // saved twice (registration + associated organisations) and read back as one object
    registrationManager.save();
    RegistrationManager reloaded = new RegistrationManagerImpl(mock(AppConfig.class), mockDataDir, registryManager,
      mock(PasswordEncrypter.class));
    reloaded.load();
    assertNotNull(reloaded.getHostingOrganisation());
    assertSame(reloaded.get(HOSTING_ORGANISATION_KEY), reloaded.getHostingOrganisation());
  }

  /**
   * Two stored copies with different tokens: the registration's token is kept when the Registry accepts it.
   */
  @Test
  public void differentHostingTokenAcceptedByRegistryIsKept() throws Exception {
    ExtendedResponse response = response(200);
    when(httpClient.get(anyString(), any(UsernamePasswordCredentials.class))).thenReturn(response);

    RegistrationManager manager = loadWithHostingToken("hostingToken");
    assertSame(manager.get(HOSTING_ORGANISATION_KEY), manager.getHostingOrganisation());
    assertEquals("hostingToken", manager.getHostingOrganisation().getPassword());
  }

  /**
   * Two stored copies with different tokens: the associated organisation's token is kept when the Registry refuses
   * the registration's one and accepts the associated one.
   */
  @Test
  public void differentHostingTokenRefusedByRegistryIsDropped() throws Exception {
    when(httpClient.get(anyString(), any(UsernamePasswordCredentials.class))).thenAnswer(invocation -> {
      UsernamePasswordCredentials credentials = invocation.getArgument(1);
      return response(ASSOCIATED_TOKEN.equals(credentials.getPassword()) ? 200 : 401);
    });

    RegistrationManager manager = loadWithHostingToken("hostingToken");
    assertSame(manager.get(HOSTING_ORGANISATION_KEY), manager.getHostingOrganisation());
    assertEquals(ASSOCIATED_TOKEN, manager.getHostingOrganisation().getPassword());
  }

  /**
   * Two stored copies with different tokens and the Registry accepts neither (e.g. it can't be reached): both copies
   * are kept as they are.
   */
  @Test
  public void differentHostingTokensNeitherAcceptedAreBothKept() throws Exception {
    ExtendedResponse response = response(503);
    when(httpClient.get(anyString(), any(UsernamePasswordCredentials.class))).thenReturn(response);

    RegistrationManager manager = loadWithHostingToken("hostingToken");
    assertEquals("hostingToken", manager.getHostingOrganisation().getPassword());
    assertEquals(ASSOCIATED_TOKEN, manager.get(HOSTING_ORGANISATION_KEY).getPassword());
  }

  private static ExtendedResponse response(int status) {
    ExtendedResponse response = new ExtendedResponse(mock(HttpResponse.class));
    when(response.getResponse().getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, status, ""));
    return response;
  }

  /**
   * Loads the registration file with the hosting organisation's stored token changed.
   */
  private RegistrationManager loadWithHostingToken(String token) throws Exception {
    // the original file: setup's load saved the copy with the mocked encrypter, which drops the tokens
    String xml = Files.readString(FileUtils.getClasspathFile("config/registration2.xml").toPath(),
      StandardCharsets.UTF_8);
    int start = xml.indexOf("<hostingOrganisation>");
    int end = xml.indexOf("</hostingOrganisation>");
    String host = xml.substring(start, end)
      .replaceFirst("<password>[^<]*</password>", "<password>" + token + "</password>");
    Files.writeString(registrationXml2.toPath(), xml.substring(0, start) + host + xml.substring(end),
      StandardCharsets.UTF_8);

    // stored tokens read as written
    PBEEncrypt cipher = mock(PBEEncrypt.class);
    when(cipher.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    when(cipher.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    RegistrationManager manager = new RegistrationManagerImpl(mock(AppConfig.class), mockDataDir, registryManager,
      new PasswordEncrypter(cipher));
    manager.load();
    return manager;
  }

  /**
   * Try adding an organisation whose DOI agency account is set to primary, when an existing organisation
   * DOI agency account has been selected as primary. Only one agency account can be activated at once.
   */
  @Test
  public void testAddAssociatedOrganisationPrimaryAgencyAccountAlreadyExists() throws AlreadyExistingException {
    Organisation org = new Organisation();
    org.setName("Oregon University");
    org.setKey(UUID.randomUUID().toString());
    org.setAgencyAccountPrimary(true);
    assertThrows(InvalidConfigException.class, () -> registrationManager.addAssociatedOrganisation(org));
  }
}
