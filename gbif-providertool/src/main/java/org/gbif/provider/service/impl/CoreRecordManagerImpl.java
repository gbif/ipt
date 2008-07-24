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

import org.appfuse.dao.GenericDao;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.dao.CoreRecordDao;
import org.gbif.provider.dao.UploadEventDao;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class CoreRecordManagerImpl<T extends CoreRecord> extends GenericManagerImpl<T> implements CoreRecordManager<T> {
    private CoreRecordDao<T> dao;

    public CoreRecordManagerImpl(final CoreRecordDao<T> coreRecordDao) {
		super(coreRecordDao);
    	this.dao=coreRecordDao;
    }


	public T findByLocalId(String localId, Long resourceId) {
		return dao.findByLocalId(localId, resourceId);
	}

	public void flagAsDeleted(Long resourceId) {
		dao.flagAsDeleted(resourceId);
	}

	public T get(Long Id, Long resourceId) {
		return dao.get(Id, resourceId);
	}

	public void updateIsDeleted(Long id, Long resourceId, boolean isDeleted) {
		dao.updateIsDeleted(id, resourceId, isDeleted);
	}



}
