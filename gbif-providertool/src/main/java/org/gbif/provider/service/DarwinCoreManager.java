package org.gbif.provider.service;

import java.util.List;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.UploadEvent;

public interface DarwinCoreManager extends CoreRecordManager<DarwinCore>{
	List<DarwinCore> getByTaxon(Long taxonId, Long resourceId, boolean inclChildren);
	List<DarwinCore> getByRegion(Long regionId, Long resourceId, boolean inclChildren);
}
