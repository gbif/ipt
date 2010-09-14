/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.DownloadUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Manager for all vocabulary related methods. Keeps an internal map of locally existing and parsed vocabularies which
 * is keyed on a normed filename derived from a vocabularies URL. We use this derived filename instead of the proper URL
 * as we do not persist any additional data than the extension file itself - which doesnt have its own URL embedded.
 */
@Singleton
public class VocabulariesManagerImpl extends BaseManager implements VocabulariesManager {
  public class UpdateResult {
    // key=uri
    public Set<String> updated = new HashSet<String>();
    // key=uri
    public Set<String> unchanged = new HashSet<String>();
    // key=uri, value=error text
    public Map<String, String> errors = new HashMap<String, String>();
  }

  private Map<URL, Vocabulary> vocabularies = new HashMap<URL, Vocabulary>();
  private Map<String, URL> uri2url = new HashMap<String, URL>();
  private static final String CONFIG_FOLDER = ".vocabularies";
  public static final String PERSISTENCE_FILE = "vocabularies.xml";
  private VocabularyFactory vocabFactory;
  private DownloadUtil downloadUtil;
  private final XStream xstream = new XStream();
  private final RegistryManager registryManager;
  private final String[] defaultVocabs = new String[]{
      Constants.VOCAB_URI_LANGUAGE, Constants.VOCAB_URI_COUNTRY, Constants.VOCAB_URI_RESOURCE_TYPE,
      Constants.VOCAB_URI_RANKS};

  /**
   * 
   */
  @Inject
  public VocabulariesManagerImpl(VocabularyFactory vocabFactory, DownloadUtil downloadUtil,
      RegistryManager registryManager) {
    super();
    this.vocabFactory = vocabFactory;
    this.downloadUtil = downloadUtil;
    this.registryManager = registryManager;
    defineXstreamMapping();
  }

  private boolean addToCache(Vocabulary v, URL url) {
    if (url == null) {
      log.error("Cannot add vocabulary " + v.getUri() + " to cache without a valid URL");
      return false;
    }
    uri2url.put(v.getUri().toLowerCase(), url);
    // keep vocab in local lookup
    if (vocabularies.containsKey(url)) {
      log.warn("Vocabulary URI " + v.getUri() + " exists already - overwriting with new vocabulary from " + url);
    }
    vocabularies.put(url, v);
    return true;
  }

  private void defineXstreamMapping() {
  }

  public void delete(String uri) {
    if (uri != null) {
      URL url = uri2url.get(uri.toLowerCase());
      Vocabulary vocab = vocabularies.remove(url);
      // TODO: check if its used by some extension
      if (vocab != null) {
        // remove file too
        File f = getVocabFile(url);
        if (f.exists()) {
          f.delete();
        } else {
          log.warn("Local vocabulary copy missing, cant delete " + f.getAbsolutePath());
        }
      }
    }
  }

  public Vocabulary get(String uri) {
    if (uri == null) {
      return null;
    }
    URL url = uri2url.get(uri.toLowerCase());
    return vocabularies.get(url);
  }

  public Vocabulary get(URL url) {
    if (!vocabularies.containsKey(url)) {
      try {
        install(url);
      } catch (InvalidConfigException e) {
        log.error(e);
      } catch (IOException e) {
        log.error(e);
      }
    }
    return vocabularies.get(url);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.VocabulariesManager#getI18nVocab(java.lang.String, java.lang.String)
   */
  public Map<String, String> getI18nVocab(String uri, String lang, boolean sortAlphabetically) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    Vocabulary v = get(uri);
    if (v != null) {
      List<VocabularyConcept> concepts;
      if (sortAlphabetically) {
        concepts = new ArrayList<VocabularyConcept>(v.getConcepts());
        final String s = lang;
        Collections.sort(concepts, new Comparator<VocabularyConcept>() {
          public int compare(VocabularyConcept o1, VocabularyConcept o2) {
            return (o1.getPreferredTerm(s) == null ? o1.getIdentifier() : o1.getPreferredTerm(s).getTitle()).compareTo((o2.getPreferredTerm(s) == null
                ? o2.getIdentifier() : o2.getPreferredTerm(s).getTitle()));
          }
        });
      } else {
        concepts = v.getConcepts();
      }
      for (VocabularyConcept c : concepts) {
        VocabularyTerm t = c.getPreferredTerm(lang);
        map.put(c.getIdentifier(), t == null ? c.getIdentifier() : t.getTitle());
      }
    }
    if (map.isEmpty()) {
      log.debug("Empty i18n map for vocabulary " + uri + " and language " + lang);
    }
    return map;
  }

