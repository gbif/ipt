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

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.registry.RegistryManager;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Makes sure IPT is aware of any recent updates to the extensions.
 */
public class ExtensionMonitor {

  // 24h interval
  public static final int MONITOR_INTERVAL_MS = 86_400_000;
  private static Thread monitorThread;
  private static final Logger LOG = LogManager.getLogger(ExtensionMonitor.class);
  private AtomicBoolean running;
  private final ExtensionManager extensionManager;
  private final RegistryManager registryManager;

  @Inject
  public ExtensionMonitor(ExtensionManager extensionManager, RegistryManager registryManager) {
    this.extensionManager = extensionManager;
    this.registryManager = registryManager;
  }

  class QueueMonitor implements Runnable {

    public QueueMonitor() {
      running = new AtomicBoolean();
    }

    @Override
    public void run() {
      running.set(true);
      while (running.get()) {
        try {
          List<Extension> installedExtensions = extensionManager.list();
          List<Extension> registeredExtensions = registryManager.getLatestExtensions();

          for (Extension extension : installedExtensions) {
            for (Extension rExtension : registeredExtensions) {
              // check if registered extension is latest, and if it is, try to use it in comparison
              if (extension.getRowType().equalsIgnoreCase(rExtension.getRowType())) {
                Date installedExtensionIssuedDate = extension.getIssued();
                Date latestExtensionIssuedDate = rExtension.getIssued();
                if (installedExtensionIssuedDate == null && latestExtensionIssuedDate != null) {
                  extension.setLatest(false);
                } else if (latestExtensionIssuedDate != null && latestExtensionIssuedDate.compareTo(installedExtensionIssuedDate) > 0) {
                  extension.setLatest(false);
                }
                break;
              }
            }
          }

          // just poll once every 'interval' millisecond
          Thread.sleep(MONITOR_INTERVAL_MS);
        } catch (InterruptedException e) {
          // should the thread have been interrupted, encountered when trying to sleep
          LOG.error("Extension monitor thread has been interrupted!", e);
        }
      }
    }
  }

  /**
   * Start the extension monitor thread itself.
   */
  private void startMonitorThread() {
    monitorThread = new Thread(new ExtensionMonitor.QueueMonitor());
    monitorThread.start();
    LOG.debug("The monitor thread has started.");
  }

  /**
   * Starts the extension monitor once and only once.
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
   * Stops the extension monitor.
   */
  public void stop() {
    running.set(false);
    monitorThread = null;
  }
}
