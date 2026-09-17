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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.struts2.RequireManagerInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In-memory index of resources known to the IPT, keyed by shortname (lower case), plus a
 * lightweight {@link ResourceSummaryView} of each resource's last published public version
 * (used to render the home page without reconstructing full resources).
 * <p>
 * This class is purely an index: it has no knowledge of the filesystem or persistence. Callers
 * (namely {@link ResourceManagerImpl}) are responsible for reading/writing resources to disk and
 * keeping this index in sync via {@link #put}/{@link #remove} etc.
 * <p>
 * Note: some existing call sites are inconsistent about lower-casing shortnames when reading vs.
 * writing {@code publishedPublicResourceSummaries}, and {@link #contains} is not case-normalized.
 * These quirks are preserved here exactly as they existed in {@code ResourceManagerImpl} prior to
 * this extraction; they are not fixed as part of this refactor.
 */
class ResourceIndex {

  // key=shortname in lower case, value=resource
  private final Map<String, Resource> resources = new HashMap<>();
  // simplified resources for home page (metadata from last published version!)
  private final Map<String, ResourceSummaryView> publishedPublicResourceSummaries = new HashMap<>();

  /**
   * Adds/updates a resource in the index, keyed by its shortname (lower case).
   */
  void put(Resource resource) {
    resources.put(resource.getShortname().toLowerCase(), resource);
  }

  /**
   * Adds/updates the published-public summary view for the given (raw, non-lower-cased) shortname.
   */
  void putPublishedPublicSummary(String shortname, ResourceSummaryView summary) {
    publishedPublicResourceSummaries.put(shortname, summary);
  }

  /**
   * Removes a resource and its published-public summary (if any), using a lower-cased shortname
   * for both lookups (matches the behaviour of the former {@code delete}/{@code deleteResourceFromIpt}).
   */
  void remove(String shortname) {
    resources.remove(shortname.toLowerCase());
    publishedPublicResourceSummaries.remove(shortname.toLowerCase());
  }

  /**
   * Removes just the published-public summary, keyed by the raw (non-lower-cased) shortname.
   */
  void removePublishedPublicSummary(String shortname) {
    publishedPublicResourceSummaries.remove(shortname);
  }

  /**
   * Clears the resource index. Does not clear published-public summaries (matches previous behaviour
   * of {@code load(File, User)}, which only cleared the {@code resources} map).
   */
  void clear() {
    resources.clear();
  }

  /**
   * @param shortname raw (non-lower-cased) shortname, matching the previous unnormalized check
   */
  boolean contains(String shortname) {
    return resources.containsKey(shortname);
  }

  Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  ResourceSummaryView getPublishedPublicSummary(String shortname) {
    return publishedPublicResourceSummaries.get(shortname);
  }

  List<Resource> list() {
    return new ArrayList<>(resources.values());
  }

  List<ResourceSummaryView> listPublishedPublicResourceSummaries() {
    return new ArrayList<>(publishedPublicResourceSummaries.values());
  }

  List<Resource> list(String type) {
    return resources.values().stream()
        .filter(res -> type.equals(res.getCoreType()))
        .collect(Collectors.toList());
  }

  List<Resource> list(PublicationStatus status) {
    List<Resource> result = new ArrayList<>();
    for (Resource r : resources.values()) {
      if (r.getStatus() == status) {
        result.add(r);
      }
    }
    return result;
  }

  List<Resource> list(User user) {
    List<Resource> result = new ArrayList<>();
    // select based on user rights - for testing return all resources for now
    for (Resource res : resources.values()) {
      if (RequireManagerInterceptor.isAuthorized(user, res)) {
        result.add(res);
      }
    }
    return result;
  }

  List<Resource> latest(int startPage, int pageSize) {
    List<Resource> resourceList = new ArrayList<>();
    for (Resource r : resources.values()) {
      VersionHistory latestVersion = r.getLastPublishedVersion();
      if (latestVersion != null) {
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE)) {
          resourceList.add(r);
        }
      }
    }
    resourceList.sort((r1, r2) -> {
      if (r1 == null || r1.getModified() == null) {
        return 1;
      }
      if (r2 == null || r2.getModified() == null) {
        return -1;
      }
      if (r1.getModified().before(r2.getModified())) {
        return 1;
      } else {
        return -1;
      }
    });
    return resourceList;
  }

  List<Resource> listPublishedPublicVersions() {
    List<Resource> result = new ArrayList<>();
    for (Resource r : resources.values()) {
      List<VersionHistory> history = r.getVersionHistory();
      if (!history.isEmpty()) {
        VersionHistory latestVersion = history.get(0);
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE) &&
            latestVersion.getReleased() != null) {
          result.add(r);
        }
      } else if (r.isRegistered()) { // for backwards compatibility with resources published prior to v2.2
        result.add(r);
      }
    }
    return result;
  }

  void updateOrganisationName(UUID organisationKey, String organisationName, String organisationAlias) {
    resources.values().stream()
        .filter(r -> r.getOrganisation() != null)
        .filter(r -> r.getOrganisation().getKey() != null)
        .filter(r -> r.getOrganisation().getKey().equals(organisationKey))
        .forEach(r -> {
          r.getOrganisation().setAlias(organisationAlias);
          r.getOrganisation().setName(organisationName);
        });
    publishedPublicResourceSummaries.values().stream()
        .filter(r -> r.getOrganisationKey() != null)
        .filter(r -> r.getOrganisationKey().equals(organisationKey))
        .forEach(r -> {
          r.setOrganisationName(organisationName);
          r.setOrganisationAlias(organisationAlias);
        });
  }
}
