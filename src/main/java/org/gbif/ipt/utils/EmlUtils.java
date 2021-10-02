/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.metadata.eml.KeywordSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import freemarker.template.TemplateException;

/**
 * Utility class for common operation on EML documents/files.
 */
public class EmlUtils {

  protected static final Logger LOG = LogManager.getLogger(EmlUtils.class);

  /*
   * Empty constructor.
   */
  private EmlUtils() {
  }

  /**
   * Add or update KeywordSet identified by thesaurus name. If the KeywordSet is found and already contains a
   * a non empty and not null keywordString, its value is not overwritten.
   * 
   * @param keywords list of KeywordSet to add/update to
   * @param keyword keyword string
   * @param thesaurus thesaurus name
   */
  public static void addOrUpdateKeywordSet(List<KeywordSet> keywords, String keyword, String thesaurus) {
    if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(thesaurus)) {
      // capitalize incoming keyword, i.e., Occurrence, Specimen
      String capped = StringUtils.capitalize(keyword.toLowerCase());
      boolean found = false;
      for (KeywordSet ks : keywords) {
        String keywordThesaurus = ks.getKeywordThesaurus();
        if (StringUtils.isNotBlank(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
          String keywordString = ks.getKeywordsString();
          // update keywordString, only if empty
          if (StringUtils.isBlank(keywordString)) {
            ks.setKeywordsString(capped);
          }
          found = true;
          break;
        }
      }
      // if no match, add new KeywordSet
      if (!found) {
        KeywordSet ks = new KeywordSet();
        ks.setKeywordThesaurus(thesaurus);
        ks.setKeywordsString(capped);
        // add new KeywordSet
        keywords.add(ks);
      }
    }
  }

  /**
   * Remove all KeywordSet with specific thesaurus name.
   * 
   * @param keywords list of KeywordSet to remove from
   * @param thesaurus thesaurus name
   */
  public static void removeKeywordSet(List<KeywordSet> keywords, String thesaurus) {
    if (StringUtils.isNotBlank(thesaurus)) {
      for (Iterator<KeywordSet> iterator = keywords.iterator(); iterator.hasNext();) {
        String keywordThesaurus = iterator.next().getKeywordThesaurus();
        if (StringUtils.isNotBlank(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Writes the EML file using a specific locale to interpret correctly: decimal separators, commas and currency
   * formats.
   */
  public static void writeWithLocale(File emlFile, Resource resource, Locale locale) {
    Locale currentLocale = Locale.getDefault();
    try {
      synchronized (currentLocale) {
        Locale.setDefault(locale);
        EmlWriter.writeEmlFile(emlFile, resource.getEml());
        Locale.setDefault(currentLocale);
      }
    } catch (IOException e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource, e);
    } catch (TemplateException e) {
      LOG.error("EML template exception", e);
      throw new InvalidConfigException(TYPE.EML, "EML template exception when writing eml for " + resource + ": " + e.getMessage(), e);
    } finally {
      Locale.setDefault(currentLocale);
    }
  }

  /**
   * Reads an EML file using a specific Locale to interpret correctly: decimal separators, commas and currency formats.
   *
   * @param emlFile EML file to read from
   * @param locale  Locale to use when interpreting EML file
   *
   * @return EML file read from file, or new EML instance if the file to load from did not exist, or could not be parsed
   */
  public static Eml loadWithLocale(File emlFile, Locale locale) {
    Eml eml = null;
    Locale currentLocale = Locale.getDefault();
    try {
      InputStream in = new FileInputStream(emlFile);
      synchronized (currentLocale) {
        Locale.setDefault(locale);
        eml = EmlFactory.build(in);
        Locale.setDefault(currentLocale);
      }
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (IOException e) {
      LOG.error(e);
    } catch (SAXException e) {
      LOG.error("Invalid EML document", e);
      eml = new Eml();
    } catch (Exception e) {
      eml = new Eml();
    } finally {
      Locale.setDefault(currentLocale);
    }
    return eml;
  }
}
