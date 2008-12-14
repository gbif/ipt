package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

public class TreeNodeSupportHibernate<T>{
	private final Class persistentClass;
    protected final Log log = LogFactory.getLog(getClass());
	
	public TreeNodeSupportHibernate(final Class<T> persistentClass) {
        this.persistentClass=persistentClass;
	}
	
	
	
	public List<T> getChildren(Long resourceId, Long parentId, Session session) {
        return session.createQuery(String.format("from %s node where node.parent.id = :parentId and node.resource.id = :resourceId order by lft", persistentClass.getName()))
        .setLong("resourceId", resourceId)
        .setLong("parentId", parentId)
		.list();
	}

	public List<T> getDescendants(Long resourceId, Long nodeId, Session session) {
		List<T> result = new ArrayList<T>(); 
		return result;
	}

	public List<Long> getParentIds(Long resourceId, Long nodeId, Session session) {
        return session.createQuery(String.format("SELECT parent.id FROM %s node, %s parent WHERE node.id = :nodeId and node.lft between parent.lft and parent.rgt   ORDER BY parent.lft", persistentClass.getName(), persistentClass.getName()))
        .setLong("nodeId", nodeId)
		.list();
	}	

	public List<T> getParents(Long resourceId, Long nodeId, Session session) {
		List<T> result = new ArrayList<T>(); 
		return result;
	}

	public List<T> getRoots(Long resourceId, Session session) {
        return session.createQuery(String.format("from %s node where node.parent=null and node.resource.id = :resourceId order by lft", persistentClass.getName()))
        .setLong("resourceId", resourceId)
		.list();
	}
	
	
	@Transactional(readOnly = false)
	public int removeAll(Resource resource, Session session) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		
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
	
	public int countTerminalNodes(Long resourceId, Session session) {
        return ((Long) session.createQuery(String.format("select count(e) from %s e WHERE e.resource.id = :resourceId", persistentClass.getSimpleName()))
        .setLong("resourceId", resourceId)
        .iterate().next() ).intValue();
	}

	public void buildNestedSet(DataResource resource, Session session){
		//FIXME
		String hqlUpdate = String.format("update %s e SET e.lft=id*2-1, e.rgt=id*2 WHERE e.resource = :resource", persistentClass.getSimpleName());
		session.createQuery( hqlUpdate ).setEntity("resource", resource).executeUpdate();
		log.warn("buildNestedSet not implemented properly yet. Just creating flat list of nodes, not nested");
	}

}
