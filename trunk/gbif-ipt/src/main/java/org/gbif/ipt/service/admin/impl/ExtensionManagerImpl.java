/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.ExtensionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
  private HttpClient httpClient;
  private final String TAXON_KEYWORD="dwc:taxon";
  private final String OCCURRENCE_KEYWORD="dwc:occurrence";

  @Inject
  public ExtensionManagerImpl(ExtensionFactory factory, HttpClient httpClient) {
    super();
    this.factory = factory;
    this.httpClient = httpClient;
  }

  public void delete(String rowType) {
    if (extensionsByRowtype.containsKey(rowType)) {
      Extension ext = extensionsByRowtype.remove(rowType);
      if (ext == null) {
        log.warn("Extension not installed locally, cant delete " + rowType);
      } else {
        // TODO: check if its used by some resources
        File f = getExtensionFile(rowType);
        if (f.exists()) {
          f.delete();
        } else {
          log.warn("Extension doesnt exist locally, cant delete " + rowType);
        }
      }
    }
  }

  public Extension get(String rowType) {
    return extensionsByRowtype.get(rowType);
  }

  private File getExtensionFile(String rowType) {
    String filename = rowType.replaceAll("[/.:]+", "_") + ".xml";
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  public Extension install(URL url) throws InvalidConfigException {
    Extension ext = null;
    // download extension into local file first for subsequent IPT startups
    // final filename is based on rowType which we dont know yet - create a tmp file first
    File tmpFile = dataDir.configFile(CONFIG_FOLDER + "/tmp-extension.xml");
    if (tmpFile.exists()) {
    }
    Writer localWriter = null;
    GetMethod method = new GetMethod(url.toString());
    method.setFollowRedirects(true);
    try {
      FileUtils.forceMkdir(tmpFile.getParentFile());
      localWriter = new FileWriter(tmpFile);
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      IOUtils.copy(is, localWriter);
      log.info("Successfully downloaded Extension " + url);
      localWriter.close();
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
      log.error(e);
      throw new InvalidConfigException(TYPE.INVALID_EXTENSION, "Error installing extension " + url);
    } finally {
      try {
        method.releaseConnection();
      } catch (RuntimeException e) {
      }
    }
    return ext;
  }

  public List<Extension> list() {
    return new ArrayList<Extension>(extensionsByRowtype.values());
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
          log.error("Cant load local extension definition " + ef.getAbsolutePath(), e);
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

public List<Extension> list(Extension core) {
	if (core!=null && core.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)){
		return search(OCCURRENCE_KEYWORD);
	}else if (core!=null && core.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)){
		return search(TAXON_KEYWORD);		
	}else{
		return list();
	}
}

public List<Extension> listCore() {
	List<Extension> list = new ArrayList<Extension>();
	Extension e = get(Constants.DWC_ROWTYPE_OCCURRENCE);
	if (e!=null){
		list.add(e);
	}
	e = get(Constants.DWC_ROWTYPE_TAXON);
	if (e!=null){
		list.add(e);
	}
	return list;
}

public List<Extension> search(String keyword) {
	List<Extension> list = new ArrayList<Extension>();
	keyword=keyword.toLowerCase();
	for (Extension e : extensionsByRowtype.values()){
		if (StringUtils.containsIgnoreCase(e.getSubject(), keyword)){
			list.add(e);
		}
	}
	return list;
}
}
