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

import java.util.List;
import java.util.Map;

import org.appfuse.dao.GenericDao;
import org.appfuse.service.GenericManager;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.ResourceDao;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class ResourceManagerImpl<T extends Resource> extends GenericManagerImpl<T> implements ResourceManager<T> {
	private static I18nLog logdb = I18nLogFactory.getLog(UploadEventManagerImpl.class);
	protected ResourceDao<T> resourceDao;

	public ResourceManagerImpl(ResourceDao<T> resourceDao) {
		super(resourceDao);
		this.resourceDao=resourceDao;
	}

	public List<T> getResourcesByUser(Long userId) {
        logdb.warn("gimme.warn","warn arg");
		return resourceDao.getResourcesByUser(userId);
	}

}
