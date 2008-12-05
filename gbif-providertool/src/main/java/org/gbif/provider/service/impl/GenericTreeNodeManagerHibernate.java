package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.BaseObject;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

public class GenericTreeNodeManagerHibernate<T extends TreeNode<T,?>> extends GenericResourceRelatedManagerHibernate<T> implements TreeNodeManager<T> {
	private TreeNodeSupportHibernate<T> treeNodeSupport;
	
	public GenericTreeNodeManagerHibernate(final Class<T> persistentClass) {
	        super(persistentClass);
	        treeNodeSupport = new TreeNodeSupportHibernate<T>(persistentClass);
    }

	
	
	public List<T> getChildren(Long resourceId, Long parentId) {
		return treeNodeSupport.getChildren(resourceId, parentId, getSession());
	}

	public List<T> getDescendants(Long resourceId, Long nodeId) {
		return treeNodeSupport.getDescendants(resourceId, nodeId, getSession());
	}

	public List<Long> getParentIds(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParentIds(resourceId, nodeId, getSession());
	}

	public List<T> getParents(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParents(resourceId, nodeId, getSession());
	}

	public List<T> getRoots(Long resourceId) {
		return treeNodeSupport.getRoots(resourceId, getSession());
	}

	
	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		return treeNodeSupport.removeAll(resource, getSession());
	}

}
