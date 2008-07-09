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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.job.RdbmsUploadJob;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.OccurrenceUploadManager;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

public class OccurrenceUploadManagerImpl implements OccurrenceUploadManager{
	protected static final Log log = LogFactory.getLog(OccurrenceUploadManagerImpl.class);
	private static I18nLog logdb = I18nLogFactory.getLog(RdbmsUploadJob.class);

	private ExtensionRecordDao extensionRecordDao;
	private DarwinCoreDao darwinCoreDao;

	public void setExtensionRecordDao(ExtensionRecordDao extensionRecordDao) {
		this.extensionRecordDao = extensionRecordDao;
	}

	public void setDarwinCoreDao(DarwinCoreDao darwinCoreDao) {
		this.darwinCoreDao = darwinCoreDao;
	}

	
	public Map<String, Long> uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event) {
		log.info("Uploading occurrence core for resource "+resource.getTitle());
		Map<String, Long> idMap = new HashMap<String, Long>();
		// use a single date for now (e.g. to set dateLastModified)
		Date now = new Date();
		// flag all previously existing records as deleted before updating/inserting new ones
		darwinCoreDao.flagAsDeleted(resource.getId());
		// keep track of the following statistics for UploadEvent
		int recordsUploaded = 0;
		int recordsDeleted = 0;
		int recordsChanged = 0;
		int recordsAdded = 0;
		// go through source records one by one
		for (CoreRecord rec : source){
			if (recordsUploaded>1200){
				//FIXME: allow only small uploads while testing
				break;
			}
			// get previous record or null if it didnt exist yet based on localID and resource
			DarwinCore oldRecord = darwinCoreDao.findByLocalId(rec.getLocalId(), resource.getId());
			// get darwincore record based on this core record
			DarwinCore dwc = DarwinCore.newInstance(rec);
			
			// attach to the occurrence resource
			dwc.setResource(resource);
			
			// assign managed properties
			updateManagedProperties(dwc, oldRecord);
			
			// check if new record version is different from old one
			if (oldRecord != null && oldRecord.hashCode() == dwc.hashCode() && oldRecord.equals(dwc)){
				// same record. reset isDeleted flag = false
				darwinCoreDao.updateIsDeleted(oldRecord.getId(), false);
			}else if (oldRecord!=null){
				// modified record
				dwc.setModified(now);
				// remove old + insert new record
				// TODO: could be improved by updating existing record!
				darwinCoreDao.remove(oldRecord.getId());
				recordsChanged++;
			}else{
				// new record that didnt exist before
				dwc.setModified(now);
				recordsAdded++;
			}
			// count all inserted records
			recordsUploaded++;
			if (recordsUploaded % 1000 == 0){
				log.info(recordsUploaded+" uploaded for resource "+resource.getId());
			}
			if (recordsUploaded % 100 == 0){
				logdb.info(recordsUploaded+" uploaded for resource "+resource.getId());
			}
			// insert/update record
			dwc = darwinCoreDao.save(dwc);
			// the new darwin core id used for all other extensions
			Long coreId = dwc.getId();
			idMap.put(rec.getLocalId(), coreId);
		}
		
		// update resource and upload event statistics
		recordsDeleted = resource.getRecordCount()+recordsAdded-recordsUploaded;
		event.setRecordsAdded(recordsAdded);
		event.setRecordsChanged(recordsChanged);
		event.setRecordsDeleted(recordsDeleted);
		event.setRecordsUploaded(recordsUploaded);		
		resource.setRecordCount(recordsUploaded);
		
		return idMap;
	}
	
	private void updateManagedProperties(DarwinCore dwc, DarwinCore oldRecord){
		// assign new GUID if none exists
		if (dwc.getGuid() == null){
			// if old version exists already reuse the previously assigned GUID
			if (oldRecord != null){
				dwc.setGuid(oldRecord.getGuid());
			}else{
				dwc.setGuid(UUID.randomUUID().toString());					
			}
		}			
		// assign link to detailed record if not existing
		if (dwc.getLink() == null){
			//FIXME: assign real URL to webapp
			dwc.setLink("http://localhost/providertool/record/"+dwc.getGuid());
		}
	}

	public void uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension) {
		for (CoreRecord rec : source){
			Long coreId = idMap.get(rec.getLocalId());
			if (coreId == null){
				String[] paras = {rec.getLocalId(), extension.getName(), resource.getId().toString()};
				//FIXME: use i18n job logging ???
				log.warn("uploadManager.unknownLocalId" +paras.toString());
			}else{
				// TODO: check if record has changed
				ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
				extensionRecordDao.insertExtensionRecord(extRec);
			}
		}
	}

}
