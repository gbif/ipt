package org.gbif.provider.dao;

import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;

public interface ResourceDao<T extends Resource> extends GenericDao<T, Long>{
	
	/**
	 * Return all resources created by that user
	 * @param userId
	 * @return
	 */
	public List<T> getResourcesByUser(Long userId);
}
