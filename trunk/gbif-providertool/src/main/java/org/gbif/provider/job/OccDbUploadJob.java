package org.gbif.provider.job;

import java.io.File;
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
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.ConfigUtil;
import org.gbif.provider.util.ZipUtil;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.util.JSONUtils;
import org.springframework.transaction.annotation.Transactional;

public class OccDbUploadJob extends OccUploadBaseJob{
	public static final int SOURCE_TYPE_ID = 1;
    private DatasourceInspectionManager datasourceInspectionManager;
    
	public OccDbUploadJob(
			ResourceManager<OccurrenceResource> occResourceManager,
			UploadEventManager uploadEventManager,
			DatasourceInspectionManager datasourceInspectionManager,
			ExtensionRecordDao extensionRecordDao,
			CoreRecordManager<DarwinCore> darwinCoreManager
			) {
		super(uploadEventManager, darwinCoreManager, extensionRecordDao, occResourceManager);
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
		job.setJobClassName(OccDbUploadJob.class.getName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setRepeatInDays(repeatInDays);
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setRunningGroup(JobUtils.getJobGroup(resource));
		job.setName("DB data upload");
		job.setDescription("Data upload from RDBMS to resource "+resource.getTitle());
		return job;				
	}
	

	public int getSourceType() {
		return SOURCE_TYPE_ID;
	}

	public ImportSource getCoreImportSource(Map<String, Object> seed, OccurrenceResource resource, Integer maxRecords) throws ImportSourceException {
			Long resourceId = resource.getId();
			// set resource context for DatasourceInterceptor
			DatasourceContextHolder.setResourceId(resourceId);
			// create rdbms source
			ViewCoreMapping coreViewMapping = resource.getCoreMapping();
			ResultSet rs;
			try {
				rs = datasourceInspectionManager.executeViewSql(coreViewMapping.getSourceSql());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new ImportSourceException();
			}	        
			RdbmsImportSource source = RdbmsImportSource.newInstance(rs, coreViewMapping, maxRecords);

			return source;
	}
	
	public ImportSource getImportSource(Map<String, Object> seed, OccurrenceResource resource, Extension extension, Integer maxRecords) throws ImportSourceException {
		Long resourceId = resource.getId();
		// set resource context for DatasourceInterceptor
		DatasourceContextHolder.setResourceId(resourceId);
		// create rdbms source
		ViewMappingBase vm = resource.getExtensionMapping(extension);
		if (vm == null){
			throw new ImportSourceException("No mapping exists for extension "+extension.getName());
		}
		ResultSet rs;
		try {
			rs = datasourceInspectionManager.executeViewSql(vm.getSourceSql());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new ImportSourceException(e);
		}
		RdbmsImportSource source = RdbmsImportSource.newInstance(rs, vm, maxRecords);

		return source;
}

}
