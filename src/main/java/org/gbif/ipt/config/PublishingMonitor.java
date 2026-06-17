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
package org.gbif.ipt.config;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.PublicationOptions;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.PublicationFailureEmailUtils;

import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to start a monitor thread which is responsible for auto-publishing resources when they are due,
 * and which ensures publication always finishes entirely.
 */
public class PublishingMonitor {

  // 10-seconds interval
  public static final int MONITOR_INTERVAL_MS = 10000;
  // wait at least this long after a failed auto-publication before retrying, to avoid re-attempting
  // publication on every monitor cycle right after a failure
  public static final int RETRY_DELAY_MS = 3 * 60 * 1000;
  private static Thread monitorThread;
  private static final Logger LOG = LogManager.getLogger(PublishingMonitor.class);
  private AtomicBoolean running;
  private final ResourceManager resourceManager;
  private final AppConfig cfg;
  private final BaseAction baseAction;
  private final Map<String, LocalDate> lastLoggedSkips = new ConcurrentHashMap<>();

  @Inject
  public PublishingMonitor(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
    this.cfg = cfg;
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  /**
   * Polls the queue and launches threads if possible.
   */
  class QueueMonitor implements Runnable {

    public QueueMonitor() {
      running = new AtomicBoolean();
    }

    @Override
    public void run() {
      running.set(true);
      while (running.get()) {
        try {
          monitorOnce();
          // just poll once every 'interval' millisecond
          Thread.sleep(MONITOR_INTERVAL_MS);
        } catch (InterruptedException e) {
          // should the thread have been interrupted, encountered when trying to sleep
          LOG.error("Monitor thread has been interrupted!", e);
        }
      }
    }
  }

  void monitorOnce() {
    // monitor resources that are currently being published or have finished
    Map<String, Future<Map<String, Integer>>> processFutures = resourceManager.getProcessFutures();
    Set<String> shortNames = new HashSet<>();
    if (!processFutures.isEmpty()) {
      // copy futures into the new set, to avoid concurrent modification exception
      shortNames.addAll(processFutures.keySet());
      // in order for publishing to finish entirely, resourceManager.isLocked() must be called
      for (String shortName : shortNames) {
        resourceManager.isLocked(shortName, baseAction);
      }
    }

    // might as well check if we can handle more publishing jobs
    ThreadPoolExecutor executor = resourceManager.getExecutor();
    int availableSlots = executor.getMaximumPoolSize() - executor.getActiveCount();
    if (availableSlots > 0) {
      Date now = new Date();
      LocalDate today = LocalDate.now();
      List<Resource> resources = resourceManager.list();
      for (Resource resource : resources) {
        if (resource.usesAutoPublishing()) {
          Date next = resource.getNextPublished();
          BigDecimal nextVersion = new BigDecimal(resource.getNextVersion().toPlainString());
          if (next != null) {
            // ensure resource is due to auto-publication
            if (next.before(now)) {
              // ensure resource isn't already being published
              if (!shortNames.contains(resource.getShortname())) {
                // ensure resource has not exceeded the maximum number of publication failures
                if (resourceManager.hasMaxProcessFailures(resource)) {
                  // once the limit is reached, only log once per day to avoid flooding the logs every interval
                  if (shouldSkipExcessiveLogging(resource.getShortname(), today)) {
                    LOG.debug("Skipping auto-publication for [{}] since it has exceeded the maximum number of failed publish attempts. " +
                        "Please try to publish this resource individually to fix the problem(s)",
                        resource.getTitleAndShortname());
                    lastLoggedSkips.put(resource.getShortname(), today);
                  }
                } else if (isWithinRetryCooldown(resource, now)) {
                  // avoid logging every time for the duration of the cooldown
                  if (shouldSkipExcessiveLogging(resource.getShortname(), today)) {
                    // recently failed - wait before retrying instead of re-attempting on the next monitor cycle
                    LOG.debug("Skipping auto-publication for [{}] since it failed recently; waiting before retrying",
                        resource.getTitleAndShortname());
                  }
                } else {
                  try {
                    // reset version change summary
                    resource.setChangeSummary(null);

                    PublicationOptions options = PublicationOptions.builder()
                        .skipPublicationIfNotChanged(resource.isSkipPublicationIfNotChanged())
                        .skipPublicationIfRecordsDrop(resource.isSkipPublicationIfRecordsDrop())
                        .notifyPublicationFailure(resource.isNotifyPublicationFailure())
                        .recordsDropThreshold(resource.getRecordsDropThreshold())
                        .build();

                    LOG.debug("Monitor: {} v#{} due to be auto-published: {}",
                        resource.getTitleAndShortname(), nextVersion.toPlainString(), next);
                    resourceManager.publish(resource, nextVersion, null, options);
                  } catch (PublicationException e) {
                    if (PublicationException.TYPE.LOCKED == e.getType()) {
                      LOG.error("Monitor: {} cannot be auto-published, because it is currently being published",
                          resource.getTitleAndShortname());
                    } else {
                      // alert user publication failed
                      LOG.error("Publishing version #{} of resource {} failed: {}",
                          nextVersion.toPlainString(), resource.getTitleAndShortname(), e.getMessage());
                      // restore the previous version since publication was unsuccessful
                      resourceManager.restoreVersion(resource, nextVersion, null);
                      // keep track of how many failures on auto publication have happened
                      resourceManager.getProcessFailures().put(resource.getShortname(), new Date());
                      sendPublicationFailureEmail(resource, nextVersion, e.getMessage());
                    }
                  } catch (InvalidConfigException e) {
                    // with this type of error, the version cannot be rolled back - just alert user it failed
                    LOG.error("Publishing version #{} of resource {} failed: {}",
                        nextVersion.toPlainString(), resource.getShortname(), e.getMessage(), e);
                    sendPublicationFailureEmail(resource, nextVersion, e.getMessage());
                  }
                }
              } else {
                LOG.debug("Skipping auto-publication for [{}] since it is already in progress",
                    resource.getTitleAndShortname());
              }
            }
          }
        }

        // ensure resource is due to become public
        if (resource.getMakePublicDate() != null && resource.getMakePublicDate().before(now)) {
          try {
            resourceManager.visibilityToPublic(resource, null);
          } catch (Exception e) {
            LOG.error("Resource {} failed to go public automatically",
                resource.getShortname(), e);
          }
        }
      }
    }
  }

  /**
   * Determines whether a resource failed to auto-publish too recently to be retried yet. This prevents the monitor
   * from re-attempting publication on every cycle right after a failure, since a failed auto-publication does not
   * always push the next publication date into the future.
   *
   * @param resource resource to check
   * @param now current time
   * @return true if the resource's most recent failure happened within the retry delay window
   */
  private boolean isWithinRetryCooldown(Resource resource, Date now) {
    ListValuedMap<String, Date> processFailures = resourceManager.getProcessFailures();
    if (processFailures.containsKey(resource.getShortname())) {
      List<Date> failures = processFailures.get(resource.getShortname());
      if (!failures.isEmpty()) {
        Date lastFailure = failures.get(failures.size() - 1);
        return now.getTime() - lastFailure.getTime() < RETRY_DELAY_MS;
      }
    }
    return false;
  }

  private boolean shouldSkipExcessiveLogging(String shortname, LocalDate today) {
    LocalDate last = lastLoggedSkips.put(shortname, today);
    return last == null || !last.equals(today);
  }

  private void sendPublicationFailureEmail(Resource resource, BigDecimal version, String reason) {
    if (resource.isNotifyPublicationFailure() && PublicationFailureEmailUtils.isConfigured(cfg)) {
      try {
        PublicationFailureEmailUtils.send(cfg, resource, version, reason);
      } catch (MessagingException e) {
        LOG.error("Failed to send publication failure email for resource {}", resource.getShortname(), e);
      }
    }
  }

  /**
   * Start the publishing monitor thread itself.
   */
  private void startMonitorThread() {
    monitorThread = new Thread(new QueueMonitor());
    monitorThread.start();
    LOG.debug("The monitor thread has started.");
  }

  /**
   * Starts the publishing monitor once and only once.
   */
  public void start() {
    if (monitorThread != null) {
      if (!monitorThread.isAlive()) {
        startMonitorThread();
      } else {
        LOG.error("The monitor thread is already running");
      }
    } else {
      startMonitorThread();
    }
  }

  /**
   * Stops the publishing monitor.
   */
  public void stop() {
    running.set(false);
    monitorThread = null;
  }
}
