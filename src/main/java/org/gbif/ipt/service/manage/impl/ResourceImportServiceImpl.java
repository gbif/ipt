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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.InvalidMetadataException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceImportService;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.IptFileUtils;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.metadata.eml.EMLProfileVersion;
import org.gbif.metadata.eml.EmlValidator;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.EmlFactory;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.parse.DatasetEmlParser;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jakarta.inject.Provider;
import jakarta.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonMappingException;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.EML_2_1_1_SCHEMA;
import static org.gbif.ipt.config.Constants.EML_2_2_0_SCHEMA;
import static org.gbif.ipt.config.DataDir.COL_DP_METADATA_FILENAME;
import static org.gbif.ipt.config.DataDir.EML_XML_FILENAME;
import static org.gbif.ipt.config.DataDir.FRICTIONLESS_METADATA_FILENAME;
import static org.gbif.ipt.utils.MetadataUtils.metadataClassForType;

/**
 * This class needs to call back into the core {@link ResourceManager} for a handful of operations
 * (duplicate-shortname checks via {@code get}, and persistence via {@code save}/{@code saveDatapackageMetadata})
 * since registering a newly built resource into the manager's in-memory state is the manager's responsibility,
 * not this service's. A {@link Provider} is used for that reference (rather than a plain constructor
 * parameter) to avoid a hard circular dependency with {@code ResourceManagerImpl}, which holds this service.
 */
public class ResourceImportServiceImpl implements ResourceImportService {

  private static final Logger LOG = LogManager.getLogger(ResourceImportServiceImpl.class);
  private static final TermFactory TERM_FACTORY = TermFactory.instance();

  private final DataDir dataDir;
  private final SourceManager sourceManager;
  private final ExtensionManager extensionManager;
  private final DataPackageSchemaManager schemaManager;
  private final MetadataReader metadataReader;
  private final Provider<ResourceManager> resourceManagerProvider;

  public ResourceImportServiceImpl(DataDir dataDir, SourceManager sourceManager, ExtensionManager extensionManager,
                                   DataPackageSchemaManager schemaManager, MetadataReader metadataReader,
                                   Provider<ResourceManager> resourceManagerProvider) {
    this.dataDir = dataDir;
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.metadataReader = metadataReader;
    this.resourceManagerProvider = resourceManagerProvider;
  }

  @Override
  public Resource createNew(String shortname, String type, User creator) throws AlreadyExistingException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
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
      resourceManagerProvider.get().save(res);
      LOG.info("Created resource {}", res.getShortname());
    } catch (InvalidConfigException e) {
      LOG.error("Error creating resource", e);
      return null;
    }
    return res;
  }

  /**
   * Create a new resource from an eml file.
   *
   * @param shortname resource shortname
   * @param emlFile   eml file
   * @param creator   User creating resource
   * @param alog      ActionLogger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException          if the eml file could not be read/parsed
   */
  @Override
  public Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
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
    Resource resource = createNew(shortname, Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resource.setMetadataModified(new Date());
    resource.setEml(eml);
    return resource;
  }

  @Override
  public Resource createFromPackageDescriptor(String shortname, String type, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
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
    Resource resource = createNew(shortname, type, creator);
    resource.setMetadataModified(new Date());
    resource.setDataPackageMetadata(metadata);
    return resource;
  }

  @Override
  public Resource createFromColDpMetadata(String shortname, File metadataFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
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
    Resource resource = createNew(shortname, COL_DP, creator);
    resource.setMetadataModified(new Date());
    resource.setDataPackageMetadata(metadata);
    return resource;
  }

  @Override
  public Resource createFromDwcArchive(String shortname, File dwca, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
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
      resource = createNew(shortname, resourceType.toString().toUpperCase(Locale.ENGLISH), creator);
      Date lastModifiedDate = new Date();

      // read core source+mappings
      TextFileSource s = importSource(resource, arch.getCore());
      sources.put(arch.getCore().getLocations().get(0), s);
      ExtensionMapping map = importMappings(alog, arch.getCore(), s);
      map.setLastModified(lastModifiedDate);
      resource.addMapping(map);

      resource.setSourcesModified(lastModifiedDate);
      resource.setMappingsModified(lastModifiedDate);

      // if extensions are being used,
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

      // finally, persist the whole thing
      resourceManagerProvider.get().save(resource);

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

  @Override
  public Resource createFromFrictionlessDataPackage(
      String shortname,
      File archiveDir,
      String packageType,
      List<File> packageFiles,
      User creator,
      ActionLogger alog)
      throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);

    // check if existing already
    if (resourceManagerProvider.get().get(shortname) != null) {
      throw new AlreadyExistingException();
    }

    Resource resource;
    try {
      Map<String, TextFileSource> sources = new HashMap<>();
      resource = createNew(shortname, packageType, creator);
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
      ResourceManager resourceManager = resourceManagerProvider.get();
      resourceManager.save(resource);
      resourceManager.saveDatapackageMetadata(resource);

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

  @Override
  public void inferNameFieldsForCamtrapContributor(CamtrapContributor contributor) {
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

  @Override
  public void validateEmlFile(File emlFile)
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

  @Override
  public void validateDatapackageMetadataFile(BaseAction action, File metadataFile, Class<? extends DataPackageMetadata> metadataClass)
      throws IOException, InvalidMetadataException {
    DataPackageMetadataValidator validator = new DataPackageMetadataValidator();
    DataPackageMetadata metadata = metadataReader.readValue(metadataFile, metadataClass);
    validator.validate(action, metadata);

    // additional ColDP metadata validation
    if (FrictionlessColMetadata.class.equals(metadataClass)) {
      ColMetadata colMetadata = metadataReader.readValue(metadataFile, ColMetadata.class);
      validator.validateColMetadata(action, colMetadata);
    }
  }

  @Override
  public Eml copyMetadata(String shortname, File emlFile) throws ImportException {
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
      IptFileUtils.deleteDirectoryContainingSingleFile(emlFile2);
      throw new ImportException("Invalid EML document", e);
    }
    return eml;
  }

  @Override
  public DataPackageMetadata copyDatapackageMetadata(String shortname, File metadataFile, String datapackageType) throws ImportException {
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
      IptFileUtils.deleteDirectoryContainingSingleFile(dataDirMetadataFile);
      throw new ImportException("Unable to read metadata document.");
    }

    return metadata;
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
        // some archives don't indicate the name of the eml metadata file,
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
}
