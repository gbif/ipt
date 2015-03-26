/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.service.registry.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.utils.HttpUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistryManagerImplTest extends IptMockBaseTest {

  // logging
  private static final Logger LOG = Logger.getLogger(RegistryManagerImplTest.class);

  private AppConfig mockAppConfig;
  private DataDir mockDataDir;
  private SAXParserFactory mockSAXParserFactory;
  private ConfigWarnings mockConfigWarnings;
  private SimpleTextProvider mockSimpleTextProvider;
  private RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private HttpUtil mockHttpUtil;
  private HttpUtil.Response mockResponse;

  @Before
  public void setup() throws SAXException, ParserConfigurationException {
    mockHttpUtil = mock(HttpUtil.class);
    mockResponse = mock(HttpUtil.Response.class);
    mockAppConfig = mock(AppConfig.class);
    mockDataDir = mock(DataDir.class);
    mockSAXParserFactory = mock(SAXParserFactory.class);
    mockConfigWarnings = mock(ConfigWarnings.class);
    mockSimpleTextProvider = mock(SimpleTextProvider.class);
    mockRegistrationManager = mock(RegistrationManager.class);
  }

  @Test
  public void testGetExtensions() throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    // mock response from Registry with local test resource
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/extensions.json"), "UTF-8");
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    List<Extension> extensions = manager.getExtensions();
    // a total of 14 Extensions are expected
    assertEquals(14, extensions.size());
  }

  @Test
  public void testGetExtensionsThrowsRegistryException()
    throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response from Registry as ClassCastException
    ConnectException connectException = new ConnectException("ConnectException occurred!");
    when(mockHttpUtil.get(anyString())).thenThrow(connectException);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    // getExtensions() throws a RegistryException of type PROXY
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      assertEquals(RegistryException.TYPE.PROXY, e.getType());
    }
  }

  @Test
  public void testGetExtensionsEmptyContentThrowsRegistryException()
    throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response from Registry as empty content
    mockResponse.content = null;
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    // getExtensions() throws a RegistryException of type BAD_RESPONSE
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      assertEquals(RegistryException.TYPE.BAD_RESPONSE, e.getType());
    }
  }

  @Test
  public void testGetExtensionsBadURLThrowsRegistryException()
    throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response HttpUtil as URISyntaxException
    when(mockHttpUtil.get(anyString())).thenThrow(new URISyntaxException("httpgoog.c", "Wrong syntax!"));

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    // getExtensions() throws a RegistryException of type BAD_REQUEST
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      LOG.info(e.getMessage());
      assertEquals(RegistryException.TYPE.BAD_REQUEST, e.getType());
    }
  }

  @Test
  public void testGetVocabularies() throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    // mock response from Registry with local test resource
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/thesauri.json"), "UTF-8");

    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    List<Vocabulary> vocabularies = manager.getVocabularies();
    assertEquals(45, vocabularies.size());
  }

  @Test
  public void testGetOrganisationsResources()
    throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response from Registry with local test resource
    mockResponse.content = IOUtils
      .toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/organisations_resources.json"), "UTF-8");

    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    List<Resource> resources = manager.getOrganisationsResources("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    assertEquals(3, resources.size());
  }

  @Test
  public void testGetOrganisation() throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response from Registry with local test resource
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/organisation.json"), "UTF-8");

    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    Organisation organisation = manager.getRegisteredOrganisation("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    assertNotNull(organisation);

    // individual fields now
    assertEquals("us", organisation.getNodeKey());
    assertEquals("USA", organisation.getNodeName());
    assertEquals("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862", organisation.getKey().toString());
    assertEquals("http://www.acnatsci.org/", organisation.getDescription());
    assertEquals("New Name Academy of Natural Sciences", organisation.getName());
    assertEquals("http://www.acnatsci.org/", organisation.getHomepageURL());
    assertEquals("technical", organisation.getPrimaryContactType());
    assertEquals("Paul J. Morris ", organisation.getPrimaryContactName());
    assertEquals("mole@morris.net", organisation.getPrimaryContactEmail());
    assertEquals("1-215-299-1161", organisation.getPrimaryContactPhone());
  }
}
