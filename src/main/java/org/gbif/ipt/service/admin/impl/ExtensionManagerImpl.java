package org.gbif.ipt.service.admin.impl;

import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import static org.gbif.utils.HttpUtil.success;

@Singleton
public class ExtensionManagerImpl extends BaseManager implements ExtensionManager {

  // logging
  private static final Logger log = Logger.getLogger(ExtensionManagerImpl.class);
  public static final String EXTENSION_FILE_SUFFIX = ".xml";
  protected static final String CONFIG_FOLDER = ".extensions";
  private final static String TAXON_KEYWORD = "dwc:taxon";
  private final static String OCCURRENCE_KEYWORD = "dwc:occurrence";
  private final static String EVENT_KEYWORD = "dwc:event";
  private final static String RECORD_LEVEL_CLASS = "Record-level";
  private final Map<String, Extension> extensionsByRowtype = Maps.newHashMap();
  private final ExtensionFactory factory;
  private final HttpUtil downloader;
  private final ResourceManager resourceManager;
  private final ConfigWarnings warnings;
  private final RegistryManager registryManager;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;
  // map of deprecated terms and their replacedBy terms
  private static Map<String, Term> TERMS_REPLACED_BY_ANOTHER_TERM;

  @Inject
  public ExtensionManagerImpl(AppConfig cfg, DataDir dataDir, ExtensionFactory factory, ResourceManager resourceManager,
    HttpUtil httpUtil, ConfigWarnings warnings, SimpleTextProvider textProvider,
    RegistrationManager registrationManager, RegistryManager registryManager) {
    super(cfg, dataDir);
    this.factory = factory;
    this.resourceManager = resourceManager;
    this.downloader = httpUtil;
    this.warnings = warnings;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
    this.registryManager = registryManager;

    TERMS_REPLACED_BY_ANOTHER_TERM =
      new ImmutableMap.Builder<String, Term>().put("http://purl.org/dc/terms/source", DcTerm.references)
        .put("http://purl.org/dc/terms/rights", DcTerm.license)
        .put("http://rs.tdwg.org/dwc/terms/individualID", DwcTerm.organismID)
        .put("http://rs.tdwg.org/dwc/terms/occurrenceDetails", DcTerm.references).build();
  }

  public static String normalizeRowType(String rowType) {
    // occurrence alternatives
    if ("http://rs.tdwg.org/dwc/terms/DarwinCore".equalsIgnoreCase(rowType)
        || "http://rs.tdwg.org/dwc/xsd/simpledarwincore/".equalsIgnoreCase(rowType)
        || "http://rs.tdwg.org/dwc/terms/SimpleDarwinCore".equalsIgnoreCase(rowType)
        || "http://rs.tdwg.org/dwc/dwctype/Occurrence".equalsIgnoreCase(rowType)
        || "http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_OCCURRENCE;
    }

    // taxon alternatives
    if ("http://rs.tdwg.org/dwc/dwctype/Taxon".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_TAXON;
    }

    return rowType;
  }

  @Override
  public void uninstallSafely(String rowType) throws DeletionNotAllowedException {
    if (extensionsByRowtype.containsKey(rowType)) {
      // check if its used by some resources
      for (Resource r : resourceManager.list()) {
        if (!r.getMappings(rowType).isEmpty()) {
          String msg = "Extension mapped in resource " + r.getShortname();
          log.warn(msg);
          throw new DeletionNotAllowedException(Reason.EXTENSION_MAPPED, msg);
        }
      }
      uninstall(rowType);
    } else {
      log.warn("Extension not installed locally, cant delete " + rowType);
    }
  }

  /**
   * Uninstall extension by its unique rowType.
   *
   * @param rowType rowType of extension to uninstall
   */
  private void uninstall(String rowType) {
    if (extensionsByRowtype.containsKey(rowType)) {
      extensionsByRowtype.remove(rowType);
      File f = getExtensionFile(rowType);
      if (f.exists()) {
        FileUtils.deleteQuietly(f);
      } else {
        log.warn("Extension doesnt exist locally, cant delete " + rowType);
      }
    } else {
      log.warn("Extension not installed locally, cant delete " + rowType);
    }
  }

