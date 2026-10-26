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
package org.gbif.ipt.utils;

import org.gbif.api.vocabulary.License;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.col.Agent;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetadataUtilsTest {

  /**
   * License URLs are compared as GBIF parses them: with or without legalcode, http or https, ODC-PDDL and ODC-BY as
   * CC0 and CC BY. Other licenses, URLs that are only part of one, and no URL aren't supported.
   */
  @Test
  public void testGbifLicenseOfUrl() {
    for (String url : List.of(
        "http://creativecommons.org/publicdomain/zero/1.0/legalcode",
        "https://creativecommons.org/publicdomain/zero/1.0/",
        "http://creativecommons.org/licenses/by/4.0/legalcode",
        "https://creativecommons.org/licenses/by/4.0",
        "https://creativecommons.org/licenses/by-nc/4.0/legalcode",
        "http://www.opendatacommons.org/licenses/pddl/1.0/",
        "http://www.opendatacommons.org/licenses/by/1.0/")) {
      assertNotNull(MetadataUtils.gbifLicenseOfUrl(url), url);
    }
    for (String url : Arrays.asList(
        "http://creativecommons.org/licenses/by-sa/4.0/legalcode",
        "http://creativecommons.org/licenses/by/3.0/",
        "http://creativecommons.org/licenses/by",
        "http://creativecommons.org",
        "not a url",
        "",
        null)) {
      assertNull(MetadataUtils.gbifLicenseOfUrl(url), url);
    }
  }

  /**
   * A resource's license is recognised as the basic metadata page does: from its URL or, without one, from the name of
   * a license the IPT offers, also old-fashioned with the version outside the parenthesis.
   */
  @Test
  public void testGbifLicense() {
    assertEquals(License.CC_BY_4_0, MetadataUtils.gbifLicense(eml(
        "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">Creative Commons Attribution (CC-BY 4.0) License</a>.")));
    assertEquals(License.CC0_1_0, MetadataUtils.gbifLicense(eml(
        "To the extent possible under law, the publisher has waived all rights to these data and has dedicated them to the Public Domain (CC0 1.0).")));
    assertEquals(License.CC_BY_NC_4_0, MetadataUtils.gbifLicense(eml(
        "This work is licensed under a Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License.")));

    // other links than the license's, a written out URL, a Creative Commons deed
    assertEquals(License.CC_BY_4_0, MetadataUtils.gbifLicense(eml(
        "See <a href=\"https://museum.org/contact\">us</a>. This work is licensed under a <a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">Creative Commons Attribution (CC-BY 4.0) License</a>.")));
    assertEquals(License.CC0_1_0, MetadataUtils.gbifLicense(eml(
        "Public Domain (CC0 1.0). Questions: <a href=\"https://museum.org/contact\">contact us</a>.")));
    assertEquals(License.CC_BY_4_0, MetadataUtils.gbifLicense(eml(
        "Licensed under CC BY 4.0, see https://creativecommons.org/licenses/by/4.0/.")));
    assertEquals(License.CC_BY_NC_4_0, MetadataUtils.gbifLicense(eml(
        "<a href=\"https://creativecommons.org/licenses/by-nc/4.0/deed.en\">CC BY-NC 4.0</a>")));

    // the URL is the license, whatever the text names
    assertNull(MetadataUtils.gbifLicense(eml(
        "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by-sa/4.0/legalcode\">Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License</a>.")));
    assertNull(MetadataUtils.gbifLicense(eml("All rights reserved.")));
    assertNull(MetadataUtils.gbifLicense(eml("")));
    assertNull(MetadataUtils.gbifLicense(null));
  }

  /**
   * A license is labelled by its title, else its URL, else the start of the text.
   */
  @Test
  public void testLicenseLabel() {
    assertEquals("Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License", MetadataUtils.licenseLabel(eml(
        "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by-sa/4.0/legalcode\">Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License</a>.")));
    // not the title of another link
    assertEquals("http://creativecommons.org/licenses/by-sa/4.0/legalcode", MetadataUtils.licenseLabel(eml(
        "See <a href=\"https://museum.org/contact\">us</a>, licensed under <a href=\"http://creativecommons.org/licenses/by-sa/4.0/legalcode\">CC BY-SA</a>.")));
    assertEquals("All rights reserved.", MetadataUtils.licenseLabel(eml("All  rights\n reserved.")));
    assertEquals(100, MetadataUtils.licenseLabel(eml("x".repeat(300))).length());
  }

  private static Eml eml(String intellectualRights) {
    Eml eml = new Eml();
    eml.setIntellectualRights(intellectualRights);
    return eml;
  }

  /**
   * The data package "created" timestamp ends with "Z", so it must be in UTC regardless of the server time zone.
   */
  @Test
  public void testFormatDataPackageCreatedIsUtcRegardlessOfDefaultTimeZone() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("Europe/Copenhagen"));
      assertEquals("2026-01-15T10:00:00Z",
          MetadataUtils.formatDataPackageCreated(Instant.parse("2026-01-15T10:00:00.123Z")));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }

  @Test
  public void testFormatCamtrapTemporalDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(2026, Calendar.JANUARY, 15, 23, 59, 59);

    assertEquals("2026-01-15", MetadataUtils.formatCamtrapTemporalDate(calendar.getTime()));
  }

  @Test
  public void testFormatCamtrapTemporalDateOfSqlDate() {
    assertEquals("2026-01-15", MetadataUtils.formatCamtrapTemporalDate(java.sql.Date.valueOf("2026-01-15")));
  }

  /**
   * Formatted by requests and publications at the same time; a shared SimpleDateFormat mixes up dates.
   */
  @Test
  public void testFormatCamtrapTemporalDateConcurrently() throws Exception {
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Future<List<String>>> results = new ArrayList<>();
      for (int t = 0; t < threads; t++) {
        int year = 2000 + t;
        results.add(pool.submit(() -> {
          List<String> wrong = new ArrayList<>();
          Calendar calendar = Calendar.getInstance();
          for (int day = 1; day <= 365; day++) {
            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_YEAR, day);
            String expected = String.format("%d-%02d-%02d", year, calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
            String formatted = MetadataUtils.formatCamtrapTemporalDate(calendar.getTime());
            if (!expected.equals(formatted)) {
              wrong.add(expected + " formatted as " + formatted);
            }
          }
          return wrong;
        }));
      }
      for (Future<List<String>> result : results) {
        assertEquals(List.of(), result.get(30, TimeUnit.SECONDS));
      }
    } finally {
      pool.shutdownNow();
    }
  }

  /**
   * ColDPs write licenses in many ways, e.g. Plazi's CC0 and ChecklistBank's cc0.
   */
  @Test
  public void testColDpLicenseToGbifCode() {
    for (String license : List.of("CC0", "cc0", "CC0-1.0", "CC0 1.0", "cc0_1.0")) {
      assertEquals("CC0-1.0", MetadataUtils.colDpLicenseToGbifCode(license), license);
    }
    for (String license : List.of("CC BY", "CC-BY", "cc by 4.0", "CC-BY-4.0")) {
      assertEquals("CC-BY-4.0", MetadataUtils.colDpLicenseToGbifCode(license), license);
    }
    for (String license : List.of("CC BY NC", "CC BY-NC", "cc by nc 4.0", "CC-BY-NC-4.0")) {
      assertEquals("CC-BY-NC-4.0", MetadataUtils.colDpLicenseToGbifCode(license), license);
    }
    for (String license : Arrays.asList("CC BY 3.0", "CC BY-SA", "CC-BY-ND-4.0", "other", "", null)) {
      assertNull(MetadataUtils.colDpLicenseToGbifCode(license), license);
    }
  }

  /**
   * A descriptor's license needs a name or path: it's the SPDX name and URL of a known license, none for another.
   */
  @Test
  public void testColDpLicensesForDescriptor() {
    List<FrictionlessLicense> licenses = MetadataUtils.colDpLicensesForDescriptor("CC BY 4.0");
    assertEquals(1, licenses.size());
    assertEquals("CC-BY-4.0", licenses.get(0).getName());
    assertEquals("https://creativecommons.org/licenses/by/4.0/", licenses.get(0).getPath());

    assertEquals("CC0-1.0", MetadataUtils.colDpLicensesForDescriptor("cc0").get(0).getName());
    assertTrue(MetadataUtils.colDpLicensesForDescriptor("other").isEmpty());
    assertTrue(MetadataUtils.colDpLicensesForDescriptor(null).isEmpty());
  }

  /**
   * A descriptor's contributors have a title and a role, they're the ColDP's agents with their name.
   */
  @Test
  public void testColDpContributorsForDescriptor() {
    ColMetadata metadata = new ColMetadata();
    metadata.setCreator(List.of(agent("Kwang-Tsao", "Shao", "Academia Sinica", "0000-0001-9650-3136"),
        agent(null, null, null, null)));
    metadata.setEditor(List.of(agent(null, "Lee", null, null)));
    metadata.setPublisher(agent(null, null, "TaiBIF", null));
    Agent contact = agent("Sz-Yi", "Tsai", null, null);
    contact.setEmail("contact@example.org");
    metadata.setContact(contact);

    List<FrictionlessContributor> contributors = MetadataUtils.colDpContributorsForDescriptor(metadata);

    assertEquals(List.of("Kwang-Tsao Shao:author", "Lee:contributor", "TaiBIF:publisher", "Sz-Yi Tsai:maintainer"),
        contributors.stream().map(c -> c.getTitle() + ":" + c.getRole()).toList());
    assertEquals("Academia Sinica", contributors.get(0).getOrganization());
    assertEquals("https://orcid.org/0000-0001-9650-3136", contributors.get(0).getPath());
    // an organisation is the title, not its organization
    assertNull(contributors.get(2).getOrganization());
    assertEquals("contact@example.org", contributors.get(3).getEmail());
  }

  /**
   * A contributor's path is its ORCID, written as an ID or URL, or else its URL, but only a full one: the descriptor
   * can't be created with another.
   */
  @Test
  public void testColDpContributorPath() {
    ColMetadata metadata = new ColMetadata();
    Agent orcidUrl = agent("A", "B", null, "https://orcid.org/0000-0001-9650-3136");
    Agent orcidWithoutScheme = agent("G", "H", null, "orcid.org/0000-0001-9650-3136");
    Agent url = agent("C", "D", null, null);
    url.setUrl(URI.create("https://example.org"));
    Agent partialUrl = agent("E", "F", null, null);
    partialUrl.setUrl(URI.create("www.example.org"));
    metadata.setCreator(List.of(orcidUrl, url, partialUrl, orcidWithoutScheme));

    List<FrictionlessContributor> contributors = MetadataUtils.colDpContributorsForDescriptor(metadata);

    assertEquals("https://orcid.org/0000-0001-9650-3136", contributors.get(0).getPath());
    assertEquals("https://example.org", contributors.get(1).getPath());
    assertNull(contributors.get(2).getPath());
    assertEquals("https://orcid.org/0000-0001-9650-3136", contributors.get(3).getPath());
  }

  private static Agent agent(String given, String family, String organisation, String orcid) {
    Agent agent = new Agent();
    agent.setGiven(given);
    agent.setFamily(family);
    agent.setOrganisation(organisation);
    agent.setOrcid(orcid);
    return agent;
  }
}
