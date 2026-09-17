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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.MetadataFiles;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceImportService;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.service.manage.ResourceTypeService;
import org.gbif.ipt.service.manage.ResourceVersioningService;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.IptFileUtils;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.KeywordSet;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.DataDir.COL_DP_METADATA_FILENAME;
import static org.gbif.ipt.config.DataDir.EML_XML_FILENAME;
import static org.gbif.ipt.config.DataDir.FRICTIONLESS_METADATA_FILENAME;
import static org.gbif.ipt.utils.IptFileUtils.getFileExtension;
import static org.gbif.ipt.utils.MetadataUtils.metadataClassForType;

public class ResourceManagerImpl extends BaseManager implements ResourceManager {

  private final ResourceIndex resourceIndex = new ResourceIndex();

  private final XStream xstream;

  private final ExtensionManager extensionManager;
  private final DataPackageSchemaManager schemaManager;
  private final RegistryManager registryManager;
  private final SimpleTextProvider textProvider;
  private final RegistrationManager registrationManager;
  private final MetadataReader metadataReader;
  private final ResourceImportService resourceImportService;
  private final ResourceVersioningService resourceVersioningService;
  private final ResourceTypeService resourceTypeService;
  private final ResourceMetadataLoader resourceMetadataLoader;

  public static final SimpleDateFormat CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public ResourceManagerImpl(
      AppConfig cfg,
      DataDir dataDir,
      ResourceConvertersManager resourceConvertersManager,
      ExtensionManager extensionManager,
      DataPackageSchemaManager schemaManager,
      RegistryManager registryManager,
      PasswordEncrypter passwordEncrypter,
      SimpleTextProvider textProvider,
      RegistrationManager registrationManager,
      MetadataReader metadataReader,
      ResourceImportService resourceImportService,
      ResourceVersioningService resourceVersioningService,
      ResourceTypeService resourceTypeService,
      ResourceMetadataLoader resourceMetadataLoader) {
    super(cfg, dataDir);
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.registryManager = registryManager;
    this.xstream = ResourceXStreamFactory.create(resourceConvertersManager, passwordEncrypter);
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
    this.metadataReader = metadataReader;
    this.resourceImportService = resourceImportService;
    this.resourceVersioningService = resourceVersioningService;
    this.resourceTypeService = resourceTypeService;
    this.resourceMetadataLoader = resourceMetadataLoader;
  }

  private void addResource(Resource res) {
    resourceIndex.put(res);
    // add only public/registered resources with at least one published version
    try {
      if (!res.getVersionHistory().isEmpty()) {
        VersionHistory latestVersion = res.getVersionHistory().get(0);
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE) &&
            latestVersion.getReleased() != null) {
          resourceIndex.putPublishedPublicSummary(res.getShortname(), toResourceSummaryViewReconstructed(res));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to reconstruct resource's last published version", e);
    }
  }

  @Override
  public void updateOrganisationNameForResources(Organisation organisation) {
    updateOrganisationNameForResources(organisation.getKey(), organisation.getName(), organisation.getAlias());
  }

  @Override
  public void updateOrganisationNameForResources(UUID organisationKey, String organisationName, String organisationAlias) {
    resourceIndex.updateOrganisationName(organisationKey, organisationName, organisationAlias);
  }

