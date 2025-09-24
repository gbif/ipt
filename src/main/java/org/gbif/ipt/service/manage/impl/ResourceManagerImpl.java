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
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.DoiException;
import org.gbif.doi.service.DoiExistsException;
import org.gbif.doi.service.InvalidMetadataException;
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
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.PublicationOptions;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.SimplifiedResource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Geojson;
import org.gbif.ipt.model.datapackage.metadata.camtrap.RelatedIdentifier;
import org.gbif.ipt.model.datapackage.metadata.camtrap.Temporal;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;
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
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDataPackage;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.GeneratorException;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.task.TaskMessage;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.metadata.eml.EMLProfileVersion;
import org.gbif.metadata.eml.EmlValidator;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.EmlFactory;
import org.gbif.metadata.eml.ipt.model.Citation;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;
import org.gbif.metadata.eml.ipt.model.KeywordSet;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;
import org.gbif.metadata.eml.ipt.model.TaxonKeyword;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TemporalCoverage;
import org.gbif.metadata.eml.parse.DatasetEmlParser;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.xml.sax.SAXException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.CAMTRAP_DP_OBSERVATIONS;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.EML_2_1_1_SCHEMA;
import static org.gbif.ipt.config.Constants.EML_2_2_0_SCHEMA;
import static org.gbif.ipt.config.DataDir.COL_DP_METADATA_FILENAME;
import static org.gbif.ipt.config.DataDir.EML_XML_FILENAME;
import static org.gbif.ipt.config.DataDir.FRICTIONLESS_METADATA_FILENAME;
import static org.gbif.ipt.model.Resource.CoreRowType.METADATA;
import static org.gbif.ipt.utils.FileUtils.getFileExtension;
import static org.gbif.ipt.utils.MetadataUtils.metadataClassForType;

public class ResourceManagerImpl extends BaseManager implements ResourceManager, ReportHandler {

  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<>();
  // simplified resources for home page (metadata from last published version!)
  private Map<String, SimplifiedResource> publishedPublicVersionsSimplified = new HashMap<>();
  private static final int MAX_PROCESS_FAILURES = 3;
  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private final XStream xstream = new XStream();
  private SourceManager sourceManager;
  private ExtensionManager extensionManager;
  private DataPackageSchemaManager schemaManager;
  private RegistryManager registryManager;
  private ResourceMetadataInferringService resourceMetadataInferringService;
  private ThreadPoolExecutor executor;
  private GenerateDwcaFactory dwcaFactory;
  private GenerateDataPackageFactory dataPackageFactory;
  private Map<String, Future<Map<String, Integer>>> processFutures = new HashMap<>();
  private ListValuedMap<String, Date> processFailures = new ArrayListValuedHashMap<>();
  private Map<String, LocalDate> lastLoggedFailures = new ConcurrentHashMap<>();
  private Map<String, StatusReport> processReports = new HashMap<>();
  private List<String> resourcesToSkip = new ArrayList<>();
  private Eml2Rtf eml2Rtf;
  private VocabulariesManager vocabManager;
  private SimpleTextProvider textProvider;
  private RegistrationManager registrationManager;
  private final MetadataReader metadataReader;

