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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.datatable.HomeSearchRequest;
import org.gbif.ipt.model.datatable.HomeSearchResult;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.gbif.metadata.eml.ipt.model.Eml;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * The home page search: {@link ResourceDataTableViewManagerImpl#searchPublishedPublicResources}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceDataTableViewManagerImplHomeTest {

  private static final UUID ORG_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ORG_B = UUID.fromString("22222222-2222-2222-2222-222222222222");

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

  private ResourceDataTableViewManagerImpl manager;

  @BeforeEach
  void setUp() {
    lenient().when(cfg.getMaxThreads()).thenReturn(2);
    lenient().when(cfg.getBaseUrl()).thenReturn("http://localhost:8080");
    lenient().when(cfg.getPortalUrl()).thenReturn("https://www.gbif.org");
    lenient().when(vocabManager.getI18nDatasetTypesVocab(anyString(), eq(false)))
        .thenReturn(new HashMap<>(Map.of("occurrence", "Occurrence", "checklist", "Checklist", "metadata", "Metadata-only")));
    lenient().when(vocabManager.getI18nDatasetSubtypesVocab(anyString(), eq(false)))
        .thenReturn(new HashMap<>(Map.of("specimen", "Specimen")));
    lenient().when(schemaManager.list()).thenReturn(Collections.emptyList());
    lenient().when(textProvider.getText(any(Locale.class), anyString(), anyString(), anyList()))
        .thenAnswer(invocation -> invocation.getArgument(2));

    manager = new ResourceDataTableViewManagerImpl(
        cfg, dataDir, resourceManager, vocabManager, schemaManager, textProvider, resourcePublicationManager);
  }

  private static ResourceSummaryView resource(String shortname, String type, UUID org, String licence, int daysAgo, long records) {
    ResourceSummaryView r = new ResourceSummaryView();
    r.setShortname(shortname);
    r.setTitle("Title " + shortname);
    r.setStatus(PublicationStatus.PUBLIC);
    r.setCoreType(type);
    r.setOrganisationKey(org);
    r.setOrganisationName(org == null ? null : "Org " + org.toString().charAt(0));
    r.setLicence(licence);
    r.setRecordsPublished(records);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
    r.setLastPublished(cal.getTime());
    r.setPublished(true);
    return r;
  }

  private void seed(ResourceSummaryView... resources) {
    when(resourceManager.listPublishedPublicResourceSummaries()).thenReturn(new ArrayList<>(Arrays.asList(resources)));
  }

  private static List<String> shortnames(HomeSearchResult result) {
    return result.getItems().stream().map(HomeSearchResult.Item::getShortname).toList();
  }

  private static Map<String, Integer> counts(HomeSearchResult result, String facet) {
    return result.getFacets().get(facet).stream()
        .collect(Collectors.toMap(HomeSearchResult.FacetValue::getValue, HomeSearchResult.FacetValue::getCount));
  }

  private void seedThree() {
    seed(
        resource("a", "occurrence", ORG_A, "CC0-1.0", 5, 100),
        resource("b", "checklist", ORG_B, "CC-BY-4.0", 100, 10),
        resource("c", "occurrence", ORG_B, null, 800, 1000));
  }

  @Test
  void defaultSortIsMostRecentlyPublishedFirst() {
    seedThree();
    HomeSearchResult result = manager.searchPublishedPublicResources(new HomeSearchRequest());
    assertEquals(List.of("a", "b", "c"), shortnames(result));
    assertEquals(3, result.getTotal());
    assertEquals(3, result.getMatched());
  }

  @Test
  void sortsByTitleAndRecords() {
    seedThree();
    HomeSearchRequest request = new HomeSearchRequest();
    request.setSort(HomeSearchRequest.SORT_RECORDS);
    assertEquals(List.of("c", "a", "b"), shortnames(manager.searchPublishedPublicResources(request)));
    request.setSort(HomeSearchRequest.SORT_TITLE);
    assertEquals(List.of("a", "b", "c"), shortnames(manager.searchPublishedPublicResources(request)));
  }

  @Test
  void filtersCombineAndValuesWithinAFilterAreAlternatives() {
    seedThree();
    HomeSearchRequest request = new HomeSearchRequest();
    request.setTypes(Set.of("occurrence"));
    request.setOrganisations(Set.of(ORG_A.toString(), ORG_B.toString()));
    assertEquals(List.of("a", "c"), shortnames(manager.searchPublishedPublicResources(request)));

    request.setLicences(Set.of(ResourceDataTableViewManagerImpl.HOME_LICENCE_OTHER));
    HomeSearchResult result = manager.searchPublishedPublicResources(request);
    assertEquals(List.of("c"), shortnames(result));
    assertEquals(1, result.getMatched());
    assertEquals(3, result.getTotal());
  }

  @Test
  void facetCountsLeaveOutTheirOwnFilter() {
    seedThree();
    HomeSearchRequest request = new HomeSearchRequest();
    request.setTypes(Set.of("checklist"));
    HomeSearchResult result = manager.searchPublishedPublicResources(request);

    // the type counts ignore the type filter
    assertEquals(Map.of("occurrence", 2, "checklist", 1), counts(result, ResourceDataTableViewManagerImpl.FACET_TYPES));
    // the others apply it: only b is a checklist; values without resources stay, with 0
    assertEquals(Map.of(ORG_A.toString(), 0, ORG_B.toString(), 1),
        counts(result, ResourceDataTableViewManagerImpl.FACET_ORGANISATIONS));
    assertEquals(Map.of("CC0-1.0", 0, "CC-BY-4.0", 1, "other", 0),
        counts(result, ResourceDataTableViewManagerImpl.FACET_LICENCES));
  }

  @Test
  void licenceValuesInFixedOrderWithOtherLast() {
    seedThree();
    HomeSearchResult result = manager.searchPublishedPublicResources(new HomeSearchRequest());
    assertEquals(List.of("CC0-1.0", "CC-BY-4.0", "other"),
        result.getFacets().get(ResourceDataTableViewManagerImpl.FACET_LICENCES).stream()
            .map(HomeSearchResult.FacetValue::getValue).toList());
  }

  @Test
  void updatedFilter() {
    seedThree();
    HomeSearchRequest request = new HomeSearchRequest();
    request.setUpdated(HomeSearchRequest.UPDATED_MONTH);
    assertEquals(List.of("a"), shortnames(manager.searchPublishedPublicResources(request)));
    request.setUpdated(HomeSearchRequest.UPDATED_YEAR);
    assertEquals(List.of("a", "b"), shortnames(manager.searchPublishedPublicResources(request)));
    request.setUpdated(HomeSearchRequest.UPDATED_OLDER);
    HomeSearchResult result = manager.searchPublishedPublicResources(request);
    assertEquals(List.of("c"), shortnames(result));
    assertEquals(Map.of("month", 1, "year", 2, "older", 1), counts(result, ResourceDataTableViewManagerImpl.FACET_UPDATED));
  }

  @Test
  void searchMatchesDescription() {
    ResourceSummaryView a = resource("a", "occurrence", ORG_A, "CC0-1.0", 5, 100);
    a.setDescription("Benthos of the Wadden Sea");
    seed(a, resource("b", "checklist", ORG_B, "CC-BY-4.0", 100, 10));
    HomeSearchRequest request = new HomeSearchRequest();
    request.setSearch("wadden");
    assertEquals(List.of("a"), shortnames(manager.searchPublishedPublicResources(request)));
  }

  @Test
  void resourcesWithoutOrganisationShareTheDefaultOrganisationValue() {
    seed(resource("a", "occurrence", null, "CC0-1.0", 5, 1),
        resource("b", "occurrence", Constants.DEFAULT_ORG_KEY, "CC0-1.0", 5, 1),
        resource("c", "occurrence", ORG_A, "CC0-1.0", 5, 1));
    HomeSearchResult result = manager.searchPublishedPublicResources(new HomeSearchRequest());
    List<HomeSearchResult.FacetValue> organisations = result.getFacets().get(ResourceDataTableViewManagerImpl.FACET_ORGANISATIONS);
    assertEquals(2, organisations.size());
    // "not set" last
    assertEquals(Constants.DEFAULT_ORG_KEY.toString(), organisations.get(1).getValue());
    assertEquals(2, organisations.get(1).getCount());
    assertEquals("Organization not set", organisations.get(1).getLabel());
    result.getItems().stream()
        .filter(item -> !item.getShortname().equals("c"))
        .forEach(item -> assertNull(item.getOrganisation()));
  }

  @Test
  void pages() {
    seedThree();
    HomeSearchRequest request = new HomeSearchRequest();
    request.setLimit(2);
    request.setOffset(2);
    HomeSearchResult result = manager.searchPublishedPublicResources(request);
    assertEquals(List.of("c"), shortnames(result));
    assertEquals(3, result.getMatched());
  }

  @Test
  void itemFields() {
    ResourceSummaryView a = resource("a", "metadata", ORG_A, "CC-BY-NC-4.0", 0, 0);
    a.setGbifKey(UUID.fromString("33333333-3333-3333-3333-333333333333"));
    a.setDoi("10.1234/abc");
    a.setDoiUrl("https://doi.org/10.1234/abc");
    a.setSubtype("specimen");
    ResourceSummaryView b = resource("b", "occurrence", ORG_B, "CC0-1.0", 1, 12345);
    seed(a, b);

    Map<String, HomeSearchResult.Item> items = manager.searchPublishedPublicResources(new HomeSearchRequest())
        .getItems().stream().collect(Collectors.toMap(HomeSearchResult.Item::getShortname, item -> item));
    HomeSearchResult.Item item = items.get("a");
    assertTrue(item.isMetadataOnly());
    assertEquals("CC BY-NC 4.0", item.getLicenceLabel());
    assertEquals("https://www.gbif.org/dataset/33333333-3333-3333-3333-333333333333", item.getGbifUrl());
    assertEquals("Specimen", item.getSubtypeLabel());
    assertEquals("Metadata-only", item.getTypeLabel());
    assertEquals("Org 1", item.getOrganisation());
    assertEquals("12,345", items.get("b").getRecordsLabel());
    assertNull(items.get("b").getGbifUrl());
  }

  @Test
  void dateOfLastPublication() {
    ResourceSummaryView a = resource("a", "occurrence", ORG_A, "CC0-1.0", 0, 1);
    Calendar cal = Calendar.getInstance();
    cal.set(2026, Calendar.MARCH, 4, 12, 0, 0);
    a.setLastPublished(cal.getTime());
    seed(a);
    HomeSearchResult.Item item = manager.searchPublishedPublicResources(new HomeSearchRequest()).getItems().get(0);
    assertEquals("2026-03-04", item.getLastPublished());
    assertEquals("2026-03-04 12:00:00", item.getLastPublishedTime());
  }

  @Test
  void descriptionAsPlainText() {
    Resource resource = new Resource();
    resource.setShortname("a");
    Eml eml = new Eml();
    eml.setDescription("<para>First</para><para>Second <emphasis>bold</emphasis>ly &amp; more</para>");
    resource.setEml(eml);
    assertEquals("First Second boldly & more", ResourceManagerImpl.summaryDescription(resource));

    eml.setDescription("<para>" + "word ".repeat(200) + "</para>");
    String cut = ResourceManagerImpl.summaryDescription(resource);
    assertEquals(ResourceManagerImpl.SUMMARY_DESCRIPTION_LENGTH, cut.length());
    assertTrue(cut.endsWith("…"));

    eml.setDescription(null);
    assertNull(ResourceManagerImpl.summaryDescription(resource));
  }
}
