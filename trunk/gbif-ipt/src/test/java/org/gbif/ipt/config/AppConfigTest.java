/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************/

package org.gbif.ipt.config;


import java.math.BigDecimal;
import java.net.URISyntaxException;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(InjectingTestClassRunner.class)
public class AppConfigTest {

  @Inject
  AppConfig cfg;
  @Inject
  DataDir dd;

  @Test
  public void testTestConfig() {
    System.out.println(dd.configFile("").getAbsolutePath());
  }

  @Test
  public void testGetResourceUriFromFullyQualifiedName() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://ipt.gbif.org/resource?r=ants", cfg.getResourceUrl("ants"));

  }

  @Test
  public void testGetResourceUriFromIPAddress() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants", cfg.getResourceUrl("ants"));
  }

  @Test
  public void testGetResourceUriFromLocalhost() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?r=ants", cfg.getResourceUri("ants").toString());
    assertEquals("http://localhost:8080/resource?r=ants", cfg.getResourceUrl("ants"));
  }

  @Test
  public void testGetResourceVersionUriFromFullyQualifiedName() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceVersionUriFromIPAddress() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceVersionUriFromLocalhost() throws URISyntaxException {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?r=ants&v=1.0",
      cfg.getResourceVersionUri("ants", new BigDecimal("1.0")).toString());
  }

  @Test
  public void testGetResourceEmlUrl() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceEmlUrlFromIPAddress() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceEmlUrlFromLocalhost() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/eml.do?r=ants", cfg.getResourceEmlUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrl() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrlFromIPAddress() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceArchiveUrlFromLocalhost() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/archive.do?r=ants", cfg.getResourceArchiveUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrl() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrlFromIPAddress() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLogoUrlFromLocalhost() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/logo.do?r=ants", cfg.getResourceLogoUrl("ants"));
  }

  @Test
  public void testGetResourceLink() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://ipt.gbif.org");
    assertEquals("http://ipt.gbif.org/resource?id=ants", cfg.getResourceGuid("ants"));
  }

  @Test
  public void testGetResourceLinkFromIPAddress() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://192.168.0.84:8080/ipt");
    assertEquals("http://192.168.0.84:8080/ipt/resource?id=ants", cfg.getResourceGuid("ants"));
  }

  @Test
  public void testGetResourceLinkFromLocalhost() throws Exception {
    cfg.setProperty("ipt.baseURL", "http://localhost:8080");
    assertEquals("http://localhost:8080/resource?id=ants", cfg.getResourceGuid("ants"));
  }
}
