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

import jakarta.inject.Inject;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTaxonomicCoverage;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.InferredMetadataManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;

import java.util.Date;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.service.manage.impl.ResourceMetadataInferringServiceImpl.CAMTRAP_SERVER_ERROR;
import static org.gbif.ipt.service.manage.impl.ResourceMetadataInferringServiceImpl.EML_SERVER_ERROR;

public class InferredMetadataManagerImpl implements InferredMetadataManager {

  private static final Logger LOG = LogManager.getLogger(InferredMetadataManagerImpl.class);

  // up to this number of rows, inferring takes a few seconds at most, and is done right away
  public static final long MAX_ROWS_INFERRED_RIGHT_AWAY = 100_000;

  private final ResourceMetadataInferringService inferringService;
  private final ResourceManager resourceManager;
  // runs the background jobs, one at a time: see the metadataInferringExecutor bean
  private final Executor executor;
  // jobs running or waiting to run, by resource shortname
  private final Map<String, Job> jobs = new ConcurrentHashMap<>();

  @Inject
  public InferredMetadataManagerImpl(
      ResourceMetadataInferringService inferringService,
      ResourceManager resourceManager,
      Executor executor) {
    this.inferringService = inferringService;
    this.resourceManager = resourceManager;
    this.executor = executor;
  }

  @Override
  public InferredMetadata getInferredMetadata(Resource resource, boolean reinfer) {
    if (!isInferring(resource.getShortname()) && (reinfer || isOutdated(resource))) {
      if (isSmall(resource)) {
        infer(resource);
      } else {
        inferInBackground(resource);
      }
    }
    return storedInferredMetadata(resource);
  }

  @Override
  public InferredMetadata getUpToDateInferredMetadata(Resource resource) {
    String shortname = resource.getShortname();
    Job job = jobs.get(shortname);

    if (job != null && job.claim()) {
      // still waiting behind other resources' jobs: run here instead, registered meanwhile so no other job starts
      try {
        inferIfNotUpToDate(resource);
      } finally {
        finish(shortname, job);
      }
    } else {
      if (job != null) {
        waitFor(shortname, job);
      }
      inferIfNotUpToDate(resource);
    }
    return storedInferredMetadata(resource);
  }

  @Override
  public boolean isInferring(String shortname) {
    return jobs.containsKey(shortname);
  }

  /**
   * Metadata-only resources have no data to infer from.
   */
  private boolean isOutdated(Resource resource) {
    return !resource.isMetadataOnly() && resource.isInferredMetadataOutdated(inferredMetadataType(resource));
  }

  private boolean isSmall(Resource resource) {
    OptionalLong rows = inferringService.countRowsToRead(resource);
    return rows.isPresent() && rows.getAsLong() <= MAX_ROWS_INFERRED_RIGHT_AWAY;
  }

  private void infer(Resource resource) {
    store(resource, inferringService.inferMetadata(resource));
  }

  private void store(Resource resource, InferredMetadata inferred) {
    if (!inferredMetadataType(resource).isInstance(inferred)) {
      LOG.error("Wrong type of the metadata inferred for resource {}, expected {} got {}", resource.getShortname(),
          inferredMetadataType(resource).getSimpleName(), inferred == null ? null : inferred.getClass().getSimpleName());
      inferred = emptyInferredMetadata(resource);
    }
    resource.setInferredMetadata(inferred);
    resourceManager.saveInferredMetadata(resource);
  }

  /**
   * Inferred again also after a server error, which can be temporary, and for data read remotely, which can change
   * without the IPT knowing.
   */
  private void inferIfNotUpToDate(Resource resource) {
    if (isOutdated(resource)
        || hasServerError(resource.getInferredMetadata())
        || inferringService.countRowsToRead(resource).isEmpty()) {
      infer(resource);
    }
  }

  private void inferInBackground(Resource resource) {
    String shortname = resource.getShortname();
    Job job = new Job();

    // registered before it runs, so that it is never seen as finished before it started
    if (jobs.putIfAbsent(shortname, job) == null) {
      LOG.info("Inferring the metadata of resource {} in the background", shortname);
      executor.execute(() -> {
        // already run by publishing
        if (!job.claim()) {
          return;
        }
        try {
          runInBackground(resource, shortname);
        } finally {
          finish(shortname, job);
        }
      });
    }
  }

