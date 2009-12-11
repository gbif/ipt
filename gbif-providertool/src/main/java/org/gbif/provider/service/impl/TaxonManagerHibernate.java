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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.StatsUtils;

import org.hibernate.Query;
import org.hibernate.cfg.NamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonManagerHibernate extends CoreRecordManagerHibernate<Taxon>
    implements TaxonManager {
  private final TreeNodeSupportHibernate<Taxon, Rank> treeNodeSupport;
  @Autowired
  private AnnotationManager annotationManager;
  @Autowired
  private NamingStrategy namingStrategy;

  public TaxonManagerHibernate() {
    super(Taxon.class);
    treeNodeSupport = new TreeNodeSupportHibernate<Taxon, Rank>(Taxon.class,
        this);
  }

  public int annotateAmbigousNames(Long resourceId) {
    List<Taxon> ambigousTaxa = query(
        "select t from Taxon t, Taxon t2 WHERE t.label=t2.label and t.taxonAccordingTo=t2.taxonAccordingTo and t.resource=t2.resource and t.id<>t2.id and t.resource.id = :resourceId").setLong(
        "resourceId", resourceId).list();
    for (Taxon tax : ambigousTaxa) {
      annotationManager.annotate(tax, AnnotationType.AmbigousTaxon,
          String.format("Multiple taxa named '%s' according to '%s' found",
              tax.getScientificName(), tax.getTaxonAccordingTo()));
    }
    return ambigousTaxa.size();
  }

  public void buildNestedSet(Long resourceId) {
    treeNodeSupport.buildNestedSet(resourceId, getSession());
  }

  public int countAccepted(Long resourceId) {
    return countTreeNodes(resourceId);
  }

  public int countByType(Long resourceId, Rank rank) {
    return treeNodeSupport.countByType(resourceId, rank, getSession());
  }

  public int countSynonyms(Long resourceId) {
    return ((Long) query(
        "select count(tax) from Taxon tax WHERE tax.acc is not null and tax.resource.id = :resourceId").setLong(
        "resourceId", resourceId).iterate().next()).intValue();
  }

  public int countTerminalNodes(Long resourceId) {
    return treeNodeSupport.countTerminalNodes(resourceId, getSession(), null);
  }

  public int countTreeNodes(Long resourceId) {
    return treeNodeSupport.countTreeNodes(resourceId, getSession());
  }

  public Taxon getByMaterializedPath(Long resourceId, String mpath) {
    return treeNodeSupport.getByMaterializedPath(resourceId, mpath,
        getSession());
  }

  public List<Taxon> getByRank(Long resourceId, Long taxonId, String rank) {
    Query query;
    if (taxonId == null) {
      query = query(
          "from Taxon WHERE taxonRank = :rank and resource.id = :resourceId order by label").setLong(
          "resourceId", resourceId).setString("rank", rank);
    } else {
      query = query(
          "select t from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt and t.taxonRank = :rank   order by t.label").setLong(
          "taxonId", taxonId).setString("rank", rank);
    }
    return query.list();
  }

  public List<Taxon> getByStatus(Long resourceId, Long taxonId, StatusType st,
      String category) {
    Query query;
    if (taxonId == null) {
      query = query(
          String.format(
              "from Taxon WHERE %s = :category and resource.id = :resourceId order by label",
              st.columnName)).setLong("resourceId", resourceId).setString(
          "category", category);
    } else {
      query = query(
          String.format(
              "select t from Taxon, Taxon root WHERE root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt and t.%s=:category   order by t.label",
              st.columnName)).setLong("taxonId", taxonId).setString("category",
          category);
    }
    return query.list();
  }

  public List<Taxon> getChildren(Long resourceId, Long parentId) {
    return treeNodeSupport.getChildren(resourceId, parentId, getSession());
  }

  public List<Long> getParentIds(Long resourceId, Long nodeId) {
    return treeNodeSupport.getParentIds(resourceId, nodeId, getSession());
  }

  public List<Taxon> getParents(Long resourceId, Long nodeId) {
    return treeNodeSupport.getParents(resourceId, nodeId, getSession());
  }

  public List<StatsCount> getRankStats(Long taxonId) {
    String hql = "";
    List<Object[]> data;
    hql = "select t.taxonRank, count(t)   from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt   group by t.taxonRank, t.type  order by t.type, t.taxonRank";
    data = getSession().createQuery(hql).setLong("taxonId", taxonId).list();
    return StatsUtils.getDataMap(data);
  }

  public List<Taxon> getRoots(Long resourceId) {
    return treeNodeSupport.getRoots(resourceId, getSession(), "n.acc is null");
  }

  public List<Taxon> getSynonyms(Long taxonId) {
    return query(
        "select s from Taxon s, Taxon t  where t.id=:taxonId and s.acc=t  order by s.label").setLong(
        "taxonId", taxonId).list();
  }

  public void lookupAcceptedTaxa(Long resourceId) {
    lookupColumn(resourceId, "acc", "acceptedTaxon");
  }

  public void lookupBasionymTaxa(Long resourceId) {
    lookupColumn(resourceId, "bas", "basionym");
  }

  public void lookupParentTaxa(Long resourceId) {
    lookupColumn(resourceId, "parent", "higherTaxon");
  }

  @Override
  @Transactional(readOnly = false)
  public int removeAll(Resource resource) {
    return treeNodeSupport.removeAll(resource, getSession());
  }

  /**
   * 1. lookup matching columns with scientific_name & accordingTo having
   * count=1 (avoid linking to ambigous names) 2. annotate records with broken
   * taxon pointers
   * 
   * @param resourceId
   * @param fkColumn
   * @param lookupColumn
   * @return
   */
  private int lookupColumn(Long resourceId, String fkColumn, String lookupColumn) {
    String jdbcLookupColumn = namingStrategy.propertyToColumnName(lookupColumn);
    String jdbcFkColumn = fkColumn + "_fk";

    Connection cn = getConnection();
    String sql = String.format(
        "update Taxon t set %s = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.source_id = dwc.%s_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s",
        jdbcFkColumn, jdbcLookupColumn, resourceId, resourceId);
    log.debug(sql);
    int i = 0;
    try {
      Statement st = cn.createStatement();
      i = st.executeUpdate(sql);
      if (i > 0) {
        log.debug(i + " taxa resolved via " + jdbcLookupColumn + " ID.");
      } else {
        // select id from taxon where label in
        // no taxa have been resolved. Try to use the verbose higher taxon name
        // string to match
        sql = String.format(
            "update Taxon t set %s = (select max(tp.id) FROM taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.label = dwc.%s and tp.id!=t.id and tp.resource_fk = %s  GROUP BY tp.label  HAVING count(tp.id)=1 ) WHERE resource_fk = %s",
            jdbcFkColumn, jdbcLookupColumn, resourceId, resourceId);
        log.debug(sql);
        st = cn.createStatement();
        i = st.executeUpdate(sql);
        log.debug(i + " taxa resolved via " + lookupColumn + ".");
      }

      // warn about taxa with non matching pointers
      String hql = String.format(
          "select t from Taxon t, DarwinCore dwc WHERE dwc.sourceId=t.sourceId and dwc.resource=t.resource and (dwc.%s is not null or dwc.%sID is not null) and t.%s is null and t.resource.id = :resourceId",
          lookupColumn, lookupColumn, fkColumn);
      log.debug(hql);
      List<Taxon> corruptTaxa = query(hql).setLong("resourceId", resourceId).list();
      for (Taxon tax : corruptTaxa) {
        annotationManager.annotate(tax, AnnotationType.BadPointer,
            String.format("Taxon:%s '%s' with broken %s pointer",
                tax.getSourceId(), tax.getScientificName(), lookupColumn));
      }
    } catch (SQLException e) {
      log.debug("Resolving taxon " + lookupColumn + " failed.", e);
    }
    return i;
  }

}
