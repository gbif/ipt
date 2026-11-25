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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.HealthService;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runs the remote checks in the background: every 12 hours while they pass, and after a failure again in 5 minutes,
 * 15 minutes, 1 hour and then hourly until they pass. About a thousand IPTs call the GBIF services, and the URL
 * validator calls each IPT back, so the checks stay rare. Viewing the health page runs them too, unless they ran
 * only minutes ago.
 */
public class HealthServiceImpl implements HealthService {

  private static final Logger LOG = LogManager.getLogger(HealthServiceImpl.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final String REPOSITORY_URL = "http://rs.gbif.org";
  static final String PUBLIC_ACCESS_VALIDATOR_URL = "https://tools.gbif.org/ws-validurl/?url=";

  // Free space is measured against what publishing needs, not a share of the disk: 15% free is plenty on a 2 TB disk
  // and nothing on a 20 GB one, and a few GB are plenty for small resources. Publishing a resource needs room for its
  // data written out again and the new archive, so the largest resource sets the limit. These floors cover logs,
  // uploads and resources that haven't been published yet.
  static final long DISK_FULL_FLOOR_BYTES = 500L * FileUtils.ONE_MB;
  static final long DISK_LOW_FLOOR_BYTES = 2L * FileUtils.ONE_GB;
  static final String DISK_OK = "ok";
  static final String DISK_LOW = "warn";
  static final String DISK_FULL = "bad";

  static final long PASSED_MS = TimeUnit.HOURS.toMillis(12);
  static final long PASSED_JITTER_MS = TimeUnit.HOURS.toMillis(1);
  static final List<Long> FAILED_MS = List.of(
      TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(15), TimeUnit.HOURS.toMillis(1));
  // before setup there is no public URL yet
  static final long NOT_SET_UP_MS = TimeUnit.MINUTES.toMillis(15);
  // the resources are loaded by then; random, so IPTs restarted together don't call at once
  private static final long FIRST_MIN_MS = TimeUnit.MINUTES.toMillis(1);
  private static final long FIRST_MAX_MS = TimeUnit.MINUTES.toMillis(5);
  // a health page view reuses checks this recent, so reloading it doesn't call GBIF again
  static final long PAGE_VIEW_MAX_AGE_MS = TimeUnit.MINUTES.toMillis(5);
  // the menu shows the icon on every admin page: the folder scan runs at most this often
  static final long LOCAL_MAX_AGE_MS = TimeUnit.MINUTES.toMillis(1);

  private final AppConfig cfg;
  private final DataDir dataDir;
  private final HttpClient http;
  private final RegistrationManager registrationManager;
  private final ResourceManager resourceManager;
  LongSupplier clock = System::currentTimeMillis;

  private volatile RemoteChecks remote;
  private volatile LocalChecks local;
  private volatile long localChecked;
  // one run at a time; its own lock, so start/stop never wait for network calls (up to 30 s each)
  private final Object refreshLock = new Object();
  private int failuresInRow;
  private ScheduledExecutorService executor;
  private ScheduledFuture<?> next;

  public HealthServiceImpl(AppConfig cfg, DataDir dataDir, HttpClient http, RegistrationManager registrationManager,
      ResourceManager resourceManager) {
    this.cfg = cfg;
    this.dataDir = dataDir;
    this.http = http;
    this.registrationManager = registrationManager;
    this.resourceManager = resourceManager;
  }

  /**
   * Schedules the first remote checks (Spring init-method).
   */
  public synchronized void start() {
    if (executor != null) {
      return;
    }
    executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "health-checks");
      // never keeps the IPT from shutting down
      t.setDaemon(true);
      return t;
    });
    schedule(ThreadLocalRandom.current().nextLong(FIRST_MIN_MS, FIRST_MAX_MS));
  }

  /**
   * Stops the background thread (Spring destroy-method).
   */
  public synchronized void stop() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
      next = null;
    }
  }

  private synchronized void schedule(long delayMs) {
    if (executor == null || executor.isShutdown()) {
      return;
    }
    if (next != null) {
      next.cancel(false);
    }
    next = executor.schedule(this::scheduledRun, delayMs, TimeUnit.MILLISECONDS);
    LOG.debug("Next health checks in {} min", TimeUnit.MILLISECONDS.toMinutes(delayMs));
  }

  private void scheduledRun() {
    try {
      refreshRemoteChecks(true);
    } catch (RuntimeException e) {
      LOG.error("Health checks failed", e);
      schedule(FAILED_MS.get(FAILED_MS.size() - 1));
    }
  }

  @Nullable
  @Override
  public RemoteChecks refreshRemoteChecks(boolean force) {
    synchronized (refreshLock) {
      // a request that waited for a run in progress gets its result
      RemoteChecks last = remote;
      if (!force && last != null && clock.getAsLong() - last.checked() < PAGE_VIEW_MAX_AGE_MS) {
        return last;
      }
      if (!dataDir.isConfigured() || StringUtils.isBlank(cfg.getBaseUrl())) {
        schedule(NOT_SET_UP_MS);
        return null;
      }
      RemoteChecks checks = runRemoteChecks();
      remote = checks;
      boolean passed = passed(checks, isRegistered());
      failuresInRow = passed ? 0 : failuresInRow + 1;
      if (!passed) {
        LOG.info("Health checks: GBIF Registry {}, schema repository {}, public access {}",
            checks.registry() ? "reachable" : "not reachable", checks.repository() ? "reachable" : "not reachable",
            !checks.publicAccessChecked() ? "not checked" : checks.publicAccess() ? "reachable" : "not reachable");
      }
      schedule(nextDelay(failuresInRow));
      return checks;
    }
  }

  RemoteChecks runRemoteChecks() {
    String registryUrl = cfg.getRegistryUrl();
    boolean registry = reachable(registryUrl);
    boolean repository = reachable(REPOSITORY_URL);

    String publicUrl = cfg.getBaseUrl();
    PublicAccessCheck check;
    try {
      String checkUrl = PUBLIC_ACCESS_VALIDATOR_URL + publicUrl;
      ExtendedResponse resp = http.get(checkUrl);
      LOG.debug("Public access check: url={}, status={}, body={}",
          checkUrl, resp != null ? resp.getStatusCode() : null, resp != null ? resp.getContent() : null);
      check = readPublicAccessResponse(resp, publicUrl);
    } catch (Exception e) {
      LOG.error("Public access check failed for {}", publicUrl, e);
      check = new PublicAccessCheck(false, false, "ERROR",
          "Could not run the public access check. See logs for details.");
    }
    return new RemoteChecks(registryUrl, registry, REPOSITORY_URL, repository, publicUrl, check.checked(),
        check.reachable(), check.status(), check.message(), clock.getAsLong());
  }

  private boolean reachable(String url) {
    try {
      ExtendedResponse resp = http.get(url);
      return resp != null && resp.getStatusCode() == 200;
    } catch (Exception e) {
      LOG.debug("{} not reachable: {}", url, e.getMessage());
      return false;
    }
  }

  @Override
  public LocalChecks checkLocal() {
    long total = dataDir.getDataDirTotalSpace();
    long free = dataDir.getDataDirUsableSpace();
    String largest = null;
    long need = 0;
    for (Resource resource : resourceManager.list()) {
      long resourceNeed = publishingNeed(resource);
      if (resourceNeed > need) {
        need = resourceNeed;
        largest = resource.getShortname();
      }
    }
    LocalChecks checks = new LocalChecks(total, free, diskLevel(free, need), need, largest,
        dataDir.getDirectoryReadWriteStatus(dataDir.configDir()),
        dataDir.getDirectoryReadWriteStatus(dataDir.loggingDir()),
        dataDir.getDirectoryReadWriteStatus(dataDir.tmpRootDir()),
        dataDir.getDirectoryReadWriteStatus(dataDir.resourcesDir()),
        dataDir.getSubDirectoriesReadWriteStatus(dataDir.resourcesDir()));
    local = checks;
    localChecked = clock.getAsLong();
    return checks;
  }

  @Override
  public Level getLevel() {
    if (!dataDir.isConfigured()) {
      return Level.OK;
    }
    LocalChecks checks = local;
    if (checks == null || clock.getAsLong() - localChecked >= LOCAL_MAX_AGE_MS) {
      checks = checkLocal();
    }
    return level(checks, remote, isRegistered());
  }

  private boolean isRegistered() {
    return registrationManager.getIpt() != null;
  }

  /**
   * @param registered public access only counts for a registered IPT: an installation on a local network can't be
   *                   reached from the internet and doesn't need to be
   */
  static Level level(LocalChecks local, @Nullable RemoteChecks remote, boolean registered) {
    boolean dirsOk = local.config() == DataDir.DirStatus.READ_WRITE
        && local.log() == DataDir.DirStatus.READ_WRITE
        && local.tmp() == DataDir.DirStatus.READ_WRITE
        && local.resources() == DataDir.DirStatus.READ_WRITE
        && local.subResources() == DataDir.DirStatus.READ_WRITE;
    if (!dirsOk || DISK_FULL.equals(local.diskLevel())) {
      return Level.BAD;
    }
    if (DISK_LOW.equals(local.diskLevel()) || (remote != null && !passed(remote, registered))) {
      return Level.WARN;
    }
    return Level.OK;
  }

  static boolean passed(RemoteChecks remote, boolean registered) {
    return remote.registry() && remote.repository() && (!registered || remote.publicAccess());
  }

  /**
   * @param failuresInRow 0 after checks that passed
   *
   * @return milliseconds until the next checks
   */
  static long nextDelay(int failuresInRow) {
    if (failuresInRow <= 0) {
      return PASSED_MS + ThreadLocalRandom.current().nextLong(PASSED_JITTER_MS);
    }
    return FAILED_MS.get(Math.min(failuresInRow, FAILED_MS.size()) - 1);
  }

  /**
   * Estimates the space publishing a resource needs: its source files (written out again as data files) plus its last
   * archive (the new one is about as big). SQL sources have no file size, so for them only the archive counts.
   * Uses sizes the IPT already knows, no folder scan.
   */
  long publishingNeed(Resource resource) {
    long need = 0;
    for (Source source : resource.getSources()) {
      if (source instanceof FileSource fileSource) {
        need += Math.max(fileSource.getFileSize(), 0);
      }
    }
    BigDecimal version = resource.getLastPublishedVersionsVersion();
    if (version != null) {
      File archive = dataDir.resourceArchiveFile(resource, version);
      // 0 if it doesn't exist
      need += archive.length();
    }
    return need;
  }

  /**
   * @param freeBytes usable space left on the data directory's disk
   * @param needBytes space publishing the largest resource needs
   *
   * @return "bad" (almost full) when the largest resource can't be published, "warn" (running low) when it can be
   * only once more, else "ok"
   */
  static String diskLevel(long freeBytes, long needBytes) {
    if (freeBytes < Math.max(needBytes, DISK_FULL_FLOOR_BYTES)) {
      return DISK_FULL;
    }
    return freeBytes < Math.max(2 * needBytes, DISK_LOW_FLOOR_BYTES) ? DISK_LOW : DISK_OK;
  }

  /**
   * The validator's answer about the IPT's public URL.
   *
   * @param checked   true when the validator ran; false when it didn't answer, failed or answered something unreadable
   * @param reachable true when the validator could open the URL
   */
  record PublicAccessCheck(boolean checked, boolean reachable, String status, String message) {
  }

  /**
   * Reads a ws-validurl response. Only a readable answer from the validator says something about the IPT; a missing or
   * failed answer means the check couldn't run.
   */
  static PublicAccessCheck readPublicAccessResponse(ExtendedResponse resp, String publicUrl) {
    if (resp == null) {
      return new PublicAccessCheck(false, false, "NO_RESPONSE", "No response from the URL validation service.");
    }
    if (resp.getStatusCode() != 200) {
      return new PublicAccessCheck(false, false, String.valueOf(resp.getStatusCode()),
          "The URL validation service returned HTTP " + resp.getStatusCode() + ".");
    }

    JsonNode json;
    try {
      json = MAPPER.readTree(StringUtils.defaultString(resp.getContent()));
    } catch (Exception e) {
      json = null;
    }
    if (json == null || !json.isObject()) {
      return new PublicAccessCheck(false, false, "BAD_RESPONSE",
          "The URL validation service returned a response that could not be read.");
    }

    if (json.path("success").asBoolean(false)) {
      return new PublicAccessCheck(true, true, "", "");
    }

    // the validator couldn't reach the IPT, or the IPT answered with an error status
    String status = jsonString(json, "status");
    String error = jsonString(json, "error");
    if (error.isEmpty()) {
      error = jsonString(json, "message");
    }
    String message;
    if ("NonPublicAddress".equals(error)) {
      // validurl-ws refuses localhost and private network addresses
      message = publicUrl + " points to this computer or a private network, which the internet cannot reach.";
    } else if (!status.isEmpty()) {
      message = "IPT responded to the validator with HTTP " + status + ".";
    } else if (!error.isEmpty()) {
      message = error;
    } else {
      message = "The validator could not reach " + publicUrl
          + ". Check that the base URL is correct and reachable from the public internet.";
    }
    return new PublicAccessCheck(true, false, status, message);
  }

  private static String jsonString(JsonNode json, String key) {
    JsonNode el = json.get(key);
    return (el != null && !el.isNull()) ? el.asText() : "";
  }
}
