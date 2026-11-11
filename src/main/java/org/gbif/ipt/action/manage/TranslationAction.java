/*
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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import freemarker.ext.beans.SimpleMapModel;
import lombok.Getter;

public class TranslationAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -8350422710092468050L;

  private static final Logger LOG = LogManager.getLogger(TranslationAction.class);

  private final SourceManager sourceManager;
  private final VocabulariesManager vocabManager;
  @Getter
  private final ValueTranslation translation = new ValueTranslation();

  protected static final String REQ_PARAM_TERM = "term";
  protected static final String REQ_PARAM_ROWTYPE = "rowtype";
  protected static final String REQ_PARAM_MAPPINGID = "mid";
  // config
  @Getter
  private PropertyMapping field;
  @Getter
  private ExtensionProperty property;
  private ExtensionMapping mapping;
  @Getter
  private SimpleMapModel vocabTerms;
  @Getter
  private Integer mid;

  @Inject
  public TranslationAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      SourceManager sourceManager,
      VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
    this.vocabManager = vocabManager;
    defaultResult = SUCCESS;
  }

  /**
   * Automatically map source values not translated yet to the terms in a vocabulary. A match occurs when the source
   * value matches one of the vocabulary's term's name, preferred name, or alternate name, ignoring case, and failing
   * that also ignoring whitespace, underscores and hyphens.
   *
   * @return SUCCESS result, staying on the translation page
   */
  public String automap() {
    if (notFound) {
      return NOT_FOUND;
    }
    if (property == null || property.getVocabulary() == null) {
      addActionError(getText("manage.translation.cantfind.vocabulary"));
    } else {
      Vocabulary vocab = property.getVocabulary();
      int count = translation.automap(value -> {
        VocabularyConcept vc = vocab.findConcept(value);
        if (vc == null) {
          vc = vocab.getConcepts().stream()
              .filter(c -> looselyMatches(c, value))
              .findFirst()
              .orElse(null);
        }
        return vc != null ? vc.getIdentifier() : null;
      });
      addActionMessage(getText("manage.translation.mapped.terms", new String[]{String.valueOf(count)}));
    }
    return SUCCESS;
  }

  private static boolean looselyMatches(VocabularyConcept concept, String value) {
    return ValueTranslation.looselyEquals(concept.getIdentifier(), value)
        || concept.getPreferredTerms().stream().anyMatch(t -> ValueTranslation.looselyEquals(t.getTitle(), value))
        || concept.getAlternativeTerms().stream().anyMatch(t -> ValueTranslation.looselyEquals(t.getTitle(), value));
  }

  /**
   * Deletes the translation for the PropertyTerm.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String delete() {
    if (field != null) {
      field.setTranslation(new TreeMap<>());
      saveResource();
      addActionMessage(getText("manage.translation.deleted", new String[]{field.getTerm().toString()}));
    } else {
      LOG.error("User wanted to delete translation for propertyMapping field, but field was null");
    }

    // capture rowType, needed in redirect
    Extension ext = mapping.getExtension();
    id = (ext != null) ? ext.getRowType() : null;

    // leaves translation page, goes back to mapping page
    return NONE;
  }

  @Override
  public void prepare() {
    super.prepare();
    notFound = true;

    try {
      // get mapping sequence id from parameters as setters are not called yet
      String midStr = StringUtils.trimToNull(req.getParameter(REQ_PARAM_MAPPINGID));
      if (midStr != null) {
        // not a number: -1, which matches no mapping (404)
        mid = NumberUtils.toInt(midStr, -1);
        mapping = resource.getMapping(req.getParameter(REQ_PARAM_ROWTYPE), mid);
      }
    } catch (Exception e) {
      LOG.error("An exception was encountered: {}", e.getMessage(), e);
    }
    if (mapping != null) {
      field = mapping.getField(req.getParameter(REQ_PARAM_TERM));
      if (field != null) {
        notFound = false;
        property = mapping.getExtension().getProperty(field.getTerm());
        if (property.getVocabulary() != null) {
          Map<String, String> vocabTermsRawData =
              vocabManager.getI18nVocab(property.getVocabulary().getUriString(), getLocaleLanguage(), true);
          vocabTerms = new SimpleMapModel(vocabTermsRawData, null);
        }
        // a submitted form carries its own source values
        if (!isHttpPost()) {
          loadSourceValues();
        }
      }
    }
  }

  /**
   * Tells the user the source values were reloaded, which prepare() did.
   *
   * @return SUCCESS regardless of outcome
   */
  public String reload() {
    if (notFound) {
      return NOT_FOUND;
    }
    if (!hasActionErrors()) {
      addActionMessage(getText("manage.translation.reloaded.values",
          new String[]{String.valueOf(translation.getSourceValueCount()), field.getTerm().toString()}));
    }
    // leaves the user on the translation page
    return SUCCESS;
  }

  /**
   * Reads the distinct source values and merges them with the saved translation.
   */
  void loadSourceValues() {
    Set<String> values;
    try {
      values = sourceManager.inspectColumn(mapping.getSource(), field.getIndex(),
          ValueTranslation.MAX_DISTINCT_VALUES + 1, ValueTranslation.MAX_SQL_ROWS);
    } catch (SourceException e) {
      // if an error has occurred, bring it to the user's attention
      addActionError(getText("manage.translation.reloaded.fail",
          new String[]{field.getTerm().toString(), e.getMessage()}));
      values = Collections.emptySet();
    }
    translation.load(values, field.getTranslation());
    if (translation.isTruncated()) {
      addActionWarning(getText("manage.translation.values.truncated",
          new String[]{String.valueOf(ValueTranslation.MAX_DISTINCT_VALUES)}));
    }
  }

  /**
   * Persist the translation for the PropertyTerm by saving the Resource anew, and display to the User this happened.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String save() {
    field.setTranslation(translation.getPersistentMap());
    // save entire resource config
    saveResource();
    id = mapping.getExtension().getRowType();
    addActionMessage(getText("manage.translation.saved", new String[]{field.getTerm().toString()}));
    return NONE;
  }

  public Map<String, String> getSourceValuesMap() {
    return translation.getSourceValues();
  }

  /**
   * On submitting the translation form, sets each row's source value, named "smap".
   */
  @StrutsParameter(depth = 2)
  public Map<String, String> getSmap() {
    return translation.getSourceValues();
  }

  /**
   * On submitting the translation form, sets each row's translated value, named "tmap". Keys correspond to "smap".
   */
  @StrutsParameter(depth = 2)
  public Map<String, String> getTmap() {
    return translation.getTranslatedValues();
  }

  public boolean isNotInSource(String key) {
    return translation.isNotInSource(key);
  }

  public int getVocabTermsSize() {
    return vocabTerms != null ? vocabTerms.size() : 0;
  }

  public Set<String> getVocabTermsKeys() {
    return vocabTerms != null && (vocabTerms.getWrappedObject() instanceof Map) ?
        ((Map) vocabTerms.getWrappedObject()).keySet() : Collections.emptySet();
  }

  public void setField(PropertyMapping field) {
    this.field = field;
  }

  public void setProperty(ExtensionProperty property) {
    this.property = property;
  }

  /**
   * setMapping method name interrupts with Struts2/freemarker page.
   *
   * @param mapping ExtensionMapping
   */
  public void setExtensionMapping(ExtensionMapping mapping) {
    this.mapping = mapping;
  }
}
