package org.gbif.provider.dao;

import java.util.List;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.UploadEvent;
import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;


public class UploadEventDaoTest extends BaseDaoTestCase{
	protected UploadEventDao uploadEventDao;

	public void setUploadEventDao(UploadEventDao uploadEventDao) {
		this.uploadEventDao = uploadEventDao;
	}
	
	@Test
	public void testGetUploadEventsByResource(){
		try {
			List<UploadEvent> events = uploadEventDao.getUploadEventsByResource(1l);
			for (UploadEvent ev : events){
				logger.debug(ev);
			}
		}catch(ObjectRetrievalFailureException e){
			logger.debug(e.getMessage());
		}
	}
}
