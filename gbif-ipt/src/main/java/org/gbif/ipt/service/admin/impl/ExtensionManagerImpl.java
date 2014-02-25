package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Resource;
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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

@Singleton
public class ExtensionManagerImpl extends BaseManager implements ExtensionManager {

  // logging
  private static final Logger log = Logger.getLogger(ExtensionManagerImpl.class);
  protected static final String CONFIG_FOLDER = ".extensions";
  private final static String TAXON_KEYWORD = "dwc:taxon";
  private final static String OCCURRENCE_KEYWORD = "dwc:occurrence";
  private final Map<String, Extension> extensionsByRowtype = new HashMap<String, Extension>();
  private final ExtensionFactory factory;
  private final HttpUtil downloader;
  private final ResourceManager resourceManager;
  private final ConfigWarnings warnings;
  private final RegistryManager registryManager;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  @Inject
  public ExtensionManagerImpl(AppConfig cfg, DataDir dataDir, ExtensionFactory factory,
    ResourceManager resourceManager, HttpUtil httpUtil, ConfigWarnings warnings, SimpleTextProvider textProvider,
    RegistrationManager registrationManager, RegistryManager registryManager) {
    super(cfg, dataDir);
    this.factory = factory;
    this.resourceManager = resourceManager;
    this.downloader = httpUtil;
    this.warnings = warnings;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
    this.registryManager = registryManager;
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

  public void delete(String rowType) throws DeletionNotAllowedException {
    if (extensionsByRowtype.containsKey(rowType)) {
      // check if its used by some resources
      for (Resource r : resourceManager.list()) {
        if (!r.getMappings(rowType).isEmpty()) {
          String msg = "Extension mapped in resource " + r.getShortname();
          log.warn(msg);
          throw new DeletionNotAllowedException(Reason.EXTENSION_MAPPED, msg);
        }
      }
      // delete
      extensionsByRowtype.remove(rowType);
      File f = getExtensionFile(rowType);
      if (f.exists()) {
        f.delete();
      } else {
        log.warn("Extension doesnt exist locally, cant delete " + rowType);
      }
    } else {
      log.warn("Extension not installed locally, cant delete " + rowType);
    }
  }

  public Extension get(String rowType) {
    return extensionsByRowtype.get(normalizeRowType(rowType));
  }

  /**
   * Retrieve a list containing all core type extensions from the registry.
   * 
   * @return list containing all core type extensions.
   */
  private List<Extension> getCoreTypes() {
    List<Extension> coreTypes = new ArrayList<Extension>();

    // copy used to allow a warning message for anything not mapped
    List<String> coreTypesCopy = Lists.newArrayList(AppConfig.getCoreRowTypes());
    try {

      for (Extension ext : registryManager.getExtensions()) {
        log.debug("Extension from registry: " + ext.getRowType());

        Iterator<String> iter = coreTypesCopy.iterator();
        while (iter.hasNext()) {
          String rowType = iter.next();
          if (rowType.equals(normalizeRowType(ext.getRowType()))) {
            coreTypes.add(ext);
            iter.remove(); // it's mapped
            break;
          }
        }
      }
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), baseAction);
      // add startup error message about Registry error
      warnings.addStartupError(msg);
      log.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.extensions.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      log.error(msg);
    }

    // Warn users if there are any core types that do not have a mapping to a type
    if (!coreTypesCopy.isEmpty()) {
      log.error("The IPT appears to be misconfigured.  The following core types are not mapped to an ID field: "
        + coreTypesCopy);
    }

