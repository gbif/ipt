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
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.task.StatusReport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
   * @param shortname Resource shortName
   * @param action    action
   *
   * @return result of trying to cancel publishing: was successful or not
   *
   * @throws PublicationException if publishing couldn't be cancelled
   */
  boolean cancelPublishing(String shortname, BaseAction action) throws PublicationException;

  /**
   * Create a new Resource.
   *
   * @param shortname Resource's shortName
   * @param dwca      DwC-A file
   * @param creator   Creator User
   * @param action    action
   *
   * @return Resource newly created, or null if it couldn't be created successfully
   *
   * @throws AlreadyExistingException if Resource already existed
   * @throws ImportException          if a problem occurred importing the DwC-A file
   */
  Resource create(String shortname, File dwca, User creator, BaseAction action)
    throws AlreadyExistingException, ImportException;

  /**
   * Create a new Resource.
   *
   * @param shortname Resource's shortName
   * @param creator   Creator User
   *
   * @return Resource newly created, or null if it couldn't be created successfully
   *
   * @throws AlreadyExistingException if Resource already existed
   */
  Resource create(String shortname, User creator) throws AlreadyExistingException;

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
   * Check whether the resource is currently locked or not.
   *
   * @param shortname Resource shortname
   * @param action    the action to use for logging messages
   *
   * @return true if resource is currently locked for any management.
   */
  boolean isLocked(String shortname, BaseAction action);

  /**
   * Check whether the resource is currently locked or not.
   *
   * @param shortname Resource shortname
   *
   * @return true if resource is currently locked for any management.
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
   * list all resources that have been published in the IPT.
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> listPublished();

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
   * @param action   the action to use for logging messages to
   *
   * @return true if a new asynchronous DwC-A generation job has been issued which requires some mapped data
   *
   * @throws PublicationException if resource was already registered
   */
  boolean publish(Resource resource, BaseAction action) throws PublicationException;

  /**
   * Issues a new EML version for the given resource.
   *
   * @param resource Resource
   * @param action   the action to use for logging messages
   *
   * @throws PublicationException if resource was already registered
   */
  void publishMetadata(Resource resource, BaseAction action) throws PublicationException;

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
   * For a published resource, updates its existing DwC-A with the latest eml (presumably generated by publishEml,
   * above). Note that this method does not republish the DwC-A (so no data is refreshed, and no load hits any
   * databases), only repackages with the new eml.
   *
   * @param resource the published resource
   * @param action   the calling action that will receive log messages
   *
   * @throws PublicationException if the resource is locked or hasn't been published
   */
  void updateDwcaEml(Resource resource, BaseAction action) throws PublicationException;

  /**
   * Update the registration of the resource with the GBIF Registry.
   *
   * @param resource the published resource
   * @param action   the action to use for logging messages
   */
  void updateRegistration(Resource resource, BaseAction action);

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
}
