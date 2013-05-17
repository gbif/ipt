/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.ConceptTerm;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.apache.commons.lang.xwork.StringUtils;
import org.apache.log4j.Logger;

public class TranslationAction extends ManagerBaseAction {

  // logging
  private static final Logger log = Logger.getLogger(TranslationAction.class);

  @SessionScoped
  static class Translation {

    private String rowType;
    private ConceptTerm term;
    private TreeMap<String, String> tmap;

    public Map<String, String> getPersistentMap() {
      Map<String, String> m = new HashMap<String, String>();
      for (String key : tmap.keySet()) {
        if (tmap.get(key) != null && !(tmap.get(key).length() == 0) && !tmap.get(key).equals(key)) {
          m.put(key, tmap.get(key));
        }
      }
      return m;
    }

    public TreeMap<String, String> getTmap() {
      return tmap;
    }

    public boolean isLoaded(String rowType, ConceptTerm term) {
      return this.rowType != null && this.rowType.equals(rowType) && this.term != null && this.term.equals(term)
             && tmap != null;
    }

    public void setTmap(String rowType, ConceptTerm term, TreeMap<String, String> tmap) {
      this.tmap = tmap;
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
  private Map<String, String> vocabTerms = new HashMap<String, String>();
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

  public String automap() {
    // try to lookup vocabulary synonyms and terms
    if (property == null || property.getVocabulary() == null) {
      addActionError(getText("manage.translation.cantfind.vocabulary"));
    } else {
      Vocabulary vocab = property.getVocabulary();
      int count = 0;
      for (String src : trans.getTmap().keySet()) {
        // only if not yet mapped
        if (trans.getTmap().get(src) == null) {
          VocabularyConcept vc = vocab.findConcept(src);
          if (vc != null) {
            trans.getTmap().put(src, vc.getIdentifier());
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
   * will reappear on the next page visit.
   *
   * @return NONE result, going back to mapping page
   */
  @Override
  public String delete() {
    // proceed with deletion
    if (field != null) {
      // 1. ensure the translation map on the PropertyMapping (field) is empty
      field.setTranslation(new TreeMap<String, String>());
      // 2. ensure the static sessionScoped translation for this rowType and ConceptTerm is empty
      trans.setTmap(this.mapping.getExtension().getRowType(), property, new TreeMap<String, String>());
      // 3. save the resource
      saveResource();
      // 4. add msg to appear in UI indicating the translation for this PropertyMapping has been deleted
      addActionMessage(getText("manage.translation.deleted", new String[] {field.getTerm().toString()}));
      // 5. reload source values, so they aren't empty on next page visit
      reloadSourceValues();
    } else {
      log.error("User wanted to deleted translation for propertyMapping field, but field was null");
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
      log.error("An exception was encountered: " + e.getMessage(), e);
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

  void reloadSourceValues() {
    try {
      String midStr = StringUtils.trimToNull(req.getParameter(REQ_PARAM_MAPPINGID));
      if (midStr != null) {
        mid = Integer.valueOf(midStr);
        mapping = resource.getMapping(req.getParameter(REQ_PARAM_ROWTYPE), mid);
      }
      trans.setTmap(this.mapping.getExtension().getRowType(), property, new TreeMap<String, String>());
      // reload new values
      for (String val : sourceManager.inspectColumn(mapping.getSource(), field.getIndex(), 1000, 10000)) {
        trans.getTmap().put(val, null);
      }
      // keep existing translations
      if (field.getTranslation() != null) {
        for (Entry<String, String> entry : field.getTranslation().entrySet()) {
          // only keep entries with values mapped that exist in the newly reloaded map
          if (entry.getValue() != null && trans.getTmap().containsKey(entry.getKey())) {
            trans.getTmap().put(entry.getKey(), entry.getValue());
          }
        }
      }
      // bring it to user's attention, that the source values have been reloaded
      addActionMessage(getText("manage.translation.reloaded.values",
        new String[] {String.valueOf(trans.getTmap().size()), field.getTerm().toString()}));

    } catch (SourceException e) {
      // if an error has occurred, bring it to the user's attention
      addActionError(
        getText("manage.translation.reloaded.fail", new String[] {field.getTerm().toString(), e.getMessage()}));
    }
  }

  @Override
  public String save() throws IOException {
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

  public Map<String, String> getTmap() {
    return trans.getTmap();
  }

  public Map<String, String> getVocabTerms() {
    return vocabTerms;
  }

  public void setTmap(TreeMap<String, String> tmap) {
    this.trans.tmap = tmap;
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
