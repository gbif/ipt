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

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.registry.Dataset;
import org.gbif.dwc.Archive;
import org.gbif.dwc.ArchiveField;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.UnsupportedArchiveException;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldConstraints;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageFieldReference;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapGeographicScope;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredCamtrapTaxonomicScope;
import org.gbif.ipt.model.InferredCamtrapTemporalScope;
import org.gbif.ipt.model.InferredEmlGeographicCoverage;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredEmlTaxonomicCoverage;
import org.gbif.ipt.model.InferredEmlTemporalCoverage;
import org.gbif.ipt.model.MetadataFiles;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.SafeTreeMapConverter;
import org.gbif.ipt.model.converter.SafeTreeSetConverter;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
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
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.metadata.eml.EMLProfileVersion;
import org.gbif.metadata.eml.EmlValidator;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.EmlFactory;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.KeywordSet;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;
import org.gbif.metadata.eml.parse.DatasetEmlParser;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.EML_2_1_1_SCHEMA;
import static org.gbif.ipt.config.Constants.EML_2_2_0_SCHEMA;
import static org.gbif.ipt.config.DataDir.COL_DP_METADATA_FILENAME;
import static org.gbif.ipt.config.DataDir.EML_XML_FILENAME;
import static org.gbif.ipt.config.DataDir.FRICTIONLESS_METADATA_FILENAME;
import static org.gbif.ipt.utils.FileUtils.getFileExtension;
import static org.gbif.ipt.utils.MetadataUtils.metadataClassForType;

public class ResourceManagerImpl extends BaseManager implements ResourceManager {

  // key=shortname in lower case, value=resource
  private final Map<String, Resource> resources = new HashMap<>();
  // simplified resources for home page (metadata from last published version!)
  private final Map<String, ResourceSummaryView> publishedPublicResourceSummaries = new HashMap<>();

  private final XStream xstream = new XStream();

  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private final SourceManager sourceManager;
  private final ExtensionManager extensionManager;
  private final DataPackageSchemaManager schemaManager;
  private final RegistryManager registryManager;
  private final VocabulariesManager vocabManager;
  private final SimpleTextProvider textProvider;
  private final RegistrationManager registrationManager;
  private final MetadataReader metadataReader;

