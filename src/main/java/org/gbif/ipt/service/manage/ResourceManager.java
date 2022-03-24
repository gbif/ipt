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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.task.StatusReport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import org.apache.commons.collections4.ListValuedMap;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the main resource entity.
 * The manager keeps a map of the basic metadata and authorisation information in memory, but further details like the
 * full EML or mapping configuration is stored in files and loaded into manager sessions when needed.
 */
@ImplementedBy(ResourceManagerImpl.class)
public interface ResourceManager {

  /**
   * Cancels publishing.
   *
   * @param shortname Resource shortname
   * @param action    action
   *
   * @return result of trying to cancel publishing: was successful or not
   */
  boolean cancelPublishing(String shortname, BaseAction action);

  /**
   * Create a new Resource.
   *
   * @param shortname Resource's shortName
   * @param type      resource type
   * @param dwca      DwC-A file
   * @param creator   Creator User
   * @param action    action
   *
   * @return Resource newly created, or null if it couldn't be created successfully
   *
   * @throws AlreadyExistingException if Resource already existed
   * @throws ImportException          if a problem occurred importing the DwC-A file
   * @throws org.gbif.ipt.service.InvalidFilenameException if source filename contained an illegal character
   */
  Resource create(String shortname, String type, File dwca, User creator, BaseAction action)
    throws AlreadyExistingException, ImportException, InvalidFilenameException;

  /**
   * Create a new Resource.
   *
   * @param shortname Resource's shortName
   * @param type      resource type
   * @param creator   Creator User
   *
   * @return Resource newly created, or null if it couldn't be created successfully
   *
   * @throws AlreadyExistingException if Resource already existed
   */
  Resource create(String shortname, String type, User creator) throws AlreadyExistingException;

  /**
   * Deletes a Resource's data dir.
   *
   * @param resource Resource
   *
   * @throws IOException                 if deletion could not be completed
   */
  void deleteResourceFromIpt(Resource resource) throws IOException;

  /**
   * Deletes a Resource.
   *
   * @param resource Resource
   * @param remove whether the resource folder should be deleted from the data directory during deletion
   *
   * @throws IOException                 if deletion could not be completed
   * @throws DeletionNotAllowedException if deletion was not allowed to be completed
   */
  void delete(Resource resource, boolean remove) throws IOException, DeletionNotAllowedException;

  /**
   * Gets a resource by its shortName.
   *
   * @param shortname Resource shortName
   *
   * @return Resource, or null if none was found for this shortName
   */
  Resource get(String shortname);

  /**
   * Validate if the EML file exists for a specific resource in the data directory.
   *
   * @param shortName Resource shortname
   *
   * @return true if EML File exists, and false otherwise.
   */
  boolean isEmlExisting(String shortName);

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
   * @see org.gbif.ipt.service.manage.ResourceManager#isLocked(String, org.gbif.ipt.action.BaseAction)
   */
  boolean isLocked(String shortname);

  /**
   * Returns the latest resources ,ordered by last modified date.
   *
   * @param startPage start page
   * @param pageSize  page size
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> latest(int startPage, int pageSize);

  /**
   * list all resources in the IPT.
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list();

  /**
   * list all resources in the IPT having a certain publication status.
   *
   * @param status PublicationStatus
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(PublicationStatus status);

  /**
   * List all resources in the IPT whose last published version was public (at the time of publication). This
   * is used to populate the list of resources publicly shown on the IPT home page.
   * </br>
   * If a resource is registered with GBIF, it is assumed the resource is public and therefore is included in the list.
   * Please note only resource published using IPT v2.2 or later store a VersionHistory.
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> listPublishedPublicVersions();

  /**
   * list all resource that can be managed by a given user.
   *
   * @param user User
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(User user);

  /**
   * Load all configured resources from the data directory into memory.
   * We do not keep the EML or mapping configuration in memory for all resources, but we
   * maintain a map of the basic metadata and authorisation information in this manager.
   *
   * @param resourcesDir resources directory (inside data directory)
   * @param creator User that created resource (only used to populate creator when missing)
   * @return number of configured resources loaded into memory
   */
  int load(File resourcesDir, @Nullable User creator);

  /**
   * Publishes a new version of a resource including generating a darwin core archive and issuing a new EML version.
   *
   * @param resource Resource
   * @param version version number of eml/rft/archive to be published
   * @param action   the action to use for logging messages to
   *
   * @return true if a new asynchronous DwC-A generation job has been issued which requires some mapped data
   *
   * @throws PublicationException if resource was already registered
   * @throws InvalidConfigException if resource or metadata could not be saved
   */
  boolean publish(Resource resource, BigDecimal version, @Nullable BaseAction action) throws PublicationException;

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
   * Persists the whole resource configuration *but* not the EML file.
   *
   * @param resource Resource
   */
  void save(Resource resource) throws InvalidConfigException;

  /**
   * Save the eml file of a resource only. Complementary method to @See save(Resource).
   *
   * @param resource Resource
   */
  void saveEml(Resource resource) throws InvalidConfigException;

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
   * Update the resource publicationMode.
   *
   * @param resource resource
   */
  void updatePublicationMode(Resource resource);

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
   * Check if the maximum number of publish event failures has occurred for a resource.
   *
   * @param resource resource
   *
   * @return true if publication has failed the maximum allowed times for a given resource
   */
  boolean hasMaxProcessFailures(Resource resource);

  /**
   * Remove a specific archived version of a resource
   *
   * @param resource
   * @param version
   */
  void removeVersion(Resource resource, BigDecimal version);

  /**
   * Replace the EML file in a resource by the provided file
   *
   * @param resource
   * @param emlFile
   */
  void replaceEml(Resource resource, File emlFile) throws ImportException;
}
