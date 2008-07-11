package org.gbif.provider.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.dao.ResourceDao;
import org.gbif.provider.dao.UploadEventDao;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ResourceDaoImpl<T extends Resource> extends GenericDaoHibernate<T, Long> implements ResourceDao<T> {
	private Class<T> resourceClass;
	
	public ResourceDaoImpl(final Class<T> persistentClass) {
		super(persistentClass);
		resourceClass = persistentClass;
	}

	public List<T> getResourcesByUser(final Long userId) {
		HibernateTemplate template = getHibernateTemplate();
		
		//template.findByCriteria(criteria);
		List<T> resources =  (List) template.execute(new HibernateCallback() {
			public Object doInHibernate(final Session session) {
				final Query query = session.createQuery("select res FROM :resourceClass res JOIN res.created user WHERE user.id = :userId");
				query.setParameter("resourceClass", resourceClass.getSimpleName());
				query.setParameter("userId", userId);
				query.setCacheable(true);
				return query.list();
			}
		});
		return resources;
	}

}
