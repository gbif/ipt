package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

/**
 * Manager for all vocabulary related methods. Keeps an internal map of locally existing and parsed vocabularies which
 * is keyed on a normed filename derived from a vocabularies URL. We use this derived filename instead of the proper
 * URL
 * as we do not persist any additional data than the extension file itself - which doesnt have its own URL embedded.
 */
@Singleton
public class VocabulariesManagerImpl extends BaseManager implements VocabulariesManager {

  public static class UpdateResult {

    // key=uri
    public Set<String> updated = new HashSet<String>();
    // key=uri
    public Set<String> unchanged = new HashSet<String>();
    // key=uri, value=error text
    public Map<String, String> errors = new HashMap<String, String>();
  }

  private Map<URI, Vocabulary> vocabularies = new HashMap<URI, Vocabulary>();
  // Vocabulary identifier, to Vocabulary resolvable URI
  private Map<String, URI> id2uri = new HashMap<String, URI>();
  protected static final String CONFIG_FOLDER = ".vocabularies";
  public static final String PERSISTENCE_FILE = "vocabularies.xml";
  private static final String VOCAB_FILE_SUFFIX = ".vocab";
  private VocabularyFactory vocabFactory;
  private HttpUtil downloadUtil;
  private final XStream xstream = new XStream();
  private final RegistryManager registryManager;
  private final ExtensionManager extensionManager;
  // these vocabularies are always updated on startup of the IPT
  private final String[] defaultVocabs =
  {Constants.VOCAB_URI_LANGUAGE, Constants.VOCAB_URI_COUNTRY, Constants.VOCAB_URI_DATASET_TYPE,
    Constants.VOCAB_URI_RANKS, Constants.VOCAB_URI_ROLES, Constants.VOCAB_URI_PRESERVATION_METHOD,
    Constants.VOCAB_URI_DATASET_SUBTYPES, Constants.VOCAB_URI_UPDATE_FREQUENCIES};
  private ConfigWarnings warnings;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private BaseAction baseAction;

