package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.IptTerm;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.BasicMetadata;
import org.gbif.metadata.MetadataException;
import org.gbif.metadata.MetadataFactory;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import freemarker.template.TemplateException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager, ReportHandler {
  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  private final XStream xstream = new XStream();
  private SourceManager sourceManager;
  private ExtensionManager extensionManager;
  private RegistryManager registryManager;
  private RegistrationManager registrationManager;
  private ThreadPoolExecutor executor;
  private GenerateDwcaFactory dwcaFactory;
  private Map<String, Future<Integer>> processFutures = new HashMap<String, Future<Integer>>();
  private Map<String, StatusReport> processReports = new HashMap<String, StatusReport>();
  private Eml2Rtf eml2Rtf;

  @Inject
  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
      OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter,
      JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager, ExtensionManager extensionManager,
      RegistryManager registryManager, ConceptTermConverter conceptTermConverter, GenerateDwcaFactory dwcaFactory,
      PasswordConverter passwordConverter, RegistrationManager registrationManager, Eml2Rtf eml2Rtf) {
    super(cfg, dataDir);
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.registryManager = registryManager;
    this.registrationManager = registrationManager;
    this.dwcaFactory = dwcaFactory;
    this.eml2Rtf = eml2Rtf;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cfg.getMaxThreads());
    defineXstreamMapping(userConverter, orgConverter, extensionConverter, conceptTermConverter, jdbcInfoConverter,
        passwordConverter);
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
  }

  public boolean cancelPublishing(String shortname, BaseAction action) throws PublicationException {
    boolean canceled = false;
    // get future
    Future<Integer> f = processFutures.get(shortname);
    if (f != null) {
      canceled = f.cancel(true);
      if (canceled) {
        log.info("Publication of resource " + shortname + " canceled");
        // remove process from locking list
        processFutures.remove(shortname);
      } else {
        log.warn("Canceling publication of resource " + shortname + " failed");
      }
    }

    return canceled;
  }

  public synchronized void closeWriter(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        log.error(e);
      }
    }
  }

  private Eml convertMetadataToEml(BasicMetadata metadata, ActionLogger alog) {
    Eml eml = null;
    if (metadata != null) {
      if (metadata instanceof Eml) {
        eml = (Eml) metadata;
      } else {
        // copy properties
        eml = new Eml();
        eml.setTitle(metadata.getTitle());
        eml.setDescription(metadata.getDescription());
        eml.setHomeUrl(metadata.getHomepageUrl());
        eml.setLogoUrl(metadata.getLogoUrl());
        eml.setSubject(metadata.getSubject());
        eml.setPubDate(metadata.getPublished());
      }
      alog.info("Metadata imported.");
    }
    return eml;
  }

  private Eml copyMetadata(String shortname, File emlFile) throws ImportException {

    File emlFile2 = dataDir.resourceEmlFile(shortname, null);
    try {
      FileUtils.copyFile(emlFile, emlFile2);
    } catch (IOException e1) {
      log.error("Unnable to copy EML File", e1);
    }
    Eml eml = null;
    try {
      InputStream in = new FileInputStream(emlFile2);
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (IOException e) {
      log.error(e);
    } catch (SAXException e) {
      log.error("Invalid EML document", e);
    }

    if (eml == null) {
      throw new ImportException("Invalid EML document");
    }

    return eml;
  }

  public Resource create(String shortname, File dwca, User creator, BaseAction action) throws AlreadyExistingException,
      ImportException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // decompress archive
    File dwcaDir = dataDir.tmpDir();
    try {
      CompressionUtil.decompressFile(dwcaDir, dwca);
      return createFromArchive(shortname, dwcaDir, creator, alog);
    } catch (UnsupportedCompressionType e) {
      // try to read single eml file
      return createFromEml(shortname, dwca, creator, alog);
    } catch (AlreadyExistingException e) {
      throw e;
    } catch (ImportException e) {
      throw e;
    } catch (Exception e) {
      alog.warn(e);
      throw new ImportException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#create(java.lang.String)
   */
  public Resource create(String shortname, User creator) throws AlreadyExistingException {
    Resource res = null;
    if (shortname != null) {
      // check if existing already
      shortname = shortname.toLowerCase();
      if (resources.containsKey(shortname)) {
        throw new AlreadyExistingException();
      }
      res = new Resource();
      res.setShortname(shortname);
      res.setCreated(new Date());
      res.setCreator(creator);
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
      throws AlreadyExistingException, ImportException {
    Resource resource = null;
    try {
      // open the dwca with dwca reader
      Archive arch = ArchiveFactory.openArchive(dwca);
      // create new resource once we know the archive can be read
      resource = create(shortname, creator);
      // keep track of source files as an dwc archive might refer to the same source file multiple times
      Map<String, FileSource> sources = new HashMap<String, FileSource>();
      if (arch.getCore() != null) {
        // read core source+mappings
        FileSource s = importSource(alog, resource, arch.getCore());
        sources.put(arch.getCore().getLocation(), s);
        ExtensionMapping map = importMappings(alog, arch.getCore(), s);
        resource.addMapping(map);
        // read extension sources+mappings
        for (ArchiveFile ext : arch.getExtensions()) {
          if (sources.containsKey(ext.getLocation())) {
            s = sources.get(ext.getLocation());
            log.debug("Source " + s.getName() + " shared by multiple extensions");
          } else {
            s = importSource(alog, resource, ext);
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
          alog.info("manage.resource.create.success.nocore", new String[]{
              "" + resource.getSources().size(), "" + (resource.getMappings().size())});
        } else {
          alog.info("manage.resource.create.success", new String[]{
              resource.getCoreRowType(), "" + resource.getSources().size(), "" + (resource.getMappings().size())});
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

  private Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog)
      throws AlreadyExistingException, ImportException {
    // Eml eml = readMetadata(emlFile, alog);
    Eml eml = copyMetadata(shortname, emlFile);

    if (eml != null) {
      // create resource with imorted metadata
      Resource resource = create(shortname, creator);
      resource.setEml(eml);
      return resource;
    } else {
      alog.error("manage.resource.create.failed");
      throw new ImportException("Cant read the uploaded file");
    }
  }

  /**
   * @param passwordConverter
   * @param jdbcInfoConverter
   * @param conceptTermConverter
   * @param extensionConverter
   * @param orgConverter
   * @param userConverter
   */
  private void defineXstreamMapping(UserEmailConverter userConverter, OrganisationKeyConverter orgConverter,
      ExtensionRowTypeConverter extensionConverter, ConceptTermConverter conceptTermConverter,
      JdbcInfoConverter jdbcInfoConverter, PasswordConverter passwordConverter) {
    // xstream.setMode(XStream.NO_REFERENCES);

    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);
    xstream.alias("filesource", FileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", PropertyMapping.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "type");
    // make files transient to allow moving the datadir
    xstream.omitField(FileSource.class, "file");

    // persist only emails for users
    xstream.registerConverter(userConverter);
    // persist only rowtype
    xstream.registerConverter(extensionConverter);
    // persist only qualified concept name
    xstream.registerConverter(conceptTermConverter);
    // encrypt passwords
    xstream.registerConverter(passwordConverter);

    xstream.addDefaultImplementation(ExtensionProperty.class, ConceptTerm.class);
    xstream.addDefaultImplementation(DwcTerm.class, ConceptTerm.class);
    xstream.addDefaultImplementation(DcTerm.class, ConceptTerm.class);
    xstream.addDefaultImplementation(GbifTerm.class, ConceptTerm.class);
    xstream.addDefaultImplementation(IucnTerm.class, ConceptTerm.class);
    xstream.addDefaultImplementation(IptTerm.class, ConceptTerm.class);
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
   * @See isLocked for removing jobs from internal maps
   * @param resource
   * @param alog
   */
  private void generateDwca(Resource resource, ActionLogger alog) {
    // use threads to run in the background as sql sources might take a long time
    GenerateDwca worker = dwcaFactory.create(resource, this);
    Future<Integer> f = executor.submit(worker);
    processFutures.put(resource.getShortname(), f);
    // make sure we have at least a first report for this resource
    worker.report();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#get(java.lang.String)
   */
  public Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  /*
   * Returns the size of the DwC-A file using the dataDir.
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#getDwcaSize(org.gbif.ipt.model.Resource)
   */
  public long getDwcaSize(Resource resource) {
    File data = dataDir.resourceDwcaFile(resource.getShortname());
    long size = data.length();
    return size;
  }

  private int getEmlHash(Resource resource, Eml eml) {
    // TODO: replace by hashing the eml xml file content?
    // Alternatively code a proper hashCode method for Eml that needs to be maintained - might be too much effort
    return eml.hashCode();
  }

  /*
   * Returns the size of the EML file using the dataDir
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#getEmlSize(org.gbif.ipt.model.Resource)
   */
  public long getEmlSize(Resource resource) {
    File data = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
    return data.length();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#getResourceLink(java.lang.String)
   */
  public URL getResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseURL() + "/resource.do?id=" + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  /*
   * Returns the size of the RTF file using the dataDir.
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#getRtfSize(org.gbif.ipt.model.Resource)
   */
  public long getRtfSize(Resource resource) {
    File data = dataDir.resourceRtfFile(resource.getShortname());
    return data.length();
  }

  private ExtensionMapping importMappings(ActionLogger alog, ArchiveFile af, Source source) {
    ExtensionMapping map = new ExtensionMapping();
    map.setSource(source);
    Extension ext = extensionManager.get(af.getRowType());
    if (ext == null) {
      alog.warn("manage.resource.create.rowType.null", new String[]{af.getRowType()});
      return null;
    }
    map.setExtension(ext);

    // set ID column
    map.setIdColumn(af.getId().getIndex());

    Set<PropertyMapping> fields = new HashSet<PropertyMapping>();
    // iterate over each field to make sure its part of the extension we know
    for (ArchiveField f : af.getFields().values()) {
      if (ext.hasProperty(f.getTerm())) {
        fields.add(new PropertyMapping(f));
      } else {
        alog.warn("manage.resource.create.mapping.concept.skip",
            new String[]{f.getTerm().qualifiedName(), ext.getRowType()});
      }
    }
    map.setFields(fields);

    return map;
  }

  private FileSource importSource(ActionLogger alog, Resource config, ArchiveFile af) throws ImportException {
    File extFile = af.getLocationFile();
    FileSource s = sourceManager.add(config, extFile, af.getLocation());
    SourceManagerImpl.copyArchiveFileProperties(af, s);

    // the number of rows was calculated using the standard file importer
    // make an adjustment now that the exact number of header rows are known
    if (s.getIgnoreHeaderLines() != 1) {
      log.info("Adjusting row count to " + (s.getRows() + 1 - s.getIgnoreHeaderLines()) + " from " + +s.getRows()
          + " since header count is declared as " + s.getIgnoreHeaderLines());
    }
    s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());

    return s;
  }

  /*
   * Checks if a resource is locked due some background processing.
   * While doing so it checks the known futures for completion.
   * If completed the resource is updated with the status messages and the lock is removed.
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#isLocked(java.lang.String)
   */
  public boolean isLocked(String shortname) {
    if (processFutures.containsKey(shortname)) {
      // is listed as locked but task might be finished, check
      Future<Integer> f = processFutures.get(shortname);
      if (f.isDone()) {
        try {
          Integer coreRecords = f.get();
          Resource res = get(shortname);
          res.setRecordsPublished(coreRecords);
          save(res);
          return false;
        } catch (InterruptedException e) {
          log.info("Process interrupted for resource " + shortname);
        } catch (CancellationException e) {
          log.info("Process canceled for resource " + shortname);
        } catch (ExecutionException e) {
          log.error("Process for resource " + shortname + " aborted due to error: " + e.getMessage());
        } finally {
          processFutures.remove(shortname);
        }
      }
      return true;
    }
    return false;
  }

  public boolean isRtfExisting(String shortName) {
    File rtfFile = dataDir.resourceRtfFile(shortName);
    return rtfFile.exists();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#latest(int, int)
   */
  public List<Resource> latest(int startPage, int pageSize) {
    List<Resource> resourceList = new ArrayList<Resource>();
    for (Resource resource : resources.values()) {
      if (!(resource.getStatus().equals(PublicationStatus.PRIVATE))) {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.voc.PublicationStatus)
   */
  public List<Resource> list(PublicationStatus status) {
    List<Resource> result = new ArrayList<Resource>();
    for (Resource r : resources.values()) {
      if (r.getStatus() == status) {
        result.add(r);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.User)
   */
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
    int counter = 0;
    resources.clear();
    if (resourcesDir != null) {
      File[] resources = resourcesDir.listFiles();
      if (resources != null) {
        for (File resourceDir : resources) {
          if (resourceDir.isDirectory()) {
            try {
              addResource(loadFromDir(resourceDir));
              counter++;
            } catch (InvalidConfigException e) {
              log.error("Cant load resource " + resourceDir.getName(), e);
            }
          }
        }
        log.info("Loaded " + counter + " resources into memory alltogether.");
      } else {
        log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
      }
    } else {
      log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
    }
    return counter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#getEml(java.lang.String)
   */
  private Eml loadEml(Resource resource) {
    File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    Eml eml = null;
    try {
      InputStream in = new FileInputStream(emlFile);
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (IOException e) {
      log.error(e);
    } catch (SAXException e) {
      log.error("Invalid EML document", e);
      eml = new Eml();
    } catch (Exception e) {
      eml = new Eml();
    }
    resource.setEml(eml);
    syncEmlWithResource(resource);
    return eml;
  }

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache
   * 
   * @param resourceDir
   * @return
   */
  private Resource loadFromDir(File resourceDir) throws InvalidConfigException {
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

        // shortname persists as folder name, so xstream doesnt handle this:
        resource.setShortname(shortname);
        // add proper source file pointer
        for (Source src : resource.getSources()) {
          src.setResource(resource);
          if (src instanceof FileSource) {
            ((FileSource) src).setFile(dataDir.sourceFile(resource, src));
          }
        }
        // load eml
        loadEml(resource);
        log.debug("Read resource configuration for " + shortname);
        return resource;
      } catch (FileNotFoundException e) {
        log.error("Cannot read resource configuration for " + shortname, e);
        throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot read resource configuration for " + shortname
            + ": " + e.getMessage());
      }
    }
    return null;
  }

  public boolean publish(Resource resource, BaseAction action) throws PublicationException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname()
          + " is currently locked by another process");
    }

    // publish EML as well as RTF
    publishMetadata(resource, action);

    // regenerate dwca asynchronously
    boolean dwca = false;
    if (resource.hasMappedData()) {
      generateDwca(resource, alog);
      dwca = true;
      // make sure the dwca service is registered
      // this might not have been the case when the first dwca is created, but the resource was already registered
// before
      if (resource.isRegistered()) {
        registryManager.updateResource(resource, registrationManager.getIpt());
      }
    } else {
      resource.setRecordsPublished(0);
    }

    // persist any resource object changes
    resource.setLastPublished(new Date());
    save(resource);
    return dwca;
  }

  public void publishMetadata(Resource resource, BaseAction action) throws PublicationException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname()
          + " is currently locked by another process");
    }

    // increase eml version
    int version = resource.getEmlVersion();
    version++;
    resource.setEmlVersion(version);
    saveEml(resource);
    // copy stable version of the eml file
    File trunkFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);
    try {
      FileUtils.copyFile(trunkFile, versionedFile);
    } catch (IOException e) {
      alog.error("Can't publish resource " + resource.getShortname(), e);
      throw new PublicationException(PublicationException.TYPE.EML, "Can't publish eml file for resource "
          + resource.getShortname(), e);
    }
    // publish also as RTF
    publishRtf(resource, action);

    // copy current rtf version.
    File trunkRtfFile = dataDir.resourceRtfFile(resource.getShortname());
    File versionedRtfFile = dataDir.resourceRtfFile(resource.getShortname(), version);
    try {
      FileUtils.copyFile(trunkRtfFile, versionedRtfFile);
    } catch (IOException e) {
      alog.error("Can't publish resource " + resource.getShortname() + "as RTF", e);
      throw new PublicationException(PublicationException.TYPE.EML, "Can't publish rtf file for resource "
          + resource.getShortname(), e);
    }
  }

  private void publishRtf(Resource resource, BaseAction action) {
    ActionLogger alog = new ActionLogger(this.log, action);
    Document doc = new Document();
    File rtfFile = dataDir.resourceRtfFile(resource.getShortname());
    OutputStream out;
    try {
      out = new FileOutputStream(rtfFile);
      RtfWriter2.getInstance(doc, out);
      eml2Rtf.writeEmlIntoRtf(doc, resource, action);
      out.close();
    } catch (FileNotFoundException e) {
      alog.error("Cant find rtf file to write metadata to: " + rtfFile.getAbsolutePath(), e);
    } catch (DocumentException e) {
      alog.error("RTF DocumentException while writing to file " + rtfFile.getAbsolutePath(), e);
    } catch (IOException e) {
      alog.error("Cant write to rtf file " + rtfFile.getAbsolutePath(), e);
    }
  }

  private Eml readMetadata(File file, ActionLogger alog) {
    MetadataFactory fact = new MetadataFactory();
    try {
      return convertMetadataToEml(fact.read(file), alog);
    } catch (MetadataException e) {
      // swallow;
    }
    return null;
  }

  private Eml readMetadata(String shortname, Archive archive, ActionLogger alog) {
    Eml eml = null;
    File emlFile = archive.getMetadataLocationFile();
    try {
      if (emlFile == null || !emlFile.exists()) {
        // some archives dont indicate the name of the eml metadata file
        // so we also try with the default eml.xml name
        emlFile = new File(archive.getLocation(), "eml.xml");
      }
      if (emlFile.exists()) {
        // InputStream emlIn = new FileInputStream(emlFile);
        // eml = EmlFactory.build(emlIn);
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
      eml = convertMetadataToEml(archive.getMetadata(), alog);
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
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public void register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
    if (PublicationStatus.REGISTERED != resource.getStatus()) {
      UUID key = registryManager.register(resource, organisation, ipt);
      if (key == null) {
        throw new RegistryException(RegistryException.TYPE.MISSING_METADATA, "No key returned for registered resoruce.");
      }
      resource.setStatus(PublicationStatus.REGISTERED);
      save(resource);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.task.ReportHandler#report(org.gbif.ipt.task.StatusReport)
   */
  public synchronized void report(String shortname, StatusReport report) {
    processReports.put(shortname, report);
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
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant write mapping configuration");
    } finally {
      if (writer != null) {
        closeWriter(writer);
      }
      System.gc();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
   */
  public synchronized void saveEml(Resource resource) throws InvalidConfigException {
    // udpate EML with latest resource basics
    syncEmlWithResource(resource);
    // save into data dir
    File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
    try {
      EmlWriter.writeEmlFile(emlFile, resource.getEml());
      log.debug("Updated EML file for " + resource);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource);
    } catch (TemplateException e) {
      log.error("EML template exception", e);
      throw new InvalidConfigException(TYPE.EML, "EML template exception when writing eml for " + resource + ": "
          + e.getMessage());
    }
  }

  public StatusReport status(String shortname) {
    isLocked(shortname);
    return processReports.get(shortname);
  }

  private void syncEmlWithResource(Resource resource) {
    resource.getEml().setEmlVersion(resource.getEmlVersion());
    // we need some GUID. If we have use the registry key, if not use the resource URL
    if (resource.getKey() != null) {
      resource.getEml().setGuid(resource.getKey().toString());
    } else {
      resource.getEml().setGuid(getResourceLink(resource.getShortname()).toString());
    }
  }

  public void updateDwcaEml(Resource resource, BaseAction action) throws PublicationException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname()
          + " is currently locked by another process");
    }

    if (!resource.hasPublishedData()) {
      throw new PublicationException(PublicationException.TYPE.DWCA, "Resource " + resource.getShortname()
          + " has no published data - can't update a non-existent dwca.");
    }

    try {
      // tmp directory to work in
      File dwcaFolder = dataDir.tmpDir();
      if (log.isDebugEnabled()) {
        log.debug("Using tmp dir [" + dwcaFolder.getAbsolutePath() + "]");
      }

      // the latest files
      File dwcaFile = dataDir.resourceDwcaFile(resource.getShortname());
      if (log.isDebugEnabled()) {
        log.debug("Using dwca file [" + dwcaFile.getAbsolutePath() + "]");
      }
      File emlFile = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
      if (log.isDebugEnabled()) {
        log.debug("Using eml file [" + emlFile.getAbsolutePath() + "]");
      }

      // unzip, copy the eml file in, rezip
      CompressionUtil.unzipFile(dwcaFolder, dwcaFile);
      if (log.isDebugEnabled()) {
        log.debug("Copying new eml file [" + emlFile.getAbsolutePath() + "] to [" + dwcaFolder.getAbsolutePath()
            + "] as eml.xml");
      }
      FileUtils.copyFile(emlFile, new File(dwcaFolder, "eml.xml"));
      File zip = dataDir.tmpFile("dwca", ".zip");
      CompressionUtil.zipDir(dwcaFolder, zip);

      // move to data dir
      dwcaFile.delete();
      FileUtils.moveFile(zip, dwcaFile);
    } catch (IOException e) {
      alog.error("Can't update dwca for resource " + resource.getShortname(), e);
      throw new PublicationException(PublicationException.TYPE.DWCA, "Could not process dwca file for resource ["
          + resource.getShortname() + "]");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#updateRegistration(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public void updateRegistration(Resource resource, Ipt ipt) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      log.debug("Updating resource with key: " + resource.getKey().toString());
      registryManager.updateResource(resource, ipt);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#unpublish(org.gbif.ipt.model.Resource)
   */
  public void visibilityToPrivate(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      resource.setStatus(PublicationStatus.PRIVATE);
      save(resource);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.manage.ResourceManager#publish(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.voc.PublicationStatus)
   */
  public void visibilityToPublic(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      resource.setStatus(PublicationStatus.PUBLIC);
      save(resource);
    }
  }
}