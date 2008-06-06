package org.gbif.provider.dao.hibernate;

import java.util.List;

import org.appfuse.dao.hibernate.GenericDaoHibernate;
import org.gbif.provider.dao.ViewMappingDao;
import org.gbif.provider.model.ViewMapping;

public class ViewMappingHibernateDao extends GenericDaoHibernate<ViewMapping, Long> implements ViewMappingDao{

	public ViewMappingHibernateDao() {
		super(ViewMapping.class);
	}

	
	public List<ViewMapping> findByResource(Long resourceId) {
        List mappings = getHibernateTemplate().find("from ViewMapping as map join map.resource as resource where resource.id = ?", resourceId);
        if (mappings.isEmpty()) {
            //return null;
        } else {
            //return (ViewMapping) mappings.get(0);
        }
		return mappings;
	}

}
