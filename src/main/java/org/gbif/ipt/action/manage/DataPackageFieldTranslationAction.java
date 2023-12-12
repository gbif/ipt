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
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataSchemaFieldMapping;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import freemarker.ext.beans.SimpleMapModel;

public class DataPackageFieldTranslationAction extends ManagerBaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(TranslationAction.class);

  @SessionScoped
  static class Translation {

    private String tableSchema;

    private String field;

    private TreeMap<String, String> sourceValues;

    private TreeMap<String, String> translatedValues;

    public Map<String, String> getPersistentMap() {
      Map<String, String> m = new HashMap<>();
      for (Entry<String, String> translatedValueEntry: translatedValues.entrySet()) {
        m.put(sourceValues.get(translatedValueEntry.getKey()), StringUtils.trimToEmpty(translatedValueEntry.getValue()));
      }
      return m;
    }

    public TreeMap<String, String> getSourceValues() {
      return sourceValues;
    }

    public TreeMap<String, String> getTranslatedValues() {
      return translatedValues;
    }

    public boolean isLoaded(String tableSchema, DataPackageField field) {
      return this.tableSchema != null
        && this.tableSchema.equals(tableSchema)
        && this.field != null
        && this.field.equals(field.getName())
        && sourceValues != null;
    }

    public void setTmap(String tableSchema, String field, TreeMap<String, String> sourceValues,
                        TreeMap<String, String> translatedValues) {
      this.sourceValues = sourceValues;
      this.translatedValues = translatedValues;
      this.tableSchema = tableSchema;
      this.field = field;
    }
  }

  private SourceManager sourceManager;
  private Translation trans;

  protected static final String REQ_PARAM_MAPPINGID = "mid";
  protected static final String REQ_FIELD = "field";
  // config
  private DataSchemaFieldMapping fieldMapping;
  private DataPackageField field;
  private DataSchemaMapping mapping;

  private SimpleMapModel vocabTerms;
  private Integer mid;

  @Inject
  public DataPackageFieldTranslationAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                           ResourceManager resourceManager, SourceManager sourceManager, Translation trans) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
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
    if (field == null || field.getConstraints().getVocabulary() == null) {
      addActionError(getText("manage.translation.cantfind.vocabulary"));
    } else {
      List<String> vocab = field.getConstraints().getVocabulary();
      int count = 0;
      for (Entry<String, String> sourceValueEntry: getSourceValuesMap().entrySet()) {
        // only if not yet mapped
        if (!getTmap().containsValue(sourceValueEntry.getValue())) {
          Optional<String> vocabularyMatch = vocab.stream()
            .filter(v -> v.replaceAll("_", "")
              .equalsIgnoreCase(sourceValueEntry.getValue().replaceAll("", "_")))
            .findFirst();

          if (vocabularyMatch.isPresent()) {
            getTmap().put(sourceValueEntry.getKey(), vocabularyMatch.get());
            count++;
          }
        }
      }
      addActionMessage(getText("manage.translation.mapped.terms", new String[] {String.valueOf(count)}));
    }
    return SUCCESS;
  }

  /**
   * Deletes the translation for the field. The sessionScoped translation must also be deleted, otherwise it
   * will reappear on the next page visit. The source values get reloaded, so they are populated on next page visit.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String delete() {
    // proceed with deletion
    if (fieldMapping != null) {
      // 1. ensure the translation map on the FieldMapping is empty
      fieldMapping.setTranslation(new TreeMap<>());
      // 2. ensure the static sessionScoped translation for this field is empty
      trans.setTmap(this.mapping.getDataSchemaFile().getName(), field.getName(), new TreeMap<>(), new TreeMap<>());
      // 3. save the resource
      saveResource();
      // 4. add msg to appear in UI indicating the translation for this PropertyMapping has been deleted
      addActionMessage(getText("manage.translation.deleted", new String[] {field.getName()}));
      // 5. reload source values, so they aren't empty on next page visit
      reloadSourceValues();
    } else {
      LOG.error("User wanted to deleted translation for propertyMapping field, but field was null");
    }

    DataPackageSchema schema = mapping.getDataPackageSchema();
    id = (schema != null) ? schema.getIdentifier() : null;

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
        mapping = resource.getDataSchemaMapping(mid);
      }
    } catch (Exception e) {
      LOG.error("An exception was encountered: " + e.getMessage(), e);
    }

    if (mapping != null) {
      String fieldParam = req.getParameter(REQ_FIELD);
      fieldMapping = mapping.getField(fieldParam);

      if (fieldMapping != null) {
        notFound = false;
        field = mapping.getField(fieldParam).getField();

        if (field.getConstraints() != null && field.getConstraints().getVocabulary() != null) {
          Map<String, String> vocabRawData = field.getConstraints().getVocabulary().stream()
            .collect(Collectors.toMap(Function.identity(), Function.identity()));
          vocabTerms = new SimpleMapModel(vocabRawData, null);
        }

        if (!trans.isLoaded(mapping.getDataSchemaFile().getName(), fieldMapping.getField())) {
          reloadSourceValues();
        }

        // empty translation before POST
        if (isHttpPost()) {
          trans.getTranslatedValues().clear();
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
        mapping = resource.getDataSchemaMapping(mid);
      }

      // reinitialize translation, including maps
      trans.setTmap(mapping.getDataSchemaFile().getName(), field.getName(), new TreeMap<>(), new TreeMap<>());

      // reload new values
      int i = 1;
      for (String val : sourceManager.inspectColumn(mapping.getSource(), fieldMapping.getIndex(), 1000, 10000)) {
        String key = "k" + i;
        getSourceValuesMap().put(key, val);
        i++;
      }

      // keep existing translations
      if (fieldMapping.getTranslation() != null) {
        for (Entry<String, String> entry : fieldMapping.getTranslation().entrySet()) {
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
      if (!isHttpPost()) {
        addActionMessage(getText("manage.translation.reloaded.values",
          new String[]{String.valueOf(getSourceValuesMap().size()), fieldMapping.getField().getName()}));
      }

    } catch (SourceException e) {
      // if an error has occurred, bring it to the user's attention
      addActionError(getText("manage.translation.reloaded.fail",
        new String[] {fieldMapping.getField().getName(), e.getMessage()}));
    }
  }

  /**
   * Persist the translation for the PropertyTerm by saving the Resource anew, and display to the User this happened.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String save() {
    // put map with non-empty values back to field
    fieldMapping.setTranslation(trans.getPersistentMap());
    // save entire resource config
    saveResource();
    id = mapping.getDataSchemaFile().getName();
    addActionMessage(getText("manage.translation.saved", new String[] {fieldMapping.getField().getName()}));

    return NONE;
  }

  public Integer getMid() {
    return mid;
  }

  public Map<String, String> getSourceValuesMap() {
    return trans.getSourceValues();
  }

  public Map<String, String> getTmap() {
    return trans.getTranslatedValues();
  }

  public SimpleMapModel getVocabTerms() {
    return vocabTerms;
  }

  public int getVocabTermsSize() {
    return vocabTerms != null ? vocabTerms.size() : 0;
  }

  public Set<String> getVocabTermsKeys() {
    return vocabTerms != null && (vocabTerms.getWrappedObject() instanceof Map) ?
      ((Map) vocabTerms.getWrappedObject()).keySet() : Collections.emptySet();
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

  public DataSchemaMapping getMapping() {
    return mapping;
  }

  public DataSchemaFieldMapping getFieldMapping() {
    return fieldMapping;
  }

  public DataPackageField getField() {
    return field;
  }
}
