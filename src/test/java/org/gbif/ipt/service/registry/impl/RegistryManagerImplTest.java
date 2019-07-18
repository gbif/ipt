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

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.gbif.utils.HttpUtil.Response;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistryManagerImplTest extends IptMockBaseTest {

  // logging
  private static final Logger LOG = LogManager.getLogger(RegistryManagerImplTest.class);

  private AppConfig mockAppConfig;
  private DataDir mockDataDir;
  private SAXParserFactory mockSAXParserFactory;
  private ConfigWarnings mockConfigWarnings;
  private SimpleTextProvider mockSimpleTextProvider;
  private RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private ResourceManager mockResourceManager = mock(ResourceManager.class);
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    List<Extension> extensions = manager.getExtensions();
    // a total of 22 Extensions are expected
    assertEquals(22, extensions.size());

    // a total of 1 Extensions with rowType Occurrence are expected
    List<Extension> occurrenceCoreExtensions = Lists.newArrayList();
    for (Extension x: extensions) {
      if (x.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        occurrenceCoreExtensions.add(x);
      }
    }
    assertEquals(1, occurrenceCoreExtensions.size());
  }

  @Test
  public void testGetSandboxExtensions() throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    // mock response from Registry with local test resource
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/extensions_sandbox.json"), "UTF-8");
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    List<Extension> extensions = manager.getExtensions();
    // a total of 52 Extensions are expected
    assertEquals(52, extensions.size());

    // a total of 3 Extensions with rowType Occurrence are expected
    List<Extension> occurrenceCoreExtensions = Lists.newArrayList();
    for (Extension x: extensions) {
      if (x.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        occurrenceCoreExtensions.add(x);
      }
    }
    assertEquals(3, occurrenceCoreExtensions.size());
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    // getExtensions() throws a RegistryException of type PROXY
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      assertEquals(RegistryException.Type.PROXY, e.getType());
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    // getExtensions() throws a RegistryException of type BAD_RESPONSE
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      assertEquals(RegistryException.Type.BAD_RESPONSE, e.getType());
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    // getExtensions() throws a RegistryException of type BAD_REQUEST
    try {
      manager.getExtensions();
    } catch (RegistryException e) {
      LOG.info(e.getMessage());
      assertEquals(RegistryException.Type.BAD_REQUEST, e.getType());
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    List<Vocabulary> vocabularies = manager.getVocabularies();
    assertEquals(52, vocabularies.size());
  }

  @Test
  public void testGetVocabulariesSandbox() throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    // mock response from Registry with local test resource
    mockResponse.content =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/thesauri_sandbox.json"), "UTF-8");

    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    List<Vocabulary> vocabularies = manager.getVocabularies();
    assertEquals(65, vocabularies.size());

    // a total of 2 Vocabularies for QuantityType
    List<Vocabulary> quantityTypeVocabularies = Lists.newArrayList();
    for (Vocabulary v: vocabularies) {
      if (v.getUriString().contains("quantityType")) {
        quantityTypeVocabularies.add(v);
      }
    }
    assertEquals(2, quantityTypeVocabularies.size());
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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

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
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

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

  @Test
  public void testGetLastPublishedVersionExistingDoi() throws ParserConfigurationException, SAXException {
    // construct public resource having one public published version 5.0
    Resource p = new Resource();
    p.setShortname("res1");
    p.setTitle("Danish Lepidoptera");
    Date lastPublished = new Date();
    p.setModified(lastPublished);
    BigDecimal version = new BigDecimal("5.0");
    p.setEmlVersion(version);
    p.setStatus(PublicationStatus.PUBLIC);
    VersionHistory vh = new VersionHistory(version, lastPublished, PublicationStatus.PUBLIC);
    p.addVersionHistory(vh);

    AppConfig appConfig = mock(AppConfig.class);
    DataDir dataDir = mock(DataDir.class);
    // retrieve eml.xml file for version 5.0 with DOI citation identifier
    File eml = FileUtils.getClasspathFile("resources/res1/eml-5.0.xml");
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(eml);
    when(appConfig.getDataDir()).thenReturn(dataDir);

    // create instance of RegistryManager
    RegistryManagerImpl manager =
      new RegistryManagerImpl(appConfig, dataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    DOI expectedDOI = new DOI("http://doi.org/10.5072/fk22zu2ds");
    DOI existingDOI = manager.getLastPublishedVersionExistingDoi(p);
    assertTrue(expectedDOI.equals(existingDOI));

    // retrieve eml.xml file for version 5.0 WITHOUT citation identifier
    eml = FileUtils.getClasspathFile("resources/res1/eml.xml");
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(eml);
    when(appConfig.getDataDir()).thenReturn(dataDir);
    existingDOI = manager.getLastPublishedVersionExistingDoi(p);
    assertNull(existingDOI);

    // retrieve eml.xml file for version 5.0 with citation identifier but not DOI
    eml = FileUtils.getClasspathFile("data/eml.xml");
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenReturn(eml);
    when(appConfig.getDataDir()).thenReturn(dataDir);
    existingDOI = manager.getLastPublishedVersionExistingDoi(p);
    assertNull(existingDOI);
  }

  @Test
  public void testGetRegistryExceptionType() throws ParserConfigurationException, SAXException {
    // create instance of RegistryManager
    RegistryManagerImpl manager =
      new RegistryManagerImpl(mockAppConfig, dataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager, mockResourceManager);

    assertEquals(RegistryException.Type.BAD_REQUEST, manager.getRegistryExceptionType(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode()));
    assertEquals(RegistryException.Type.NOT_AUTHORISED, manager.getRegistryExceptionType(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode()));
    assertEquals(RegistryException.Type.BAD_RESPONSE,
      manager.getRegistryExceptionType(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertEquals(RegistryException.Type.BAD_RESPONSE,
      manager.getRegistryExceptionType(javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY.getStatusCode()));

    try {
      manager.getRegistryExceptionType(101);
    } catch (IllegalArgumentException e) {
      // asserts that only error codes above 300 expected
    }
  }
}