  /**
   * Converts regular Resource to lightweight SimplifiedResource.
   * Reconstructs resource from the last published EML to take data before it was changed.
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  @Override
  public ResourceSummaryView toResourceSummaryViewReconstructed(Resource resource) {
    BigDecimal v = resource.getLastPublishedVersionsVersion();
    String shortname = resource.getShortname();

    File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, v);
    File versionDatapacakgeFile = cfg.getDataDir().resourceDatapackageMetadataFile(shortname, resource.getCoreType(), v);
    MetadataFiles metadataFiles = MetadataFiles.builder()
        .eml(versionEmlFile)
        .datapackage(versionDatapacakgeFile)
        .build();

    Resource publishedPublicVersion = ResourceUtils
        .reconstructVersion(v, resource.getShortname(), resource.getCoreType(), resource.getDataPackageIdentifier(), resource.getAssignedDoi(), resource.getOrganisation(),
            resource.findVersionHistory(v), metadataFiles, resource.getKey());

    ResourceSummaryView result = new ResourceSummaryView();
    result.setShortname(publishedPublicVersion.getShortname());
    result.setTitle(publishedPublicVersion.getTitle());
    result.setStatus(publishedPublicVersion.getStatus());
    result.setRecordsPublished(publishedPublicVersion.getRecordsPublished());
    result.setLogoUrl(publishedPublicVersion.getLogoUrl());
    result.setSubject(publishedPublicVersion.getSubject());
    if (publishedPublicVersion.getOrganisation() != null) {
      result.setOrganisationKey(publishedPublicVersion.getOrganisation().getKey());
      result.setOrganisationName(publishedPublicVersion.getOrganisationName());
      result.setOrganisationAlias(publishedPublicVersion.getOrganisationAlias());
    }
    result.setCoreType(resource.getCoreType());
    result.setSubtype(resource.getSubtype());
    result.setModified(resource.getModified());
    result.setPublished(true);
    result.setLastPublished(publishedPublicVersion.getLastPublished());
    result.setNextPublished(resource.getNextPublished());
    result.setCreatorName(resource.getCreatorName());
    result.setDataPackage(resource.isDataPackage());

    // was the last published version later registered but never republished? Fix for issue #1319
    if (!publishedPublicVersion.isRegistered() && resource.isRegistered() && resource.getOrganisation() != null) {
      result.setStatus(PublicationStatus.REGISTERED);
      result.setOrganisationAlias(resource.getOrganisationAlias());
      result.setOrganisationName(resource.getOrganisationName());
    }

    return result;
  }

  /**
   * Close the file writer if the writer is not null.
   *
   * @param writer file writer
   */
  private void closeWriter(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  @Override
  public Resource create(String shortname, String type, File archiveOrSingleFile, User creator, BaseAction action)
      throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    ActionLogger alog = new ActionLogger(this.LOG, action);
    Resource resource;
    // decompress archive
    List<File> decompressed = null;
    File archiveDir = dataDir.tmpDir();
    try {
      decompressed = CompressionUtil.decompressFile(archiveDir, archiveOrSingleFile, true);
    } catch (UnsupportedCompressionType e) {
      LOG.debug("1st attempt to decompress file failed: {}", e.getMessage(), e);
    } catch (Exception e) {
      LOG.debug("Decompression failed: {}", e.getMessage(), e);
    }

    if (CollectionUtils.isEmpty(decompressed)) {
      // try again as single gzip file
      try {
        decompressed = CompressionUtil.ungzipFile(archiveDir, archiveOrSingleFile, false);
      } catch (Exception e2) {
        LOG.debug("2nd attempt to decompress file failed: {}", e2.getMessage(), e2);
      }
    }

    // create resource:
    // if decompression failed, create resource from single file: eml.xml, datapackage.json or metadata.yml
    if (CollectionUtils.isEmpty(decompressed)) {
      String fileExtension = getFileExtension(archiveOrSingleFile);

      resource = switch (fileExtension) {
        case "xml" -> resourceImportService.createFromEml(shortname, archiveOrSingleFile, creator, alog);
        case "json" -> resourceImportService.createFromPackageDescriptor(shortname, type, archiveOrSingleFile, creator, alog);
        case "yml" -> resourceImportService.createFromColDpMetadata(shortname, archiveOrSingleFile, creator, alog);
        default -> throw new ImportException("Invalid file extension: " + fileExtension);
      };
    }
    // if decompression succeeded and archive is 'IPT Resource Folder'
    else if (isIPTResourceFolder(archiveDir)) {
      resource = createFromIPTResourceFolder(shortname, archiveDir, creator, alog);
    }
    // if decompression succeeded, create resource depending on whether file was a 'DwC-A',
    // a frictionless package (Camtrap DP) or a ColDP
    else {
      if (MetadataUtils.isDataPackageType(type)) {
        resource = resourceImportService.createFromFrictionlessDataPackage(shortname, archiveDir, type, decompressed, creator, alog);
      } else {
        resource = resourceImportService.createFromDwcArchive(shortname, archiveDir, creator, alog);
      }
    }

    // set resource type if it hasn't been set already
    if (type != null && StringUtils.isBlank(resource.getCoreType())) {
      resource.setCoreType(type);
    }

    return resource;
  }