    return coreTypes;
  }

  private File getExtensionFile(String rowType) {
    String filename = rowType.replaceAll("[/.:]+", "_") + ".xml";
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  /**
   * Download extension into local file. The final filename is based on the extension's rowType. This isn't known until
   * the download is complete, so a temporary file is stored first.
   * 
   * @param url the url that returns the xml based extension definition
   * @return the installed extension
   */
  public synchronized Extension install(URL url) throws InvalidConfigException {
    Extension ext = null;
    File tmpFile = dataDir.configFile(CONFIG_FOLDER + "/tmp-extension.xml");
    String address = (url == null) ? "null" : url.toString();
    String rowType = "";
    try {
      StatusLine statusLine = downloader.download(url, tmpFile);
      if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
        log.info("Successfully downloaded Extension " + url);
        // finally read in the new file and create the extension object
        ext = loadFromFile(tmpFile);
        if (ext != null && ext.getRowType() != null) {
          rowType = ext.getRowType();
          // rename tmp file into final version
          File localFile = getExtensionFile(rowType);
          FileUtils.moveFile(tmpFile, localFile);
        } else {
          log.error("Extension could not be loaded. Is required rowType missing?");
        }
      } else {

        log.error("Download of extension with url ( " + address + ") failed, the response code was "
          + String.valueOf(statusLine.getStatusCode()));
      }
    } catch (InvalidConfigException e) {
      throw e;
    } catch (FileExistsException e) {
      String msg = baseAction.getText("admin.extension.install.duplicate", new String[] {rowType});
      e.printStackTrace();
      log.error(msg);
      throw new InvalidConfigException(TYPE.ROWTYPE_ALREADY_INSTALLED, msg, e);
    } catch (Exception e) {
      String msg = baseAction.getText("admin.extension.install.error", new String[] {address});
      e.printStackTrace();
      log.error(msg);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, msg, e);
    }
    return ext;
  }

  /**
   * Install core type extensions.
   * 
   * @throws InvalidConfigException if installation of a core type extension failed
   */
  public void installCoreTypes() throws InvalidConfigException {
    List<Extension> extensions = getCoreTypes();
    for (Extension ext : extensions) {
      install(ext.getUrl());
    }
  }

  public List<Extension> list() {
    return new ArrayList<Extension>(extensionsByRowtype.values());
  }

  public List<Extension> list(String coreRowType) {
    if (coreRowType != null && coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
      return search(OCCURRENCE_KEYWORD, true, false);
    } else if (coreRowType != null && coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
      return search(TAXON_KEYWORD, true, false);
    } else if (coreRowType != null) {
      // search using the fully qualified core name (e.g. with http://rs.tdwg.org/dwc/terms/Occurrence)
      // and include any with no scoping
      return search(coreRowType, true, false);
    } else {
      // no core type
      return list();
    }
  }

  /**
   * Returns those extensions that are suitable for use as a core.
   * 
   * @return The extensions or an empty list
   */
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

  public int load() {
    File extensionDir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (extensionDir.isDirectory()) {
      List<File> extensionFiles = new ArrayList<File>();
      FilenameFilter ff = new SuffixFileFilter(".xml", IOCase.INSENSITIVE);
      extensionFiles.addAll(Arrays.asList(extensionDir.listFiles(ff)));
      for (File ef : extensionFiles) {
        try {
          loadFromFile(ef);
          counter++;
        } catch (InvalidConfigException e) {
          warnings.addStartupError("Cant load local extension definition " + ef.getAbsolutePath(), e);
        }
      }
    }
    return counter;
  }

  /**
   * Reads a local extension file into manager cache.
   */
  private Extension loadFromFile(File localFile) throws InvalidConfigException {
    InputStream fileIn = null;
    Extension ext = null;
    if (localFile != null && localFile.exists()) {
      try {
        fileIn = new FileInputStream(localFile);
        ext = factory.build(fileIn);
        // normalise rowtype
        ext.setRowType(normalizeRowType(ext.getRowType()));
        // keep vocab in local lookup
        extensionsByRowtype.put(ext.getRowType(), ext);
        log.info("Successfully loaded extension " + ext.getRowType());
      } catch (FileNotFoundException e) {
        log.error("Cant find local extension file (" + localFile.getAbsolutePath() + ")", e);
        throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant find local extension file");
      } catch (IOException e) {
        log.error("Cant access local extension file (" + localFile.getAbsolutePath() + ")", e);
        throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant access local extension file");
      } catch (SAXException e) {
        log.error("Cant parse local extension file (" + localFile.getAbsolutePath() + ")", e);
        throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant parse local extension file");
      } catch (ParserConfigurationException e) {
        log.error("Cant create sax parser", e);
        throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant create sax parser");
      } finally {
        if (fileIn != null) {
          try {
            fileIn.close();
          } catch (IOException e) {
            log.error("Input stream on extension file (" + localFile.getAbsolutePath() + ") could not be closed.");
          }
        }
      }
    } else {
      log.error("Tried to load local extension file that doesn't exist");
    }
    return ext;
  }


  /**
   * List all available extensions matching a registered keyword.
   * 
   * @param keyword to filter by, e.g. dwc:Taxon for all taxonomic extensions
   * @param includeEmptySubject must the subject be empty
   * @param includeCoreExtensions must the extension be a core type
   */
  private List<Extension> search(String keyword, boolean includeEmptySubject, boolean includeCoreExtensions) {
    List<Extension> list = new ArrayList<Extension>();
    keyword = StringUtils.trimToNull(keyword);
    if (keyword != null) {
      keyword = keyword.toLowerCase();
      for (Extension e : extensionsByRowtype.values()) {
        if (!includeCoreExtensions && e.isCore()) {
          continue;
        }
        if (includeEmptySubject && StringUtils.trimToNull(e.getSubject()) == null
          || StringUtils.containsIgnoreCase(e.getSubject(), keyword)) {
          list.add(e);
        }
      }
    }
    return list;
  }
}