  @Inject
  public VocabulariesManagerImpl(AppConfig cfg, DataDir dataDir, VocabularyFactory vocabFactory,
    DefaultHttpClient client, RegistryManager registryManager, ExtensionManager extensionManager,
    ConfigWarnings warnings, SimpleTextProvider textProvider, RegistrationManager registrationManager) {
    super(cfg, dataDir);
    this.vocabFactory = vocabFactory;
    this.downloadUtil = new HttpUtil(client);
    this.registryManager = registryManager;
    this.extensionManager = extensionManager;
    this.warnings = warnings;
    defineXstreamMapping();
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  private boolean addToCache(Vocabulary v, URI uriObject) {
    if (uriObject == null) {
      log.error("Cannot add vocabulary " + v.getUriString() + " to cache without a valid URL");
      return false;
    }
    id2uri.put(v.getUriString().toLowerCase(), uriObject);
    // keep vocab in local lookup
    if (vocabularies.containsKey(uriObject)) {
      log.warn("Vocabulary URI " + v.getUriString() + " exists already - overwriting with new vocabulary from "
        + uriObject);
    }
    vocabularies.put(uriObject, v);
    return true;
  }

  private void defineXstreamMapping() {
  }

  public void delete(String uriString) throws DeletionNotAllowedException {
    if (uriString != null) {
      URI uriObject = id2uri.get(uriString.toLowerCase());
      Vocabulary vocab = get(uriObject);
      if (vocab != null) {
        // is its a basic IPT vocab?
        for (String defaultUri : defaultVocabs) {
          if (defaultUri.equalsIgnoreCase(uriString)) {
            throw new DeletionNotAllowedException(Reason.BASE_VOCABULARY);
          }
        }
        // check if its used by some extension
        for (Extension ext : extensionManager.list()) {
          for (Vocabulary v : ext.listVocabularies()) {
            if (uriString.equalsIgnoreCase(v.getUriString())) {
              throw new DeletionNotAllowedException(Reason.VOCABULARY_USED_IN_EXTENSION,
                "Vocabulary used by extension " + ext.getRowType());
            }
          }
        }
        // remove vocab
        vocabularies.remove(uriObject);
        // remove file too
        File f = getVocabFile(uriObject);
        if (f.exists()) {
          f.delete();
        } else {
          log.warn("Local vocabulary copy missing, cant delete " + f.getAbsolutePath());
        }
      }
    }
  }

  public Vocabulary get(String uriString) {
    if (uriString == null) {
      return null;
    }
    URI uriObject = id2uri.get(uriString.toLowerCase());
    return vocabularies.get(uriObject);
  }

  public Vocabulary get(URI uriObject) {
    if (!vocabularies.containsKey(uriObject)) {
      try {
        install(uriObject);
      } catch (InvalidConfigException e) {
        log.error(e);
      } catch (IOException e) {
        log.error(e);
      }
    }
    return vocabularies.get(uriObject);
  }

  public Map<String, String> getI18nVocab(String uriString, String lang, boolean sortAlphabetically) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    Vocabulary v = get(uriString);
    if (v != null) {
      List<VocabularyConcept> concepts;
      if (sortAlphabetically) {
        concepts = new ArrayList<VocabularyConcept>(v.getConcepts());
        final String s = lang;
        Collections.sort(concepts, new Comparator<VocabularyConcept>() {

          public int compare(VocabularyConcept o1, VocabularyConcept o2) {
            return (o1.getPreferredTerm(s) == null ? o1.getIdentifier() : o1.getPreferredTerm(s).getTitle())
              .compareTo((o2.getPreferredTerm(s) == null ? o2.getIdentifier() : o2.getPreferredTerm(s).getTitle()));
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
      log.debug("Empty i18n map for vocabulary " + uriString + " and language " + lang);
    }
    return map;
  }

  private File getVocabFile(URI uriObject) {
    String filename = uriObject.toString().replaceAll("[/.:]+", "_") + VOCAB_FILE_SUFFIX;
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  /**
   * Downloads vocabulary into local file for subsequent IPT startups and adds the vocab to the internal cache.
   * Downloads use a conditional GET, i.e. only download the vocabulary files if the content has been changed since the
   * last download.
   * lastModified dates are taken from the filesystem.
   */
  private Vocabulary install(URI uriObject) throws IOException, InvalidConfigException {
    Vocabulary v = null;
    if (uriObject != null) {
      // the file to download to. It may exist already in which case we do a conditional download
      File vocabFile = getVocabFile(uriObject);
      FileUtils.forceMkdir(vocabFile.getParentFile());
      // URI -> URL, used in download
      if (downloadUtil.downloadIfChanged(uriObject.toURL(), vocabFile)) {
        // parse vocabulary file
        try {
          v = loadFromFile(vocabFile);
          addToCache(v, uriObject);
          save();
        } catch (InvalidConfigException e) {
          warnings.addStartupError("Cannot install vocabulary " + uriObject, e);
        }

      } else {
        log.info("Vocabulary " + uriObject + " hasn't been modified since last download");
      }
    }
    return v;
  }

  public List<Vocabulary> list() {
    return new ArrayList<Vocabulary>(vocabularies.values());
  }

  /**
   * First, the vocabularies.xml file, that stores information about the list of installed vocabularies, previous to
   * 2.0.4, the IPT stored the resolvable vocabulary address in a URL object. Now, it stores it as a URI. To avoid
   * startup errors updating existing IPT installations, this method should be called to help transition
   * the vocabularies.xml to use this new format.
   * </p>
   * Second, any vocabularies that are deprecated and will never be referenced again from any extension or used again
   * in the IPT, must be removed. Otherwise, the IPT will try to download the vocabulary which no loner exists.
   */
  private void transitionVocabulariesBetweenVersions() {
    // first load persistent id2uri map
    InputStream in = null;
    try {
      in = new FileInputStream(dataDir.configFile(CONFIG_FOLDER + "/" + PERSISTENCE_FILE));
      // as of 2.0.4, need to transition id2url from Map<String, URL -> Map<String, URI>
      Map<String, Object> tempId2uri = (Map<String, Object>) xstream.fromXML(in);
      for (Map.Entry<String, Object> entry : tempId2uri.entrySet()) {
        try {
          String st = entry.getValue().toString();
          URI uri = new URI(st);
          id2uri.put(entry.getKey(), uri);
        } catch (URISyntaxException e1) {
          // log error, clear vocabs dir, try download them all again
          log.error("URL could not be converted to URI - check vocabularies.xml");
        }
      }
    } catch (IOException e) {
      log.warn("Cannot load the id2uri mapping from datadir (This is normal when first setting up a new datadir)");
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.error("InputStream on vocabularies.xml could not be closed");
        }
      }
    }
    // remove the old vocabularies.xml file - it will soon get rewritten..
    FileUtils.deleteQuietly(dataDir.configFile(CONFIG_FOLDER + "/" + PERSISTENCE_FILE));

    // before rewriting, take the chance to remove any deprecated vocabularies that no longer should be persisted/loaded
    if (id2uri.containsKey(Constants.DEPRECATED_VOCAB_URI_RESOURCE_TYPE)) {
      id2uri.remove(Constants.DEPRECATED_VOCAB_URI_RESOURCE_TYPE);
    }
    // ensure the actual deprecated vocab file is also removed
    File dep1 = dataDir.configFile(CONFIG_FOLDER + "/" + Constants.DEPRECATED_VOCAB_URL_RESOLVABLE_RESOURCE_TYPE);
    if (dep1.exists()) {
      FileUtils.deleteQuietly(dep1);
    }
    // rewrite vocabularies.xml
    save();
  }

  public int load() {
    File vocabularies = dataDir.configFile(CONFIG_FOLDER + "/" + PERSISTENCE_FILE);
    // for IPTs version 2.0.3 or earlier: must transition vocabularies.xml to store vocabulary address as URI vs URL
    if (vocabularies.exists()) {
      transitionVocabulariesBetweenVersions();
    }

    // now iterate over all vocab files and load them
    File dir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (dir.isDirectory()) {
      List<File> files = new ArrayList<File>();
      FilenameFilter ff = new SuffixFileFilter(VOCAB_FILE_SUFFIX, IOCase.INSENSITIVE);
      files.addAll(Arrays.asList(dir.listFiles(ff)));
      for (File ef : files) {
        try {
          Vocabulary v = loadFromFile(ef);
          if (v != null && addToCache(v, id2uri.get(v.getUriString().toLowerCase()))) {
            counter++;
          }
        } catch (InvalidConfigException e) {
          warnings.addStartupError("Cant load local vocabulary definition " + ef.getAbsolutePath(), e);
        }
      }
    }

    // we could be starting up for the very first time. Try to load mandatory/default vocabs with URIs from registry
    Map<String, URI> registeredVocabs = null;
    for (String vuriString : defaultVocabs) {
      if (!id2uri.containsKey(vuriString.toLowerCase())) {
        // lazy load list of all registered vocabularies
        registeredVocabs = registeredVocabs();
        // provided that at least some vocabularies were loaded, proceed loading default vocabularies
        if (!registeredVocabs.isEmpty()) {
          try {
            URI vuriObject = registeredVocabs.get(vuriString);
            if (vuriObject == null) {
              log.warn("Default vocabulary " + vuriString + " unknown to GBIF registry");
            } else {
              install(vuriObject);
              // increment counter, since these haven't been loaded yet
              counter++;
            }
          } catch (Exception e) {
            warnings.addStartupError(baseAction.getTextWithDynamicArgs("admin.extensions.vocabulary.couldnt.load",
              new String[] {vuriString, cfg.getRegistryUrl()}));
          }
        }
      }
    }

    return counter;
  }

  private Vocabulary loadFromFile(File vocabFile) throws InvalidConfigException {
    Vocabulary v = null;
    // finally read in the new file and create the vocabulary object
    Closer closer = Closer.create();
    String fileName = (vocabFile.exists()) ? vocabFile.getName() : "";
    try {
      InputStream fileIn = closer.register(new FileInputStream(vocabFile));
      v = vocabFactory.build(fileIn);
      // read filesystem date
      Date modified = new Date(vocabFile.lastModified());
      v.setLastUpdate(modified);
      log.info("Successfully loaded Vocabulary: " + v.getTitle());
    } catch (FileNotFoundException e) {
      warnings.addStartupError("Cant find local vocabulary file: " + fileName, e);
    } catch (IOException e) {
      warnings.addStartupError("Cant access local vocabulary file: " + fileName, e);
    } catch (SAXException e) {
      warnings.addStartupError("Cant parse local vocabulary file: " + fileName, e);
    } catch (ParserConfigurationException e) {
      warnings.addStartupError("Cant create sax parser", e);
    } finally {
      try {
        closer.close();
      } catch (IOException e) {
      }
    }
    return v;
  }

  /**
   * Retrieves a Map of registered vocabularies. The key is equal to the vocabulary URI String. The value is equal to
   * the
   * vocabulary URI object.
   * 
   * @return Map of registered vocabularies
   */
  private Map<String, URI> registeredVocabs() {
    Map<String, URI> registeredVocabs = new HashMap<String, URI>();
    try {
      for (Vocabulary v : registryManager.getVocabularies()) {
        if (v != null) {
          registeredVocabs.put(v.getUriString(), v.getUriResolvable());
        }
      }
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), baseAction);
      // add startup error message about Registry error
      warnings.addStartupError(msg);
      log.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.extensions.vocabularies.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      log.error(msg);
    }
    return registeredVocabs;
  }

  public synchronized void save() {
    // persist uri2url
    log.debug("Saving id2uri vocabulary map with " + id2uri.size() + " entries ...");
    Writer userWriter;
    try {
      userWriter =
        org.gbif.ipt.utils.FileUtils.startNewUtf8File(dataDir.configFile(CONFIG_FOLDER + "/" + PERSISTENCE_FILE));
      xstream.toXML(id2uri, userWriter);
    } catch (IOException e) {
      log.error("Cant write id2uri mapping", e);
    }
  }

  public UpdateResult updateAll() {
    UpdateResult result = new UpdateResult();
    // list all known vocab URIs in debug log
    log.info("Updating all installed vocabularies");
    log.debug("Known vocabulary locations for URIs: " + StringUtils.join(id2uri.keySet(), ", "));
    for (Vocabulary v : vocabularies.values()) {
      if (v.getUriString() == null) {
        log.warn("Vocabulary without identifier, skipped!");
        continue;
      }
      log.debug("Updating vocabulary " + v.getUriString());
      URI uriObject = id2uri.get(v.getUriString().toLowerCase());
      if (uriObject == null) {
        String msg =
          "Dont know the vocabulary URL to retrieve update from for vocabulary Identifier " + v.getUriString();
        result.errors.put(v.getUriString(), msg);
        log.error(msg);
        continue;
      }
      File vocabFile = getVocabFile(uriObject);
      Date modified = new Date(vocabFile.lastModified());
      try {
        install(uriObject);
        Date modified2 = new Date(vocabFile.lastModified());
        if (modified.equals(modified2)) {
          // no update
          result.unchanged.add(v.getUriString());
        } else {
          result.updated.add(v.getUriString());
        }
      } catch (Exception e) {
        result.errors.put(v.getUriString(), e.getMessage());
        log.error(e);
      }
    }
    return result;
  }
}
