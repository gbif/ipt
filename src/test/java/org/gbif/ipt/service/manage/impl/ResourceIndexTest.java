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

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceIndexTest {

  private static final UUID ORG_KEY = UUID.fromString("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
  private static final UUID OTHER_ORG_KEY = UUID.fromString("0a16da09-7719-40e4-b7a9-8bdb2a3a4d41");

  private ResourceIndex index;

  @BeforeEach
  public void setUp() {
    index = new ResourceIndex();
  }

  @Test
  public void testPutAndGetIsCaseInsensitive() {
    Resource resource = resource("Bees");
    index.put(resource);

    assertSame(resource, index.get("bees"));
    assertSame(resource, index.get("BEES"));
    assertSame(resource, index.get("Bees"));
  }

  @Test
  public void testGetUnknownOrNull() {
    index.put(resource("bees"));

    assertNull(index.get("ants"));
    assertNull(index.get(null));
  }

  @Test
  public void testPutReplacesResourceWithSameShortname() {
    Resource first = resource("bees");
    Resource second = resource("BEES");
    index.put(first);
    index.put(second);

    assertSame(second, index.get("bees"));
    assertEquals(1, index.list().size());
  }

  @Test
  public void testContains() {
    index.put(resource("bees"));

    assertTrue(index.contains("bees"));
    assertFalse(index.contains("ants"));
  }

  @Test
  public void testRemoveRemovesResourceAndSummary() {
    index.put(resource("bees"));
    index.putPublishedPublicSummary("bees", summary("bees"));

    index.remove("BEES");

    assertNull(index.get("bees"));
    assertNull(index.getPublishedPublicSummary("bees"));
    assertTrue(index.list().isEmpty());
    assertTrue(index.listPublishedPublicResourceSummaries().isEmpty());
  }

  @Test
  public void testRemovePublishedPublicSummaryKeepsResource() {
    Resource resource = resource("bees");
    index.put(resource);
    index.putPublishedPublicSummary("bees", summary("bees"));

    index.removePublishedPublicSummary("bees");

    assertSame(resource, index.get("bees"));
    assertNull(index.getPublishedPublicSummary("bees"));
  }

  @Test
  public void testClearKeepsSummaries() {
    index.put(resource("bees"));
    ResourceSummaryView summary = summary("bees");
    index.putPublishedPublicSummary("bees", summary);

    index.clear();

    assertTrue(index.list().isEmpty());
    assertSame(summary, index.getPublishedPublicSummary("bees"));
  }

  @Test
  public void testListReturnsCopies() {
    index.put(resource("bees"));
    index.putPublishedPublicSummary("bees", summary("bees"));

    List<Resource> resources = index.list();
    resources.clear();
    List<ResourceSummaryView> summaries = index.listPublishedPublicResourceSummaries();
    summaries.clear();

    assertEquals(1, index.list().size());
    assertEquals(1, index.listPublishedPublicResourceSummaries().size());
    assertNotSame(index.list(), index.list());
  }

  @Test
  public void testListByType() {
    Resource occurrence = resource("occ");
    occurrence.setCoreType("occurrence");
    Resource checklist = resource("chk");
    checklist.setCoreType("checklist");
    Resource noType = resource("none");
    index.put(occurrence);
    index.put(checklist);
    index.put(noType);

    assertEquals(List.of(occurrence), index.list("occurrence"));
    assertEquals(List.of(checklist), index.list("checklist"));
    assertTrue(index.list("samplingevent").isEmpty());
  }

  @Test
  public void testListByStatus() {
    Resource privateResource = resource("private");
    privateResource.setStatus(PublicationStatus.PRIVATE);
    Resource publicResource = resource("public");
    publicResource.setStatus(PublicationStatus.PUBLIC);
    index.put(privateResource);
    index.put(publicResource);

    assertEquals(List.of(privateResource), index.list(PublicationStatus.PRIVATE));
    assertEquals(List.of(publicResource), index.list(PublicationStatus.PUBLIC));
    assertTrue(index.list(PublicationStatus.REGISTERED).isEmpty());
  }

  @Test
  public void testListByUser() {
    User admin = user("admin@example.org", User.Role.Admin);
    User creator = user("creator@example.org", User.Role.Manager);
    User manager = user("manager@example.org", User.Role.Manager);
    User otherManager = user("other@example.org", User.Role.Manager);
    User creatorWithoutManagerRights = user("user@example.org", User.Role.User);

    Resource createdByCreator = resource("created");
    createdByCreator.setCreator(creator);
    Resource managedByManager = resource("managed");
    managedByManager.setCreator(otherManager);
    managedByManager.addManager(manager);
    Resource createdByUser = resource("user");
    createdByUser.setCreator(creatorWithoutManagerRights);
    index.put(createdByCreator);
    index.put(managedByManager);
    index.put(createdByUser);

    assertEquals(3, index.list(admin).size());
    assertEquals(List.of(createdByCreator), index.list(creator));
    assertEquals(List.of(managedByManager), index.list(manager));
    assertEquals(List.of(managedByManager), index.list(otherManager));
    // even resource creators need manager rights
    assertTrue(index.list(creatorWithoutManagerRights).isEmpty());
  }

  @Test
  public void testLatestExcludesUnpublishedPrivateAndDeleted() {
    Resource neverPublished = resource("never");
    Resource privateVersion = publishedResource("private", PublicationStatus.PRIVATE, new Date(1000));
    Resource deletedVersion = publishedResource("deleted", PublicationStatus.DELETED, new Date(2000));
    Resource publicVersion = publishedResource("public", PublicationStatus.PUBLIC, new Date(3000));
    Resource registeredVersion = publishedResource("registered", PublicationStatus.REGISTERED, new Date(4000));
    index.put(neverPublished);
    index.put(privateVersion);
    index.put(deletedVersion);
    index.put(publicVersion);
    index.put(registeredVersion);

    assertEquals(List.of(registeredVersion, publicVersion), index.latest(1, 10));
  }

  @Test
  public void testLatestSortsByModifiedDescendingWithNullsLast() {
    Resource oldest = publishedResource("oldest", PublicationStatus.PUBLIC, new Date(1000));
    Resource newest = publishedResource("newest", PublicationStatus.PUBLIC, new Date(3000));
    Resource middle = publishedResource("middle", PublicationStatus.PUBLIC, new Date(2000));
    Resource noModified = publishedResource("nomodified", PublicationStatus.PUBLIC, null);
    index.put(oldest);
    index.put(noModified);
    index.put(newest);
    index.put(middle);

    assertEquals(List.of(newest, middle, oldest, noModified), index.latest(1, 10));
  }

  @Test
  public void testListPublishedPublicVersions() {
    Resource neverPublished = resource("never");
    Resource privateVersion = publishedResource("private", PublicationStatus.PRIVATE, new Date());
    Resource deletedVersion = publishedResource("deleted", PublicationStatus.DELETED, new Date());
    Resource publicVersion = publishedResource("public", PublicationStatus.PUBLIC, new Date());
    Resource registeredVersion = publishedResource("registered", PublicationStatus.REGISTERED, new Date());
    Resource notReleased = resource("notreleased");
    notReleased.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), PublicationStatus.PUBLIC));
    index.put(neverPublished);
    index.put(privateVersion);
    index.put(deletedVersion);
    index.put(publicVersion);
    index.put(registeredVersion);
    index.put(notReleased);

    List<Resource> result = index.listPublishedPublicVersions();

    assertEquals(2, result.size());
    assertTrue(result.contains(publicVersion));
    assertTrue(result.contains(registeredVersion));
  }

  @Test
  public void testListPublishedPublicVersionsUsesLatestVersion() {
    // the latest version was made private
    Resource resource = resource("bees");
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC));
    resource.addVersionHistory(new VersionHistory(new BigDecimal("2.0"), new Date(), PublicationStatus.PRIVATE));
    index.put(resource);

    assertTrue(index.listPublishedPublicVersions().isEmpty());
  }

  @Test
  public void testListPublishedPublicVersionsIncludesRegisteredResourcesWithoutVersionHistory() {
    // resources published before IPT 2.2 have no version history
    Resource resource = resource("bees");
    resource.setKey(UUID.randomUUID());
    resource.setStatus(PublicationStatus.REGISTERED);
    index.put(resource);

    assertEquals(List.of(resource), index.listPublishedPublicVersions());
  }

  @Test
  public void testUpdateOrganisationName() {
    Resource resource = resource("bees");
    resource.setOrganisation(organisation(ORG_KEY, "Old name", "OLD"));
    Resource otherResource = resource("ants");
    otherResource.setOrganisation(organisation(OTHER_ORG_KEY, "Other name", "OTHER"));
    Resource noOrganisation = resource("wasps");
    Resource organisationWithoutKey = resource("flies");
    organisationWithoutKey.setOrganisation(new Organisation());
    index.put(resource);
    index.put(otherResource);
    index.put(noOrganisation);
    index.put(organisationWithoutKey);

    ResourceSummaryView summary = summary("bees");
    summary.setOrganisationKey(ORG_KEY);
    summary.setOrganisationName("Old name");
    summary.setOrganisationAlias("OLD");
    ResourceSummaryView otherSummary = summary("ants");
    otherSummary.setOrganisationKey(OTHER_ORG_KEY);
    otherSummary.setOrganisationName("Other name");
    otherSummary.setOrganisationAlias("OTHER");
    ResourceSummaryView summaryWithoutOrganisation = summary("wasps");
    index.putPublishedPublicSummary("bees", summary);
    index.putPublishedPublicSummary("ants", otherSummary);
    index.putPublishedPublicSummary("wasps", summaryWithoutOrganisation);

    index.updateOrganisationName(ORG_KEY, "New name", "NEW");

    assertEquals("New name", resource.getOrganisation().getName());
    assertEquals("NEW", resource.getOrganisation().getAlias());
    assertEquals("New name", summary.getOrganisationName());
    assertEquals("NEW", summary.getOrganisationAlias());

    assertEquals("Other name", otherResource.getOrganisation().getName());
    assertEquals("OTHER", otherResource.getOrganisation().getAlias());
    assertEquals("Other name", otherSummary.getOrganisationName());
    assertEquals("OTHER", otherSummary.getOrganisationAlias());

    assertNull(noOrganisation.getOrganisation());
    assertNull(organisationWithoutKey.getOrganisation().getName());
    assertNull(summaryWithoutOrganisation.getOrganisationName());
  }

  private static Resource resource(String shortname) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    return resource;
  }

  private static Resource publishedResource(String shortname, PublicationStatus status, Date modified) {
    Resource resource = resource(shortname);
    resource.setModified(modified);
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), status));
    return resource;
  }

  private static ResourceSummaryView summary(String shortname) {
    ResourceSummaryView summary = new ResourceSummaryView();
    summary.setShortname(shortname);
    return summary;
  }

  private static User user(String email, User.Role role) {
    User user = new User();
    user.setEmail(email);
    user.setRole(role);
    return user;
  }

  private static Organisation organisation(UUID key, String name, String alias) {
    Organisation organisation = new Organisation();
    organisation.setKey(key.toString());
    organisation.setName(name);
    organisation.setAlias(alias);
    return organisation;
  }
}
