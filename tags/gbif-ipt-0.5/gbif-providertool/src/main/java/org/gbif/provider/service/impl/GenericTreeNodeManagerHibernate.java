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

	public GenericTreeNodeManagerHibernate(final Class<T> persistentClass) {
	        super(persistentClass);
    }

	public List<T> getChildren(Long resourceId, Long parentId) {
        return getSession().createQuery(String.format("from %s node where node.parent.id = :parentId and node.resource.id = :resourceId order by lft", persistentClass.getName()))
        .setLong("resourceId", resourceId)
        .setLong("parentId", parentId)
		.list();
	}

	public List<T> getDescendants(Long resourceId, Long nodeId) {
		List<T> result = new ArrayList<T>(); 
		return result;
	}

	public List<Long> getParentIds(Long resourceId, Long nodeId) {
        return getSession().createQuery(String.format("SELECT parent.id FROM %s node, %s parent WHERE node.id = :nodeId and node.lft between parent.lft and parent.rgt   ORDER BY parent.lft", persistentClass.getName(), persistentClass.getName()))
        .setLong("nodeId", nodeId)
		.list();
	}	

	public List<T> getParents(Long resourceId, Long nodeId) {
		List<T> result = new ArrayList<T>(); 
		return result;
	}

	public List<T> getRoots(Long resourceId) {
        return getSession().createQuery(String.format("from %s node where node.parent=null and node.resource.id = :resourceId order by lft", persistentClass.getName()))
        .setLong("resourceId", resourceId)
		.list();
	}
	
	@Override
	@Transactional(readOnly = false)
	public int removeAll(Resource resource) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		Session session = getSession();
		
		// get property name for this entity
		String propName = persistentClass.getSimpleName().toLowerCase();
		
		// first remove treeNodes from dwcore records
		String hqlUpdate = String.format("update DarwinCore dwc SET dwc.%s=null WHERE dwc.resource = :resource", propName);
		session.createQuery( hqlUpdate ).setEntity("resource", resource).executeUpdate();
		
		// now delete region entities
		hqlUpdate = String.format("delete %s reg WHERE reg.resource = :resource", persistentClass.getSimpleName());
		int count = session.createQuery( hqlUpdate )
		        .setEntity("resource", resource)
		        .executeUpdate();
		log.info(String.format("Removed %s %ss bound to resource %s", count, propName, resource.getTitle()));
		return count;
	}

}
