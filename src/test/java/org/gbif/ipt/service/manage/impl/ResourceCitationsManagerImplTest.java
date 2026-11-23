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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceCitationsManagerImplTest {

  private static final String API = "https://api.example.org/v1";
  private static final Pattern DATASET_KEY = Pattern.compile("gbifDatasetKey=([0-9a-f-]{36})");

  @TempDir
  Path dir;

  private AppConfig cfg;
  private DataDir dataDir;
  private ResourceManager resourceManager;
  private HttpClient http;
  private final List<Resource> resources = new ArrayList<>();
  private final List<String> requested = new ArrayList<>();

  @BeforeEach
  public void setUp() {
    cfg = mock(AppConfig.class);
    when(cfg.getApiUrl()).thenReturn(API + "/");
    dataDir = mock(DataDir.class);
    when(dataDir.isConfigured()).thenReturn(true);
    when(dataDir.configFile(anyString())).thenAnswer(inv -> dir.resolve((String) inv.getArgument(0)).toFile());
    resourceManager = mock(ResourceManager.class);
    when(resourceManager.list()).thenReturn(resources);
    http = mock(HttpClient.class);
  }

  private ResourceCitationsManagerImpl manager() {
    ResourceCitationsManagerImpl m = new ResourceCitationsManagerImpl(cfg, dataDir, resourceManager, http);
    m.pauseMs = 0;
    return m;
  }

  private static Resource resource(UUID key, PublicationStatus status) {
    Resource r = new Resource();
    r.setShortname(key == null ? "private" : "r" + key);
    r.setKey(key);
    r.setStatus(status);
    return r;
  }

  private static UUID key(int i) {
    return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", i));
  }

  /**
   * Answers every GET with the given function: a JSON body (status 200), or null for a status 500.
   */
  private void answer(Function<String, String> body) throws Exception {
    when(http.get(anyString())).thenAnswer(inv -> {
      String url = inv.getArgument(0);
      requested.add(url);
      String content = body.apply(url);
      ExtendedResponse resp = mock(ExtendedResponse.class);
      when(resp.getStatusCode()).thenReturn(content == null ? 500 : 200);
      when(resp.getContent()).thenReturn(content);
      return resp;
    });
  }

  private static String count(long n) {
    return "{\"offset\":0,\"limit\":0,\"count\":" + n + ",\"results\":[]}";
  }

  private static List<String> keysIn(String url) {
    List<String> keys = new ArrayList<>();
    Matcher m = DATASET_KEY.matcher(url);
    while (m.find()) {
      keys.add(m.group(1));
    }
    return keys;
  }

  @Test
  public void fetchesRegisteredResourcesOnlyAndStoresThem() throws Exception {
    resources.add(resource(key(1), PublicationStatus.REGISTERED));
    resources.add(resource(key(2), PublicationStatus.REGISTERED));
    resources.add(resource(key(3), PublicationStatus.PUBLIC));
    resources.add(resource(null, PublicationStatus.PRIVATE));
    answer(url -> {
      List<String> keys = keysIn(url);
      // two keys in one search: the total, each paper once
      if (keys.size() == 2) return count(4);
      if (keys.equals(List.of(key(1).toString()))) return count(2);
      if (keys.equals(List.of(key(2).toString()))) return count(3);
      return null;
    });

    ResourceCitationsManagerImpl m = manager();
    assertTrue(m.refresh());
    assertEquals(2L, m.getCitations(key(1)));
    assertEquals(3L, m.getCitations(key(2)));
    assertNull(m.getCitations(key(3)));
    assertNull(m.getCitations(null));
    // a search per resource and one for the total
    assertEquals(3, requested.size());
    assertEquals(4L, m.getCitationsTotal());
    // the base URL's trailing slash is dropped
    assertTrue(requested.stream().allMatch(u -> u.startsWith(API + "/") && !u.startsWith(API + "//")));
    assertFalse(requested.stream().anyMatch(u -> u.contains(key(3).toString())));

    // a new instance reads the stored counts without calling the API
    ResourceCitationsManagerImpl reloaded =
        new ResourceCitationsManagerImpl(cfg, dataDir, resourceManager, mock(HttpClient.class));
    reloaded.load();
    assertEquals(2L, reloaded.getCitations(key(1)));
    assertEquals(4L, reloaded.getCitationsTotal());
    assertTrue(reloaded.snapshot().fetched() > 0);
  }

  @Test
  public void noDataDirStartsWithoutTouchingIt() throws Exception {
    when(dataDir.isConfigured()).thenReturn(false);
    when(dataDir.configFile(anyString())).thenThrow(new IllegalStateException("No data dir has been configured yet"));
    resources.add(resource(key(1), PublicationStatus.REGISTERED));
    ResourceCitationsManagerImpl m = manager();
    m.start();
    m.stop();
    assertFalse(m.refresh());
    verify(http, never()).get(anyString());
  }

  @Test
  public void noRegisteredResourcesMeansNoTotal() throws Exception {
    resources.add(resource(key(3), PublicationStatus.PUBLIC));
    ResourceCitationsManagerImpl m = manager();
    assertTrue(m.refresh());
    assertNull(m.getCitationsTotal());
    verify(http, never()).get(anyString());
  }

  @Test
  public void oneFailingResourceKeepsItsPreviousCounts() throws Exception {
    resources.add(resource(key(1), PublicationStatus.REGISTERED));
    resources.add(resource(key(2), PublicationStatus.REGISTERED));
    answer(url -> count(5));
    ResourceCitationsManagerImpl m = manager();
    assertTrue(m.refresh());

    answer(url -> url.contains(key(1).toString()) && !url.contains(key(2).toString()) ? null : count(9));
    assertTrue(m.refresh());
    assertEquals(5L, m.getCitations(key(1)));
    assertEquals(9L, m.getCitations(key(2)));
    assertEquals(9L, m.getCitationsTotal());
  }

  @Test
  public void unreachableApiStopsTheRoundAndKeepsEverything() throws Exception {
    for (int i = 1; i <= 5; i++) {
      resources.add(resource(key(i), PublicationStatus.REGISTERED));
    }
    answer(url -> count(1));
    ResourceCitationsManagerImpl m = manager();
    assertTrue(m.refresh());
    long fetched = m.snapshot().fetched();

    answer(url -> null);
    // stubbing calls the mock once more
    requested.clear();
    assertFalse(m.refresh());
    // stops after three resources in a row failed, without asking for the total
    assertEquals(ResourceCitationsManagerImpl.MAX_FAILURES_IN_ROW, requested.size());
    for (int i = 1; i <= 5; i++) {
      assertEquals(1L, m.getCitations(key(i)));
    }
    assertEquals(1L, m.getCitationsTotal());
    assertEquals(fetched, m.snapshot().fetched());
  }

  @Test
  public void totalOverSeveralSearchesCountsEachPaperOnce() throws Exception {
    // 60 keys: searches of 50 and 10; "p0" cites a resource in each; the first search has two pages
    List<UUID> keys = IntStream.rangeClosed(1, 60).mapToObj(ResourceCitationsManagerImplTest::key).toList();
    answer(url -> {
      boolean first = keysIn(url).contains(key(1).toString());
      boolean secondPage = url.contains("offset=" + ResourceCitationsManagerImpl.PAGE_SIZE + "&");
      List<String> ids = first ? (secondPage ? List.of("p2") : List.of("p0", "p1")) : List.of("p0", "p3");
      return "{\"endOfRecords\":" + !(first && !secondPage) + ",\"results\":["
          + ids.stream().map(id -> "{\"id\":\"" + id + "\"}").collect(Collectors.joining(",")) + "]}";
    });
    ResourceCitationsManagerImpl m = manager();
    assertEquals(4, m.total(API, keys, Map.of()));
    assertEquals(3, requested.size());
    assertTrue(requested.stream().allMatch(u -> keysIn(u).size() <= ResourceCitationsManagerImpl.KEYS_PER_SEARCH));
  }

  @Test
  public void oneResourceNeedsNoSearchForTheTotal() throws Exception {
    resources.add(resource(key(1), PublicationStatus.REGISTERED));
    answer(url -> count(6));
    ResourceCitationsManagerImpl m = manager();
    assertTrue(m.refresh());
    assertEquals(6L, m.getCitationsTotal());
    assertEquals(1, requested.size());
  }

  @Test
  public void brokenFileIsIgnored() throws IOException {
    File file = dir.resolve(ResourceCitationsManagerImpl.CITATIONS_FILE).toFile();
    Files.writeString(file.toPath(), "{not json", StandardCharsets.UTF_8);
    ResourceCitationsManagerImpl m = manager();
    m.load();
    assertNull(m.getCitationsTotal());
    assertEquals(0, m.snapshot().fetched());
  }
}
