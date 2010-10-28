/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author markus
 * 
 */
public class TranslationAction extends ManagerBaseAction {
  @SessionScoped
  static class Translation {
    private String rowType;
    private ConceptTerm term;
    private TreeMap<String, String> tmap;

    public Map<String, String> getPersistentMap() {
      Map<String, String> m = new HashMap<String, String>();
      for (String key : tmap.keySet()) {
        if (tmap.get(key) != null && !tmap.get(key).equals("") && !tmap.get(key).equals(key)) {
          m.put(key, tmap.get(key));
        }
      }
      return m;
    }

    public TreeMap<String, String> getTmap() {
      return tmap;
    }

    public boolean isLoaded(String rowType, ConceptTerm term) {
      return (this.rowType != null && this.rowType.equals(rowType) && this.term != null && this.term.equals(term) && tmap != null);
    }

    public void setTmap(String rowType, ConceptTerm term, TreeMap<String, String> tmap) {
      this.tmap = tmap;
      this.rowType = rowType;
      this.term = term;
    }

  }

  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private SourceManager sourceManager;
  @Inject
  private VocabulariesManager vocabManager;
  //
  private static final String REQ_PARAM_TERM = "term";
  private static final String REQ_PARAM_ROWTYPE = "rowtype";
  private static final String REQ_PARAM_MAPPINGID = "mid";
  // config
  private PropertyMapping field;
  private ExtensionProperty property;
  private ExtensionMapping mapping;
  private Map<String, String> vocabTerms = new HashMap<String, String>();

  @Inject
  private Translation trans;

  public TranslationAction() {
    super();
    defaultResult = SUCCESS;
  }

  public String automap() {
    // try to lookup vocabulary synonyms and terms
    if (property != null && property.getVocabulary() != null) {
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
      addActionMessage("Mapped " + count + " terms based on vocabulary terms");
    } else {
      addActionError("Cant find a vocabulary to automap translation");
    }
    return SUCCESS;
  }

  @Override
  public String delete() {
    addActionMessage("Couldnt delete translation for term " + field.getTerm());
    return SUCCESS;
  }

  public PropertyMapping getField() {
    return field;
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

  @Override
  public void prepare() throws Exception {
    super.prepare();
    notFound = true;

    try {
      mapping = resource.getMapping(req.getParameter(REQ_PARAM_ROWTYPE),
          Integer.valueOf(req.getParameter(REQ_PARAM_MAPPINGID)));
    } catch (Exception e) {
    }
    if (mapping != null) {
      field = mapping.getField(req.getParameter(REQ_PARAM_TERM));
      if (field != null) {
        notFound = false;
        property = mapping.getExtension().getProperty(field.getTerm());
        if (property.getVocabulary() != null) {
          vocabTerms = vocabManager.getI18nVocab(property.getVocabulary().getUri(), getLocaleLanguage(), true);
        }
        if (!trans.isLoaded(mapping.getExtension().getRowType(), field.getTerm())) {
          reloadSourceValues();
        }
      }
    }
  }

  public String reload() {
    try {
      reloadSourceValues();
    } catch (SourceException e) {
      log.error("Cant inspect source values", e);
      addActionError(e.getMessage());
    }
    return SUCCESS;
  }

  private void reloadSourceValues() throws SourceException {
    trans.setTmap(this.mapping.getExtension().getRowType(), property, new TreeMap<String, String>());
    // reload new values
    for (String val : sourceManager.inspectColumn(mapping.getSource(), field.getIndex(), 1000)) {
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

    addActionMessage("Reloaded " + trans.getTmap().size() + " distinct values from source");
  }

  @Override
  public String save() throws IOException {
    // put map with non empty values back to field
    field.setTranslation(trans.getPersistentMap());
    // save entire resource config
    saveResource();
    return SUCCESS;
  }

  public void setTmap(TreeMap<String, String> tmap) {
    this.trans.tmap = tmap;
  }

}
