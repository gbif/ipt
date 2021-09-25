/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigManagerImpl;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.PublishingMonitor;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockRegistrationManager;
import org.gbif.ipt.mock.MockResourceManager;
import org.gbif.ipt.mock.MockUserAccountManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.conn.params.ConnRoutePNames;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * This class tests the relevant methods of the ConfigManagerImpl class.
 */
public class ConfigManagerImplTest {

  private HttpClient client;
  private AppConfig appConfig;

  /**
   * @return a new ConfigManager instance.
   */
  private ConfigManagerImpl getConfigManager() throws ParserConfigurationException, SAXException {
    DataDir mockedDataDir = MockDataDir.buildMock();
    ResourceManager mockedResourceManager = MockResourceManager.buildMock();
    ExtensionManager mockedExtensionManager = mock(ExtensionManager.class);
    VocabulariesManager mockedVocabularies = mock(VocabulariesManager.class);
    RegistrationManager mockedRegistrationManager = MockRegistrationManager.buildMock();
    UserAccountManager mockedUserManager = MockUserAccountManager.buildMock();
    ConfigWarnings warnings = new ConfigWarnings();
    PublishingMonitor mockPublishingMonitor = mock(PublishingMonitor.class);

    client = new HttpClient(HttpUtil.newMultithreadedClient(1000, 1, 1));
    appConfig = new AppConfig(mockedDataDir);

    return new ConfigManagerImpl(mockedDataDir, appConfig, mockedUserManager, mockedResourceManager,
        mockedExtensionManager, mockedVocabularies, mockedRegistrationManager, warnings, client, mockPublishingMonitor);
  }

  /**
   * Test that the method setProxy of the ConfigManager throws an InvalidConfigException if the proxy given by the user
   * don't exists or the client can't connect with it.
   */
  // TODO: 20/09/2021 find a way to extract params or something
  @Test(expected = InvalidConfigException.class)
  public void testBadProxy() throws ParserConfigurationException, SAXException {
    // Creating configManager
    ConfigManager configManager = getConfigManager();

    // Saving a bad proxy
    String newProxy = "proxy.example:8080";
    configManager.setProxy(newProxy);
    assertEquals(newProxy, client.getClient().getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY).toString());

    // Saving a bad proxy
    newProxy = "http://proxy.example:8080";
    configManager.setProxy(newProxy);
    assertEquals(newProxy, client.getClient().getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY).toString());
  }

  /**
   * Test that the method setBaseUrl of the ConfigManager throws an InvalidConfigException if the baseURL given by the
   * user doesn't exist or the client can't connect with it.
   *
   * @throws MalformedURLException if the baseURL is malformed.
   */
  @Ignore
  @Test
  public void testSetBaseURL() throws ParserConfigurationException, SAXException, MalformedURLException {
    // Create configManager.
    ConfigManager configManager = getConfigManager();

    // Save good baseURL
    URL baseURL = new URL("https://ipt.gbif.org");
    configManager.setBaseUrl(baseURL);
    assertEquals(baseURL.toString(), appConfig.getProperty(AppConfig.BASEURL));

    // try to save a nonexistent baseURL.
    try {
      URL baseURL2 = new URL("http://192.0.2.0/ipt");
      configManager.setBaseUrl(baseURL2);
      // the validation should never get here.
      fail();
    } catch (InvalidConfigException ignored) {
    }

    // try to save an existent baseURL without an IPT installed.
    try {
      URL baseURL2 = new URL("https://www.gbif.org");
      configManager.setBaseUrl(baseURL2);
      // the validation should never get here.
      fail();
    } catch (InvalidConfigException ignored) {
    }

    // TODO figure out how to test a proxy

    // // With proxy
    // String proxy = "http://proxy4.ciat.cgiar.org:8080";
    // configManager.setProxy(proxy);
    //
    // baseURL = new URL("http://127.0.0.1:7001/ipt");
    // configManager.setBaseUrl(baseURL);
    //
    // assertEquals(baseURL.toString(), appConfig.getProperty(AppConfig.BASEURL));
  }


  /**
   * Test that the method setProxy of the ConfigManager saves the proxy in the application properties if the client can
   * connect with it.
   */
  @Test
  public void testSetProxy() throws ParserConfigurationException, SAXException {
    // Creating configManager
    ConfigManager configManager = getConfigManager();

    // TODO figure out how to test a proxy

    // // Saving proxy first time
    // String newProxy = "http://proxy.ciat.cgiar.org:8080";
    // configManager.setProxy(newProxy);
    // assertEquals(newProxy, appConfig.getProperty(AppConfig.PROXY));
    //
    // // Saving proxy second time
    // newProxy = "http://proxy.ciat.cgiar.org:8080";
    // configManager.setProxy(newProxy);
    // assertEquals(newProxy, appConfig.getProperty(AppConfig.PROXY));

    // Saving null proxy
    configManager.setProxy(null);
    assertEquals("", appConfig.getProperty(AppConfig.PROXY));

    // Saving an empty proxy String
    String newProxy = "";
    configManager.setProxy(newProxy);
    assertEquals(newProxy, appConfig.getProperty(AppConfig.PROXY));
  }

  @Test
  public void testValidateBaseURLBadHostName()
    throws ParserConfigurationException, SAXException, MalformedURLException {
    // Creating configManager
    ConfigManagerImpl configManager = getConfigManager();

    // Base URL invalid, since host name has an underscore "_", which violates RFC 1123 and RFC 952
    URL testURL = new URL("https://testipt1_vh.gbif.org:8080/ipt");
    assertFalse(configManager.validateBaseURL(testURL));
  }

}
