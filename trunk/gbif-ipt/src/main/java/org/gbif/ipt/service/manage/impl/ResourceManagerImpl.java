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
import org.gbif.file.CompressionUtil;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.model.converter.ConceptTermConverter;
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
import java.util.Collections;
import java.util.Comparator;
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
	private final ConceptTermConverter conceptTermConverter;
	private final JdbcInfoConverter jdbcInfoConverter;
	private SourceManager sourceManager;
	private ExtensionManager extensionManager;
	private GBIFRegistryManager registryManager;
	private RegistryManager registryManager2;

	@Inject
	public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter,
			OrganisationKeyConverter orgConverter, GBIFRegistryManager registryManager,
			ExtensionRowTypeConverter extensionConverter, JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager,
			ExtensionManager extensionManager, RegistryManager registryManager2, ConceptTermConverter conceptTermConverter) {
		super(cfg, dataDir);
		this.userConverter = userConverter;
		this.registryManager = registryManager;
		this.orgConverter = orgConverter;
		this.extensionConverter = extensionConverter;
		this.jdbcInfoConverter = jdbcInfoConverter;
		this.sourceManager = sourceManager;
		this.extensionManager = extensionManager;
		this.registryManager2 = registryManager2;
		this.conceptTermConverter = conceptTermConverter;
		defineXstreamMapping();
	}

	private void addResource(Resource res) {
		resources.put(res.getShortname().toLowerCase(), res);
	}

	public Resource create(String shortname, File dwca, User creator, BaseAction action)
	throws AlreadyExistingException, ImportException {
		Resource resource;
		ActionLogger alog = new ActionLogger(this.log, action);
		try {
			// decompress archive
			File dwcaDir = dataDir.tmpDir();
			CompressionUtil.decompressFile(dwcaDir, dwca);
			// open the dwca with dwca reader
			Archive arch = ArchiveFactory.openArchive(dwcaDir);
			// create new resource once we know the archive can be read
			resource = create(shortname, creator);
			// keep track of source files as an dwc archive might refer to the same source file multiple times
			Map<String, FileSource> sources = new HashMap<String, FileSource>();
			if (arch.getCore() != null) {
				// read core source+mappings
				FileSource s = importSource(alog, resource, arch.getCore());
				sources.put(arch.getCore().getLocation(), s);
				ExtensionMapping map = importMappings(alog, arch.getCore(), s);
				resource.setCore(map);
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
					resource.addExtension(map);
				}
				// finally persist the whole thing
				save(resource);
				alog.info("Imported existing darwin core archive with core row type " + resource.getCoreRowType() + " and "
						+ resource.getSources().size() + " source(s), " + resource.getExtensions().size() + " mapping(s)");
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
		return resource;
	}

	/*
	 * (non-Javadoc)
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

	/**
	 * 
	 */
	private void defineXstreamMapping() {
		// xstream.setMode(XStream.NO_REFERENCES);

		xstream.alias("resource", Resource.class);
		xstream.alias("user", User.class);
		xstream.alias("filesource", FileSource.class);
		xstream.alias("sqlsource", SqlSource.class);
		xstream.alias("mapping", ExtensionMapping.class);
		xstream.alias("field", ArchiveField.class);

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
		xstream.addDefaultImplementation(ExtensionProperty.class, ConceptTerm.class);
		xstream.addDefaultImplementation(DwcTerm.class, ConceptTerm.class);
		xstream.addDefaultImplementation(DcTerm.class, ConceptTerm.class);
		xstream.addDefaultImplementation(GbifTerm.class, ConceptTerm.class);
		xstream.addDefaultImplementation(IucnTerm.class, ConceptTerm.class);
		xstream.addDefaultImplementation(IptTerm.class, ConceptTerm.class);
		xstream.registerConverter(orgConverter);
		xstream.registerConverter(jdbcInfoConverter);
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

	/*
	 * (non-Javadoc)
	 * @see org.gbif.ipt.service.manage.ResourceManager#getEml(java.lang.String)
	 */
	private Eml loadEml(Resource resource) {
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
		resource.setEml(eml);
		syncEmlWithResource(resource);
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

	private FileSource importSource(ActionLogger alog, Resource config, ArchiveFile af)
	throws ImportException {
		File extFile = af.getLocationFile();
		FileSource s = sourceManager.add(config, extFile, af.getLocation());
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

	/*
	 * (non-Javadoc)
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

	/*
	 * (non-Javadoc)
	 * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
	 * org.gbif.ipt.model.Organisation)
	 */
	public void register(Resource resource, Organisation organisation, Ipt ipt)
	throws RegistryException {
		if (PublicationStatus.REGISTERED != resource.getStatus()) {
			registryManager2.setRegistryCredentials(organisation.getKey().toString(), organisation.getPassword());
			UUID key = registryManager2.register(resource, organisation, ipt);
			if (key == null) {
				throw new RegistryException(RegistryException.TYPE.MISSING_METADATA, "No key returned for registered resoruce.");
			}
			resource.setKey(key);
			resource.setOrganisation(organisation);
			resource.setStatus(PublicationStatus.REGISTERED);
			save(resource);
		}
	}

	public void save(Resource resource) throws InvalidConfigException {
		File cfgFile = dataDir.resourceFile(resource, PERSISTENCE_FILE);
		try {
			// make sure resource dir exists
			FileUtils.forceMkdir(cfgFile.getParentFile());
			// persist data
			Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
			xstream.toXML(resource, writer);
			// add to internal map
			addResource(resource);
			log.debug("Saved " + resource);
		} catch (IOException e) {
			log.error(e);
			throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant write mapping configuration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
	 */
	public void saveEml(Resource resource) throws InvalidConfigException {
		// udpate EML with latest resource basics
		syncEmlWithResource(resource);
		// save into data dir
		File emlFile = dataDir.resourceFile(resource, EML_FILE);
		try {
			EmlWriter.writeEmlFile(emlFile, resource.getEml());
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

	private void syncEmlWithResource(Resource resource) {
		// we need some GUID. If we have use the registry key, if not use the resource URL
		if (resource.getKey() != null) {
			resource.getEml().setGuid(resource.getKey().toString());
		} else {
			resource.getEml().setGuid(getResourceLink(resource.getShortname()).toString());
		}
	}

	/*
	 * (non-Javadoc)
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

	public void publish(Resource resource) throws InvalidConfigException {
		// see if eml has changed since last publication
		Eml eml = resource.getEml();
		int newHash = getEmlHash(resource,eml); 
		if (newHash!=resource.getLastPublishedEmlHash()){
			int version = eml.getEmlVersion();
			version++;
			eml.setEmlVersion(version);
			saveEml(resource);
			resource.setLastPublishedEmlHash(newHash);
		}
		// TODO: regenerate dwca ?
		// persist any resource object changes
		save(resource);
	}

	private int getEmlHash(Resource resource, Eml eml){
		//TODO: replace by hashing the eml xml file content? 
		// Alternatively code a proper hashCode method for Eml that needs to be maintained - might be too much effort
		return eml.hashCode();
	}

}
