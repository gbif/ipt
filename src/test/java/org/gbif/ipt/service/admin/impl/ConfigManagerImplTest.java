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

import org.gbif.ipt.config.AppConfig;
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

import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
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
  private ConfigManagerImpl getConfigManager() {
    DataDir mockedDataDir = MockDataDir.buildMock();
    ResourceManager mockedResourceManager = MockResourceManager.buildMock();
    ExtensionManager mockedExtensionManager = mock(ExtensionManager.class);
    VocabulariesManager mockedVocabularies = mock(VocabulariesManager.class);
    RegistrationManager mockedRegistrationManager = MockRegistrationManager.buildMock();
    UserAccountManager mockedUserManager = MockUserAccountManager.buildMock();
    ConfigWarnings warnings = new ConfigWarnings();
    PublishingMonitor mockPublishingMonitor = mock(PublishingMonitor.class);

    client = HttpUtil.newMultithreadedClient(1000, 1, 1);
    appConfig = new AppConfig(mockedDataDir);

    return new ConfigManagerImpl(mockedDataDir, appConfig, mockedUserManager, mockedResourceManager,
        mockedExtensionManager, mockedVocabularies, mockedRegistrationManager, warnings, client, mockPublishingMonitor);
  }

  /**
   * Test that the method setProxy of the ConfigManager throws an InvalidConfigException if the proxy given by the user
   * doesn't exist or the client can't connect with it.
   */
  @Test
  public void testSetProxyThrowsInvalidConfigExceptionWhenProxyIsWrong() {
    // Creating configManager
    ConfigManager configManager = getConfigManager();

    // Saving a bad proxy
    String newProxy = "proxy.example:8080";
    try {
      configManager.setProxy(newProxy);
      fail("Proxy is wrong, so InvalidConfigException is expected");
    } catch (InvalidConfigException e) {
      assertNull(appConfig.getProperty(AppConfig.PROXY));
    }
  }

  /**
   * Test that the method setBaseUrl of the ConfigManager throws an InvalidConfigException if the baseURL given by the
   * user doesn't exist or the client can't connect with it.
   */
  @Test
  public void testSetBaseURL() throws Exception {
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
      fail("Base URL is wrong (non-existent), so InvalidConfigException is expected");
    } catch (InvalidConfigException ignored) {
    }

    // try to save an existent baseURL without an IPT installed.
    try {
      URL baseURL2 = new URL("https://www.gbif.org");
      configManager.setBaseUrl(baseURL2);
      // the validation should never get here.
      fail("Base URL is wrong (no IPT installed there), so InvalidConfigException is expected");
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
  public void testSetProxy() {
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
  public void testValidateBaseURLBadHostName() throws Exception {
    // Creating configManager
    ConfigManagerImpl configManager = getConfigManager();

    // Base URL invalid, since host name has an underscore "_", which violates RFC 1123 and RFC 952
    URL testURL = new URL("https://testipt1_vh.gbif.org:8080/ipt");
    assertFalse(configManager.isValidBaseUrl(testURL, null));
  }
}
