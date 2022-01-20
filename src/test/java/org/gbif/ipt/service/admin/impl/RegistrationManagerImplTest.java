/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.converters.ConversionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  private RegistrationManager registrationManager;
  private DataDir mockDataDir;

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

    // mock instance of ResourceManager: returns list of Resource that has one associated to Academy of Natural Sciences
    ResourceManager mockResourceManager = mock(ResourceManager.class);

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
    List<Resource> resourcesList = new ArrayList<>();
    resourcesList.add(r1);
    resourcesList.add(r2);
    when(mockResourceManager.list()).thenReturn(resourcesList);

    // mock returning registration.xml file
    File registrationXML = FileUtils.getClasspathFile("config/registration.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V1)).thenReturn(registrationXML);

    // mock returning registration2.xml file
    File registrationXML2 = FileUtils.getClasspathFile("config/registration2.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V2)).thenReturn(registrationXML2);

    // mock returning list of registered Organisation with local test resource
    mockHttpClient = mock(HttpClient.class);
    mockResponse = mock(HttpResponse.class);
    extResponse = new ExtendedResponse(mockResponse);
    extResponse.setContent(
      IOUtils.toString(
          Objects.requireNonNull(RegistrationManagerImplTest.class.getResourceAsStream("/responses/organisation.json")),
          StandardCharsets.UTF_8));
    when(mockHttpClient.get(anyString())).thenReturn(extResponse);

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpClient, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mock(RegistrationManager.class), mock(ResourceManager.class));

    // make sure the list of organisations is fully populated
    assertNotNull(mockRegistryManager.getRegisteredOrganisation(RESOURCE1_ORGANISATION_KEY));

    // create instance of manager
    registrationManager =
      new RegistrationManagerImpl(mockAppConfig, mockDataDir, mockResourceManager, mockRegistryManager,
        mock(PasswordEncrypter.class));

    // load associatedOrganisations, hostingOrganisation, etc
    registrationManager.load();
  }

  @Test
  public void testDeleteOrganizationAssociatedToResource() {
    // try deleting the Academy of Natural Sciences - it will throw a DeletionNotAllowedException since there is a
    // resource associated to it
    try {
      registrationManager.delete(RESOURCE1_ORGANISATION_KEY);
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
   * Test migrating former registration configuration (registration.xml) to encrypted registration configuration
   * (registration2.xml) but on converting a LegacyOrganization, the method fails because the LegacyOrganization is
   * missing its mandatory key.
   */
  @Test
  @Disabled("floating behaviour")
  public void testEncryptRegistrationFailsOnOrganisationMissingKey() {
    // mock returning registration.xml file, with organization missing name which is NotNull field
    File registrationXML = FileUtils.getClasspathFile("config/registration_invalid.xml");
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
      registrationManager.delete(RESOURCE2_ORGANISATION_KEY);
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.RESOURCE_DOI_REGISTERED_WITH_ORGANISATION, e.getReason());
    }
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
