package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationManagerImplTest extends IptMockBaseTest {

  // Key of Academy of Natural Sciences - Organisation IPT is associated to
  private static final String HOSTING_ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
  // Key of Academy of Natural Sciences - Organisation Resource is associated to
  private static final String RESOURCE_ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

  private RegistrationManager registrationManager;

  @Before
  public void setup() throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock instances
    AppConfig mockAppConfig = mock(AppConfig.class);
    DataDir mockDataDir = mock(DataDir.class);
    SAXParserFactory mockSAXParserFactory = mock(SAXParserFactory.class);
    ConfigWarnings mockConfigWarnings = mock(ConfigWarnings.class);
    SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
    HttpUtil mockHttpUtil;
    HttpUtil.Response mockResponse;

    // mock instance of ResourceManager: returns list of Resource that has one associated to Academy of Natural Sciences
    ResourceManager mockResourceManager = mock(ResourceManager.class);

    // create Resource
    Resource r1 = new Resource();
    r1.setShortname("Test Resource");
    Organisation r1Org = new Organisation();
    r1Org.setName("Old Name Academy of Natural Sciences");
    r1Org.setKey(RESOURCE_ORGANISATION_KEY);
    r1.setStatus(PublicationStatus.REGISTERED);
    r1.setOrganisation(r1Org);

    // mock list() to return list with the mocked Resource - notable its organisation name is the old version
    List<Resource> resourcesList = new ArrayList<Resource>();
    resourcesList.add(r1);
    when(mockResourceManager.list()).thenReturn(resourcesList);

    // mock returning registration.xml file
    File registrationXML = FileUtils.getClasspathFile("config/registration.xml");
    when(mockDataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE)).thenReturn(registrationXML);

    // mock returning list of registered Organisation with local test resource
    mockHttpUtil = mock(HttpUtil.class);
    mockResponse = mock(HttpUtil.Response.class);
    mockResponse.content = IOUtils
      .toString(RegistrationManagerImplTest.class.getResourceAsStream("/responses/organisation.json"), "UTF-8");
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager mockRegistryManager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mock(RegistrationManager.class));

    // make sure the list of organisations is fully populated
    assertNotNull(mockRegistryManager.getRegisteredOrganisation(RESOURCE_ORGANISATION_KEY));

    // create instance of manager
    registrationManager = new RegistrationManagerImpl(mockAppConfig, mockDataDir, mockResourceManager,
      mockRegistryManager);

    // load associatedOrganisations, hostingOrganisation, etc
    registrationManager.load();
  }

  @Test
  public void testDeleteOrganizationAssociatedToResource() {
    // try deleting the Academy of Natural Sciences - it will throw a DeletionNotAllowedException since there is a
    // resource associated to it
    try {
      registrationManager.delete(RESOURCE_ORGANISATION_KEY);
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
    Organisation associated = registrationManager.get(RESOURCE_ORGANISATION_KEY);
    assertEquals("New Name Academy of Natural Sciences", associated.getName());
    assertEquals(RESOURCE_ORGANISATION_KEY, associated.getKey().toString());
  }
}