  private File getVocabFile(URL url) {
    String filename = url.toString().replaceAll("[/.:]+", "_") + ".vocab";
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  /**
   * Downloads vocabulary into local file for subsequent IPT startups
   * and adds the vocab to the internal cache.
   * Downloads use a conditional GET, i.e. only download the vocabulary files if the content has been changed since the
   * last download.
   * lastModified dates are taken from the filesystem.
   * 
   * @param url
   * @return
   * @throws IOException
   */
  private Vocabulary install(URL url) throws IOException, InvalidConfigException {
    Vocabulary v = null;
    if (url != null) {
      // the file to download to. It may exist already in which case we do a conditional download
      File vocabFile = getVocabFile(url);
      FileUtils.forceMkdir(vocabFile.getParentFile());
      if (downloadUtil.downloadIfChanged(url, vocabFile)) {
        // parse vocabulary file
        try {
          v = loadFromFile(vocabFile);
          addToCache(v, url);
          save();
        } catch (InvalidConfigException e) {
          log.error(e.getMessage(), e);
        }

      } else {
        log.info("Vocabulary " + url + " hasn't been modified since last download");
      }
    }
    return v;
  }

  public List<Vocabulary> list() {
    return new ArrayList<Vocabulary>(vocabularies.values());
  }

  public int load() {
    File dir = dataDir.configFile(CONFIG_FOLDER);
    // first load peristent uri2url map
    try {
      InputStream in = new FileInputStream(dataDir.configFile(CONFIG_FOLDER + "/" + PERSISTENCE_FILE));
      uri2url = (Map<String, URL>) xstream.fromXML(in);
      log.debug("Loaded uri2url vocabulary map with " + uri2url.size() + " entries");
    } catch (IOException e) {
      log.warn("Cannot load the uri2url mapping from datadir: " + e.getMessage());
    }
    // now iterate over all vocab files and load them
    int counter = 0;
    if (dir.isDirectory()) {
      List<File> files = new ArrayList<File>();
      FilenameFilter ff = new SuffixFileFilter(".vocab", IOCase.INSENSITIVE);
      files.addAll(Arrays.asList(dir.listFiles(ff)));
      for (File ef : files) {
        try {
          Vocabulary v = loadFromFile(ef);
          if (addToCache(v, uri2url.get(v.getUri().toLowerCase()))) {
            counter++;
          }
        } catch (InvalidConfigException e) {
          log.error("Cant load local vocabulary definition " + ef.getAbsolutePath(), e);
        }
      }
    }

    // we could be starting up for the very first time. Try to load default vocabs with URLs from registry
    // load mandatory vocabs
    Map<String, Vocabulary> registeredVocabs = null;
    for (String vuri : defaultVocabs) {
      if (!uri2url.containsKey(vuri.toLowerCase())) {
        if (registeredVocabs == null) {
          // lazy load list of all registered vocabularies
          registeredVocabs = registeredVocabs();
        }
        try {
          Vocabulary v = registeredVocabs.get(vuri);
          if (v == null) {
            log.warn("Default vocabulary " + vuri + " unknown to GBIF registry");
          } else {
            install(v.getUrl());
          }
        } catch (Exception e) {
          log.warn("Cant load default vocabulary " + vuri, e);
        }
      }
    }

    return counter;
  }

  private Vocabulary loadFromFile(File vocabFile) throws InvalidConfigException {
    Vocabulary v = null;
    // finally read in the new file and create the vocabulary object
    InputStream fileIn = null;
    try {
      fileIn = new FileInputStream(vocabFile);
      v = vocabFactory.build(fileIn);
      // read filesystem date
      Date modified = new Date(vocabFile.lastModified());
      v.setLastUpdate(modified);
      log.info("Successfully loaded Vocabulary: " + v.getTitle());
    } catch (FileNotFoundException e) {
      log.error("Cant find local vocabulary file", e);
    } catch (IOException e) {
      log.error("Cant access local vocabulary file", e);
    } catch (SAXException e) {
      log.error("Cant parse local vocabulary file", e);
    } catch (ParserConfigurationException e) {
      log.error("Cant create sax parser", e);
    } finally {
      if (fileIn != null) {
        try {
          fileIn.close();
        } catch (IOException e) {
        }
      }
    }
    return v;
  }

  private Map<String, Vocabulary> registeredVocabs() {
    Map<String, Vocabulary> registeredVocabs = new HashMap<String, Vocabulary>();
    for (Vocabulary v : registryManager.getVocabularies()) {
      if (v != null) {
        registeredVocabs.put(v.getUri(), v);
      }
    }
    // TODO: remove this "hack" once the registry is listing all vocabs fine
    try {
      Vocabulary v = new Vocabulary();
      v.setUri(Constants.VOCAB_URI_LANGUAGE);
      v.setUrl(new URL("http://rs.gbif.org/vocabulary/iso/639-1.xml"));
      registeredVocabs.put(Constants.VOCAB_URI_LANGUAGE, v);

      v = new Vocabulary();
      v.setUri(Constants.VOCAB_URI_COUNTRY);
      v.setUrl(new URL("http://rs.gbif.org/vocabulary/iso/3166-1_alpha2.xml"));
      registeredVocabs.put(Constants.VOCAB_URI_COUNTRY, v);

      v = new Vocabulary();
      v.setUri(Constants.VOCAB_URI_RESOURCE_TYPE);
      v.setUrl(new URL("http://rs.gbif.org/vocabulary/gbif/resource_type.xml"));
      registeredVocabs.put(Constants.VOCAB_URI_RESOURCE_TYPE, v);

      v = new Vocabulary();
      v.setUri(Constants.VOCAB_URI_RANKS);
      v.setUrl(new URL("http://rs.gbif.org/vocabulary/gbif/rank.xml"));
      registeredVocabs.put(Constants.VOCAB_URI_RANKS, v);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return registeredVocabs;
  }

  public void save() {
    // persist uri2url
    log.debug("Saving uri2url vocabulary map with " + uri2url.size() + " entries ...");
    Writer userWriter;
    try {
      userWriter = org.gbif.ipt.utils.FileUtils.startNewUtf8File(dataDir.configFile(CONFIG_FOLDER + "/"
          + PERSISTENCE_FILE));
      xstream.toXML(uri2url, userWriter);
    } catch (IOException e) {
      log.error("Cant write uri2url mapping", e);
    }
  }

  public UpdateResult updateAll() {
    UpdateResult result = new UpdateResult();
    for (Vocabulary v : vocabularies.values()) {
      log.debug("Updating vocabulary " + v.getUri());
      URL url = uri2url.get(v.getUri());
      File vocabFile = getVocabFile(url);
      Date modified = new Date(vocabFile.lastModified());
      try {
        install(url);
        Date modified2 = new Date(vocabFile.lastModified());
        if (modified.equals(modified2)) {
          // no update
          result.unchanged.add(v.getUri());
        } else {
          result.updated.add(v.getUri());
        }
      } catch (Exception e) {
        result.errors.put(v.getUri(), e.getMessage());
        log.error(e);
      }
    }
    return result;
  }

}