  @Override
  public synchronized void update(String rowType) throws IOException, RegistryException {
    // identify installed extension by rowType
    Extension installed = get(rowType);

    if (installed != null) {

      // verify there is a newer (latest) version
      Extension latestVersion = null;
      for (Extension e : registryManager.getExtensions()) {
        // match by rowType and isLatest, plus the URL cannot be null in order to be installed
        if (e.getRowType() != null && e.getRowType().equalsIgnoreCase(rowType) && e.isLatest()) {
          latestVersion = e;
          break;
        }
      }

      boolean isNewVersion = false;
      if (latestVersion != null) {
        Date issued = installed.getIssued();
        Date issuedLatest = latestVersion.getIssued();
        if (issued == null && issuedLatest != null) {
          isNewVersion = true;
        } else if (issued != null && issuedLatest != null) {
          isNewVersion = (issuedLatest.compareTo(issued) > 0); // latest version must have newer issued date
        }
      }

      if (isNewVersion && latestVersion.getUrl() != null) {
        // check if there are any associated resource mappings
        List<Resource> resourcesToMigrate = Lists.newArrayList();
        for (Resource r : resourceManager.list()) {
          if (!r.getMappings(rowType).isEmpty()) {
            resourcesToMigrate.add(r);
          }
        }

        // first download latestVersion XML file
        File tmpFile = download(latestVersion.getUrl());
        Extension extension = loadFromFile(tmpFile);

        // if there are mappings to this extension - do migrations to latest version, save resources
        if (!resourcesToMigrate.isEmpty()) {
          for (Resource r : resourcesToMigrate) {
            log.info("Updating " + rowType + " mappings for resource: " + r.getTitleAndShortname() + "...");
            migrateResourceToNewExtensionVersion(r, installed, extension);
            resourceManager.save(r);
            log.info("Updated " + rowType + " mappings successfully for resource: " + r.getTitleAndShortname());
          }
        }

        // uninstall and install new version
        uninstall(rowType);
        finishInstall(tmpFile, extension);
      }
    }
  }

  @Override
  public synchronized boolean updateIfChanged(String rowType) throws IOException, RegistryException {
    // identify installed extension by rowType
    Extension installed = get(rowType);
    if (installed != null) {
      // match extension by rowType and issued date
      Extension matched = null;
      for (Extension ex : registryManager.getExtensions()) {
        if (ex.getRowType() != null && ex.getRowType().equalsIgnoreCase(rowType)
            && installed.getIssued() != null && ex.getIssued() != null && installed.getIssued().compareTo(ex.getIssued()) == 0) {
          matched = ex;
          break;
        }
      }
      // verify the version was updated
      if (matched != null && matched.getUrl() != null) {
        File extensionFile = getExtensionFile(rowType);
        return downloader.downloadIfChanged(matched.getUrl(), extensionFile);
      }
    }
    return false;
  }

  /**
   * Migrate a resource's extension mappings to an extension to a newer version of that extension.
   *
   * @param r       resource whose mappings must be migrated
   * @param current extension
   * @param newer   newer version of extension to migrate mappings to
   */
  @VisibleForTesting
  protected void migrateResourceToNewExtensionVersion(Resource r, Extension current, Extension newer) {
    // sanity check that the current and newer extensions share same rowType
    Preconditions.checkState(current.getRowType().equalsIgnoreCase(newer.getRowType()));
    Preconditions.checkState(!r.getMappings(current.getRowType()).isEmpty());
    log.info("Migrating " + r.getShortname() + " mappings to extension " + current.getRowType()
             + " to latest extension version");

    // populate various set to keep track of how many terms were deprecated, how terms' vocabulary was updated, etc
    Set<ExtensionProperty> deprecated = Sets.newHashSet();
    Set<ExtensionProperty> vocabulariesRemoved = Sets.newHashSet();
    Set<ExtensionProperty> vocabulariesUnchanged = Sets.newHashSet();
    Set<ExtensionProperty> vocabulariesUpdated = Sets.newHashSet();
    for (ExtensionProperty property : current.getProperties()) {
      // newer extension still contain this property?
      if (!newer.hasProperty(property.qualifiedName())) {
        deprecated.add(property);
      }
      // if so, check if this property uses a vocabulary, and whether the newer extension uses a newer version of it
      else {
        if (property.getVocabulary() != null) {
          Vocabulary v1 = property.getVocabulary();
          Vocabulary v2 = newer.getProperty(property.qualifiedName()).getVocabulary();
          // case 1: vocabulary removed in newer version
          if (v2 == null) {
            vocabulariesRemoved.add(property);
          }
          // case 2: vocabulary versions are unchanged between versions
          else if (v1.getUriString().equalsIgnoreCase(v2.getUriString())) {
            vocabulariesUnchanged.add(property);
          }
          // case 3: vocabulary has been updated in newer version
          else if (!v1.getUriString().equalsIgnoreCase(v2.getUriString())) {
            vocabulariesUpdated.add(property);
          }
        }
      }
    }
    log.debug(deprecated.size() + " properties have been deprecated in the newer version");
    log.debug(vocabulariesRemoved.size() + " properties in the newer version of extension no longer use a vocabulary");
    log.debug(vocabulariesUnchanged.size() + " properties in the newer version of extension use the same vocabulary");
    log.debug(vocabulariesUpdated.size() + " properties in the newer version of extension use a newer vocabulary");

    // set of new terms (terms to add)
    Set<ExtensionProperty> added = Sets.newHashSet();
    for (ExtensionProperty property : newer.getProperties()) {
      // older extension contain this property?
      if (!current.hasProperty(property.qualifiedName())) {
        added.add(property);
      }
    }
    log.debug("Newer version of extension has " + added.size() + " new properties");

    for (ExtensionMapping extensionMapping : r.getMappings(current.getRowType())) {
      migrateExtensionMapping(extensionMapping, newer, deprecated);
    }
  }

