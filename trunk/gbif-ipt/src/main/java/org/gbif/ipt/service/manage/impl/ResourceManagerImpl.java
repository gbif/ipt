package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
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
import org.gbif.ipt.service.admin.VocabulariesManager;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager, ReportHandler {

  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  public static final String RESOURCE_IDENTIFIER_LINK_PART = "/resource.do?id=";
  public static final String RESOURCE_PUBLIC_LINK_PART = "/resource.do?r=";
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
  private VocabulariesManager vocabManager;

  @Inject
  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
    OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter,
    JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager, ExtensionManager extensionManager,
    RegistryManager registryManager, ConceptTermConverter conceptTermConverter, GenerateDwcaFactory dwcaFactory,
    PasswordConverter passwordConverter, RegistrationManager registrationManager, Eml2Rtf eml2Rtf,
    VocabulariesManager vocabManager) {
    super(cfg, dataDir);
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.registryManager = registryManager;
    this.registrationManager = registrationManager;
    this.dwcaFactory = dwcaFactory;
    this.eml2Rtf = eml2Rtf;
    this.vocabManager = vocabManager;
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
        eml.setHomepageUrl(metadata.getHomepageUrl());
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

  public Resource create(String shortname, File dwca, User creator, BaseAction action)
    throws AlreadyExistingException, ImportException {
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
    Resource resource;
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
        String coreRowType = arch.getCore().getRowType();
        // Set the coreType for the resource
        if (coreRowType != null) {
          resource.setCoreType(StringUtils.capitalize(CoreRowType.OCCURRENCE.toString().toLowerCase()));
          if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
            resource.setCoreType(StringUtils.capitalize(CoreRowType.CHECKLIST.toString().toLowerCase()));
          }
        }
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
   * @see #isLocked(String) for removing jobs from internal maps
   */
  private void generateDwca(Resource resource, ActionLogger alog) {
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

  /**
   * Returns the size of the DwC-A file using the dataDir.
   */
  public long getDwcaSize(Resource resource) {
    File data = dataDir.resourceDwcaFile(resource.getShortname());
    return data.length();
  }

  private int getEmlHash(Resource resource, Eml eml) {
    // TODO: replace by hashing the eml xml file content?
    // Alternatively code a proper hashCode method for Eml that needs to be maintained - might be too much effort
    return eml.hashCode();
  }

  /**
   * Returns the size of the EML file using the dataDir.
   */
  public long getEmlSize(Resource resource) {
    File data = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
    return data.length();
  }

  /**
   * Construct a resource link (identifier) using its shortname and return it.
   *
   * @param shortname resource shortname
   *
   * @return resource (identifier) link
   */
  public URL getResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseUrl() + RESOURCE_IDENTIFIER_LINK_PART + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  /**
   * Construct a public resource link using its shortname and return it.
   *
   * @param shortname resource shortname
   *
   * @return public resource link
   */
  public URL getPublicResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseUrl() + RESOURCE_PUBLIC_LINK_PART + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  /**
   * Returns the size of the RTF file using the dataDir.
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
      alog.warn("manage.resource.create.rowType.null", new String[] {af.getRowType()});
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
          new String[] {f.getTerm().qualifiedName(), ext.getRowType()});
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

  /**
   * Checks if a resource is locked due some background processing.
   * While doing so it checks the known futures for completion.
   * If completed the resource is updated with the status messages and the lock is removed.
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
   * and returns the Resource instance for the internal in memory cache.
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
            ((FileSource) src).setFile(dataDir.sourceFile(resource, src));
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
   *
   * @return resource with coreType set if it could be inferred, or unchanged if it couldn't be inferred.
   */
  Resource inferCoreType(Resource resource) {
    if (resource != null && resource.getCoreRowType() != null) {
      if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(CoreRowType.OCCURRENCE.toString().toLowerCase());
      } else if (Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(CoreRowType.CHECKLIST.toString().toLowerCase());
      }
    } else {
      // don't do anything - no taxon or occurrence mapping has been done yet
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
        }
      }
      // if the subtype doesn't use a standardized term from the vocab, it's reset to null
      if (!usesVocab) {
        resource.setSubtype(null);
      }
    }
    return resource;
  }

  public boolean publish(Resource resource, BaseAction action) throws PublicationException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    // publish EML as well as RTF
    publishMetadata(resource, action);

    // regenerate dwca asynchronously
    boolean dwca = false;
    if (resource.hasMappedData()) {
      generateDwca(resource, alog);
      dwca = true;
    } else {
      resource.setRecordsPublished(0);
    }

    // persist any resource object changes
    resource.setLastPublished(new Date());

    // ensure alternate identifier for Registry UUID is set
    updateAlternateIdentifierForRegistry(resource);
    // ensure alternate identifier for IPT URL to resource is set
    updateAlternateIdentifierForIPTURLToResource(resource);

    save(resource);
    return dwca;
  }

  /**
   * Updates the resource's alternate identifier for its corresponding Registry UUID.
   * If registered the method ensures that it won't be added a second time, with only 1 ever being set. Ideally this
   * would happen the very first time that the resource gets registered, however, to accommodate updates from older
   * versions of the IPT, it can updated every time the resource gets published.
   *
   * @param resource resource
   *
   * @return resource with Registry UUID for the resource updated
   */
  public Resource updateAlternateIdentifierForRegistry(Resource resource) {
    // retrieve a list of the resource's alternate identifiers
    List<String> ids = null;
    if (resource.getEml() != null) {
      ids = resource.getEml().getAlternateIdentifiers();
    } else {
      resource.setEml(new Eml());
    }

    if (resource.isRegistered()) {
      // GBIF Registry UUID
      UUID key = resource.getKey();
      if (key != null && ids != null && !ids.contains(key.toString())) {
        resource.getEml().getAlternateIdentifiers().add(key.toString());
        log.info("GBIF Registry UUID added to Resource's list of alternate identifiers");
      }
    }
    return resource;
  }

  /**
   * Updates the resource's alternative identifier for the IPT URL to the resource.
   * This identifier should only exist for the resource, if its visibility is public.
   * If the resource visibility is set to private, this method should be called to ensure the identifier is removed.
   * Any time the baseURL changes, all resources will need to be republished and in this case, this identifier
   * will be updated. This method will remove an IPT URL identifier with the wrong baseURL by matching the
   * RESOURCE_PUBLIC_LINK_PART, updating it with one having the latest baseURL.
   *
   * @param resource resource
   *
   * @return resource with the IPT URL alternate identifier for the resource updated
   */
  public Resource updateAlternateIdentifierForIPTURLToResource(Resource resource) {
    // retrieve a list of the resource's alternate identifiers
    List<String> ids = null;
    if (resource.getEml() != null) {
      ids = resource.getEml().getAlternateIdentifiers();
    } else {
      resource.setEml(new Eml());
    }

    // has this been added before, perhaps with a different baseURL?
    boolean exists = false;
    String existingId = null;
    if (ids != null) {
      for (String id : ids) {
        // try to match resource.do?r=shortname
        if (id.contains(RESOURCE_PUBLIC_LINK_PART)) {
          exists = true;
          existingId = id;
        }
      }
    }

    // if the resource is PUBLIC, or REGISTERED
    if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) != 0) {
      URL url = getPublicResourceLink(resource.getShortname());
      // if the URL is not null, and the identifier does not exist yet - add it!
      if (url != null && !exists) {
        resource.getEml().getAlternateIdentifiers().add(url.toString());
        log.info("IPT URL to resource added to Resource's list of alternate ids");
      }
    }
    // otherwise if the resource is PRIVATE
    else if (resource.getStatus().compareTo(PublicationStatus.PRIVATE) == 0) {
      // no public resource alternate identifier can exist if the resource visibility is private - remove it if app.
      if (exists) {
        ids.remove(existingId);
        log.warn("Following visibility change, IPT URL to resource removed from Resource's list of alternate ids");
      }
    }
    return resource;
  }

  public void publishMetadata(Resource resource, BaseAction action) throws PublicationException {
    ActionLogger alog = new ActionLogger(this.log, action);
    // check if publishing task is already running
    if (isLocked(resource.getShortname())) {
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
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
      throw new PublicationException(PublicationException.TYPE.EML,
        "Can't publish eml file for resource " + resource.getShortname(), e);
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
      throw new PublicationException(PublicationException.TYPE.EML,
        "Can't publish rtf file for resource " + resource.getShortname(), e);
    }
  }

  private void publishRtf(Resource resource, BaseAction action) {
    ActionLogger alog = new ActionLogger(this.log, action);
    Document doc = new Document();
    File rtfFile = dataDir.resourceRtfFile(resource.getShortname());
    try {
      OutputStream out = new FileOutputStream(rtfFile);
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
    Eml eml;
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
   * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public void register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
    if (PublicationStatus.REGISTERED != resource.getStatus()) {
      UUID key = registryManager.register(resource, organisation, ipt);
      if (key == null) {
        throw new RegistryException(RegistryException.TYPE.MISSING_METADATA,
          "No key returned for registered resoruce.");
      }
      resource.setStatus(PublicationStatus.REGISTERED);

      // ensure alternate identifier for Registry UUID set
      updateAlternateIdentifierForRegistry(resource);

      // save all changes to resource
      save(resource);
    }
  }

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
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write mapping configuration");
    } finally {
      if (writer != null) {
        closeWriter(writer);
      }
      System.gc();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
   */
  public synchronized void saveEml(Resource resource) throws InvalidConfigException {
    // udpate EML with latest resource basics
    syncEmlWithResource(resource);
    // set modified date
    resource.setModified(new Date());
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
      throw new InvalidConfigException(TYPE.EML,
        "EML template exception when writing eml for " + resource + ": " + e.getMessage());
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
      throw new PublicationException(PublicationException.TYPE.LOCKED,
        "Resource " + resource.getShortname() + " is currently locked by another process");
    }

    if (!resource.hasPublishedData()) {
      throw new PublicationException(PublicationException.TYPE.DWCA,
        "Resource " + resource.getShortname() + " has no published data - can't update a non-existent dwca.");
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
      throw new PublicationException(PublicationException.TYPE.DWCA,
        "Could not process dwca file for resource [" + resource.getShortname() + "]");
    }
  }

  public void updateRegistration(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      log.debug("Updating resource with key: " + resource.getKey().toString());
      registryManager.updateResource(resource);
    }
  }

  public void visibilityToPrivate(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      // update visibility to public
      resource.setStatus(PublicationStatus.PRIVATE);

      // ensure the alternate id for the IPT URL to the resource is removed!
      updateAlternateIdentifierForIPTURLToResource(resource);

      // save all changes to resource
      save(resource);
    }
  }

  public void visibilityToPublic(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED,
        "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      // update visibility to public
      resource.setStatus(PublicationStatus.PUBLIC);

      // ensure the alternate id for the IPT URL to the resource is updated
      updateAlternateIdentifierForIPTURLToResource(resource);

      // save all changes to resource
      save(resource);
    }
  }
}
