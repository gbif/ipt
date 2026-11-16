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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.model.BulkPublication.Item;
import org.gbif.ipt.model.BulkPublication.NotReady;
import org.gbif.ipt.model.BulkPublication.Scope;
import org.gbif.ipt.model.BulkPublication.State;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.BulkPublicationManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.ipt.validation.EmlValidator;

import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BulkPublicationManagerImpl implements BulkPublicationManager {

  private static final Logger LOG = LogManager.getLogger(BulkPublicationManagerImpl.class);
  private static final long POLL_MILLIS = 1000;
  // polls without a running publication or a final report before a resource counts as failed
  private static final int MAX_MISSES = 60;

  private final AppConfig cfg;
  private final SimpleTextProvider textProvider;
  private final RegistrationManager registrationManager;
  private final ResourceManager resourceManager;
  private final ResourcePublicationManager publicationManager;
  private final EmlValidator emlValidator;
  private final DataPackageMetadataValidator dpMetadataValidator;

  private volatile BulkPublication current;

  @Inject
  public BulkPublicationManagerImpl(
      AppConfig cfg,
      SimpleTextProvider textProvider,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      ResourcePublicationManager publicationManager,
      EmlValidator emlValidator,
      DataPackageMetadataValidator dpMetadataValidator) {
    this.cfg = cfg;
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
    this.resourceManager = resourceManager;
    this.publicationManager = publicationManager;
    this.emlValidator = emlValidator;
    this.dpMetadataValidator = dpMetadataValidator;
  }

  @Override
  public BulkPublication getCurrent() {
    return current;
  }

  @Override
  public boolean isRunning() {
    BulkPublication run = current;
    return run != null && !run.isFinished();
  }

  @Override
  public NotReady notReady(Resource resource) {
    if (resource.getStatus() == PublicationStatus.DELETED) {
      return NotReady.DELETED;
    }
    // read the futures directly: isLocked() finishes completed publications as a side effect
    Future<?> f = publicationManager.getProcessFutures().get(resource.getShortname());
    if (f != null && !f.isDone()) {
      return NotReady.LOCKED;
    }
    // DwC-DP metadata is EML, like for DwC-A
    boolean validMetadata = resource.isDataPackage() && !resource.isDwcDp()
        ? dpMetadataValidator.isValid(resource)
        : emlValidator.isValid(resource, null);
    if (!validMetadata) {
      return NotReady.METADATA;
    }
    if (resource.isDataPackage() && resource.getDataPackageMappings().isEmpty()) {
      return NotReady.MAPPINGS;
    }
    return null;
  }

  @Override
  public boolean isChanged(Resource resource) {
    return !publicationManager.isUnchangedSinceLastPublication(resource);
  }

  @Override
  public List<Resource> resourcesInScope(Scope scope, Collection<String> selected, Collection<String> excluded) {
    Collection<String> picked = selected != null ? selected : Collections.emptyList();
    Collection<String> left = excluded != null ? excluded : Collections.emptyList();
    return resourceManager.list().stream()
        .filter(r -> switch (scope) {
          case ALL -> true;
          case CHANGED -> isChanged(r);
          case SELECTED -> picked.contains(r.getShortname());
          case EXCLUDED -> !left.contains(r.getShortname());
        })
        .toList();
  }

  @Override
  public synchronized BulkPublication start(Scope scope, Collection<String> selected, Collection<String> excluded,
                                            String startedBy) {
    if (isRunning()) {
      throw new IllegalStateException("A bulk publication is in progress");
    }

    List<Resource> resources = resourcesInScope(scope, selected, excluded);
    List<Item> items = new ArrayList<>();
    for (Resource resource : resources) {
      Item item = new Item(resource.getShortname(), titleOf(resource), formatOf(resource), resource.getStatus(),
          resource.getLastPublishedVersionsVersion() != null ? resource.getLastPublishedVersionsVersion().toPlainString() : null,
          resource.getNextVersion().toPlainString(), resource.getRecordsDropThreshold());
      NotReady reason = notReady(resource);
      if (reason != null) {
        item.setNotReady(reason);
        item.setState(State.SKIPPED);
      }
      items.add(item);
    }

    BulkPublication run = new BulkPublication(startedBy, scope, items);
    current = run;

    Thread thread = new Thread(() -> runSafely(run), "bulk-publication");
    thread.setDaemon(true);
    thread.start();
    return run;
  }

  private void runSafely(BulkPublication run) {
    try {
      run(run);
    } catch (Exception e) {
      LOG.error("Bulk publication failed", e);
      for (Item item : run.getItems()) {
        if (!item.getState().isFinished()) {
          item.setState(State.FAILED);
          item.setError(e.getMessage());
        }
      }
    } finally {
      run.setEnded(new Date());
    }
  }

  private void run(BulkPublication run) {
    // texts are made outside a request, as for auto-publishing
    BaseAction action = new BaseAction(textProvider, cfg, registrationManager);

    // the CHANGED scope compares the archives of database and URL sources after generating them
    boolean skipIfNotChanged = run.getScope() == Scope.CHANGED;
    List<Item> launched = new ArrayList<>();
    for (Item item : run.getItems()) {
      if (item.getState() == State.SKIPPED) {
        continue;
      }
      if (launch(item, action, skipIfNotChanged)) {
        launched.add(item);
      }
    }

    // wait for the archives being generated; the publishing monitor finishes them too
    Map<String, Future<Map<String, Long>>> seen = new HashMap<>();
    Map<String, Integer> misses = new HashMap<>();
    while (launched.stream().anyMatch(i -> !i.getState().isFinished())) {
      for (Item item : launched) {
        if (!item.getState().isFinished()) {
          follow(item, action, seen, misses);
        }
      }
      try {
        Thread.sleep(POLL_MILLIS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOG.error("Bulk publication was interrupted while waiting for publications to finish", e);
        return;
      }
    }
  }

  /**
   * Starts publishing a resource, like on the resource overview.
   *
   * @return true if an archive is being generated in the background, false if the resource has already finished
   */
  private boolean launch(Item item, BaseAction action, boolean skipIfNotChanged) {
    String shortname = item.getShortname();
    Resource resource = resourceManager.get(shortname);
    if (resource == null) {
      item.setState(State.FAILED);
      return false;
    }
    // check again: a manager may have started publishing or changed the resource since the run started
    NotReady reason = notReady(resource);
    if (reason != null) {
      item.setNotReady(reason);
      item.setState(State.SKIPPED);
      return false;
    }

    BigDecimal version = resource.getNextVersion();
    try {
      // bulk publication is an explicit admin action, so resources that hit the max failures limit for
      // auto-publishing get a fresh start
      publicationManager.getProcessFailures().remove(shortname);
      item.setState(State.RUNNING);
      boolean archive = publicationManager.publish(resource, version, action, skipIfNotChanged);
      if (archive) {
        return true;
      }
      // skipped because nothing changed, or a metadata-only resource published at once
      StatusReport report = publicationManager.getProcessReports().get(shortname);
      if (report != null && report.getOutcome() == StatusReport.Outcome.NOT_CHANGED) {
        item.setState(State.NOT_CHANGED);
        item.setMessages(report.getMessages());
      } else {
        item.setState(State.PUBLISHED);
        item.setRecords(resource.getRecordsPublished());
      }
    } catch (PublicationException e) {
      if (PublicationException.TYPE.LOCKED == e.getType()) {
        item.setNotReady(NotReady.LOCKED);
        item.setState(State.SKIPPED);
      } else {
        fail(item, e.getMessage());
        // restore the previous version since publication was unsuccessful
        publicationManager.restoreVersion(resource, version, action);
        publicationManager.getProcessFailures()
            .computeIfAbsent(shortname, k -> new CopyOnWriteArrayList<>())
            .add(new Date());
      }
    } catch (Exception e) {
      // e.g. InvalidConfigException: the resource couldn't be saved, so there's no version to restore
      LOG.error("Bulk publication of {} failed", shortname, e);
      fail(item, e.getMessage());
    }
    return false;
  }

  private void fail(Item item, String message) {
    item.setError(message);
    item.setState(State.FAILED);
  }

  /**
   * Reads how far a resource's archive has got, and its result once it's finished.
   */
  private void follow(Item item, BaseAction action, Map<String, Future<Map<String, Long>>> seen,
                      Map<String, Integer> misses) {
    String shortname = item.getShortname();
    // finishes the publication if its archive is done
    publicationManager.isLocked(shortname, action);

    Future<Map<String, Long>> f = publicationManager.getProcessFutures().get(shortname);
    StatusReport report = publicationManager.getProcessReports().get(shortname);
    if (report != null) {
      item.setMessages(report.getMessages());
    }

    if (f != null) {
      seen.putIfAbsent(shortname, f);
      // waiting for a free publishing thread, or running
      boolean queued = f instanceof Runnable runnable && publicationManager.getExecutor().getQueue().contains(runnable);
      item.setState(queued ? State.WAITING : State.RUNNING);
      if (report != null) {
        item.setStep(report.getStep());
        item.setStateText(report.getState());
      }
      return;
    }

    if (report == null || !report.isCompleted()) {
      Future<Map<String, Long>> known = seen.get(shortname);
      if (known != null && known.isCancelled()) {
        // cancelled on the publishing page, which doesn't make a final report
        item.setState(State.CANCELLED);
        return;
      }
      // the publishing monitor may be finishing it; give up when no final report comes
      int count = misses.merge(shortname, 1, Integer::sum);
      if (count > MAX_MISSES) {
        fail(item, report != null ? report.getState() : null);
      }
      return;
    }
    item.setStep(report.getStep());
    if (report.isCancelled()) {
      item.setState(State.CANCELLED);
    } else if (report.hasException()) {
      fail(item, report.getExceptionMessage() != null ? report.getExceptionMessage() : report.getState());
    } else if (report.getOutcome() == StatusReport.Outcome.NOT_CHANGED) {
      item.setState(State.NOT_CHANGED);
    } else if (report.getOutcome() == StatusReport.Outcome.RECORDS_DROPPED) {
      item.setState(State.RECORDS_DROPPED);
    } else {
      Resource resource = resourceManager.get(shortname);
      if (resource != null) {
        item.setRecords(resource.getRecordsPublished());
      }
      item.setState(State.PUBLISHED);
    }
  }

  private static String titleOf(Resource resource) {
    return resource.getTitle() != null && !resource.getTitle().isBlank() ? resource.getTitle() : resource.getShortname();
  }

  /**
   * @return dwca, dwc-dp, camtrap-dp, coldp or metadata
   */
  public static String formatOf(Resource resource) {
    if (resource.isMetadataOnly()) {
      return "metadata";
    }
    if (resource.isDwcDp()) {
      return "dwc-dp";
    }
    if (resource.isDataPackage() && resource.getDataPackageType() != null) {
      return resource.getDataPackageType().toLowerCase();
    }
    return "dwca";
  }
}