  /**
   * Migrate an ExtensionMapping to use a newer version of that extension:
   * 1. Migrate property mappings for deprecated terms that have been replaced by another term. Careful, the replacing
   * term must be included in the newer extension version, and cannot already be mapped
   * 2. Remove property mappings for deprecated terms that have NOT been replaced by another term
   *
   * @param extensionMapping ExtensionMapping to migrate to use newer version of Extension
   * @param newer            newer version of Extension ExtensionMapping is based on
   * @param deprecated       set of ExtensionProperty deprecated in newer version of Extension
   */
  private ExtensionMapping migrateExtensionMapping(ExtensionMapping extensionMapping, Extension newer,
    Set<ExtensionProperty> deprecated) {
    log.debug("Migrating extension mapping...");
    // update Extension
    extensionMapping.setExtension(newer);
    // migrate or remove property mappings to deprecated terms
    for (ExtensionProperty deprecatedProperty : deprecated) {
      Term replacedBy = TERMS_REPLACED_BY_ANOTHER_TERM.get(deprecatedProperty.qualifiedName());
      // replacing term must exist in new extension, and it cannot already be mapped!
      if (replacedBy != null && newer.getProperty(replacedBy) != null && !extensionMapping.isMapped(replacedBy)) {
        PropertyMapping pm = extensionMapping.getField(deprecatedProperty.qualifiedName());
        ExtensionProperty ep = newer.getProperty(replacedBy);
        if (pm != null && ep != null) {
          pm.setTerm(ep);
          log.debug("Mapping to deprecated term " + deprecatedProperty.qualifiedName() + " has been migrated to term "
                    + replacedBy.qualifiedName());
        }
      }
      // otherwise simply remove the property mapping
      else {
        log.debug("Mapping to deprecated term " + deprecatedProperty.qualifiedName()
                  + " cannot be migrated therefore it is being removed!");
        removePropertyMapping(extensionMapping, deprecatedProperty.qualifiedName());
      }
    }
    return extensionMapping;
  }

  /**
   * Remove a PropertyMapping from an ExtensionMapping.
   *
   * @param extensionMapping ExtensionMapping
   * @param qualifiedName    of PropertyMapping term to remove
   */
  private void removePropertyMapping(ExtensionMapping extensionMapping, String qualifiedName) {
    PropertyMapping pm = extensionMapping.getField(qualifiedName);
    Set<PropertyMapping> propertyMappings = extensionMapping.getFields();
    if (pm != null && propertyMappings.contains(pm)) {
      propertyMappings.remove(pm);
      log.debug("Removed mapping to term " + pm.getTerm().qualifiedName());
    }
  }

  @Override
  public Extension get(String rowType) {
    return extensionsByRowtype.get(normalizeRowType(rowType));
  }

  /**
   * Return the latest versions of core extensions (that the IPT is configured to use) from the registry.
   *
   * @return list containing latest versions of core extensions
   */
  private List<Extension> getCoreTypes() {
    List<Extension> coreTypes = Lists.newArrayList();
    try {
      for (Extension ext : registryManager.getExtensions()) {
        if (ext.getRowType() != null && AppConfig.getCoreRowTypes().contains(ext.getRowType())) {
          if (ext.isLatest()) { // must be latest version
            coreTypes.add(ext);
          }
        }
      }
    } catch (RegistryException e) {
      // add startup error message about Registry error
      String msg = RegistryException.logRegistryException(e, baseAction);
      warnings.addStartupError(msg);
      log.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.extensions.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      log.error(msg);
    }

    // throw exception if not all core type extensions could not be loaded
    if (AppConfig.getCoreRowTypes().size() != coreTypes.size()) {
      String msg = "Not all core extensions were loaded!";
      log.error(msg);
      throw new InvalidConfigException(TYPE.INVALID_DATA_DIR, msg);
    }
    return coreTypes;
  }

