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
import java.util.Locale.Category;

import com.google.common.base.Strings;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class EmlUtils {

  protected static final Logger LOG = Logger.getLogger(EmlUtils.class);

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
    if (!Strings.isNullOrEmpty(keyword) && !Strings.isNullOrEmpty(thesaurus)) {
      // capitalize incoming keyword, i.e., Occurrence, Specimen
      String capped = StringUtils.capitalize(keyword.toLowerCase());
      boolean found = false;
      for (KeywordSet ks : keywords) {
        String keywordThesaurus = ks.getKeywordThesaurus();
        if (!Strings.isNullOrEmpty(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
          String keywordString = ks.getKeywordsString();
          // update keywordString, only if empty
          if (Strings.isNullOrEmpty(keywordString)) {
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
    if (!Strings.isNullOrEmpty(thesaurus)) {
      for (Iterator<KeywordSet> iterator = keywords.iterator(); iterator.hasNext();) {
        String keywordThesaurus = iterator.next().getKeywordThesaurus();
        if (!Strings.isNullOrEmpty(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
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
    Locale currentLocale = Locale.getDefault(Category.FORMAT);
    try {
      synchronized (currentLocale) {
        Locale.setDefault(Category.FORMAT, locale);
        EmlWriter.writeEmlFile(emlFile, resource.getEml());
        Locale.setDefault(Category.FORMAT, currentLocale);
      }
    } catch (IOException e) {
      LOG.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource);
    } catch (TemplateException e) {
      LOG.error("EML template exception", e);
      throw new InvalidConfigException(TYPE.EML,
        "EML template exception when writing eml for " + resource + ": " + e.getMessage());
    } finally {
      Locale.setDefault(Category.FORMAT, currentLocale);
    }
  }

  /**
   * Reads an EML file using a specific Locale to interpret correctly: decimal separators, commas and currency formats.
   */
  public static Eml loadWithLocale(File emlFile, Locale locale) {
    Eml eml = null;
    Locale currentLocale = Locale.getDefault(Category.FORMAT);
    try {
      InputStream in = new FileInputStream(emlFile);
      synchronized (currentLocale) {
        Locale.setDefault(Category.FORMAT, locale);
        eml = EmlFactory.build(in);
        Locale.setDefault(Category.FORMAT, currentLocale);
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
      Locale.setDefault(Category.FORMAT, currentLocale);
    }
    return eml;
  }
}
