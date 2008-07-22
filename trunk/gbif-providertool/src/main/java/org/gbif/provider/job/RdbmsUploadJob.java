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

import org.apache.log4j.MDC;
import org.appfuse.model.User;
import org.gbif.provider.service.GenericManager;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.util.JSONUtils;
import org.springframework.transaction.annotation.Transactional;

public class RdbmsUploadJob extends UploadBaseJob{
	private ResourceManager<OccurrenceResource> occResourceManager;
    private DatasourceInspectionManager datasourceInspectionManager;
    
	public RdbmsUploadJob(
			ResourceManager<OccurrenceResource> occResourceManager,
			UploadEventManager uploadEventManager,
			DatasourceInspectionManager datasourceInspectionManager,
			ExtensionRecordDao extensionRecordDao,
			GenericManager<DarwinCore> darwinCoreManager
			) {
		super(uploadEventManager, darwinCoreManager, extensionRecordDao);
		this.occResourceManager = occResourceManager;
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public static Map<String, Object> getSeed(Long resourceId, Long userId, Integer maxRecords){
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put(RESOURCE_ID, resourceId);
		seed.put(USER_ID, userId);
		seed.put(MAX_RECORDS, maxRecords);
		return seed;
	}

	
	public static Job newUploadJob(Resource resource, User user, int repeatInDays, Integer maxRecords){
		// create job data
		Map<String, Object> seed = getSeed(resource.getId(), user.getId(), maxRecords);
		// create upload job
		Job job = new Job();
		job.setJobClassName(RdbmsUploadJob.class.getName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setRepeatInDays(repeatInDays);
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setRunningGroup(JobUtils.getJobGroup(resource));
		job.setName("RDBMS data upload");
		job.setDescription("Data upload from RDBMS to resource "+resource.getTitle());
		return job;				
	}
	

	public void launch(Map<String, Object> seed) {
		try {
			log.info("Starting "+this.getClass().getSimpleName() +" with seed "+seed);
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, JobUtils.getSourceTypeId(this.getClass()));
			//TODO: set this in the scheduler constructor???
			//MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
			Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
			try{
				Long userId = Long.valueOf(seed.get(USER_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_USER, userId);
			} catch (NumberFormatException e) {
				String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
				logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
			}
			Integer maxRecords = null;
			if (seed.get(MAX_RECORDS) != null){
				try{
					maxRecords = Integer.valueOf(seed.get(MAX_RECORDS).toString());
				} catch (NumberFormatException e) {
					String[] params = {MAX_RECORDS, seed.toString()};
					logdb.warn("{0} in seed is no Integer {1}", params, e);
				}
			}
			MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, resourceId);
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
			String[] params = {RESOURCE_ID, seed.toString()};
			logdb.error("{0} in seed is no Integer {1}", params, e);
		} catch (Exception e) {
			logdb.error("Error occurred while running "+this.getClass().getSimpleName(), e);
		}
	}

}
