package org.gbif.provider.dao;

import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.CoreRecord;

public interface CoreRecordDao<T extends CoreRecord> extends GenericDao<T, Long>{
	/**
	 * Flag all core records for a given resource as deleted by setting coreRecord.isDeleted=true
	 * @param resourceId
	 */
	public void flagAsDeleted(Long resourceId);
	public void updateIsDeleted(Long id, boolean isDeleted);
	/**
	 * Find a core record via its local ID within a given resource
	 * @param localId the local identifier used in the source
	 * @param resourceId the resource identifier for the source
	 * @return
	 */
	public T findByLocalId(String localId, Long resourceId);
}
