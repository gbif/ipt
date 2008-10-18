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

package org.gbif.provider.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class GenericResourceManagerHibernate<T extends Resource> extends GenericManagerHibernate<T> implements GenericResourceManager<T> {
	private static I18nLog logdb = I18nLogFactory.getLog(GenericResourceManagerHibernate.class);

	public GenericResourceManagerHibernate(final Class<T> persistentClass) {
		super(persistentClass);
	}
	
	public T get(final String guid) {
		return (T) getSession().createQuery(String.format("select res FROM %s res WHERE guid = :guid", persistentClass.getSimpleName()))
		.setParameter("guid", guid).uniqueResult();
	}

	public List<T> getResourcesByUser(final Long userId) {
		return getSession().createQuery(String.format("select res FROM %s res WHERE res.creator.id = :userId", persistentClass.getSimpleName()))
    		.setParameter("userId", userId).list();
	}
	
	@Override
    public List<T> getTop(int maxResults) {
        return getSession().createQuery(String.format("from %s order by modified desc", persistentClass.getName()))
        		.setMaxResults(maxResults)
        		.list();
    }
	
}
