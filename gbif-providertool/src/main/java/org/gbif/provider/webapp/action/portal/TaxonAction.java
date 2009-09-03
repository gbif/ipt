/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.NamespaceRegistry;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonAction extends BaseTreeNodeAction<Taxon, Rank> implements
    Preparable {
  @Autowired
  private ExtensionRecordManager extensionRecordManager;
  @Autowired
  private AnnotationManager annotationManager;
  private final TaxonManager taxonManager;
  // parameters
  private String action;
  private int type;
  private String category;
  private String format;
  private String q;
  // results
  private String title;
  private List<Taxon> taxa;
  private List<Taxon> synonyms;
  private List<StatsCount> stats;
  private List<Annotation> annotations;
  // xml/json serialisation only
  private Map<Object, Object> json;
  private NamespaceRegistry nsr;
  private ExtendedRecord rec;
  private final List<Extension> extensions = new ArrayList<Extension>();

  public TaxonAction(TaxonManager taxonManager) {
    super(taxonManager);
    this.taxonManager = taxonManager;
  }

  @Override
  public String execute() {
    setRequestedTaxon();
    if (node != null) {
      stats = taxonManager.getRankStats(node.getId());
      synonyms = taxonManager.getSynonyms(node.getId());
      rec = extensionRecordManager.extendCoreRecord(node.getResource(), node);
      if (format != null) {
        if (format.equalsIgnoreCase("xml")) {
          nsr = new NamespaceRegistry(node.getResource());
          return "xml";
        } else if (format.equalsIgnoreCase("rdf")) {
          return "rdf";
        } else if (format.equalsIgnoreCase("json")) {
          // TODO: create map to serialise into JSON
          json = new HashMap<Object, Object>();
          return "json";
        } else {
          return format;
        }
      } else {
        extensions.addAll(rec.getExtensions());
      }
      // find annotations
      annotations = annotationManager.getByRecord(node.getResourceId(),
          node.getGuid());
      return SUCCESS;
    }
    return RECORD404;
  }

  public String getAction() {
    return action;
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public String getCategory() {
    return category;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public String getFormat() {
    return format;
  }

  @Override
  public String getGeoserverMapUrl() {
    return geoserverMapUrl;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public Long getId() {
    return id;
  }

  public Map<Object, Object> getJson() {
    return json;
  }

  public NamespaceRegistry getNsr() {
    return nsr;
  }

  @Override
  public List<DarwinCore> getOccurrences() {
    return occurrences;
  }

  public String getQ() {
    return q;
  }

  public ExtendedRecord getRec() {
    return rec;
  }

  public Taxon getRecord() {
    return node;
  }

  public List<StatsCount> getStats() {
    return stats;
  }

  public List<Taxon> getSynonyms() {
    return synonyms;
  }

  public List<Taxon> getTaxa() {
    return taxa;
  }

  public Taxon getTaxon() {
    return node;
  }

  public Long getTaxonId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public int getWidth() {
    return width;
  }

  public String listByRank() {
    title = category;
    setRequestedTaxon();
    if (node != null) {
      title += " below " + node.getScientificName();
    }
    taxa = taxonManager.getByRank(resourceId, id, category);
    return SUCCESS;
  }

  public String listByStatus() {
    StatusType st = StatusType.getByInt(type);
    title = String.format("%s - %s", st.name(), category);
    setRequestedTaxon();
    if (node != null) {
      title += " below " + node.getScientificName();
    }
    taxa = taxonManager.getByStatus(resourceId, id, st, category);
    return SUCCESS;
  }

  @Override
  public String occurrences() {
    String result = super.occurrences();
    if (node != null) {
      occurrences = darwinCoreManager.getByTaxon(node.getId(), resourceId,
          true);
    }
    return result;
  }

  public String search() {
    super.prepare();
    taxa = taxonManager.search(resourceId, q);
    return SUCCESS;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public void setType(int type) {
    this.type = type;
  }

  private void setRequestedTaxon() {
    if (id != null) {
      node = taxonManager.get(id);
    } else if (guid != null) {
      node = taxonManager.get(guid);
      if (node != null) {
        id = node.getId();
      }
    }
    if (resource == null & node != null) {
      resource = node.getResource();
      updateResourceType();
    }
  }

}