package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.http.StatusLine;
import org.xml.sax.SAXException;

import static org.gbif.utils.HttpUtil.success;

/**
 * Manager for all vocabulary related methods. Keeps an internal map of locally existing and parsed vocabularies which
 * is keyed on a normed filename derived from a vocabularies URL. We use this derived filename instead of the proper
 * URL as we don't persist any more data than the extension file itself - which doesn't have its own URL embedded.
 */
@Singleton
public class VocabulariesManagerImpl extends BaseManager implements VocabulariesManager {

  // local lookup
  private Map<String, Vocabulary> vocabulariesById = Maps.newHashMap();
  public static final String CONFIG_FOLDER = ".vocabularies";
  public static final String VOCAB_FILE_SUFFIX = ".vocab";
  private VocabularyFactory vocabFactory;
  private HttpUtil downloader;
  private final RegistryManager registryManager;

  // these vocabularies are always updated on startup of the IPT
  private static final List<String> DEFAULT_VOCABS = ImmutableList
    .of(Constants.VOCAB_URI_LANGUAGE, Constants.VOCAB_URI_COUNTRY, Constants.VOCAB_URI_DATASET_TYPE,
      Constants.VOCAB_URI_RANKS, Constants.VOCAB_URI_ROLES, Constants.VOCAB_URI_PRESERVATION_METHOD,
      Constants.VOCAB_URI_DATASET_SUBTYPES, Constants.VOCAB_URI_UPDATE_FREQUENCIES);

  private ConfigWarnings warnings;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private BaseAction baseAction;

