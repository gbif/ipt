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
package org.gbif.ipt.service.manage;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PublicationOptions;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.task.StatusReport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections4.ListValuedMap;

import javax.annotation.Nullable;

/**
 * Publication lifecycle for resources: publishing (EML/RTF/DwC-A/data package generation), DOI
 * registration/replacement, GBIF Registry registration and updates, version restore/removal, auto-publication
 * scheduling, checksum calculation, and tracking of in-flight publication jobs.
 * <p>
 * Any service, Struts action, or scheduled job that needs to publish, register, restore, or check the publication
 * status of a resource should depend on this interface directly, rather than going through {@link ResourceManager}.
 */
public interface ResourcePublicationManager {

  /**
   * Cancels publishing.
   *
   * @param shortname Resource shortname
   *
   * @return result of trying to cancel publishing: was successful or not
   */
  boolean cancelPublishing(String shortname);

  /**
   * Checks whether the resource is currently locked or not. It then checks if the task is done or not. If done
   * successfully, the remaining steps in publishing are executed. If it failed for any reason,
   * the previous published version is restored.
   *
   * @param shortname Resource shortname
   * @param action    the action to use for logging messages
   *
   * @return true if resource is currently locked for any management.
   */
  boolean isLocked(String shortname, BaseAction action);

  /**
   * Defaults BaseAction to null.
   *
   * @see ResourcePublicationManager#isLocked(String, org.gbif.ipt.action.BaseAction)
   */
  boolean isLocked(String shortname);

  /**
   * Return status report of current task either running or on queue for the requested resource or null if none exists.
   *
   * @param shortname for the resource
   *
   * @return status report of current task either running or on queue for the requested resource or null if none exists
   */
  @Nullable
  StatusReport status(String shortname);

  /**
   * Publishes a new version of a resource including generating a darwin core archive and issuing a new EML version for
   * DwC resources or a data package archive and a metadata file for data package resources.
   *
   * @param resource Resource
   * @param version version number of eml/rft/archive to be published
   * @param action   the action to use for logging messages to
   *
   * @return true if a new asynchronous archive generation job has been issued which requires some mapped data
   *
   * @throws PublicationException if resource was already registered
   * @throws InvalidConfigException if resource or metadata could not be saved
   */
  boolean publish(Resource resource, BigDecimal version, @Nullable BaseAction action) throws PublicationException;

  /**
   * Publishes a new version of a resource including generating a darwin core archive and issuing a new EML version for
   * DwC resources or a data package acrhive and a metadata file for data package resources.
   *
   * @param resource Resource
   * @param version version number of eml/rft/archive to be published
   * @param action   the action to use for logging messages to
   * @param skipIfNotChanged do not publish a new version if it hasn't changed since last publication
   *
   * @return true if a new asynchronous archive generation job has been issued which requires some mapped data
   *
   * @throws PublicationException if resource was already registered
   * @throws InvalidConfigException if resource or metadata could not be saved
   */
  boolean publish(Resource resource, BigDecimal version, @Nullable BaseAction action, boolean skipIfNotChanged) throws PublicationException;

  /**
   * Publishes a new version of a resource including generating a darwin core archive and issuing a new EML version for
   * DwC resources or a data package acrhive and a metadata file for data package resources.
   *
   * @param resource Resource
   * @param version version number of eml/rft/archive to be published
   * @param action   the action to use for logging messages to
   * @param options advanced publication options
   *
   * @return true if a new asynchronous archive generation job has been issued which requires some mapped data
   *
   * @throws PublicationException if resource was already registered
   * @throws InvalidConfigException if resource or metadata could not be saved
   */
  boolean publish(Resource resource, BigDecimal version, BaseAction action, PublicationOptions options) throws PublicationException;

  void publishDataPackageMetadata(Resource resource, BigDecimal version);

  Resource updateAlternateIdentifierForRegistry(Resource resource);

  /**
   * Updates the resource's alternative identifier for the IPT URL to the resource, and saves the EML afterward.
   * This identifier should only exist for the resource, if its visibility is public.
   * If the resource visibility is set to private, this method should be called to ensure the identifier is removed.
   * Any time the baseURL changes, this method must be called for all public resources so that this identifier
   * will be updated. This method will remove an IPT URL identifier with the wrong baseURL by matching the
   * RESOURCE_PUBLIC_LINK_PART, updating it with one having the latest baseURL.
   *
   * @param resource resource
   *
   * @return resource with the IPT URL alternate identifier for the resource updated
   */
  Resource updateAlternateIdentifierForIPTURLToResource(Resource resource);

