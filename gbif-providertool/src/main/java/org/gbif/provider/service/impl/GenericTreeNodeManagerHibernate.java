package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.BaseObject;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.Session;

public class GenericTreeNodeManagerHibernate<T extends TreeNode<T,?>> extends GenericManagerHibernate<T> implements TreeNodeManager<T> {

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

	public List<T> getParents(Long resourceId, Long nodeId) {
		List<T> result = new ArrayList<T>(); 
		return result;
	}

	public List<T> getRoots(Long resourceId) {
        return getSession().createQuery(String.format("from %s node where node.parent=null and node.resource.id = :resourceId order by lft", persistentClass.getName()))
        .setLong("resourceId", resourceId)
		.list();
	}

	public int deleteAll(DatasourceBasedResource resource) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		Session session = getSession();
		
		// get property name for this entity
		String propName = persistentClass.getSimpleName().toLowerCase();
		
		// first remove regions from dwcore records
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
