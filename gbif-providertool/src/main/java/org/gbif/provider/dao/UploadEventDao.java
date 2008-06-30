package org.gbif.provider.dao;

import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.UploadEvent;

public interface UploadEventDao extends GenericDao<UploadEvent, Long>{
	
	public List<UploadEvent> getUploadEventsByResource(Long resourceId);

}
