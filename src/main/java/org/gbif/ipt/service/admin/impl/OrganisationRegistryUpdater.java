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

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Updates the associated organisations' details (name, node, contact, etc.) from the GBIF Registry on one background
 * thread, once, 1-5 min after start-up, one organisation at a time. Start-up doesn't wait for the Registry. Details
 * rarely change, so there is no later round: an admin can update an organisation at any time from its page.
 * <p>
 * An organisation that can't be updated keeps its details. When the Registry can't be reached the round stops, the
 * remaining organisations keep theirs too, and the round is retried an hour later until one completes. Each
 * organisation that wasn't updated gets one log line; no start-up errors are added.
 */
public class OrganisationRegistryUpdater {

  private static final Logger LOG = LogManager.getLogger(OrganisationRegistryUpdater.class);

  private static final long RETRY_MS = TimeUnit.HOURS.toMillis(1);
  // the resources are loaded by then; random, so IPTs restarted together don't call at once
  private static final long FIRST_MIN_MS = TimeUnit.MINUTES.toMillis(1);
  private static final long FIRST_MAX_MS = TimeUnit.MINUTES.toMillis(5);

  private final DataDir dataDir;
  private final RegistrationManager registrationManager;
  private final ResourceManager resourceManager;
  private ScheduledExecutorService executor;

  public OrganisationRegistryUpdater(DataDir dataDir, RegistrationManager registrationManager,
      ResourceManager resourceManager) {
    this.dataDir = dataDir;
    this.registrationManager = registrationManager;
    this.resourceManager = resourceManager;
  }

  /**
   * Schedules the first round (Spring init-method).
   */
  public synchronized void start() {
    if (executor != null) {
      return;
    }
    executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "organisation-updates");
      // never keeps the IPT from shutting down
      t.setDaemon(true);
      return t;
    });
    long first = ThreadLocalRandom.current().nextLong(FIRST_MIN_MS, FIRST_MAX_MS);
    executor.schedule(this::roundAndReschedule, first, TimeUnit.MILLISECONDS);
    LOG.debug("Organisations will be updated from the Registry in {} min", TimeUnit.MILLISECONDS.toMinutes(first));
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

  /**
   * Runs a round; retries it an hour later if it didn't complete, otherwise ends the thread.
   */
  private void roundAndReschedule() {
    boolean done;
    try {
      done = refresh();
    } catch (RuntimeException e) {
      LOG.error("Updating organisations from the Registry failed", e);
      done = false;
    }
    synchronized (this) {
      if (executor == null || executor.isShutdown()) {
        return;
      }
      if (done) {
        // nothing left to do: the thread ends after this task
        executor.shutdown();
        executor = null;
      } else {
        executor.schedule(this::roundAndReschedule, RETRY_MS, TimeUnit.MILLISECONDS);
      }
    }
  }

  /**
   * One round: every associated organisation, except the default "No organisation" that isn't in the Registry.
   *
   * @return false if the round stopped because the Registry couldn't be reached, or the IPT isn't set up yet
   */
  boolean refresh() {
    // before setup there are no organisations
    if (!dataDir.isConfigured()) {
      return false;
    }
    List<Organisation> organisations = registrationManager.listAll().stream()
        .filter(o -> o.getKey() != null && !Constants.DEFAULT_ORG_KEY.equals(o.getKey()))
        .toList();
    if (organisations.isEmpty()) {
      return true;
    }

    int updated = 0;
    String unreachable = null;
    for (Organisation organisation : organisations) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      String key = organisation.getKey().toString();
      if (unreachable != null) {
        LOG.warn("Organisation {} ({}) not updated from the Registry: it couldn't be reached",
            organisation.getName(), key);
        continue;
      }
      try {
        // not found: logged by the registration manager
        if (registrationManager.updateOrganisationFromRegistry(key)) {
          Organisation current = registrationManager.get(key);
          if (current != null) {
            resourceManager.updateOrganisationNameForResources(current);
          }
          updated++;
        }
      } catch (IllegalArgumentException e) {
        // removed by an admin during the round
        LOG.debug("Organisation {} was removed during the update from the Registry", key);
      } catch (RegistryException e) {
        if (stopsRound(e)) {
          unreachable = e.getMessage();
          LOG.warn("Organisation {} ({}) not updated from the Registry: it couldn't be reached ({}), trying again later",
              organisation.getName(), key, e.getMessage());
        } else {
          LOG.warn("Organisation {} ({}) not updated from the Registry: {}", organisation.getName(), key,
              e.getMessage());
        }
      } catch (IOException e) {
        LOG.error("Organisation {} ({}) updated from the Registry but not saved", organisation.getName(), key, e);
      }
    }
    LOG.info("Organisations updated from the Registry: {} of {}", updated, organisations.size());
    return unreachable == null;
  }

  /**
   * @return true when the Registry can't be reached at all, so the other organisations would fail the same way
   */
  private static boolean stopsRound(RegistryException e) {
    RegistryException.Type type = e.getType();
    return type != RegistryException.Type.BAD_RESPONSE && type != RegistryException.Type.BAD_REQUEST;
  }
}