  /**
   * Creates a resource from an IPT Resource folder. The purpose is to preserve the original source files and mappings.
   * The managers, created date, last publication date, version history, version number, DOI(s), publication status,
   * and registration info are all cleared. The creator and modifier are set to the current creator.
   * </p>
   * This method must ensure that the folder has a unique name relative to the other resource's shortnames, otherwise
   * it tries to rename the folder using the supplied shortname. If neither of these yield a unique shortname,
   * an exception is thrown alerting the user they should try again with a unique name.
   *
   * @param shortname resource shortname
   * @param folder    IPT resource folder
   * @param creator   Creator
   * @param alog      action logging
   * @return Resource created or null if it was unsuccessful
   * @throws AlreadyExistingException if a unique shortname could not be determined
   * @throws ImportException          if a problem occurred trying to create the new Resource
   */
  private Resource createFromIPTResourceFolder(String shortname, File folder, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Resource res;
    try {

      // shortname supplied is unique?
      if (resourceIndex.contains(shortname)) {
        throw new AlreadyExistingException();
      }

      // copy folder (renamed using shortname) to resources directory in data_dir
      File dest = new File(dataDir.dataFile(DataDir.RESOURCES_DIR), shortname);
      FileUtils.copyDirectory(folder, dest);

      // proceed with resource creation (using destination folder in data_dir)
      res = loadFromDir(dest, creator, alog);

      // ensure this resource is safe to import!
      if (res != null) {
        // remove all managers associated to resource
        res.getManagers().clear();
        // change creator to the User that uploaded resource
        res.setCreator(creator);
        // change modifier to User that uploaded resource
        res.setModifier(creator);
        // change creation date
        res.setCreated(new Date());
        // resource has never been published - set last published date to null
        res.setLastPublished(null);
        // reset organization
        res.setOrganisation(null);
        // clear registration
        res.setKey(null);
        // set publication status to Private
        res.setStatus(PublicationStatus.PRIVATE);
        // set number of records published to 0
        res.setRecordsPublished(0);
        // reset version number
        res.setMetadataVersion(Constants.INITIAL_RESOURCE_VERSION);
        // reset DOI
        res.setDoi(null);
        res.setIdentifierStatus(IdentifierStatus.UNRESERVED);
        res.setDoiOrganisationKey(null);
        // reset change summary
        res.setChangeSummary(null);
        // remove all VersionHistory
        res.getVersionHistory().clear();
        // turn off auto-publication
        res.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
        res.setUpdateFrequency(null);
        res.setNextPublished(null);
        // reset other last modified dates
        Date lastModifiedDate = new Date();
        res.setMetadataModified(lastModifiedDate);
        res.setMappingsModified(lastModifiedDate);
        res.setSourcesModified(lastModifiedDate);
        res.getSources().forEach(s -> s.setLastModified(lastModifiedDate));
        res.getMappings().forEach(m -> m.setLastModified(lastModifiedDate));
        res.getDataPackageMappings().forEach(m -> m.setLastModified(lastModifiedDate));

        if (!res.isDataPackage()) {
          // reset first and last published dates
          res.getEml().setDateStamp((Date) null);
          res.getEml().setPubDate(null);
        }

        // add resource to IPT
        save(res);
      }

    } catch (InvalidConfigException e) {
      alog.error(e.getMessage(), e);
      throw new ImportException(e);
    } catch (IOException e) {
      alog.error("Could not copy resource folder into data directory: " + e.getMessage(), e);
      throw new ImportException(e);
    }

    return res;
  }

  /**
   * Determine whether the directory represents an IPT Resource directory or not. To qualify, a directory must contain
   * a resource.xml file and one of the metadata files: eml.xml/datapackage.json/metadata.yml
   *
   * @param dir directory where a compressed file was decompressed
   * @return true if it is an IPT Resource folder or false otherwise
   */
  private boolean isIPTResourceFolder(File dir) {
    if (dir.exists() && dir.isDirectory()) {
      File persistenceFile = new File(dir, DataDir.PERSISTENCE_FILENAME);
      File emlFile = new File(dir, EML_XML_FILENAME);
      File datapackageDescriptorFile = new File(dir, FRICTIONLESS_METADATA_FILENAME);
      File colDpMetadataFile = new File(dir, COL_DP_METADATA_FILENAME);

      return persistenceFile.isFile() &&
          (emlFile.isFile() || datapackageDescriptorFile.isFile() || colDpMetadataFile.isFile());
    }
    return false;
  }

