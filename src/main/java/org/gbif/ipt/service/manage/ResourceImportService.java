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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.InvalidMetadataException;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.InvalidEmlException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Builds {@link Resource} instances from raw external input: a Darwin Core Archive, an EML document, a
 * Frictionless/Camtrap data package, a ColDP metadata file, or a bare package descriptor — plus the
 * lower-level metadata parsing/validation/copying steps those formats share.
 *
 * <p>Extracted from {@code ResourceManagerImpl} to separate format-specific import/parsing concerns
 * from core resource CRUD. This service persists the resources it creates (via the injected
 * {@link ResourceManager}), but holds none of the manager's in-memory state itself.</p>
 *
 * <p>Note: creating a resource from an existing <em>IPT resource folder</em> (a folder that is already
 * in the IPT's own on-disk format) is intentionally not part of this service — that operation is really
 * a restore/clone of native IPT state rather than an import of an external format, and it depends on
 * the resource loader, which remains part of {@code ResourceManagerImpl}.</p>
 */
public interface ResourceImportService {

  /**
   * Creates a new, empty resource of the given type and persists it. This is also the starting point used
   * internally by the format-specific {@code createFrom*} methods before they populate sources, mappings,
   * and metadata.
   *
   * @param shortname resource shortname
   * @param type      resource (core) type
   * @param creator   user creating the resource
   * @return the newly created, persisted resource
   * @throws AlreadyExistingException if a resource with this shortname already exists
   */
  Resource createNew(String shortname, String type, User creator) throws AlreadyExistingException;

  /**
   * Creates a new resource from an EML document.
   *
   * @param shortname resource shortname
   * @param emlFile   EML file
   * @param creator   user creating the resource
   * @param alog      action logger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException          if the EML file could not be read/parsed
   */
  Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException;

  /**
   * Creates a new resource from a bare package descriptor file (e.g. {@code datapackage.json}/{@code metadata.yml}
   * supplied on its own, without being wrapped in an archive).
   *
   * @param shortname    resource shortname
   * @param type         resource (core) type
   * @param metadataFile package descriptor file
   * @param creator      user creating the resource
   * @param alog         action logger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException          if the metadata file could not be read/parsed
   */
  Resource createFromPackageDescriptor(String shortname, String type, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException;

  /**
   * Creates a new resource from a bare ColDP metadata file.
   *
   * @param shortname    resource shortname
   * @param metadataFile ColDP metadata file
   * @param creator      user creating the resource
   * @param alog         action logger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException          if the metadata file could not be read/parsed
   */
  Resource createFromColDpMetadata(String shortname, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException;

  /**
   * Creates a new resource from a decompressed Darwin Core Archive.
   *
   * @param shortname resource shortname
   * @param dwca      directory the archive was decompressed into
   * @param creator   user creating the resource
   * @param alog      action logger
   * @return resource created
   * @throws AlreadyExistingException  if the resource created uses a shortname that already exists
   * @throws ImportException           if the archive is invalid or could not be read
   * @throws InvalidFilenameException  if a source file name is invalid
   */
  Resource createFromDwcArchive(String shortname, File dwca, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException;

  /**
   * Creates a new resource from a decompressed Frictionless-family data package (e.g. Camtrap DP).
   *
   * @param shortname    resource shortname
   * @param archiveDir   directory the package was decompressed into
   * @param packageType  resource (core) type
   * @param packageFiles files found in the decompressed package
   * @param creator      user creating the resource
   * @param alog         action logger
   * @return resource created
   * @throws AlreadyExistingException  if the resource created uses a shortname that already exists
   * @throws ImportException           if the package's metadata could not be read/parsed
   * @throws InvalidFilenameException  if a source file name is invalid
   */
  Resource createFromFrictionlessDataPackage(String shortname, File archiveDir, String packageType,
                                             List<File> packageFiles, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException;

  /**
   * Validates an EML file against the GBIF metadata profile matching its declared namespace.
   *
   * @param emlFile EML file
   * @throws SAXException        if failed to create validator
   * @throws IOException         if failed to read EML file
   * @throws InvalidEmlException if EML is invalid
   */
  void validateEmlFile(File emlFile) throws SAXException, ParserConfigurationException, IOException, InvalidEmlException;

  /**
   * Copies an incoming EML file into the resource's directory (as {@code eml.xml}) and parses it into an
   * {@link Eml} instance. If the file is invalid and no valid {@code eml.xml} could be produced, the resource
   * directory is deleted (only if it exclusively contained the invalid file).
   *
   * @param shortname resource shortname
   * @param emlFile   EML file
   * @return populated Eml instance
   * @throws ImportException if the EML file could not be read/parsed
   */
  Eml copyMetadata(String shortname, File emlFile) throws ImportException;

  /**
   * Validates a data package metadata file (and, for ColDP, its accompanying ColDP-specific validation).
   *
   * @param action        current action, used for error reporting
   * @param metadataFile  metadata file
   * @param metadataClass the metadata class to parse the file into
   * @throws IOException               if the file could not be read
   * @throws InvalidMetadataException  if the metadata is invalid
   */
  void validateDatapackageMetadataFile(BaseAction action, File metadataFile,
                                       Class<? extends DataPackageMetadata> metadataClass)
      throws IOException, InvalidMetadataException;

  /**
   * Copies an incoming data package metadata file into the resource's directory and parses it. If the file is
   * invalid, the resource directory is deleted (only if it exclusively contained the invalid file).
   *
   * @param shortname       resource shortname
   * @param metadataFile    metadata file
   * @param datapackageType resource (core) type, used to determine the on-disk filename/metadata class
   * @return populated metadata instance
   * @throws ImportException if the metadata file could not be read/parsed
   */
  DataPackageMetadata copyDatapackageMetadata(String shortname, File metadataFile, String datapackageType)
      throws ImportException;

  /**
   * Infers firstName/lastName fields from a Camtrap contributor's title field and sets them.
   *
   * @param contributor Camtrap contributor
   */
  void inferNameFieldsForCamtrapContributor(CamtrapContributor contributor);
}
