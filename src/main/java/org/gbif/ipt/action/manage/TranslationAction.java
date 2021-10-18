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
package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.Term;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

public class TranslationAction extends ManagerBaseAction {

  private static final long serialVersionUID = -8350422710092468050L;

  // logging
  private static final Logger LOG = LogManager.getLogger(TranslationAction.class);

  @SessionScoped
  static class Translation {

    private String rowType;
    private Term term;
    private TreeMap<String, String> sourceValues;
    private TreeMap<String, String> translatedValues;

    /**
     * Return a map populated with all source value to translated value pairs.
     */
    public Map<String, String> getPersistentMap() {
      Map<String, String> m = new HashMap<>();
      for (Entry<String, String> translatedValueEntry: translatedValues.entrySet()) {
        if (StringUtils.isNotBlank(translatedValueEntry.getValue())) {
         m.put(sourceValues.get(translatedValueEntry.getKey()), translatedValueEntry.getValue().trim());
        }
      }
      return m;
    }

    /**
     * @return map with original source values, e.g. {"k1", "Obs"}.
     */
    public Map<String, String> getSourceValues() {
      return sourceValues;
    }

    /**
     * @return map with translated values, e.g. {"k1", "Observation"}. Entries relate to entries in sourceValues by
     * via their key.
     */
    public Map<String, String> getTranslatedValues() {
      return translatedValues;
    }

    /**
     * Check whether the translation has been loaded already. Call to prevent reloading original source values each
     * time translation page gets loaded, for example.
     *
     * @param rowType to which Term belongs
     * @param term Term
     * @return true if the translation has been loaded already, false otherwise
     */
    public boolean isLoaded(String rowType, Term term) {
      return this.rowType != null && this.rowType.equals(rowType) && this.term != null && this.term.equals(term)
        && sourceValues != null;
    }

    public void setTmap(String rowType, Term term, TreeMap<String, String> sourceValues,
      TreeMap<String, String> translatedValues) {
      this.sourceValues = sourceValues;
      this.translatedValues = translatedValues;
      this.rowType = rowType;
      this.term = term;
    }
  }

  private SourceManager sourceManager;
  private VocabulariesManager vocabManager;
  private Translation trans;

  protected static final String REQ_PARAM_TERM = "term";
  protected static final String REQ_PARAM_ROWTYPE = "rowtype";
  protected static final String REQ_PARAM_MAPPINGID = "mid";
  // config
  private PropertyMapping field;
  private ExtensionProperty property;
  private ExtensionMapping mapping;
  private Map<String, String> vocabTerms = new HashMap<>();
  private Integer mid;
  private String id;

