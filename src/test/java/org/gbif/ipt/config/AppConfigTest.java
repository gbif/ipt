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
package org.gbif.ipt.config;

import java.math.BigDecimal;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppConfigTest {

  ServletContext mockServletContext = new MockServletContext();
  DataDir dd = TestBeanProvider.provideDataDir(mockServletContext);
  AppConfig cfg = TestBeanProvider.provideAppConfig(dd);

  @Test
  public void testTestConfig() {
    System.out.println(dd.configFile("").getAbsolutePath());
  }

  @Test
  public void testGetResourceUriFromFullyQualifiedName() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://ipt.gbif.org/resource?r=ants", cfg.getResourceUrl("ants"));

  }

  @Test
  public void testGetResourceUriFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants", cfg.getResourceUrl("ants"));
  }

  @Test
  public void testGetResourceUriFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://localhost:8080/resource?r=ants", cfg.getResourceUrl("ants"));
  }

  @Test
  public void testGetResourceVersionUriFromFullyQualifiedName() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceVersionUriFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceVersionUriFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceEmlUrl() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));

    cfg.setProperty("ipt.baseURL", null);
    assertThrows(RuntimeException.class, () -> cfg.getResourceEmlUrl("ants"));

    cfg.setProperty("ipt.baseURL", "");
    assertThrows(RuntimeException.class, () -> cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceEmlUrlFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceEmlUrlFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrl() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));

    cfg.setProperty("ipt.baseURL", null);
    assertThrows(RuntimeException.class, () -> cfg.getResourceArchiveUrl("ants"));

    cfg.setProperty("ipt.baseURL", "");
    assertThrows(RuntimeException.class, () -> cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrlFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrlFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrl() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrlFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrlFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLink() {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?id=ants", cfg.getResourceGuid("ants"));
  }

  @Test
  public void testGetResourceLinkFromIPAddress() {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?id=ants", cfg.getResourceGuid("ants"));
  }

  @Test
  public void testGetResourceLinkFromLocalhost() {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?id=ants", cfg.getResourceGuid("ants"));
  }

  @Test
  void testGetShortVersion() {
    assertNull(RegExUtils.removePattern(null, AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("", RegExUtils.removePattern("", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3", RegExUtils.removePattern("2.6.3", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3", RegExUtils.removePattern("2.6.3-r6abcbe3", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3-SNAPSHOT",
        RegExUtils.removePattern("2.6.3-SNAPSHOT-r6abcbe3", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3-SNAPSHOT",
        RegExUtils.removePattern("2.6.3-SNAPSHOT-r${buildNumber}", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3-RC1",
        RegExUtils.removePattern("2.6.3-RC1-r6abcbe3", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3-RC1-SNAPSHOT",
        RegExUtils.removePattern("2.6.3-RC1-SNAPSHOT-r6abcbe3", AppConfig.BUILD_NUMBER_REGEX));
    assertEquals("2.6.3-RC1-SNAPSHOT",
        RegExUtils.removePattern("2.6.3-RC1-SNAPSHOT-r${buildNumber}", AppConfig.BUILD_NUMBER_REGEX));
  }

  @Test
  void testGetVersion() {
    assertNull(StringUtils.removeEnd(null, AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("", StringUtils.removeEnd("", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3", StringUtils.removeEnd("2.6.3", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-r6abcbe3",
        StringUtils.removeEnd("2.6.3-r6abcbe3", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-SNAPSHOT-r6abcbe3",
        StringUtils.removeEnd("2.6.3-SNAPSHOT-r6abcbe3", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-SNAPSHOT",
        StringUtils.removeEnd("2.6.3-SNAPSHOT-r${buildNumber}", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-RC1-r6abcbe3",
        StringUtils.removeEnd("2.6.3-RC1-r6abcbe3", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-RC1-SNAPSHOT-r6abcbe3",
        StringUtils.removeEnd("2.6.3-RC1-SNAPSHOT-r6abcbe3", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
    assertEquals("2.6.3-RC1-SNAPSHOT",
        StringUtils.removeEnd("2.6.3-RC1-SNAPSHOT-r${buildNumber}", AppConfig.BUILD_NUMBER_VARIABLE_SUFFIX));
  }
}
