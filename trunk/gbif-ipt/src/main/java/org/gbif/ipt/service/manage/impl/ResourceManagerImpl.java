package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
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
import org.gbif.ipt.utils.EmlUtils;
import org.gbif.metadata.BasicMetadata;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.KeywordSet;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.xml.sax.SAXException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager, ReportHandler {

  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  public static final String RESOURCE_IDENTIFIER_LINK_PART = "/resource.do?id=";
  public static final String RESOURCE_PUBLIC_LINK_PART = "/resource.do?r=";
  private static final int MAX_PROCESS_FAILURES = 3;
  private final XStream xstream = new XStream();
  private SourceManager sourceManager;
  private ExtensionManager extensionManager;
  private RegistryManager registryManager;
  private ThreadPoolExecutor executor;
  private GenerateDwcaFactory dwcaFactory;
  private Map<String, Future<Integer>> processFutures = new HashMap<String, Future<Integer>>();
  private ListMultimap<String, Date> processFailures = ArrayListMultimap.create();
  private Map<String, StatusReport> processReports = new HashMap<String, StatusReport>();
  private Eml2Rtf eml2Rtf;
  private VocabulariesManager vocabManager;
  private SimpleTextProvider textProvider;
  private RegistrationManager registrationManager;

  @Inject
  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
    OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter,
    JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager, ExtensionManager extensionManager,
    RegistryManager registryManager, ConceptTermConverter conceptTermConverter, GenerateDwcaFactory dwcaFactory,
    PasswordConverter passwordConverter, Eml2Rtf eml2Rtf, VocabulariesManager vocabManager,
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
      passwordConverter);
    this.textProvider = textProvider;
    this.registrationManager = registrationManager;
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
  }

  public boolean cancelPublishing(String shortname, BaseAction action) {
    boolean canceled = false;
    // get future
    Future<Integer> f = processFutures.get(shortname);
    if (f != null) {
      // cancel job, even if it's running
      canceled = f.cancel(true);
      if (canceled) {
        // remove process from locking list
        processFutures.remove(shortname);
      } else {
        log.warn("Canceling publication of resource " + shortname + " failed");
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
        log.error(e);
      }
    }
  }

  /**
   * Read other metadata formats like Dublin Core, and populate an Eml instance from all corresponding fields possible.
   * 
   * @param metadata BasicMetadata object
   * @return Eml instance
   */
  private Eml convertMetadataToEml(BasicMetadata metadata) {
    Eml eml = new Eml();
    if (metadata != null) {
      if (metadata instanceof Eml) {
        eml = (Eml) metadata;
      } else {
        // copy properties
        eml.setTitle(metadata.getTitle());
        eml.setDescription(metadata.getDescription());
        eml.setDistributionUrl(metadata.getHomepageUrl());
        eml.setLogoUrl(metadata.getLogoUrl());
        eml.setSubject(metadata.getSubject());
        eml.setPubDate(metadata.getPublished());
      }
    }
    return eml;
  }

  /**
   * Copies incoming eml file to data directory with name eml.xml.
   * </br>
   * Thie method retrieves a file handle to the eml.xml file in data directory. It then copies the incoming emlFile to
   * over to this file. From this file an Eml instance is then populated and returned.
   * 
   * @param shortname shortname
   * @param emlFile eml file
   * @return populated Eml instance
   * @throws ImportException if eml file could not be read/parsed
   */
  private Eml copyMetadata(String shortname, File emlFile) throws ImportException {
    File emlFile2 = dataDir.resourceEmlFile(shortname, null);
    try {
      FileUtils.copyFile(emlFile, emlFile2);
    } catch (IOException e1) {
      log.error("Unnable to copy EML File", e1);
    }
    Eml eml;
    try {
      InputStream in = new FileInputStream(emlFile2);
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (IOException e) {
      log.error(e);
      throw new ImportException("Invalid EML document");
    } catch (SAXException e) {
      log.error("Invalid EML document", e);
      throw new ImportException("Invalid EML document");
    }
    return eml;
  }

  public Resource create(String shortname, String type, File dwca, User creator, BaseAction action)
    throws AlreadyExistingException, ImportException, InvalidFilenameException {
    ActionLogger alog = new ActionLogger(this.log, action);
    Resource resource;
    // decompress archive
    List<File> decompressed = null;
    File dwcaDir = dataDir.tmpDir();
    try {
      decompressed = CompressionUtil.decompressFile(dwcaDir, dwca, true);
    } catch (UnsupportedCompressionType e) {
      log.debug("1st attempt to decompress file failed: " + e.getMessage(), e);
      // try again as single gzip file
      try {
        decompressed = CompressionUtil.ungzipFile(dwcaDir, dwca, false);
      } catch (Exception e2) {
        log.debug("2nd attempt to decompress file failed: " + e.getMessage(), e);
      }
    } catch (Exception e) {
      log.debug("Decompression failed: " + e.getMessage(), e);
    }

    // create resource:
    // if decompression failed, create resource from single eml file
    if (decompressed == null) {
      resource = createFromEml(shortname, dwca, creator, alog);
    }
    // if decompression succeeded, create resource depending on whether file was 'IPT Resource Folder' or a 'DwC-A'
    else {
      resource =
        (isIPTResourceFolder(dwcaDir)) ? createFromIPTResourceFolder(shortname, dwcaDir.listFiles()[0], creator,
          alog) : createFromArchive(shortname, dwcaDir, creator, alog);
    }

    // set resource type, if it hasn't been set already
    if (type != null && Strings.isNullOrEmpty(resource.getCoreType())) {
      resource.setCoreType(type);
    }

    return resource;
  }

  /**
   * Creates a resource from an IPT Resource folder. The purpose is to preserve the original source files and mappings.
   * The managers, created date, last publication date, and registration info is all cleared. The creator and modifier
   * are set to the current creator.
   * </p>
   * This method must ensure that the folder has a unique name relative to the other resource's shortnames, otherwise
   * it tries to rename the folder using the supplied shortname. If neither of these yield a unique shortname,
   * an exception is thrown alerting the user they should try again with a unique name.
   * 
   * @param shortname resource shortname
   * @param folder IPT resource folder (in tmp directory of IPT data_dir)
   * @param creator Creator
   * @param alog action logging
   * @return Resource created or null if it was unsuccessful
   * @throws AlreadyExistingException if a unique shortname could not be determined
   * @throws ImportException if a problem occurred trying to create the new Resource
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
      res = loadFromDir(dest, alog);

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
        // reset isRegistered, do this by resetting key
        res.setKey(null);
        // set publication status to Private
        res.setStatus(PublicationStatus.PRIVATE);
        // set rowIterator published to 0
        res.setRecordsPublished(0);
        // set isPublished to false
        res.isPublished();

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
   * Determine whether the decompressed file represents an IPT Resource folder or not. To qualify, the root
   * folder must contain at the very least a resource.xml file, and an eml.xml file.
   * 
   * @param tmpDir folder where compressed file was decompressed
   * @return if there is an IPT Resource folder or not that has been extracted in the tmpDir
   */
  private boolean isIPTResourceFolder(File tmpDir) {
    boolean foundResourceFile = false;
    boolean foundEmlFile = false;
    if (tmpDir.exists() && tmpDir.isDirectory()) {
      // get the compressed folder's root folder
      File[] contents = tmpDir.listFiles();
      if (contents == null) {
        return false;
      } else if (contents.length != 1) {
        return false;
      } else {
        File root = contents[0];
        // differentiate between single file and potential resource folder
        if (root.isDirectory()) {
          // return all files in root directory, and filter by .xml files
          for (File f : root.listFiles(new XmlFilenameFilter())) {
            // have we found the resource.xml file?
            if (f.getName().equalsIgnoreCase(PERSISTENCE_FILE)) {
              foundResourceFile = true;
            }
            // have we found the eml.xml file?
            if (f.getName().equalsIgnoreCase(DataDir.EML_XML_FILENAME)) {
              foundEmlFile = true;
            }
          }
        } else {
          log.debug("A single file has been encountered");
        }
      }
    }
    return (foundEmlFile && foundResourceFile);
  }

  /**
   * Try to locate a DwC-A located inside a parent folder, open it, and return the Archive.
   * 
   * @param tmpDir folder where compressed file was decompressed
   * @return the Archive, or null if none exists
   * @throws UnsupportedArchiveException if the DwC-A was invalid
   * @throws IOException if the DwC-A could not be opened
   */
  protected Archive openArchiveInsideParentFolder(File tmpDir) throws UnsupportedArchiveException, IOException {
    if (tmpDir.exists() && tmpDir.isDirectory()) {
      // get the compressed folder's root folder
      File[] contents = tmpDir.listFiles();
      if (contents == null) {
        return null;
      } else if (contents.length != 1) {
        return null;
      } else {
        File root = contents[0];
        // differentiate between single file and potential DwC-A folder
        if (root.isDirectory()) {
          return ArchiveFactory.openArchive(root);
        } else {
          log.debug("A single file has been encountered");
        }
      }
    }
    return null;
  }

  /**
   * Filter those files with suffixes ending in .xml.
   */
  private static class XmlFilenameFilter implements FilenameFilter {

    public boolean accept(File dir, String name)
    {
      return name != null && name.toLowerCase().endsWith(".xml");
    }
  }

  public Resource create(String shortname, String type, User creator) throws AlreadyExistingException {
    Resource res = null;
    if (shortname != null) {
      // convert short name to lower case
      String lower = shortname.toLowerCase();
      // check if existing already
      if (resources.containsKey(lower)) {
        throw new AlreadyExistingException();
      }
      res = new Resource();
      res.setShortname(lower);
      res.setCreated(new Date());
      res.setCreator(creator);
      if (type != null) {
        res.setCoreType(type);
      }
      // create dir
      try {
        save(res);
        log.info("Created resource " + res.getShortname());
      } catch (InvalidConfigException e) {
        log.error("Error creating resource", e);
        return null;
      }
    }
    return res;
  }

  private Resource createFromArchive(String shortname, File dwca, User creator, ActionLogger alog)
    throws AlreadyExistingException, ImportException, InvalidFilenameException {
    Resource resource;
    try {
      // open the dwca, assuming it is located inside a parent folder
      Archive arch = openArchiveInsideParentFolder(dwca);
      if (arch == null) {
        // otherwise, open the dwca with dwca reader
        arch = ArchiveFactory.openArchive(dwca);
      }

      // keep track of source files as a dwca might refer to the same source file multiple times
      Map<String, TextFileSource> sources = new HashMap<String, TextFileSource>();
      if (arch.getCore() != null) {

        // determine coreType for the resource based on the rowType
        String coreRowType = StringUtils.trimToNull(arch.getCore().getRowType());
        if (Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(coreRowType)) {
          // Taxon
          coreRowType = StringUtils.capitalize(CoreRowType.CHECKLIST.toString().toLowerCase());
        } else if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(coreRowType)) {
          // Occurrence
          coreRowType = StringUtils.capitalize(CoreRowType.OCCURRENCE.toString().toLowerCase());
        } else {
          coreRowType = StringUtils.capitalize(CoreRowType.OTHER.toString().toLowerCase());
        }

        // create new resource
        resource = create(shortname, coreRowType, creator);

        // read core source+mappings
        TextFileSource s = importSource(resource, arch.getCore());
        sources.put(arch.getCore().getLocation(), s);
        ExtensionMapping map = importMappings(alog, arch.getCore(), s);

        resource.addMapping(map);
        // read extension sources+mappings
        for (ArchiveFile ext : arch.getExtensions()) {
          if (sources.containsKey(ext.getLocation())) {
            s = sources.get(ext.getLocation());
            log.debug("SourceBase " + s.getName() + " shared by multiple extensions");
          } else {
            s = importSource(resource, ext);
            sources.put(ext.getLocation(), s);
          }
          map = importMappings(alog, ext, s);
          resource.addMapping(map);
        }
        // try to read metadata
        Eml eml = readMetadata(resource.getShortname(), arch, alog);
        if (eml != null) {
          resource.setEml(eml);
        }
        // finally persist the whole thing
        save(resource);

        if (StringUtils.isBlank(resource.getCoreRowType())) {
          alog.info("manage.resource.create.success.nocore",
            new String[] {String.valueOf(resource.getSources().size()), String.valueOf(resource.getMappings().size())});
        } else {
          alog.info("manage.resource.create.success",
            new String[] {resource.getCoreRowType(), String.valueOf(resource.getSources().size()),
              String.valueOf(resource.getMappings().size())});
        }

      } else {
        alog.warn("manage.resource.create.core.invalid");
        throw new ImportException("Darwin core archive is invalid and does not have a core mapping");
      }

    } catch (UnsupportedArchiveException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    } catch (IOException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    }

    return resource;
  }

  /**
   * Create new resource from eml file.
   * 
   * @param shortname resource shortname
   * @param emlFile eml file
   * @param creator User creating resource
   * @param alog ActionLogger
   * @return resource created
   * @throws AlreadyExistingException if the resource created uses a shortname that already exists
   * @throws ImportException if the eml file could not be read/parsed
   */
  private Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog)
    throws AlreadyExistingException, ImportException {
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
    ExtensionRowTypeConverter extensionConverter, ConceptTermConverter conceptTermConverter,
    JdbcInfoConverter jdbcInfoConverter, PasswordConverter passwordConverter) {
    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);
    xstream.alias("filesource", TextFileSource.class);
    xstream.alias("excelsource", ExcelFileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", PropertyMapping.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "type");
    // make files transient to allow moving the datadir
    xstream.omitField(TextFileSource.class, "file");

    // persist only emails for users
    xstream.registerConverter(userConverter);
    // persist only rowtype
    xstream.registerConverter(extensionConverter);
    // persist only qualified concept name
    xstream.registerConverter(conceptTermConverter);
    // encrypt passwords
    xstream.registerConverter(passwordConverter);

    xstream.addDefaultImplementation(ExtensionProperty.class, Term.class);
    xstream.addDefaultImplementation(DwcTerm.class, Term.class);
    xstream.addDefaultImplementation(DcTerm.class, Term.class);
    xstream.addDefaultImplementation(GbifTerm.class, Term.class);
    xstream.addDefaultImplementation(IucnTerm.class, Term.class);
    xstream.registerConverter(orgConverter);
    xstream.registerConverter(jdbcInfoConverter);
  }

  public void delete(Resource resource) throws IOException, DeletionNotAllowedException {
    // deregister resource?
    if (resource.getKey() != null) {
      try {
        registryManager.deregister(resource);
      } catch (RegistryException e) {
        log.error("Failed to deregister resource: " + e.getMessage(), e);
        throw new DeletionNotAllowedException(Reason.REGISTRY_ERROR, e.getMessage());
      }
    }
    // remove from data dir
    FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
    // remove object
    resources.remove(resource.getShortname().toLowerCase());
  }

  /**
   * @see #isLocked(String, BaseAction) for removing jobs from internal maps
   */
  private void generateDwca(Resource resource) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDwca worker = dwcaFactory.create(resource, this);
    Future<Integer> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  public Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  public long getDwcaSize(Resource resource) {
    File data = dataDir.resourceDwcaFile(resource.getShortname());
    return data.length();
  }

  public long getEmlSize(Resource resource) {
    File data = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
    return data.length();
  }

  public URL getResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseUrl() + RESOURCE_IDENTIFIER_LINK_PART + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  public URL getPublicResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseUrl() + RESOURCE_PUBLIC_LINK_PART + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  public long getRtfSize(Resource resource) {
    File data = dataDir.resourceRtfFile(resource.getShortname());
    return data.length();
  }

  private ExtensionMapping importMappings(ActionLogger alog, ArchiveFile af, Source source) {
    ExtensionMapping map = new ExtensionMapping();
    map.setSource(source);
    Extension ext = extensionManager.get(af.getRowType());
    if (ext == null) {
      alog.warn("manage.resource.create.rowType.null", new String[] {af.getRowType()});
      return null;
    }
    map.setExtension(ext);

    // set ID column
    map.setIdColumn(af.getId().getIndex());

    Set<PropertyMapping> fields = new TreeSet<PropertyMapping>();
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

  private TextFileSource importSource(Resource config, ArchiveFile af) throws ImportException,
    InvalidFilenameException {
    File extFile = af.getLocationFile();
    TextFileSource s = (TextFileSource) sourceManager.add(config, extFile, af.getLocation());
    SourceManagerImpl.copyArchiveFileProperties(af, s);

    // the number of rows was calculated using the standard file importer
    // make an adjustment now that the exact number of header rows are known
    if (s.getIgnoreHeaderLines() != 1) {
      log.info("Adjusting row count to " + (s.getRows() + 1 - s.getIgnoreHeaderLines()) + " from " + s.getRows()
        + " since header count is declared as " + s.getIgnoreHeaderLines());
    }
    s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());

    return s;
  }

  public boolean isEmlExisting(String shortName) {
    File emlFile = dataDir.resourceEmlFile(shortName, null);
    return emlFile.exists();
  }

  public boolean isLocked(String shortname, BaseAction action) {
    if (processFutures.containsKey(shortname)) {
      Resource resource = get(shortname);
      String sVersion = String.valueOf(resource.getEmlVersion());

      // is listed as locked but task might be finished, check
      Future<Integer> f = processFutures.get(shortname);
      // if this task finished
      if (f.isDone()) {
        boolean succeeded = false;
        String reasonFailed = null;
        Throwable cause = null;
        try {
          // retrieve resource record count (number of records published in DwC-A)
          Integer recordCount = f.get();
          // finish publication (update registration, persist resource changes)
          publishEnd(resource, recordCount, action);
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
            String msg = action.getText("publishing.success", new String[] {sVersion, resource.getShortname()});
            StatusReport updated = new StatusReport(true, msg, getTaskMessages(shortname));
            processReports.put(shortname, updated);
          } else {
            // alert user publication failed
            String msg = action.getText("publishing.failed", new String[] {sVersion, shortname, reasonFailed});
            action.addActionError(msg);

            // update StatusReport on publishing page
            if (cause != null) {
              StatusReport updated = new StatusReport(new Exception(cause), msg, getTaskMessages(shortname));
              processReports.put(shortname, updated);
            }

            // the previous version needs to be rolled back
            restoreVersion(resource, resource.getLastVersion(), action);

            // keep track of how many failures on auto publication have happened
            processFailures.put(resource.getShortname(), new Date());
          }
          // remove process from locking list
          processFutures.remove(shortname);
        }
        return false;
      }
      return true;
    }
    return false;
  }

  public boolean isLocked(String shortname) {
    return isLocked(shortname, new BaseAction(textProvider, cfg, registrationManager));
  }

  public boolean isRtfExisting(String shortName) {
    File rtfFile = dataDir.resourceRtfFile(shortName);
    return rtfFile.exists();
  }

  public List<Resource> latest(int startPage, int pageSize) {
    List<Resource> resourceList = new ArrayList<Resource>();
    for (Resource resource : resources.values()) {
      if (!resource.getStatus().equals(PublicationStatus.PRIVATE)) {
        resourceList.add(resource);
      }
    }
    Collections.sort(resourceList, new Comparator<Resource>() {

      public int compare(Resource r1, Resource r2) {
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
      }
    });
    return resourceList;
  }

  public List<Resource> list() {
    return new ArrayList<Resource>(resources.values());
  }

  public List<Resource> list(PublicationStatus status) {
    List<Resource> result = new ArrayList<Resource>();
    for (Resource r : resources.values()) {
      if (r.getStatus() == status) {
        result.add(r);
      }
    }
    return result;
  }

  public List<Resource> list(User user) {
    List<Resource> result = new ArrayList<Resource>();
    // select basedon user rights - for testing return all resources for now
    for (Resource res : resources.values()) {
      if (RequireManagerInterceptor.isAuthorized(user, res)) {
        result.add(res);
      }
    }
    return result;
  }

  public int load() {
    File resourcesDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
    resources.clear();
    int counter = 0;
    if (resourcesDir != null) {
      File[] files = resourcesDir.listFiles();
      if (files != null) {
        for (File resourceDir : files) {
          if (resourceDir.isDirectory()) {
            try {
              addResource(loadFromDir(resourceDir));
              counter++;
            } catch (InvalidConfigException e) {
              log.error("Cant load resource " + resourceDir.getName(), e);
            }
          }
        }
        log.info("Loaded " + counter + " resources into memory altogether.");
      } else {
        log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
      }
    } else {
      log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
    }
    return counter;
  }

  /**
   * Loads a resource's metadata from its eml.xml file located inside its resource directory. If no eml.xml file was
   * found, the resource is loaded with an empty EML instance.
   * 
   * @param resource resource
   * @return EML object loaded from eml.xml file or a new EML instance if none found
   */
  private Eml loadEml(Resource resource) {
    File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    // US Locale is used because uses '.' for decimal separator
    Eml eml = EmlUtils.loadWithLocale(emlFile, Locale.US);
    // load resource metadata
    resource.setEml(eml);
    // udpate EML with latest resource basics (version and GUID)
    syncEmlWithResource(resource);
    return eml;
  }

  /**
   * Calls loadFromDir(File, ActionLogger), inserting a new instance of ActionLogger.
   * 
   * @param resourceDir resource directory
   * @return loaded Resource
   */
  protected Resource loadFromDir(File resourceDir) {
    return loadFromDir(resourceDir, new ActionLogger(log, new BaseAction(textProvider, cfg, registrationManager)));
  }

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache.
   */
  private Resource loadFromDir(File resourceDir, ActionLogger alog) throws InvalidConfigException {
    if (resourceDir.exists()) {
      // load full configuration from resource.xml and eml.xml files
      String shortname = resourceDir.getName();
      try {
        File cfgFile = dataDir.resourceFile(shortname, PERSISTENCE_FILE);
        InputStream input = new FileInputStream(cfgFile);
        Resource resource = (Resource) xstream.fromXML(input);
        // non existing users end up being a NULL in the set, so remove them
        // shouldnt really happen - but people can even manually cause a mess
        resource.getManagers().remove(null);

        // 1. Non existent Extension end up being NULL
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
        // load eml
        loadEml(resource);
        log.debug("Read resource configuration for " + shortname);
        return resource;
      } catch (FileNotFoundException e) {
        log.error("Cannot read resource configuration for " + shortname, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG,
          "Cannot read resource configuration for " + shortname + ": " + e.getMessage());
      }
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
        }
      }
      // if the subtype doesn't use a standardized term from the vocab, it's reset to null
      if (!usesVocab) {
        resource.setSubtype(null);
      }
    }
    return resource;
  }

  public boolean publish(Resource resource, int version, BaseAction action)
    throws PublicationException, InvalidConfigException {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    // publish EML
    publishEml(resource, version);

    // publish RTF
    publishRtf(resource, version);

    // (re)generate dwca asynchronously
    boolean dwca = false;
    if (resource.hasMappedData()) {
      generateDwca(resource);
      dwca = true;
    } else {
      // finish publication now
      publishEnd(resource, 0, action);
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
   * @param recordCount number of records publishes (core file record count)
   * @param action action
   * @throws PublicationException if publication was unsuccessful
   * @throws InvalidConfigException if resource configuration could not be saved
   */
  private void publishEnd(Resource resource, int recordCount, BaseAction action)
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
    updateNextPublishedDate(resource);
    // set number of records published
    resource.setRecordsPublished(recordCount);
    // save the number of records published for version file (needed to display record count on resource version page)
    saveVersionCount(resource);
    // persist resource object changes
    save(resource);
    // final logging
    String msg = action
      .getText("publishing.success", new String[] {String.valueOf(resource.getEmlVersion()), resource.getShortname()});
    action.addActionMessage(msg);
    log.info(msg);
  }

  public void restoreVersion(Resource resource, int version, BaseAction action) {
    // prevent null action from being handled
    if (action == null) {
      action = new BaseAction(textProvider, cfg, registrationManager);
    }
    String shortname = resource.getShortname();
    String sVersion = String.valueOf(version);
    log.info("Restoring version #" + sVersion + " of resource " + shortname);

    if (version >= 0) {
      int versionToRollback = version + 1;
      try {
        // delete eml-1.xml if it exists (eml.xml must remain)
        File versionedEMLFile = dataDir.resourceEmlFile(shortname, versionToRollback);
        if (versionedEMLFile.exists()) {
          FileUtils.forceDelete(versionedEMLFile);
        }
        // delete shortname-1.rtf if it exists
        File versionedRTFFile = dataDir.resourceRtfFile(shortname, versionToRollback);
        if (versionedRTFFile.exists()) {
          FileUtils.forceDelete(versionedRTFFile);
        }
        // delete dwca-1.zip if it exists
        File versionedDwcaFile = dataDir.resourceDwcaFile(shortname, versionToRollback);
        if (versionedDwcaFile.exists()) {
          FileUtils.forceDelete(versionedDwcaFile);
        }
        // dwca.zip should be replaced with dwca-version.zip - if it exists
        File versionedDwcaFileToRestore = dataDir.resourceDwcaFile(shortname, version);
        if (versionedDwcaFileToRestore.exists()) {
          // proceed with overwriting/replacing dwca.zip with dwca-version.zip
          File dwca = dataDir.resourceDwcaFile(resource.getShortname(), null);
          FileUtils.copyFile(versionedDwcaFileToRestore, dwca);
        }
        // delete .recordspublished-1 if it exists
        File versionedCountFile = dataDir.resourceCountFile(shortname, versionToRollback);
        if (versionedCountFile != null && versionedCountFile.exists()) {
          FileUtils.forceDelete(versionedCountFile);
        }

        // update resource.xml
        resource.setEmlVersion(version);
        save(resource);

        // update eml.xml and persist changes
        resource.getEml().setPubDate(resource.getLastPublished());
        saveEml(resource);

      } catch (IOException e) {
        String msg = action.getText("restore.resource.failed", new String[] {sVersion, shortname, e.getMessage()});
        log.error(msg, e);
        action.addActionError(msg);
      }
      // alert user version rollback was successful
      String msg = action.getText("restore.resource.success", new String[] {sVersion, shortname});
      log.info(msg);
      action.addActionMessage(msg);
      // update StatusReport on publishing page
      // Warning: don't retrieve status report using status() otherwise a cyclical call to isLocked results
      StatusReport report = processReports.get(shortname);
      if (report != null) {
        report.getMessages().add(new TaskMessage(Level.INFO, msg));
      }
    }
  }

  /**
   * Updates the resource's alternate identifier for its corresponding Registry UUID and saves the EML.
   * If called on a resource that is already registered, the method ensures that it won't be added a second time.
   * To accommodate updates from older versions of the IPT, the identifier is added by calling this method every
   * time the resource gets re-published.
   * 
   * @param resource resource
   * @return resource with Registry UUID for the resource updated
   */
  public Resource updateAlternateIdentifierForRegistry(Resource resource) {
    Eml eml = resource.getEml();
    if (eml != null) {
      // retrieve a list of the resource's alternate identifiers
      List<String> currentIds = eml.getAlternateIdentifiers();
      if (currentIds != null) {
        // make new list of alternative identifiers in lower case so comparison is done in lower case only
        List<String> ids = new ArrayList<String>();
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
              log.info("GBIF Registry UUID added to Resource's list of alternate identifiers");
            }
          }
        }
      }
    } else {
      resource.setEml(new Eml());
    }

    return resource;
  }

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
        // try to match resource.do?r=
        if (id.contains(RESOURCE_PUBLIC_LINK_PART)) {
          exists = true;
          existingId = id;
        }
      }
      // if the resource is PUBLIC, or REGISTERED
      if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) != 0) {
        URL url = getPublicResourceLink(resource.getShortname());
        // if the URL is not null, and the identifier does not exist yet - add it!
        if (url != null) {
          // if it already exists, then replace it just in case the baseURL has changed, for example
          if (exists) {
            ids.remove(existingId);
          }
          // lastly, be sure to add it
          ids.add(url.toString());
          // save all changes to Eml
          saveEml(resource);
          if (cfg.debug()) {
            log.info("IPT URL to resource added to (or updated in) Resource's list of alt ids");
          }
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
            log.info("Following visibility change, IPT URL to resource was removed from Resource's list of alt ids");
          }
        }
      }
    }
    return resource;
  }

  /**
   * Publishes a new version of the EML file for the given resource. After publishing the new version, it copies a
   * stable version of the EML file for archival purposes.
   * 
   * @param resource Resource
   * @param version version number to publish
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishEml(Resource resource, int version) throws PublicationException {
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

    // save all changes to Eml
    saveEml(resource);

    // copy stable version of the eml file
    File trunkFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);
    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.EML,
        "Can't publish eml file for resource " + resource.getShortname(), e);
    }
  }

  /**
   * Publishes a new version of the RTF file for the given resource. After publishing the new version, it copies a
   * stable version of the RTF file for archival purposes.
   * 
   * @param resource Resource
   * @param version version number to publish
   * @throws PublicationException if resource was already being published, or if publishing failed for any reason
   */
  private void publishRtf(Resource resource, int version) throws PublicationException {
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    Document doc = new Document();
    File rtfFile = dataDir.resourceRtfFile(resource.getShortname());
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
          log.warn("FileOutputStream to RTF file could not be closed");
        }
      }
    }

    // copy current rtf version.
    File trunkRtfFile = dataDir.resourceRtfFile(resource.getShortname());
    File versionedRtfFile = dataDir.resourceRtfFile(resource.getShortname(), version);
    try {
      FileUtils.copyFile(trunkRtfFile, versionedRtfFile);
    } catch (IOException e) {
      throw new PublicationException(PublicationException.TYPE.RTF,
        "Can't publish rtf file for resource " + resource.getShortname(), e);
    }
  }

  /**
   * Try to read metadata file for a DwC-Archive.
   * 
   * @param shortname resource shortname
   * @param archive archive
   * @param alog ActionLogger
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
        log.warn("Cant find any eml metadata to import");
      }
    } catch (ImportException e) {
      String msg = "Cant read basic archive metadata: " + e.getMessage();
      log.warn(msg);
      alog.warn(msg);
      return null;
    } catch (Exception e) {
      log.warn("Cant read archive eml metadata", e);
    }
    // try to read other metadata formats like dc
    try {
      eml = convertMetadataToEml(archive.getMetadata());
      alog.info("manage.resource.read.basic.metadata");
      return eml;
    } catch (Exception e) {
      log.warn("Cant read basic archive metadata: " + e.getMessage());
    }
    alog.warn("manage.resource.read.problem");
    return null;
  }


  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public void register(Resource resource, Organisation organisation, Ipt ipt, BaseAction action)
    throws RegistryException {
    ActionLogger alog = new ActionLogger(this.log, action);

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
        UUID candidate = Iterables.getOnlyElement(candidateResourceUUIDs);
        List<String> duplicateUses = detectDuplicateUsesOfUUID(candidate, resource.getShortname());
        if (duplicateUses.isEmpty()) {
          if (organisation.getKey() != null && organisation.getName() != null) {
            boolean matched = false;
            // collect list of registered resources associated to organization
            List<Resource> existingResources =
              registryManager.getOrganisationsResources(organisation.getKey().toString());
            for (Resource entry : existingResources) {
              // is the candidate UUID equal to the UUID from an existing registered resource owned by the
              // organization? There should only be one match, and the first one encountered will be used for migration.
              if (entry.getKey() != null && candidate.equals(entry.getKey())) {
                log.debug("Resource matched to existing registered resource, UUID=" + entry.getKey().toString());

                // fill in registration info - we've found the original resource being migrated to the IPT
                resource.setStatus(PublicationStatus.REGISTERED);
                resource.setKey(entry.getKey());
                resource.setOrganisation(organisation);

                // display update about migration to user
                alog.info("manage.resource.migrate", new String[] {entry.getKey().toString(), organisation.getName()});

                // update the resource, adding the new service(s)
                updateRegistration(resource, action);

                // indicate a match was found
                matched = true;

                // just in case, ensure only a single existing resource is updated
                break;
              }
            }
            // if no match was ever found, this is considered a failed resource migration
            if (!matched) {
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
          throw new RegistryException(RegistryException.TYPE.MISSING_METADATA,
            "No key returned for registered resource");
        }
        // display success to user
        alog.info("manage.overview.resource.registered", new String[] {organisation.getName()});

        // change status to registered
        resource.setStatus(PublicationStatus.REGISTERED);

        // ensure alternate identifier for Registry UUID set
        updateAlternateIdentifierForRegistry(resource);
      }
      // save all changes to resource
      save(resource);
    } else {
      log.error("Registration request failed: the resource must be public. Status=" + resource.getStatus().toString());
    }
  }

  /**
   * For a candidate UUID, find out:
   * -how many public resources have a matching alternate identifier UUID
   * -how many registered resources have the same UUID
   * 
   * @param candidate UUID
   * @param shortname shortname of resource to exclude from matching
   * @return list of names of resources that have matched candidate UUID
   */
  @VisibleForTesting
  protected List<String> detectDuplicateUsesOfUUID(UUID candidate, String shortname) {
    ListMultimap<UUID, String> duplicateUses = ArrayListMultimap.create();
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
   * @return set of UUIDs that could qualify as GBIF Registry Dataset UUIDs
   */
  private Set<UUID> collectCandidateResourceUUIDsFromAlternateIds(Resource resource) {
    Set<UUID> ls = new HashSet<UUID>();
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

  public synchronized void report(String shortname, StatusReport report) {
    processReports.put(shortname, report);
  }

  /**
   * Store the number of records published for version v of resource to a hidden file called .recordspublished-v needed
   * to display the record number on the resource homepage for that particular version.
   * 
   * @param resource resource
   */
  @VisibleForTesting
  protected synchronized void saveVersionCount(Resource resource) {
    Writer writer = null;
    try {
      File file = dataDir.resourceCountFile(resource.getShortname(), resource.getEmlVersion());
      if (file != null) {
        writer = org.gbif.utils.file.FileUtils.startNewUtf8File(file);
        writer.write(String.valueOf(resource.getRecordsPublished()));
      } else {
        log.error("Count file for resource " + resource.getShortname() + " and version " + resource.getEmlVersion()
          + " non existing");
      }
    } catch (IOException e) {
      log.error("Problem writing to count file", e);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          log.error("Problem closing count file");
        }
      }
    }
  }

  public synchronized void save(Resource resource) throws InvalidConfigException {
    File cfgFile = dataDir.resourceFile(resource, PERSISTENCE_FILE);
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
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write mapping configuration");
    } finally {
      if (writer != null) {
        closeWriter(writer);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
   */
  public synchronized void saveEml(Resource resource) throws InvalidConfigException {
    // update EML with latest resource basics (version and GUID)
    syncEmlWithResource(resource);
    // set modified date
    resource.setModified(new Date());
    // save into data dir
    File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    // Locale.US it's used because uses '.' as the decimal separator
    EmlUtils.writeWithLocale(emlFile, resource, Locale.US);
    log.debug("Updated EML file for " + resource);
  }

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
   */
  private void syncEmlWithResource(Resource resource) {
    // set EML version
    resource.getEml().setEmlVersion(resource.getEmlVersion());
    // we need some GUID. If we have use the registry key, if not use the resource URL
    if (resource.getKey() != null) {
      resource.getEml().setGuid(resource.getKey().toString());
    } else {
      resource.getEml().setGuid(getResourceLink(resource.getShortname()).toString());
    }
    // add/update KeywordSet for dataset type and subtype
    updateKeywordsWithDatasetTypeAndSubtype(resource);
  }

  public void updateRegistration(Resource resource, BaseAction action) throws PublicationException {
    if (resource.isRegistered()) {
      // prevent null action from being handled
      if (action == null) {
        action = new BaseAction(textProvider, cfg, registrationManager);
      }
      try {
        log.debug("Updating registration of resource with key: " + resource.getKey().toString());

        // get IPT key
        String iptKey = null;
        if (registrationManager.getIpt() != null) {
          iptKey =
            (registrationManager.getIpt().getKey() == null) ? null : registrationManager.getIpt().getKey().toString();
        }

        // perform update
        registryManager.updateResource(resource, iptKey);

        // log
        String msg = action.getText("manage.overview.resource.update.registration", new String[] {resource.getTitle()});
        action.addActionMessage(msg);
        log.debug(msg);
      } catch (RegistryException e) {
        // log as specific error message as possible about why the Registry error occurred
        String msg = RegistryException.logRegistryException(e.getType(), action);
        action.addActionError(msg);
        log.error(msg);

        // add error message that explains the consequence of the Registry error to user
        msg = action.getText("admin.config.updateMetadata.resource.fail.registry", new String[] {cfg.getRegistryUrl()});
        action.addActionError(msg);
        log.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      } catch (InvalidConfigException e) {
        String msg = action.getText("manage.overview.failed.resource.update", new String[] {e.getMessage()});
        action.addActionError(msg);
        log.error(msg);
        throw new PublicationException(PublicationException.TYPE.REGISTRY, msg, e);
      }
    }
  }

  public void visibilityToPrivate(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      // update visibility to public
      resource.setStatus(PublicationStatus.PRIVATE);

      // Changing the visibility, means some public things now need to be removed, e.g. IPT URL alt. id for resource!
      // This means the EML needs to be updated and saved
      updateAlternateIdentifierForIPTURLToResource(resource);

      // save all changes to resource
      save(resource);
    }
  }

  public void visibilityToPublic(Resource resource, BaseAction action) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      // update visibility to public
      resource.setStatus(PublicationStatus.PUBLIC);

      // Changing the visibility, means some public things now need to be added, e.g. IPT URL alt. id for resource!
      // This means the EML needs to be updated and saved
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
   * @return resource's StatusReport's list of TaskMessage or an empty list if no StatusReport exists for resource
   */
  private List<TaskMessage> getTaskMessages(String shortname) {
    return ((processReports.get(shortname)) == null) ? new ArrayList<TaskMessage>()
      : processReports.get(shortname).getMessages();
  }

  /**
   * Updates the date the resource is scheduled to be published next. The resource must have been configured with
   * a maintenance update frequency that is suitable for auto-publishing (annually, biannually, monthly, weekly,
   * daily), and have auto-publishing mode turned on for this update to take place.
   * 
   * @param resource resource
   * @throws PublicationException if the next published date cannot be set for any reason
   */
  private void updateNextPublishedDate(Resource resource) throws PublicationException {
    if (resource.usesAutoPublishing()) {
      try {
        log.debug("Updating next published date of resource: " + resource.getShortname());

        // get the time now, from this the next published date will be calculated
        Date now = new Date();

        // get update period in days
        int days = resource.getUpdateFrequency().getPeriodInDays();

        // calculate next published date
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, days);
        Date nextPublished = cal.getTime();

        // alert user that auto publishing has been turned on
        if (resource.getNextPublished() == null) {
          log.debug("Auto-publishing turned on");
        }

        // set next published date
        resource.setNextPublished(nextPublished);

        // log
        log.debug("The next publication date is: " + nextPublished.toString());
      } catch (Exception e) {
        // add error message that explains the consequence of the error to user
        String msg = "Auto-publishing failed: " + e.getMessage();
        log.error(msg, e);
        throw new PublicationException(PublicationException.TYPE.SCHEDULING, msg, e);
      }
    } else {
      log.debug("Resource: " + resource.getShortname() + " has not been configured to use auto-publishing");
    }
  }

  public void publicationModeToOff(Resource resource) {
    if (PublicationMode.AUTO_PUBLISH_OFF == resource.getPublicationMode()) {
      throw new InvalidConfigException(TYPE.AUTO_PUBLISHING_ALREADY_OFF,
        "Auto-publishing mode has already been switched off");
    } else if (PublicationMode.AUTO_PUBLISH_ON == resource.getPublicationMode()) {
      // update publicationMode to OFF
      resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
      // clear frequency
      resource.setUpdateFrequency(null);
      // clear next published date
      resource.setNextPublished(null);
      log.debug("Auto-publishing turned off");
      // save change to resource
      save(resource);
    }
  }

  /**
   * Try to add/update/remove KeywordSet for dataset type and subtype.
   * 
   * @param resource resource
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
        if (!Strings.isNullOrEmpty(type)) {
          EmlUtils.addOrUpdateKeywordSet(keywords, type, Constants.THESAURUS_DATASET_TYPE);
          log.debug("GBIF Dataset Type Vocabulary added/updated to Resource's list of keywords");
        }
        // its absence means that it must removed (if it exists)
        else {
          EmlUtils.removeKeywordSet(keywords, Constants.THESAURUS_DATASET_TYPE);
          log.debug("GBIF Dataset Type Vocabulary removed from Resource's list of keywords");
        }

        // add or update KeywordSet for dataset subtype
        String subtype = resource.getSubtype();
        if (!Strings.isNullOrEmpty(subtype)) {
          EmlUtils.addOrUpdateKeywordSet(keywords, subtype, Constants.THESAURUS_DATASET_SUBTYPE);
          log.debug("GBIF Dataset Subtype Vocabulary added/updated to Resource's list of keywords");
        }
        // its absence means that it must be removed (if it exists)
        else {
          EmlUtils.removeKeywordSet(keywords, Constants.THESAURUS_DATASET_SUBTYPE);
          log.debug("GBIF Dataset Type Vocabulary removed from Resource's list of keywords");
        }
      }
    }
    return resource;
  }

  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  public Map<String, Future<Integer>> getProcessFutures() {
    return processFutures;
  }

  public ListMultimap<String, Date> getProcessFailures() {
    return processFailures;
  }

  public boolean hasMaxProcessFailures(Resource resource) {
    if (processFailures.containsKey(resource.getShortname())) {
      List<Date> failures = processFailures.get(resource.getShortname());
      log.debug("Publication has failed " + String.valueOf(failures.size()) + " time(s) for resource: " + resource
        .getTitleAndShortname());
      if (failures.size() >= MAX_PROCESS_FAILURES) {
        return true;
      }
    }
    return false;
  }

  @VisibleForTesting
  public GenerateDwcaFactory getDwcaFactory() {
    return dwcaFactory;
  }
}
