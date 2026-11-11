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
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import freemarker.ext.beans.SimpleMapModel;
import lombok.Getter;

public class DataPackageFieldTranslationAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -5414310011336523439L;

  private static final Logger LOG = LogManager.getLogger(DataPackageFieldTranslationAction.class);

  private final SourceManager sourceManager;
  @Getter
  private final ValueTranslation translation = new ValueTranslation();

  protected static final String REQ_PARAM_MAPPINGID = "mid";
  protected static final String REQ_FIELD = "field";
  // config
  @Getter
  private DataPackageFieldMapping fieldMapping;
  @Getter
  private DataPackageField field;
  @Getter
  private DataPackageMapping mapping;

  @Getter
  private SimpleMapModel vocabTerms;
  @Getter
  private Integer mid;

  @Inject
  public DataPackageFieldTranslationAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      SourceManager sourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.sourceManager = sourceManager;
    defaultResult = SUCCESS;
  }

  /**
   * Automatically map source values not translated yet to the values in the field's vocabulary. A match occurs when
   * the source value matches a vocabulary value ignoring case, whitespace, underscores and hyphens.
   *
   * @return SUCCESS result, staying on translation page
   */
  public String automap() {
    if (notFound) {
      return NOT_FOUND;
    }
    if (field == null || field.getConstraints() == null || field.getConstraints().getVocabulary() == null) {
      addActionError(getText("manage.translation.cantfind.vocabulary"));
    } else {
      List<String> vocab = field.getConstraints().getVocabulary();
      int count = translation.automap(value -> vocab.stream()
          .filter(v -> ValueTranslation.looselyEquals(v, value))
          .findFirst()
          .orElse(null));
      addActionMessage(getText("manage.translation.mapped.fields", new String[]{String.valueOf(count)}));
    }
    return SUCCESS;
  }

  /**
   * Deletes the translation for the field.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String delete() {
    if (fieldMapping != null) {
      fieldMapping.setTranslation(new TreeMap<>());
      saveResource();
      addActionMessage(getText("manage.translation.field.deleted", new String[]{field.getName()}));
    } else {
      LOG.error("User wanted to delete translation for propertyMapping field, but field was null");
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
        // not a number: -1, which matches no mapping (404)
        mid = NumberUtils.toInt(midStr, -1);
        mapping = resource.getDataPackageMapping(mid);
      }
    } catch (Exception e) {
      LOG.error("An exception was encountered: {}", e.getMessage(), e);
    }

    if (mapping != null) {
      String fieldParam = req.getParameter(REQ_FIELD);
      fieldMapping = mapping.getField(fieldParam);

      if (fieldMapping != null) {
        notFound = false;
        field = fieldMapping.getField();

        if (field.getConstraints() != null && field.getConstraints().getVocabulary() != null) {
          Map<String, String> vocabRawData = field.getConstraints().getVocabulary().stream()
              .collect(Collectors.toMap(Function.identity(), Function.identity(), (e1, e2) -> e1, LinkedHashMap::new));
          vocabTerms = new SimpleMapModel(vocabRawData, null);
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
      addActionMessage(getText("manage.translation.reloaded.field.values",
          new String[]{String.valueOf(translation.getSourceValueCount()), field.getName()}));
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
      values = sourceManager.inspectColumn(mapping.getSource(), fieldMapping.getIndex(),
          ValueTranslation.MAX_DISTINCT_VALUES + 1, ValueTranslation.MAX_SQL_ROWS);
    } catch (SourceException e) {
      // if an error has occurred, bring it to the user's attention
      addActionError(getText("manage.translation.reloaded.field.fail",
          new String[]{field.getName(), e.getMessage()}));
      values = Collections.emptySet();
    }
    translation.load(values, fieldMapping.getTranslation());
    if (translation.isTruncated()) {
      addActionWarning(getText("manage.translation.values.truncated",
          new String[]{String.valueOf(ValueTranslation.MAX_DISTINCT_VALUES)}));
    }
  }

  /**
   * Persist the translation for the field by saving the Resource anew, and display to the User this happened.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String save() {
    fieldMapping.setTranslation(translation.getPersistentMap());
    // save entire resource config
    saveResource();
    id = mapping.getDataPackageTableSchemaName().getName();
    addActionMessage(getText("manage.translation.field.saved", new String[]{field.getName()}));

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
}
