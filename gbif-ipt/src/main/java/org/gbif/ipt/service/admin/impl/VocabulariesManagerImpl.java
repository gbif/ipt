/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.factory.VocabularyFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.VocabulariesManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
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

/**
 * Manager for all vocabulary related methods. Keeps an internal map of locally existing and parsed vocabularies which
 * is keyed on a normed filename derived from a vocabularies URL. We use this derived filename instead of the proper URL
 * as we do not persist any additional data than the extension file itself - which doesnt have its own URL embedded.
 */
@Singleton
public class VocabulariesManagerImpl extends BaseManager implements VocabulariesManager {
  // keep vocabularies accessible via their normed filename which is build from a vocabularies URL
  private Map<String, Vocabulary> vocabulariesByFilename = new HashMap<String, Vocabulary>();
  private static final String CONFIG_FOLDER = ".vocabularies";
  private VocabularyFactory vocabFactory;
  private HttpClient httpClient;

  @Inject
  public VocabulariesManagerImpl(VocabularyFactory vocabFactory, HttpClient httpClient) {
    super();
    this.vocabFactory = vocabFactory;
    this.httpClient = httpClient;
  }

  public void delete(URL url) {
    File f = getVocabFile(url);
    if (vocabulariesByFilename.containsKey(f.getName())) {
      Vocabulary vocab = vocabulariesByFilename.remove(f.getName());
      // TODO: check if its used by some extension
      if (f.exists()) {
        f.delete();
      } else {
        log.warn("Local vocabulary copy missing, cant delete " + url);
      }
    }
  }

  public Vocabulary get(URL url) {
    File f = getVocabFile(url);
    if (!vocabulariesByFilename.containsKey(f.getName())) {
      install(url);
    }
    return vocabulariesByFilename.get(f.getName());
  }

  private File getVocabFile(URL url) {
    String filename = url.toString().replaceAll("[/.:]+", "_") + ".xml";
    return dataDir.configFile(CONFIG_FOLDER + "/" + filename);
  }

  private Vocabulary install(URL url) {
    // download vocabulary into local file first for subsequent IPT startups
    File vocabFile = getVocabFile(url);
    Writer localVocabWriter = null;
    GetMethod method = new GetMethod(url.toString());
    method.setFollowRedirects(true);
    try {
      FileUtils.forceMkdir(vocabFile.getParentFile());
      localVocabWriter = new FileWriter(vocabFile);
      httpClient.executeMethod(method);
      InputStream is = method.getResponseBodyAsStream();
      IOUtils.copy(is, localVocabWriter);
      log.info("Successfully downloaded Vocabulary " + url);
    } catch (Exception e) {
      log.error(e);
    } finally {
      try {
        if (localVocabWriter != null) {
          localVocabWriter.close();
        }
        method.releaseConnection();
      } catch (RuntimeException e) {
      } catch (IOException e) {
      }
    }

    try {
      return loadFromFile(vocabFile);
    } catch (InvalidConfigException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Vocabulary> list() {
    return new ArrayList<Vocabulary>(vocabulariesByFilename.values());
  }

  public int load() {
    File dir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (dir.isDirectory()) {
      List<File> files = new ArrayList<File>();
      FilenameFilter ff = new SuffixFileFilter(".xml", IOCase.INSENSITIVE);
      files.addAll(Arrays.asList(dir.listFiles(ff)));
      for (File ef : files) {
        try {
          loadFromFile(ef);
          counter++;
        } catch (InvalidConfigException e) {
          log.error("Cant load local vocabulary definition " + ef.getAbsolutePath(), e);
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
      // keep vocab in local lookup
      vocabulariesByFilename.put(vocabFile.getName(), v);
      log.info("Successfully parsed Vocabulary: " + v.getTitle());
    } catch (FileNotFoundException e) {
      log.error("Cant find local vocabulary file", e);
    } catch (IOException e) {
      log.error("Cant access local vocabulary file", e);
    } catch (SAXException e) {
      log.error("Cant parse local vocabulary file", e);
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

  public void update(Vocabulary vocabulary) {
    // TODO Auto-generated method stub
  }
}
