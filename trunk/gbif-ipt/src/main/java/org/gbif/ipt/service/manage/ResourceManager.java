package org.gbif.ipt.service.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.task.StatusReport;

import com.google.inject.ImplementedBy;
import com.google.inject.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This interface details ALL methods associated with the main resource entity.
 * 
 * The manager keeps a map of the basic metadata and authorisation information in memory, but further details like the
 * full EML or mapping configuration is stored in files and loaded into manager sessions when needed.
 * 
 * @See ResourceManagerSession
 * 
 * @author markus
 */
@ImplementedBy(ResourceManagerImpl.class)
public interface ResourceManager {

  public Resource create(String shortname, File dwca, User creator, BaseAction asction)
      throws AlreadyExistingException, ImportException;

  public Resource create(String shortname, User creator) throws AlreadyExistingException;

  public void delete(Resource resource) throws IOException;

  public Resource get(String shortname);

  /**
   * Returns the URL to a public resource in the IPT
   * 
   * @param shortname
   * @return
   */
  public URL getResourceLink(String shortname);

  /**
   * Returns the map of resources
   * 
   * @return map of resources
   */
  public Map<String, Resource> getResources();

  /**
   * @param shortname
   * @return true if resource is currently locked for any management
   */
  public boolean isLocked(String shortname);

  /**
   * Returns the latest resources , order by last modified
   * 
   * @param startPage
   * @param pageSize
   * @return list of resources
   */
  public List<Resource> latest(int startPage, int pageSize);

  /**
   * list all resources in the IPT having a certain publication status
   * 
   * @param status
   * @return
   */
  public List<Resource> list(PublicationStatus status);

  /**
   * list all resource that can be managed by a given user
   * 
   * @param user
   * @return
   */
  public List<Resource> list(User user);

  /**
   * Load all configured resources from the datadir into memory.
   * We do not keep the EML or mapping configuration in memory for all resources, but we
   * maintain a map of the basic metadata and authorisation information in this manager.
   * 
   */
  public int load();

  /**
   * @param shortname for the resource
   * @return status report of current task either running or on queue for the requested resource or null if none exists
   */
  public StatusReport status(String shortname);

  /**
   * Publishes a new version of a resource including generating a darwin core archive and issuing a new EML version.
   * 
   * @param resource
   * @param action the action to use for logging messages to
   * @return true if a new eml version has been published or false if the content hasnt changed
   * @throws PublicationException if resource was already registered
   */
  public boolean publish(Resource resource, BaseAction action) throws PublicationException;

  /**
   * Registers the resource with gbif
   * 
   * @param resource
   * @param organisation the org that the resource will be associated with
   * @param ipt the ipt that the resource will be published through
   * @param eml eml object
   * @throws InvalidConfigException
   */
  public void register(Resource resource, Organisation organisation, Ipt ipt) throws InvalidConfigException;

  /**
   * Persists the whole resource configuration *but* not the EML file.
   * 
   * @See saveEml(Resource)
   * @param resource
   * @throws InvalidConfigException
   */
  public void save(Resource resource) throws InvalidConfigException;

  /**
   * Save the eml file of a resource only. Complementary method to @See save(Resource)
   * 
   * @param resource
   * @throws InvalidConfigException
   */
  public void saveEml(Resource resource) throws InvalidConfigException;

  /**
   * list all resource that match the given full text search string and optional resource type
   * 
   * @param type
   * @return
   */
  public List<Resource> search(String q, @Nullable String type);

  /**
   * makes a resource private
   * 
   * @param resource
   * @throws InvalidConfigException if resource was already registered
   */
  public void visibilityToPrivate(Resource resource) throws InvalidConfigException;

  /**
   * Makes a resource public
   * 
   * @param resource
   * @throws InvalidConfigException if resource was already registered
   */
  public void visibilityToPublic(Resource resource) throws InvalidConfigException;

}
