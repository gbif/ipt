package org.gbif.provider.service;

import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.UploadEvent;

public interface GenericManager<T> extends org.appfuse.service.GenericManager<T, Long>{
	/**
	 * Gets all records without duplicates.
	 * @See GenericDao.getAllDistinct()
	 * @return
	 */
	public List<T> getAllDistinct();
	
	public void flush();
		
}
