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

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.StatusReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * The manage page table: organisation filter, columns and the line saying what needs doing.
 * Row layout: 0 name, 1 status, 2 organisation, 3 type, 4 records, 5 last published, 6 next published, 7 modified.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceDataTableViewManagerImplManageTest {

  private static final int COL_NAME = 0;
  private static final int COL_STATUS = 1;
  private static final int COL_ORGANISATION = 2;
  private static final int COL_NEXT = 6;

  @Mock
  private AppConfig cfg;
  @Mock
  private DataDir dataDir;
  @Mock
  private DataPackageSchemaManager schemaManager;
  @Mock
  private VocabulariesManager vocabManager;
  @Mock
  private SimpleTextProvider textProvider;
  @Mock
  private ResourceManager resourceManager;
  @Mock
  private ResourcePublicationManager resourcePublicationManager;

  private final Map<String, Future<Map<String, Long>>> futures = new HashMap<>();
  private final Map<String, StatusReport> reports = new HashMap<>();
  private final List<Resource> resources = new ArrayList<>();

  private ResourceDataTableViewManagerImpl manager;

  private final User me = user("me@example.org", User.Role.Manager);
  private final User other = user("other@example.org", User.Role.Manager);
  private final User admin = user("admin@example.org", User.Role.Admin);
  private final Organisation orgA = organisation("Organisation A");
  private final Organisation orgB = organisation("Organisation B");

  @BeforeEach
  void setUp() {
    lenient().when(cfg.getMaxThreads()).thenReturn(2);
    lenient().when(cfg.getBaseUrl()).thenReturn("http://localhost:8080");
    lenient().when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    lenient().when(vocabManager.getI18nDatasetTypesVocab(anyString(), eq(false)))
        .thenReturn(new HashMap<>(Map.of("occurrence", "Occurrence")));
    lenient().when(vocabManager.getI18nDatasetSubtypesVocab(anyString(), eq(false))).thenReturn(new HashMap<>());
    lenient().when(schemaManager.list()).thenReturn(Collections.emptyList());
    // echo the default text, filling in {0}
    lenient().when(textProvider.getText(any(Locale.class), anyString(), anyString(), anyList()))
        .thenAnswer(invocation -> {
          String text = invocation.getArgument(2);
          List<?> args = invocation.getArgument(3);
          return args.isEmpty() ? text : text.replace("{0}", String.valueOf(args.get(0)));
        });
    lenient().when(resourceManager.list()).thenReturn(resources);
    lenient().when(resourcePublicationManager.getProcessFutures()).thenReturn(futures);
    lenient().when(resourcePublicationManager.getProcessReports()).thenReturn(reports);

    manager = new ResourceDataTableViewManagerImpl(
        cfg, dataDir, resourceManager, vocabManager, schemaManager, textProvider, resourcePublicationManager);
  }

  private static User user(String email, User.Role role) {
    User u = new User();
    u.setEmail(email);
    u.setRole(role);
    return u;
  }

  private static Organisation organisation(String name) {
    Organisation o = new Organisation();
    o.setKey(UUID.randomUUID().toString());
    o.setName(name);
    return o;
  }

  private Resource resource(String shortname, User creator, Organisation organisation) {
    Resource r = new Resource();
    r.setShortname(shortname);
    r.setTitle(shortname);
    r.setCoreType("occurrence");
    r.setCreator(creator);
    r.setOrganisation(organisation);
    r.setStatus(PublicationStatus.PUBLIC);
    resources.add(r);
    return r;
  }

  private static Date minutesAgo(int minutes) {
    return new Date(System.currentTimeMillis() - minutes * 60_000L);
  }

  private static DatatableRequest request() {
    DatatableRequest request = new DatatableRequest();
    request.setSortFieldIndex(0);
    return request;
  }

  private static List<String> names(DatatableResult result) {
    List<String> names = new ArrayList<>();
    for (List<String> row : result.getData()) {
      String cell = row.get(COL_NAME);
      names.add(cell.substring(cell.indexOf("mh-shortname\">") + 14, cell.indexOf("</div>")));
    }
    return names;
  }

  private String statusCell(Resource resource) {
    DatatableResult result = manager.list(admin, request());
    for (List<String> row : result.getData()) {
      if (row.get(COL_NAME).contains("mh-shortname\">" + resource.getShortname() + "<")) {
        return row.get(COL_STATUS);
      }
    }
    throw new AssertionError("no row for " + resource.getShortname());
  }

  @Test
  void listsWhatTheUserCanManage() {
    resource("mine-1", me, orgA);
    resource("shared-1", other, orgA).addManager(me);
    resource("not-mine", other, orgB);

    assertEquals(List.of("mine-1", "shared-1"), names(manager.list(me, request())));
    assertEquals(3, manager.list(admin, request()).getTotalRecords());
  }

  @Test
  void organisationFilterAppliesToTheListAndTheTotal() {
    resource("a-1", me, orgA);
    resource("b-1", me, orgB);
    resource("b-2", other, orgB);

    DatatableRequest request = request();
    request.setOrganisation(orgB.getKey().toString());
    DatatableResult result = manager.list(admin, request);

    assertEquals(List.of("b-1", "b-2"), names(result));
    assertEquals(2, result.getTotalRecords());
  }

  @Test
  void organisationsForTheFilterAreTheUsersSortedByName() {
    orgA.setAlias("Zeta");
    resource("a-1", me, orgA);
    resource("b-1", me, orgB);
    resource("b-2", me, orgB);
    resource("c-1", other, organisation("Not mine"));
    resource("no-organisation", me, null);

    Map<String, String> organisations = manager.listOrganisations(me);
    // a resource without an organisation is under "No organization", the default organisation's key
    assertEquals(List.of("No organization", "Organisation B", "Zeta"), new ArrayList<>(organisations.values()));
    assertEquals(org.gbif.ipt.config.Constants.DEFAULT_ORG_KEY.toString(), organisations.keySet().iterator().next());
  }

  @Test
  void organisationColumnIsWantedOnlyWithSeveralOrganisations() {
    resource("a-1", me, orgA);
    resource("a-2", other, orgA);
    resource("no-organisation", me, null);
    Organisation noOrganisation = organisation("No organization");
    noOrganisation.setKey(org.gbif.ipt.config.Constants.DEFAULT_ORG_KEY.toString());
    resource("default-organisation", me, noOrganisation);
    assertFalse(manager.hasSeveralOrganisations());

    resource("b-1", other, orgB);
    assertTrue(manager.hasSeveralOrganisations());
  }

  @Test
  void noOrganisationFilterFindsResourcesWithoutOneAndTheCellSaysSo() {
    resource("a-1", me, orgA);
    resource("none", me, null);
    Organisation defaultOrganisation = organisation("No organization");
    defaultOrganisation.setKey(org.gbif.ipt.config.Constants.DEFAULT_ORG_KEY.toString());
    resource("default", me, defaultOrganisation);

    DatatableRequest request = request();
    request.setOrganisation(org.gbif.ipt.config.Constants.DEFAULT_ORG_KEY.toString());
    DatatableResult result = manager.list(me, request);

    assertEquals(List.of("default", "none"), names(result));
    for (List<String> row : result.getData()) {
      assertTrue(row.get(COL_ORGANISATION).contains(">not set<"), row.get(COL_ORGANISATION));
    }
  }

  @Test
  void resourcesWithoutOrganisationSortTogetherAtOneEnd() {
    resource("aardvark", me, organisation("Aardvark"));
    Organisation defaultOrganisation = organisation("No organization");
    defaultOrganisation.setKey(org.gbif.ipt.config.Constants.DEFAULT_ORG_KEY.toString());
    resource("default", me, defaultOrganisation);
    resource("zeta", me, organisation("Zeta"));
    resource("none", me, null);

    DatatableRequest ascending = request();
    ascending.setSortFieldIndex(COL_ORGANISATION);
    ascending.setSortOrder("asc");
    assertEquals(List.of("default", "none", "aardvark", "zeta"), names(manager.list(me, ascending)));

    DatatableRequest descending = request();
    descending.setSortFieldIndex(COL_ORGANISATION);
    descending.setSortOrder("desc");
    assertEquals(List.of("zeta", "aardvark", "default", "none"), names(manager.list(me, descending)));
  }

  @Test
  void longOrganisationNameIsKeptWholeInTheTitle() {
    Organisation longName = organisation("Netherlands Mycological Society <NMV>");
    resource("a-1", me, longName);

    String cell = manager.list(me, request()).getData().get(0).get(COL_ORGANISATION);
    assertTrue(cell.contains("title=\"Netherlands Mycological Society &lt;NMV&gt;\""), cell);
  }

  @Test
  void editsSinceLastPublicationAreNotFlagged() {
    // the IPT can't see changes in URL and SQL source data, so the list doesn't claim to know what changed
    Resource r = resource("r", me, orgA);
    r.setLastPublished(minutesAgo(10));
    r.setMetadataModified(minutesAgo(1));
    r.setMappingsModified(minutesAgo(1));
    r.setSourcesModified(minutesAgo(1));
    assertFalse(statusCell(r).contains("mh-attention"));
  }

  @Test
  void pendingVisibilityIsShownInTheStatusPill() {
    Resource r = resource("r", me, orgA);
    r.setStatus(PublicationStatus.PRIVATE);
    r.setLastPublished(minutesAgo(10));
    r.setPendingStatus(PublicationStatus.PUBLIC);

    String status = statusCell(r);
    assertTrue(status.contains("status-private"), status);
    assertTrue(status.contains("#arrow_forward"), status);
    assertFalse(status.contains("mh-attention"), status);
  }

  @Test
  void neverPublishedOrDeletedHaveNoAttentionLine() {
    Resource neverPublished = resource("never", me, orgA);
    neverPublished.setMetadataModified(new Date());
    // records and last publication say it; no line under the status
    assertFalse(statusCell(neverPublished).contains("mh-attention"));
    List<String> neverPublishedRow = manager.list(me, request()).getData().get(0);
    assertTrue(neverPublishedRow.get(4).contains(">not published yet<"), neverPublishedRow.get(4));
    assertTrue(neverPublishedRow.get(5).contains(">not published yet<"), neverPublishedRow.get(5));

    Resource deleted = resource("deleted", me, orgA);
    deleted.setStatus(PublicationStatus.DELETED);
    deleted.setLastPublished(minutesAgo(10));
    deleted.setMetadataModified(new Date());
    assertFalse(statusCell(deleted).contains("mh-attention"));
  }

  @Test
  void failedPublicationIsShownAndReadWithoutSideEffects() {
    Resource r = resource("r", me, orgA);
    r.setLastPublished(minutesAgo(10));
    reports.put("r", new StatusReport(new RuntimeException("boom"), "Failed", Collections.emptyList()));

    String status = statusCell(r);
    assertTrue(status.contains("Last publication failed"), status);
    // isLocked and status() would finish a completed publication: the list must not call them
    verify(resourcePublicationManager, never()).isLocked(anyString());
    verify(resourcePublicationManager, never()).status(anyString());
  }

  @Test
  void runningPublicationIsShown() {
    Resource r = resource("r", me, orgA);
    futures.put("r", new CompletableFuture<>());

    assertTrue(statusCell(r).contains("Publishing"));
  }

  @Test
  void suspendedAutoPublishingIsShownOnlyWhenAutoPublishingIsOn() {
    Resource r = resource("r", me, orgA);
    r.setLastPublished(minutesAgo(10));
    r.setPublicationMode(org.gbif.ipt.model.voc.PublicationMode.AUTO_PUBLISH_ON);
    r.setUpdateFrequency("monthly");
    lenient().when(resourcePublicationManager.hasMaxProcessFailures(r, false)).thenReturn(true);
    // suspension follows failed publications, so the last one has failed too: suspended is the one shown
    reports.put("r", new StatusReport(new RuntimeException("boom"), "Failed", Collections.emptyList()));

    String status = statusCell(r);
    assertTrue(status.contains("Auto-publishing suspended"), status);
    assertFalse(status.contains("Last publication failed"), status);

    lenient().when(cfg.isAutoPublishingEnabled()).thenReturn(false);
    assertFalse(statusCell(r).contains("Auto-publishing suspended"));
  }

  @Test
  void nextPublicationShowsTheDateAndFrequencyOrManualOrStopped() {
    lenient().when(vocabManager.getI18nVocab(anyString(), anyString(), eq(false)))
        .thenReturn(Map.of("monthly", "Monthly"));
    Resource auto = resource("auto", me, orgA);
    auto.setLastPublished(minutesAgo(10));
    auto.setPublicationMode(org.gbif.ipt.model.voc.PublicationMode.AUTO_PUBLISH_ON);
    auto.setUpdateFrequency("monthly");
    auto.setNextPublished(new Date(System.currentTimeMillis() + 86_400_000L));
    Resource manual = resource("manual", me, orgA);
    manual.setLastPublished(minutesAgo(10));

    DatatableResult result = manager.list(me, request());
    String autoNext = result.getData().get(0).get(COL_NEXT);
    String manualNext = result.getData().get(1).get(COL_NEXT);
    assertTrue(autoNext.contains("#autorenew") && autoNext.contains(" monthly<"), autoNext);
    assertFalse(autoNext.contains("text-gbif-danger"), autoNext);
    assertTrue(manualNext.contains(">manual<"), manualNext);

    lenient().when(resourcePublicationManager.hasMaxProcessFailures(auto, false)).thenReturn(true);
    String stoppedNext = manager.list(me, request()).getData().get(0).get(COL_NEXT);
    assertTrue(stoppedNext.contains(">stopped<"), stoppedNext);
  }

  @Test
  void reservedDoiIsShown() {
    Resource r = resource("r", me, orgA);
    r.setLastPublished(minutesAgo(10));
    r.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    r.setDoi(new DOI("10.5072/abc"));

    assertTrue(statusCell(r).contains("DOI 10.5072/abc reserved"));
  }

  @Test
  void sortsByTheManageColumns() {
    Resource b = resource("b", me, orgA);
    b.setLastPublished(minutesAgo(5));
    Resource a = resource("a", me, orgA);
    a.setLastPublished(minutesAgo(50));
    Resource c = resource("c", me, orgA);

    DatatableRequest byLastPublished = request();
    byLastPublished.setSortFieldIndex(5);
    byLastPublished.setSortOrder("desc");
    assertEquals(List.of("b", "a", "c"), names(manager.list(me, byLastPublished)));

    DatatableRequest byName = request();
    byName.setSortFieldIndex(0);
    byName.setSortOrder("asc");
    assertEquals(List.of("a", "b", "c"), names(manager.list(me, byName)));
  }
}