  @Override
  public Resource create(String shortname, String type, User creator) throws AlreadyExistingException {
    return resourceImportService.createNew(shortname, type, creator);
  }

  /**
   * Replace the EML file in a resource by the provided file.
   * Validation is optional.
   */
  @Override
  public void replaceEml(Resource resource, File emlFile, boolean validate) throws SAXException, ParserConfigurationException, IOException, InvalidEmlException, ImportException {
    if (validate) {
      resourceImportService.validateEmlFile(emlFile);
    }
    // copy eml file to data directory (with name eml.xml) and populate Eml instance
    Eml eml = resourceImportService.copyMetadata(resource.getShortname(), emlFile);
    resource.setEml(eml);
    resource.setMetadataModified(new Date());
    save(resource);
    saveEml(resource, true);
  }

  @Override
  public void replaceDatapackageMetadata(BaseAction action, Resource resource, File metadataFile, boolean validate)
      throws IOException, ImportException, org.gbif.ipt.service.InvalidMetadataException {
    if (validate) {
      resourceImportService.validateDatapackageMetadataFile(action, metadataFile, metadataClassForType(resource.getCoreType()));
    }
    DataPackageMetadata metadata = resourceImportService.copyDatapackageMetadata(resource.getShortname(), metadataFile, resource.getCoreType());

    if (metadata instanceof ColMetadata colMetadata) {
      colMetadata.setVersion(resource.getDataPackageMetadata().getVersion());
    }

    if (metadata instanceof FrictionlessMetadata frictionlessMetadata) {
      // set name, erase some internal fields
      frictionlessMetadata.setName(resource.getShortname());
      frictionlessMetadata.setId(null);
      frictionlessMetadata.setCreated(null);
      frictionlessMetadata.getAdditionalProperties().clear();
      frictionlessMetadata.setVersion(resource.getDataPackageMetadata().getVersion());
    }

    if (metadata instanceof CamtrapMetadata camtrapMetadata) {
      camtrapMetadata.getContributors().stream()
          .map(contributor -> (CamtrapContributor) contributor)
          .filter(contributor -> CamtrapContributor.Role.CITATION_ROLES.contains(contributor.getRole()))
          .forEach(resourceImportService::inferNameFieldsForCamtrapContributor);
    }

    resource.setDataPackageMetadata(metadata);
    // do not automatically infer scope metadata
    resource.setInferGeocoverageAutomatically(false);
    resource.setInferTaxonomicCoverageAutomatically(false);
    resource.setInferTemporalCoverageAutomatically(false);
    resource.setMetadataModified(new Date());
    save(resource);
    saveDatapackageMetadata(resource);
  }

  @Override
  public void deleteResourceFromIpt(Resource resource) throws IOException {
    // remove from data dir
    FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
    // remove object
    resourceIndex.remove(resource.getShortname());
  }

  @Override
  public void delete(Resource resource, boolean remove) throws IOException, DeletionNotAllowedException {
    // deregister resource?
    if (resource.isRegistered()) {
      try {
        registryManager.deregister(resource);
      } catch (RegistryException e) {
        LOG.error("Failed to deregister resource: {}", e.getMessage(), e);
        throw new DeletionNotAllowedException(Reason.REGISTRY_ERROR, e.getMessage());
      }
    }

    // remove from data dir?
    if (remove) {
      FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
      // remove object
      resourceIndex.remove(resource.getShortname());
    }
  }

  @Override
  public Resource get(String shortname) {
    return resourceIndex.get(shortname);
  }

  @Override
  public boolean isEmlExisting(String shortName) {
    File emlFile = dataDir.resourceEmlFile(shortName);
    return emlFile.exists();
  }

