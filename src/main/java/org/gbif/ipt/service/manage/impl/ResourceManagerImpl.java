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
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.date.DateParsers;
import org.gbif.common.parsers.date.TemporalParser;
import org.gbif.common.parsers.geospatial.CoordinateParseUtils;
import org.gbif.common.parsers.geospatial.LatLng;
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
import org.gbif.ipt.action.portal.OrganizedTaxonomicCoverage;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.InferredGeographicCoverage;
import org.gbif.ipt.model.InferredTaxonomicCoverage;
import org.gbif.ipt.model.InferredTemporalCoverage;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.SimplifiedResource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.UrlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.UserEmailConverter;
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
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.GeneratorException;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.task.TaskMessage;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.ipt.utils.DataCiteMetadataBuilder;
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;
import org.gbif.metadata.eml.Point;
import org.gbif.metadata.eml.TaxonKeyword;
import org.gbif.metadata.eml.TaxonomicCoverage;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.registry.metadata.EMLProfileVersion;
import org.gbif.registry.metadata.EmlValidator;
import org.gbif.registry.metadata.InvalidEmlException;
import org.gbif.registry.metadata.parse.DatasetParser;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

import static org.gbif.ipt.config.Constants.CLASS;
import static org.gbif.ipt.config.Constants.FAMILY;
import static org.gbif.ipt.config.Constants.KINGDOM;
import static org.gbif.ipt.config.Constants.ORDER;
import static org.gbif.ipt.config.Constants.PHYLUM;
import static org.gbif.ipt.config.Constants.VOCAB_CLASS;
import static org.gbif.ipt.config.Constants.VOCAB_DECIMAL_LATITUDE;
import static org.gbif.ipt.config.Constants.VOCAB_DECIMAL_LONGITUDE;
import static org.gbif.ipt.config.Constants.VOCAB_EVENT_DATE;
import static org.gbif.ipt.config.Constants.VOCAB_FAMILY;
import static org.gbif.ipt.config.Constants.VOCAB_KINGDOM;
import static org.gbif.ipt.config.Constants.VOCAB_ORDER;
import static org.gbif.ipt.config.Constants.VOCAB_PHYLUM;

