/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.dao.ResourceDao;
import org.gbif.provider.dao.UploadEventDao;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ResourceDaoImpl<T extends Resource> extends GenericDaoHibernate<T, Long> implements ResourceDao<T> {
	private final Class<T> resourceClass;
	
	public ResourceDaoImpl(final Class<T> persistentClass) {
		super(persistentClass);
		this.resourceClass = persistentClass;
	}

	public List<T> getResourcesByUser(final Long userId) {
		HibernateTemplate template = getHibernateTemplate();
		List<T> resources =  (List) template.execute(new HibernateCallback() {
			public Object doInHibernate(final Session session) {
				String hql = "select res FROM "+resourceClass.getSimpleName()+" res WHERE res.created.id = :userId";
				final Query query = session.createQuery(hql);
				query.setParameter("userId", userId);
				query.setCacheable(true);
				return query.list();
			}
		});
		return resources;
	}

}
