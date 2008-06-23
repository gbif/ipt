package org.gbif.provider.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.dao.UploadEventDao;
import org.gbif.provider.model.UploadEvent;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class UploadEventDaoImpl extends GenericDaoHibernate<UploadEvent, Long> implements UploadEventDao {
	public UploadEventDaoImpl(Class<UploadEvent> persistentClass) {
		super(persistentClass);
	}

	@SuppressWarnings("unchecked")
	public List<UploadEvent> getUploadEventsByResource(final Long resourceId) {
		HibernateTemplate template = getHibernateTemplate();

		List<UploadEvent> events =  (List) template.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createQuery("FROM UploadEvent event JOIN event.resource res WHERE res.id = :resourceId");
				//Query query = session.createQuery("from UploadEvent as event where UploadEvent.resource.id = :resourceId");
				query.setParameter("resourceId", resourceId);
				query.setCacheable(true);
				return query.list();
			}
		});
		return events;
	}

}
