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

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InferredMetadataManagerImplTest {

  private static final long SMALL = InferredMetadataManagerImpl.MAX_ROWS_INFERRED_RIGHT_AWAY;
  private static final long BIG = SMALL + 1;

  private ResourceMetadataInferringService inferringService;
  private ResourceManager resourceManager;
  // background jobs run only when the test says so
  private final List<Runnable> queuedJobs = new ArrayList<>();
  private InferredMetadataManagerImpl manager;
  private Resource resource;
  private InferredEmlMetadata inferred;

  @BeforeEach
  public void setUp() {
    inferringService = mock(ResourceMetadataInferringService.class);
    resourceManager = mock(ResourceManager.class);
    Executor manualExecutor = queuedJobs::add;
    manager = new InferredMetadataManagerImpl(inferringService, resourceManager, manualExecutor);

    resource = new Resource();
    resource.setShortname("res");
    resource.setCoreType("occurrence");
    when(resourceManager.get("res")).thenReturn(resource);
    inferred = inferredAt(new Date());
    when(inferringService.inferMetadata(resource)).thenReturn(inferred);
  }

  @DisplayName("Small dataset, never inferred: inferred right away and saved")
  @Test
  public void testSmallDatasetInferredRightAway() {
    rows(OptionalLong.of(SMALL));

    InferredMetadata result = manager.getInferredMetadata(resource, false);

    assertSame(inferred, result);
    assertSame(inferred, resource.getInferredMetadata());
    verify(resourceManager).saveInferredMetadata(resource);
    assertFalse(manager.isInferring("res"));
    assertTrue(queuedJobs.isEmpty());
  }

  @DisplayName("Big dataset: the stored metadata is returned while it is inferred in the background")
  @Test
  public void testBigDatasetInferredInBackground() {
    rows(OptionalLong.of(BIG));

    InferredMetadata result = manager.getInferredMetadata(resource, false);

    // nothing inferred yet: the empty instance of a new resource
    assertInstanceOf(InferredEmlMetadata.class, result);
    assertNull(result.getLastModified());
    assertTrue(manager.isInferring("res"));
    verify(inferringService, never()).inferMetadata(any());

    runQueuedJobs();

    assertSame(inferred, resource.getInferredMetadata());
    verify(resourceManager).saveInferredMetadata(resource);
    assertFalse(manager.isInferring("res"));
  }

  @DisplayName("Source read remotely (SQL, URL): size unknown, inferred in the background")
  @Test
  public void testRemoteSourceInferredInBackground() {
    rows(OptionalLong.empty());

    manager.getInferredMetadata(resource, false);

    assertTrue(manager.isInferring("res"));
    verify(inferringService, never()).inferMetadata(any());
  }

  @DisplayName("Requests while inferring in the background start no other job, even to re-infer")
  @Test
  public void testOneJobPerResource() {
    rows(OptionalLong.of(BIG));

    manager.getInferredMetadata(resource, false);
    manager.getInferredMetadata(resource, false);
    manager.getInferredMetadata(resource, true);
    runQueuedJobs();

    verify(inferringService, times(1)).inferMetadata(resource);
  }

  @DisplayName("Up to date: not inferred again, unless requested")
  @Test
  public void testUpToDate() {
    rows(OptionalLong.of(SMALL));
    InferredEmlMetadata stored = inferredAt(new Date(2_000_000L));
    resource.setInferredMetadata(stored);
    resource.setMappingsModified(new Date(1_000_000L));
    resource.setSourcesModified(new Date(1_000_000L));

    assertSame(stored, manager.getInferredMetadata(resource, false));
    verify(inferringService, never()).inferMetadata(any());

    assertSame(inferred, manager.getInferredMetadata(resource, true));
  }

  @DisplayName("Metadata-only resource: nothing to infer from, not inferred unless requested")
  @Test
  public void testMetadataOnly() {
    rows(OptionalLong.of(0));
    resource.setCoreType(Constants.DATASET_TYPE_METADATA_IDENTIFIER);

    manager.getInferredMetadata(resource, false);
    verify(inferringService, never()).inferMetadata(any());

    manager.getInferredMetadata(resource, true);
    verify(inferringService).inferMetadata(resource);
  }

  @DisplayName("Inferring returns another type or nothing: empty inferred metadata of the resource type")
  @Test
  public void testWrongType() {
    rows(OptionalLong.of(SMALL));
    when(inferringService.inferMetadata(resource)).thenReturn(new InferredCamtrapMetadata());

    InferredMetadata result = manager.getInferredMetadata(resource, false);

    assertInstanceOf(InferredEmlMetadata.class, result);
    assertNull(result.getLastModified());

    when(inferringService.inferMetadata(resource)).thenReturn(null);
    assertInstanceOf(InferredEmlMetadata.class, manager.getInferredMetadata(resource, true));
  }

  @DisplayName("Camtrap DP resource: Camtrap DP inferred metadata, the EML default replaced")
  @Test
  public void testCamtrapType() {
    rows(OptionalLong.of(BIG));
    resource.setCoreType(Constants.CAMTRAP_DP);

    // the EML default of a new resource is not returned
    assertInstanceOf(InferredCamtrapMetadata.class, manager.getInferredMetadata(resource, false));
  }

  @DisplayName("Inferring fails in the background: stored with an error, not started again until something changes")
  @Test
  public void testBackgroundFailure() {
    rows(OptionalLong.of(BIG));
    when(inferringService.inferMetadata(resource)).thenThrow(new IllegalStateException("broken"));
    resource.setMappingsModified(new Date(1_000_000L));

    manager.getInferredMetadata(resource, false);
    runQueuedJobs();

    assertFalse(manager.isInferring("res"));
    verify(resourceManager).saveInferredMetadata(resource);
    InferredEmlMetadata failed = assertInstanceOf(InferredEmlMetadata.class, resource.getInferredMetadata());
    assertAll(
        () -> assertNotNull(failed.getLastModified()),
        () -> assertEquals(Set.of("eml.error.serverError"), failed.getInferredGeographicCoverage().getErrors()),
        () -> assertEquals(Set.of("eml.error.serverError"), failed.getInferredTaxonomicCoverage().getErrors()),
        () -> assertEquals(Set.of("eml.error.serverError"), failed.getInferredTemporalCoverage().getErrors()));

    // the page reloaded when the job is done
    manager.getInferredMetadata(resource, false);
    assertFalse(manager.isInferring("res"));
    assertTrue(queuedJobs.isEmpty());

    // mappings changed, after the date stored: within the same millisecond it would not count as later
    resource.setMappingsModified(new Date(failed.getLastModified().getTime() + 1));
    manager.getInferredMetadata(resource, false);
    assertTrue(manager.isInferring("res"));
  }

  @DisplayName("Up to date for publishing: inferred again after a server error, which can be temporary")
  @Test
  public void testUpToDateForPublishingAfterServerError() {
    rows(OptionalLong.of(BIG));
    when(inferringService.inferMetadata(resource)).thenThrow(new IllegalStateException("broken"));
    manager.getInferredMetadata(resource, false);
    runQueuedJobs();
    assertFalse(resource.isInferredMetadataOutdated(InferredEmlMetadata.class));

    // the next attempt succeeds
    doReturn(inferred).when(inferringService).inferMetadata(resource);

    assertSame(inferred, manager.getUpToDateInferredMetadata(resource));
  }

  @DisplayName("Up to date for publishing: Camtrap DP inferred again after a server error")
  @Test
  public void testUpToDateForPublishingAfterCamtrapServerError() {
    rows(OptionalLong.of(BIG));
    resource.setCoreType(Constants.CAMTRAP_DP);
    InferredCamtrapMetadata stored = new InferredCamtrapMetadata();
    stored.setLastModified(new Date());
    stored.setInferredTaxonomicScope(new InferredCamtrapTaxonomicScope());
    stored.getInferredTaxonomicScope().addError("datapackagemetadata.error.serverError");
    resource.setInferredMetadata(stored);
    InferredCamtrapMetadata camtrapInferred = new InferredCamtrapMetadata();
    camtrapInferred.setLastModified(new Date());
    when(inferringService.inferMetadata(resource)).thenReturn(camtrapInferred);

    assertSame(camtrapInferred, manager.getUpToDateInferredMetadata(resource));
  }

  @DisplayName("Up to date for publishing: other errors, e.g. fields not mapped, are not a reason to infer again")
  @Test
  public void testUpToDateForPublishingOtherError() {
    rows(OptionalLong.of(BIG));
    InferredEmlMetadata stored = inferredAt(new Date());
    stored.setInferredGeographicCoverage(new InferredEmlGeographicCoverage());
    stored.getInferredGeographicCoverage().addError("eml.geospatialCoverages.error.fieldsNotMapped");
    resource.setInferredMetadata(stored);

    assertSame(stored, manager.getUpToDateInferredMetadata(resource));
    verify(inferringService, never()).inferMetadata(any());
  }

  @DisplayName("Camtrap DP inferring fails in the background: stored with an error for each scope")
  @Test
  public void testCamtrapBackgroundFailure() {
    rows(OptionalLong.of(BIG));
    resource.setCoreType(Constants.CAMTRAP_DP);
    when(inferringService.inferMetadata(resource)).thenThrow(new IllegalStateException("broken"));

    manager.getInferredMetadata(resource, false);
    runQueuedJobs();

    InferredCamtrapMetadata failed = assertInstanceOf(InferredCamtrapMetadata.class, resource.getInferredMetadata());
    String error = "datapackagemetadata.error.serverError";
    assertAll(
        () -> assertEquals(Set.of(error), failed.getInferredGeographicScope().getErrors()),
        () -> assertEquals(Set.of(error), failed.getInferredTaxonomicScope().getErrors()),
        () -> assertEquals(Set.of(error), failed.getInferredTemporalScope().getErrors()));
  }

  @DisplayName("Resource deleted while inferring in the background: the result is not saved")
  @Test
  public void testResourceDeletedWhileInferring() {
    rows(OptionalLong.of(BIG));

    manager.getInferredMetadata(resource, false);
    when(resourceManager.get("res")).thenReturn(null);
    runQueuedJobs();

    verify(resourceManager, never()).saveInferredMetadata(any());
    assertFalse(manager.isInferring("res"));
  }

  @DisplayName("Up to date for publishing: outdated metadata is inferred right away, even for big datasets")
  @Test
  public void testUpToDateForPublishingOutdated() {
    rows(OptionalLong.of(BIG));

    assertSame(inferred, manager.getUpToDateInferredMetadata(resource));
    assertTrue(queuedJobs.isEmpty());
  }

  @DisplayName("Up to date for publishing: stored metadata of local files is used, remote data is inferred again")
  @Test
  public void testUpToDateForPublishingStored() {
    InferredEmlMetadata stored = inferredAt(new Date(2_000_000L));
    resource.setInferredMetadata(stored);

    rows(OptionalLong.of(BIG));
    assertSame(stored, manager.getUpToDateInferredMetadata(resource));
    verify(inferringService, never()).inferMetadata(any());

    // data read remotely can have changed
    rows(OptionalLong.empty());
    assertSame(inferred, manager.getUpToDateInferredMetadata(resource));
  }

  @DisplayName("Up to date for publishing: waits for inferring in the background, and uses its result")
  @Test
  public void testUpToDateForPublishingWaitsForBackground() throws Exception {
    CountDownLatch inferringStarted = new CountDownLatch(1);
    CountDownLatch finishInferring = new CountDownLatch(1);
    when(inferringService.inferMetadata(resource)).thenAnswer(invocation -> {
      inferringStarted.countDown();
      finishInferring.await();
      return inferred;
    });
    rows(OptionalLong.of(BIG));
    manager = new InferredMetadataManagerImpl(inferringService, resourceManager, Executors.newSingleThreadExecutor());

    manager.getInferredMetadata(resource, false);
    assertTrue(inferringStarted.await(5, TimeUnit.SECONDS));
    new Thread(() -> {
      sleep(50);
      finishInferring.countDown();
    }).start();

    InferredMetadata result = assertTimeoutPreemptively(Duration.ofSeconds(5),
        () -> manager.getUpToDateInferredMetadata(resource));

    assertSame(inferred, result);
    // not inferred a second time
    verify(inferringService, times(1)).inferMetadata(resource);
  }

  @DisplayName("Up to date for publishing: a job still waiting behind other jobs is run right away, not run again later")
  @Test
  public void testUpToDateForPublishingRunsQueuedJob() {
    rows(OptionalLong.of(BIG));
    manager.getInferredMetadata(resource, false);
    assertTrue(manager.isInferring("res"));

    InferredMetadata result = assertTimeoutPreemptively(Duration.ofSeconds(5),
        () -> manager.getUpToDateInferredMetadata(resource));

    assertSame(inferred, result);
    assertFalse(manager.isInferring("res"));

    // the background thread reaches it later
    runQueuedJobs();
    verify(inferringService, times(1)).inferMetadata(resource);
  }

  @DisplayName("Up to date for publishing: a job waiting behind others is still seen as inferring while publishing runs it")
  @Test
  public void testUpToDateForPublishingRunsQueuedJobRegistered() {
    rows(OptionalLong.of(BIG));
    manager.getInferredMetadata(resource, false);
    when(inferringService.inferMetadata(resource)).thenAnswer(invocation -> {
      assertTrue(manager.isInferring("res"));
      // no other job started meanwhile
      manager.getInferredMetadata(resource, true);
      return inferred;
    });

    manager.getUpToDateInferredMetadata(resource);
    runQueuedJobs();

    verify(inferringService, times(1)).inferMetadata(resource);
  }

  private void rows(OptionalLong rows) {
    when(inferringService.countRowsToRead(resource)).thenReturn(rows);
  }

  private void runQueuedJobs() {
    List<Runnable> jobs = new ArrayList<>(queuedJobs);
    queuedJobs.clear();
    jobs.forEach(Runnable::run);
  }

  private static InferredEmlMetadata inferredAt(Date date) {
    InferredEmlMetadata metadata = new InferredEmlMetadata();
    metadata.setLastModified(date);
    return metadata;
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
