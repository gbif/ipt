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

import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.Agent;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COLDP_LICENSES_CODES_TO_GBIF;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DWC_DP;

public final class MetadataUtils {

  private static final DateTimeFormatter CAMTRAP_TEMPORAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
