package org.gbif.ipt.config;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
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
  protected Logger log = Logger.getLogger(this.getClass());
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
          ThreadPoolExecutor executor = resourceManager.getExecutor();

          // might as well check if we can handle more publishing jobs
          if (executor.getMaximumPoolSize() - executor.getActiveCount() > 0) {
            Date now = new Date();
            List<Resource> resources = resourceManager.list();
            // monitor for resources due to be auto-published
            for (Resource resource : resources) {
              Date next = resource.getNextPublished();
              int v = resource.getNextVersion();
              if (next != null) {
                if (next.before(now)) {
                  try {
                    log.debug(
                      "Monitor: " + resource.getTitleAndShortname() + " v# " + v + " due to be auto-published: " + next
                        .toString());
                    resourceManager.publish(resource, v, null);
                  } catch (PublicationException e) {
                    if (PublicationException.TYPE.LOCKED == e.getType()) {
                      log.error("Monitor: " + resource.getTitleAndShortname() + " cannot be auto-published, because "
                                + "it is currently being published");
                    } else {
                      // alert user publication failed
                      log.error(
                        "Publishing version #" + String.valueOf(v) + " of resource " + resource.getTitleAndShortname()
                        + " failed: " + e.getMessage());
                      // restore the previous version since publication was unsuccessful
                      resourceManager.restoreVersion(resource, resource.getLastVersion(), null);
                    }
                  }
                }
              }
            }
          }

          // monitor resources that are currently being published or have finished
          // in order for publishing to finish entirely, resourceManager.isLocked() must be called
          Map<String, Future<Integer>> processFutures = resourceManager.getProcessFutures();
          if (processFutures.size() > 0) {
            // copy futures into new set, to avoid concurrent modification exception
            Set<String> shortNames = new HashSet<String>();
            shortNames.addAll(processFutures.keySet());
            for (String shortName : shortNames) {
              resourceManager.isLocked(shortName, baseAction);
            }
          }

          // these jobs take some time, so just poll once every 'interval' milliseconds
          Thread.sleep(MONITOR_INTERVAL_MS);
        } catch (InterruptedException e) {
          // should the thread have been interrupted, encountered when trying to sleep
          log.error("Monitor thread has been interrupted!", e);
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
    log.debug("The monitor thread has started.");
  }

  /**
   * Starts the publishing monitor once and only once.
   */
  public void start() {
    if (monitorThread != null) {
      if (!monitorThread.isAlive()) {
        startMonitorThread();
      } else {
        log.error("The monitor thread is already running");
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
