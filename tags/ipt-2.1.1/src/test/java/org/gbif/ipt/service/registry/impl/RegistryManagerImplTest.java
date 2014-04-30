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
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptMockBaseTest;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistryManagerImplTest extends IptMockBaseTest {

  // logging
  private static final Logger LOG = Logger.getLogger(RegistryManagerImplTest.class);

  private static final String ORG_UUID = "62922b92-69d1-4c4b-831c-b23d5412a124";
  private static final String ORG_PASSWORD = "password";
  private static final String ORG_NAME = "AAA4Organisation";

  private RegistryManager manager;

  private AppConfig mockAppConfig;
  private DataDir mockDataDir;
  private SAXParserFactory mockSAXParserFactory;
  private ConfigWarnings mockConfigWarnings;
  private SimpleTextProvider mockSimpleTextProvider;
  private RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private HttpUtil mockHttpUtil;
  private HttpUtil.Response mockResponse;

  private Organisation organisation;
  private Ipt ipt;

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

    organisation = new Organisation();
    organisation.setKey(ORG_UUID);
    organisation.setName(ORG_NAME);
    organisation.setPassword(ORG_PASSWORD);

    ipt = new Ipt();

    // manager that issues real http requests
    manager =
      new RegistryManagerImpl(cfg, dataDir, new HttpUtil(buildHttpClient()), buildSaxFactory(), mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);
  }

  @Test
  public void testBuild() {
    try {
      // test organisation "AAA4Organisation"
      assertTrue(manager.validateOrganisation(ORG_UUID, ORG_PASSWORD));
      assertFalse(manager.validateOrganisation(ORG_UUID, ORG_NAME));
      assertFalse(manager.validateOrganisation("INVALID92-69d1-4c4b-831c-b23d5412a124", ORG_PASSWORD));

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testWriteReadResource() {
    try {
      ipt.setCreated(new Date());
      ipt.setDescription("a unit test mock IPT");
      ipt.setPrimaryContactName("Ms. IPT Admin");
      ipt.setPrimaryContactEmail("gbif@mailinator.com");
      ipt.setPrimaryContactType("technical");
      ipt.setLanguage("en");
      ipt.setName("Mock IPT");

      // register IPT
      ipt.setKey(manager.registerIPT(ipt, organisation));

      // register resource
      Resource res = new Resource();
      User user = new User();
      user.setFirstname("Mock Name");
      user.setEmail("mocking@themock.org");
      res.setCreator(user);
      res.setShortname("mock");
      res.setTitle("Möck rèşürçe wíŧħ ƒũñňÿ ćĥåřæċŧëŗş");
      res.setCreated(new Date());
      res.setSubtype("occurrence test");
      Eml eml = new Eml();
      eml.setAbstract(
        "An IPT unit test resource that can be deleted. Testing unicode characters like ą ć ę ł ń  ś ź ż (for polish) ť ů ž č ď ě ň ř š (for czech) and other taken from http://www.alanflavell.org.uk/unicode/unidata.html  ᠀᠔ᡎᢥ(mongolian) ⅛  Ⅳ ⅸ ↂ (numbers) ∀ ∰ ⊇ ⋩ (maths) CJK Symbols and Punctuation U+3000 – U+303F (12288–12351) 々 〒 〣 〰 Hiragana U+3040 – U+309F (12352–12447) あ ぐ る ゞ Katakana U+30A0 – U+30FF (12448–12543) ア ヅ ヨ ヾ Bopomofo U+3100 – U+312F (12544–12591) ㄆ ㄓ ㄝ ㄩ Hangul Compatibility Jamo U+3130 – U+318F (12592–12687) ㄱ ㄸ ㅪ ㆍ Kanbun U+3190 – U+319F (12688–12703) ㆐ ㆕ ㆚ ㆟ Bopomofo Extended U+31A0 – U+32BF (12704–12735) ㆠ ㆧ ㆯ ㆷ Katakana Phonetic Extensions U+31F0 – U+31FF (12784–12799) ㇰ ㇵ ㇺ ㇿ Enclosed CJK Letters and Months U+3200 – U+32FF (12800–13055) ㈔ ㈲ ㊧ ㋮ CJK Compatibility U+3300 – U+33FF (13056–13311) ㌃ ㍻ ㎡ ㏵ CJK Unified Ideographs Extension A U+3400 – U+4DB5 (13312–19893) 㐅 㒅 㝬 㿜 Yijing Hexagram Symbols U+4DC0 – U+4DFF (19904–19967) ䷂ ䷫ ䷴ ䷾ CJK Unified Ideographs U+4E00 – U+9FFF (19968–40959) 一 憨 田 龥 Yi Syllables U+A000 – U+A48F (40960–42127) ꀀ ꅴ ꊩ ꒌ Yi Radicals U+A490 – U+A4CF (42128–42191) ꒐ ꒡ ꒰ ꓆ ");
      res.setEml(eml);

      res.setLastPublished(new Date());
      UUID uuid = manager.register(res, organisation, ipt);
      assertNotNull(uuid);

      // get resource and compare
      // TODO: no registry method for this yet
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
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
    String response =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/thesauri.json"), "UTF-8");

    mockResponse.content = response;
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
    String response =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/organisations_resources.json"), "UTF-8");

    mockResponse.content = response;
    when(mockHttpUtil.get(anyString())).thenReturn(mockResponse);

    // create instance of RegistryManager
    RegistryManager manager =
      new RegistryManagerImpl(mockAppConfig, mockDataDir, mockHttpUtil, mockSAXParserFactory, mockConfigWarnings,
        mockSimpleTextProvider, mockRegistrationManager);

    List<Resource> resources = manager.getOrganisationsResources("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    assertEquals(3, resources.size());
  }

  @Test
  public void testGetOrganisation()
    throws IOException, URISyntaxException, SAXException, ParserConfigurationException {
    // mock response from Registry with local test resource
    String response =
      IOUtils.toString(RegistryManagerImplTest.class.getResourceAsStream("/responses/organisation.json"), "UTF-8");

    mockResponse.content = response;
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