  /**
   * This method rolls back a pending version (a version being published that can't finish successfully). This method
   * must be called when publication fails, for whatever reason.
   * </br>
   * This method deletes the pending version's DwC-A, RTF, and EML files.
   * </br>
   * This method then restores the resource (version) back to the last successfully published version. This includes
   * updating the resource's version history, last publication date, version, etc.
   *
   * @param resource resource
   * @param rollingBack version to rollback
   * @param action   action
   */
  void restoreVersion(Resource resource, BigDecimal rollingBack, @Nullable BaseAction action);

  /**
   * Remove a specific archived version of a resource
   *
   * @param resource
   * @param version
   */
  void removeVersion(Resource resource, BigDecimal version);

  void removeArchiveVersion(String shortname, BigDecimal version);

  /**
   * Registers the resource with the GBIF Registry. Instead of registering a new resource, the resource can instead
   * update an existing registered resource if a UUID corresponding to an existing registered resource (owned by the
   * specified organization) is found in the resource's alternate identifiers list.
   *
   * @param resource     the published resource
   * @param organisation the organization that owns the resource
   * @param ipt          the ipt that the resource will be published through
   * @param action       Action used to show log messages on UI
   */
  void register(Resource resource, Organisation organisation, Ipt ipt, BaseAction action) throws InvalidConfigException;

  /**
   * Update the registration of the resource with the GBIF Registry. This is always done as part of a resource
   * publication.
   *
   * @param resource the published resource
   * @param action   the action to use for logging messages
   *
   * @throws PublicationException (TYPE.REGISTRY) if update was unsuccessful
   */
  void updateRegistration(Resource resource, BaseAction action) throws PublicationException;

  /**
   * Makes a resource private.
   *
   * @param resource Resource
   * @param action the action to use for logging messages
   *
   * @throws InvalidConfigException if resource was already registered
   */
  void visibilityToPrivate(Resource resource, BaseAction action) throws InvalidConfigException;

  /**
   * Makes a resource public.
   *
   * @param resource Resource
   * @param action the action to use for logging messages
   *
   * @throws InvalidConfigException if resource was already registered
   */
  void visibilityToPublic(Resource resource, BaseAction action) throws InvalidConfigException;

  /**
   * Update the resource publicationMode.
   *
   * @param resource resource
   */
  void updatePublicationMode(Resource resource);

  void cleanArchiveVersions(Resource resource);

  /**
   * Check if the maximum number of publish event failures has occurred for a resource.
   *
   * @param resource resource
   *
   * @return true if publication has failed the maximum allowed times for a given resource
   */
  boolean hasMaxProcessFailures(Resource resource);

  String calculateChecksum(File file) throws Exception;

  String calculateArchiveChecksum(File archive) throws Exception;

  /*
   * Return the ThreadPoolExecutor.
   *
   * @return the ThreadPoolExecutor
   */
  ThreadPoolExecutor getExecutor();

  /**
   * Return the Futures map, representing all publishing jobs that have been fired.
   *
   * @return the Futures map
   */
  Map<String, Future<Map<String, Integer>>> getProcessFutures();

  /**
   * Return the failures map, representing all publishing jobs that have failed.
   * </br>
   * This map can be queried, to find out which resources have failed publishing jobs.
   * </br>
   * Auto-publication for a resource halts, if there have been 3 failed publish events. A successful publish event run
   * manually, is needed to clear the failed publish events for the resource.
   *
   * @return map of resource name (key) to List of Date when publishing job failed
   */
  ListValuedMap<String, Date> getProcessFailures();

  /**
   * Return the report map.
   *
   * @return map of publication reports
   */
  Map<String, StatusReport> getProcessReports();

  /**
   * Clear the report map.
   */
  void clearProcessReports();

  List<String> detectDuplicateUsesOfUUID(UUID candidate, String shortname);

  /**
   * Update DOI metadata. The DOI URI isn't changed. This is done for each minor version change.
   *
   * @param resource resource whose DOI will be updated
   */
  void doUpdateDoi(Resource resource);

  /**
   * Replace DOI currently assigned to resource with new DOI that has been reserved for resource.
   * This corresponds to a new major version change.
   *
   * @param resource        resource whose DOI will be registered
   * @param version         new version
   * @param replacedVersion previous version being replaced
   */
  void doReplaceDoi(Resource resource, BigDecimal version, BigDecimal replacedVersion);

  /**
   * Register DOI. Corresponds to a major version change.
   *
   * @param resource resource whose DOI will be registered
   */
  void doRegisterDoi(Resource resource, @Nullable DOI replaced);

  void updateNextPublishedDate(Date currentDate, Resource resource) throws PublicationException;
}