@Singleton
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
  private RegistryManager registryManager;
  private ThreadPoolExecutor executor;
  private GenerateDwcaFactory dwcaFactory;
  private Map<String, Future<Map<String, Integer>>> processFutures = new HashMap<>();
  private ListValuedMap<String, Date> processFailures = new ArrayListValuedHashMap<>();
  private Map<String, StatusReport> processReports = new HashMap<>();
  private Eml2Rtf eml2Rtf;
  private VocabulariesManager vocabManager;
  private SimpleTextProvider textProvider;
  private RegistrationManager registrationManager;

  private static final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);
  private static final Comparator<Date> nullSafeDateComparator = Comparator.nullsFirst(Date::compareTo);
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Inject
  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
                             OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter,
                             JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager,
                             ExtensionManager extensionManager,
                             RegistryManager registryManager, ConceptTermConverter conceptTermConverter,
                             GenerateDwcaFactory dwcaFactory,
                             PasswordEncrypter passwordEncrypter, Eml2Rtf eml2Rtf, VocabulariesManager vocabManager,
                             SimpleTextProvider textProvider, RegistrationManager registrationManager) {
    super(cfg, dataDir);
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.registryManager = registryManager;
    this.dwcaFactory = dwcaFactory;
    this.eml2Rtf = eml2Rtf;
    this.vocabManager = vocabManager;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cfg.getMaxThreads());
    defineXstreamMapping(userConverter, orgConverter, extensionConverter, conceptTermConverter, jdbcInfoConverter,
      passwordEncrypter);
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
    // add only public/registered resources with at least one published version
    if (!res.getVersionHistory().isEmpty()) {
      VersionHistory latestVersion = res.getVersionHistory().get(0);
      if (!latestVersion.getPublicationStatus().equals(PublicationStatus.DELETED) &&
          !latestVersion.getPublicationStatus().equals(PublicationStatus.PRIVATE) &&
          latestVersion.getReleased() != null) {
        publishedPublicVersionsSimplified.put(res.getShortname(), toSimplifiedResourceReconstructedVersion(res));
      }
    }
  }

  /**
   * Converts regular Resource to lightweight SimplifiedResource.
   * Reconstructs resource from last published EML to take data before it was changed.
   *
   * @param resource regular Resource
   * @return simplified resource
   */
  private SimplifiedResource toSimplifiedResourceReconstructedVersion(Resource resource) {
    BigDecimal v = resource.getLastPublishedVersionsVersion();
    String shortname = resource.getShortname();
    File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, v);
    Resource publishedPublicVersion = ResourceUtils
        .reconstructVersion(v, resource.getShortname(), resource.getCoreType(), resource.getAssignedDoi(), resource.getOrganisation(),
            resource.findVersionHistory(v), versionEmlFile, resource.getKey());

    SimplifiedResource result = new SimplifiedResource();
    result.setShortname(publishedPublicVersion.getShortname());
    result.setTitle(publishedPublicVersion.getTitle());
    result.setStatus(publishedPublicVersion.getStatus());
    result.setRecordsPublished(publishedPublicVersion.getRecordsPublished());
    result.setLogoUrl(publishedPublicVersion.getLogoUrl());
    result.setSubject(publishedPublicVersion.getSubject());
    if (publishedPublicVersion.getOrganisation() != null) {
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

    // TODO: 15/11/2022 this might be redundant, just update on registration
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
        // split description into paragraphs
        List<String> paragraphs = Arrays.stream(metadata.getDescription().split("\r?\n"))
            .map(org.gbif.utils.text.StringUtils::trim)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList());
        for (String para : paragraphs) {
          eml.addDescriptionPara(para);
        }
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
  private void validateEmlFile(File emlFile) throws SAXException, IOException, InvalidEmlException {
      EmlValidator emlValidator = EmlValidator.newValidator(EMLProfileVersion.GBIF_1_1);
      String emlString = FileUtils.readFileToString(emlFile, StandardCharsets.UTF_8);
      emlValidator.validate(emlString);
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
    } catch (IOException e1) {
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
  public Resource create(String shortname, String type, File dwca, User creator, BaseAction action)
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
    File dwcaDir = dataDir.tmpDir();
    try {
      decompressed = CompressionUtil.decompressFile(dwcaDir, dwca, true);
    } catch (UnsupportedCompressionType e) {
      LOG.debug("1st attempt to decompress file failed: " + e.getMessage(), e);
    } catch (Exception e) {
      LOG.debug("Decompression failed: " + e.getMessage(), e);
    }

    if (CollectionUtils.isEmpty(decompressed)) {
      // try again as single gzip file
      try {
        decompressed = CompressionUtil.ungzipFile(dwcaDir, dwca, false);
      } catch (Exception e2) {
        LOG.debug("2nd attempt to decompress file failed: " + e2.getMessage(), e2);
      }
    }

    // create resource:
    // if decompression failed, create resource from single eml file
    if (CollectionUtils.isEmpty(decompressed)) {
      resource = createFromEml(shortname, dwca, creator, alog);
    }
    // if decompression succeeded, create resource depending on whether file was 'IPT Resource Folder' or a 'DwC-A'
    else {
      resource = isIPTResourceFolder(dwcaDir) ? createFromIPTResourceFolder(shortname, dwcaDir, creator, alog)
        : createFromArchive(shortname, dwcaDir, creator, alog);
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
        res.setEmlVersion(Constants.INITIAL_RESOURCE_VERSION);
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
        res.setMetadataModified(null);
        res.setMappingsModified(null);
        res.setSourcesModified(null);
        // reset first and last published dates
        res.getEml().setDateStamp((Date) null);
        res.getEml().setPubDate(null);
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
   * at least a resource.xml and eml.xml file.
   *
   * @param dir directory where compressed file was decompressed
   *
   * @return true if it is an IPT Resource folder or false otherwise
   */
  private boolean isIPTResourceFolder(File dir) {
    if (dir.exists() && dir.isDirectory()) {
      File persistenceFile = new File(dir, DataDir.PERSISTENCE_FILENAME);
      File emlFile = new File(dir, DataDir.EML_XML_FILENAME);
      return persistenceFile.isFile() && emlFile.isFile();
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

  private Resource createFromArchive(String shortname, File dwca, User creator, ActionLogger alog)
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

      // read core source+mappings
      TextFileSource s = importSource(resource, arch.getCore());
      sources.put(arch.getCore().getLocation(), s);
      ExtensionMapping map = importMappings(alog, arch.getCore(), s);
      resource.addMapping(map);

      // if extensions are being used..
      // the core must contain an id element that indicates the identifier for a record
      if (!arch.getExtensions().isEmpty()) {
        if (map.getIdColumn() == null) {
          alog.error("manage.resource.create.core.invalid.id");
          throw new ImportException("Darwin Core Archive is invalid, core mapping has no id element");
        }

        // read extension sources+mappings
        for (ArchiveFile ext : arch.getExtensions()) {
          if (sources.containsKey(ext.getLocation())) {
            s = sources.get(ext.getLocation());
            LOG.debug("SourceBase " + s.getName() + " shared by multiple extensions");
          } else {
            s = importSource(resource, ext);
            sources.put(ext.getLocation(), s);
          }
          map = importMappings(alog, ext, s);
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
  public void replaceEml(Resource resource, File emlFile, boolean validate) throws SAXException, IOException, InvalidEmlException, ImportException {
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
    resource.setEml(eml);
    return resource;
  }

  private void defineXstreamMapping(UserEmailConverter userConverter, OrganisationKeyConverter orgConverter,
                                    ExtensionRowTypeConverter extensionConverter,
                                    ConceptTermConverter conceptTermConverter, JdbcInfoConverter jdbcInfoConverter,
                                    PasswordEncrypter passwordEncrypter) {
    xstream.addPermission(AnyTypePermission.ANY);
    xstream.ignoreUnknownElements();
    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);

    // aliases for inferred metadata
    xstream.alias("inferredMetadata", InferredMetadata.class);
    xstream.alias("inferredGeographicCoverage", InferredGeographicCoverage.class);
    xstream.alias("inferredTaxonomicCoverage", InferredTaxonomicCoverage.class);
    xstream.alias("inferredTemporalCoverage", InferredTemporalCoverage.class);
    xstream.alias("taxonKeyword", TaxonKeyword.class);
    xstream.alias("organizedTaxonomicKeywords", OrganizedTaxonomicKeywords.class);

    xstream.alias("filesource", TextFileSource.class);
    xstream.alias("excelsource", ExcelFileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("urlsource", UrlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", PropertyMapping.class);
    xstream.alias("versionhistory", VersionHistory.class);
    xstream.alias("doi", DOI.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "type");
    // inferred metadata in the separate file
    xstream.omitField(Resource.class, "inferredMetadata");
    // make files transient to allow moving the datadir
    xstream.omitField(TextFileSource.class, "file");

    // persist only emails for users
    xstream.registerConverter(userConverter);
    // persist only rowtype
    xstream.registerConverter(extensionConverter);
    // persist only qualified concept name
    xstream.registerConverter(conceptTermConverter);
    // encrypt passwords
    xstream.registerConverter(passwordEncrypter);

    xstream.addDefaultImplementation(ExtensionProperty.class, Term.class);
    xstream.registerConverter(orgConverter);
    xstream.registerConverter(jdbcInfoConverter);
  }

  @Override
  public void deleteResourceFromIpt(Resource resource) throws IOException {
    // remove from data dir
    FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
    // remove object
    resources.remove(resource.getShortname().toLowerCase());
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

  private TextFileSource importSource(Resource config, ArchiveFile af)
    throws ImportException, InvalidFilenameException {
    File extFile = af.getLocationFile();
    TextFileSource s = (TextFileSource) sourceManager.add(config, extFile, af.getLocation());
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

  @Override
  public boolean isEmlExisting(String shortName) {
    File emlFile = dataDir.resourceEmlFile(shortName);
    return emlFile.exists();
  }

  @Override
  public boolean isLocked(String shortname, BaseAction action) {
    if (processFutures.containsKey(shortname)) {
      Resource resource = get(shortname);
      BigDecimal version = resource.getEmlVersion();

      // is listed as locked but task might be finished, check
      Future<Map<String, Integer>> f = processFutures.get(shortname);
      // if this task finished
      if (f.isDone()) {
        // remove process from locking list immediately! Fixes Issue 1141
        processFutures.remove(shortname);
        boolean succeeded = false;
        String reasonFailed = null;
        Throwable cause = null;
        try {
          // store record counts by extension
          resource.setRecordsByExtension(f.get());
          // populate core record count
          Integer recordCount = resource.getRecordsByExtension().get(StringUtils.trimToEmpty(resource.getCoreRowType()));
          resource.setRecordsPublished(recordCount == null ? 0 : recordCount);
          // finish publication (update registration, persist resource changes)
          publishEnd(resource, action, version);
          // important: indicate publishing finished successfully!
          succeeded = true;
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
          // if publication was successful..
          if (succeeded) {
            // update StatusReport on publishing page
            String msg =
              action.getText("publishing.success", new String[] {version.toPlainString(), resource.getShortname()});
            StatusReport updated = new StatusReport(true, msg, getTaskMessages(shortname));
            processReports.put(shortname, updated);
          } else {
            // alert user publication failed
            String msg =
              action.getText("publishing.failed", new String[] {version.toPlainString(), shortname, reasonFailed});
            action.addActionError(msg);

            // update StatusReport on publishing page
            if (cause != null) {
              StatusReport updated = new StatusReport(new Exception(cause), msg, getTaskMessages(shortname));
              processReports.put(shortname, updated);
            }

            // the previous version needs to be rolled back
            restoreVersion(resource, version, action);

            // keep track of how many failures on auto publication have happened
            processFailures.put(resource.getShortname(), new Date());
          }
        }
        return false;
      }
      return true;
    }
    return false;
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

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(this::toDatatableResourcePortalView)
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
   * @return UI data (array)
   */
  private List<String> toDatatableResourcePortalView(SimplifiedResource resource) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceHomeLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType()));
    result.add(toTypeBadge(resource.getSubtype()));
    result.add(toUiRecordsPublished(resource));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus()));
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
   * @return UI data (array)
   */
  private List<String> toDatatableResourceManageView(SimplifiedResource resource) {
    List<String> result = new ArrayList<>();
    result.add(toUiLogoUrl(resource.getLogoUrl()));
    result.add(toResourceManageLink(resource));
    result.add(toUiOrganization(resource));
    result.add(toTypeBadge(resource.getCoreType()));
    result.add(toTypeBadge(resource.getSubtype()));
    result.add(toUiRecordsPublished(resource));
    result.add(toUiDateTime(resource.getModified()));
    result.add(toUiDateTime(resource.getLastPublished()));
    result.add(toUiNextPublished(resource.getNextPublished()));
    result.add(toUiStatus(resource.getStatus()));
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
    return "<img class=\"resourceminilogo\" src=\"" + logoUrl + "/>";
  }

  /**
   * Converts raw data to UI format.
   * Organization alias or name or "--" if empty
   *
   * @param resource lightweight resource
   * @return alias or name or "--"
   */
  private String toUiOrganization(SimplifiedResource resource) {
    String result = StringUtils.defaultString(resource.getOrganisationAlias(), resource.getOrganisationName());
    return !"No organization".equals(result) ? result : "--";
  }

  /**
   * Converts raw data to UI format.
   * Wraps number of published records into a link
   *
   * @param resource lightweight resource
   * @return link to records section
   */
  private String toUiRecordsPublished(SimplifiedResource resource) {
    return "<a href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "#anchor-dataRecords'>" + resource.getRecordsPublished() + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps core type or subtype into span to make it badge on UI.
   *
   * @param type core type or subtype
   * @return wrapped type (badge)
   */
  private String toTypeBadge(String type) {
    if (type == null) {
      return "<span>--</span>";
    }
    return "<span class=\"text-nowrap ct-content__link ct-content__pill coreType-" + type.toLowerCase() + "\">" + type.toLowerCase() + "</span>";
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
    return "<a href='" + cfg.getBaseUrl() + "/resource?r=" + resource.getShortname() + "'>" + resourceName + "</a>";
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
    return "<a href='" + cfg.getBaseUrl() + "/manage/resource?r=" + resource.getShortname() + "'>" + resourceName + "</a>";
  }

  /**
   * Converts raw data to UI format.
   * Wraps lower case status into span to make it badge on UI.
   *
   * @param status publication status
   * @return wrapped publication status (badge)
   */
  private String toUiStatus(PublicationStatus status) {
    return "<span class=\"text-nowrap ct-content__link ct-content__pill status-" + status.name().toLowerCase() + "\">" + status + "</span>";
  }

  @Override
  public DatatableResult list(User user, DatatableRequest request) {
    List<SimplifiedResource> filteredResources = resources.values().stream()
        .filter(res -> RequireManagerInterceptor.isAuthorized(user, res))
        .map(this::toSimplifiedResource)
        .filter(res -> matchesSearchString(res, request.getSearch()))
        .collect(Collectors.toList());

    List<List<String>> data = filteredResources.stream()
        .sorted(resourceComparator(request.getSortFieldIndex(), request.getSortOrder()))
        .skip(request.getOffset())
        .limit(request.getLimit())
        .map(this::toDatatableResourceManageView)
        .collect(Collectors.toList());

    DatatableResult result = new DatatableResult();
    result.setTotalRecords(publishedPublicVersionsSimplified.values().size());
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
   * Loads a resource's inferred metadata from the xml file located inside its resource directory.
   * If no inferredMetadata.xml file was found, the resource is loaded with an empty InferredMetadata instance.
   *
   * @param resource resource
   */
  private void loadInferredMetadata(Resource resource) {
    File inferredMetadataFile = dataDir.resourceInferredMetadataFile(resource.getShortname());
    if (!inferredMetadataFile.exists()) {
      resource.setInferredMetadata(new InferredMetadata());
      return;
    }

    try {
      InputStream input = new FileInputStream(inferredMetadataFile);
      InferredMetadata inferredMetadata = (InferredMetadata) xstream.fromXML(input);
      resource.setInferredMetadata(inferredMetadata);
    } catch (Exception e) {
      LOG.error("Cannot read inferred metadata file for resource " + resource.getShortname(), e);
      throw new InvalidConfigException(TYPE.RESOURCE_CONFIG,
          "Cannot read inferred metadata file for resource " + resource.getShortname() + ": " + e.getMessage());
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
        // therefore if a non-Taxon core extension is using auto-generated IDs, the coreID is set to No ID (-99)
        for (ExtensionMapping ext : resource.getMappings()) {
          Extension x = ext.getExtension();
          if (x == null) {
            alog.warn("manage.resource.create.extension.null");
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

        // shortname persists as folder name, so xstream doesnt handle this:
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

        // load eml (this must be done before trying to convert version below)
        loadEml(resource);

        // load inferred metadata
        loadInferredMetadata(resource);

        // pre v2.2 resources: convert resource version from integer to major_version.minor_version style
        // also convert/rename eml, rtf, and dwca versioned files also
        BigDecimal converted = convertVersion(resource);
        if (converted != null) {
          updateResourceVersion(resource, resource.getEmlVersion(), converted);
        }

        // pre v2.2 resources: construct a VersionHistory for last published version (if appropriate)
        VersionHistory history = constructVersionHistoryForLastPublishedVersion(resource);
        if (history != null) {
          resource.addVersionHistory(history);
        }

        // pre v2.2.1 resources: rename dwca.zip to dwca-18.0.zip (where 18.0 is the last published version for example)
        if (resource.getLastPublishedVersionsVersion() != null) {
          renameDwcaToIncludeVersion(resource, resource.getLastPublishedVersionsVersion());
        }

        // update EML with the latest resource basics (version and GUID)
        syncEmlWithResource(resource);

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
    if (resource.getEmlVersion() != null) {
      BigDecimal version = resource.getEmlVersion();
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
        resource.setEmlVersion(newVersion);
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
        new VersionHistory(resource.getEmlVersion(), resource.getLastPublished(), resource.getStatus());
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
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    // add new version history
    addOrUpdateVersionHistory(resource, version, false, action);

    // publish EML
    publishEml(resource, version);

    // publish RTF
    publishRtf(resource, version);

    // remove StatusReport from previous publishing round
    StatusReport report = status(resource.getShortname());
    if (report != null) {
      processReports.remove(resource.getShortname());
    }

    // (re)generate dwca asynchronously
    boolean dwca = false;
    if (resource.hasMappedData()) {
      generateDwca(resource);
      dwca = true;
    } else {
      // set number of records published
      resource.setRecordsPublished(0);
      // finish publication now
      publishEnd(resource, action, version);
    }
    return dwca;
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
    // update the resource's registration (if registered), even if it is a metadata-only resource.
    updateRegistration(resource, action);
    // set last published date
    resource.setLastPublished(new Date());
    // set next published date (if resource configured for auto-publishing)
    updateNextPublishedDate(new Date(), resource);
    // register/update DOI
    executeDoiWorkflow(resource, version, resource.getReplacedEmlVersion(), action);
    // finalise/update version history
    addOrUpdateVersionHistory(resource, version, true, action);
    // persist resource object changes
    save(resource);
    // if archival mode is NOT turned on, don't keep former archive version (version replaced)
    if (!cfg.isArchivalMode() && version.compareTo(resource.getReplacedEmlVersion()) != 0) {
      removeArchiveVersion(resource.getShortname(), resource.getReplacedEmlVersion());
    }
    // clean archive versions
    if (cfg.isArchivalMode() && cfg.getArchivalLimit() != null && cfg.getArchivalLimit() > 0) {
      cleanArchiveVersions(resource);
    }
    // final logging
    String msg = action
      .getText("publishing.success", new String[] {String.valueOf(resource.getEmlVersion()), resource.getShortname()});
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
        && resource.getEmlVersion() != null && resource.getEmlVersion().compareTo(version) == 0
        && replacedVersion != null && resource.findVersionHistory(replacedVersion) != null) {

      // register new DOI first, indicating it replaces former DOI
      doRegisterDoi(resource, doiToReplace);

      // update previously assigned DOI, indicating it has been replaced by new DOI
      try {
        // reconstruct last published version (version being replaced)
        File replacedVersionEmlFile = dataDir.resourceEmlFile(resource.getShortname(), replacedVersion);
        Resource lastPublishedVersion = ResourceUtils
          .reconstructVersion(replacedVersion, resource.getShortname(), resource.getCoreType(), doiToReplace, resource.getOrganisation(),
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
        resource.setEmlVersion(toRestore);

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

  /**
   * Publishes a new version of the EML file for the given resource.
   *
   * @param resource Resource
   * @param version  version number to publish
   *
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishEml(Resource resource, BigDecimal version) throws PublicationException {
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    // ensure alternate identifier for Registry UUID is set - if resource is registered
    updateAlternateIdentifierForRegistry(resource);
    // ensure alternate identifier for IPT URL to resource is set - if resource is public
    updateAlternateIdentifierForIPTURLToResource(resource);
    // update eml version
    resource.setEmlVersion(version);
    // update eml pubDate (represents date when the resource was last published)
    resource.getEml().setPubDate(new Date());
    // set eml dateStamp (represents date when the resource was published for the first time). Do only once
    if (resource.getEml().getDateStamp() == null) {
      resource.getEml().setDateStamp(new Date());
    }
    // update resource citation with auto generated citation (if auto-generation has been turned on)
    if (resource.isCitationAutoGenerated()) {
      URI homepage = cfg.getResourceVersionUri(resource.getShortname(), version); // potential citation identifier
      String citation = resource.generateResourceCitation(version, homepage);
      resource.getEml().getCitation().setCitation(citation);
    }
    // update eml with inferred data (if infer automatically is turned on)
    if (resource.isInferGeocoverageAutomatically()
        || resource.isInferTaxonomicCoverageAutomatically()
        || resource.isInferTemporalCoverageAutomatically()) {
      InferredMetadata inferredMetadata = inferMetadata(resource);
      // save inferred metadata
      resource.setInferredMetadata(inferredMetadata);
      saveInferredMetadata(resource);

      if (resource.isInferGeocoverageAutomatically()) {
        updateGeocoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTaxonomicCoverageAutomatically()) {
        updateTaxonomicCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }

      if (resource.isInferTemporalCoverageAutomatically()) {
        updateTemporalCoverageWithInferredFromSourceData(resource, inferredMetadata);
      }
    }

    // save all changes to Eml
    saveEml(resource);

    // create versioned eml file
    File trunkFile = dataDir.resourceEmlFile(resource.getShortname());
    File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);
    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
        "Can't publish eml file for resource " + resource.getShortname(), e);
    }
  }

  public List<OrganizedTaxonomicCoverage> constructOrganizedTaxonomicCoverages(List<TaxonomicCoverage> coverages) {
    List<OrganizedTaxonomicCoverage> organizedTaxonomicCoverages = new ArrayList<>();
    for (TaxonomicCoverage coverage : coverages) {
      OrganizedTaxonomicCoverage organizedCoverage = constructOrganizedTaxonomicCoverage(coverage);
      organizedTaxonomicCoverages.add(organizedCoverage);
    }
    return organizedTaxonomicCoverages;
  }

  public OrganizedTaxonomicCoverage constructOrganizedTaxonomicCoverage(TaxonomicCoverage coverage) {
    OrganizedTaxonomicCoverage organizedCoverage = new OrganizedTaxonomicCoverage();
    organizedCoverage.setDescription(coverage.getDescription());
    organizedCoverage.setKeywords(setOrganizedTaxonomicKeywords(coverage.getTaxonKeywords()));
    return organizedCoverage;
  }

  private List<OrganizedTaxonomicKeywords> setOrganizedTaxonomicKeywords(List<TaxonKeyword> keywords) {
    List<OrganizedTaxonomicKeywords> organizedTaxonomicKeywordsList = new ArrayList<>();

    // also, we want a unique set of names corresponding to empty rank
    Set<String> uniqueNamesForEmptyRank = new HashSet<>();

    Map<String, String> ranks = new LinkedHashMap<>(vocabManager.getI18nVocab(Constants.VOCAB_URI_RANKS, Locale.ENGLISH.getLanguage(), false));

    for (String rank : ranks.keySet()) {
      OrganizedTaxonomicKeywords organizedKeywords = new OrganizedTaxonomicKeywords();
      // set rank
      organizedKeywords.setRank(rank);
      // construct display name for each TaxonKeyword, and add display name to organized keywords list
      for (TaxonKeyword keyword : keywords) {
        // add display name to appropriate list if it isn't null
        String displayName = createKeywordDisplayName(keyword);
        if (displayName != null) {
          if (rank.equalsIgnoreCase(keyword.getRank())) {
            organizedKeywords.getDisplayNames().add(displayName);
          } else if (StringUtils.trimToNull(keyword.getRank()) == null) {
            uniqueNamesForEmptyRank.add(displayName);
          }
        }
      }
      // add to list
      organizedTaxonomicKeywordsList.add(organizedKeywords);
    }
    // if there were actually some names with empty ranks, add the special OrganizedTaxonomicKeywords for empty rank
    if (!uniqueNamesForEmptyRank.isEmpty()) {
      // create special OrganizedTaxonomicKeywords for empty rank
      OrganizedTaxonomicKeywords emptyRankKeywords = new OrganizedTaxonomicKeywords();
      emptyRankKeywords.setRank("Unranked");
      emptyRankKeywords.setDisplayNames(new ArrayList<>(uniqueNamesForEmptyRank));
      organizedTaxonomicKeywordsList.add(emptyRankKeywords);
    }
    // return list
    return organizedTaxonomicKeywordsList;
  }

  private String createKeywordDisplayName(TaxonKeyword keyword) {
    String combined = null;
    if (keyword != null) {
      String scientificName = StringUtils.trimToNull(keyword.getScientificName());
      String commonName = StringUtils.trimToNull(keyword.getCommonName());
      if (scientificName != null && commonName != null) {
        combined = scientificName + " (" + commonName + ")";
      } else if (scientificName != null) {
        combined = scientificName;
      } else if (commonName != null) {
        combined = commonName;
      }
    }
    return combined;
  }

  private void updateGeocoverageWithInferredFromSourceData(Resource resource, InferredMetadata inferredMetadata) {
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

  private void updateTaxonomicCoverageWithInferredFromSourceData(Resource resource, InferredMetadata inferredMetadata) {
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

  private void updateTemporalCoverageWithInferredFromSourceData(Resource resource, InferredMetadata inferredMetadata) {
    if (!resource.getMappings().isEmpty()
        && inferredMetadata.getInferredTemporalCoverage() != null
        && inferredMetadata.getInferredTemporalCoverage().getData() != null) {
      TemporalCoverage inferredTemporalCoverage = inferredMetadata.getInferredTemporalCoverage().getData();
      resource.getEml().getTemporalCoverages().clear();
      resource.getEml().addTemporalCoverage(inferredTemporalCoverage);
    }
  }

  @Override
  public InferredMetadata inferMetadata(Resource resource) {
    InferredMetadata inferredMetadata = new InferredMetadata();

    boolean serverError = false;

    // geo coverage column indexes
    int decimalLongitudeSourceColumnIndex = -1;
    int decimalLatitudeSourceColumnIndex = -1;

    // tax coverage column indexes
    int kingdomSourceColumnIndex = -1;
    int phylumSourceColumnIndex = -1;
    int classSourceColumnIndex = -1;
    int orderSourceColumnIndex = -1;
    int familySourceColumnIndex = -1;

    // temp coverage column indexes
    int eventDataSourceColumnIndex = -1;

    // geo coverage variables
    boolean geoDataMappedForAtLeastOneMapping = false;
    boolean geoDataMappedForThisMapping;
    boolean noValidDataGeo = true;
    Double minDecimalLongitude = -180.0D;
    Double maxDecimalLongitude = 180.0D;
    Double minDecimalLatitude = -90.0D;
    Double maxDecimalLatitude = 90.0D;

    // tax coverage variables
    boolean taxDataMappedForAtLeastOneMapping = false;
    boolean taxDataMappedForThisMapping;
    int taxonItemsAdded = 0;
    final int maxNumberOfTaxonItems = 200;
    Set<TaxonKeyword> taxa = new HashSet<>();

    // temp coverage variables
    boolean tempDataMappedForAtLeastOneMapping = false;
    boolean tempDataMappedForThisMapping;
    boolean noValidDataTemporal = true;
    String startDateStr = null;
    TemporalAccessor startDateTA = null;
    String endDateStr = null;
    TemporalAccessor endDateTA = null;

    boolean isNoMappings = resource.getMappings().isEmpty();

    if (!isNoMappings) {
      for (ExtensionMapping mapping : resource.getMappings()) {

        // calculate column indexes for mapping
        for (PropertyMapping field : mapping.getFields()) {
          if (VOCAB_DECIMAL_LONGITUDE.equals(field.getTerm().qualifiedName())) {
            decimalLongitudeSourceColumnIndex = field.getIndex();
          } else if (VOCAB_DECIMAL_LATITUDE.equals(field.getTerm().qualifiedName())) {
            decimalLatitudeSourceColumnIndex = field.getIndex();
          } else if (VOCAB_KINGDOM.equals(field.getTerm().qualifiedName())) {
            kingdomSourceColumnIndex = field.getIndex();
          } else if (VOCAB_PHYLUM.equals(field.getTerm().qualifiedName())) {
            phylumSourceColumnIndex = field.getIndex();
          } else if (VOCAB_CLASS.equals(field.getTerm().qualifiedName())) {
            classSourceColumnIndex = field.getIndex();
          } else if (VOCAB_ORDER.equals(field.getTerm().qualifiedName())) {
            orderSourceColumnIndex = field.getIndex();
          } else if (VOCAB_FAMILY.equals(field.getTerm().qualifiedName())) {
            familySourceColumnIndex = field.getIndex();
          } else if (VOCAB_EVENT_DATE.equals(field.getTerm().qualifiedName())) {
            eventDataSourceColumnIndex = field.getIndex();
          }
        }

        // both fields should be present
        if (decimalLongitudeSourceColumnIndex != -1 && decimalLatitudeSourceColumnIndex != -1) {
          geoDataMappedForThisMapping = true;
          geoDataMappedForAtLeastOneMapping = true;
        } else {
          geoDataMappedForThisMapping = false;
        }

        // at least one field should be present
        if (kingdomSourceColumnIndex != -1
            || phylumSourceColumnIndex != -1
            || classSourceColumnIndex != -1
            || orderSourceColumnIndex != -1
            || familySourceColumnIndex != -1) {
          taxDataMappedForThisMapping = true;
          taxDataMappedForAtLeastOneMapping = true;
        } else {
          taxDataMappedForThisMapping = false;
        }

        // field should be present
        if (eventDataSourceColumnIndex != -1) {
          tempDataMappedForThisMapping = true;
          tempDataMappedForAtLeastOneMapping = true;
        } else {
          tempDataMappedForThisMapping = false;
        }

        ClosableReportingIterator<String[]> iter = null;
        try {
          // get the source iterator
          iter = sourceManager.rowIterator(mapping.getSource());
          boolean initializeExtremeValues = true;

          while (iter.hasNext()) {
            String[] in = iter.next();
            if (in == null || in.length == 0) {
              continue;
            }

            // geographic coverage section
            if (geoDataMappedForThisMapping) {
              String rawLatitudeValue = in[decimalLatitudeSourceColumnIndex];
              String rawLongitudeValue = in[decimalLongitudeSourceColumnIndex];

              OccurrenceParseResult<LatLng> latLngParseResult =
                  CoordinateParseUtils.parseLatLng(rawLatitudeValue, rawLongitudeValue);
              LatLng latLng = latLngParseResult.getPayload();

              // skip erratic records
              if (latLng != null && latLngParseResult.isSuccessful()) {
                noValidDataGeo = false;

                // initialize min and max values
                if (initializeExtremeValues) {
                  minDecimalLatitude = latLng.getLat();
                  maxDecimalLatitude = latLng.getLat();
                  minDecimalLongitude = latLng.getLng();
                  maxDecimalLongitude = latLng.getLng();
                  initializeExtremeValues = false;
                }

                if (latLng.getLat() > maxDecimalLatitude) {
                  maxDecimalLatitude = latLng.getLat();
                }
                if (latLng.getLat() < minDecimalLatitude) {
                  minDecimalLatitude = latLng.getLat();
                }

                if (latLng.getLng() > maxDecimalLongitude) {
                  maxDecimalLongitude = latLng.getLng();
                }
                if (latLng.getLng() < minDecimalLongitude) {
                  minDecimalLongitude = latLng.getLng();
                }
              }
            }

            // taxonomic coverage section
            if (taxDataMappedForThisMapping && taxonItemsAdded < maxNumberOfTaxonItems) {
              if (kingdomSourceColumnIndex != -1
                  && StringUtils.isNotEmpty(in[kingdomSourceColumnIndex])) {
                taxa.add(new TaxonKeyword(in[kingdomSourceColumnIndex], KINGDOM, null));
                taxonItemsAdded++;
              }
              if (phylumSourceColumnIndex != -1
                  && StringUtils.isNotEmpty(in[phylumSourceColumnIndex])) {
                taxa.add(new TaxonKeyword(in[phylumSourceColumnIndex], PHYLUM, null));
                taxonItemsAdded++;
              }
              if (classSourceColumnIndex != -1
                  && StringUtils.isNotEmpty(in[classSourceColumnIndex])) {
                taxa.add(new TaxonKeyword(in[classSourceColumnIndex], CLASS, null));
                taxonItemsAdded++;
              }
              if (orderSourceColumnIndex != -1
                  && StringUtils.isNotEmpty(in[orderSourceColumnIndex])) {
                taxa.add(new TaxonKeyword(in[orderSourceColumnIndex], ORDER, null));
                taxonItemsAdded++;
              }
              if (familySourceColumnIndex != -1
                  && StringUtils.isNotEmpty(in[familySourceColumnIndex])) {
                taxa.add(new TaxonKeyword(in[familySourceColumnIndex], FAMILY, null));
                taxonItemsAdded++;
              }
            }

            // temporal coverage section
            if (tempDataMappedForThisMapping) {
              String rawEventDateValue = in[eventDataSourceColumnIndex];

              TemporalParser temporalParser = DateParsers.defaultTemporalParser();
              ParseResult<TemporalAccessor> parsedEventDateResult = temporalParser.parse(rawEventDateValue);
              TemporalAccessor parsedEventDateTA = parsedEventDateResult.getPayload();

              // skip erratic records
              if (!parsedEventDateResult.isSuccessful() || parsedEventDateTA == null || !parsedEventDateTA.isSupported(ChronoField.YEAR)) {
                continue;
              } else {
                noValidDataTemporal = false;
              }

              if (startDateTA == null) {
                startDateTA = parsedEventDateTA;
                startDateStr = rawEventDateValue;
              }
              if (endDateTA == null) {
                endDateTA = parsedEventDateTA;
                endDateStr = rawEventDateValue;
              }

              if (parsedEventDateTA instanceof YearMonth) {
                parsedEventDateTA = ((YearMonth) parsedEventDateTA).atEndOfMonth();
              }

              if (parsedEventDateTA instanceof ChronoLocalDate && ((ChronoLocalDate) startDateTA).isAfter((ChronoLocalDate) parsedEventDateTA)) {
                startDateTA = parsedEventDateTA;
                startDateStr = rawEventDateValue;
              }

              if (parsedEventDateTA instanceof ChronoLocalDate && ((ChronoLocalDate) endDateTA).isBefore((ChronoLocalDate) parsedEventDateTA)) {
                endDateTA = parsedEventDateTA;
                endDateStr = rawEventDateValue;
              }
            }

          }
        } catch (Exception e) {
          LOG.error("Error while trying to infer metadata from source data", e);
          serverError = true;
        } finally {
          if (iter != null) {
            try {
              iter.close();
            } catch (Exception e) {
              LOG.error("Error while closing iterator", e);
              serverError = true;
            }
          }
        }
      }
    }

    // finalize geocoverage
    InferredGeographicCoverage inferredGeographicCoverage = new InferredGeographicCoverage();
    inferredMetadata.setInferredGeographicCoverage(inferredGeographicCoverage);
    if (serverError) {
      inferredGeographicCoverage.addError("eml.error.serverError");
    } else if (isNoMappings) {
      inferredGeographicCoverage.addError("eml.error.noMappings");
    } else if (!geoDataMappedForAtLeastOneMapping) {
      inferredGeographicCoverage.addError("eml.geospatialCoverages.error.fieldsNotMapped");
    } else if (noValidDataGeo) {
      inferredGeographicCoverage.addError("eml.error.noValidData");
    } else {
      inferredGeographicCoverage.setInferred(true);
      GeospatialCoverage geospatialCoverage = new GeospatialCoverage();
      geospatialCoverage.setBoundingCoordinates(new BBox(new Point(minDecimalLatitude, minDecimalLongitude), new Point(maxDecimalLatitude, maxDecimalLongitude)));
      inferredGeographicCoverage.setData(geospatialCoverage);
    }

    // finalize taxcoverage
    InferredTaxonomicCoverage inferredTaxonomicCoverage = new InferredTaxonomicCoverage();
    inferredMetadata.setInferredTaxonomicCoverage(inferredTaxonomicCoverage);
    if (serverError) {
      inferredTaxonomicCoverage.addError("eml.error.serverError");
    } else if (isNoMappings) {
      inferredTaxonomicCoverage.addError("eml.error.noMappings");
    } else if (!taxDataMappedForAtLeastOneMapping) {
      inferredTaxonomicCoverage.addError("eml.taxonomicCoverages.error.fieldsNotMapped");
    } else if (taxonItemsAdded == 0) {
      inferredTaxonomicCoverage.addError("eml.error.noValidData");
    } else {
      TaxonomicCoverage taxCoverage = new TaxonomicCoverage();
      taxCoverage.setTaxonKeywords(new ArrayList<>(taxa));
      OrganizedTaxonomicCoverage organizedTaxCoverage = constructOrganizedTaxonomicCoverage(taxCoverage);
      inferredTaxonomicCoverage.setInferred(true);
      inferredTaxonomicCoverage.setData(taxCoverage);
      inferredTaxonomicCoverage.setOrganizedData(organizedTaxCoverage);
    }

    // finalize tempcoverage
    InferredTemporalCoverage inferredTemporalCoverage = new InferredTemporalCoverage();
    inferredMetadata.setInferredTemporalCoverage(inferredTemporalCoverage);
    if (serverError) {
      inferredTemporalCoverage.addError("eml.error.serverError");
    } else if (isNoMappings) {
      inferredTemporalCoverage.addError("eml.error.noMappings");
    } else if (!tempDataMappedForAtLeastOneMapping) {
      inferredTemporalCoverage.addError("eml.temporalCoverages.error.fieldsNotMapped");
    } else if (noValidDataTemporal) {
      inferredTemporalCoverage.addError("eml.error.noValidData");
    } else {
      TemporalCoverage tempCoverage = new TemporalCoverage();
      try {
        tempCoverage.setStart(startDateStr);
        tempCoverage.setEnd(endDateStr);
        inferredTemporalCoverage.setInferred(true);
        inferredTemporalCoverage.setData(tempCoverage);
      } catch (ParseException e) {
        LOG.error("Failed to parse date for temporal coverage", e);
        inferredTemporalCoverage.addError("eml.temporalCoverages.error.dateParseException");
      }
    }

    inferredMetadata.setLastModified(new Date());

    return inferredMetadata;
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
        emlFile = new File(archive.getLocation(), DataDir.EML_XML_FILENAME);
      }
      if (emlFile.exists()) {
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
      Dataset dataset = DatasetParser.build(archive.getMetadata().getBytes(StandardCharsets.UTF_8));
      eml = convertMetadataToEml(dataset);
      alog.info("manage.resource.read.basic.metadata");
      return eml;
    } catch (Exception e) {
      LOG.warn("Cant read basic archive metadata: " + e.getMessage());
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

        // TODO: 15/11/2022 update simplifiedResources?

        // ensure alternate identifier for Registry UUID set
        updateAlternateIdentifierForRegistry(resource);
      }
      // save all changes to resource
      save(resource);
    } else {
      LOG.error("Registration request failed: the resource must be public. Status=" + resource.getStatus().toString());
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
    if (resource.getEml() != null) {
      List<String> ids = resource.getEml().getAlternateIdentifiers();
      for (String id : ids) {
        try {
          UUID uuid = UUID.fromString(id);
          ls.add(uuid);
        } catch (IllegalArgumentException e) {
          // skip, isn't a candidate UUID
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
      LOG.info("Archival mode is ON with a limit of "+ cfg.getArchivalLimit()+" elements)");
      LOG.info("Clean archive versions, if needed, for resource: " + resource.getShortname());
      List<VersionHistory> history = resource.getVersionHistory();
      if (history.size() > cfg.getArchivalLimit()) {
        for (int i=cfg.getArchivalLimit(); i<history.size(); i++) {
          VersionHistory oldVersion = history.get(i);
          try {
            BigDecimal version = new BigDecimal(oldVersion.getVersion());
            LOG.info("Deleting archive version " + version + " for resource: " + resource.getShortname());
            removeArchiveVersion(resource.getShortname(), version);
          }
          catch (Exception e) {
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
    Writer writer = null;
    try {
      // make sure resource dir exists
      FileUtils.forceMkdir(cfgFile.getParentFile());
      // persist data
      writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
      xstream.toXML(resource, writer);
      // add to internal map
      addResource(resource);
    } catch (IOException e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write mapping configuration");
    } finally {
      if (writer != null) {
        closeWriter(writer);
      }
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
      // update visibility to public
      resource.setStatus(PublicationStatus.PRIVATE);

      // Changing the visibility means some public alternateIds need to be removed, e.g. IPT URL
      updateAlternateIdentifierForIPTURLToResource(resource);

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

      // Changing the visibility means some public alternateIds need to be added, e.g. IPT URL
      updateAlternateIdentifierForIPTURLToResource(resource);

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
        LOG.debug("Updating next published date of resource: " + resource.getShortname());

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
        LOG.debug("The next publication date is: " + nextPublished);
      } catch (Exception e) {
        resource.setNextPublished(null);
        // add error message that explains the consequence of the error to user
        String msg = "Auto-publishing failed: " + e.getMessage();
        LOG.error(msg, e);
        throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg, e);
      }
    } else {
      resource.setNextPublished(null);
      LOG.debug("Resource: " + resource.getShortname() + " has not been configured to use auto-publishing");
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
  public boolean hasMaxProcessFailures(Resource resource) {
    if (processFailures.containsKey(resource.getShortname())) {
      List<Date> failures = processFailures.get(resource.getShortname());

      LOG.debug("Publication has failed " + failures.size() + " time(s) for resource: " + resource
        .getTitleAndShortname());
      return failures.size() >= MAX_PROCESS_FAILURES;
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
    if ((version != null) && !version.equals(resource.getEmlVersion())) {
      LOG.debug("Removing version "+version+" for resource: "+resource.getShortname());
      try {
        removeVersion(resource.getShortname(), version);
        resource.removeVersionHistory(version);
        save(resource);
        LOG.debug("Version "+version+" has been removed for resource: "+resource.getShortname());
      }
      catch(IOException e) {
        LOG.error("Cannot remove version "+version+" for resource: "+resource.getShortname(), e);
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
        LOG.debug(dwcaFile.getAbsolutePath() + " has been successfully deleted.");
      }
    }
  }

  public void removeVersion(String shortname, BigDecimal version) throws IOException {
    // delete eml-1.1.xml if it exists (eml.xml must remain)
    File versionedEMLFile = dataDir.resourceEmlFile(shortname, version);
    if (versionedEMLFile.exists()) {
      FileUtils.forceDelete(versionedEMLFile);
    }
    // delete shortname-1.1.rtf if it exists
    File versionedRTFFile = dataDir.resourceRtfFile(shortname, version);
    if (versionedRTFFile.exists()) {
      FileUtils.forceDelete(versionedRTFFile);
    }
    // delete dwca-1.1.zip if it exists
    File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, version);
    if (versionedDwcaFile.exists()) {
      FileUtils.forceDelete(versionedDwcaFile);
    }
  }
}