  @Inject
  public VocabulariesManagerImpl(AppConfig cfg, DataDir dataDir, VocabularyFactory vocabFactory, HttpUtil httpUtil,
    RegistryManager registryManager, ConfigWarnings warnings, SimpleTextProvider textProvider,
    RegistrationManager registrationManager) {
    super(cfg, dataDir);
    this.vocabFactory = vocabFactory;
    this.downloader = httpUtil;
    this.registryManager = registryManager;
    this.warnings = warnings;
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  /**
   * Uninstall vocabulary by its unique identifier.
   *
   * @param identifier identifier of vocabulary to uninstall
   */
  private void uninstall(String identifier) {
    if (vocabulariesById.containsKey(identifier)) {
      // 1. delete persisted vocab file
      Vocabulary toUninstall = vocabulariesById.get(identifier);
      File f = getVocabFile(toUninstall.getUriResolvable());
      if (f.exists()) {
        f.delete();
        LOG.debug("Successfully deleted (uninstalled) vocabulary file: " + f.getAbsolutePath());
      } else {
        LOG.warn("Vocabulary file doesn't exist locally - can't delete: " + f.getAbsolutePath());
      }
      // 2. delete from local lookup
      vocabulariesById.remove(identifier);
    } else {
      LOG.warn("Vocabulary not installed locally, can't uninstall: " + identifier);
    }
  }

  @Override
  public Vocabulary get(String identifier) {
    Preconditions.checkNotNull(identifier);
    return vocabulariesById.get(identifier);
  }

  @Override
  public Vocabulary get(URL url) {
    Preconditions.checkNotNull(url);
    for (Vocabulary v : list()) {
      if (v.getUriResolvable() != null) {
        try {
          if (v.getUriResolvable().compareTo(url.toURI()) == 0) {
            return v;
          }
        } catch (URISyntaxException e) {
          LOG.error("Getting vocabulary by URL failed", e);
        }
      }
    }
    return null;
  }

  @Override
  public Map<String, String> getI18nVocab(String identifier, String lang, boolean sortAlphabetically) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    Vocabulary v = get(identifier);
    if (v != null) {
      List<VocabularyConcept> concepts;
      if (sortAlphabetically) {
        concepts = new ArrayList<VocabularyConcept>(v.getConcepts());
        final String s = lang;
        Collections.sort(concepts, new Comparator<VocabularyConcept>() {

          @Override
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
      LOG.error("Empty i18n map for vocabulary " + identifier + " and language " + lang);
    }
    return map;
  }

  /**
   * Retrieve vocabulary file by its resolvable URI.
   *
   * @param uri resolvable URI of vocabulary to retrieve
   *
   * @return vocabulary file
   */
  private File getVocabFile(URI uri) {
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(uri.toString(), VOCAB_FILE_SUFFIX);
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  @Override
  public synchronized Vocabulary install(URL url) throws InvalidConfigException {
    Preconditions.checkNotNull(url);

    try {
      File tmpFile = download(url);
      Vocabulary vocabulary = loadFromFile(tmpFile);
      vocabulary.setUriResolvable(url.toURI());
      finishInstall(tmpFile, vocabulary);
      return vocabulary;
    } catch (InvalidConfigException e) {
      throw e;
    } catch (Exception e) {
      String msg = baseAction.getText("admin.vocabulary.install.error", new String[] {url.toString()});
      LOG.error(msg, e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_EXTENSION, msg, e);
    }
  }

  /**
   * Move and rename temporary file to final version. Update vocabulary loaded into local lookup.
   *
   * @param tmpFile    downloaded vocabulary file (in temporary location with temporary filename)
   * @param vocabulary vocabulary from JSON (excluding concepts)
   *
   * @throws IOException if moving file fails
   */
  private void finishInstall(File tmpFile, Vocabulary vocabulary) throws IOException {
    Preconditions.checkNotNull(tmpFile);
    Preconditions.checkNotNull(vocabulary);
    Preconditions.checkNotNull(vocabulary.getUriString());

    try {
      File installedFile = getVocabFile(vocabulary.getUriResolvable());
      // never replace an existing vocabulary file. It can only be uninstalled (removed), or updated
      if (!installedFile.exists()) {
        FileUtils.moveFile(tmpFile, installedFile);
      }

      // build Vocabulary from file, so it includes concepts
      Vocabulary fromFile = loadFromFile(installedFile);
      // don't forget to set vocabulary URL (only available from JSON)
      fromFile.setUriResolvable(vocabulary.getUriResolvable());

      // keep vocabulary in local lookup: allowed one installed vocabulary per identifier
      vocabulariesById.put(vocabulary.getUriString(), fromFile);
    } catch (IOException e) {
      LOG.error("Installing vocabulary failed, while trying to move and rename vocabulary file: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Download a vocabulary into temporary file and return it.
   *
   * @param url URL of vocabulary to download
   *
   * @return temporary file vocabulary was downloaded to, or null if it failed to be downloaded
   */
  private File download(URL url) throws IOException {
    Preconditions.checkNotNull(url);
    String filename = url.toString().replaceAll("[/:.]+", "_") + ".xml";
    File tmpFile = dataDir.tmpFile(filename);
    StatusLine statusLine = downloader.download(url, tmpFile);
    if (success(statusLine)) {
      LOG.info("Successfully downloaded vocabulary: " + url.toString());
      return tmpFile;
    } else {
      String msg =
        "Failed to download vocabulary: " + url.toString() + ". Response=" + String.valueOf(statusLine.getStatusCode());
      LOG.error(msg);
      throw new IOException(msg);
    }
  }

  @Override
  public List<Vocabulary> list() {
    return new ArrayList<Vocabulary>(vocabulariesById.values());
  }

  @Override
  public int load() {
    Map<String, Vocabulary> fileNameToVocabulary = getFileNameToVocabularyMap();
    int counter = 0;
    if (!fileNameToVocabulary.isEmpty()) {
      // now iterate over all vocab files and load them
      File dir = dataDir.configFile(CONFIG_FOLDER);
      if (dir.isDirectory()) {
        List<File> files = new ArrayList<File>();
        FilenameFilter ff = new SuffixFileFilter(VOCAB_FILE_SUFFIX, IOCase.INSENSITIVE);
        files.addAll(Arrays.asList(dir.listFiles(ff)));
        for (File vf : files) {
          try {
            Vocabulary v = loadFromFile(vf);
            if (fileNameToVocabulary.containsKey(vf.getName())) {
              if (!vocabulariesById.containsKey(v.getUriString())) {
                // populate vocabulary's resolvable URI (needed in order to properly uninstall vocabulary later)
                v.setUriResolvable(fileNameToVocabulary.get(vf.getName()).getUriResolvable());
                // keep vocabulary in local lookup: allowed one installed vocabulary per identifier
                vocabulariesById.put(v.getUriString(), v);
                counter++;
              } else {
                // skip - was loaded already
                counter++;
              }
            } else {
              LOG.warn("An invalid vocabulary has been encountered and will be deleted: " + vf.getAbsolutePath());
              FileUtils.deleteQuietly(vf);
            }
          } catch (InvalidConfigException e) {
            warnings.addStartupError("Failed to load vocabulary definition file: " + vf.getAbsolutePath(), e);
          }
        }
      }
    }
    return counter;
  }

  /**
   * Iterate through all registered vocabularies and populate a map where each key is the name of the file if it were
   * persisted, and the value is the Vocabulary object.
   *
   * @return map containing all registered vocabularies
   */
  private Map<String, Vocabulary> getFileNameToVocabularyMap() {
    Map<String, Vocabulary> map = Maps.newHashMap();
    try {
      for (Vocabulary v : registryManager.getVocabularies()) {
        if (v.getUriString() != null && v.getUriResolvable() != null) {
          String filename =
            org.gbif.ipt.utils.FileUtils.getSuffixedFileName(v.getUriResolvable().toString(), VOCAB_FILE_SUFFIX);
          map.put(filename, v);
        }
      }
    } catch (RegistryException e) {
      // add startup error message about Registry error
      String msg = RegistryException.logRegistryException(e, baseAction);
      warnings.addStartupError(msg);
      LOG.error(msg, e);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.extensions.vocabularies.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    }
    return map;
  }

  @Override
  public synchronized void installOrUpdateDefaults() throws InvalidConfigException, RegistryException {
    // all registered vocabularies
    List<Vocabulary> vocabularies = registryManager.getVocabularies();

    for (Vocabulary latest : getLatestDefaults(vocabularies)) {
      Vocabulary installed = null;
      for (Vocabulary vocabulary : list()) {
        if (latest.getUriString().equalsIgnoreCase(vocabulary.getUriString())) {
          installed = vocabulary;
          break;
        }
      }

      if (installed == null) {
        try {
          URL url = latest.getUriResolvable().toURL();
          try {
            install(url);
          } catch (Exception e) {
            // All are likely to be HTTP connection errors
            throw new RegistryException(url.toString(), e);
          }
        } catch (MalformedURLException e) {
          throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_VOCABULARY,
              "Vocabulary has an invalid URL: " + latest.getUriResolvable().toString());
        }
      } else {
        try {
          updateToLatest(installed, latest);
        } catch (IOException e) {
          throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR,
              "Can't update default vocabulary: " + installed.getUriString(), e);
        }
      }
    }
    // update each installed vocabulary indicating whether it is the latest version (for its identifier) or not
    updateIsLatest(list(), vocabularies);
  }

  /**
   * Return the latest versions of default vocabularies (that the IPT is configured to use) from the registry.
   *
   * @return list containing latest versions of default vocabularies
   */
  private List<Vocabulary> getLatestDefaults(List<Vocabulary> registered) {
    List<Vocabulary> defaults = Lists.newArrayList();
    for (Vocabulary v : registered) {
      if (v.getUriString() != null && DEFAULT_VOCABS.contains(v.getUriString()) && v.isLatest()) {
        defaults.add(v);
      }
    }

    // throw exception if not all default vocabularies could not be loaded
    if (DEFAULT_VOCABS.size() != defaults.size()) {
      String msg = "Not all default vocabularies were loaded!";
      LOG.error(msg);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR, msg);
    }
    return defaults;
  }

  /**
   * Load the Vocabulary object from the XML definition file.
   *
   * @param localFile vocabulary XML definition file
   *
   * @return vocabulary loaded from file
   *
   * @throws InvalidConfigException if vocabulary could not be loaded successfully
   */
  private Vocabulary loadFromFile(File localFile) throws InvalidConfigException {
    Preconditions.checkNotNull(localFile);
    Preconditions.checkState(localFile.exists());

    Closer closer = Closer.create();
    try {
      InputStream fileIn = closer.register(new FileInputStream(localFile));
      Vocabulary v = vocabFactory.build(fileIn);
      v.setModified(new Date(localFile.lastModified())); // filesystem date
      LOG.info("Successfully loaded vocabulary: " + v.getUriString());
      return v;
    } catch (IOException e) {
      LOG.error("Can't access local vocabulary file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_VOCABULARY,
        "Can't access local vocabulary file");
    } catch (SAXException e) {
      LOG.error("Can't parse local extension file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_VOCABULARY,
        "Can't parse local vocabulary file");
    } catch (ParserConfigurationException e) {
      LOG.error("Can't create sax parser", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_VOCABULARY, "Can't create sax parser");
    } finally {
      try {
        closer.close();
      } catch (IOException e) {
        LOG.debug("Failed to close input stream on vocabulary file", e);
      }
    }
  }

  /**
   * Iterate through list of installed vocabularies. Update each one, indicating if it is the latest version or not.
   */
  @VisibleForTesting
  protected void updateIsLatest(List<Vocabulary> vocabularies, List<Vocabulary> registered) {
    if (!vocabularies.isEmpty() && !registered.isEmpty()) {
      for (Vocabulary vocabulary : vocabularies) {
        // is this the latest version?
        for (Vocabulary rVocabulary : registered) {
          if (vocabulary.getUriString() != null && rVocabulary.getUriString() != null) {
            String idOne = vocabulary.getUriString();
            String idTwo = rVocabulary.getUriString();
            // first compare on identifier
            if (idOne.equalsIgnoreCase(idTwo)) {
              Date issuedOne = vocabulary.getIssued();
              Date issuedTwo = rVocabulary.getIssued();
              // next compare on issued date: can both be null, or issued date must be same
              if ((issuedOne == null && issuedTwo == null) || (issuedOne != null && issuedTwo != null
                                                               && issuedOne.compareTo(issuedTwo) == 0)) {
                vocabulary.setLatest(rVocabulary.isLatest());
              }
            }
          }
        }
        LOG.debug(
          "Installed vocabulary with identifier " + vocabulary.getUriString() + " latest=" + vocabulary.isLatest());
      }
    }
  }

  /**
   * Update an installed vocabulary to the latest version.
   *
   * @param installed version of vocabulary installed
   * @param latestVersion latest version of vocabulary (not installed yet)
   *
   * @throws IOException if latest version cannot be downloaded
   * @throws InvalidConfigException if latest version cannot be read
   */
  private void updateToLatest(Vocabulary installed, Vocabulary latestVersion)
    throws IOException, InvalidConfigException {
    if (installed != null && latestVersion != null) {

      boolean isNewVersion = false;
      Date issued = installed.getIssued();
      Date issuedLatest = latestVersion.getIssued();
      if (issued == null && issuedLatest != null) {
        isNewVersion = true;
      } else if (issued != null && issuedLatest != null) {
        isNewVersion = (issuedLatest.compareTo(issued) > 0); // latest version must have newer issued date
      }

      if (isNewVersion && latestVersion.getUriResolvable() != null) {
        // first download latestVersion XML file
        File tmpFile = download(latestVersion.getUriResolvable().toURL());
        // uninstall old version, then install new version
        uninstall(installed.getUriString());
        finishInstall(tmpFile, latestVersion);
      }
    }
  }

  @Override
  public synchronized boolean updateIfChanged(String identifier) throws IOException, RegistryException {
    // identify installed vocabulary by identifier
    Vocabulary installed = get(identifier);
    if (installed != null) {
      // match vocabulary by identifier and issued date
      Vocabulary matched = null;
      for (Vocabulary v : registryManager.getVocabularies()) {
        if (v.getUriString() != null && v.getUriString().equalsIgnoreCase(identifier)) {
          if (installed.getIssued() == null
              || (installed.getIssued() != null && v.getIssued() != null && installed.getIssued().compareTo(v.getIssued()) < 0)) {
            matched = v;
            break;
          }
        }
      }
      // verify the version was updated
      if (matched != null && matched.getUriResolvable() != null) {
        File vocabFile = getVocabFile(matched.getUriResolvable());
        return downloader.downloadIfChanged(matched.getUriResolvable().toURL(), vocabFile);
      }

    }
    return false;
  }
}
