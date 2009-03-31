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

public class GenericTreeNodeManagerHibernate<T extends TreeNode<T,E>, E extends Enum> extends GenericResourceRelatedManagerHibernate<T> implements TreeNodeManager<T,E> {
	private TreeNodeSupportHibernate<T,E> treeNodeSupport;
	
	public GenericTreeNodeManagerHibernate(final Class<T> persistentClass) {
	        super(persistentClass);
	        treeNodeSupport = new TreeNodeSupportHibernate<T,E>(persistentClass, this);
    }

	
	
	public List<T> getChildren(Long resourceId, Long parentId) {
		return treeNodeSupport.getChildren(resourceId, parentId, getSession());
	}

	public List<Long> getParentIds(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParentIds(resourceId, nodeId, getSession());
	}

	public List<T> getParents(Long resourceId, Long nodeId) {
		return treeNodeSupport.getParents(resourceId, nodeId, getSession());
	}

	public List<T> getRoots(Long resourceId) {
		return treeNodeSupport.getRoots(resourceId, getSession(), null);
	}

	
	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		return treeNodeSupport.removeAll(resource, getSession());
	}

	public int countTerminalNodes(Long resourceId) {
		return treeNodeSupport.countTerminalNodes(resourceId, getSession(), null);
	}

	public int countByType(Long resourceId, E type) {
		return treeNodeSupport.countByType(resourceId, type, getSession());
	}

	public int countTreeNodes(Long resourceId) {
		return treeNodeSupport.countTreeNodes(resourceId, getSession());
	}

	
	@Transactional(readOnly=false)
	public void buildNestedSet(Long resourceId) {
		treeNodeSupport.buildNestedSet(resourceId, getSession());
	}

	public T getByMaterializedPath(Long resourceId, String mpath) {
		return treeNodeSupport.getByMaterializedPath(resourceId, mpath, getSession());
	}

}
