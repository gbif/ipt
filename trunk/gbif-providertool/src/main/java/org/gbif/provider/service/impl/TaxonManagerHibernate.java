package org.gbif.provider.service.impl;

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
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.StatsUtils;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

public class TaxonManagerHibernate extends CoreRecordManagerHibernate<Taxon> implements TaxonManager {
	private TreeNodeSupportHibernate<Taxon> treeNodeSupport;

	public TaxonManagerHibernate() {
		super(Taxon.class);
        treeNodeSupport = new TreeNodeSupportHibernate<Taxon>(Taxon.class, this);
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
		return treeNodeSupport.getRoots(resourceId, getSession(), "accepted=true");
	}

	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		return treeNodeSupport.removeAll(resource, getSession());
	}

	public int countTerminalNodes(Long resourceId) {
		return treeNodeSupport.countTerminalNodes(resourceId, getSession(), "accepted=true");
	}



	public void buildNestedSet(Long resourceId) {
		treeNodeSupport.buildNestedSet(resourceId, getSession());
	}

	
	public void lookupAcceptedTaxa(Long resourceId) {
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set accepted_taxon_fk = (select tp.id from taxon tp where tp.local_id=t.accepted_taxon_id and tp.id!=t.id and resource_fk=%s) WHERE resource_fk = %s", resourceId, resourceId);
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
		String sql = String.format("update Taxon t set basionym_fk = (select tp.id from taxon tp where tp.local_id = t.basionym_id and tp.id!=t.id and resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
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
		String sql = String.format("update Taxon t set parent_fk = (select tp.id from taxon tp where tp.local_id = t.taxonomic_parent_id and tp.id!=t.id and resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
		try {
			Statement st = cn.createStatement();
			int i = st.executeUpdate(sql);
			log.debug(i+" taxa updated with parent taxon.");
		} catch (SQLException e) {
			log.debug("Setting parent taxa failed.", e);
		}
	}
	
	
	private int countByRank(Long resourceId, Rank rank){
		Long cnt = (Long) query("select count(tax) from Taxon tax WHERE tax.dwcRank = :rank and tax.resource.id = :resourceId")
        	.setLong("resourceId", resourceId)
        	.setParameter("rank", rank)
        	.iterate().next();
        return cnt.intValue();
	}
	
	public ChecklistResource setResourceStats(ChecklistResource resource){
		Long resourceId = resource.getId();
		resource.setNumClasses(countByRank(resourceId, Rank.Class));
		resource.setNumFamilies(countByRank(resourceId, Rank.Family));
		resource.setNumGenera(countByRank(resourceId, Rank.Genus));
		resource.setNumKingdoms(countByRank(resourceId, Rank.Kingdom));
		resource.setNumOrders(countByRank(resourceId, Rank.Order));
		resource.setNumPhyla(countByRank(resourceId, Rank.Phylum));
		resource.setNumTaxa(count(resourceId));
		resource.setNumTerminalTaxa(countTerminalNodes(resourceId));
		int cnt = ((Long) query("select count(tax) from Taxon tax WHERE tax.accepted=false and tax.resource.id = :resourceId")
					.setLong("resourceId", resourceId)
					.iterate().next()).intValue();
		resource.setNumSynonyms(cnt);
		return resource;
	}
	
	public List<Taxon> getByRank(Long resourceId, Long taxonId, String rank){
		Query query;
		if (taxonId==null){
			query = query("from Taxon WHERE rank = :rank and resource.id = :resourceId order by scientificName")
		        	.setLong("resourceId", resourceId)
		        	.setString("rank", rank);
		}else{
			query = query("select t from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.accepted=true and t.lft>root.lft and t.rgt<root.rgt and t.rank = :rank   order by t.scientificName")
		        	.setLong("taxonId", taxonId)
		        	.setString("rank", rank);
		}
		return query.list();
	}

	public List<Taxon> getByStatus(Long resourceId, Long taxonId, StatusType st, String category) {
		Query query;
		if (taxonId==null){
			query = query(String.format("from Taxon WHERE %s = :category and resource.id = :resourceId order by scientificName", st.columnName))
		        	.setLong("resourceId", resourceId)
		        	.setString("category", category);
		}else{
			query = query(String.format("select t from Taxon, Taxon root WHERE root.id=:taxonId and t.resource=root.resource and t.accepted=true and t.lft>root.lft and t.rgt<root.rgt and t.%s=:category   order by t.scientificName", st.columnName))
		        	.setLong("taxonId", taxonId)
		        	.setString("category", category);
		}
		return query.list();
	}

	public List<Taxon> getSynonyms(Long taxonId) {
		return query("select s from Taxon s, Taxon t  where t.id=:taxonId and s.acceptedTaxon=t  order by s.scientificName")
    	.setLong("taxonId", taxonId)
    	.list();
	}


	public List<StatsCount> getRankStats(Long taxonId) {
		String hql = "";
		List<Object[]> data;
		hql = "select t.rank, count(t)   from Taxon t, Taxon root   where root.id=:taxonId and t.resource=root.resource and t.accepted=true and t.lft>root.lft and t.rgt<root.rgt   group by t.rank, t.dwcRank  order by t.dwcRank";		
        data = getSession().createQuery(hql)
        	.setLong("taxonId", taxonId)
        	.list();
        return StatsUtils.getDataMap(data);
	}


}
