package org.gbif.provider.service.impl;

import java.util.List;

import org.appfuse.service.impl.GenericManagerImpl;
import org.gbif.provider.dao.hibernate.ViewMappingHibernateDao;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.ViewMappingManager;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class ViewMappingManagerImpl extends GenericManagerImpl<ViewMapping, Long> implements ViewMappingManager {
	private static ViewMappingHibernateDao viewMappingDao;
    
	public ViewMappingManagerImpl(ViewMappingHibernateDao viewMappingDao) {
		super(viewMappingDao);
		this.viewMappingDao = viewMappingDao;
	}

	public List<ViewMapping> findByResource(Long resourceId) {
		return viewMappingDao.findByResource(resourceId);		
	}

}
