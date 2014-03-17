package org.gbif.ipt.config;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;

/**
 * Class used to start a monitor thread which is responsible for auto-publishing resources when they are due,
 * and which ensures publication always finishes entirely.
 */
@Singleton
public class PublishingMonitor {

  // 10 second interval
  public static final int MONITOR_INTERVAL_MS = 10000;
  private static Thread monitorThread;
  private static final Logger LOG = Logger.getLogger(PublishingMonitor.class);
  private AtomicBoolean running;
  private final ResourceManager resourceManager;
  private final BaseAction baseAction;

  @Inject
  public PublishingMonitor(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  /**
   * Polls the queue and launches threads if possible.
   */
  @Singleton
  class QueueMonitor implements Runnable {

    private ResourceManager resourceManager;

    public QueueMonitor(ResourceManager resourceManager) {
      this.resourceManager = resourceManager;
      running = new AtomicBoolean();
    }

    public void run() {
      running.set(true);
      while (running.get()) {
        try {
          // monitor resources that are currently being published or have finished
          Map<String, Future<Integer>> processFutures = resourceManager.getProcessFutures();
          Set<String> shortNames = new HashSet<String>();
          if (!processFutures.isEmpty()) {
            // copy futures into new set, to avoid concurrent modification exception
            shortNames.addAll(processFutures.keySet());
            // in order for publishing to finish entirely, resourceManager.isLocked() must be called
            for (String shortName : shortNames) {
              resourceManager.isLocked(shortName, baseAction);
            }
          }

          // might as well check if we can handle more publishing jobs
          ThreadPoolExecutor executor = resourceManager.getExecutor();
          if (executor.getMaximumPoolSize() - executor.getActiveCount() > 0) {
            Date now = new Date();
            List<Resource> resources = resourceManager.list();
            for (Resource resource : resources) {
              Date next = resource.getNextPublished();
              int v = resource.getNextVersion();
              if (next != null) {
                // ensure resource is due to be auto-published
                if (next.before(now)) {
                  // ensure resource isn't already being published
                  if (!shortNames.contains(resource.getShortname())) {
                    // ensure resource has not exceeded the maximum number of publication failures
                    if (!resourceManager.hasMaxProcessFailures(resource)) {
                      try {
                        LOG.debug(
                          "Monitor: " + resource.getTitleAndShortname() + " v# " + v + " due to be auto-published: "
                            + next
                              .toString());
                        resourceManager.publish(resource, v, null);
                      } catch (PublicationException e) {
                        if (PublicationException.TYPE.LOCKED == e.getType()) {
                          LOG.error("Monitor: " + resource.getTitleAndShortname()
                            + " cannot be auto-published, because "
                            + "it is currently being published");
                        } else {
                          // alert user publication failed
                          LOG.error(
                            "Publishing version #" + String.valueOf(v) + " of resource "
                              + resource.getTitleAndShortname()
                              + " failed: " + e.getMessage());
                          // restore the previous version since publication was unsuccessful
                          resourceManager.restoreVersion(resource, resource.getLastVersion(), null);
                          // keep track of how many failures on auto publication have happened
                          resourceManager.getProcessFailures().put(resource.getShortname(), new Date());
                        }
                      } catch (InvalidConfigException e) {
                        // with this type of error, the version cannot be rolled back - just alert user publication
// failed
                        LOG.error(
                          "Publishing version #" + String.valueOf(v) + "of resource " + resource.getShortname()
                            + "failed:"
                            + e.getMessage(), e);
                      }

                    } else {
                      LOG.debug("Skipping auto-publication for [" + resource.getTitleAndShortname()
                        + "] since it has exceeded the maximum number of failed publish attempts. Please try "
                        + "to publish this resource individually to fix the problem(s)");
                    }
                  } else {
                    LOG.debug("Skipping auto-publication for [" + resource.getTitleAndShortname()
                      + "] since it is already in progress");
                  }
                }
              }
            }
          }

          // just poll once every 'interval' milliseconds
          Thread.sleep(MONITOR_INTERVAL_MS);
        } catch (InterruptedException e) {
          // should the thread have been interrupted, encountered when trying to sleep
          LOG.error("Monitor thread has been interrupted!", e);
        }
      }
    }
  }

  /**
   * Start the publishing monitor thread itself.
   */
  private void startMonitorThread() {
    monitorThread = new Thread(new QueueMonitor(resourceManager));
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
