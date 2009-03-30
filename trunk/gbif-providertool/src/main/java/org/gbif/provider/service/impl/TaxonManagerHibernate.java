package org.gbif.provider.service.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.StatsUtils;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TaxonManagerHibernate extends CoreRecordManagerHibernate<Taxon> implements TaxonManager {
	private TreeNodeSupportHibernate<Taxon, Rank> treeNodeSupport;
	@Autowired
	private AnnotationManager annotationManager;

	public TaxonManagerHibernate() {
		super(Taxon.class);
        treeNodeSupport = new TreeNodeSupportHibernate<Taxon, Rank>(Taxon.class, this);
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

	public List<Taxon> getRoots(Long resourceId) {
		return treeNodeSupport.getRoots(resourceId, getSession(), "n.acc is null");
	}

	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		return treeNodeSupport.removeAll(resource, getSession());
	}

	public int countTerminalNodes(Long resourceId) {
		return treeNodeSupport.countTerminalNodes(resourceId, getSession(), null);
	}



	public void buildNestedSet(Long resourceId) {
		treeNodeSupport.buildNestedSet(resourceId, getSession());
	}

	public Taxon getByMaterializedPath(Long resourceId, String mpath) {
		return treeNodeSupport.getByMaterializedPath(resourceId, mpath, getSession());
	}

	
	public void lookupAcceptedTaxa(Long resourceId) {
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set acc_fk = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk   where tp.source_id=dwc.accepted_taxon_id and tp.id!=t.id and tp.resource_fk=%s) WHERE resource_fk = %s", resourceId, resourceId);
		try {
			Statement st = cn.createStatement();
			int i = st.executeUpdate(sql);
			log.debug(i+" taxa updated with accepted taxon.");
		} catch (SQLException e) {
			log.debug("Setting accepted taxa failed.", e);
		}
	}

	public void lookupBasionymTaxa(Long resourceId) {
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set bas_fk = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk   where tp.source_id = dwc.basionym_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
		try {
			Statement st = cn.createStatement();
			int i = st.executeUpdate(sql);
			log.debug(i+" taxa updated with basionym taxon.");
		} catch (SQLException e) {
			log.debug("Setting basionym taxa failed.", e);
		}
	}

	public void lookupParentTaxa(Long resourceId) {
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set parent_fk = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.source_id = dwc.taxonomic_parent_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
		try {
			Statement st = cn.createStatement();
			int i = st.executeUpdate(sql);
			if (i>0){
				log.debug(i+" taxa resolved via higherTaxonID.");
			}else{
				// no taxa have been resolved. Try to use the verbose higher taxon name string to match
				sql = String.format("update Taxon t set parent_fk = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.source_id = dwc.taxonomic_parent_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
				st = cn.createStatement();
				i = st.executeUpdate(sql);
				log.debug(i+" taxa resolved via higherTaxon.");
			}
			//TODO: warn about taxa with non matching higherTaxon or higherTaxonID
		} catch (SQLException e) {
			log.debug("Setting parent taxa failed.", e);
		}
	}
	
	/**
	 * 1. lookup matching columns with scientific_name & accordingTo having count=1 (avoid linking to ambigous names)
	 * 2. annotate records with broken taxon pointers
	 * @param resourceId
	 * @param fkColumn
	 * @param lookupColumn
	 * @param useSourceId
	 * @return
	 */
	private int lookupColumn(Long resourceId, String fkColumn, String lookupColumn, boolean useSourceId){
		String dwcColumn = "scientific_name";
		String taxColumn = "label";
		if (useSourceId){
			dwcColumn="source_id";
			taxColumn = "source_id";
		}
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set %s = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.source_id = dwc.taxonomic_parent_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s", fkColumn, resourceId, resourceId);
		int i = 0;
		try {
			Statement st = cn.createStatement();
			i = st.executeUpdate(sql);
			if (i>0){
				log.debug(i+" taxa resolved via higherTaxonID.");
			}else{
				// no taxa have been resolved. Try to use the verbose higher taxon name string to match
				sql = String.format("update Taxon t set parent_fk = (select tp.id   from taxon tp join darwin_core dwc on dwc.source_id=t.source_id and dwc.resource_fk=t.resource_fk    where tp.source_id = dwc.taxonomic_parent_id and tp.id!=t.id and tp.resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
				st = cn.createStatement();
				i = st.executeUpdate(sql);
				log.debug(i+" taxa resolved via higherTaxon.");
			}
			//TODO: warn about taxa with non matching higherTaxon or higherTaxonID
		} catch (SQLException e) {
			log.debug("Setting parent taxa failed.", e);
		}
		return i;		
	}
	
	public int annotateAmbigousNames(Long resourceId){
		List<Taxon> ambigousTaxa = query("select t from Taxon t, Taxon t2 WHERE t.scientificName=t2.scientificName and t.taxonAccordingTo=t2.taxonAccordingTo and t.resource=t2.resource and t.id<>t2.id and t.resource.id = :resourceId")
				.setLong("resourceId", resourceId)
				.list();
		for (Taxon tax : ambigousTaxa){
			annotationManager.annotate(tax, AnnotationType.AmbigousTaxon, String.format("Multiple taxa named '%s' according to '%s' found",tax.getScientificName(), tax.getTaxonAccordingTo()));
		}
		return ambigousTaxa.size();
	}
	public int countTreeNodes(Long resourceId) {
		return treeNodeSupport.countTreeNodes(resourceId, getSession());
	}

	public int countByType(Long resourceId, Rank rank){
		return treeNodeSupport.countByType(resourceId, rank, getSession());
	}
	public int countSynonyms(Long resourceId) {
		return ((Long) query("select count(tax) from Taxon tax WHERE tax.acc is not null and tax.resource.id = :resourceId")
				.setLong("resourceId", resourceId)
				.iterate().next()).intValue();
	}
	public int countAccepted(Long resourceId) {
		return countTreeNodes(resourceId);
	}

	
	public List<Taxon> getByRank(Long resourceId, Long taxonId, String rank){
		Query query;
		if (taxonId==null){
			query = query("from Taxon WHERE rank = :rank and resource.id = :resourceId order by label")
		        	.setLong("resourceId", resourceId)
		        	.setString("rank", rank);
		}else{
			query = query("select t from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt and t.rank = :rank   order by t.label")
		        	.setLong("taxonId", taxonId)
		        	.setString("rank", rank);
		}
		return query.list();
	}

	public List<Taxon> getByStatus(Long resourceId, Long taxonId, StatusType st, String category) {
		Query query;
		if (taxonId==null){
			query = query(String.format("from Taxon WHERE %s = :category and resource.id = :resourceId order by label", st.columnName))
		        	.setLong("resourceId", resourceId)
		        	.setString("category", category);
		}else{
			query = query(String.format("select t from Taxon, Taxon root WHERE root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt and t.%s=:category   order by t.label", st.columnName))
		        	.setLong("taxonId", taxonId)
		        	.setString("category", category);
		}
		return query.list();
	}

	public List<Taxon> getSynonyms(Long taxonId) {
		return query("select s from Taxon s, Taxon t  where t.id=:taxonId and s.acc=t  order by s.label")
    	.setLong("taxonId", taxonId)
    	.list();
	}


	public List<StatsCount> getRankStats(Long taxonId) {
		String hql = "";
		List<Object[]> data;
		hql = "select t.rank, count(t)   from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.lft>root.lft and t.rgt<root.rgt   group by t.rank, t.type  order by t.type, t.rank";		
        data = getSession().createQuery(hql)
        	.setLong("taxonId", taxonId)
        	.list();
        return StatsUtils.getDataMap(data);
	}
	
	
}