  @Override
  public List<Resource> latest(int startPage, int pageSize) {
    return resourceIndex.latest(startPage, pageSize);
  }

  @Override
  public List<Resource> list() {
    return resourceIndex.list();
  }

  @Override
  public List<ResourceSummaryView> listPublishedPublicResourceSummaries() {
    return resourceIndex.listPublishedPublicResourceSummaries();
  }

  @Override
  public List<Resource> list(String type) {
    return resourceIndex.list(type);
  }

  @Override
  public List<Resource> list(PublicationStatus status) {
    return resourceIndex.list(status);
  }

  @Override
  public List<Resource> listPublishedPublicVersions() {
    return resourceIndex.listPublishedPublicVersions();
  }

  @Override
  public List<Resource> list(User user) {
    return resourceIndex.list(user);
  }

  @Override
  public int load(File resourcesDir, User creator) {
    resourceIndex.clear();
    int counter = 0;
    int counterDeleted = 0;
    File[] files = resourcesDir.listFiles();
    if (files != null) {
      for (File resourceDir : files) {
        if (resourceDir.isDirectory()) {
          // list of files and folders in resource directory, excluding .DS_Store
          File[] resourceDirFiles = resourceDir.listFiles((dir, name) -> !name.equalsIgnoreCase(".DS_Store"));

          if (resourceDirFiles == null) {
            LOG.error("Resource directory {} could not be read. Please verify its content", resourceDir.getName());
          } else if (resourceDirFiles.length == 0) {
            LOG.warn("Cleaning up empty resource directory {}", resourceDir.getName());
            FileUtils.deleteQuietly(resourceDir);
            counterDeleted++;
          } else {
            try {
              LOG.debug("Loading resource from directory {}", resourceDir.getName());
              addResource(loadFromDir(resourceDir, creator));
              counter++;
            } catch (InvalidConfigException e) {
              LOG.error("Can't load resource {}", resourceDir.getName(), e);
            }
          }
        }
      }
      LOG.info("Loaded {} resources into memory altogether.", counter);
      LOG.info("Cleaned up {} resources altogether.", counterDeleted);
    } else {
      LOG.error("Data directory does not hold a resources directory: {}", dataDir.dataFile(""));
    }
    return counter;
  }

