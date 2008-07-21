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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.appfuse.model.User;
import org.appfuse.service.GenericManager;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
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

public class RdbmsUploadJob extends UploadBaseJob {
	protected static final Log log = LogFactory.getLog(RdbmsUploadJob.class);
	private static final String RESOURCE_ID = "resourceId";
	private static final String USER_ID = "userId";
	private static final String MAX_RECORDS = "maxRecords";
	
	private static I18nLog logdb = I18nLogFactory.getLog(RdbmsUploadJob.class);


    private ResourceManager<OccurrenceResource> occResourceManager;
    private GenericManager<UploadEvent, Long> uploadEventManager;
    private DatasourceInspectionManager datasourceInspectionManager;
    private OccurrenceUploadManager occurrenceUploadManager;

    
	public RdbmsUploadJob(){
		super();
	}
	
	public RdbmsUploadJob(
			ResourceManager<OccurrenceResource> occResourceManager,
			GenericManager<UploadEvent, Long> uploadEventManager,
			DatasourceInspectionManager datasourceInspectionManager,
			OccurrenceUploadManager occurrenceUploadManager) {
		super();
		this.occResourceManager = occResourceManager;
		this.uploadEventManager = uploadEventManager;
		this.datasourceInspectionManager = datasourceInspectionManager;
		this.occurrenceUploadManager = occurrenceUploadManager;
	}

	public void setOccResourceManager(
			ResourceManager<OccurrenceResource> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
	public void setUploadEventManager(
			GenericManager<UploadEvent, Long> uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}
	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}
	public void setOccurrenceUploadManager(
			OccurrenceUploadManager occurrenceUploadManager) {
		this.occurrenceUploadManager = occurrenceUploadManager;
	}

	
	public static Job newUploadJob(Resource resource, User user, int repeatInDays, Integer maxRecords){
		// create job data
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put(RESOURCE_ID, resource.getId());
		seed.put(USER_ID, user.getId());
		seed.put(MAX_RECORDS, maxRecords);
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
	

	public void launch(Map<String, Object> seed) throws Exception {
		try {
			log.info("Starting "+this.getClass().getSimpleName() +" with seed "+seed);
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, JobUtils.getSourceTypeId(this.getClass()));
			//TODO: set this in the scheduler constructor???
			//MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);
			try {
				Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
				Long userId = Long.valueOf(seed.get(USER_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));
				MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, resourceId);
				MDC.put(I18nDatabaseAppender.MDC_USER, userId);
				// set resource context for DatasourceInterceptor
				DatasourceContextHolder.setResourceId(resourceId);
				OccurrenceResource resource = occResourceManager.get(resourceId);
				// create rdbms source
				ViewMapping coreViewMapping = resource.getCoreMapping();
				ResultSet rs = datasourceInspectionManager.executeViewSql(coreViewMapping.getViewSql());	        
				RdbmsImportSource source = RdbmsImportSource.newInstance(rs, coreViewMapping);
				// track upload in upload event metadata (mainly statistics)
				UploadEvent coreEvent = new UploadEvent();
				coreEvent.setResource(resource);
				// try to upload records
				try {
					Map<String, Long> idMap = occurrenceUploadManager.uploadCore(source, resource, coreEvent);
					// upload further extensions one by one
					for (ViewMapping view : resource.getExtensionMappings().values()){
						rs = datasourceInspectionManager.executeViewSql(view.getViewSql());	        
						source = RdbmsImportSource.newInstance(rs, view);
						occurrenceUploadManager.uploadExtension(source, idMap, resource, view.getExtension());
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


	public String status() {
		return "still running";
	}
}
