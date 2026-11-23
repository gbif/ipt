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
import org.gbif.ipt.service.manage.ResourceCitationsManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fetches the citations of the registered resources from the GBIF literature API on one background thread: 5-30 min
 * after start-up, then about once a day, one resource at a time. The counts are stored in
 * {@code config/gbif-citations.json}, so a restart doesn't fetch them again before they are a day old.
 * <p>
 * A resource that fails keeps its previous count. After several failures in a row (API unreachable) the round stops,
 * all previous counts are kept and the round is retried an hour later.
 */
public class ResourceCitationsManagerImpl implements ResourceCitationsManager {

  private static final Logger LOG = LogManager.getLogger(ResourceCitationsManagerImpl.class);
  // a file written by a later version (more fields) is still read
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  static final String CITATIONS_FILE = "gbif-citations.json";
  // the API answers 400 to more than about 50 dataset keys in one search (URL length)
  static final int KEYS_PER_SEARCH = 50;
  static final int PAGE_SIZE = 1000;
  static final int MAX_FAILURES_IN_ROW = 3;

  private static final long DAY_MS = TimeUnit.DAYS.toMillis(1);
  private static final long RETRY_MS = TimeUnit.HOURS.toMillis(1);
  // the resources are loaded by then; random, so IPTs don't all call at once
  private static final long FIRST_MIN_MS = TimeUnit.MINUTES.toMillis(5);
  private static final long FIRST_MAX_MS = TimeUnit.MINUTES.toMillis(30);

  private final AppConfig cfg;
  private final DataDir dataDir;
  private final ResourceManager resourceManager;
  private final HttpClient http;
  // pause between two API calls, to keep the load low
  long pauseMs = 500;

  private volatile Snapshot snapshot = new Snapshot(Collections.emptyMap(), null, 0);
  private ScheduledExecutorService executor;

  /**
   * The counts of the last round. fetched: when, in epoch milliseconds (0: never)
   */
  record Snapshot(Map<UUID, Long> citations, @Nullable Long total, long fetched) {
  }

  /**
   * The content of {@link #CITATIONS_FILE}.
   */
  record StoredCitations(long fetched, @Nullable Long total, Map<UUID, Long> resources) {
  }