  public static final SimpleDateFormat CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, ResourceConvertersManager resourceConvertersManager,
                             SourceManager sourceManager, ExtensionManager extensionManager,
                             DataPackageSchemaManager schemaManager, RegistryManager registryManager,
                             PasswordEncrypter passwordEncrypter, VocabulariesManager vocabManager,
                             SimpleTextProvider textProvider, RegistrationManager registrationManager,
                             MetadataReader metadataReader) {
    super(cfg, dataDir);
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.registryManager = registryManager;
    this.vocabManager = vocabManager;
    defineXstreamMapping(resourceConvertersManager, passwordEncrypter);
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
    this.metadataReader = metadataReader;
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
    // add only public/registered resources with at least one published version
    try {
      if (!res.getVersionHistory().isEmpty()) {
        VersionHistory latestVersion = res.getVersionHistory().get(0);
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE) &&
            latestVersion.getReleased() != null) {
          publishedPublicResourceSummaries.put(res.getShortname(), toResourceSummaryViewReconstructed(res));
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
    resources.values().stream()
        .filter(r -> r.getOrganisation() != null)
        .filter(r -> r.getOrganisation().getKey() != null)
        .filter(r -> r.getOrganisation().getKey().equals(organisationKey))
        .forEach(r -> {
          r.getOrganisation().setAlias(organisationAlias);
          r.getOrganisation().setName(organisationName);
        });
    publishedPublicResourceSummaries.values().stream()
        .filter(r -> r.getOrganisationKey() != null)
        .filter(r -> r.getOrganisationKey().equals(organisationKey))
        .forEach(r -> {
          r.setOrganisationName(organisationName);
          r.setOrganisationAlias(organisationAlias);
        });
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

    // was last published version later registered but never republished? Fix for issue #1319
    if (!publishedPublicVersion.isRegistered() && resource.isRegistered() && resource.getOrganisation() != null) {
      result.setStatus(PublicationStatus.REGISTERED);
      result.setOrganisationAlias(resource.getOrganisationAlias());
      result.setOrganisationName(resource.getOrganisationName());
    }

    return result;
  }



  /**
   * Close file writer if the writer is not null.
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

  /**
   * Populate an Eml instance from a Dataset object that was created from a Dublin Core metadata document, or
   * another basic metadata format. Note: only a small number of fields actually contain data.
   *
   * @param metadata Dataset object
   * @return Eml instance
   */
  private Eml convertMetadataToEml(Dataset metadata) {
    Eml eml = new Eml();
    if (metadata != null) {
      // copy properties
      eml.setTitle(metadata.getTitle());

      if (metadata.getDescription() != null) {
        eml.setDescription(metadata.getDescription());
      }

      if (metadata.getHomepage() != null) {
        eml.setDistributionUrl(metadata.getHomepage().toString());
      }
      if (metadata.getLogoUrl() != null) {
        eml.setLogoUrl(metadata.getLogoUrl().toString());
      }
      if (metadata.getPubDate() != null) {
        eml.setPubDate(metadata.getPubDate());
      } else {
        eml.setPubDate(new Date());
        LOG.debug("pubDate set to today, because incoming pubDate was null");
      }
    }
    return eml;
  }

  /**
   * Validates EML file
   *
   * @param emlFile EML file
   * @throws SAXException        if failed to create validator
   * @throws IOException         if failed to read EML file
   * @throws InvalidEmlException if EML is invalid
   */
  private void validateEmlFile(File emlFile)
      throws SAXException, ParserConfigurationException, IOException, InvalidEmlException {
    EMLProfileVersion emlProfileVersion = getEmlProfileVersion(emlFile);
    EmlValidator emlValidator = EmlValidator.newValidator(emlProfileVersion);
    String emlString = FileUtils.readFileToString(emlFile, StandardCharsets.UTF_8);
    emlValidator.validate(emlString);
  }

  private EMLProfileVersion getEmlProfileVersion(File emlFile)
      throws SAXException, ParserConfigurationException, IOException, InvalidEmlException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    org.w3c.dom.Document document = builder.parse(emlFile);

    String emlNamespace = document.getDocumentElement().getNamespaceURI();

    EMLProfileVersion emlProfileVersion;
    if (EML_2_1_1_SCHEMA.equals(emlNamespace)) {
      LOG.debug("Use GBIF metadata profile 1.2 for validation");
      emlProfileVersion = EMLProfileVersion.GBIF_1_2;
    } else if (EML_2_2_0_SCHEMA.equals(emlNamespace)) {
      LOG.debug("Use GBIF metadata profile 1.3 for validation");
      emlProfileVersion = EMLProfileVersion.GBIF_1_3;
    } else {
      LOG.error("Unsupported EML version or unrecognized namespace.");
      throw new InvalidEmlException("Unsupported EML version or unrecognized namespace.");
    }

    return emlProfileVersion;
  }

  private void validateDatapackageMetadataFile(BaseAction action, File metadataFile, Class<? extends DataPackageMetadata> metadataClass) throws IOException, org.gbif.ipt.service.InvalidMetadataException {
    DataPackageMetadataValidator validator = new DataPackageMetadataValidator();
    DataPackageMetadata metadata = metadataReader.readValue(metadataFile, metadataClass);
    validator.validate(action, metadata);

    // additional ColDP metadata validation
    if (FrictionlessColMetadata.class.equals(metadataClass)) {
      ColMetadata colMetadata = metadataReader.readValue(metadataFile, ColMetadata.class);
      validator.validateColMetadata(action, colMetadata);
    }
  }

  /**
   * Copies incoming eml file to resource directory with name eml.xml.
   * </br>
   * This method retrieves a file handle to the eml.xml file in resource directory. It then copies the incoming emlFile
   * over to this file. From this file an Eml instance is then populated and returned.
   * </br>
   * If the incoming eml file was invalid, meaning a valid eml.xml failed to be created, this method deletes the
   * resource directory. To be safe, the resource directory will only be deleted if it exclusively contained the invalid
   * eml.xml file.
   *
   * @param shortname shortname
   * @param emlFile   eml file
   * @return populated Eml instance
   * @throws ImportException if eml file could not be read/parsed
   */
  private Eml copyMetadata(String shortname, File emlFile) throws ImportException {
    File emlFile2 = dataDir.resourceEmlFile(shortname);
    try {
      FileUtils.copyFile(emlFile, emlFile2);
    } catch (Exception e1) {
      LOG.error("Unable to copy EML File", e1);
    }
    Eml eml;
    try (InputStream in = new FileInputStream(emlFile2)) {
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (Exception e) {
      deleteDirectoryContainingSingleFile(emlFile2);
      throw new ImportException("Invalid EML document", e);
    }
    return eml;
  }

  private DataPackageMetadata copyDatapackageMetadata(String shortname, File metadataFile, String datapackageType) throws ImportException {
    File dataDirMetadataFile = dataDir.resourceDatapackageMetadataFile(shortname, datapackageType);
    try {
      FileUtils.copyFile(metadataFile, dataDirMetadataFile);
    } catch (IOException e) {
      LOG.error("Unable to copy datapackage metadata file {}", e.getMessage(), e);
    }

    DataPackageMetadata metadata;
    try {
      metadata = metadataReader.readValue(
          dataDirMetadataFile,
          metadataClassForType(datapackageType));
    } catch (JsonMappingException e) {
      throw new ImportException("Invalid metadata document: " + e.getOriginalMessage());
    } catch (IOException e) {
      LOG.error("Unable to read datapackage metadata file", e);
      deleteDirectoryContainingSingleFile(dataDirMetadataFile);
      throw new ImportException("Unable to read metadata document.");
    }

    return metadata;
  }

  /**
   * Method deletes entire directory if it exclusively contains a single file. This method can be to cleanup
   * a resource directory containing an invalid eml.xml.
   *
   * @param file file enclosed in a resource directory
   */
  protected void deleteDirectoryContainingSingleFile(File file) {
    File parent = file.getParentFile();
    File[] files = parent.listFiles();
    if (files != null && files.length == 1 && files[0].equals(file)) {
      try {
        FileUtils.deleteDirectory(parent);
        LOG.info("Deleted directory: {}", parent.getAbsolutePath());
      } catch (IOException e) {
        LOG.error("Failed to delete directory {}: {}", parent.getAbsolutePath(), e.getMessage(), e);
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
        case "xml" -> createFromEml(shortname, archiveOrSingleFile, creator, alog);
        case "json" -> createFromPackageDescriptor(shortname, type, archiveOrSingleFile, creator, alog);
        case "yml" -> createFromColDpMetadata(shortname, archiveOrSingleFile, creator, alog);
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
        resource = createFromFrictionlessDataPackage(shortname, archiveDir, type, decompressed, creator, alog);
      } else {
        resource = createFromDwcArchive(shortname, archiveDir, creator, alog);
      }
    }

    // set resource type, if it hasn't been set already
    if (type != null && StringUtils.isBlank(resource.getCoreType())) {
      resource.setCoreType(type);
    }

    return resource;
  }

  /**
   * Creates a resource from an IPT Resource folder. The purpose is to preserve the original source files and mappings.
   * The managers, created date, last publication date, version history, version number, DOI(s), publication status,
   * and registration info is all cleared. The creator and modifier are set to the current creator.
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
      if (resources.containsKey(shortname)) {
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
   * Determine whether the directory represents an IPT Resource directory or not. To qualify, directory must contain
   * resource.xml file and one of the metadata files: eml.xml/datapackage.json/metadata.yml
   *
   * @param dir directory where compressed file was decompressed
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

  // TODO: remove - unused
  /**
   * Filter those files with suffixes ending in .xml.
   */
  private static class XmlFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      return name != null && name.toLowerCase().endsWith(".xml");
    }
  }

  @Override
  public Resource create(String shortname, String type, User creator) throws AlreadyExistingException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    Resource res = new Resource();
    res.setShortname(shortname.toLowerCase());
    res.setCreated(new Date());
    res.setCreator(creator);

    // make sure the correct metadata class is set
    if (CAMTRAP_DP.equals(type)) {
      res.setDataPackageMetadata(new CamtrapMetadata());
      res.inferCoverageMetadataAutomatically(true);
    } else if (COL_DP.equals(type)) {
      res.setDataPackageMetadata(new FrictionlessColMetadata());
    }

    String schemaIdentifier = schemaManager.getSchemaIdentifier(type);
    String schemaVersion = schemaManager.getVersion(schemaIdentifier);
    if (schemaIdentifier != null) {
      res.setDataPackageIdentifier(schemaIdentifier);
      res.setDataPackageVersion(schemaVersion);
    }

    res.setCoreType(type);
    // first and last published dates are nulls
    res.getEml().setDateStamp(((Date) null));
    res.getEml().setPubDate(null);
    // create dir
    try {
      save(res);
      LOG.info("Created resource {}", res.getShortname());
    } catch (InvalidConfigException e) {
      LOG.error("Error creating resource", e);
      return null;
    }
    return res;
  }

  private Resource createFromFrictionlessDataPackage(
      String shortname,
      File archiveDir,
      String packageType,
      List<File> packageFiles,
      User creator,
      ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);

    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }

    Resource resource;
    try {
      Map<String, TextFileSource> sources = new HashMap<>();
      resource = create(shortname, packageType, creator);
      Date lastModifiedDate = new Date();

      // Step 1: locate the metadata file (COL DP takes precedence over Frictionless)
      File metadataFile = findMetadataFile(packageFiles);

      // Parse metadata up front so we know which files are declared as data sources,
      // rather than guessing from filenames
      DataPackageMetadata metadata;
      Map<String, String> declaredResources;
      if (metadataFile != null) {
        metadata = readDataPackageMetadata(resource.getShortname(), packageType, metadataFile, alog);
        if (metadata == null) {
          throw new ImportException("Failed to read/parse data package metadata from " + metadataFile.getName());
        }
        declaredResources = extractDeclaredResources(metadata);
      } else {
        LOG.error("No metadata file found in Frictionless package");
        alog.error("manage.resource.dp.create.no.metadata");
        throw new ImportException("No metadata file found in Frictionless package");
      }

      // Step 2: import data sources, driven by the metadata where available
      for (File packageFile : packageFiles) {
        Collection<File> filesToProcess = packageFile.isDirectory()
            ? FileUtils.listFiles(packageFile, null, false)
            : Collections.singletonList(packageFile);

        for (File file : filesToProcess) {
          if (file.equals(metadataFile) || EML_XML_FILENAME.equals(file.getName())) {
            continue;
          }
          if (isTabularSource(file, archiveDir, declaredResources.keySet())) {
            TextFileSource s = importSource(resource, file);
            s.setFieldsEnclosedBy("\"");
            String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());
            sources.put(filenameWithoutExtension, s);

            DataPackageMapping map = importDataPackageMappings(alog, packageType, file, getDeclaredResourceForFile(declaredResources, archiveDir, file), s);
            map.setLastModified(lastModifiedDate);
            resource.addDataPackageMapping(map);
          }
        }
      }

      resource.setSourcesModified(lastModifiedDate);
      resource.setMappingsModified(lastModifiedDate);

      if (metadata != null) {
        if (metadata instanceof FrictionlessMetadata frictionlessMetadata) {
          frictionlessMetadata.setName(resource.getShortname());
          frictionlessMetadata.setId(null);
          frictionlessMetadata.setCreated(null);
          frictionlessMetadata.getAdditionalProperties().clear();
        }

        if (metadata instanceof CamtrapMetadata camtrapMetadata) {
          camtrapMetadata.getContributors().stream()
              .filter(Objects::nonNull)
              .map(contributor -> (CamtrapContributor) contributor)
              .filter(contributor -> CamtrapContributor.Role.CITATION_ROLES.contains(contributor.getRole()))
              .forEach(this::inferNameFieldsForCamtrapContributor);
        }

        resource.setDataPackageMetadata(metadata);
        resource.setMetadataModified(lastModifiedDate);
        // do not automatically infer metadata
        resource.setInferGeocoverageAutomatically(false);
        resource.setInferTaxonomicCoverageAutomatically(false);
        resource.setInferTemporalCoverageAutomatically(false);
      }

      // finally persist the whole thing
      save(resource);
      saveDatapackageMetadata(resource);

      alog.info("manage.resource.dp.create.success",
          new String[]{String.valueOf(resource.getSources().size()), String.valueOf(resource.getDataPackageMappings().size())});
    } catch (UnsupportedArchiveException | InvalidConfigException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    } finally {
      FileUtils.deleteQuietly(archiveDir);
    }

    return resource;
  }

  private String getDeclaredResourceForFile(Map<String, String> declaredResources, File packageRoot, File file) {
    String relativePath = packageRoot.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
    return declaredResources.get(relativePath);
  }

  /**
   * Extracts the resources declared in a Frictionless data package's metadata, mapping each
   * declared file's filename to its Frictionless resource {@code name}.
   * <p>
   * The {@code resources} array is not (currently) a modeled field on {@link FrictionlessMetadata}
   * — Jackson deserializes it into {@link FrictionlessMetadata#getAdditionalProperties()} as raw
   * {@code List<Map<String, Object>>} JSON, so this method reads it out defensively rather than
   * via typed accessors. If the model is ever updated to include a proper {@code resources} field,
   * this method should be updated to use it instead.
   * <p>
   * Only entries where both {@code path} and {@code name} are present and are strings are included;
   * malformed or partial entries are silently skipped. The {@code path} value may be a relative path
   * (e.g. {@code "data/observations.csv"}); only the filename component is used as the map key, since
   * package files are matched by filename elsewhere in the import process.
   * <p>
   * <b>Note:</b> this must be called before any code clears
   * {@link FrictionlessMetadata#getAdditionalProperties()} (which the caller does later, after
   * metadata is attached to the resource), or the declared resources will no longer be readable.
   *
   * @param metadata the parsed data package metadata; only {@link FrictionlessMetadata} instances
   *                  are currently supported
   * @return a map of declared filename to Frictionless resource name, or an empty map if
   *         {@code metadata} is not a {@link FrictionlessMetadata}, has no {@code resources} entry,
   *         or the entry is not in the expected shape
   */
  private Map<String, String> extractDeclaredResources(DataPackageMetadata metadata) {
    if (metadata instanceof FrictionlessMetadata fm) {
      Map<String, Object> additionalProperties = fm.getAdditionalProperties();
      if (additionalProperties == null) {
        return Collections.emptyMap();
      }

      Object resourcesRaw = additionalProperties.get("resources");
      if (!(resourcesRaw instanceof List<?> resourcesList)) {
        return Collections.emptyMap();
      }

      Map<String, String> declaredResources = new HashMap<>();
      for (Object entry : resourcesList) {
        if (!(entry instanceof Map<?, ?> resourceMap)) {
          continue;
        }
        // resource path is under "path"
        Object path = resourceMap.get("path");
        Object name = resourceMap.get("name");

        if (path instanceof String pathStr && name instanceof String nameStr) {
//          // path may be a relative path (e.g. "data/observations.csv") — we only care about the filename
//          declaredResources.put(FilenameUtils.getName(pathStr), nameStr);
          declaredResources.put(pathStr, nameStr);
        }
      }
      return declaredResources;
    }
    return Collections.emptyMap();
  }

  /**
   * Locates the data package metadata file among a resource's package files, if any.
   * <p>
   * A package may contain a COL DP metadata file, a Frictionless metadata file, or (in principle)
   * both. COL DP takes precedence: if both are present, the COL DP file is returned and the
   * Frictionless file is ignored. This mirrors the priority used elsewhere when interpreting a
   * package's format.
   * <p>
   * If multiple files match the Frictionless metadata filename, only the first one encountered is
   * kept (later matches are ignored); this cannot happen for COL DP, where the last match wins,
   * since duplicates would be unexpected in practice and are not otherwise validated here.
   *
   * @param packageFiles all files found in the package
   * @return the metadata {@link File} to parse, or {@code null} if the package contains neither a
   *         COL DP nor a Frictionless metadata file
   */
  private File findMetadataFile(List<File> packageFiles) {
    File frictionless = null;
    File colDp = null;
    for (File f : packageFiles) {
      if (FRICTIONLESS_METADATA_FILENAME.equals(f.getName()) && frictionless == null) {
        frictionless = f;
      } else if (COL_DP_METADATA_FILENAME.equals(f.getName())) {
        colDp = f;
      }
    }
    return colDp != null ? colDp : frictionless;
  }

  /**
   * Determines whether a package file is declared as a data resource in the package's metadata.
   * <p>
   * Metadata is required for import: a data package with no metadata file, or metadata that
   * declares no resources, is rejected earlier in the import process. By the time this method is
   * called, {@code declaredResources} is therefore guaranteed non-empty, and this is a
   * straightforward membership check — files present in the package but not listed in
   * {@code datapackage.json} are treated as extraneous and skipped, with a warning logged, since
   * their presence likely indicates an incomplete or broken package.
   *
   * @param file the candidate package file
   * @param declaredResources filenames declared as resources in the package metadata; expected to
   *                           be non-empty, since packages with no declared resources are rejected
   *                           before this method is called
   * @return {@code true} if {@code file} is declared as a resource in the package metadata
   */
  private boolean isTabularSource(File file, File packageRoot, Set<String> declaredResources) {
    String relativePath = packageRoot.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
    boolean declared = declaredResources.contains(relativePath);
    if (!declared) {
      LOG.warn("File {} is not declared in datapackage.json", relativePath);
    }
    return declared;
  }

  private Resource createFromDwcArchive(String shortname, File dwca, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    Resource resource;
    try {
      // try to read dwca
      Archive arch = DwcFiles.fromLocation(dwca.toPath());

      if (arch.getCore() == null) {
        alog.error("manage.resource.create.core.invalid");
        throw new ImportException("Darwin Core Archive is invalid and does not have a core mapping");
      }

      if (arch.getCore().getRowType() == null) {
        alog.error("manage.resource.create.core.invalid.rowType");
        throw new ImportException("Darwin Core Archive is invalid, core mapping has no rowType");
      }

      Set<String> installedExtensionRowTypes = extensionManager.list().stream()
          .map(Extension::getRowType)
          .collect(Collectors.toSet());

      List<String> missingExtensionRowTypes = arch.getExtensions().stream()
          .map(e -> e.getRowType().qualifiedName())
          .filter(qName -> !installedExtensionRowTypes.contains(qName))
          .collect(Collectors.toList());

      if (!missingExtensionRowTypes.isEmpty()) {
        alog.error("manage.resource.create.rowTypes.null", new String[]{String.join("<br>", missingExtensionRowTypes)});
        throw new ImportException("Resource references non-installed extension(s)");
      }

      // keep track of source files as a dwca might refer to the same source file multiple times
      Map<String, TextFileSource> sources = new HashMap<>();

      // determine core type for the resource based on the rowType
      Term coreRowType = arch.getCore().getRowType();
      CoreRowType resourceType;
      if (coreRowType.equals(DwcTerm.Taxon)) {
        resourceType = CoreRowType.CHECKLIST;
      } else if (coreRowType.equals(DwcTerm.Occurrence)) {
        resourceType = CoreRowType.OCCURRENCE;
      } else if (coreRowType.equals(DwcTerm.Event)) {
        resourceType = CoreRowType.SAMPLINGEVENT;
      } else {
        resourceType = CoreRowType.OTHER;
      }

      // create new resource
      resource = create(shortname, resourceType.toString().toUpperCase(Locale.ENGLISH), creator);
      Date lastModifiedDate = new Date();

      // read core source+mappings
      TextFileSource s = importSource(resource, arch.getCore());
      sources.put(arch.getCore().getLocations().get(0), s);
      ExtensionMapping map = importMappings(alog, arch.getCore(), s);
      map.setLastModified(lastModifiedDate);
      resource.addMapping(map);

      resource.setSourcesModified(lastModifiedDate);
      resource.setMappingsModified(lastModifiedDate);

      // if extensions are being used
      // the core must contain an id element that indicates the identifier for a record
      if (!arch.getExtensions().isEmpty()) {
        if (map.getIdColumn() == null) {
          alog.error("manage.resource.create.core.invalid.id");
          throw new ImportException("Darwin Core Archive is invalid, core mapping has no id element");
        }

        // read extension sources+mappings
        for (ArchiveFile ext : arch.getExtensions()) {
          if (sources.containsKey(ext.getLocations().get(0))) {
            s = sources.get(ext.getLocations().get(0));
            LOG.debug("SourceBase {} shared by multiple extensions", s.getName());
          } else {
            s = importSource(resource, ext);
            sources.put(ext.getLocations().get(0), s);
          }
          map = importMappings(alog, ext, s);
          map.setLastModified(lastModifiedDate);
          if (map.getIdColumn() == null) {
            alog.error("manage.resource.create.core.invalid.coreid");
            throw new ImportException("Darwin Core Archive is invalid, extension mapping has no coreId element");
          }

          // ensure the extension contains a coreId term mapping with the correct coreId index
          if (resource.getCoreRowType() != null) {
            updateExtensionCoreIdMapping(map, resource.getCoreRowType());
          }
          resource.addMapping(map);
        }
      }

      // try to read metadata
      Eml eml = readMetadata(resource.getShortname(), arch, alog);
      if (eml != null) {
        resource.setEml(eml);
        resource.setMetadataModified(lastModifiedDate);
      }

      // finally persist the whole thing
      save(resource);

      alog.info("manage.resource.create.success",
          new String[]{StringUtils.trimToEmpty(resource.getCoreRowType()), String.valueOf(resource.getSources().size()),
              String.valueOf(resource.getMappings().size())});
    } catch (UnsupportedArchiveException | InvalidConfigException | IOException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    } finally {
      FileUtils.deleteQuietly(dwca);
    }

    return resource;
  }

  /**
   * Replace the EML file in a resource by the provided file.
   * Validation is optional.
   */
  @Override
  public void replaceEml(Resource resource, File emlFile, boolean validate) throws SAXException, ParserConfigurationException, IOException, InvalidEmlException, ImportException {
    if (validate) {
      validateEmlFile(emlFile);
    }
    // copy eml file to data directory (with name eml.xml) and populate Eml instance
    Eml eml = copyMetadata(resource.getShortname(), emlFile);
    resource.setEml(eml);
    resource.setMetadataModified(new Date());
    save(resource);
    saveEml(resource, true);
  }

  @Override
  public void replaceDatapackageMetadata(BaseAction action, Resource resource, File metadataFile, boolean validate)
      throws IOException, ImportException, org.gbif.ipt.service.InvalidMetadataException {
    if (validate) {
      validateDatapackageMetadataFile(action, metadataFile, metadataClassForType(resource.getCoreType()));
    }
    DataPackageMetadata metadata = copyDatapackageMetadata(resource.getShortname(), metadataFile, resource.getCoreType());

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
          .forEach(this::inferNameFieldsForCamtrapContributor);
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

  /**
   * Infer firstName/lastName fields from the title field and set them.
   *
   * @param contributor camtrap contributor
   */
  protected void inferNameFieldsForCamtrapContributor(CamtrapContributor contributor) {
    String title = StringUtils.trimToNull(contributor.getTitle());

    if (StringUtils.isNotEmpty(title)) {
      String[] names = title.split("\\s+");

      if (names.length > 0) {
        if (names.length == 1) {
          contributor.setLastName(names[0]);
        } else {
          String firstName = names[0];
          String lastName = String.join(" ", Arrays.copyOfRange(names, 1, names.length));

          contributor.setFirstName(firstName);
          contributor.setLastName(lastName);
        }
      }
    }
  }

  /**
   * Method ensures an Extension's mapping:
   * a) always contains the coreId term mapping (if it doesn't exist yet)
   * b) coreId element's index is always the same as the coreId term's index (see issue #1229)
   *
   * @param mapping             an extension's mapping (ExtensionMapping)
   * @param resourceCoreRowType resource's core row type
   */
  private void updateExtensionCoreIdMapping(ExtensionMapping mapping, String resourceCoreRowType) {
    Objects.requireNonNull(mapping.getIdColumn(), "The extension must contain a coreId element");

    String coreIdTermQName = AppConfig.coreIdTerm(resourceCoreRowType);
    PropertyMapping coreIdTermPropertyMapping = mapping.getField(coreIdTermQName);
    if (coreIdTermPropertyMapping == null) {
      Term coreIdTerm = TERM_FACTORY.findTerm(coreIdTermQName);
      PropertyMapping coreIdTermMapping = new PropertyMapping(new ArchiveField(mapping.getIdColumn(), coreIdTerm));
      mapping.getFields().add(coreIdTermMapping);
    } else {
      if (coreIdTermPropertyMapping.getIndex() != null && !coreIdTermPropertyMapping.getIndex()
          .equals(mapping.getIdColumn())) {
        mapping.setIdColumn(coreIdTermPropertyMapping.getIndex());
      }
    }
  }

  /**
   * Create new resource from eml file.
   *
   * @param shortname resource shortname
   * @param emlFile   eml file
   * @param creator   User creating resource
   * @param alog      ActionLogger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException          if the eml file could not be read/parsed
   */
  private Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    Eml eml;
    try {
      // copy eml file to data directory (with name eml.xml) and populate Eml instance
      eml = copyMetadata(shortname, emlFile);
    } catch (ImportException e) {
      alog.error("manage.resource.create.failed");
      throw e;
    }
    // create resource of type metadata, with Eml instance
    Resource resource = create(shortname, Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resource.setMetadataModified(new Date());
    resource.setEml(eml);
    return resource;
  }

  private Resource createFromPackageDescriptor(String shortname, String type, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    DataPackageMetadata metadata;

    try {
      // copy metadata file to data directory (with name datapackage.json) and populate metadata instance
      metadata = copyDatapackageMetadata(shortname, metadataFile, type);

      if (metadata instanceof FrictionlessMetadata frictionlessMetadata) {
        // set name, erase some internal fields
        frictionlessMetadata.setName(shortname);
        frictionlessMetadata.setId(null);
        frictionlessMetadata.setCreated(null);
        frictionlessMetadata.getAdditionalProperties().clear();
      }
    } catch (ImportException e) {
      alog.error("manage.resource.create.failed");
      throw e;
    }
    // create resource of Frictionless type, with metadata instance
    Resource resource = create(shortname, type, creator);
    resource.setMetadataModified(new Date());
    resource.setDataPackageMetadata(metadata);
    return resource;
  }

  private Resource createFromColDpMetadata(String shortname, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    DataPackageMetadata metadata;

    try {
      // copy metadata file to data directory (with name datapackage.json) and populate metadata instance
      metadata = copyDatapackageMetadata(shortname, metadataFile, COL_DP);
    } catch (ImportException e) {
      alog.error("manage.resource.create.failed");
      throw e;
    }
    // create resource of ColDP type, with metadata instance
    Resource resource = create(shortname, COL_DP, creator);
    resource.setMetadataModified(new Date());
    resource.setDataPackageMetadata(metadata);
    return resource;
  }

  private void defineXstreamMapping(ResourceConvertersManager resourceConvertersManager, PasswordEncrypter passwordEncrypter) {
    xstream.addPermission(AnyTypePermission.ANY);
    xstream.ignoreUnknownElements();
    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);

    // aliases for inferred metadata
    xstream.alias("inferredMetadata", InferredEmlMetadata.class);
    xstream.alias("inferredMetadataCamtrap", InferredCamtrapMetadata.class);
    xstream.alias("inferredGeographicCoverage", InferredEmlGeographicCoverage.class);
    xstream.alias("inferredGeographicScope", InferredCamtrapGeographicScope.class);
    xstream.alias("inferredTaxonomicCoverage", InferredEmlTaxonomicCoverage.class);
    xstream.alias("inferredTaxonomicScope", InferredCamtrapTaxonomicScope.class);
    xstream.alias("inferredTemporalCoverage", InferredEmlTemporalCoverage.class);
    xstream.alias("inferredTemporalScope", InferredCamtrapTemporalScope.class);
    xstream.alias("taxonKeyword", TaxonKeyword.class);
    xstream.alias("organizedTaxonomicKeywords", OrganizedTaxonomicKeywords.class);

    xstream.alias("filesource", TextFileSource.class);
    xstream.alias("excelsource", ExcelFileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("urlsource", UrlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", PropertyMapping.class);
    xstream.alias("dataPackageMapping", DataPackageMapping.class);
    xstream.alias("dataPackageFieldMapping", DataPackageFieldMapping.class);
    xstream.alias("tableSchema", DataPackageTableSchema.class);
    xstream.alias("dataPackageField", DataPackageField.class);
    xstream.alias("dataPackageForeignKey", DataPackageTableSchemaForeignKey.class);
    xstream.alias("dataPackageFieldReference", DataPackageFieldReference.class);
    xstream.alias("constraints", DataPackageFieldConstraints.class);
    xstream.alias("versionhistory", VersionHistory.class);
    xstream.alias("doi", DOI.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "dataPackageMetadata");
    xstream.omitField(Resource.class, "type");
    // inferred metadata in the separate file
    xstream.omitField(Resource.class, "inferredMetadata");
    // make files transient to allow moving the datadir
    xstream.omitField(TextFileSource.class, "file");

    // Read legacy TreeMap/TreeSet without triggering XStream's TreeMapConverter (Struts 7/Java 17 issues).
    xstream.registerConverter(new SafeTreeMapConverter(), 10000);
    xstream.registerConverter(new SafeTreeSetConverter(), 10000);
    // persist only emails for users
    xstream.registerConverter(resourceConvertersManager.getUserConverter());
    // custom converter for ExtensionMapping
    xstream.registerConverter(resourceConvertersManager.getExtensionMappingConverter());
    // persist only rowtype
    xstream.registerConverter(resourceConvertersManager.getExtensionConverter());
    // persist only qualified concept name
    xstream.registerConverter(resourceConvertersManager.getConceptTermConverter());
    // persist only schema identifier, table schema name and field name
    xstream.registerConverter(resourceConvertersManager.getDataSchemaConverter());
    xstream.registerConverter(resourceConvertersManager.getTableSchemaNameConverter());
    xstream.registerConverter(resourceConvertersManager.getDataPackageFieldConverter());
    // encrypt passwords
    xstream.registerConverter(passwordEncrypter);

    xstream.addDefaultImplementation(ExtensionProperty.class, Term.class);
    xstream.registerConverter(resourceConvertersManager.getOrgConverter());
    xstream.registerConverter(resourceConvertersManager.getJdbcInfoConverter());
  }

  @Override
  public void deleteResourceFromIpt(Resource resource) throws IOException {
    // remove from data dir
    FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
    // remove object
    resources.remove(resource.getShortname().toLowerCase());
    publishedPublicResourceSummaries.remove(resource.getShortname().toLowerCase());
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
      resources.remove(resource.getShortname().toLowerCase());
      publishedPublicResourceSummaries.remove(resource.getShortname().toLowerCase());
    }
  }

  @Override
  public Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  /**
   * Creates an ExtensionMapping from an ArchiveFile, which encapsulates information about a file contained
   * within a Darwin Core Archive.
   *
   * @param alog   ActionLogger
   * @param af     ArchiveFile
   * @param source source file corresponding to ArchiveFile
   * @return ExtensionMapping created from ArchiveFile
   * @throws InvalidConfigException if ExtensionMapping could not be created because the ArchiveFile uses
   *                                an extension that has not been installed yet.
   */
  @NotNull
  private ExtensionMapping importMappings(ActionLogger alog, ArchiveFile af, Source source) {
    ExtensionMapping map = new ExtensionMapping();
    Extension ext = extensionManager.get(af.getRowType().qualifiedName());
    if (ext == null) {
      // cleanup source file immediately
      if (source.isFileSource()) {
        File file = ((TextFileSource) source).getFile();
        boolean deleted = FileUtils.deleteQuietly(file);
        // to bypass "Unable to delete file" error on Windows, run garbage collector to clean up file i/o mapping
        if (!deleted) {
          System.gc();
          FileUtils.deleteQuietly(file);
        }
      }
      alog.warn("manage.resource.create.rowType.null", new String[]{af.getRowType().qualifiedName()});
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-installed extension");
    }
    map.setSource(source);
    map.setExtension(ext);

    // set ID column (warning: handmade DwC-A can be missing id index)
    if (af.getId() != null) {
      map.setIdColumn(af.getId().getIndex());
    }

    Set<PropertyMapping> fields = new TreeSet<>();
    // iterate over each field to make sure its part of the extension we know
    for (ArchiveField f : af.getFields().values()) {
      if (f.getTerm() == null) {
        alog.warn("manage.resource.create.mapping.concept.skip",
            new String[]{"null", ext.getRowType()});
      } else if (ext.hasProperty(f.getTerm())) {
        fields.add(new PropertyMapping(f));
      } else {
        alog.warn("manage.resource.create.mapping.concept.skip",
            new String[]{f.getTerm().qualifiedName(), ext.getRowType()});
      }
    }
    map.setFields(fields);

    return map;
  }

  private DataPackageMapping importDataPackageMappings(
      ActionLogger alog, String packageType, File file, String resourceName, TextFileSource source) {
    DataPackageMapping map = new DataPackageMapping();
    DataPackageSchema dataPackageSchema = schemaManager.get(packageType);

    if (dataPackageSchema == null) {
      // clean up source file immediately
      if (source.isFileSource()) {
        boolean deleted = FileUtils.deleteQuietly(file);
        // to bypass "Unable to delete file" error on Windows, run garbage collector to clean up file i/o mapping
        if (!deleted) {
          System.gc();
          FileUtils.deleteQuietly(file);
        }
      }
      alog.warn("manage.resource.create.schema.null", new String[]{packageType});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource references non-installed data package");
    }

    DataPackageTableSchema tableSchema = dataPackageSchema.getTableSchemas().stream()
        .filter(s -> s.getName().equals(resourceName))
        .findAny()
        .orElse(null);

    if (tableSchema == null) {
      alog.error("manage.resource.create.tableschema.null", new String[]{dataPackageSchema.getTitle(), resourceName});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource references unknown table");
    }

    map.setDataPackageSchema(dataPackageSchema);
    map.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchema.getName()));
    map.setSource(source);

    // extract column names from file's first row
    String[] columnNames;
    try (CSVReader csvReader = CSVReaderFactory.build(
        file,
        source.getEncoding(),
        source.getFieldsTerminatedBy(),
        source.getFieldQuoteChar(),
        source.getIgnoreHeaderLines())) {
      columnNames = csvReader.getHeader();
      if (columnNames == null) {
        alog.error("manage.resource.create.table.header.empty", new String[]{resourceName});
        throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource is empty: " + resourceName);
      }
    } catch (IOException e) {
      alog.error("manage.resource.create.table.read.error", new String[]{resourceName, e.getMessage()});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Failed to read resource: " + resourceName);
    }

    List<DataPackageFieldMapping> fields = new ArrayList<>();
    Map<String, DataPackageField> schemaFieldsMap = tableSchema.getFields().stream()
        .collect(Collectors.toMap(DataPackageField::getName, p -> p));

    // iterate over each field to make sure its part of the extension we know
    for (int i = 0; i < columnNames.length; i++) {
      String unwrappedColumnName = StringUtils.unwrap(columnNames[i], '"');
      DataPackageField dataPackageField = schemaFieldsMap.get(unwrappedColumnName);
      if (dataPackageField != null) {
        fields.add(new DataPackageFieldMapping(i, dataPackageField));
      } else {
        alog.warn("manage.resource.create.mapping.field.skip",
            new String[]{columnNames[i], dataPackageSchema.getName() + "/" + tableSchema.getName()});
      }
    }

    map.setFieldsMapped(columnNames.length);
    map.setLastModified(new Date());
    map.setFields(fields);

    return map;
  }

  private TextFileSource importSource(Resource config, ArchiveFile af)
      throws ImportException, InvalidFilenameException {
    File extFile = af.getLocationFiles().get(0);
    TextFileSource s = (TextFileSource) sourceManager.add(config, extFile, af.getLocations().get(0));
    SourceManagerImpl.copyArchiveFileProperties(af, s);

    // the number of rows was calculated using the standard file importer
    // make an adjustment now that the exact number of header rows are known
    if (s.getIgnoreHeaderLines() != 1) {
      LOG.info("Adjusting row count to {} from {} since header count is declared as {}", s.getRows() + 1 - s.getIgnoreHeaderLines(), s.getRows(), s.getIgnoreHeaderLines());
    }
    s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());

    return s;
  }

  private TextFileSource importSource(Resource config, File file)
      throws ImportException, InvalidFilenameException {
    TextFileSource s = (TextFileSource) sourceManager.add(config, file, FilenameUtils.removeExtension(file.getName()));

    // the number of rows was calculated using the standard file importer
    // make an adjustment now that the exact number of header rows is known
    if (s.getIgnoreHeaderLines() != 1) {
      LOG.info("Adjusting row count to {} from {} since header count is declared as {}",
          s.getRows() + 1 - s.getIgnoreHeaderLines(), s.getRows(), s.getIgnoreHeaderLines());
    }
    s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());

    return s;
  }

  @Override
  public boolean isEmlExisting(String shortName) {
    File emlFile = dataDir.resourceEmlFile(shortName);
    return emlFile.exists();
  }

  @Override
  public List<Resource> latest(int startPage, int pageSize) {
    List<Resource> resourceList = new ArrayList<>();
    for (Resource r : resources.values()) {
      VersionHistory latestVersion = r.getLastPublishedVersion();
      if (latestVersion != null) {
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE)) {
          resourceList.add(r);
        }
      }
    }
    resourceList.sort((r1, r2) -> {
      if (r1 == null || r1.getModified() == null) {
        return 1;
      }
      if (r2 == null || r2.getModified() == null) {
        return -1;
      }
      if (r1.getModified().before(r2.getModified())) {
        return 1;
      } else {
        return -1;
      }
    });
    return resourceList;
  }

  @Override
  public List<Resource> list() {
    return new ArrayList<>(resources.values());
  }

  @Override
  public List<ResourceSummaryView> listPublishedPublicResourceSummaries() {
    return new ArrayList<>(publishedPublicResourceSummaries.values());
  }

  @Override
  public List<Resource> list(String type) {
    return resources.values().stream()
        .filter(res -> type.equals(res.getCoreType()))
        .collect(Collectors.toList());
  }

  @Override
  public List<Resource> list(PublicationStatus status) {
    List<Resource> result = new ArrayList<>();
    for (Resource r : resources.values()) {
      if (r.getStatus() == status) {
        result.add(r);
      }
    }
    return result;
  }

  @Override
  public List<Resource> listPublishedPublicVersions() {
    List<Resource> result = new ArrayList<>();
    for (Resource r : resources.values()) {
      List<VersionHistory> history = r.getVersionHistory();
      if (!history.isEmpty()) {
        VersionHistory latestVersion = history.get(0);
        if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
            !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE) &&
            latestVersion.getReleased() != null) {
          result.add(r);
        }
      } else if (r.isRegistered()) { // for backwards compatibility with resources published prior to v2.2
        result.add(r);
      }
    }
    return result;
  }

  @Override
  public List<Resource> list(User user) {
    List<Resource> result = new ArrayList<>();
    // select based on user rights - for testing return all resources for now
    for (Resource res : resources.values()) {
      if (RequireManagerInterceptor.isAuthorized(user, res)) {
        result.add(res);
      }
    }
    return result;
  }

  @Override
  public int load(File resourcesDir, User creator) {
    resources.clear();
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
   * Loads a resource's metadata from its eml.xml file located inside its resource directory. If no eml.xml file was
   * found, the resource is loaded with an empty EML instance.
   *
   * @param resource resource
   */
  private void loadEml(Resource resource) {
    File emlFile = dataDir.resourceEmlFile(resource.getShortname());
    // load resource metadata, use US Locale to interpret it because uses '.' for decimal separator
    Eml eml = EmlUtils.loadWithLocale(emlFile, Locale.US);
    resource.setEml(eml);
  }

  /**
   * Loads a resource's metadata from its datapackage.json (for frictionless) or metadata.yaml (for ColDP) file located
   * inside its resource directory.
   * If no file was found, the resource is loaded with an empty metadata class instance.
   *
   * @param resource resource
   */
  private void loadDatapackageMetadata(Resource resource) {
    DataPackageMetadata metadata;

    if (CAMTRAP_DP.equals(resource.getCoreType())) {
      metadata = new CamtrapMetadata();
    } else if (COL_DP.equals(resource.getCoreType())) {
      metadata = new ColMetadata();
    } else {
      metadata = new FrictionlessMetadata<>();
    }

    File metadataFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    if (metadataFile.exists() && !metadataFile.isDirectory()) {
      try {
        metadata = metadataReader.readValue(metadataFile, metadataClassForType(resource.getCoreType()));
      } catch (IOException e) {
        LOG.error("Failed to read resource metadata {}", resource.getShortname());
        LOG.error(e);
        throw new RuntimeException(e);
      }
    } else {
      if (metadata instanceof FrictionlessMetadata) {
        ((FrictionlessMetadata<?, ?, ?>) metadata).setName(resource.getShortname());
      }
    }

    resource.setDataPackageMetadata(metadata);
  }

  private void loadMetadata(Resource resource) {
    if (resource.isDataPackage() && resource.isDwcDp()) {
      loadDatapackageMetadata(resource);
      loadEml(resource);
    } else if (resource.isDataPackage()) {
      loadDatapackageMetadata(resource);
    } else {
      loadEml(resource);
    }
  }

  /**
   * Loads a resource's inferred metadata from the xml file located inside its resource directory.
   * If no inferredMetadata.xml file was found, the resource is loaded with an empty InferredMetadata instance.
   *
   * @param resource resource
   */
  private void loadInferredMetadata(Resource resource) {
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
   * Change resource status to REGISTERED and update organization.
   */
  @Override
  public void updateStoredResources(Resource resource) {
    ResourceSummaryView resourceSummaryView = publishedPublicResourceSummaries.get(resource.getShortname());
    if (resourceSummaryView != null) {
      resourceSummaryView.setStatus(PublicationStatus.REGISTERED);
      resourceSummaryView.setOrganisationAlias(resource.getOrganisationAlias());
      resourceSummaryView.setOrganisationName(resource.getOrganisationName());
    }
  }

  @Override
  public void removePublishedPublicVersion(String shortname) {
    publishedPublicResourceSummaries.remove(shortname);
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

        // populate missing creator - it cannot be null! (this fixes issue #1309)
        if (creator != null && resource.getCreator() == null) {
          resource.setCreator(creator);
          LOG.warn("On load, populated missing creator for resource: {}", shortname);
        }

        // non-existing users end up being a NULL in the set, so remove them
        // shouldn't really happen - but people can even manually cause a mess
        resource.getManagers().remove(null);

        // 1. Non-existent Extension end up being NULL
        // E.g. a user is trying to import a resource from one IPT to another without all required exts installed.
        // 2. Auto-generating IDs is only available for Taxon core extension since IPT v2.1,
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
          inferCoreType(resource);
        }

        // standardize subtype if not null
        if (resource.getSubtype() != null) {
          standardizeSubtype(resource);
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
        loadMetadata(resource);

        // load inferred metadata
        loadInferredMetadata(resource);

        // pre v2.2 resources: convert resource version from integer to major_version.minor_version style
        // also convert/rename eml, rtf, and dwca versioned files also
        if (!resource.isDataPackage()) {
          BigDecimal converted = convertVersion(resource);
          if (converted != null) {
            updateResourceVersion(resource, resource.getMetadataVersion(), converted);
          }
        }

        // pre v2.2 resources: construct a VersionHistory for last published version (if appropriate)
        VersionHistory history = constructVersionHistoryForLastPublishedVersion(resource);
        if (history != null) {
          resource.addVersionHistory(history);
        }

        if (!resource.isDataPackage()) {
          // pre v2.2.1 resources: rename dwca.zip to dwca-18.0.zip (where 18.0 is the last published version for example)
          if (resource.getLastPublishedVersionsVersion() != null) {
            renameDwcaToIncludeVersion(resource, resource.getLastPublishedVersionsVersion());
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
   *   <li>Only index is present, but references no field</li>
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

  /**
   * Convert integer version number to major_version.minor_version version number. Please note IPTs before v2.2 used
   * integer-based version numbers.
   *
   * @param resource resource
   * @return converted version number, or null if no conversion happened
   */
  @SuppressWarnings("BigDecimalEquals")
  protected BigDecimal convertVersion(Resource resource) {
    if (resource.getMetadataVersion() != null) {
      BigDecimal version = resource.getMetadataVersion();
      // special conversion: 0 -> 1.0
      if (version.equals(BigDecimal.ZERO)) {
        return Constants.INITIAL_RESOURCE_VERSION;
      } else if (version.scale() == 0) {
        BigDecimal majorMinorVersion = version.setScale(1, RoundingMode.CEILING);
        LOG.debug("Converted version [{}] to [{}]", version.toPlainString(), majorMinorVersion.toPlainString());
        return majorMinorVersion;
      }
    }
    return null;
  }

  /**
   * Update a resource's version and rename its eml, rtf, and dwca versioned files to have the new version also.
   *
   * @param resource   resource to update
   * @param oldVersion old version number
   * @param newVersion new version number
   * @return resource whose version number and files' version numbers have been updated
   */
  @SuppressWarnings("BigDecimalEquals")
  protected Resource updateResourceVersion(Resource resource, BigDecimal oldVersion, BigDecimal newVersion) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(oldVersion);
    Objects.requireNonNull(newVersion);
    // proceed if old and new versions are not equal in both value and scale - comparison done using .equals
    if (!oldVersion.equals(newVersion)) {
      try {
        // rename e.g. eml-18.xml to eml-18.0.xml (if eml-18.xml exists)
        File oldEml = dataDir.resourceEmlFile(resource.getShortname(), oldVersion);
        File newEml = dataDir.resourceEmlFile(resource.getShortname(), newVersion);
        if (oldEml.exists() && !newEml.exists()) {
          FileUtils.moveFile(oldEml, newEml);
        }

        // rename e.g. zvv-18.rtf to zvv-18.0.rtf
        File oldRtf = dataDir.resourceRtfFile(resource.getShortname(), oldVersion);
        File newRtf = dataDir.resourceRtfFile(resource.getShortname(), newVersion);
        if (oldRtf.exists() && !newRtf.exists()) {
          FileUtils.moveFile(oldRtf, newRtf);
        }

        // rename e.g. dwca-18.zip to dwca-18.0.zip
        File oldDwca = dataDir.resourceDwcaFile(resource.getShortname(), oldVersion);
        File newDwca = dataDir.resourceDwcaFile(resource.getShortname(), newVersion);
        if (oldDwca.exists() && !newDwca.exists()) {
          FileUtils.moveFile(oldDwca, newDwca);
        }

        // if all renames were successful (didn't throw an exception), set new version
        resource.setMetadataVersion(newVersion);
      } catch (IOException e) {
        LOG.error("Failed to update version number for {}", resource.getShortname(), e);
        throw new InvalidConfigException(TYPE.CONFIG_WRITE,
            "Failed to update version number for " + resource.getShortname() + ": " + e.getMessage());
      }
    }
    return resource;
  }

  /**
   * Rename a resource's dwca.zip to have the last published version, e.g. dwca-18.0.zip
   *
   * @param resource resource to update
   * @param version  last published version number
   */
  protected void renameDwcaToIncludeVersion(Resource resource, BigDecimal version) {
    Objects.requireNonNull(resource);
    Objects.requireNonNull(version);
    File unversionedDwca = dataDir.resourceDwcaFile(resource.getShortname());
    File versionedDwca = dataDir.resourceDwcaFile(resource.getShortname(), version);
    // proceed if resource has previously been published, and versioned dwca does not exist
    if (unversionedDwca.exists() && !versionedDwca.exists()) {
      try {
        FileUtils.moveFile(unversionedDwca, versionedDwca);
        LOG.debug("Renamed dwca.zip to {}", versionedDwca.getName());
      } catch (IOException e) {
        LOG.error("Failed to rename dwca.zip file name with version number for {}", resource.getShortname(), e);
        throw new InvalidConfigException(TYPE.CONFIG_WRITE,
            "Failed to update version number for " + resource.getShortname() + ": " + e.getMessage());
      }
    }
  }

  /**
   * Construct VersionHistory for last published version of resource, if resource has been published but had no
   * VersionHistory. Please note IPTs before v2.2 had no list of VersionHistory.
   *
   * @param resource resource
   * @return VersionHistory, or null if no VersionHistory needed to be created.
   */
  protected VersionHistory constructVersionHistoryForLastPublishedVersion(Resource resource) {
    if (resource.isPublished() && resource.getVersionHistory().isEmpty()) {
      VersionHistory vh =
          new VersionHistory(resource.getMetadataVersion(), resource.getLastPublished(), resource.getStatus());
      vh.setRecordsPublished(resource.getRecordsPublished());
      return vh;
    }
    return null;
  }

  /**
   * The resource's coreType could be null. This could happen because before 2.0.3 it was not saved to resource.xml.
   * During upgrades to 2.0.3, a bug in MetadataAction would (wrongly) automatically set the coreType:
   * Checklist resources became Occurrence, and vice versa. This method will try to infer the coreType by matching
   * the coreRowType against the taxon and occurrence rowTypes.
   *
   * @param resource Resource
   * @return resource with coreType set if it could be inferred, or unchanged if it couldn't be inferred.
   */
  Resource inferCoreType(Resource resource) {
    if (resource != null && resource.getCoreRowType() != null) {
      if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(CoreRowType.OCCURRENCE.toString().toLowerCase());
      } else if (Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(CoreRowType.CHECKLIST.toString().toLowerCase());
      } else if (Constants.DWC_ROWTYPE_EVENT.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(CoreRowType.SAMPLINGEVENT.toString().toLowerCase());
      }
    }
    return resource;
  }

  /**
   * The resource's subType might not have been set using a standardized term from the dataset_subtype vocabulary.
   * All versions before 2.0.4 didn't use the vocabulary, so this method is particularly important during upgrades
   * to 2.0.4 and later. Basically, if the subType isn't recognized as belonging to the vocabulary, it is reset as
   * null. That would mean the user would then have to reselect the subtype from the Basic Metadata page.
   *
   * @param resource Resource
   * @return resource with subtype set using term from dataset_subtype vocabulary (assuming it has been set).
   */
  Resource standardizeSubtype(Resource resource) {
    if (resource != null && resource.getSubtype() != null) {
      // the vocabulary key names are identifiers and standard across Locales
      // it's this key we want to persist as the subtype
      Map<String, String> subtypes =
          vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, Locale.ENGLISH.getLanguage(), false);
      boolean usesVocab = false;
      for (Map.Entry<String, String> entry : subtypes.entrySet()) {
        // remember to do comparison regardless of case, since the subtype is stored in lowercase
        if (resource.getSubtype().equalsIgnoreCase(entry.getKey())) {
          usesVocab = true;
          break;
        }
      }
      // if the subtype doesn't use a standardized term from the vocab, it's reset to null
      if (!usesVocab) {
        resource.setSubtype(null);
      }
    }
    return resource;
  }

  /**
   * Try to read metadata file for a DwC-Archive.
   *
   * @param shortname resource shortname
   * @param archive   archive
   * @param alog      ActionLogger
   * @return Eml instance or null if none could be created because the metadata file did not exist or was invalid
   */
  @Nullable
  private Eml readMetadata(String shortname, Archive archive, ActionLogger alog) {
    Eml eml;
    File emlFile = archive.getMetadataLocationFile();
    try {
      if (emlFile == null || !emlFile.exists()) {
        // some archives dont indicate the name of the eml metadata file
        // so we also try with the default eml.xml name
        emlFile = new File(archive.getLocation(), EML_XML_FILENAME);
      }
      if (emlFile.exists() && emlFile.getName().endsWith(EML_XML_FILENAME)) {
        // read metadata and populate Eml instance
        eml = copyMetadata(shortname, emlFile);
        alog.info("manage.resource.read.eml.metadata");
        return eml;
      } else {
        LOG.warn("Cant find any eml metadata to import");
      }
    } catch (ImportException e) {
      String msg = "Cant read basic archive metadata: " + e.getMessage();
      LOG.warn(msg);
      alog.warn(msg);
      return null;
    } catch (Exception e) {
      LOG.warn("Cant read archive eml metadata", e);
    }
    // try to read other metadata formats like dc
    try {
      LOG.debug("try to read other metadata formats");
      // TODO: 08/12/2022 why do we build Dataset? Should do EML directly
      Dataset dataset = DatasetEmlParser.build(archive.getMetadata().getBytes(StandardCharsets.UTF_8));
      eml = convertMetadataToEml(dataset);
      alog.info("manage.resource.read.basic.metadata");
      return eml;
    } catch (Exception e) {
      LOG.warn("Cant read basic archive metadata: {}", e.getMessage());
    }
    alog.warn("manage.resource.read.problem");
    return null;
  }

  private DataPackageMetadata readDataPackageMetadata(String shortname, String dataPackageType, File file, ActionLogger alog) {
    DataPackageMetadata metadata;

    try {
      metadata = copyDatapackageMetadata(shortname, file, dataPackageType);
      alog.info("manage.resource.read.datapackage.metadata");
      return metadata;
    } catch (ImportException e) {
      String msg = "Cannot read data package metadata: " + e.getMessage();
      LOG.warn(msg);
      alog.warn(msg);
      return null;
    } catch (Exception e) {
      LOG.warn("Cannot read data package metadata", e);
    }

    alog.warn("manage.resource.read.problem");
    return null;
  }

  @Override
  public synchronized void save(Resource resource) throws InvalidConfigException {
    File cfgFile = dataDir.resourceFile(resource);
    try {
      // make sure resource dir exists
      FileUtils.forceMkdir(cfgFile.getParentFile());
      // persist data
      try (Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile)) {
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
      writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
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
   * @param preserveKeywords perform keywords update or not
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
   * @return resource whose Eml list of KeywordSet has been updated depending on presence of dataset type or subtype
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
