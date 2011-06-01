/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

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
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.utils.HttpUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author tim
 */
@Singleton
public class ExtensionManagerImpl extends BaseManager implements ExtensionManager {
  private Map<String, Extension> extensionsByRowtype = new HashMap<String, Extension>();
  private static final String CONFIG_FOLDER = ".extensions";
  private ExtensionFactory factory;;
  private HttpUtil downloader;
  private final String TAXON_KEYWORD = "dwc:taxon";
  private final String OCCURRENCE_KEYWORD = "dwc:occurrence";
  private ResourceManager resourceManager;
  private ConfigWarnings warnings;
  @Inject
  private RegisteredExtensions registered;
  
  public static class RegisteredExtensions {
    private List<Extension> extensions = new ArrayList<Extension>();
    private RegistryManager registryManager;

    @Inject
    public RegisteredExtensions(RegistryManager registryManager) {
      super();
      this.registryManager = registryManager;
    }

    public boolean isLoaded() {
        if (extensions.size() > 0) {
          return true;
        }
        return false;
      }
    
    public void load() throws RuntimeException {
      extensions = registryManager.getExtensions();
    }
    
    public List<Extension> getCoreTypes() {
      if(!isLoaded())load();
      List<Extension> coreTypes = new ArrayList<Extension>();
	  for(Extension ext :extensions){
		  if(Constants.DWC_ROWTYPE_OCCURRENCE.equals(normalizeRowType(ext.getRowType()))){
			  coreTypes.add(ext);
		  }
		  if(Constants.DWC_ROWTYPE_TAXON.equals(normalizeRowType(ext.getRowType()))){
			  coreTypes.add(ext);
		  }
	  }
    	return coreTypes;
    }
    
    public List<Extension> getExtensions() {
    	return extensions;
    }
  }

  @Inject
  public ExtensionManagerImpl(AppConfig cfg, DataDir dataDir, ExtensionFactory factory,
      ResourceManager resourceManager, ConfigWarnings warnings, DefaultHttpClient client) {
    super(cfg, dataDir);
    this.factory = factory;
    this.warnings = warnings;
    this.resourceManager = resourceManager;
    this.downloader = new HttpUtil(client);
  }

  public static String normalizeRowType(String rowType) {
    // occurrence alternatives
    if ("http://rs.tdwg.org/dwc/terms/DarwinCore".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_OCCURRENCE;
    } else if ("http://rs.tdwg.org/dwc/xsd/simpledarwincore/".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_OCCURRENCE;
    } else if ("http://rs.tdwg.org/dwc/terms/SimpleDarwinCore".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_OCCURRENCE;
    } else if ("http://rs.tdwg.org/dwc/dwctype/Occurrence".equalsIgnoreCase(rowType)) {
      return Constants.DWC_ROWTYPE_OCCURRENCE;
    } else if ("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord".equalsIgnoreCase(rowType)) {
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

  private File getExtensionFile(String rowType) {
    String filename = rowType.replaceAll("[/.:]+", "_") + ".xml";
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  public synchronized Extension install(URL url) throws InvalidConfigException {
    Extension ext;
    ext = null;
    // download extension into local file first for subsequent IPT startups
    // final filename is based on rowType which we dont know yet - create a tmp file first
    File tmpFile = dataDir.configFile(CONFIG_FOLDER + "/tmp-extension.xml");
    try {
      downloader.download(url, tmpFile);
      log.info("Successfully downloaded Extension " + url);
      // finally read in the new file and create the extension object
      ext = loadFromFile(tmpFile);
      if (ext != null && ext.getRowType() != null) {
        // rename tmp file into final version
        File localFile = getExtensionFile(ext.getRowType());
        FileUtils.moveFile(tmpFile, localFile);
      } else {
        log.error("Extension lacking required rowType!");
      }
    } catch (InvalidConfigException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Error installing extension " + url, e);
    }
    return ext;
  }

  public List<Extension> list() {
    return new ArrayList<Extension>(extensionsByRowtype.values());
  }

  public List<Extension> list(String coreRowType) {
    if (coreRowType != null && coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
      return search(OCCURRENCE_KEYWORD, true, false);
    } else if (coreRowType != null && coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
      return search(TAXON_KEYWORD, true, false);
    } else {
      return list();
    }
  }

  public List<Extension> listCore() {
    List<Extension> list = new ArrayList<Extension>();
    Extension e = get(Constants.DWC_ROWTYPE_OCCURRENCE);
    if (e != null) {
      list.add(e);
    }
    e = get(Constants.DWC_ROWTYPE_TAXON);
    if (e != null) {
      list.add(e);
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
   * Reads a local extension file into manager cache
   * 
   * @param localFile
   * @return
   */
  private Extension loadFromFile(File localFile) throws InvalidConfigException {
    InputStream fileIn = null;
    Extension ext = null;
    try {
      fileIn = new FileInputStream(localFile);
      ext = factory.build(fileIn);
      // normalise rowtype
      ext.setRowType(normalizeRowType(ext.getRowType()));
      // keep vocab in local lookup
      extensionsByRowtype.put(ext.getRowType(), ext);
      log.info("Successfully loaded extension " + ext.getRowType());
    } catch (FileNotFoundException e) {
      log.error("Cant find local extension file", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant find local extension file");
    } catch (IOException e) {
      log.error("Cant access local extension file", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant access local extension file");
    } catch (SAXException e) {
      log.error("Cant parse local extension file", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant parse local extension file");
    } catch (ParserConfigurationException e) {
      log.error("Cant create sax parser", e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Cant create sax parser");
    } finally {
      if (fileIn != null) {
        try {
          fileIn.close();
        } catch (IOException e) {
        }
      }
    }
    return ext;
  }

  public List<Extension> search(String keyword) {
    return search(keyword, false, false);
  }

  private List<Extension> search(String keyword, boolean includeEmptySubject, boolean includeCoreExtensions) {
    List<Extension> list = new ArrayList<Extension>();
    keyword = StringUtils.trimToNull(keyword);
    if (keyword != null) {
      keyword = keyword.toLowerCase();
      for (Extension e : extensionsByRowtype.values()) {
        if (!includeCoreExtensions && e.isCore()) {
          continue;
        }
        if ((includeEmptySubject && StringUtils.trimToNull(e.getSubject()) == null)
            || StringUtils.containsIgnoreCase(e.getSubject(), keyword)) {
          list.add(e);
        }
      }
    }
    return list;
  }

  public void installCoreTypes() {
	  for(Extension ext :registered.getCoreTypes()){
		  try {
			  	install(ext.getUrl());
			  } catch (Exception e) {
				  log.debug(e);
			  }	
	  }
  }
}
