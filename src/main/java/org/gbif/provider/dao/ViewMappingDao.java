package org.gbif.provider.dao;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.ViewMapping;

public interface ViewMappingDao extends GenericDao<ViewMapping, Long>{
	public List<ViewMapping> findByResource(Long resourceId);
}
