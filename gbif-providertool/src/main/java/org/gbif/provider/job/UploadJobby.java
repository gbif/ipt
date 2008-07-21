package org.gbif.provider.job;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.appfuse.model.User;
import org.appfuse.service.GenericManager;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.OccurrenceUploadManager;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.util.JSONUtils;
import org.springframework.transaction.annotation.Transactional;

public class UploadJobby extends UploadBaseJob{
	protected static final Log log = LogFactory.getLog(UploadJobby.class);
	private static final String RESOURCE_ID = "resourceId";
	private static final String USER_ID = "userId";
	private static final String MAX_RECORDS = "maxRecords";
	
	private static I18nLog logdb = I18nLogFactory.getLog(UploadJobby.class);

    private ResourceManager<OccurrenceResource> occResourceManager;
    private GenericManager<UploadEvent, Long> uploadEventManager;
    private DatasourceInspectionManager datasourceInspectionManager;
	private ExtensionRecordDao extensionRecordDao;
	private DarwinCoreDao darwinCoreDao;
    	
	public UploadJobby(
			ResourceManager<OccurrenceResource> occResourceManager,
			GenericManager<UploadEvent, Long> uploadEventManager,
			DatasourceInspectionManager datasourceInspectionManager,
			ExtensionRecordDao extensionRecordDao,
			DarwinCoreDao darwinCoreDao
			) {
		super();
		this.occResourceManager = occResourceManager;
		this.uploadEventManager = uploadEventManager;
		this.datasourceInspectionManager = datasourceInspectionManager;
		this.extensionRecordDao=extensionRecordDao;
		this.darwinCoreDao=darwinCoreDao;
	}

	
	public static Job newUploadJob(Resource resource, User user, int repeatInDays, Integer maxRecords){
		// create job data
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put(RESOURCE_ID, resource.getId());
		seed.put(USER_ID, user.getId());
		seed.put(MAX_RECORDS, maxRecords);
		// create upload job
		Job job = new Job();
		job.setJobClassName(UploadJobby.class.getName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setRepeatInDays(repeatInDays);
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setRunningGroup(JobUtils.getJobGroup(resource));
		job.setName("RDBMS data upload");
		job.setDescription("Data upload from RDBMS to resource "+resource.getTitle());
		return job;				
	}
	

	public void launch(Map<String, Object> seed) throws Exception {
		try {
			log.info("Starting "+this.getClass().getSimpleName() +" with seed "+seed);
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, JobUtils.getSourceTypeId(this.getClass()));
			//TODO: set this in the scheduler constructor???
			//MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
			try {
				Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
				Long userId = Long.valueOf(seed.get(USER_ID).toString());
				Integer maxRecords = Integer.valueOf(seed.get(MAX_RECORDS).toString());
				MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));
				MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, resourceId);
				MDC.put(I18nDatabaseAppender.MDC_USER, userId);
				// set resource context for DatasourceInterceptor
				DatasourceContextHolder.setResourceId(resourceId);
				OccurrenceResource resource = occResourceManager.get(resourceId);
				// create rdbms source
				ViewMapping coreViewMapping = resource.getCoreMapping();
				ResultSet rs = datasourceInspectionManager.executeViewSql(coreViewMapping.getViewSql());	        
				RdbmsImportSource source = RdbmsImportSource.newInstance(rs, coreViewMapping, maxRecords);
				// track upload in upload event metadata (mainly statistics)
				UploadEvent coreEvent = new UploadEvent();
				coreEvent.setResource(resource);
				// try to upload records
				try {
					Map<String, Long> idMap = uploadCore(source, resource, coreEvent);
					// upload further extensions one by one
					for (ViewMapping view : resource.getExtensionMappings().values()){
						rs = datasourceInspectionManager.executeViewSql(view.getViewSql());	        
						source = RdbmsImportSource.newInstance(rs, view);
						uploadExtension(source, idMap, resource, view.getExtension());
					}
				} catch (Exception e) {
					logdb.error("Error uploading data", e);
					e.printStackTrace();
				}
				// save upload event
				Date now = new Date();
				coreEvent.setExecutionDate(now);
				uploadEventManager.save(coreEvent);
				// update resource properties
				resource.setLastImport(now);
				resource.setRecordCount(coreEvent.getRecordsUploaded());
				occResourceManager.save(resource);
			} catch (NumberFormatException e) {
				String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
				logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
			}

		} catch (Exception e) {
			logdb.error("Error occurred while running "+this.getClass().getSimpleName(), e);
		}
	}

	private Map<String, Long> uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event) throws InterruptedException {
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
		for (ImportRecord rec : source){
			// check if thread should shutdown...
			if (Thread.interrupted()) {
			    throw new InterruptedException();
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
				darwinCoreDao.updateIsDeleted(oldRecord.getId(), resource.getId(), false);
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

	private void uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension) throws InterruptedException {
		for (ImportRecord rec : source){
			// check if thread should shutdown...
			if (Thread.interrupted()) {
			    throw new InterruptedException();
			}
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
	
	public String status() {
		return "still running";
	}
}
