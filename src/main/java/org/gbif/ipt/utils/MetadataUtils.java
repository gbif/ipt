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
import org.gbif.common.parsers.LicenseParser;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.Agent;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COLDP_LICENSES_CODES_TO_GBIF;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DWC_DP;

public final class MetadataUtils {

  private static final DateTimeFormatter CAMTRAP_TEMPORAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Logger LOG = LogManager.getLogger(MetadataUtils.class);
  private static final LicenseParser LICENSE_PARSER = LicenseParser.getInstance();
  // as MetadataAction loads them for the basic metadata page
  private static final String LICENSES_PROPFILE_PATH = "/org/gbif/metadata/eml/licenses.properties";
  private static final List<String> IPT_LICENSE_NAMES = iptLicenseNames();
  private static final Pattern URL = Pattern.compile("https?://[^\\s\"'<>)]+");
  // the sites of the licenses GBIF parses
  private static final List<String> LICENSE_HOSTS = List.of("creativecommons.org", "opendatacommons.org");
  // a Creative Commons license and version, e.g. creativecommons.org/licenses/by/4.0
  private static final Pattern CREATIVE_COMMONS_LICENSE =
      Pattern.compile("^(.*creativecommons\\.org/(?:licenses/[a-z-]+|publicdomain/zero)/\\d\\.\\d)(?:/.*)?$");
  // the URLs of the GBIF-supported licenses a ColDP can have, see colDpLicenseToGbifCode
  private static final Map<String, String> COLDP_LICENSE_URLS = Map.of(
      "CC0-1.0", "https://creativecommons.org/publicdomain/zero/1.0/",
      "CC-BY-4.0", "https://creativecommons.org/licenses/by/4.0/",
      "CC-BY-NC-4.0", "https://creativecommons.org/licenses/by-nc/4.0/");

  private MetadataUtils() {
  }

  public static Class<? extends DataPackageMetadata> metadataClassForType(String type) {
    if (CAMTRAP_DP.equals(type)) {
      return CamtrapMetadata.class;
    } else if (COL_DP.equals(type)) {
      return ColMetadata.class;
    } else {
      return FrictionlessMetadata.class;
    }
  }

  /**
   * The GBIF-supported license code (e.g. CC-BY-4.0) of a ColDP license, null if it isn't one of them. ColDPs write
   * licenses in many ways (CC0, CC0-1.0, cc0, CC BY 4.0, CC-BY-NC-4.0), they're compared in upper case without
   * spaces, hyphens and underscores.
   *
   * @param license ColDP license
   * @return GBIF-supported license code, or null
   */
  public static String colDpLicenseToGbifCode(String license) {
    if (license == null) {
      return null;
    }
    return COLDP_LICENSES_CODES_TO_GBIF.get(license.replaceAll("[\\s_-]", "").toUpperCase(Locale.ENGLISH));
  }

  /**
   * The GBIF-supported license of a resource's intellectual rights, recognised as the basic metadata page does: from
   * its license URL or, without one, from the name of a license the IPT offers, e.g. Public Domain (CC0 1.0).
   *
   * @param eml the resource's metadata
   * @return CC0 1.0, CC BY 4.0 or CC BY-NC 4.0, null if the license isn't one GBIF supports or there's none
   */
  @Nullable
  public static License gbifLicense(@Nullable Eml eml) {
    if (eml == null || StringUtils.isBlank(eml.getIntellectualRights())) {
      return null;
    }
    String url = licenseUrl(eml.getIntellectualRights());
    if (url != null) {
      return gbifLicenseOfUrl(url);
    }
    // can be old-fashioned, with the version outside the parenthesis
    String rights = eml.getIntellectualRights().replace(") 4.0", " 4.0)");
    return IPT_LICENSE_NAMES.stream()
        .filter(rights::contains)
        .findFirst()
        .map(name -> LICENSE_PARSER.parseUriThenTitle(null, name))
        .filter(License::isConcrete)
        .orElse(null);
  }