  /**
   * Loads a resource's inferred metadata from the XML file located inside its resource directory.
   * If no inferredMetadata.xml file was found, the resource is loaded with an empty InferredMetadata instance.
   *
   * @param resource resource
   */
  public void loadInferredMetadata(Resource resource) {
    File inferredMetadataFile = dataDir.resourceInferredMetadataFile(resource.getShortname());

    if (resource.isDataPackage()) {
      // skip non-camtrap resources
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        return;
      }

      // no metadata file found - initialize with an empty object
      if (!inferredMetadataFile.exists()) {
        resource.setInferredMetadata(new InferredCamtrapMetadata());
        return;
      }

      // otherwise read the metadata file
      try {
        InputStream input = Files.newInputStream(inferredMetadataFile.toPath());
        InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) xstream.fromXML(input);
        resource.setInferredMetadata(inferredMetadata);
      } catch (Exception e) {
        LOG.error("Cannot read inferred metadata file (Camtrap) for resource {}", resource.getShortname(), e);
        resource.setInferredMetadata(new InferredCamtrapMetadata());
      }
    } else {
      if (inferredMetadataFile == null || !inferredMetadataFile.exists()) {
        resource.setInferredMetadata(new InferredEmlMetadata());
        return;
      }

      try {
        InputStream input = Files.newInputStream(inferredMetadataFile.toPath());
        InferredEmlMetadata inferredMetadata = (InferredEmlMetadata) xstream.fromXML(input);
        resource.setInferredMetadata(inferredMetadata);
      } catch (Exception e) {
        LOG.error("Cannot read inferred metadata file (EML) for resource {}", resource.getShortname(), e);
        resource.setInferredMetadata(new InferredEmlMetadata());
      }
    }
  }

  /**
   * Change resource status to REGISTERED and update the organization.
   */
  @Override
  public void updateStoredResources(Resource resource) {
    ResourceSummaryView resourceSummaryView = resourceIndex.getPublishedPublicSummary(resource.getShortname());
    if (resourceSummaryView != null) {
      resourceSummaryView.setStatus(PublicationStatus.REGISTERED);
      resourceSummaryView.setOrganisationAlias(resource.getOrganisationAlias());
      resourceSummaryView.setOrganisationName(resource.getOrganisationName());
    }
  }

  @Override
  public void removePublishedPublicVersion(String shortname) {
    resourceIndex.removePublishedPublicSummary(shortname);
  }

  /**
   * Calls loadFromDir(File, User, ActionLogger), inserting a new instance of ActionLogger.
   *
   * @param resourceDir resource directory
   * @param creator     User that created resource (only used to populate creator when missing)
   * @return loaded Resource
   */
  protected Resource loadFromDir(File resourceDir, @Nullable User creator) {
    return loadFromDir(resourceDir, creator, new ActionLogger(LOG, new BaseAction(textProvider, cfg, registrationManager)));
  }

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache.
   */
  private Resource loadFromDir(File resourceDir, @Nullable User creator, ActionLogger alog) throws InvalidConfigException {
    if (resourceDir.exists()) {
      // load full configuration from resource.xml and eml.xml files
      String shortname = resourceDir.getName();
      try {
        File cfgFile = dataDir.resourceFile(shortname);
        InputStream input = new FileInputStream(cfgFile);
        Resource resource = (Resource) xstream.fromXML(input);

        // populate a missing creator - it cannot be null! (this fixes issue #1309)
        if (creator != null && resource.getCreator() == null) {
          resource.setCreator(creator);
          LOG.warn("On load, populated missing creator for resource: {}", shortname);
        }

        // non-existing users end up being a NULL in the set, so remove them
        // shouldn't really happen - but people can even manually cause a mess
        resource.getManagers().remove(null);

        // 1. Non-existent Extension ends up being NULL
        // E.g., a user is trying to import a resource from one IPT to another without all required extensions installed.
        // 2. Auto-generating IDs are only available for Taxon core extension since IPT v2.1,
        // therefore, if a non-Taxon core extension is using auto-generated IDs, the coreID is set to No ID (-99)
        for (ExtensionMapping ext : resource.getMappings()) {
          Extension x = ext.getExtension();
          if (x == null) {
            alog.warn("manage.resource.create.extension.null", new String[]{ext.getExtensionVerbatim()});
            throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-existent extension");
          } else if (extensionManager.get(x.getRowType()) == null) {
            alog.warn("manage.resource.create.rowType.null", new String[]{x.getRowType()});
            throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-installed extension");
          }
          // is the ExtensionMapping of core type, not taxon core type, and uses a coreIdColumn mapping?
          if (ext.isCore() && !ext.isTaxonCore() && ext.getIdColumn() != null) {
            if (ext.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER) || ext.getIdColumn()
                .equals(ExtensionMapping.IDGEN_UUID)) {
              ext.setIdColumn(ExtensionMapping.NO_ID);
            }
          }
        }

        // shortname persists as folder name, so xstream doesn't handle this:
        resource.setShortname(shortname);

        // infer coreType if null
        if (resource.getCoreType() == null) {
          resourceTypeService.inferCoreType(resource);
        }

        // standardize subtype if not null
        if (resource.getSubtype() != null) {
          resourceTypeService.standardizeSubtype(resource);
        }

        // add proper source file pointer
        for (Source src : resource.getSources()) {
          src.setResource(resource);
          src.setProcessing(false);
          if (src instanceof FileSource frSrc) {
            frSrc.setFile(dataDir.sourceFile(resource, frSrc));
          }
        }

        // pre v2.2 resources: set IdentifierStatus if null
        if (resource.getIdentifierStatus() == null) {
          resource.setIdentifierStatus(IdentifierStatus.UNRESERVED);
        }

        // load metadata (this must be done before trying to convert version below)
        resourceMetadataLoader.loadMetadata(resource);

        // load inferred metadata
        loadInferredMetadata(resource);

        // pre v2.2 resources: convert resource version from integer to major_version.minor_version style
        // also convert/rename eml, rtf, and dwca versioned files also
        if (!resource.isDataPackage()) {
          BigDecimal converted = resourceVersioningService.convertVersion(resource);
          if (converted != null) {
            resourceVersioningService.updateResourceVersion(resource, resource.getMetadataVersion(), converted);
          }
        }

        // pre v2.2 resources: construct a VersionHistory for last published version (if appropriate)
        VersionHistory history = resourceVersioningService.constructVersionHistoryForLastPublishedVersion(resource);
        if (history != null) {
          resource.addVersionHistory(history);
        }

        if (!resource.isDataPackage()) {
          // pre v2.2.1 resources: rename dwca.zip to dwca-18.0.zip (where 18.0 is the last published version for example)
          if (resource.getLastPublishedVersionsVersion() != null) {
            resourceVersioningService.renameDwcaToIncludeVersion(resource, resource.getLastPublishedVersionsVersion());
          }

          // update EML with the latest resource basics (version and GUID)
          syncEmlWithResource(resource);
        }

        // clean up data package mappings (remove dangling field mappings)
        // backfill data package version if not set
        if (resource.isDataPackage()) {
          cleanUpDataPackageMappings(resource);
          backfillDataPackageVersion(resource);
        }

        LOG.debug("Read resource configuration for {}", shortname);
        return resource;
      } catch (Exception e) {
        LOG.error("Cannot read resource configuration for {}", shortname, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG,
            "Cannot read resource configuration for " + shortname + ": " + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Remove field mappings from mappings that do not reference any actual fields.
   * <ol>
   *   <li>Only an index is present, but references no field</li>
   *   <li>Both index and field are absent</li>
   * </ol>
   *
   * @param resource resource
   */
  private void cleanUpDataPackageMappings(Resource resource) {
    for (DataPackageMapping dpm : resource.getDataPackageMappings()) {
      dpm.getFields().removeIf(f -> onlyFieldIndexPresent(f) || emptyMapping(f));
    }
  }

  /**
   * Backfill the data package version if not set.
   *
   * @param resource resource
   */
  private void backfillDataPackageVersion(Resource resource) {
    if (resource.getDataPackageVersion() == null) {
      String identifier = resource.getDataPackageIdentifier();
      String installedVersion = schemaManager.getVersion(identifier);
      if (installedVersion != null) {
        resource.setDataPackageVersion(installedVersion);
        save(resource);
        LOG.warn("Backfilled dataPackageVersion={} for resource {} (schema {})",
            installedVersion, resource.getShortname(), identifier);
      } else {
        LOG.error("Could not backfill dataPackageVersion for resource {}: schema {} not installed",
            resource.getShortname(), identifier);
      }
    }
  }

  private boolean onlyFieldIndexPresent(DataPackageFieldMapping dpfm) {
    return dpfm.getIndex() != null && dpfm.getField() == null && StringUtils.isEmpty(dpfm.getDefaultValue());
  }

  private boolean emptyMapping(DataPackageFieldMapping dpfm) {
    return dpfm.getIndex() == null && dpfm.getField() == null && StringUtils.isEmpty(dpfm.getDefaultValue());
  }

  @Override
  public synchronized void save(Resource resource) throws InvalidConfigException {
    File cfgFile = dataDir.resourceFile(resource);
    try {
      // make sure resource dir exists
      FileUtils.forceMkdir(cfgFile.getParentFile());
      // persist data
      try (Writer writer = IptFileUtils.startNewUtf8File(cfgFile)) {
        xstream.toXML(resource, writer);
        // add to internal map
        addResource(resource);
      }
    } catch (IllegalArgumentException e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, e.getMessage());
    } catch (Exception e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write mapping configuration");
    }
  }

  @Override
  public synchronized void saveInferredMetadata(Resource resource) throws InvalidConfigException {
    File cfgFile = dataDir.resourceInferredMetadataFile(resource.getShortname());
    Writer writer = null;
    try {
      // make sure resource dir exists
      FileUtils.forceMkdir(cfgFile.getParentFile());
      // persist data
      writer = IptFileUtils.startNewUtf8File(cfgFile);
      xstream.toXML(resource.getInferredMetadata(), writer);
    } catch (IOException e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write inferred metadata file");
    } finally {
      if (writer != null) {
        closeWriter(writer);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void saveEml(Resource resource) throws InvalidConfigException {
    saveEml(resource, false);
  }

  private synchronized void saveEml(Resource resource, boolean preserveKeywords) throws InvalidConfigException {
    // update EML with the latest resource basics (version and GUID)
    syncEmlWithResource(resource, preserveKeywords);
    // set modified date
    resource.setModified(new Date());
    // save into data dir
    File emlFile = dataDir.resourceEmlFile(resource.getShortname());
    // Locale.US it's used because uses '.' as the decimal separator
    EmlUtils.writeWithLocale(emlFile, resource, Locale.US);
    LOG.debug("Updated EML file for {}", resource);
  }

  @Override
  public synchronized void saveDatapackageMetadata(Resource resource) {
    // set modified date
    resource.setModified(new Date());
    // save into data dir
    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    try {
      metadataReader.writeValue(metadataFile, resource.getDataPackageMetadata());
    } catch (IOException e) {
      LOG.error("Failed to save datapackage metadata!", e);
      throw new RuntimeException(e);
    }

    LOG.debug("Updated metadata file for {}", resource);
  }

  /**
   * Updates the EML version and EML GUID. The GUID is set to the Registry UUID if the resource is
   * registered, otherwise it is set to the resource URL.
   * </br>
   * This method also updates the EML list of KeywordSet with the dataset type and subtype.
   * </br>
   * This method must be called before persisting the EML file to ensure that the EML file and resource are in sync.
   *
   * @param resource         Resource
   * @param preserveKeywords perform keywords' update or not
   */
  private void syncEmlWithResource(Resource resource, boolean preserveKeywords) {
    // set EML version
    resource.getEml().setEmlVersion(resource.getEmlVersion());
    // we need some GUID: use the registry key if resource is registered, otherwise use the resource URL
    if (resource.getKey() != null) {
      resource.getEml().setGuid(resource.getKey().toString());
    } else {
      resource.getEml().setGuid(cfg.getResourceGuid(resource.getShortname()));
    }
    if (!preserveKeywords && !resource.isDataPackage()) {
      // add/update KeywordSet for dataset type and subtype
      updateKeywordsWithDatasetTypeAndSubtype(resource);
    }
  }

  private void syncEmlWithResource(Resource resource) {
    syncEmlWithResource(resource, false);
  }

  /**
   * Try to add/update/remove KeywordSet for dataset type and subtype.
   *
   * @param resource resource
   * @return resource whose Eml list of KeywordSet has been updated depending on the presence of a dataset type or subtype
   */
  private Resource updateKeywordsWithDatasetTypeAndSubtype(Resource resource) {
    Eml eml = resource.getEml();
    if (eml != null) {
      // retrieve a list of the resource's KeywordSet
      List<KeywordSet> keywords = eml.getKeywords();
      if (keywords != null) {
        // add or update KeywordSet for dataset type
        String type = resource.getCoreType();
        if (StringUtils.isNotBlank(type)) {
          EmlUtils.addOrUpdateKeywordSet(keywords, type, Constants.THESAURUS_DATASET_TYPE);
          LOG.debug("GBIF Dataset Type Vocabulary added/updated to Resource's list of keywords");
        }
        // its absence means that it must be removed (if it exists)
        else {
          EmlUtils.removeKeywordSet(keywords, Constants.THESAURUS_DATASET_TYPE);
          LOG.debug("GBIF Dataset Type Vocabulary removed from Resource's list of keywords");
        }

        // add or update KeywordSet for dataset subtype
        String subtype = resource.getSubtype();
        if (StringUtils.isNotBlank(subtype)) {
          EmlUtils.addOrUpdateKeywordSet(keywords, subtype, Constants.THESAURUS_DATASET_SUBTYPE);
          LOG.debug("GBIF Dataset Subtype Vocabulary added/updated to Resource's list of keywords");
        }
        // its absence means that it must be removed (if it exists)
        else {
          EmlUtils.removeKeywordSet(keywords, Constants.THESAURUS_DATASET_SUBTYPE);
          LOG.debug("GBIF Dataset Type Vocabulary removed from Resource's list of keywords");
        }
      }
    }
    return resource;
  }
}
