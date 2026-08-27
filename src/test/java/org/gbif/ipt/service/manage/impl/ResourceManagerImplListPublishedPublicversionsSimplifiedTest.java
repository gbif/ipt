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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.SimplifiedResource;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ResourceManagerImpl#listPublishedPublicVersionsSimplified(DatatableRequest)}.
 *
 * <p>Row layout produced by {@code toDatatableResourcePortalView} (index -> content):
 * 0 logo, 1 home link, 2 organization, 3 core type badge, 4 subtype badge, 5 records published link,
 * 6 modified, 7 last published, 8 next published, 9 status badge, 10 creator name, 11 shortname, 12 subject.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceManagerImplListPublishedPublicVersionsSimplifiedTest {

  private static final int COL_HOME_LINK = 1;
  private static final int COL_CORE_TYPE_BADGE = 3;
  private static final int COL_CREATOR = 10;
  private static final int COL_SHORTNAME = 11;
  private static final int COL_SUBJECT = 12;

  @Mock
  private AppConfig cfg;
  @Mock
  private DataDir dataDir;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceConvertersManager resourceConvertersManager;
  @Mock
  private SourceManager sourceManager;
  @Mock
  private ExtensionManager extensionManager;
  @Mock
  private DataPackageSchemaManager schemaManager;
  @Mock
  private RegistryManager registryManager;
  @Mock
  private GenerateDwcaFactory dwcaFactory;
  @Mock
  private GenerateDataPackageFactory dataPackageFactory;
  @Mock
  private GenerateDarwinCoreDataPackageFactory dwcDpFactory;
  @Mock
  private PasswordEncrypter passwordEncrypter;
  @Mock
  private Eml2Rtf eml2Rtf;
  @Mock
  private VocabulariesManager vocabManager;
  @Mock
  private SimpleTextProvider textProvider;
  @Mock
  private RegistrationManager registrationManager;
  @Mock
  private MetadataReader metadataReader;
  @Mock
  private ResourceMetadataInferringService resourceMetadataInferringService;

  private ResourceManagerImpl resourceManager;

  @BeforeEach
  void setUp() {
    lenient().when(cfg.getMaxThreads()).thenReturn(2);
    lenient().when(cfg.getBaseUrl()).thenReturn("http://localhost:8080");

    // default, empty-ish vocabularies; individual tests override as needed
    lenient().when(vocabManager.getI18nDatasetTypesVocab(anyString(), eq(false)))
        .thenReturn(new HashMap<>(Map.of("occurrence", "Occurrence", "checklist", "Checklist")));
    lenient().when(vocabManager.getI18nDatasetSubtypesVocab(anyString(), eq(false)))
        .thenReturn(new HashMap<>());
    lenient().when(schemaManager.list()).thenReturn(Collections.emptyList());

    // just echo back the default text passed in, so assertions don't depend on i18n resource bundles
    lenient().when(textProvider.getText(any(Locale.class), anyString(), anyString(), anyList()))
        .thenAnswer(invocation -> invocation.getArgument(2));

    resourceManager = new ResourceManagerImpl(cfg, dataDir, resourceConvertersManager, sourceManager,
        extensionManager, schemaManager, registryManager, dwcaFactory, dataPackageFactory, dwcDpFactory,
        passwordEncrypter, eml2Rtf, vocabManager, textProvider, registrationManager, metadataReader,
        resourceMetadataInferringService);
  }

  private static SimplifiedResource resource(String shortname) {
    SimplifiedResource r = new SimplifiedResource();
    r.setShortname(shortname);
    r.setTitle(shortname);
    r.setStatus(PublicationStatus.PUBLIC);
    r.setCoreType("occurrence");
    r.setCreatorName("Creator-" + shortname);
    r.setRecordsPublished(0);
    return r;
  }

  private static SimplifiedResource resource(String shortname, String title) {
    SimplifiedResource r = new SimplifiedResource();
    r.setShortname(shortname);
    r.setTitle(title);
    r.setStatus(PublicationStatus.PUBLIC);
    r.setCoreType("occurrence");
    r.setCreatorName("Creator-" + shortname);
    r.setRecordsPublished(0);
    return r;
  }

  private static DatatableRequest request() {
    return new DatatableRequest();
  }

  private static Date daysAgo(int days) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -days);
    return cal.getTime();
  }

  private void seed(SimplifiedResource... resources) throws Exception {
    Map<String, SimplifiedResource> map = new HashMap<>();
    for (SimplifiedResource r : resources) {
      map.put(r.getShortname(), r);
    }
    Field field = ResourceManagerImpl.class.getDeclaredField("publishedPublicVersionsSimplified");
    field.setAccessible(true);
    field.set(resourceManager, map);
  }

  private List<String> shortnamesInOrder(DatatableResult result) {
    List<String> result1 = new ArrayList<>();
    for (List<String> row : result.getData()) {
      result1.add(row.get(COL_SHORTNAME));
    }
    return result1;
  }

  @Test
  void returnsAllResourcesAndCorrectTotalsWhenSearchIsEmpty() throws Exception {
    seed(resource("res-a"), resource("res-b"), resource("res-c"));

    DatatableRequest req = request();
    req.setSearch("");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(3, result.getTotalRecords());
    assertEquals(3, result.getTotalDisplayRecords());
    assertEquals(3, result.getData().size());
  }

  @Test
  void totalRecordsReflectsFullMapEvenWhenFilteredOrPaginated() throws Exception {
    SimplifiedResource a = resource("alpha", "does-not-match-search");
    SimplifiedResource b = resource("beta", "does-not-match-search-either");
    SimplifiedResource c = resource("gamma");
    seed(a, b, c);

    DatatableRequest req = request();
    req.setSearch("alpha");
    req.setLimit(1);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    // totalRecords = size of the underlying map, unaffected by search or pagination
    assertEquals(3, result.getTotalRecords());
    // totalDisplayRecords = size after filtering only, not pagination
    assertEquals(1, result.getTotalDisplayRecords());
    assertEquals(1, result.getData().size());
  }

  @Test
  void searchMatchesByTitleCaseInsensitively() throws Exception {
    SimplifiedResource match = resource("res-a", "Amazing Butterflies of Peru");
    SimplifiedResource noMatch = resource("res-b", "Something else entirely");
    seed(match, noMatch);

    DatatableRequest req = request();
    req.setSearch("BUTTERFLIES");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(1, result.getTotalDisplayRecords());
    assertEquals(List.of("res-a"), shortnamesInOrder(result));
  }

  @Test
  void searchMatchesByShortname() throws Exception {
    seed(resource("dwca-birds"), resource("other-resource"));

    DatatableRequest req = request();
    req.setSearch("birds");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(1, result.getTotalDisplayRecords());
    assertEquals(List.of("dwca-birds"), shortnamesInOrder(result));
  }

  @Test
  void searchMatchesByOrganisationName() throws Exception {
    SimplifiedResource match = resource("res-a");
    match.setOrganisationName("Museum of Natural History");
    SimplifiedResource noMatch = resource("res-b");
    noMatch.setOrganisationName("Some University");
    seed(match, noMatch);

    DatatableRequest req = request();
    req.setSearch("museum");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("res-a"), shortnamesInOrder(result));
  }

  @Test
  void searchMatchesByCoreType() throws Exception {
    SimplifiedResource match = resource("res-a");
    match.setCoreType("checklist");
    SimplifiedResource noMatch = resource("res-b");
    noMatch.setCoreType("occurrence");
    seed(match, noMatch);

    DatatableRequest req = request();
    req.setSearch("checklist");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("res-a"), shortnamesInOrder(result));
  }

  @Test
  void searchMatchesBySubject() throws Exception {
    SimplifiedResource match = resource("res-a");
    match.setSubject("marine biodiversity");
    SimplifiedResource noMatch = resource("res-b");
    noMatch.setSubject("terrestrial biodiversity");
    seed(match, noMatch);

    DatatableRequest req = request();
    req.setSearch("marine");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("res-a"), shortnamesInOrder(result));
  }

  @Test
  void searchWithNoMatchesReturnsEmptyDataButKeepsTotalRecords() throws Exception {
    seed(resource("res-a"), resource("res-b"));

    DatatableRequest req = request();
    req.setSearch("no-such-resource-exists");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(2, result.getTotalRecords());
    assertEquals(0, result.getTotalDisplayRecords());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void paginationAppliesOffsetAndLimit() throws Exception {
    // shortnames chosen so default sort (index 1 -> title/shortname, asc) is alphabetical and predictable
    seed(resource("res-1"), resource("res-2"), resource("res-3"), resource("res-4"), resource("res-5"));

    DatatableRequest req = request();
    req.setOffset(1);
    req.setLimit(2);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(5, result.getTotalRecords());
    assertEquals(5, result.getTotalDisplayRecords());
    assertEquals(List.of("res-2", "res-3"), shortnamesInOrder(result));
  }

  @Test
  void offsetBeyondAvailableResourcesReturnsEmptyData() throws Exception {
    seed(resource("res-1"), resource("res-2"));

    DatatableRequest req = request();
    req.setOffset(10);
    req.setLimit(10);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(2, result.getTotalDisplayRecords());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void sortsAscendingByShortnameWhenSortIndexIsUnrecognized() throws Exception {
    seed(resource("charlie"), resource("alpha"), resource("bravo"));

    DatatableRequest req = request();
    req.setSortFieldIndex(99); // falls into the "else" branch -> sort by shortname
    req.setSortOrder("asc");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("alpha", "bravo", "charlie"), shortnamesInOrder(result));
  }

  @Test
  void sortsDescendingWhenOrderIsDesc() throws Exception {
    seed(resource("charlie"), resource("alpha"), resource("bravo"));

    DatatableRequest req = request();
    req.setSortFieldIndex(99);
    req.setSortOrder("desc");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("charlie", "bravo", "alpha"), shortnamesInOrder(result));
  }

  @Test
  void sortsByRecordsPublishedNumerically() throws Exception {
    SimplifiedResource small = resource("small");
    small.setRecordsPublished(5);
    SimplifiedResource medium = resource("medium");
    medium.setRecordsPublished(500);
    SimplifiedResource large = resource("large");
    large.setRecordsPublished(50000);
    seed(small, medium, large);

    DatatableRequest req = request();
    req.setSortFieldIndex(5); // records published
    req.setSortOrder("asc");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("small", "medium", "large"), shortnamesInOrder(result));
  }

  @Test
  void sortsByModifiedDateWithNullsFirstAscending() throws Exception {
    SimplifiedResource noDate = resource("no-date");
    noDate.setModified(null);
    SimplifiedResource older = resource("older");
    older.setModified(daysAgo(10));
    SimplifiedResource newer = resource("newer");
    newer.setModified(daysAgo(1));
    seed(noDate, older, newer);

    DatatableRequest req = request();
    req.setSortFieldIndex(6); // modified date
    req.setSortOrder("asc");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("no-date", "older", "newer"), shortnamesInOrder(result));
  }

  @Test
  void sortsByModifiedDateWithNullsLastDescending() throws Exception {
    SimplifiedResource noDate = resource("no-date");
    noDate.setModified(null);
    SimplifiedResource older = resource("older");
    older.setModified(daysAgo(10));
    SimplifiedResource newer = resource("newer");
    newer.setModified(daysAgo(1));
    seed(noDate, older, newer);

    DatatableRequest req = request();
    req.setSortFieldIndex(6); // modified date
    req.setSortOrder("desc");

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    assertEquals(List.of("newer", "older", "no-date"), shortnamesInOrder(result));
  }

  @Test
  void mergesInstalledDataPackageSchemasIntoDatasetTypeBadges() throws Exception {
    SimplifiedResource camtrapResource = resource("camtrap-res");
    camtrapResource.setCoreType("camtrap");
    seed(camtrapResource);

    when(schemaManager.list()).thenReturn(List.of(
        DataPackageSchema.builder().name("camtrap").shortTitle("Camtrap DP").build()));

    DatatableRequest req = request();

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    String badge = result.getData().get(0).get(COL_CORE_TYPE_BADGE);
    assertTrue(badge.contains("Camtrap DP"), "expected badge to contain schema short title, was: " + badge);
  }

  @Test
  void fallsBackToSchemaNameWhenShortTitleIsMissing() throws Exception {
    SimplifiedResource dpResource = resource("dp-res");
    dpResource.setCoreType("my-schema");
    seed(dpResource);

    when(schemaManager.list()).thenReturn(List.of(
        DataPackageSchema.builder().name("my-schema").shortTitle(null).build()));

    DatatableRequest req = request();

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    String badge = result.getData().get(0).get(COL_CORE_TYPE_BADGE);
    assertTrue(badge.contains("my-schema"), "expected badge to fall back to schema name, was: " + badge);
  }

  @Test
  void rowContainsCreatorNameShortnameAndSubjectVerbatim() throws Exception {
    SimplifiedResource r = resource("res-a");
    r.setCreatorName("Jane Doe");
    r.setSubject("Test Subject");
    seed(r);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(request());

    List<String> row = result.getData().get(0);
    assertEquals("Jane Doe", row.get(COL_CREATOR));
    assertEquals("res-a", row.get(COL_SHORTNAME));
    assertEquals("Test Subject", row.get(COL_SUBJECT));
  }

  @Test
  void rowUsesEmptyStringWhenSubjectIsNull() throws Exception {
    SimplifiedResource r = resource("res-a");
    r.setSubject(null);
    seed(r);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(request());

    assertEquals("", result.getData().get(0).get(COL_SUBJECT));
  }

  @Test
  void homeLinkPrefersTitleOverShortnameWhenTitleIsPresent() throws Exception {
    SimplifiedResource r = resource("official-shortname");
    r.setTitle("A Completely Different Human-Readable Title");
    seed(r);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(request());

    String homeLink = result.getData().get(0).get(COL_HOME_LINK);
    assertTrue(homeLink.contains("A Completely Different Human-Readable Title"),
        "expected home link to show the title, was: " + homeLink);
    assertFalse(homeLink.contains(">official-shortname<"),
        "home link incorrectly fell back to shortname even though a title was set: " + homeLink);
  }

  @Test
  void homeLinkFallsBackToShortnameOnlyWhenTitleIsBlank() throws Exception {
    SimplifiedResource r = resource("only-shortname-here");
    r.setTitle(null);
    seed(r);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(request());

    String homeLink = result.getData().get(0).get(COL_HOME_LINK);
    assertTrue(homeLink.contains(">only-shortname-here<"),
        "expected home link to fall back to shortname when title is null, was: " + homeLink);
  }

  @Test
  void distinctResourcesWithDistinctTitlesAreNotAllCollapsedToShortname() throws Exception {
    // guards specifically against a regression where every row ends up showing its shortname,
    // regardless of how distinct/populated the titles are
    SimplifiedResource a = resource("short-a", "Title For A");
    SimplifiedResource b = resource("short-b", "Title For B");
    SimplifiedResource c = resource("short-c", "Title For C");
    seed(a, b, c);

    DatatableRequest req = request();
    req.setLimit(10);

    DatatableResult result = resourceManager.listPublishedPublicVersionsSimplified(req);

    List<String> homeLinks = new ArrayList<>();
    for (List<String> row : result.getData()) {
      homeLinks.add(row.get(COL_HOME_LINK));
    }

    assertTrue(homeLinks.stream().anyMatch(link -> link.contains("Title For A")));
    assertTrue(homeLinks.stream().anyMatch(link -> link.contains("Title For B")));
    assertTrue(homeLinks.stream().anyMatch(link -> link.contains("Title For C")));
    assertTrue(homeLinks.stream().noneMatch(link -> link.contains(">short-a<")));
    assertTrue(homeLinks.stream().noneMatch(link -> link.contains(">short-b<")));
    assertTrue(homeLinks.stream().noneMatch(link -> link.contains(">short-c<")));
  }
}