  /**
   * The GBIF-supported license of a license URL, as GBIF parses it: with or without legalcode, http or https, and
   * ODC-PDDL and ODC-BY as CC0 and CC BY.
   *
   * @param url license URL, e.g. parsed from the EML's intellectual rights
   * @return CC0 1.0, CC BY 4.0 or CC BY-NC 4.0, null if the license isn't one GBIF supports or there's no URL
   */
  @Nullable
  static License gbifLicenseOfUrl(@Nullable String url) {
    if (StringUtils.isBlank(url)) {
      return null;
    }
    // GBIF doesn't parse a Creative Commons deed, e.g. .../by/4.0/deed.en, as its license
    String licenseUrl = CREATIVE_COMMONS_LICENSE.matcher(url.trim()).replaceFirst("$1/");
    try {
      License license = LICENSE_PARSER.parseUriThenTitle(URI.create(licenseUrl), null);
      return license.isConcrete() ? license : null;
    } catch (IllegalArgumentException e) {
      // not a URL
      return null;
    }
  }

  /**
   * Warns that the license of uploaded or imported metadata isn't one GBIF supports: the resource can't be published
   * until one is chosen. Metadata without a license is reported as incomplete anyway.
   */
  public static void warnIfLicenseUnsupported(Eml eml, ActionLogger alog) {
    if (StringUtils.isNotBlank(eml.getIntellectualRights()) && gbifLicense(eml) == null) {
      alog.warn("manage.resource.license.unsupported", new String[] {licenseLabel(eml)});
    }
  }

  /**
   * A resource's license as a user would recognise it in its intellectual rights: its title, else its URL, else the
   * start of the text.
   */
  public static String licenseLabel(Eml eml) {
    String url = licenseUrl(StringUtils.defaultString(eml.getIntellectualRights()));
    // the title of the first link, which may not be the license
    String title = url != null && url.equals(eml.parseLicenseUrl()) ? StringUtils.trimToNull(eml.parseLicenseTitle()) : null;
    if (title != null) {
      return title;
    }
    if (url != null) {
      return url;
    }
    return StringUtils.abbreviate(StringUtils.normalizeSpace(eml.getIntellectualRights()), 100);
  }

  /**
   * The first license URL in intellectual rights: other links are e.g. the publisher's, as a link or written out.
   *
   * @return the URL, null if there's none
   */
  @Nullable
  private static String licenseUrl(String intellectualRights) {
    Matcher matcher = URL.matcher(intellectualRights);
    while (matcher.find()) {
      // a written out URL can end a sentence
      String url = StringUtils.stripEnd(matcher.group(), ".,;:");
      if (LICENSE_HOSTS.stream().anyMatch(url::contains)) {
        return url;
      }
    }
    return null;
  }

