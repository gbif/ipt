package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.CompressionUtil;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceConfiguration;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import freemarker.template.TemplateException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager {
  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  public static final String EML_FILE = "eml.xml";
  private final XStream xstream = new XStream();
  private final UserEmailConverter userConverter;
  private final OrganisationKeyConverter orgConverter;
  private final ExtensionRowTypeConverter extensionConverter;
  private final JdbcInfoConverter jdbcInfoConverter;
  private GBIFRegistryManager registryManager;
  private SourceManager sourceManager;
  private ExtensionManager extensionManager;
  private RegistryManager registryManager2;

  @Inject
  public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
      OrganisationKeyConverter orgConverter, GBIFRegistryManager registryManager,
      ExtensionRowTypeConverter extensionConverter, JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager,
      ExtensionManager extensionManager, RegistryManager registryManager2) {
    super(cfg, dataDir);
    this.userConverter = userConverter;
    this.registryManager = registryManager;
    this.orgConverter = orgConverter;
    this.extensionConverter = extensionConverter;
    this.jdbcInfoConverter = jdbcInfoConverter;
    this.sourceManager = sourceManager;
    this.extensionManager = extensionManager;
    this.registryManager2=registryManager2;
    defineXstreamMapping();
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
  }

  public ResourceConfiguration create(String shortname, File dwca, User creator, BaseAction action)
      throws AlreadyExistingException, ImportException {
    ResourceConfiguration config;
    ActionLogger alog = new ActionLogger(this.log, action);
    try {
      // decompress archive
      File dwcaDir = dataDir.tmpDir();
      CompressionUtil.decompressFile(dwcaDir, dwca);
      // open the dwca with dwca reader
      Archive arch = ArchiveFactory.openArchive(dwcaDir);
      // create new resource once we know the archive can be read
      config = create(shortname, creator);
      // keep track of source files as an dwc archive might refer to the same source file multiple times
      Map<String, FileSource> sources = new HashMap<String, FileSource>();
      if (arch.getCore() != null) {
        // read core source+mappings
        FileSource s = importSource(alog, config, arch.getCore());
        sources.put(arch.getCore().getLocation(), s);
        ExtensionMapping map = importMappings(alog, arch.getCore(), s);
        config.setCore(map);
        // read extension sources+mappings
        for (ArchiveFile ext : arch.getExtensions()) {
          if (sources.containsKey(ext.getLocation())) {
            s = sources.get(ext.getLocation());
            log.debug("Source " + s.getName() + " shared by multiple extensions");
          } else {
            s = importSource(alog, config, ext);
            sources.put(ext.getLocation(), s);
          }
          map = importMappings(alog, ext, s);
          config.addExtension(map);
        }
        // finally persist the whole thing
        save(config);
        alog.info("Imported existing darwin core archive with core row type " + config.getCoreRowType() + " and "
            + config.getSources().size() + " source(s), " + config.getExtensions().size() + " mapping(s)");
      } else {
        alog.warn("Darwin core archive is invalid and does not have a core mapping");
        throw new ImportException("Darwin core archive is invalid and does not have a core mapping");
      }
    } catch (UnsupportedArchiveException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    } catch (IOException e) {
      alog.warn(e.getMessage(), e);
      throw new ImportException(e);
    }
    return config;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#create(java.lang.String)
   */
  public ResourceConfiguration create(String shortname, User creator) throws AlreadyExistingException {
    ResourceConfiguration config = null;
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
      config = new ResourceConfiguration();
      config.setResource(res);
      // create dir
      try {
        save(config);
        log.info("Created resource " + res.getShortname());
      } catch (InvalidConfigException e) {
        log.error("Error creating resource", e);
        return null;
      }
    }
    return config;
  }

  /**
	 * 
	 */
  private void defineXstreamMapping() {
    xstream.alias("resource", Resource.class);
    xstream.alias("user", User.class);
    xstream.alias("config", ResourceConfiguration.class);
    xstream.alias("filesource", FileSource.class);
    xstream.alias("sqlsource", SqlSource.class);
    xstream.alias("mapping", ExtensionMapping.class);
    xstream.alias("field", ArchiveField.class);

    // transient properties
    xstream.omitField(Resource.class, "shortname");
    xstream.omitField(Resource.class, "title");
    xstream.omitField(Resource.class, "description");
    xstream.omitField(Resource.class, "type");
    // make files transient to allow moving the datadir
    xstream.omitField(FileSource.class, "file");
    // persist only emails for users
    xstream.registerConverter(userConverter);
    xstream.registerConverter(orgConverter);
    xstream.registerConverter(jdbcInfoConverter);
    // persist only rowtype for extensions
    // TODO: replace with full ExtensionMappingConverter
    xstream.registerConverter(extensionConverter);
  }

  public void delete(Resource resource) throws IOException {
    // remove from data dir
    FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
    // remove object
    resources.remove(resource.getShortname().toLowerCase());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#get(java.lang.String)
   */
  public Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  public ResourceConfiguration getConfig(String shortname) {
    try {
      File cfgFile = dataDir.resourceFile(shortname, PERSISTENCE_FILE);
      InputStream input = new FileInputStream(cfgFile);
      ResourceConfiguration config = (ResourceConfiguration) xstream.fromXML(input);
      // shortname persists as folder name, so xstream doesnt handle this:
      config.getResource().setShortname(shortname);
      // add proper source file pointer
      for (Source src : config.getSources()) {
        src.setResource(config.getResource());
        if (src instanceof FileSource) {
          ((FileSource) src).setFile(dataDir.sourceFile(config.getResource(), src));
        }
      }
      log.debug("Read resource configuration for " + shortname);
      return config;
    } catch (FileNotFoundException e) {
      log.error("Cannot read resource configuration for " + shortname, e);
      throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot read resource configuration for " + shortname
          + ": " + e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#getEml(java.lang.String)
   */
  public Eml getEml(Resource resource) {
    File emlFile = dataDir.resourceFile(resource, EML_FILE);
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
    }

    syncEmlWithResource(resource, eml);
    return eml;
  }

  /*
   * (non-Javadoc)
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
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#getResources()
   */
  public Map<String, Resource> getResources() {
    return resources;
  }

  private ExtensionMapping importMappings(ActionLogger alog, ArchiveFile af, Source source) {
    ExtensionMapping map = new ExtensionMapping();
    map.setSource(source);
    Extension ext = extensionManager.get(af.getRowType());
    if (ext == null) {
      alog.warn("RowType " + af.getRowType() + " not available in this IPT installation");
      return null;
    }
    map.setExtension(ext);

    Set<ArchiveField> fields = new HashSet<ArchiveField>();
    // iterate over each field to make sure its part of the extension we know
    for (ArchiveField f : af.getFields().values()) {
      if (ext.hasProperty(f.getTerm())) {
        fields.add(f);
      } else {
        alog.info("Skip mapped concept term " + f.getTerm().qualifiedName() + " which is unkown to extension "
            + ext.getRowType());
      }
    }
    map.setFields(fields);

    return map;
  }

  private FileSource importSource(ActionLogger alog, ResourceConfiguration config, ArchiveFile af)
      throws ImportException {
    File extFile = new File(af.getLocation());
    FileSource s = sourceManager.add(config, extFile);
    SourceManagerImpl.copyArchiveFileProperties(af, s);
    return s;
  }

  /*
   * (non-Javadoc)
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
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.User)
   */
  public List<Resource> list(User user) {
    // TODO: select basedon user rights - for testing return all resources for now
    List<Resource> result = new ArrayList<Resource>(resources.values());
    return result;
  }

  public int load() {
    File extensionDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
    int counter = 0;
    resources.clear();
    for (File resourceDir : extensionDir.listFiles()) {
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
    return counter;
  }

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder and returns only
   * the basic Resource instance for the internal in memory cache
   * 
   * @param resourceDir
   * @return
   */
  private Resource loadFromDir(File resourceDir) throws InvalidConfigException {
    if (!resourceDir.exists()) {
      return null;
    } else {
      // load full configuration from resource.xml and eml.xml files
      ResourceConfiguration config = getConfig(resourceDir.getName());
      Eml eml = getEml(config.getResource());
      syncEmlWithResource(config.getResource(), eml);
      return config.getResource();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#publish(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.voc.PublicationStatus)
   */
  public void publish(ResourceConfiguration config) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == config.getResource().getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == config.getResource().getStatus()) {
      config.getResource().setStatus(PublicationStatus.PUBLIC);
      save(config);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public void register(ResourceConfiguration config, Organisation organisation, Ipt ipt) throws RegistryException {
    if (PublicationStatus.REGISTERED != config.getResource().getStatus()) {
      registryManager2.setRegistryCredentials(organisation.getKey().toString(), organisation.getPassword());
      UUID key = registryManager2.register(config.getResource(), organisation, ipt);
      if (key == null) {
        throw new RegistryException(RegistryException.TYPE.MISSING_METADATA, "No key returned for registered resoruce.");
      }
      config.getResource().setKey(key);
      config.getResource().setOrganisation(organisation);
      config.getResource().setStatus(PublicationStatus.REGISTERED);
      save(config);
    }
  }

  public void save(ResourceConfiguration config) throws InvalidConfigException {
    File cfgFile = dataDir.resourceFile(config.getResource(), PERSISTENCE_FILE);
    try {
      // make sure resource dir exists
      FileUtils.forceMkdir(cfgFile.getParentFile());
      // persist data
      Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
      xstream.toXML(config, writer);
      // add to internal map
      addResource(config.getResource());
      log.debug("Saved " + config);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant write mapping configuration");
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
   */
  public void saveEml(Resource resource, Eml eml) throws InvalidConfigException {
    // udpate EML with latest resource basics
    syncEmlWithResource(resource, eml);
    // save into data dir
    File emlFile = dataDir.resourceFile(resource, EML_FILE);
    try {
      EmlWriter.writeEmlFile(emlFile, eml);
      log.debug("Updated EML file for " + resource);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource);
    } catch (TemplateException e) {
      log.error("EML template exception", e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "EML template exception when writing eml for " + resource);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#search(java.lang.String, org.gbif.ipt.model.voc.ResourceType)
   */
  public List<Resource> search(String q, String type) {
    // TODO: do real search - for testing return all resources for now
    return new ArrayList<Resource>(resources.values());
  }

  private void syncEmlWithResource(Resource resource, Eml eml) {
    resource.setTitle(eml.getTitle());
    resource.setDescription(eml.getDescription());
    // we need some GUID. If we have use the registry key, if not use the resource URL
    if (resource.getKey() != null) {
      eml.setGuid(resource.getKey().toString());
    } else {
      eml.setGuid(getResourceLink(resource.getShortname()).toString());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#unpublish(org.gbif.ipt.model.Resource)
   */
  public void unpublish(ResourceConfiguration config) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == config.getResource().getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == config.getResource().getStatus()) {
      config.getResource().setStatus(PublicationStatus.PRIVATE);
      save(config);
    }
  }
}