  private static final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
  private static final Comparator<Date> nullSafeDateComparator = Comparator.nullsFirst(Date::compareTo);
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public static final SimpleDateFormat CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, ResourceConvertersManager resourceConvertersManager,
                             SourceManager sourceManager, ExtensionManager extensionManager,
                             DataPackageSchemaManager schemaManager, RegistryManager registryManager,
                             GenerateDwcaFactory dwcaFactory, GenerateDataPackageFactory dataPackageFactory,
                             PasswordEncrypter passwordEncrypter, Eml2Rtf eml2Rtf, VocabulariesManager vocabManager,
                             SimpleTextProvider textProvider, RegistrationManager registrationManager,
                             MetadataReader metadataReader, ResourceMetadataInferringService resourceMetadataInferringService) {
    super(cfg, dataDir);
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.registryManager = registryManager;
    this.dwcaFactory = dwcaFactory;
    this.dataPackageFactory = dataPackageFactory;
    this.eml2Rtf = eml2Rtf;
    this.vocabManager = vocabManager;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cfg.getMaxThreads());
    defineXstreamMapping(resourceConvertersManager, passwordEncrypter);
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
    this.metadataReader = metadataReader;
    this.resourceMetadataInferringService = resourceMetadataInferringService;
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
          publishedPublicVersionsSimplified.put(res.getShortname(), toSimplifiedResourceReconstructedVersion(res));
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
    publishedPublicVersionsSimplified.values().stream()
            .filter(r -> r.getOrganisationKey() != null)
            .filter(r -> r.getOrganisationKey().equals(organisationKey))
            .forEach(r -> {
              r.setOrganisationName(organisationName);
              r.setOrganisationAlias(organisationAlias);
            });
  }

  /**
   * Converts regular Resource to lightweight SimplifiedResource.
   * Reconstructs resource from last published EML to take data before it was changed.
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  protected SimplifiedResource toSimplifiedResourceReconstructedVersion(Resource resource) {
    BigDecimal v = resource.getLastPublishedVersionsVersion();
    String shortname = resource.getShortname();

    File versionMetadataFile = resource.isDataPackage()
        ? cfg.getDataDir().resourceDatapackageMetadataFile(shortname, resource.getCoreType(), v)
        : cfg.getDataDir().resourceEmlFile(shortname, v);

    Resource publishedPublicVersion = ResourceUtils
        .reconstructVersion(v, resource.getShortname(), resource.getCoreType(), resource.getDataPackageIdentifier(), resource.getAssignedDoi(), resource.getOrganisation(),
            resource.findVersionHistory(v), versionMetadataFile, resource.getKey());

    SimplifiedResource result = new SimplifiedResource();
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
   * Converts regular Resource to lightweight SimplifiedResource.
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  private SimplifiedResource toSimplifiedResource(Resource resource) {
    SimplifiedResource result = new SimplifiedResource();
    result.setShortname(resource.getShortname());
    result.setTitle(resource.getTitle());
    result.setStatus(resource.getStatus());
    result.setRecordsPublished(resource.getRecordsPublished());
    result.setLogoUrl(resource.getLogoUrl());
    result.setSubject(resource.getSubject());
    if (resource.getOrganisation() != null) {
      result.setOrganisationKey(resource.getOrganisation().getKey());
      result.setOrganisationName(resource.getOrganisationName());
      result.setOrganisationAlias(resource.getOrganisationAlias());
    }
    result.setCoreType(resource.getCoreType());
    result.setSubtype(resource.getSubtype());
    result.setModified(resource.getModified());
    result.setPublished(resource.getLastPublished() != null);
    result.setLastPublished(resource.getLastPublished());
    result.setNextPublished(resource.getNextPublished());
    result.setCreatorName(resource.getCreatorName());
    result.setDataPackage(resource.isDataPackage());

    return result;
  }

  @Override
  public boolean cancelPublishing(String shortname, BaseAction action) {
    boolean canceled = false;
    // get future
    Future<Map<String, Integer>> f = processFutures.get(shortname);
    if (f != null) {
      // cancel job, even if it's running
      canceled = f.cancel(true);
      if (canceled) {
        // remove process from locking list
        processFutures.remove(shortname);
      } else {
        LOG.warn("Canceling publication of resource " + shortname + " failed");
      }
    }
    return canceled;
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
   *
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
   * @throws SAXException if failed to create validator
   * @throws IOException if failed to read EML file
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
   *
   * @return populated Eml instance
   *
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
      LOG.error("Unable to copy datapackage metadata file");
    }

    DataPackageMetadata metadata;
    try {
      metadata = metadataReader.readValue(dataDirMetadataFile, metadataClassForType(datapackageType));
    } catch (Exception e) {
      deleteDirectoryContainingSingleFile(dataDirMetadataFile);
      throw new ImportException("Invalid metadata document", e);
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
        LOG.info("Deleted directory: " + parent.getAbsolutePath());
      } catch (IOException e) {
        LOG.error("Failed to delete directory " + parent.getAbsolutePath() + ": " + e.getMessage(), e);
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
      LOG.debug("1st attempt to decompress file failed: " + e.getMessage(), e);
    } catch (Exception e) {
      LOG.debug("Decompression failed: " + e.getMessage(), e);
    }

    if (CollectionUtils.isEmpty(decompressed)) {
      // try again as single gzip file
      try {
        decompressed = CompressionUtil.ungzipFile(archiveDir, archiveOrSingleFile, false);
      } catch (Exception e2) {
        LOG.debug("2nd attempt to decompress file failed: " + e2.getMessage(), e2);
      }
    }

    // create resource:
    // if decompression failed, create resource from single file: eml.xml, datapackage.json or metadata.yml
    if (CollectionUtils.isEmpty(decompressed)) {
      String fileExtension = getFileExtension(archiveOrSingleFile);

      switch (fileExtension) {
        case "xml":
          resource = createFromEml(shortname, archiveOrSingleFile, creator, alog);
          break;
        case "json":
          resource = createFromPackageDescriptor(shortname, type, archiveOrSingleFile, creator, alog);
          break;
        case "yml":
          resource = createFromColDpMetadata(shortname, archiveOrSingleFile, creator, alog);
          break;
        default:
          throw new ImportException("Invalid file extension: " + fileExtension);
      }
    }
    // if decompression succeeded and archive is 'IPT Resource Folder'
    else if (isIPTResourceFolder(archiveDir)) {
      resource = createFromIPTResourceFolder(shortname, archiveDir, creator, alog);
    }
    // if decompression succeeded, create resource depending on whether file was a 'DwC-A',
    // a frictionless package (Camtrap DP) or a ColDP
    else {
      if (MetadataUtils.isDataPackageType(type)) {
        resource = createFromFrictionlessDataPackage(shortname, type, decompressed, creator, alog);
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
   *
   * @return Resource created or null if it was unsuccessful
   *
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
   *
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
    if (schemaIdentifier != null) {
      res.setDataPackageIdentifier(schemaIdentifier);
    }

    res.setCoreType(type);
    // first and last published dates are nulls
    res.getEml().setDateStamp(((Date) null));
    res.getEml().setPubDate(null);
    // create dir
    try {
      save(res);
      LOG.info("Created resource " + res.getShortname());
    } catch (InvalidConfigException e) {
      LOG.error("Error creating resource", e);
      return null;
    }
    return res;
  }

  private Resource createFromFrictionlessDataPackage(String shortname, String packageType, List<File> packageFiles, User creator,
                                                     ActionLogger alog)
    throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Objects.requireNonNull(shortname);
    // check if existing already
    if (get(shortname) != null) {
      throw new AlreadyExistingException();
    }
    Resource resource;
    try {
      // keep track of source files as a package might refer to the same source file multiple times
      Map<String, TextFileSource> sources = new HashMap<>();

      // create new resource
      resource = create(shortname, packageType, creator);
      Date lastModifiedDate = new Date();

      File metadataFile = null;

      for (File packageFile : packageFiles) {
        if ("csv".equals(getFileExtension(packageFile))) {
          TextFileSource s = importSource(resource, packageFile);
          // set default property
          s.setFieldsEnclosedBy("\"");
          String filenameWithoutExtension = FilenameUtils.removeExtension(packageFile.getName());
          sources.put(filenameWithoutExtension, s);

          DataPackageMapping map = importDataPackageMappings(alog, packageType, packageFile, s);
          map.setLastModified(lastModifiedDate);
          resource.addDataPackageMapping(map);
        } else if (packageFile.getName().equals(FRICTIONLESS_METADATA_FILENAME) && metadataFile == null) {
          metadataFile = packageFile;
        } else if (packageFile.getName().equals(COL_DP_METADATA_FILENAME)) {
          metadataFile = packageFile;
        }
      }

      resource.setSourcesModified(lastModifiedDate);
      resource.setMappingsModified(lastModifiedDate);

      // try to read metadata
      if (metadataFile != null) {
        DataPackageMetadata metadata = readDataPackageMetadata(resource.getShortname(), packageType, metadataFile, alog);

        if (metadata instanceof FrictionlessMetadata) {
          FrictionlessMetadata frictionlessMetadata = (FrictionlessMetadata) metadata;
          // set name, erase some internal fields
          frictionlessMetadata.setName(resource.getShortname());
          frictionlessMetadata.setId(null);
          frictionlessMetadata.setCreated(null);
          frictionlessMetadata.getAdditionalProperties().clear();
        }

        if (metadata instanceof CamtrapMetadata) {
          CamtrapMetadata camtrapMetadata = (CamtrapMetadata) metadata;

          camtrapMetadata.getContributors().stream()
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
        new String[] {String.valueOf(resource.getSources().size()), String.valueOf(resource.getDataPackageMappings().size())});
    } catch (UnsupportedArchiveException | InvalidConfigException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    }

    return resource;
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
            LOG.debug("SourceBase " + s.getName() + " shared by multiple extensions");
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
        new String[] {StringUtils.trimToEmpty(resource.getCoreRowType()), String.valueOf(resource.getSources().size()),
          String.valueOf(resource.getMappings().size())});
    } catch (UnsupportedArchiveException | InvalidConfigException | IOException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
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

    if (metadata instanceof ColMetadata) {
      ColMetadata colMetadata = (ColMetadata) metadata;
      colMetadata.setVersion(resource.getDataPackageMetadata().getVersion());
    }

    if (metadata instanceof FrictionlessMetadata) {
      FrictionlessMetadata frictionlessMetadata = (FrictionlessMetadata) metadata;
      // set name, erase some internal fields
      frictionlessMetadata.setName(resource.getShortname());
      frictionlessMetadata.setId(null);
      frictionlessMetadata.setCreated(null);
      frictionlessMetadata.getAdditionalProperties().clear();
      frictionlessMetadata.setVersion(resource.getDataPackageMetadata().getVersion());
    }

    if (metadata instanceof CamtrapMetadata) {
      CamtrapMetadata camtrapMetadata = (CamtrapMetadata) metadata;

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
   *
   * @return resource created
   *
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

      if (metadata instanceof FrictionlessMetadata) {
        FrictionlessMetadata frictionlessMetadata = (FrictionlessMetadata) metadata;
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
    publishedPublicVersionsSimplified.remove(resource.getShortname().toLowerCase());
  }

  @Override
  public void delete(Resource resource, boolean remove) throws IOException, DeletionNotAllowedException {
    // deregister resource?
    if (resource.isRegistered()) {
      try {
        registryManager.deregister(resource);
      } catch (RegistryException e) {
        LOG.error("Failed to deregister resource: " + e.getMessage(), e);
        throw new DeletionNotAllowedException(Reason.REGISTRY_ERROR, e.getMessage());
      }
    }

    // remove from data dir?
    if (remove) {
      FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
      // remove object
      resources.remove(resource.getShortname().toLowerCase());
      publishedPublicVersionsSimplified.remove(resource.getShortname().toLowerCase());
    }
  }

  // Generic method for DwC-A and data packages
  private void generateArchive(Resource resource) {
    if (resource.isDataPackage()) {
      generateDataPackage(resource);
    } else {
      generateDwca(resource);
    }
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDwca(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDwca worker = dwcaFactory.create(resource, this);
    Future<Map<String, Integer>> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDataPackage(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDataPackage worker = dataPackageFactory.create(resource, this);
    Future<Map<String, Integer>> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
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
   * @param alog ActionLogger
   * @param af ArchiveFile
   * @param source source file corresponding to ArchiveFile
   *
   * @return ExtensionMapping created from ArchiveFile
   * @throws InvalidConfigException if ExtensionMapping could not be created because the ArchiveFile uses
   * an extension that has not been installed yet.
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
      alog.warn("manage.resource.create.rowType.null", new String[] {af.getRowType().qualifiedName()});
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
      if (ext.hasProperty(f.getTerm())) {
        fields.add(new PropertyMapping(f));
      } else {
        alog.warn("manage.resource.create.mapping.concept.skip",
          new String[] {f.getTerm().qualifiedName(), ext.getRowType()});
      }
    }
    map.setFields(fields);

    return map;
  }

  private DataPackageMapping importDataPackageMappings(ActionLogger alog, String packageType, File file, Source source) {
    DataPackageMapping map = new DataPackageMapping();
    DataPackageSchema dataPackageSchema = schemaManager.get(packageType);
    String filenameWithoutExtension = FilenameUtils.removeExtension(file.getName());

    if (dataPackageSchema == null) {
      // cleanup source file immediately
      if (source.isFileSource()) {
        boolean deleted = FileUtils.deleteQuietly(file);
        // to bypass "Unable to delete file" error on Windows, run garbage collector to clean up file i/o mapping
        if (!deleted) {
          System.gc();
          FileUtils.deleteQuietly(file);
        }
      }
      alog.warn("manage.resource.create.schema.null", new String[] {packageType});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource references non-installed data schema");
    }

    DataPackageTableSchema tableSchema = dataPackageSchema.getTableSchemas().stream()
      .filter(s -> s.getName().equals(filenameWithoutExtension))
      .findAny()
      .orElse(null);

    if (tableSchema == null) {
      alog.warn("manage.resource.create.tableschema.null", new String[] {filenameWithoutExtension});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource references unknown schema");
    }

    map.setDataPackageSchema(dataPackageSchema);
    map.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchema.getName()));
    map.setSource(source);

    // extract column names from file's first row
    String[] columnNames;
    try (BufferedReader brTest = new BufferedReader(new FileReader(file))) {
      String fileHeaderRow = brTest.readLine();
      columnNames = StringUtils.split(fileHeaderRow, ",");
    } catch (IOException e) {
      alog.warn("manage.resource.create.tableschema.null", new String[] {filenameWithoutExtension});
      throw new InvalidConfigException(TYPE.INVALID_DATA_SCHEMA, "Resource references unknown schema");
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
          new String[] {columnNames[i], dataPackageSchema.getName() + "/" + tableSchema.getName()});
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
      LOG.info("Adjusting row count to " + (s.getRows() + 1 - s.getIgnoreHeaderLines()) + " from " + s.getRows()
               + " since header count is declared as " + s.getIgnoreHeaderLines());
    }
    s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());

    return s;
  }

  private TextFileSource importSource(Resource config, File file)
    throws ImportException, InvalidFilenameException {
    TextFileSource s = (TextFileSource) sourceManager.add(config, file, FilenameUtils.removeExtension(file.getName()));
    SourceManagerImpl.copyArchiveFileProperties(file, s);

    // the number of rows was calculated using the standard file importer
    // make an adjustment now that the exact number of header rows are known
    if (s.getIgnoreHeaderLines() != 1) {
      LOG.info("Adjusting row count to " + (s.getRows() + 1 - s.getIgnoreHeaderLines()) + " from " + s.getRows()
        + " since header count is declared as " + s.getIgnoreHeaderLines());
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
  public boolean isLocked(String shortname, BaseAction action) {
    if (processFutures.containsKey(shortname)) {
      Resource resource = get(shortname);
      BigDecimal version = resource.getMetadataVersion();

      // is listed as locked, but the task might be finished, check
      Future<Map<String, Integer>> f = processFutures.get(shortname);
      // if this task finished
      if (f.isDone()) {
        // remove the process from the locking list immediately! Fixes Issue 1141
        processFutures.remove(shortname);
        boolean succeeded = false;
        boolean dataOrMetadataChanged = true;
        boolean skipIfNotChanged = resourcesToSkip.contains(shortname) || resource.isSkipPublicationIfNotChanged();
        boolean noSignificantRecordsDrop = true;
        double dropPercentage;
        String reasonFailed = null;
        Throwable cause = null;
        try {
          if (resource.isSkipPublicationIfRecordsDrop()) {
            // check the number of records after publishing
            int newRecordCount = getResourceRecordsCount(resource, f.get());
            int previousRecordCount = resource.getRecordsPublished();
            int dropThreshold = resource.getRecordsDropThreshold();

            int dropAmount = previousRecordCount - newRecordCount;
            dropPercentage = previousRecordCount == 0 ? 0 : (dropAmount * 100.0) / previousRecordCount;

            if (dropPercentage > dropThreshold) {
              // drop is too big, prevent publication
              noSignificantRecordsDrop = false;
              String message = String.format(
                  "The number of records dropped more than allowed %d%%: %.2f%%.",
                  dropThreshold,
                  dropPercentage
              );
              LOG.error(message);
              getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, message));
            } else {
              LOG.debug("No significant drop in records detected.");
              getTaskMessages(shortname).add(new TaskMessage(Level.ERROR,
                  "No significant drop in records detected."));
            }
          }

          // if no significant drop (or it's switched off) - proceed with the publication
          if (noSignificantRecordsDrop) {
            // store record counts by extension
            resource.setRecordsByExtension(f.get());
            // populate record count
            Integer recordCount = getResourceRecordsCount(resource);
            resource.setRecordsPublished(recordCount);

            if (skipIfNotChanged) {
              getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "? Checking if data has been changed since last published"));
            }

            File resourceArchiveFile = dataDir.resourceArchiveFile(resource, version);
            LOG.debug("Calculating checksum for the resource: {}", shortname);
            try {
              String archiveChecksum = calculateArchiveChecksum(resourceArchiveFile);
              String lastPublishedArchiveChecksum = resource.getLastPublishedArchiveChecksum();

              if (lastPublishedArchiveChecksum == null) {
                LOG.debug("No checksum found for the resource {}", shortname);
                resource.setLastPublishedArchiveChecksum(archiveChecksum);

                // do not log additional info about checksum if it is disabled
                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "No checksum found for comparison, skipping."));
                }
              } else if (lastPublishedArchiveChecksum.equals(archiveChecksum)) {
                LOG.debug("New checksum [{}] matches the stored one [{}] for the resource {}",
                    archiveChecksum, lastPublishedArchiveChecksum, resource.getShortname());

                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Checksum has not changed since last published"));
                }

                // check metadata if data hasn't changed
                Date lastPublished = resource.getLastPublished();
                Date metadataLastModified = resource.getMetadataModified();
                boolean metadataChanged = metadataLastModified.after(lastPublished);

                if (skipIfNotChanged && !metadataChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Metadata has not changed since last published"));
                  dataOrMetadataChanged = false;
                }
              } else {
                LOG.debug("New checksum [{}] for the resource {}", archiveChecksum, resource.getShortname());
                resource.setLastPublishedArchiveChecksum(archiveChecksum);

                if (skipIfNotChanged) {
                  getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "✓ Checksum has changed since last published"));
                }
              }
            } catch (Exception e) {
              LOG.error("Failed to calculate checksum for DwC-A: {}", resourceArchiveFile.getName(), e);

              if (skipIfNotChanged) {
                getTaskMessages(shortname).add(new TaskMessage(Level.WARN, "Failed to calculate checksum"));
              }
            }

            if (dataOrMetadataChanged) {
              // finish publication (update registration, persist resource changes)
              publishEnd(resource, action, version);
              // important: indicate publishing finished successfully!
              succeeded = true;
            }
          }
        } catch (ExecutionException e) {
          // getCause holds the actual exception our callable (GenerateDwca) threw
          cause = e.getCause();
          if (cause instanceof GeneratorException) {
            reasonFailed = action.getText("dwca.failed", new String[] {shortname, cause.getMessage()});
          } else if (cause instanceof InterruptedException) {
            reasonFailed = action.getText("dwca.interrupted", new String[] {shortname, cause.getMessage()});
          } else {
            reasonFailed = action.getText("dwca.failed", new String[] {shortname, cause.getMessage()});
          }
        } catch (InterruptedException e) {
          reasonFailed = action.getText("dwca.interrupted", new String[] {shortname, e.getMessage()});
          cause = e;
        } catch (PublicationException e) {
          reasonFailed = action.getText("publishing.error", new String[] {e.getType().toString(), e.getMessage()});
          cause = e;
          // this type of exception happens outside GenerateDwca - so add reason to StatusReport
          getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, reasonFailed));
        } finally {
          // if publication was successful
          if (succeeded) {
            // update StatusReport on publishing page
            String msg =
              action.getText("publishing.success", new String[] {version.toPlainString(), resource.getShortname()});
            StatusReport updated = new StatusReport(true, msg, getTaskMessages(shortname));
            processReports.put(shortname, updated);
          } else {
            boolean failedDueToDataNotChanged = !dataOrMetadataChanged;
            boolean failedDueToRecordsDrop = !noSignificantRecordsDrop;

            if (failedDueToDataNotChanged) {
              reasonFailed = action.getText("publishing.dataNotChanged");
            }

            if (failedDueToRecordsDrop) {
              reasonFailed = action.getText("publishing.dropInRecords");
            }

            // alert user publication failed
            String msg =
              action.getText("publishing.failed", new String[] {version.toPlainString(), shortname, reasonFailed});
            action.addActionError(msg);

            // update StatusReport on publishing page
            if (cause != null) {
              StatusReport updated = new StatusReport(new Exception(cause), msg, getTaskMessages(shortname));
              processReports.put(shortname, updated);
            }

            if (failedDueToDataNotChanged) {
              String dataNotChanged = action.getText("publishing.dataNotChanged.revert");
              StatusReport updated = new StatusReport(true, dataNotChanged, getTaskMessages(shortname));
              processReports.put(shortname, updated);
              updateNextPublishedDate(new Date(), resource);
            }

            if (failedDueToRecordsDrop) {
              String dropInRecords = action.getText("publishing.dropInRecords.revert");
              StatusReport updated = new StatusReport(true, dropInRecords, getTaskMessages(shortname));
              processReports.put(shortname, updated);
              updateNextPublishedDate(new Date(), resource);
            }

            // the previous version needs to be rolled back
            restoreVersion(resource, version, action);

            // do not count "data not changed" as an actual failure
            if (!failedDueToDataNotChanged) {
              // keep track of how many failures on auto publication have happened
              processFailures.put(resource.getShortname(), new Date());
            }

            resourcesToSkip.remove(resource.getShortname());
          }
        }
        return false;
      }
      return true;
    }
    return false;
  }

  private Integer getResourceRecordsCount(Resource resource) {
    Integer recordCount;
    if (resource.isDataPackage()) {
      // take number of observations as number of records for Camtrap
      // for the rest data packages - total number of all records
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        recordCount = resource.getRecordsByExtension().get(CAMTRAP_DP_OBSERVATIONS);
      } else {
        recordCount = resource.getRecordsByExtension().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
      }
    } else {
      recordCount = resource.getRecordsByExtension().get(StringUtils.trimToEmpty(resource.getCoreRowType()));
    }
    return recordCount != null ? recordCount : 0;
  }

  private Integer getResourceRecordsCount(Resource resource, Map<String, Integer> publishedRecordsByExtension) {
    Integer recordCount;
    if (resource.isDataPackage()) {
      // take number of observations as number of records for Camtrap
      // for the rest data packages - total number of all records
      if (CAMTRAP_DP.equals(resource.getCoreType())) {
        recordCount = publishedRecordsByExtension.get(CAMTRAP_DP_OBSERVATIONS);
      } else {
        recordCount = publishedRecordsByExtension.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
      }
    } else {
      recordCount = publishedRecordsByExtension.get(StringUtils.trimToEmpty(resource.getCoreRowType()));
    }
    return recordCount != null ? recordCount : 0;
  }

  @Override
  public boolean isLocked(String shortname) {
    return isLocked(shortname, new BaseAction(textProvider, cfg, registrationManager));
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
  public DatatableResult listPublishedPublicVersionsSimplified(DatatableRequest request) {
    List<SimplifiedResource> filteredResources = publishedPublicVersionsSimplified.values().stream()
        .filter(p -> matchesSearchString(p, request.getSearch()))
        .collect(Collectors.toList());

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetTypesVocab(request.getLocale(), false));
    // add data packages
    List<DataPackageSchema> installedSchemas = schemaManager.list();
    for (DataPackageSchema installedSchema : installedSchemas) {
      datasetTypes.put(
        installedSchema.getName(),
        Optional.ofNullable(installedSchema.getShortTitle()).orElse(installedSchema.getName()));
    }

    Map<String, String> datasetSubtypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetSubtypesVocab(request.getLocale(), false));

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(res -> toDatatableResourcePortalView(res, currentLocale, datasetTypes, datasetSubtypes))
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(publishedPublicVersionsSimplified.values().size());
    result.setTotalDisplayRecords(filteredResources.size());
    result.setData(data);

    return result;
  }

  /**
   * Produces comparator from the raw parameters.
   *
   * @param index field index (1 - title, 2 - organization, 3 - core type etc.)
   * @param order asc/desc
   * @return comparator
   */
  private Comparator<SimplifiedResource> resourceComparator(int index, String order) {
    boolean isDescendingOrder = isDescendingOrder(order);
    if (index == 1) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getTitleOrShortname, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getTitleOrShortname, nullSafeStringComparator);
    } else if (index == 2) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getOrganizationAliasOrName, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getOrganizationAliasOrName, nullSafeStringComparator);
    } else if (index == 3) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getCoreType, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getCoreType, nullSafeStringComparator);
    } else if (index == 4) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getSubtype, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getSubtype, nullSafeStringComparator);
    } else if (index == 5) {
      return isDescendingOrder ?
          Comparator.comparingInt(SimplifiedResource::getRecordsPublished).reversed() :
          Comparator.comparingInt(SimplifiedResource::getRecordsPublished);
    } else if (index == 6) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getModified, nullSafeDateComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getModified, nullSafeDateComparator);
    } else if (index == 7) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getLastPublished, nullSafeDateComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getLastPublished, nullSafeDateComparator);
    } else if (index == 8) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getNextPublished, nullSafeDateComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getNextPublished, nullSafeDateComparator);
    } else if (index == 9) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getStatus, Comparator.nullsFirst(PublicationStatus::compareTo)).reversed() :
          Comparator.comparing(SimplifiedResource::getStatus, Comparator.nullsFirst(PublicationStatus::compareTo));
    } else if (index == 10) {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getCreatorName, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getCreatorName, nullSafeStringComparator);
    } else {
      return isDescendingOrder ?
          Comparator.comparing(SimplifiedResource::getShortname, nullSafeStringComparator).reversed() :
          Comparator.comparing(SimplifiedResource::getShortname, nullSafeStringComparator);
    }
  }

  /**
   * Check whether sort order is descending.
   *
   * @param order raw sort order string
   * @return true if descending, false otherwise
   */
  private boolean isDescendingOrder(String order) {
    return StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(order), "desc");
  }

  /**
   * Check if provided string is present in one of the searchable fields.
   *
   * @param resource lightweight resource
   * @param search search string
   * @return true/false
   */
  private boolean matchesSearchString(SimplifiedResource resource, String search) {
    if (StringUtils.isEmpty(search)) {
      return true;
    }

    return StringUtils.containsIgnoreCase(resource.getShortname(), search)
        || StringUtils.containsIgnoreCase(resource.getTitle(), search)
        || StringUtils.containsIgnoreCase(resource.getOrganisationAlias(), search)
        || StringUtils.containsIgnoreCase(resource.getOrganisationName(), search)
        || StringUtils.containsIgnoreCase(resource.getCoreType(), search)
        || StringUtils.containsIgnoreCase(resource.getSubtype(), search)
        || StringUtils.containsIgnoreCase(resource.getCreatorName(), search)
        || StringUtils.containsIgnoreCase(resource.getSubject(), search);
  }

  /**
   * Converts raw data (one simplified resource) to UI data for portal home page.
   * BEWARE! Order is crucial!
   *
   * @param resource simplified resource
   * @param datasetTypes dataset types vocabulary
   * @param datasetSubtypes dataset subtypes vocabulary
   * @return UI data (array)
   */
  private List<String> toDatatableResourcePortalView(
      SimplifiedResource resource, Locale locale, Map<String, String> datasetTypes, Map<String, String> datasetSubtypes) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceHomeLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType(), datasetTypes));
    result.add(toTypeBadge(resource.getSubtype(), datasetSubtypes));
    result.add(toUiRecordsPublished(resource, locale));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus(), locale));
    result.add(resource.getCreatorName());
    result.add(resource.getShortname());
    result.add(resource.getSubject() != null ? resource.getSubject() : "");

    return result;
  }

  /**
   * Converts raw data (one simplified resource) to UI data for manage home page.
   * BEWARE! Order is crucial!
   *
   * @param resource simplified resource
   * @param datasetTypes dataset types vocabulary
   * @param datasetSubtypes dataset subtypes vocabulary
   * @return UI data (array)
   */
  private List<String> toDatatableResourceManageView(
      SimplifiedResource resource, Locale locale, Map<String, String> datasetTypes, Map<String, String> datasetSubtypes) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceManageLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType(), datasetTypes));
    result.add(toTypeBadge(resource.getSubtype(), datasetSubtypes));
    result.add(toUiRecordsPublished(resource, locale));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus(), locale));
    result.add(resource.getCreatorName());
    result.add(resource.getShortname());
    result.add(resource.getSubject() != null ? resource.getSubject() : "");

    return result;
  }

  /**
   * Converts raw data to UI format.
   * Date formatted as yyyy-MM-dd HH:mm:ss or "--" if empty.
   *
   * @param date date
   * @return formatted date
   */
  private String toUiDateTime(Date date) {
    if (date == null) {
      return "<span>--</span>";
    }
    return DATETIME_FORMAT.format(date);
  }

  /**
   * Converts raw data to UI format.
   * Next publication date formatted as yyyy-MM-dd HH:mm:ss or "--" if empty.
   * Next published date should never be before today's date, otherwise auto-publication must have failed.
   * In this case, highlight the row to bring the problem to the resource manager's attention.
   *
   * @param date next publication date
   * @return formatted date
   */
  private String toUiNextPublished(Date date) {
    if (date == null) {
      return "<span>--</span>";
    }

    Date now = new Date();

    // highlight if next published is before now (something wrong)
    return date.before(now)
        ? "<span class=\"text-gbif-danger\">" + DATETIME_FORMAT.format(date) + "</span>"
        : DATETIME_FORMAT.format(date);
  }

  /**
   * Converts raw data to UI format.
   * Logo URL or "--" if empty
   *
   * @param logoUrl logo URL
   * @return Logo URL or "--" if empty
   */
  private String toUiLogoUrl(String logoUrl) {
    if (logoUrl == null) {
      return "<span>--</span>";
    }
    return "<img class=\"resourceminilogo\" src=\"" + logoUrl + "\"/>";
  }

  /**
   * Converts raw data to UI format.
   * Organization alias or name or "--" if empty
   *
   * @param resource lightweight resource
   * @return alias or name or "--"
   */
  private String toUiOrganization(SimplifiedResource resource) {
    String result = resource.getOrganizationAliasOrName();
    return result != null && !"No organization".equals(result) ? result : "--";
  }

  /**
   * Converts raw data to UI format.
   * Wraps number of published records into a link and format number according to the locale.
   *
   * @param resource lightweight resource
   * @param locale locale
   * @return link to records section
   */
  private String toUiRecordsPublished(SimplifiedResource resource, Locale locale) {
    NumberFormat format = NumberFormat.getInstance(locale);
    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "#anchor-dataRecords'>" + format.format(resource.getRecordsPublished()) + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps core type or subtype into span to make it badge on UI.
   *
   * @param type core type or subtype
   * @param vocab vocabulary map
   * @return wrapped type (badge)
   */
  private String toTypeBadge(String type, Map<String, String> vocab) {
    if (type == null) {
      return "<span>--</span>";
    }
    return "<span class=\"fs-smaller-2 text-nowrap dt-content-link dt-content-pill type-" + type.toLowerCase() + "\">" + vocab.getOrDefault(type.toLowerCase(), "--") + "</span>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps resource title or shortname into a link (home page)
   *
   * @param resource lightweight resource
   * @return link to resource (home page)
   */
  private String toResourceHomeLink(SimplifiedResource resource) {
    String resourceName = StringUtils.defaultIfEmpty(resource.getTitle(), resource.getShortname());
    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "'>" + resourceName + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps resource title or shortname into a link (manage page)
   *
   * @param resource lightweight resource
   * @return link to resource (manage page)
   */
  private String toResourceManageLink(SimplifiedResource resource) {
    String resourceName = StringUtils.defaultIfEmpty(resource.getTitle(), resource.getShortname());
    return "<a class=\"resource-table-link\" href='" + cfg.getBaseUrl() + "/manage/resource?r=" + resource.getShortname() + "'>" + resourceName + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps lower case status into span to make it badge on UI.
   *
   * @param status publication status
   * @return wrapped publication status (badge)
   */
  private String toUiStatus(PublicationStatus status, Locale locale) {
    String localizedStatus = textProvider.getTexts(locale).getString("manage.home.visible." + status.name().toLowerCase());
    String icon;
    if (status == PublicationStatus.PUBLIC || status == PublicationStatus.PRIVATE) {
      icon = "<i class=\"bi bi-circle fs-smaller-2 me-1\"></i>";
    } else {
      icon = "<i class=\"bi bi-circle-fill fs-smaller-2 me-1\"></i>";
    }
    return "<span class=\"text-nowrap status-pill fs-smaller-2 status-" + status.name().toLowerCase() + "\">" +
        icon +
        "<span>" +
        localizedStatus +
        "</span>" +
        "</span>";
  }

  @Override
  public DatatableResult list(User user, DatatableRequest request) {
    List<SimplifiedResource> filteredResources = resources.values().stream()
        .filter(res -> RequireManagerInterceptor.isAuthorized(user, res))
        .map(this::toSimplifiedResource)
        .filter(res -> matchesSearchString(res, request.getSearch()))
        .collect(Collectors.toList());

    Locale currentLocale = Locale.forLanguageTag(request.getLocale());

    Map<String, String> datasetTypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetTypesVocab(request.getLocale(), false));
    // add data packages
    List<DataPackageSchema> installedSchemas = schemaManager.list();
    for (DataPackageSchema installedSchema : installedSchemas) {
      datasetTypes.put(
        installedSchema.getName(),
        Optional.ofNullable(installedSchema.getShortTitle()).orElse(installedSchema.getName()));
    }
    Map<String, String> datasetSubtypes =
        MapUtils.getMapWithLowercaseKeys(
            vocabManager.getI18nDatasetSubtypesVocab(request.getLocale(), false));

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(res -> toDatatableResourceManageView(res, currentLocale, datasetTypes, datasetSubtypes))
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(resources.values().size());
    result.setTotalDisplayRecords(filteredResources.size());
    result.setData(data);

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
            LOG.error("Resource directory " + resourceDir.getName() + " could not be read. Please verify its content");
          } else if (resourceDirFiles.length == 0) {
            LOG.warn("Cleaning up empty resource directory " + resourceDir.getName());
            FileUtils.deleteQuietly(resourceDir);
            counterDeleted++;
          } else {
            try {
              LOG.debug("Loading resource from directory " + resourceDir.getName());
              addResource(loadFromDir(resourceDir, creator));
              counter++;
            } catch (InvalidConfigException e) {
              LOG.error("Can't load resource " + resourceDir.getName(), e);
            }
          }
        }
      }
      LOG.info("Loaded " + counter + " resources into memory altogether.");
      LOG.info("Cleaned up " + counterDeleted + " resources altogether.");
    } else {
      LOG.error("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
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
      metadata = new FrictionlessMetadata();
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
        ((FrictionlessMetadata) metadata).setName(resource.getShortname());
      }
    }

    resource.setDataPackageMetadata(metadata);
  }

  private void loadMetadata(Resource resource) {
    if (resource.isDataPackage()) {
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
        LOG.error("Cannot read inferred metadata file (Camtrap) for resource " + resource.getShortname(), e);
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
        LOG.error("Cannot read inferred metadata file (EML) for resource " + resource.getShortname(), e);
        resource.setInferredMetadata(new InferredEmlMetadata());
      }
    }
  }

  /**
   * Calls loadFromDir(File, User, ActionLogger), inserting a new instance of ActionLogger.
   *
   * @param resourceDir resource directory
   * @param creator User that created resource (only used to populate creator when missing)
   *
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
          LOG.warn("On load, populated missing creator for resource: " + shortname);
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
            alog.warn("manage.resource.create.extension.null", new String[] {ext.getExtensionVerbatim()});
            throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Resource references non-existent extension");
          } else if (extensionManager.get(x.getRowType()) == null) {
            alog.warn("manage.resource.create.rowType.null", new String[] {x.getRowType()});
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
          if (src instanceof FileSource) {
            FileSource frSrc = (FileSource) src;
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
        BigDecimal converted = convertVersion(resource);
        if (converted != null) {
          updateResourceVersion(resource, resource.getMetadataVersion(), converted);
        }

        // pre v2.2 resources: construct a VersionHistory for last published version (if appropriate)
        VersionHistory history = constructVersionHistoryForLastPublishedVersion(resource);
        if (history != null) {
          resource.addVersionHistory(history);
        }

        if (resource.getDataPackageIdentifier() == null) {
          // pre v2.2.1 resources: rename dwca.zip to dwca-18.0.zip (where 18.0 is the last published version for example)
          if (resource.getLastPublishedVersionsVersion() != null) {
            renameDwcaToIncludeVersion(resource, resource.getLastPublishedVersionsVersion());
          }

          // update EML with the latest resource basics (version and GUID)
          syncEmlWithResource(resource);
        }

        LOG.debug("Read resource configuration for " + shortname);
        return resource;
      } catch (Exception e) {
        LOG.error("Cannot read resource configuration for " + shortname, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG,
          "Cannot read resource configuration for " + shortname + ": " + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Convert integer version number to major_version.minor_version version number. Please note IPTs before v2.2 used
   * integer-based version numbers.
   *
   * @param resource resource
   *
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
        LOG.debug("Converted version [" + version.toPlainString() + "] to [" + majorMinorVersion.toPlainString() + "]");
        return majorMinorVersion;
      }
    }
    return null;
  }

  /**
   * Update a resource's version, and rename its eml, rtf, and dwca versioned files to have the new version also.
   *
   * @param resource   resource to update
   * @param oldVersion old version number
   * @param newVersion new version number
   *
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
        LOG.error("Failed to update version number for " + resource.getShortname(), e);
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
        LOG.debug("Renamed dwca.zip to " + versionedDwca.getName());
      } catch (IOException e) {
        LOG.error("Failed to rename dwca.zip file name with version number for " + resource.getShortname(), e);
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
   *
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
   *
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
   *
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

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action)
      throws PublicationException, InvalidConfigException {
    return publish(resource, version, action, false);
  }

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action, boolean skipIfNotChanged)
      throws PublicationException, InvalidConfigException {
    PublicationOptions options = PublicationOptions.builder().skipPublicationIfNotChanged(skipIfNotChanged).build();
    return publish(resource, version, action, options);
  }

  @Override
  public boolean publish(Resource resource, BigDecimal version, BaseAction action, PublicationOptions options)
    throws PublicationException, InvalidConfigException {
    String shortname = resource.getShortname();

    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    // add new version history
    addOrUpdateVersionHistory(resource, version, false, action);

    // remove StatusReport from previous publishing round
    StatusReport report = status(shortname);
    if (report != null) {
      processReports.remove(shortname);
    }

    // Abort further publication for Metadata Only - no changes (if skipIfNotChanged activated)
    if (resource.isMetadataOnly() && options.isSkipPublicationIfNotChanged()) {
      Date lastPublished = resource.getLastPublished();
      Date metadataLastModified = resource.getMetadataModified();

      boolean metadataChanged = metadataLastModified.after(lastPublished);

      if (!metadataChanged) {
        String metadataNotChangedStatus = action.getText("publishing.metadataNotChanged");
        StatusReport updated = new StatusReport(true, metadataNotChangedStatus, getTaskMessages(shortname));
        processReports.put(shortname, updated);

        return false;
      }
    }

    preventPublicationForSourcesInProcessingState(resource);

    publishMetadata(resource, version, action);
    publishRtf(resource, version);

    // (re)generate archive (DwC-A/DP) asynchronously
    boolean archive = false;
    if (resource.hasAnyMappedData()) {
      // for bulk publication keep resources to be skipped
      if (options.isSkipPublicationIfNotChanged()) {
        resourcesToSkip.add(shortname);
      }
      generateArchive(resource);
      archive = true;
    } else {
      // set number of records published
      resource.setRecordsPublished(0);
      // finish publication now
      publishEnd(resource, action, version);
    }
    return archive;
  }

  private void preventPublicationForSourcesInProcessingState(Resource resource) {
    Optional<Source> sourceBeingProcessed = resource.getMappings().stream()
        .map(ExtensionMapping::getSource)
        .filter(Source::isProcessing)
        .findAny();

    if (sourceBeingProcessed.isPresent()) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
          "Resource's " + resource.getShortname() + " source " + sourceBeingProcessed.get() + " is currently being processed");
    }
  }

  /**
   * Update the resource's registration (if registered) and persist any changes to the resource.
   * </br>
   * Publishing is split into 2 parts because DwC-A generation is asynchronous. This 2nd part of publishing can only
   * be called after DwC-A has completed successfully.
   *
   * @param resource resource
   * @param action   action
   * @param version  version number to finalize publishing
   *
   * @throws PublicationException   if publication was unsuccessful
   * @throws InvalidConfigException if resource configuration could not be saved
   */
  private void publishEnd(Resource resource, BaseAction action, BigDecimal version)
    throws PublicationException, InvalidConfigException {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    BigDecimal replacedMetadataVersion = resource.getReplacedMetadataVersion();

    // update the resource's registration (if registered), even if it is a metadata-only resource.
    updateRegistration(resource, action);
    // set the last published date
    resource.setLastPublished(new Date());
    // set the next published date (if resource configured for auto-publishing)
    updateNextPublishedDate(new Date(), resource);
    // register/update DOI
    executeDoiWorkflow(resource, version, replacedMetadataVersion, action);
    // finalise/update version history
    addOrUpdateVersionHistory(resource, version, true, action);
    // remove resource from the list if it's private
    if (resource.getStatus() == PublicationStatus.PRIVATE) {
      publishedPublicVersionsSimplified.remove(resource.getShortname());
    }
    // persist resource object changes
    save(resource);
    // if archival mode is NOT turned on, don't keep former archive version (version replaced)
    if (!cfg.isArchivalMode() && version.compareTo(replacedMetadataVersion) != 0) {
      removeArchiveVersion(resource.getShortname(), replacedMetadataVersion);
    }
    // clean archive versions
    if (cfg.isArchivalMode() && cfg.getArchivalLimit() != null && cfg.getArchivalLimit() > 0) {
      cleanArchiveVersions(resource);
    }
    // final logging
    String msg = action
      .getText("publishing.success", new String[] {String.valueOf(resource.getMetadataVersion()), resource.getShortname()});
    action.addActionMessage(msg);
    LOG.info(msg);
  }

  /**
   * Depending on the state of the resource and its DOI, execute one of the following operations:
   * - Register DOI
   * - Update DOI
   * - Register DOI and replace previous DOI
   *
   * @param resource        resource published
   * @param version         resource version being published
   * @param versionReplaced resource version being replaced
   * @param action          action
   *
   * @throws PublicationException thrown if any part of DOI workflow failed
   */
  private void executeDoiWorkflow(Resource resource, BigDecimal version, BigDecimal versionReplaced, BaseAction action)
    throws PublicationException {
    // All DOI operations require resource be publicly available, and resource DOI be PUBLIC/PUBLIC_PENDING_PUBLICATION
    if (resource.getDoi() != null && resource.isPubliclyAvailable() && (
      resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION) || resource
        .getIdentifierStatus().equals(IdentifierStatus.PUBLIC))) {
      if (resource.getIdentifierStatus().equals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION)) {
        if (resource.isAlreadyAssignedDoi()) {
          // another new major version that replaces previous version
          doReplaceDoi(resource, version, versionReplaced);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion.replaces",
            new String[] {resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        } else {
          // initial major version
          doRegisterDoi(resource, null);
          String msg = action.getText("manage.overview.publishing.doi.publish.newMajorVersion",
            new String[] {resource.getDoi().toString()});
          LOG.info(msg);
          action.addActionMessage(msg);
        }
      } else {
        // minor version increment
        doUpdateDoi(resource);
        String msg = action.getText("manage.overview.publishing.doi.publish.newMinorVersion",
          new String[] {resource.getDoi().toString()});
        LOG.info(msg);
        action.addActionMessage(msg);
      }
    }
  }

  /**
   * Register DOI. Corresponds to a major version change.
   *
   * @param resource resource whose DOI will be registered
   */
  protected void doRegisterDoi(Resource resource, @Nullable DOI replaced) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DataCiteMetadata dataCiteMetadata = null;
      DOI doi = resource.getDoi();
      try {
        // DOI resolves to IPT public resource page
        URI uri = cfg.getResourceUri(resource.getShortname());
        dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);

        // if this resource (DOI) replaces a former resource version (DOI) add isNewVersionOf RelatedIdentifier
        if (replaced != null) {
          DataCiteMetadataBuilder.addIsNewVersionOfDOIRelatedIdentifier(dataCiteMetadata, replaced);
        }

        registrationManager.getDoiService().register(doi, uri, dataCiteMetadata);
        resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
        resource.updateAlternateIdentifierForDOI();
        resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
      } catch (DoiExistsException e) {
        LOG.warn(
          "Received DoiExistsException registering resource meaning this is an existing DOI that should be updated instead",
          e);
        try {
          registrationManager.getDoiService().update(doi, dataCiteMetadata);
          resource.setIdentifierStatus(
            IdentifierStatus.PUBLIC); // must transition reused (registered DOI) from public_pending_publication to public
          resource.updateAlternateIdentifierForDOI();
          resource.updateCitationIdentifierForDOI(); // set DOI as citation identifier
        } catch (DoiException e2) {
          String errorMsg = "Failed to update existing DOI  " + doi.toString() + ": " + e2.getMessage();
          LOG.error(errorMsg, e2);
          throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e2);
        }
      } catch (InvalidMetadataException e) {
        String errorMsg =
          "Failed to register " + doi.toString() + " because DOI metadata was invalid: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to register " + doi.toString() + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(TYPE.INVALID_DOI_REGISTRATION,
        "Resource not in required state to register DOI!");
    }
  }

  /**
   * Update DOI metadata. The DOI URI isn't changed. This is done for each minor version change.
   *
   * @param resource resource whose DOI will be updated
   */
  protected void doUpdateDoi(Resource resource) {
    Objects.requireNonNull(resource);

    if (resource.getDoi() != null && resource.isPubliclyAvailable()) {
      DOI doi = resource.getDoi();
      try {
        DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
        registrationManager.getDoiService().update(doi, dataCiteMetadata);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doi.toString() + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to update DOI!");
    }
  }

  /**
   * Replace DOI currently assigned to resource with new DOI that has been reserved for resource.
   * This corresponds to a new major version change.
   *
   * @param resource resource whose DOI will be registered
   * @param version new version
   * @param replacedVersion previous version being replaced
   */
  protected void doReplaceDoi(Resource resource, BigDecimal version, BigDecimal replacedVersion) {
    Objects.requireNonNull(resource);

    DOI doiToRegister = resource.getDoi();
    DOI doiToReplace = resource.getAssignedDoi();

    if (doiToRegister != null && resource.isPubliclyAvailable() && doiToReplace != null
        && resource.getMetadataVersion() != null && resource.getMetadataVersion().compareTo(version) == 0
        && replacedVersion != null && resource.findVersionHistory(replacedVersion) != null) {

      // register new DOI first, indicating it replaces former DOI
      doRegisterDoi(resource, doiToReplace);

      // update previously assigned DOI, indicating it has been replaced by new DOI
      try {
        // reconstruct last published version (version being replaced)
        File replacedVersionEmlFile = dataDir.resourceEmlFile(resource.getShortname(), replacedVersion);
        Resource lastPublishedVersion = ResourceUtils
          .reconstructVersion(replacedVersion, resource.getShortname(), resource.getCoreType(), resource.getDataPackageIdentifier(), doiToReplace, resource.getOrganisation(),
            resource.findVersionHistory(replacedVersion), replacedVersionEmlFile, resource.getKey());

        DataCiteMetadata assignedDoiMetadata =
          DataCiteMetadataBuilder.createDataCiteMetadata(doiToReplace, lastPublishedVersion);

        // add isPreviousVersionOf new resource version registered above
        DataCiteMetadataBuilder.addIsPreviousVersionOfDOIRelatedIdentifier(assignedDoiMetadata, doiToRegister);

        // update its URI first
        URI resourceVersionUri = cfg.getResourceVersionUri(resource.getShortname(), replacedVersion);
        registrationManager.getDoiService().update(doiToReplace, resourceVersionUri);
        // then update its metadata
        registrationManager.getDoiService().update(doiToReplace, assignedDoiMetadata);

      } catch (InvalidMetadataException e) {
        String errorMsg = "Failed to update " + doiToReplace + " metadata: " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (DoiException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      } catch (IllegalArgumentException e) {
        String errorMsg = "Failed to update " + doiToReplace + ": " + e.getMessage();
        LOG.error(errorMsg, e);
        throw new PublicationException(PublicationException.TYPE.DOI, errorMsg, e);
      }
    } else {
      throw new InvalidConfigException(TYPE.INVALID_DOI_REGISTRATION, "Resource not in required state to replace DOI!");
    }
  }

  /**
   * After ensuring the version being rolled back is equal to the last version of the resource attempted to be
   * published, the method returns the last successfully published version, which is the version to restore.
   *
   * @param resource   resource
   * @param toRollBack version to rollback
   *
   * @return the version to restore, or null if version history is invalid
   */
  private BigDecimal getVersionToRestore(@NotNull Resource resource, @NotNull BigDecimal toRollBack) {
    BigDecimal lastVersion = resource.getLastVersionHistoryVersion();
    BigDecimal penultimateVersion = resource.getLastPublishedVersionsVersion();

    // return penultimate version if all checks pass
    if (penultimateVersion != null && penultimateVersion.compareTo(Constants.INITIAL_RESOURCE_VERSION) >= 0
        && lastVersion != null && lastVersion.compareTo(toRollBack) == 0
        && penultimateVersion.compareTo(lastVersion) != 0) {
      return penultimateVersion;
    }
    return null;
  }

  @Override
  public void restoreVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }

    if (resource.isDataPackage()) {
      restoreDataPackageResourceVersion(resource, rollingBack, action);
    } else {
      restoreDarwinCoreResourceVersion(resource, rollingBack, action);
    }
  }

  private void restoreDarwinCoreResourceVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // determine version to restore (looking at version history)
    BigDecimal toRestore = getVersionToRestore(resource, rollingBack);

    if (toRestore != null) {
      String shortname = resource.getShortname();
      LOG.info(
        "Rolling back version #" + rollingBack.toPlainString() + ". Restoring version #" + toRestore.toPlainString()
        + " of resource " + shortname);

      try {
        // delete eml-1.1.xml if it exists (eml.xml must remain)
        File versionedEMLFile = dataDir.resourceEmlFile(shortname, rollingBack);
        if (versionedEMLFile.exists()) {
          FileUtils.forceDelete(versionedEMLFile);
        }
        // delete shortname-1.1.rtf if it exists
        File versionedRTFFile = dataDir.resourceRtfFile(shortname, rollingBack);
        if (versionedRTFFile.exists()) {
          FileUtils.forceDelete(versionedRTFFile);
        }
        // delete dwca-1.1.zip if it exists
        File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, rollingBack);
        if (versionedDwcaFile.exists()) {
          FileUtils.forceDelete(versionedDwcaFile);
        }

        // remove VersionHistory of version being rolled back
        resource.removeVersionHistory(rollingBack);

        // reset recordsPublished count from restored VersionHistory
        VersionHistory restoredVersionVersionHistory = resource.findVersionHistory(toRestore);
        if (restoredVersionVersionHistory != null) {
          resource.setRecordsPublished(restoredVersionVersionHistory.getRecordsPublished());
        }

        // update version
        resource.setMetadataVersion(toRestore);

        // update replaced version with next last version
        if (resource.getVersionHistory().size() > 1) {
          BigDecimal replacedVersion = new BigDecimal(resource.getVersionHistory().get(1).getVersion());
          resource.setReplacedEmlVersion(replacedVersion);
        }

        // persist resource.xml changes
        save(resource);

        // restore EML pubDate to last published date (provided last published date exists)
        if (resource.getLastPublished() != null) {
          resource.getEml().setPubDate(resource.getLastPublished());
        }

        // persist EML changes
        saveEml(resource);

      } catch (IOException e) {
        String msg = action
          .getText("restore.resource.failed", new String[] {toRestore.toPlainString(), shortname, e.getMessage()});
        LOG.error(msg, e);
        action.addActionError(msg);
      }
      // alert user version rollback was successful
      String msg = action.getText("restore.resource.success", new String[] {toRestore.toPlainString(), shortname});
      LOG.info(msg);
      action.addActionMessage(msg);
      // update StatusReport on publishing page
      // Warning: don't retrieve status report using status() otherwise a cyclical call to isLocked results
      StatusReport report = processReports.get(shortname);
      if (report != null) {
        report.getMessages().add(new TaskMessage(Level.INFO, msg));
      }
    } else {
      String msg = action
          .getText("restore.resource.failed.version.notFound", new String[] {rollingBack.toPlainString()});
      LOG.error(msg);
      action.addActionError(msg);
    }
  }

  private void restoreDataPackageResourceVersion(Resource resource, BigDecimal rollingBack, BaseAction action) {
    // determine version to restore (looking at version history)
    BigDecimal toRestore = getVersionToRestore(resource, rollingBack);

    if (toRestore != null) {
      String shortname = resource.getShortname();
      LOG.info(
              "Rolling back version #" + rollingBack.toPlainString() + ". Restoring version #" + toRestore.toPlainString()
                      + " of resource " + shortname);

      try {
        // delete versioned metadata file if exists (datapackage.json must remain)
        File versionedCamtrapMetadataFile = dataDir.resourceDatapackageMetadataFile(shortname, CAMTRAP_DP, rollingBack);
        if (versionedCamtrapMetadataFile.exists()) {
          FileUtils.forceDelete(versionedCamtrapMetadataFile);
        }

        File versionedColMetadataFile = dataDir.resourceDatapackageMetadataFile(shortname, COL_DP, rollingBack);
        if (versionedColMetadataFile.exists()) {
          FileUtils.forceDelete(versionedColMetadataFile);
        }

        // delete versioned data package archive if exists
        File versionedDataPackageFile = dataDir.resourceDataPackageFile(shortname, rollingBack);
        if (versionedDataPackageFile.exists()) {
          FileUtils.forceDelete(versionedDataPackageFile);
        }

        // remove VersionHistory of version being rolled back
        resource.removeVersionHistory(rollingBack);

        // update version
        resource.setMetadataVersion(toRestore);

        // update replaced version with next last version
        if (resource.getVersionHistory().size() > 1) {
          BigDecimal replacedVersion = new BigDecimal(resource.getVersionHistory().get(1).getVersion());
          resource.setReplacedDataPackageMetadataVersion(replacedVersion);
        }

        // persist resource.xml changes
        save(resource);

        // persist EML changes
        saveDatapackageMetadata(resource);

      } catch (IOException e) {
        String msg = action
                .getText("restore.resource.failed", new String[] {toRestore.toPlainString(), shortname, e.getMessage()});
        LOG.error(msg, e);
        action.addActionError(msg);
      }
      // alert user version rollback was successful
      String msg = action.getText("restore.resource.success", new String[] {toRestore.toPlainString(), shortname});
      LOG.info(msg);
      action.addActionMessage(msg);
      // update StatusReport on publishing page
      // Warning: don't retrieve status report using status() otherwise a cyclical call to isLocked results
      StatusReport report = processReports.get(shortname);
      if (report != null) {
        report.getMessages().add(new TaskMessage(Level.INFO, msg));
      }
    } else {
      String msg = action
              .getText("restore.resource.failed.version.notFound", new String[] {rollingBack.toPlainString()});
      LOG.error(msg);
      action.addActionError(msg);
    }
  }

  /**
   * Updates the resource's alternate identifier for its corresponding Registry UUID and saves the EML.
   * If called on a resource that is already registered, the method ensures that it won't be added a second time.
   * To accommodate updates from older versions of the IPT, the identifier is added by calling this method every
   * time the resource gets re-published.
   *
   * @param resource resource
   *
   * @return resource with Registry UUID for the resource updated
   */
  public Resource updateAlternateIdentifierForRegistry(Resource resource) {
    Eml eml = resource.getEml();
    if (eml != null) {
      // retrieve a list of the resource's alternate identifiers
      List<String> currentIds = eml.getAlternateIdentifiers();
      if (currentIds != null) {
        // make new list of alternative identifiers in lower case so comparison is done in lower case only
        List<String> ids = new ArrayList<>();
        for (String id : currentIds) {
          ids.add(id.toLowerCase());
        }
        if (resource.isRegistered()) {
          // GBIF Registry UUID
          UUID key = resource.getKey();
          // has the Registry UUID been added as an alternative identifier yet? If not, add it!
          if (key != null && !ids.contains(key.toString().toLowerCase())) {
            currentIds.add(key.toString());
            // save all changes to Eml
            saveEml(resource);
            if (cfg.debug()) {
              LOG.info("GBIF Registry UUID added to Resource's list of alternate identifiers");
            }
          }
        }
      }
    } else {
      resource.setEml(new Eml());
    }

    return resource;
  }

  @Override
  public Resource updateAlternateIdentifierForIPTURLToResource(Resource resource) {
    // retrieve a list of the resource's alternate identifiers
    List<String> ids = null;
    if (resource.getEml() != null) {
      ids = resource.getEml().getAlternateIdentifiers();
    } else {
      resource.setEml(new Eml());
    }

    if (ids != null) {
      // has this been added before, perhaps with a different baseURL?
      boolean exists = false;
      String existingId = null;
      for (String id : ids) {
        // try to match "resource"
        if (id.contains(Constants.REQ_PATH_RESOURCE)) {
          exists = true;
          existingId = id;
        }
      }
      // if the resource is PUBLIC, or REGISTERED
      if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) != 0) {
        String url = cfg.getResourceUrl(resource.getShortname());
        // if identifier does not exist yet - add it!
        // if it already exists, then replace it just in case the baseURL has changed, for example
        if (exists) {
          ids.remove(existingId);
        }
        // lastly, be sure to add it
        ids.add(url);
        // save all changes to Eml
        saveEml(resource);
        if (cfg.debug()) {
          LOG.info("IPT URL to resource added to (or updated in) Resource's list of alt ids");
        }
      }
      // otherwise if the resource is PRIVATE
      else if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) == 0) {
        // no public resource alternate identifier can exist if the resource visibility is private - remove it if app.
        if (exists) {
          ids.remove(existingId);
          // save all changes to Eml
          saveEml(resource);
          if (cfg.debug()) {
            LOG.info("Following visibility change, IPT URL to resource was removed from Resource's list of alt ids");
          }
        }
      }
    }
    return resource;
  }

  private void publishMetadata(Resource resource, BigDecimal version, BaseAction action) throws PublicationException {
    if (resource.isDataPackage()) {
      publishDataPackageMetadata(resource, version);
    } else {
      publishEml(resource, version, action);
    }
  }

  /**
   * Publishes a new version of the EML file for the given resource.
   *
   * @param resource Resource
   * @param version  version number to publish
   *
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishEml(Resource resource, BigDecimal version, BaseAction action) throws PublicationException {
    String shortname = resource.getShortname();

    // check if publishing task is already running
    if (isLocked(shortname)) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + shortname + " is currently locked by another process");
    }

    if (resource.isMetadataOnly()) {
      StatusReport report = new StatusReport("Started publishing EML #" + version, new ArrayList<>());
      processReports.put(shortname, report);
      getTaskMessages(shortname).add(
          new TaskMessage(Level.INFO, "EML generation started for version #" + version));
    }

    // ensure alternate identifier for Registry UUID is set - if resource is registered
    updateAlternateIdentifierForRegistry(resource);
    // ensure alternate identifier for IPT URL to resource is set - if resource is public
    updateAlternateIdentifierForIPTURLToResource(resource);
    // update eml version
    resource.setMetadataVersion(version);
    // update eml pubDate (represents date when the resource was last published)
    resource.getEml().setPubDate(new Date());
    // set eml dateStamp (represents date when the resource was published for the first time). Do only once
    if (resource.getEml().getDateStamp() == null) {
      resource.getEml().setDateStamp(new Date());
    }
    // update resource citation with auto generated citation (if auto-generation has been turned on)
    if (resource.isCitationAutoGenerated()) {
      URI homepage = cfg.getResourceVersionUri(shortname, version); // potential citation identifier
      String citation = resource.generateResourceCitation(version, homepage);
      if (resource.getEml().getCitation() != null) {
        resource.getEml().getCitation().setCitation(citation);
      } else {
        Citation c = new Citation();
        c.setCitation(citation);
        resource.getEml().setCitation(c);
      }
    }
    // update eml with inferred data (if infer automatically is turned on)
    if (resource.isInferGeocoverageAutomatically()
        || resource.isInferTaxonomicCoverageAutomatically()
        || resource.isInferTemporalCoverageAutomatically()) {
      InferredEmlMetadata inferredMetadata = (InferredEmlMetadata) resourceMetadataInferringService.inferMetadata(resource);
      // save inferred metadata
      resource.setInferredMetadata(inferredMetadata);
      saveInferredMetadata(resource);

      if (resource.isInferGeocoverageAutomatically()) {
        updateEmlGeocoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTaxonomicCoverageAutomatically()) {
        updateEmlTaxonomicCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTemporalCoverageAutomatically()) {
        updateEmlTemporalCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }
    }

    // save all changes to Eml
    saveEml(resource);

    // create versioned eml file
    File trunkFile = dataDir.resourceEmlFile(shortname);

    // validate EML (only for metadata-only resources, otherwise it will be validated afterward)
    if (METADATA.toString().equalsIgnoreCase(resource.getCoreType())) {
      try {
        EmlValidator emlValidator = org.gbif.metadata.eml.EmlValidator.newValidator(EMLProfileVersion.GBIF_1_3);
        String emlString = FileUtils.readFileToString(trunkFile, StandardCharsets.UTF_8);

        getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "? Validating EML file"));
        emlValidator.validate(emlString);
        getTaskMessages(shortname).add(new TaskMessage(Level.INFO, "✓ Validated EML file"));
        StatusReport report = new StatusReport(
            true,
            action.getText("publishing.success", new String[] {version.toPlainString(), shortname}),
            getTaskMessages(shortname));
        processReports.put(shortname, report);
      } catch (IOException | SAXException e) {
        getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, "Failed to validate EML"));
        PublicationException exception = new PublicationException(PublicationException.TYPE.EML,
            "Can't publish eml file for resource " + shortname + ". Failed to validate EML", e);
        StatusReport errorReport = new StatusReport(
            exception,
            action.getText("publishing.failed", new String[] {version.toPlainString(), shortname, "Failed to validate EML"}),
            getTaskMessages(shortname));
        processReports.put(shortname, errorReport);
        throw exception;
      } catch (InvalidEmlException e) {
        getTaskMessages(shortname).add(new TaskMessage(Level.ERROR, "Invalid EML:  " + e.getMessage()));
        PublicationException exception = new PublicationException(PublicationException.TYPE.EML,
            "Can't publish eml file for resource " + resource.getShortname() + ". Invalid EML", e);
        StatusReport errorReport = new StatusReport(
            exception,
            action.getText("publishing.failed", new String[] {version.toPlainString(), shortname, "Invalid EML"}),
            getTaskMessages(shortname));
        processReports.put(shortname, errorReport);
        throw exception;
      }
    }

    File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);

    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
        "Can't publish eml file for resource " + resource.getShortname(), e);
    }
  }

  public void publishDataPackageMetadata(Resource resource, BigDecimal version) {
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
          "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    // update metadata version
    resource.setMetadataVersion(version);
    if (resource.getDataPackageMetadata() instanceof FrictionlessMetadata) {
      FrictionlessMetadata frictionlessMetadata = (FrictionlessMetadata) resource.getDataPackageMetadata();
      frictionlessMetadata.setCreated(new Date());
    }

    // update metadata created (represents date when the resource was last published)
    resource.getDataPackageMetadata().setVersion(version.toPlainString());

    // update metadata with inferred data (if infer automatically is turned on)
    if (CAMTRAP_DP.equals(resource.getCoreType())
        && (resource.isInferGeocoverageAutomatically()
        || resource.isInferTaxonomicCoverageAutomatically()
        || resource.isInferTemporalCoverageAutomatically())) {
      InferredCamtrapMetadata inferredMetadata = (InferredCamtrapMetadata) resourceMetadataInferringService.inferMetadata(resource);
      // save inferred metadata
      resource.setInferredMetadata(inferredMetadata);
      saveInferredMetadata(resource);

      if (resource.isInferGeocoverageAutomatically()) {
        updateCamtrapGeographicScopeWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTaxonomicCoverageAutomatically()) {
        updateCamtrapTaxonomicScopeWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTemporalCoverageAutomatically()) {
        updateCamtrapTemporalScopeWithInferredFromSourceData(resource, inferredMetadata);
      }
    }

    // save all changes to metadata
    saveDatapackageMetadata(resource);

    // create versioned metadata file
    File trunkFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType());
    File versionedFile = dataDir.resourceDatapackageMetadataFile(resource.getShortname(), resource.getCoreType(), version);
    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
          "Can't publish metadata file for resource " + resource.getShortname(), e);
    }
  }

  private void updateEmlGeocoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredGeographicCoverage() != null
        && inferredMetadata.getInferredGeographicCoverage().getData() != null) {
      GeospatialCoverage inferredGeocoverage = inferredMetadata.getInferredGeographicCoverage().getData();

      // check object to preserve description
      if (!resource.getEml().getGeospatialCoverages().isEmpty()) {
        inferredGeocoverage.setDescription(resource.getEml().getGeospatialCoverages().get(0).getDescription());
      } else {
        inferredGeocoverage.setDescription("N/A");
      }
      resource.getEml().getGeospatialCoverages().clear();
      resource.getEml().addGeospatialCoverage(inferredGeocoverage);
    }
  }

  private void updateCamtrapGeographicScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredGeographicScope() != null
        && inferredMetadata.getInferredGeographicScope().isInferred()) {

      Geojson geojson = new Geojson();
      geojson.setType(Geojson.Type.POLYGON);
      List<List<List<Double>>> coordinates = new ArrayList<>();
      InferredCamtrapGeographicScope inferredScope = inferredMetadata.getInferredGeographicScope();

      coordinates.add(
          Arrays.asList(
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMinLatitude()),
              Arrays.asList(inferredScope.getMaxLongitude(), inferredScope.getMinLatitude()),
              Arrays.asList(inferredScope.getMaxLongitude(), inferredScope.getMaxLatitude()),
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMaxLatitude()),
              Arrays.asList(inferredScope.getMinLongitude(), inferredScope.getMinLatitude())
          )
      );

      geojson.setCoordinates(coordinates);

      ((CamtrapMetadata) resource.getDataPackageMetadata()).setSpatial(geojson);
    }
  }

  private void updateEmlTaxonomicCoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredTaxonomicCoverage() != null
        && inferredMetadata.getInferredTaxonomicCoverage().getData() != null) {
      TaxonomicCoverage inferredTaxonomicCoverage = inferredMetadata.getInferredTaxonomicCoverage().getData();

      // check object to preserve description
      if (!resource.getEml().getTaxonomicCoverages().isEmpty()) {
        inferredTaxonomicCoverage.setDescription(resource.getEml().getTaxonomicCoverages().get(0).getDescription());
      } else {
        inferredTaxonomicCoverage.setDescription("N/A");
      }
      resource.getEml().getTaxonomicCoverages().clear();
      resource.getEml().addTaxonomicCoverage(inferredTaxonomicCoverage);
    }
  }

  private void updateCamtrapTaxonomicScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredTaxonomicScope() != null
        && inferredMetadata.getInferredTaxonomicScope().isInferred()) {

      InferredCamtrapTaxonomicScope inferredTaxonomicScope = inferredMetadata.getInferredTaxonomicScope();
      ((CamtrapMetadata) resource.getDataPackageMetadata()).setTaxonomic(inferredTaxonomicScope.getData());
    }
  }

  private void updateEmlTemporalCoverageWithInferredFromSourceData(Resource resource, InferredEmlMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredTemporalCoverage() != null
        && inferredMetadata.getInferredTemporalCoverage().getData() != null) {
      TemporalCoverage inferredTemporalCoverage = inferredMetadata.getInferredTemporalCoverage().getData();
      resource.getEml().getTemporalCoverages().clear();
      resource.getEml().addTemporalCoverage(inferredTemporalCoverage);
    }
  }

  private void updateCamtrapTemporalScopeWithInferredFromSourceData(Resource resource, InferredCamtrapMetadata inferredMetadata) {
    if (!resource.getDataPackageMappings().isEmpty()
        && inferredMetadata.getInferredTemporalScope() != null
        && inferredMetadata.getInferredTemporalScope().isInferred()) {

      InferredCamtrapTemporalScope inferredTemporalScope = inferredMetadata.getInferredTemporalScope();

      Temporal temporal = new Temporal();
      temporal.setStart(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getStartDate()));
      temporal.setEnd(CAMTRAP_TEMPORAL_METADATA_DATE_FORMAT.format(inferredTemporalScope.getEndDate()));

      ((CamtrapMetadata) resource.getDataPackageMetadata()).setTemporal(temporal);
    }
  }

  /**
   * Publishes a new version of the RTF file for the given resource.
   *
   * @param resource Resource
   * @param version  version number to publish
   *
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishRtf(Resource resource, BigDecimal version) throws PublicationException {
    // Skip RTF for data packages
    if (resource.isDataPackage()) {
      return;
    }

    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    Document doc = new Document();
    File rtfFile = dataDir.resourceRtfFile(resource.getShortname(), version);
    OutputStream out = null;
    try {
      out = new FileOutputStream(rtfFile);
      RtfWriter2.getInstance(doc, out);
      eml2Rtf.writeEmlIntoRtf(doc, resource);
    } catch (FileNotFoundException e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
        "Can't find rtf file to write metadata to: " + rtfFile.getAbsolutePath(), e);
    } catch (DocumentException e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
        "RTF DocumentException while writing to file: " + rtfFile.getAbsolutePath(), e);
    } catch (Exception e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
        "An unexpected error occurred while writing RTF file: " + e.getMessage(), e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          LOG.warn("FileOutputStream to RTF file could not be closed");
        }
      }
    }
  }

  /**
   * Try to read metadata file for a DwC-Archive.
   *
   * @param shortname resource shortname
   * @param archive   archive
   * @param alog      ActionLogger
   *
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
      LOG.warn("Cant read basic archive metadata: " + e.getMessage());
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
      String msg = "Cant read data package metadata: " + e.getMessage();
      LOG.warn(msg);
      alog.warn(msg);
      return null;
    } catch (Exception e) {
      LOG.warn("Cant read data package metadata", e);
    }

    alog.warn("manage.resource.read.problem");
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(Resource resource, Organisation organisation, Ipt ipt, BaseAction action)
    throws RegistryException {
    ActionLogger alog = new ActionLogger(this.LOG, action);

    if (PublicationStatus.REGISTERED != resource.getStatus() && PublicationStatus.PUBLIC == resource.getStatus()) {

      // Check: is there a chance this resource is meant to update an existing registered resource?
      // Populate set of UUIDs from eml.alternateIdentifiers that could represent existing registered resource UUIDs
      Set<UUID> candidateResourceUUIDs = collectCandidateResourceUUIDsFromAlternateIds(resource);

      // there can be max 1 candidate UUID. This safeguards against migration errors
      if (candidateResourceUUIDs.size() > 1) {
        String reason =
          action.getText("manage.resource.migrate.failed.multipleUUIDs", new String[] {organisation.getName()});
        String help = action.getText("manage.resource.migrate.failed.help");
        throw new InvalidConfigException(TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help);
      }
      // resource migration can happen if a single UUID corresponding to the resource UUID of an existing registered
      // resource owned by the specified organization has been found in the resource's alternate ids
      else if (candidateResourceUUIDs.size() == 1) {

        // there cannot be any public res with the same alternate identifier UUID, or registered res with the same UUID
        UUID candidate = candidateResourceUUIDs.iterator().next();
        List<String> duplicateUses = detectDuplicateUsesOfUUID(candidate, resource.getShortname());
        if (duplicateUses.isEmpty()) {
          if (organisation.getKey() != null && organisation.getName() != null) {
            // check in the registry resource with the provided key has this publishing organisation
            boolean matched =
                registryManager.isResourceBelongsToOrganisation(candidate.toString(), organisation.getKey().toString());

            if (matched) {
              LOG.debug("Resource matched to existing registered resource, UUID=" + organisation.getKey());

              // fill in registration info - we've found the original resource being migrated to the IPT
              resource.setStatus(PublicationStatus.REGISTERED);
              resource.setKey(candidate);
              resource.setOrganisation(organisation);

              // display update about migration to user
              alog.info("manage.resource.migrate", new String[] {organisation.getKey().toString(), organisation.getName()});

              // update the resource, adding the new service(s)
              updateRegistration(resource, action);
            }
            // if no match was ever found, this is considered a failed resource migration
            else {
              String reason =
                action.getText("manage.resource.migrate.failed.badUUID", new String[] {organisation.getName()});
              String help = action.getText("manage.resource.migrate.failed.help");
              throw new InvalidConfigException(TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help);
            }
          }
        } else {
          String reason = action.getText("manage.resource.migrate.failed.duplicate",
            new String[] {candidate.toString(), duplicateUses.toString()});
          String help1 = action.getText("manage.resource.migrate.failed.help");
          String help2 = action.getText("manage.resource.migrate.failed.duplicate.help");
          throw new InvalidConfigException(TYPE.INVALID_RESOURCE_MIGRATION, reason + " " + help1 + " " + help2);
        }
      } else {
        UUID key = registryManager.register(resource, organisation, ipt);
        if (key == null) {
          throw new RegistryException(RegistryException.Type.MISSING_METADATA, null,
            "No key returned for registered resource");
        }
        // display success to user
        alog.info("manage.overview.resource.registered", new String[] {organisation.getName()});

        // change status to registered
        resource.setStatus(PublicationStatus.REGISTERED);

        // ensure alternate identifier for Registry UUID set
        updateAlternateIdentifierForRegistry(resource);

        // update stored resources
        updateStoredResources(resource);
      }
      // save all changes to resource
      save(resource);
    } else {
      LOG.error("Registration request failed: the resource must be public. Status=" + resource.getStatus().toString());
    }
  }

  /**
   * Change resource status to REGISTERED and update organization.
   */
  private void updateStoredResources(Resource resource) {
    SimplifiedResource simplifiedResource = publishedPublicVersionsSimplified.get(resource.getShortname());
    if (simplifiedResource != null) {
      simplifiedResource.setStatus(PublicationStatus.REGISTERED);
      simplifiedResource.setOrganisationAlias(resource.getOrganisationAlias());
      simplifiedResource.setOrganisationName(resource.getOrganisationName());
    }
  }

  /**
   * For a candidate UUID, find out:
   * -how many public resources have a matching alternate identifier UUID
   * -how many registered resources have the same UUID
   *
   * @param candidate UUID
   * @param shortname shortname of resource to exclude from matching
   *
   * @return list of names of resources that have matched candidate UUID
   */
  protected List<String> detectDuplicateUsesOfUUID(UUID candidate, String shortname) {
    ListValuedMap<UUID, String> duplicateUses = new ArrayListValuedHashMap<>();
    for (Resource other : resources.values()) {
      // only resources having a different shortname should be matched against
      if (!other.getShortname().equalsIgnoreCase(shortname)) {
        // are there public resources with this alternate identifier?
        if (other.getStatus().equals(PublicationStatus.PUBLIC)) {
          Set<UUID> otherCandidateUUIDs = collectCandidateResourceUUIDsFromAlternateIds(other);
          if (!otherCandidateUUIDs.isEmpty()) {
            for (UUID otherCandidate : otherCandidateUUIDs) {
              if (otherCandidate.equals(candidate)) {
                duplicateUses.put(candidate, other.getTitleAndShortname());
              }
            }
          }
        }
        // are there registered resources with this UUID?
        else if (other.getStatus().equals(PublicationStatus.REGISTERED)) {
          if (other.getKey().equals(candidate)) {
            duplicateUses.put(candidate, other.getTitleAndShortname());
          }
        }
      }
    }
    return duplicateUses.get(candidate);
  }

  /**
   * Collect a set of UUIDs from the resource's list of alternate identifiers that could qualify as GBIF Registry
   * Dataset UUIDs.
   *
   * @param resource resource
   *
   * @return set of UUIDs that could qualify as GBIF Registry Dataset UUIDs
   */
  private Set<UUID> collectCandidateResourceUUIDsFromAlternateIds(Resource resource) {
    Set<UUID> ls = new HashSet<>();
    if (resource.getEml() != null && !resource.isDataPackage()) {
      List<String> ids = resource.getEml().getAlternateIdentifiers();
      for (String id : ids) {
        try {
          UUID uuid = UUID.fromString(id);
          ls.add(uuid);
        } catch (IllegalArgumentException e) {
          // skip, isn't a candidate UUID
        }
      }
    } else if (resource.getDataPackageMetadata() != null
        && resource.getDataPackageMetadata() instanceof CamtrapMetadata
        && CAMTRAP_DP.equals(resource.getCoreType())) {
      CamtrapMetadata metadata = (CamtrapMetadata) resource.getDataPackageMetadata();
      List<RelatedIdentifier> relatedIdentifiers = metadata.getRelatedIdentifiers();
      for (RelatedIdentifier identifier : relatedIdentifiers) {
        if (identifier != null && identifier.getRelatedIdentifier() != null
            && identifier.getRelatedIdentifier().contains("gbif")
            && identifier.getRelatedIdentifierType() == RelatedIdentifier.RelatedIdentifierType.URL) {
          String[] urlParts = identifier.getRelatedIdentifier().split("/");
          if (urlParts.length > 0) {
            String lastSegment = urlParts[urlParts.length - 1];
            try {
              UUID uuid = UUID.fromString(lastSegment);
              ls.add(uuid);
            } catch (IllegalArgumentException e) {
              // skip, isn't a candidate UUID
            }
          }
        }
      }
    }
    return ls;
  }

  @Override
  public synchronized void report(String shortname, StatusReport report) {
    processReports.put(shortname, report);
  }

  /**
   * Construct or update the VersionHistory for version v of resource, and make sure that it is added to the resource's
   * VersionHistory List.
   *
   * @param resource resource published
   * @param version  version of resource published
   * @param published true if this version has been published successfully, false otherwise
   * @param action   action
   */
  protected synchronized void addOrUpdateVersionHistory(Resource resource, BigDecimal version, boolean published,
    BaseAction action) {
    LOG.info("Adding or updating version: " + version.toPlainString());

    VersionHistory versionHistory;
    // Construct new VersionHistory, or update existing one if it exists
    VersionHistory existingVersionHistory = resource.findVersionHistory(version);
    if (existingVersionHistory == null) {
      versionHistory = new VersionHistory(version, resource.getStatus());
      resource.addVersionHistory(versionHistory);
      LOG.info("Adding VersionHistory for version " + version.toPlainString());
    } else {
      versionHistory = existingVersionHistory;
      LOG.info("Updating VersionHistory for version " + version.toPlainString());
    }

    // DOI
    versionHistory.setDoi(resource.getDoi());
    // DOI status
    versionHistory.setStatus(resource.getIdentifierStatus());
    // change summary
    versionHistory.setChangeSummary(resource.getChangeSummary());
    // core records published
    versionHistory.setRecordsPublished(resource.getRecordsPublished());
    // record published by extension
    versionHistory.setRecordsByExtension(resource.getRecordsByExtension());
    // modifiedBy
    User modifiedBy = action.getCurrentUser();
    if (modifiedBy != null) {
      versionHistory.setModifiedBy(modifiedBy);
    }
    // released - only set when version was published successfully
    if (published) {
      versionHistory.setReleased(new Date());
    }
  }

  public synchronized void cleanArchiveVersions(Resource resource) {
    if (cfg.isArchivalMode() && cfg.getArchivalLimit() != null && cfg.getArchivalLimit() > 0) {
      LOG.info("Archival mode is ON with a limit of {} elements)", cfg.getArchivalLimit());
      LOG.info("Clean archive versions, if needed, for resource: {}", resource.getShortname());
      List<VersionHistory> history = resource.getVersionHistory();
      if (history.size() > cfg.getArchivalLimit()) {
        for (int i = cfg.getArchivalLimit(); i < history.size(); i++) {
          VersionHistory oldVersion = history.get(i);
          try {
            BigDecimal version = new BigDecimal(oldVersion.getVersion());
            LOG.info("Deleting archive version {} for resource: {}", version, resource.getShortname());
            removeArchiveVersion(resource.getShortname(), version);
          } catch (Exception e) {
            LOG.error("Cannot delete old archive versions for resource: " + resource.getShortname(), e);
            return;
          }
        }
      }
    }
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
    LOG.debug("Updated EML file for " + resource);
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

    LOG.debug("Updated metadata file for " + resource);
  }

  @Override
  public StatusReport status(String shortname) {
    isLocked(shortname);
    return processReports.get(shortname);
  }

  /**
   * Updates the EML version and EML GUID. The GUID is set to the Registry UUID if the resource is
   * registered, otherwise it is set to the resource URL.
   * </br>
   * This method also updates the EML list of KeywordSet with the dataset type and subtype.
   * </br>
   * This method must be called before persisting the EML file to ensure that the EML file and resource are in sync.
   *
   * @param resource Resource
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
    if (!preserveKeywords) {
      // add/update KeywordSet for dataset type and subtype
      updateKeywordsWithDatasetTypeAndSubtype(resource);
    }
  }

  private void syncEmlWithResource(Resource resource) {
    syncEmlWithResource(resource, false);
  }

  @Override
  public void updateRegistration(Resource resource, BaseAction action) throws PublicationException {
    if (resource.isRegistered()) {
      // prevent null action from being handled
      if (action == null) {
        action = new BaseAction(textProvider, cfg, registrationManager);
      }
      try {
        LOG.debug("Updating registration of resource with key: " + resource.getKey().toString());

        // get IPT key
        String iptKey = null;
        if (registrationManager.getIpt() != null) {
          iptKey =
            (registrationManager.getIpt().getKey() == null) ? null : registrationManager.getIpt().getKey().toString();
        }

        // perform update
        registryManager.updateResource(resource, iptKey);
      } catch (RegistryException e) {
        // log as specific error message as possible about why the Registry error occurred
        String msg = RegistryException.logRegistryException(e, action);
        action.addActionError(msg);
        LOG.error(msg);
        // add error message that explains the root cause of the Registry error to user
        msg = action.getText("admin.config.updateMetadata.resource.fail.registry", new String[]{e.getMessage()});
        action.addActionError(msg);
        LOG.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      } catch (InvalidConfigException e) {
        String msg = action.getText("manage.overview.failed.resource.update", new String[] {e.getMessage()});
        action.addActionError(msg);
        LOG.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      }
    }
  }

  @Override
  public void visibilityToPrivate(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      // update visibility to private
      resource.setStatus(PublicationStatus.PRIVATE);

      // Changing the visibility means some public alternateIds need to be removed, e.g. IPT URL
      // not applicable for data packages
      if (resource.getDataPackageIdentifier() == null) {
        updateAlternateIdentifierForIPTURLToResource(resource);
      }

      // save all changes to resource
      save(resource);
    }
  }

  @Override
  public void visibilityToPublic(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      // update visibility to public
      resource.setStatus(PublicationStatus.PUBLIC);

      // erase make public date
      resource.setMakePublicDate(null);

      // Changing the visibility means some public alternateIds need to be added, e.g. IPT URL
      // not applicable for data packages
      if (resource.getDataPackageIdentifier() == null) {
        updateAlternateIdentifierForIPTURLToResource(resource);
      }

      // save all changes to resource
      save(resource);
    }
  }

  /**
   * Return a resource's StatusReport's list of TaskMessage. If no report exists for the resource, return an empty
   * list of TaskMessage.
   *
   * @param shortname resource shortname
   *
   * @return resource's StatusReport's list of TaskMessage or an empty list if no StatusReport exists for resource
   */
  private List<TaskMessage> getTaskMessages(String shortname) {
    return processReports.get(shortname) == null ? new ArrayList<>()
      : processReports.get(shortname).getMessages();
  }

  @Override
  public void updatePublicationMode(Resource resource) {
    if (resource.usesAutoPublishing()) {
      updateNextPublishedDate(new Date(), resource);
    }
    else {
      resource.setNextPublished(null);
    }
  }

  /**
   * Updates the date the resource is scheduled to be published next. The resource must have been configured with
   * a maintenance update frequency that is suitable for auto-publishing (annually, biannually, monthly, weekly,
   * daily), and have auto-publishing mode turned on for this update to take place.
   *
   * @param resource resource
   *
   * @throws PublicationException if the next published date cannot be set for any reason
   */
  protected void updateNextPublishedDate(Date currentDate, Resource resource) throws PublicationException {
    if (resource.usesAutoPublishing()) {
      try {
        LOG.debug("Updating next published date of resource: {}", resource.getShortname());

        Date nextPublished = null;

        // get update period
        MaintenanceUpdateFrequency frequency = resource.getUpdateFrequency();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        // Using the old auto publish configuration
        if (resource.isDeprecatedAutoPublishingConfiguration()) {
          // use predefined period for previous IPT version
          int days = frequency.getPeriodInDays();
          cal.add(Calendar.DATE, days);
          nextPublished = cal.getTime();
        }
        // Using the new auto publish configuration
        else {
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
          switch (frequency) {
            case ANNUALLY:
              cal.set(Calendar.MONTH, resource.getUpdateFrequencyMonth().getMonthId());
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            case BIANNUALLY:
              cal.set(Calendar.MONTH, resource.getUpdateFrequencyBiMonth().getBiMonthId());
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.MONTH, 6);
                nextPublished = cal.getTime();
                if (nextPublished.before(currentDate)) {
                  cal.add(Calendar.MONTH, 6);
                  nextPublished = cal.getTime();
                }
              }
              break;
            case MONTHLY:
              cal.set(Calendar.DAY_OF_MONTH, resource.getUpdateFrequencyDay());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.MONTH, 1);
                nextPublished = cal.getTime();
              }
              break;
            case WEEKLY:
              cal.set(Calendar.DAY_OF_WEEK, resource.getUpdateFrequencyDayOfWeek().getDayId());
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            case DAILY:
              cal.set(Calendar.HOUR_OF_DAY, resource.getUpdateFrequencyHour());
              cal.set(Calendar.MINUTE, resource.getUpdateFrequencyMinute());
              nextPublished = cal.getTime();
              if (nextPublished.before(currentDate)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                nextPublished = cal.getTime();
              }
              break;
            default:
              // Do not process others
              break;
          }
        }

        // alert user that auto publishing has been turned on
        if (resource.getNextPublished() == null) {
          LOG.debug("Auto-publishing turned on");
        }

        if (nextPublished == null) {
          String msg = "Error to compute the next publication date";
          LOG.error(msg);
          throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg);
        }

        // set next published date
        resource.setNextPublished(nextPublished);

        // log
        LOG.debug("The next publication date is: {}", nextPublished);
      } catch (Exception e) {
        resource.setNextPublished(null);
        // add error message that explains the consequence of the error to user
        String msg = "Auto-publishing failed: " + e.getMessage();
        LOG.error(msg, e);
        throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg, e);
      }
    } else {
      resource.setNextPublished(null);
      LOG.debug("Resource: {} has not been configured to use auto-publishing", resource.getShortname());
    }
  }

  /**
   * Try to add/update/remove KeywordSet for dataset type and subtype.
   *
   * @param resource resource
   *
   * @return resource whose Eml list of KeywordSet has been updated depending on presence of dataset type or subtype
   */
  protected Resource updateKeywordsWithDatasetTypeAndSubtype(Resource resource) {
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

  @Override
  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  @Override
  public Map<String, Future<Map<String, Integer>>> getProcessFutures() {
    return processFutures;
  }

  @Override
  public ListValuedMap<String, Date> getProcessFailures() {
    return processFailures;
  }

  @Override
  public Map<String, StatusReport> getProcessReports() {
    return processReports;
  }

  @Override
  public void clearProcessReports() {
    processReports.clear();
  }

  @Override
  public boolean hasMaxProcessFailures(Resource resource) {
    String resourceShortname = resource.getShortname();

    if (processFailures.containsKey(resourceShortname)) {
      List<Date> failures = processFailures.get(resourceShortname);
      int count = failures.size();

      LocalDate today = LocalDate.now();
      LocalDate last = lastLoggedFailures.get(resourceShortname);

      if (count < MAX_PROCESS_FAILURES) { // always log if count is below max
        LOG.debug("Publication has failed {} time(s) for resource: {}",
            count, resource.getTitleAndShortname());
      } else if (last == null || !last.equals(today)) { // once the limit is reached, only log once per day
        LOG.debug("Publication has failed {} time(s) for resource: {} (max amount of failures)",
            count, resource.getTitleAndShortname());
        lastLoggedFailures.put(resourceShortname, today);
      }

      return count >= MAX_PROCESS_FAILURES;
    }
    return false;
  }

  public GenerateDwcaFactory getDwcaFactory() {
    return dwcaFactory;
  }

  /**
   * Remove an archived version in the resource history and from the file system
   */
  @SuppressWarnings("BigDecimalEquals")
  @Override
  public void removeVersion(Resource resource, BigDecimal version) {
    // Cannot remove the most recent version, only archived versions
    if ((version != null) && !version.equals(resource.getMetadataVersion())) {
      LOG.debug("Removing version {} for resource: {}", version, resource.getShortname());
      try {
        removeVersionInternal(resource, version);
        resource.removeVersionHistory(version);
        save(resource);
        LOG.debug("Version {} has been removed for resource: {}", version, resource.getShortname());
      }
      catch(IOException e) {
        LOG.error("Cannot remove version {} for resource: {}", version, resource.getShortname(), e);
      }
    }
  }

  /**
   * Remove an archive version from the file system (because it has been replaced by a new published version for
   * example).
   *
   * @param version of archive to remove
   */
  public void removeArchiveVersion(String shortname, BigDecimal version) {
    File dwcaFile = dataDir.resourceDwcaFile(shortname, version);
    if (dwcaFile != null && dwcaFile.exists()) {
      boolean deleted = FileUtils.deleteQuietly(dwcaFile);
      if (deleted) {
        LOG.debug("{} has been successfully deleted.", dwcaFile.getAbsolutePath());
      }
    }

    File dpArchiveFile = dataDir.resourceDataPackageFile(shortname, version);
    if (dpArchiveFile != null && dpArchiveFile.exists()) {
      boolean deleted = FileUtils.deleteQuietly(dpArchiveFile);
      if (deleted) {
        LOG.debug("{} has been successfully deleted.", dpArchiveFile.getAbsolutePath());
      }
    }
  }

  public void removeVersionInternal(Resource resource, BigDecimal version) throws IOException {
    String shortname = resource.getShortname();

    // delete eml-*.xml if it exists (eml.xml must remain)
    File versionedEMLFile = dataDir.resourceEmlFile(shortname, version);
    if (versionedEMLFile.exists()) {
      FileUtils.forceDelete(versionedEMLFile);
    }

    // delete datapackage-*.json if it exists (datapackage.json must remain)
    File versionedDataPackageMetadataFile =
        dataDir.resourceDatapackageMetadataFile(shortname, resource.getCoreType(), version);
    if (versionedDataPackageMetadataFile.exists()) {
      FileUtils.forceDelete(versionedDataPackageMetadataFile);
    }

    // delete shortname-*.rtf if it exists
    File versionedRTFFile = dataDir.resourceRtfFile(shortname, version);
    if (versionedRTFFile.exists()) {
      FileUtils.forceDelete(versionedRTFFile);
    }

    // delete dwca-*.zip if it exists
    File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, version);
    if (versionedDwcaFile.exists()) {
      FileUtils.forceDelete(versionedDwcaFile);
    }

    // delete datapackage-*.zip if it exists
    File versionedDataPackageArchiveFile = dataDir.resourceDataPackageFile(shortname, version);
    if (versionedDataPackageArchiveFile.exists()) {
      FileUtils.forceDelete(versionedDataPackageArchiveFile);
    }
  }

  public String calculateChecksum(File file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    InputStream fis = new FileInputStream(file);

    byte[] byteArray = new byte[1024];
    int bytesCount = 0;

    while ((bytesCount = fis.read(byteArray)) != -1) {
      digest.update(byteArray, 0, bytesCount);
    };
    fis.close();

    byte[] bytes = digest.digest();

    // Convert to hex string
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }

  /**
   * Calculates a checksum of the DwC archive or the data package.
   *
   * @param archive archive
   * @return checksum of the archive
   */
  public String calculateArchiveChecksum(File archive) throws Exception {
    // Create a MessageDigest instance for SHA-256
    MessageDigest digest = MessageDigest.getInstance("SHA-256");

    try (ZipFile zipFile = new ZipFile(archive)) {
      // Iterate through the files in the DwCA
      zipFile.stream().forEach(entry -> {
        // Skip the EML metadata file
        if (entry.getName().endsWith(".xml") && entry.getName().toLowerCase().contains("eml")) {
          return;
        }

        // Skip the data package metadata file
        if (entry.getName().endsWith(".json") && entry.getName().toLowerCase().contains("datapackage")) {
          return;
        }

        // If it's a data file, calculate its checksum
        try (InputStream is = zipFile.getInputStream(entry)) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
          }
        } catch (IOException e) {
          LOG.error("Failed to read data", e);
        }
      });
    }

    // Convert the final checksum to a hex string
    byte[] hashBytes = digest.digest();
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      hexString.append(String.format("%02x", b));
    }
    return hexString.toString();
  }
}