  /**
   * The names of the licenses the IPT offers on the basic metadata page, e.g. Public Domain (CC0 1.0).
   */
  private static List<String> iptLicenseNames() {
    Properties properties = new Properties();
    try (InputStream in = MetadataUtils.class.getResourceAsStream(LICENSES_PROPFILE_PATH)) {
      if (in == null) {
        LOG.error("Licenses file {} not found", LICENSES_PROPFILE_PATH);
        return List.of();
      }
      properties.load(in);
    } catch (IOException e) {
      LOG.error("Failed to load licenses from {}", LICENSES_PROPFILE_PATH, e);
    }
    return properties.stringPropertyNames().stream()
        .filter(key -> key.startsWith("license.name."))
        .map(key -> StringUtils.trimToNull(properties.getProperty(key)))
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * A ColDP license as a frictionless descriptor's licenses: a license with its SPDX name and URL, as the descriptor
   * requires a name or path. Empty if the license isn't known, it's in the ColDP's metadata.yaml anyway.
   *
   * @param license ColDP license
   * @return licenses, empty if not known
   */
  public static List<FrictionlessLicense> colDpLicensesForDescriptor(String license) {
    String code = colDpLicenseToGbifCode(license);
    String path = code != null ? COLDP_LICENSE_URLS.get(code) : null;
    if (path == null) {
      return Collections.emptyList();
    }
    FrictionlessLicense frictionlessLicense = new FrictionlessLicense();
    frictionlessLicense.setName(code);
    frictionlessLicense.setPath(path);
    return List.of(frictionlessLicense);
  }

  /**
   * A ColDP's agents as a frictionless descriptor's contributors, with a title and a role: creators are authors,
   * editors and contributors contributors, the publisher the publisher and the contact the maintainer. Agents with
   * no name nor organisation are left out, a contributor needs a title.
   *
   * @param metadata ColDP metadata
   * @return contributors
   */
  public static List<FrictionlessContributor> colDpContributorsForDescriptor(ColMetadata metadata) {
    List<FrictionlessContributor> contributors = new ArrayList<>();
    addColDpContributors(contributors, metadata.getCreator(), "author");
    addColDpContributors(contributors, metadata.getEditor(), "contributor");
    addColDpContributors(contributors, metadata.getContributor(), "contributor");
    addColDpContributors(contributors, Collections.singletonList(metadata.getPublisher()), "publisher");
    addColDpContributors(contributors, Collections.singletonList(metadata.getContact()), "maintainer");
    return contributors;
  }

  private static void addColDpContributors(List<FrictionlessContributor> contributors, List<Agent> agents,
                                           String role) {
    if (agents == null) {
      return;
    }
    for (Agent agent : agents) {
      if (agent == null) {
        continue;
      }
      String name = StringUtils.trimToNull(
          StringUtils.trimToEmpty(agent.getGiven()) + " " + StringUtils.trimToEmpty(agent.getFamily()));
      String title = name != null ? name : StringUtils.trimToNull(agent.getOrganisation());
      if (title == null) {
        continue;
      }
      FrictionlessContributor contributor = new FrictionlessContributor();
      contributor.setTitle(title);
      contributor.setRole(role);
      contributor.setEmail(StringUtils.trimToNull(agent.getEmail()));
      if (name != null) {
        contributor.setOrganization(StringUtils.trimToNull(agent.getOrganisation()));
      }
      contributor.setPath(colDpAgentPath(agent));
      contributors.add(contributor);
    }
  }

  /**
   * A ColDP agent's ORCID (an ID or its URL, with or without https://) or else URL, as a contributor's path, if it's a
   * full http(s) URL.
   */
  private static String colDpAgentPath(Agent agent) {
    String orcid = StringUtils.trimToNull(agent.getOrcid());
    if (orcid != null) {
      return isHttpUrl(orcid) ? orcid : "https://orcid.org/" + Strings.CI.removeStart(orcid, "orcid.org/");
    }
    return httpUrlOrNull(agent.getUrl());
  }

  /**
   * The URL if it's a full http(s) URL, otherwise null: a data package descriptor can't be created with another (e.g.
   * www.example.org) as a homepage, image or contributor path.
   */
  public static String httpUrlOrNull(URI url) {
    return url != null && isHttpUrl(url.toString()) ? url.toString() : null;
  }

  private static boolean isHttpUrl(String url) {
    return Strings.CI.startsWithAny(url, "http://", "https://");
  }

  public static boolean isDataPackageType(String type) {
    return Strings.CS.equalsAny(type, CAMTRAP_DP, COL_DP, DWC_DP);
  }

  /**
   * Formats the data package "created" timestamp as UTC with second precision, e.g. 2026-01-15T10:00:00Z,
   * regardless of the server time zone.
   */
  public static String formatDataPackageCreated(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant.truncatedTo(ChronoUnit.SECONDS));
  }

  /**
   * Formats a date of the Camtrap DP temporal scope (start/end), e.g. 2026-01-15, in the server time zone. Unlike a
   * shared SimpleDateFormat, it can be called from several threads at once (requests and publications).
   */
  public static String formatCamtrapTemporalDate(Date date) {
    return CAMTRAP_TEMPORAL_DATE_FORMAT.format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()));
  }
}
