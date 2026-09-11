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
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SimplifiedResource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.InvalidMetadataException;
import org.gbif.metadata.eml.InvalidEmlException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * This interface details ALL methods associated with the main resource entity.
 * The manager keeps a map of the basic metadata and authorisation information in memory, but further details like the
 * full EML or mapping configuration is stored in files and loaded into manager sessions when needed.
 */
public interface ResourceManager {


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

  // TODO: why is marked as never used
  /**
   * Validate if the EML file exists for a specific resource in the data directory.
   *
   * @param shortName Resource shortname
   *
   * @return true if EML File exists, and false otherwise.
   */
  boolean isEmlExisting(String shortName);

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
   * list all resources in the IPT by the type.
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(String type);

  /**
   * list all resources in the IPT having a certain publication status.
   *
   * @param status PublicationStatus
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(PublicationStatus status);

  /**
   * List all resources in the IPT whose last published version was public (at the time of publication).
   * </br>
   * If a resource is registered with GBIF, it is assumed the resource is public and therefore is included in the list.
   * Please note only resource published using IPT v2.2 or later store a VersionHistory.
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> listPublishedPublicVersions();

  /**
   * List all resources in the IPT whose last published version was public (at the time of publication). This
   * is used to populate the list of resources publicly shown on the IPT home page.
   * Simplified - contain only fields required for the resources table.
   * </br>
   * If a resource is registered with GBIF, it is assumed the resource is public and therefore is included in the list.
   * Please note only resource published using IPT v2.2 or later store a VersionHistory.
   *
   * @return list of resources wrapped by DatatableResult class
   */
  DatatableResult listPublishedPublicVersionsSimplified(DatatableRequest request);

  /**
   * list all resource that can be managed by a given user.
   *
   * @param user User
   *
   * @return list of resources, or an empty list if none were found
   */
  List<Resource> list(User user);

  /**
   * list all resource that can be managed by a given user.
   *
   * @param user User
   * @param request request parameters
   *
   * @return list of resources wrapped by DatatableResult class
   */
  DatatableResult list(User user, DatatableRequest request);

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
   * Persists the whole resource configuration *but* not the EML file.
   *
   * @param resource Resource
   */
  void save(Resource resource) throws InvalidConfigException;

  /**
   * Save the eml file of a resource only. Complementary method to {@link #save(Resource)}.
   *
   * @param resource Resource
   */
  void saveEml(Resource resource) throws InvalidConfigException;

  /**
   * Save the metadata file of a resource only. Complementary method to {@link #save(Resource)}.
   *
   * @param resource Resource
   */
  void saveDatapackageMetadata(Resource resource) throws InvalidConfigException;

  /**
   * Save the inferred metadata file of a resource. Complementary method to {@link #save(Resource)}
   *
   * @param resource Resource
   */
  void saveInferredMetadata(Resource resource) throws InvalidConfigException;

  /**
   * Replace the EML file in a resource by the provided file
   *
   * @param resource
   * @param emlFile
   * @param validate
   */
  void replaceEml(Resource resource, File emlFile, boolean validate) throws SAXException, ParserConfigurationException, IOException, InvalidEmlException, ImportException;

  /**
   * Replace the datapackage metadata file in a resource by the provided file
   */
  void replaceDatapackageMetadata(BaseAction action, Resource resource, File metadataFile, boolean validate) throws IOException, ImportException, InvalidMetadataException;

  /**
   * Update organisation name and alias for published resources.
   */
  void updateOrganisationNameForResources(UUID organisationKey, String organisationName, String organisationAlias);

  /**
   * Update organisation name and alias for published resources.
   */
  void updateOrganisationNameForResources(Organisation organisation);

  void updateStoredResources(Resource resource);

  void removePublishedPublicVersion(String shortname);

  SimplifiedResource toSimplifiedResourceReconstructedVersion(Resource resource);
}