  /**
   * Retrieve extension file by its unique rowType.
   *
   * @param rowType rowType of extension
   *
   * @return extension file
   */
  private File getExtensionFile(String rowType) {
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(rowType, EXTENSION_FILE_SUFFIX);
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  /**
   * Download and install an extension into local file. The final filename is based on the extension's rowType.
   *
   * @param url the URL of the xml based extension definition
   *
   * @return the installed extension
   *
   * @throws InvalidConfigException if Extension failed to be installed
   */
  @Override
  public synchronized Extension install(URL url) throws InvalidConfigException {
    Preconditions.checkNotNull(url);

    try {
      File tmpFile = download(url);
      Extension extension = loadFromFile(tmpFile);
      finishInstall(tmpFile, extension);
      return extension;
    } catch (InvalidConfigException e) {
      throw e;
    } catch (Exception e) {
      String msg = baseAction.getText("admin.extension.install.error", new String[] {url.toString()});
      log.error(msg, e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, msg, e);
    }
  }

  /**
   * Move and rename temporary file to final version. Update extensions loaded into local lookup.
   *
   * @param tmpFile   downloaded extension file (in temporary location with temporary filename)
   * @param extension extension being installed
   *
   * @throws IOException if moving file fails
   */
  private void finishInstall(File tmpFile, Extension extension) throws IOException {
    Preconditions.checkNotNull(tmpFile);
    Preconditions.checkNotNull(extension);
    Preconditions.checkNotNull(extension.getRowType());

    try {
      File installedFile = getExtensionFile(extension.getRowType());
      FileUtils.moveFile(tmpFile, installedFile);
      // keep extension in local lookup: allowed one installed extension per rowType
      extensionsByRowtype.put(extension.getRowType(), extension);
    } catch (IOException e) {
      log.error("Installing extension failed, while trying to move and rename extension file: " + e.getMessage(), e);
      throw e;
    }
  }


  /**
   * Download an extension into temporary file and return it.
   *
   * @param url URL of extension to download
   *
   * @return temporary file extension was downloaded to, or null if it failed to be downloaded
   */
  private File download(URL url) throws IOException {
    Preconditions.checkNotNull(url);
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(url.toString(), EXTENSION_FILE_SUFFIX);
    File tmpFile = dataDir.tmpFile(filename);
    StatusLine statusLine = downloader.download(url, tmpFile);
    if (success(statusLine)) {
      log.info("Successfully downloaded extension: " + url.toString());
      return tmpFile;
    } else {
      String msg =
        "Failed to download extension: " + url.toString() + ". Response=" + String.valueOf(statusLine.getStatusCode());
      log.error(msg);
      throw new IOException(msg);
    }
  }

  /**
   * Install core extensions (that the IPT is configured to use).
   *
   * @throws InvalidConfigException if any installation fails
   */
  @Override
  public void installCoreTypes() throws InvalidConfigException {
    List<Extension> extensions = getCoreTypes();
    for (Extension ext : extensions) {
      install(ext.getUrl());
    }
  }

  @Override
  public List<Extension> list() {
    return new ArrayList<Extension>(extensionsByRowtype.values());
  }

  @Override
  public List<Extension> list(String coreRowType) {
    if (coreRowType != null) {
      if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        return search(OCCURRENCE_KEYWORD, true, false);
      } else if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
        return search(TAXON_KEYWORD, true, false);
      } else if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_EVENT)) {
        return search(EVENT_KEYWORD, true, false);
      } else {
        return search(coreRowType, true, false);
      }
    }
    return list();
  }

  @Override
  public List<Extension> listCore(String coreRowType) {
    if (coreRowType != null) {
      if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        return search(OCCURRENCE_KEYWORD, false, true);
      } else if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
        return search(TAXON_KEYWORD, false, true);
      } else if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_EVENT)) {
        return search(EVENT_KEYWORD, false, true);
      } else {
        return search(coreRowType, false, true);
      }
    }
    return listCore();
  }

  @Override
  public List<Extension> listCore() {
    List<Extension> list = Lists.newArrayList();
    for (String rowType : AppConfig.getCoreRowTypes()) {
      Extension e = get(rowType);
      if (e != null) {
        list.add(e);
      }
    }
    return list;
  }

  @Override
  public int load() {
    File extensionDir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (extensionDir.isDirectory()) {
      List<File> extensionFiles = new ArrayList<File>();
      FilenameFilter ff = new SuffixFileFilter(EXTENSION_FILE_SUFFIX, IOCase.INSENSITIVE);
      extensionFiles.addAll(Arrays.asList(extensionDir.listFiles(ff)));
      for (File ef : extensionFiles) {
        try {
          Extension extension = loadFromFile(ef);
          // keep extension in local lookup: allowed one installed extension per rowType
          extensionsByRowtype.put(extension.getRowType(), extension);
          counter++;
        } catch (InvalidConfigException e) {
          // when IPT is in test mode, remove/uninstall invalid extension definition and prompt admin to reinstall it
          if (cfg.isTestInstallation()) {
            FileUtils.deleteQuietly(ef);
            warnings.addStartupError("Extension " + ef.getAbsolutePath()
                                     + " has been deleted from the IPT data directory because it was invalid or out-of-date."
                                     + " Please install the latest version of this extension if needed and restart your web server."
                                     + " Cause: " + e.getMessage(), e);
          }
          // when IPT is in production mode, just warn user invalid extension was encountered while trying to load it
          else {
            warnings.addStartupError("Can't load local extension definition: " + e.getMessage(), e);
          }
        }
      }
    }
    return counter;
  }

  /**
   * Reads an extension from file and returns it.
   *
   * @param localFile extension file to read from
   *
   * @return extension loaded from file
   *
   * @throws InvalidConfigException if extension could not be loaded successfully
   */
  @VisibleForTesting
  protected Extension loadFromFile(File localFile) throws InvalidConfigException {
    Preconditions.checkNotNull(localFile);
    Preconditions.checkState(localFile.exists());

    Closer closer = Closer.create();
    try {
      InputStream fileIn = closer.register(new FileInputStream(localFile));
      Extension extension = factory.build(fileIn);
      // normalise rowtype
      extension.setRowType(normalizeRowType(extension.getRowType()));
      log.info("Successfully loaded extension " + extension.getRowType());
      return extension;
    } catch (IOException e) {
      log.error("Can't access local extension file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Can't access local extension file");
    } catch (SAXException e) {
      log.error("Can't parse local extension file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Can't parse local extension file: " + e.getMessage());
    } catch (ParserConfigurationException e) {
      log.error("Can't create sax parser", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Can't create sax parser");
    } finally {
      try {
        closer.close();
      } catch (IOException e) {
        log.debug("Failed to close input stream on extension file", e);
      }
    }
  }


  /**
   * List all available extensions matching a registered keyword.
   * </br>
   * For example, searching by keyword "dwc:Taxon" will return a list of extensions that have "dwc:Taxon" in their
   * subject.
   *
   * @param keyword             keyword to filter extensions by
   * @param includeEmptySubject true to include extensions with empty subject, false otherwise. An extension with an
   *                            empty subject indicates the extension is suitable for all core extensions.
   * @param searchForCores      true if core type extensions should be listed or false if non-core type extensions
   *                            should be listed
   */
  private List<Extension> search(String keyword, boolean includeEmptySubject, boolean searchForCores) {
    List<Extension> list = new ArrayList<Extension>();
    keyword = StringUtils.trimToNull(keyword);
    if (keyword != null) {
      keyword = keyword.toLowerCase();
      for (Extension e : extensionsByRowtype.values()) {
        if ((searchForCores && !e.isCore()) || (!searchForCores && e.isCore())) {
          continue;
        }
        if (includeEmptySubject && StringUtils.trimToNull(e.getSubject()) == null || StringUtils
          .containsIgnoreCase(e.getSubject(), keyword)) {
          list.add(e);
        }
      }
    }
    return list;
  }

  public List<String> getRedundantGroups(Extension extension, Extension core) {
    List<String> groups = extension.getGroups();
    List<String> coreGroups = core.getGroups();
    if (!groups.isEmpty() && !coreGroups.isEmpty()) {
      // retain groups already included in core extension...
      coreGroups.retainAll(groups);
      // exclude Record-Level since this cannot ever be a redundant class
      if (coreGroups.contains(RECORD_LEVEL_CLASS)) {
        coreGroups.remove(RECORD_LEVEL_CLASS);
      }
      return coreGroups;
    } else {
      return Lists.newArrayList();
    }
  }
}
