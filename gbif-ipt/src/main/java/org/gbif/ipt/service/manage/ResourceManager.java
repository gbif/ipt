package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;

import com.google.inject.ImplementedBy;
import com.google.inject.internal.Nullable;

import java.io.IOException;
import java.util.List;

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

  public Resource create(String shortname, User creator) throws AlreadyExistingException;

  public void delete(Resource resource) throws IOException;

  public Resource get(String shortname);

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

  public void save(Resource resource) throws IOException;

  /**
   * list all resource that match the given full text search string and optional resource type
   * 
   * @param type
   * @return
   */
  public List<Resource> search(String q, @Nullable ResourceType type);

}
