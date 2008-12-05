package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
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
}
