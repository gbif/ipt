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
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;
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
   * Deletes a Resource.
   *
   * @param resource Resource
   *
   * @throws IOException                 if deletion could not be completed
   * @throws DeletionNotAllowedException if deletion was not allowed to be completed
   */
  void delete(Resource resource) throws IOException, DeletionNotAllowedException;

  /**
   * Gets a resource by its shortName.
   *
   * @param shortname Resource shortName
   *
   * @return Resource, or null if none was found for this shortName
   */
  Resource get(String shortname);

  /**
   * Return the size of the generated DwC-A file.
   *
   * @param resource Resource
   *
   * @return size of DwC-A file
   */
  long getDwcaSize(Resource resource);

  /**
   * Return the size of the generated EML file.
   *
   * @param resource Resource
   *
   * @return size of EML file
   */
  long getEmlSize(Resource resource);

  /**
   * Construct a resource link (identifier) using its shortname and return it.
   *
   * @param shortname Resource shortname
   *
   * @return Link (identifier) to resource, or null if none could be constructed
   */
  URL getResourceLink(String shortname);

  /**
   * Construct a public resource link using its shortname and return it.
   *
   * @param shortname Resource shortname
   *
   * @return Public URL to resource, or null if none could be constructed
   */
  URL getPublicResourceLink(String shortname);

  /**
   * Returns the size of the generated RTF file.
   *
   * @param resource Resource
   *
   * @return size of RTF file
   */
  long getRtfSize(Resource resource);

  /**
   * Validate if the EML file exist for a specific resource in the data directory.
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
   * Validate if the RTF existence for a specific resource in the data directory.
   *
   * @param shortName Resource shortname
   *
   * @return true if RTF File exists, and false otherwise.
   */
  boolean isRtfExisting(String shortName);

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
   * list all resource that can be managed by a given user.
   *
   * @param user User
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(User user);

  /**
   * Load all configured resources from the datadir into memory.
   * We do not keep the EML or mapping configuration in memory for all resources, but we
   * maintain a map of the basic metadata and authorisation information in this manager.
   *
   * @return number of configured resource loaded into memory
   */
  int load();

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
   * This method:
   * 1) replaces the current (published) DwC-A with the previous version
   * 2) replaces the current (published) RTF file with the previous version
   * 3) replaces the current (published) eml.xml file with the previous version. The interim eml.xml is unchanged.
   * 4) removes the archived version.
   * This method must be called when publication fails, for whatever reason. A new published version can only be
   * created, when all stages of publication were successful (metadata, and DwC-A).
   * </br>
   * The resource's version and last publication date must be updated, and persisted to the eml.xml and resource.xml
   * file. Don't replace the existing interim eml.xml file, only update it.
   *
   * @param resource resource
   * @param version  version to restore
   * @param action   action
   */
  void restoreVersion(Resource resource, BigDecimal version, @Nullable BaseAction action);

  /**
   * Turn resource publicationMode to OFF.
   *
   * @param resource resource
   */
  void publicationModeToOff(Resource resource);

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
   * Updates the resource's alternate identifier for its DOI and saves the EML if the DOI status is RESERVED or PUBLIC.
   * This identifier should only exist for the resource, if its resource visibility is public.
   *
   * If called on a resource that already has an existing DOI (e.g. DOI for an article, or a previous major version)
   * the method will add it to the list since a resource is allowed to have multiple DOIs.
   *
   * @param resource resource
   * @return resource with DOI for the resource updated
   */
  Resource updateAlternateIdentifierForDOI(Resource resource);

  /**
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
  Map<String, Future<Integer>> getProcessFutures();

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
  ListMultimap<String, Date> getProcessFailures();

  /**
   * Check if the maximum number of publish event failures has occurred for a resource.
   *
   * @param resource resource
   *
   * @return true if publication has failed the maximum allowed times for a given resource
   */
  boolean hasMaxProcessFailures(Resource resource);
}