  @Inject
  public TranslationAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager, SourceManager sourceManager, VocabulariesManager vocabManager, Translation trans) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
    this.vocabManager = vocabManager;
    this.trans = trans;
    defaultResult = SUCCESS;
  }

  /**
   * Automatically map source values to the terms in a vocabulary. A match occurs when the source value matches
   * against one of the vocabulary's term's name, preferred name, or alternate name.
   *
   * @return SUCCESS result, staying on translation page
   */
  public String automap() {
    if (property == null || property.getVocabulary() == null) {
      addActionError(getText("manage.translation.cantfind.vocabulary"));
    } else {
      Vocabulary vocab = property.getVocabulary();
      int count = 0;
      for (Entry<String, String> sourceValueEntry: getSourceValuesMap().entrySet()) {
        // only if not yet mapped
        if (!getTmap().containsValue(sourceValueEntry.getValue())) {
          VocabularyConcept vc = vocab.findConcept(sourceValueEntry.getValue());
          if (vc != null) {
            getTmap().put(sourceValueEntry.getKey(), vc.getIdentifier());
            count++;
          }
        }
      }
      addActionMessage(getText("manage.translation.mapped.terms", new String[] {String.valueOf(count)}));
    }
    return SUCCESS;
  }

  /**
   * Deletes the translation for the PropertyTerm. The sessionScoped translation must also be deleted, otherwise it
   * will reappear on the next page visit. The source values get reloaded so they are populated on next page visit.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String delete() {
    // proceed with deletion
    if (field != null) {
      // 1. ensure the translation map on the PropertyMapping (field) is empty
      field.setTranslation(new TreeMap<>());
      // 2. ensure the static sessionScoped translation for this rowType and ConceptTerm is empty
      trans.setTmap(this.mapping.getExtension().getRowType(), property, new TreeMap<>(), new TreeMap<>());
      // 3. save the resource
      saveResource();
      // 4. add msg to appear in UI indicating the translation for this PropertyMapping has been deleted
      addActionMessage(getText("manage.translation.deleted", new String[] {field.getTerm().toString()}));
      // 5. reload source values, so they aren't empty on next page visit
      reloadSourceValues();
    } else {
      LOG.error("User wanted to deleted translation for propertyMapping field, but field was null");
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
        mid = Integer.valueOf(midStr);
        mapping = resource.getMapping(req.getParameter(REQ_PARAM_ROWTYPE), mid);
      }
    } catch (Exception e) {
      LOG.error("An exception was encountered: " + e.getMessage(), e);
    }
    if (mapping != null) {
      field = mapping.getField(req.getParameter(REQ_PARAM_TERM));
      if (field != null) {
        notFound = false;
        property = mapping.getExtension().getProperty(field.getTerm());
        if (property.getVocabulary() != null) {
          vocabTerms = vocabManager.getI18nVocab(property.getVocabulary().getUriString(), getLocaleLanguage(), true);
        }
        if (!trans.isLoaded(mapping.getExtension().getRowType(), field.getTerm())) {
          reloadSourceValues();
        }
      }
    }
  }

  /**
   * Reload the source values, and display to the User this has happened.
   *
   * @return SUCCESS regardless of outcome
   */
  public String reload() {
    reloadSourceValues();
    // leaves the user on the translation page
    return SUCCESS;
  }

  /**
   * Clears the existing translation, reloads the source values, and repopulates existing translations.
   * The key of each entry in the translation.sourceValues map, e.g. {{"k1", "obs"}, {"k2", "spe"}} corresponds to each
   * entry in the translation.translatedValues map, e.g. {{"k1", "Observation"}, {"k2", "Specimen"}}.
   */
  void reloadSourceValues() {
    try {
      String midStr = StringUtils.trimToNull(req.getParameter(REQ_PARAM_MAPPINGID));
      if (midStr != null) {
        mid = Integer.valueOf(midStr);
        mapping = resource.getMapping(req.getParameter(REQ_PARAM_ROWTYPE), mid);
      }
      // reinitialize translation, including maps
      trans.setTmap(this.mapping.getExtension().getRowType(), property, new TreeMap<>(), new TreeMap<>());
      // reload new values
      int i = 1;
      for (String val : sourceManager.inspectColumn(mapping.getSource(), field.getIndex(), 1000, 10000)) {
        StringBuilder key = new StringBuilder();
        key.append('k');
        key.append(i);
        getSourceValuesMap().put(key.toString(), val);
        i++;
      }
      // keep existing translations
      if (field.getTranslation() != null) {
        for (Entry<String, String> entry : field.getTranslation().entrySet()) {
          // only keep entries with values mapped that exist in the newly reloaded map
          if (entry.getValue() != null && getSourceValuesMap().containsValue(entry.getKey())) {
            for (Entry<String, String> sourceValueEntry: getSourceValuesMap().entrySet()) {
              if (sourceValueEntry.getValue().equals(entry.getKey())) {
                getTmap().put(sourceValueEntry.getKey(), entry.getValue());
              }
            }
          }
        }
      }
      // bring it to user's attention, that the source values have been reloaded
      addActionMessage(getText("manage.translation.reloaded.values",
        new String[] {String.valueOf(getSourceValuesMap().size()), field.getTerm().toString()}));

    } catch (SourceException e) {
      // if an error has occurred, bring it to the user's attention
      addActionError(getText("manage.translation.reloaded.fail",
        new String[] {field.getTerm().toString(), e.getMessage()}));
    }
  }

  /**
   * Persist the translation for the PropertyTerm by saving the Resource anew, and display to the User this happened.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String save() {
    // put map with non empty values back to field
    field.setTranslation(trans.getPersistentMap());
    // save entire resource config
    saveResource();
    id = mapping.getExtension().getRowType();
    addActionMessage(getText("manage.translation.saved", new String[] {field.getTerm().toString()}));
    return NONE;
  }

  public PropertyMapping getField() {
    return field;
  }

  @Override
  public String getId() {
    return id;
  }

  public Integer getMid() {
    return mid;
  }

  public ExtensionProperty getProperty() {
    return property;
  }

  public Map<String, String> getSourceValuesMap() {
    return trans.getSourceValues();
  }

  public Map<String, String> getTmap() {
    return trans.getTranslatedValues();
  }

  public Map<String, String> getVocabTerms() {
    return vocabTerms;
  }

  /**
   * On submitting the translation form sets the translated values map, named "tmap".
   *
   * @param translatedValues map with translated values, whose key corresponds to translation.sourceValues map
   */
  public void setTmap(TreeMap<String, String> translatedValues) {
    this.trans.translatedValues = translatedValues;
  }

  public Translation getTrans() {
    return trans;
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