  private void runInBackground(Resource resource, String shortname) {
    Date started = new Date();
    InferredMetadata inferred;
    try {
      inferred = inferringService.inferMetadata(resource);
    } catch (RuntimeException e) {
      LOG.error("Failed to infer the metadata of resource {} in the background", shortname, e);
      // stored with an error: otherwise the page, reloaded when the job is done, would start it again and again
      inferred = failedInferredMetadata(resource, started);
    }

    // saving the result of a deleted resource would create its folder in the data dir again
    if (resourceManager.get(shortname) != resource) {
      LOG.info("Resource {} was deleted while inferring its metadata in the background, the result is discarded",
          shortname);
      return;
    }
    try {
      store(resource, inferred);
    } catch (RuntimeException e) {
      LOG.error("Failed to save the metadata inferred in the background for resource {}", shortname, e);
    }
  }

  private void finish(String shortname, Job job) {
    jobs.remove(shortname, job);
    job.done.complete(null);
  }

  private static void waitFor(String shortname, Job job) {
    try {
      job.done.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOG.error("Failed to infer the metadata of resource {} in the background", shortname, e);
    }
  }

  private static boolean hasServerError(InferredMetadata metadata) {
    if (metadata instanceof InferredEmlMetadata eml) {
      return (eml.getInferredGeographicCoverage() != null
          && eml.getInferredGeographicCoverage().getErrors().contains(EML_SERVER_ERROR))
          || (eml.getInferredTaxonomicCoverage() != null
          && eml.getInferredTaxonomicCoverage().getErrors().contains(EML_SERVER_ERROR))
          || (eml.getInferredTemporalCoverage() != null
          && eml.getInferredTemporalCoverage().getErrors().contains(EML_SERVER_ERROR));
    } else if (metadata instanceof InferredCamtrapMetadata camtrap) {
      return (camtrap.getInferredGeographicScope() != null
          && camtrap.getInferredGeographicScope().getErrors().contains(CAMTRAP_SERVER_ERROR))
          || (camtrap.getInferredTaxonomicScope() != null
          && camtrap.getInferredTaxonomicScope().getErrors().contains(CAMTRAP_SERVER_ERROR))
          || (camtrap.getInferredTemporalScope() != null
          && camtrap.getInferredTemporalScope().getErrors().contains(CAMTRAP_SERVER_ERROR));
    }
    return false;
  }

  private InferredMetadata storedInferredMetadata(Resource resource) {
    InferredMetadata stored = resource.getInferredMetadata();
    return inferredMetadataType(resource).isInstance(stored) ? stored : emptyInferredMetadata(resource);
  }

  private static Class<? extends InferredMetadata> inferredMetadataType(Resource resource) {
    return CAMTRAP_DP.equals(resource.getCoreType()) ? InferredCamtrapMetadata.class : InferredEmlMetadata.class;
  }

  private static InferredMetadata emptyInferredMetadata(Resource resource) {
    return CAMTRAP_DP.equals(resource.getCoreType()) ? new InferredCamtrapMetadata() : new InferredEmlMetadata();
  }

  /**
   * Empty inferred metadata of the resource type, with a server error for each coverage, dated when inferring started
   * so that it is inferred again once the mappings or sources change.
   */
  private static InferredMetadata failedInferredMetadata(Resource resource, Date started) {
    if (CAMTRAP_DP.equals(resource.getCoreType())) {
      InferredCamtrapMetadata failed = new InferredCamtrapMetadata();
      failed.setInferredGeographicScope(new InferredCamtrapGeographicScope());
      failed.getInferredGeographicScope().addError(CAMTRAP_SERVER_ERROR);
      failed.setInferredTaxonomicScope(new InferredCamtrapTaxonomicScope());
      failed.getInferredTaxonomicScope().addError(CAMTRAP_SERVER_ERROR);
      failed.setInferredTemporalScope(new InferredCamtrapTemporalScope());
      failed.getInferredTemporalScope().addError(CAMTRAP_SERVER_ERROR);
      failed.setLastModified(started);
      return failed;
    }

    InferredEmlMetadata failed = new InferredEmlMetadata();
    failed.setInferredGeographicCoverage(new InferredEmlGeographicCoverage());
    failed.getInferredGeographicCoverage().addError(EML_SERVER_ERROR);
    failed.setInferredTaxonomicCoverage(new InferredEmlTaxonomicCoverage());
    failed.getInferredTaxonomicCoverage().addError(EML_SERVER_ERROR);
    failed.setInferredTemporalCoverage(new InferredEmlTemporalCoverage());
    failed.getInferredTemporalCoverage().addError(EML_SERVER_ERROR);
    failed.setLastModified(started);
    return failed;
  }

  /**
   * Inferring requested for a resource, run either by the background thread or by publishing, whichever claims it first.
   */
  private static final class Job {
    private final AtomicBoolean claimed = new AtomicBoolean();
    private final CompletableFuture<Void> done = new CompletableFuture<>();

    private boolean claim() {
      return claimed.compareAndSet(false, true);
    }
  }
}
