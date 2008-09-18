package org.gbif.provider.service.impl;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceRelatedObject;
import org.gbif.provider.service.GenericResourceRelatedManager;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

	@Transactional(readOnly=true)
	public class GenericResourceRelatedManagerHibernate<T extends ResourceRelatedObject> extends GenericManagerHibernate<T> implements GenericResourceRelatedManager<T> {

	    /**
	     * Constructor that takes in a class to see which type of entity to persist
	     * @param persistentClass the class type you'd like to persist
	     */
	    public GenericResourceRelatedManagerHibernate(final Class<T> persistentClass) {
	        super(persistentClass);
	    }
	    
		@Transactional(readOnly = false)
		public int removeAll(Resource resource) {
			// use DML-style HQL batch updates
			// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
			Session session = getSession();
			// now delete region entities
			String hqlUpdate = String.format("delete %s e WHERE e.resource = :resource", persistentClass.getSimpleName());
			int count = session.createQuery( hqlUpdate )
			        .setEntity("resource", resource)
			        .executeUpdate();
			log.info(String.format("Removed %s %ss bound to resource %s", count, persistentClass.getSimpleName(), resource.getTitle()));
			return count;
		}	
	}
	
