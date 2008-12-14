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
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

public class TaxonManagerHibernate extends CoreRecordManagerHibernate<Taxon> implements TaxonManager {
	private TreeNodeSupportHibernate<Taxon> treeNodeSupport;

	public TaxonManagerHibernate() {
		super(Taxon.class);
        treeNodeSupport = new TreeNodeSupportHibernate<Taxon>(Taxon.class);
	}

	
	
	public List<Taxon> getChildren(Long resourceId, Long parentId) {
		return treeNodeSupport.getChildren(resourceId, parentId, getSession());
	}

	public List<Taxon> getDescendants(Long resourceId, Long nodeId) {
		return treeNodeSupport.getDescendants(resourceId, nodeId, getSession());
	}

	public List<Long> getParentIds(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParentIds(resourceId, nodeId, getSession());
	}

	public List<Taxon> getParents(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParents(resourceId, nodeId, getSession());
	}

	public List<Taxon> getRoots(Long resourceId) {
		return treeNodeSupport.getRoots(resourceId, getSession());
	}

	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		return treeNodeSupport.removeAll(resource, getSession());
	}

	public int countTerminalNodes(Long resourceId) {
		return treeNodeSupport.countTerminalNodes(resourceId, getSession());
	}



	public void buildNestedSet(ChecklistResource resource) {
		treeNodeSupport.buildNestedSet(resource, getSession());
	}

	
	public void lookupAcceptedTaxa(Long resourceId) {
		Connection cn = getConnection();
		String sql = String.format("update Taxon t set accepted_fk = (select tp.id from taxon tp where tp.local_id = t.accepted_taxon_id and resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
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
		String sql = String.format("update Taxon t set basionym_fk = (select tp.id from taxon tp where tp.local_id = t.basionym_id and resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
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
		String sql = String.format("update Taxon t set parent_fk = (select tp.id from taxon tp where tp.local_id = t.taxonomic_parent_id and resource_fk = %s) WHERE resource_fk = %s", resourceId, resourceId);
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
		int cnt = ((Long) query("select count(tax) from Taxon tax WHERE tax.accepted is not null and tax.resource.id = :resourceId")
					.setLong("resourceId", resourceId)
					.iterate().next()).intValue();
		resource.setNumSynonyms(cnt);
		return resource;
	}
	public List<Taxon> getAllByRank(Long resourceId, String rank){
		return query("from Taxon WHERE rank = :rank and resource.id = :resourceId order by scientificName")
        	.setLong("resourceId", resourceId)
        	.setString("rank", rank)
        	.list();
	}

}