  public ResourceCitationsManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager,
      HttpClient http) {
    this.cfg = cfg;
    this.dataDir = dataDir;
    this.resourceManager = resourceManager;
    this.http = http;
  }

  @Nullable
  @Override
  public Long getCitations(@Nullable UUID key) {
    return key == null ? null : snapshot.citations().get(key);
  }

  @Nullable
  @Override
  public Long getCitationsTotal() {
    return snapshot.total();
  }

  /**
   * Loads the stored counts and schedules the first round (Spring init-method).
   */
  public synchronized void start() {
    if (executor != null) {
      return;
    }
    load();
    executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "gbif-citations");
      // never keeps the IPT from shutting down
      t.setDaemon(true);
      return t;
    });
    // a stored date in the future (clock changed) counts as just fetched
    long age = Math.max(System.currentTimeMillis() - snapshot.fetched(), 0);
    long first = Math.max(DAY_MS - age, 0) + ThreadLocalRandom.current().nextLong(FIRST_MIN_MS, FIRST_MAX_MS);
    executor.schedule(this::roundAndReschedule, first, TimeUnit.MILLISECONDS);
    LOG.debug("GBIF citations will be fetched in {} min", TimeUnit.MILLISECONDS.toMinutes(first));
  }

  /**
   * Stops the background thread (Spring destroy-method).
   */
  public synchronized void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }

  private void roundAndReschedule() {
    boolean done;
    try {
      done = refresh();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    } catch (RuntimeException e) {
      LOG.error("Fetching GBIF citations failed", e);
      done = false;
    }
    long next = done ? DAY_MS + ThreadLocalRandom.current().nextLong(TimeUnit.HOURS.toMillis(1)) : RETRY_MS;
    synchronized (this) {
      if (executor != null && !executor.isShutdown()) {
        executor.schedule(this::roundAndReschedule, next, TimeUnit.MILLISECONDS);
      }
    }
  }

  /**
   * One round: the citations of every registered resource, then the total; stored when done.
   *
   * @return false if the round stopped because the API couldn't be reached
   */
  boolean refresh() throws InterruptedException {
    // before setup: nothing registered and nowhere to store, try again later
    if (!dataDir.isConfigured()) {
      return false;
    }
    String api = StringUtils.removeEnd(StringUtils.trimToNull(cfg.getApiUrl()), "/");
    if (api == null) {
      return true;
    }
    List<UUID> keys = registeredKeys();
    Snapshot previous = snapshot;
    Map<UUID, Long> citations = new HashMap<>();
    int failuresInRow = 0;
    boolean stopped = false;
    for (UUID key : keys) {
      if (stopped) {
        keepPrevious(previous, citations, key);
        continue;
      }
      try {
        citations.put(key, count(searchUrl(api, List.of(key), 0, 0)));
        failuresInRow = 0;
      } catch (IOException e) {
        LOG.debug("Couldn't fetch the GBIF citations of {}: {}", key, e.getMessage());
        keepPrevious(previous, citations, key);
        if (++failuresInRow >= MAX_FAILURES_IN_ROW) {
          LOG.warn("Couldn't reach the GBIF API at {}, trying again later: {}", api, e.getMessage());
          stopped = true;
        }
      }
      pause();
    }

    Long total = previous.total();
    if (!stopped) {
      try {
        total = keys.isEmpty() ? null : total(api, keys, citations);
      } catch (IOException e) {
        LOG.warn("Couldn't fetch the GBIF citations total: {}", e.getMessage());
        stopped = true;
      }
    }

    snapshot = new Snapshot(Collections.unmodifiableMap(citations), total,
        stopped ? previous.fetched() : System.currentTimeMillis());
    save();
    return !stopped;
  }

  private static void keepPrevious(Snapshot previous, Map<UUID, Long> citations, UUID key) {
    Long old = previous.citations().get(key);
    if (old != null) {
      citations.put(key, old);
    }
  }

  private List<UUID> registeredKeys() {
    return resourceManager.list().stream()
        .filter(Resource::isRegistered)
        .map(Resource::getKey)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .toList();
  }

  /**
   * The papers citing any of the keys, each counted once. A search with several keys counts a paper once, but takes
   * at most {@link #KEYS_PER_SEARCH} keys; with more, the IDs of the papers found by each search are collected.
   *
   * @param citations the counts of single resources just fetched, reused when there is only one key
   */
  long total(String api, List<UUID> keys, Map<UUID, Long> citations) throws IOException, InterruptedException {
    if (keys.size() == 1 && citations.containsKey(keys.get(0))) {
      return citations.get(keys.get(0));
    }
    if (keys.size() <= KEYS_PER_SEARCH) {
      return count(searchUrl(api, keys, 0, 0));
    }
    Set<String> ids = new HashSet<>();
    for (int i = 0; i < keys.size(); i += KEYS_PER_SEARCH) {
      List<UUID> chunk = keys.subList(i, Math.min(i + KEYS_PER_SEARCH, keys.size()));
      for (int offset = 0; ; offset += PAGE_SIZE) {
        JsonNode page = getJson(searchUrl(api, chunk, offset, PAGE_SIZE));
        JsonNode results = page.path("results");
        if (!results.isArray() || results.isEmpty()) {
          break;
        }
        for (JsonNode r : results) {
          if (r.path("id").isTextual()) {
            ids.add(r.path("id").asText());
          }
        }
        pause();
        if (page.path("endOfRecords").asBoolean(true)) {
          break;
        }
      }
    }
    return ids.size();
  }

  private static String searchUrl(String api, List<UUID> keys, int offset, int limit) {
    StringBuilder url = new StringBuilder(api).append("/literature/search?offset=").append(offset)
        .append("&limit=").append(limit);
    keys.forEach(k -> url.append("&gbifDatasetKey=").append(k));
    return url.toString();
  }

  private void pause() throws InterruptedException {
    if (pauseMs > 0) {
      Thread.sleep(pauseMs);
    }
  }

  private long count(String url) throws IOException {
    JsonNode count = getJson(url).path("count");
    if (!count.canConvertToLong()) {
      throw new IOException("No count in the response of " + url);
    }
    return count.asLong();
  }

  private JsonNode getJson(String url) throws IOException {
    ExtendedResponse resp;
    try {
      resp = http.get(url);
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    if (resp == null || resp.getStatusCode() != 200 || resp.getContent() == null) {
      throw new IOException("HTTP " + (resp == null ? "?" : resp.getStatusCode()) + " from " + url);
    }
    JsonNode json = MAPPER.readTree(resp.getContent());
    if (json == null || !json.isObject()) {
      throw new IOException("Not a JSON object from " + url);
    }
    return json;
  }

  private File citationsFile() {
    return dataDir.configFile(CITATIONS_FILE);
  }

  /**
   * Reads the stored counts; a missing or broken file means nothing fetched yet.
   */
  void load() {
    // before setup there is no data dir to read from
    if (!dataDir.isConfigured()) {
      return;
    }
    File file = citationsFile();
    if (!file.exists()) {
      return;
    }
    try {
      StoredCitations stored = MAPPER.readValue(file, StoredCitations.class);
      Map<UUID, Long> citations = stored.resources() == null ? Map.of() : Map.copyOf(stored.resources());
      snapshot = new Snapshot(citations, stored.total(), stored.fetched());
    } catch (IOException | RuntimeException e) {
      LOG.warn("Couldn't read {}, the GBIF citations will be fetched again: {}", file, e.getMessage());
    }
  }

  private void save() {
    Snapshot s = snapshot;
    StoredCitations stored = new StoredCitations(s.fetched(), s.total(), new TreeMap<>(s.citations()));
    File file = citationsFile();
    File tmp = new File(file.getParentFile(), CITATIONS_FILE + ".tmp");
    try {
      MAPPER.writeValue(tmp, stored);
      Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      LOG.warn("Couldn't store the GBIF citations in {}: {}", file, e.getMessage());
    }
  }

  Snapshot snapshot() {
    return snapshot;
  }
}
