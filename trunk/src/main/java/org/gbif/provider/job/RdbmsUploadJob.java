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
import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceBasedResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.UploadManager;

public class RdbmsUploadJob implements Launchable{
	protected static final Log log = LogFactory.getLog(RdbmsUploadJob.class);

    private DatasourceBasedResourceManager<OccurrenceResource> occResourceManager;
    private GenericManager<UploadEvent, Long> uploadEventManager;
    private DatasourceInspectionManager datasourceInspectionManager;
    private UploadManager uploadManager;

	public void setOccResourceManager(
			DatasourceBasedResourceManager<OccurrenceResource> occResourceManager) {
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

	public void launch(Map<String, Object> seed) throws Exception {
		Long resourceId = Long.valueOf(seed.get("resourceId").toString()); 
		log.info("Starting Upload job for resource "+resourceId);
		OccurrenceResource resource = occResourceManager.get(resourceId);
		// create rdbms source
		ViewMapping coreViewMapping = resource.getCoreMapping();
		ResultSet rs = datasourceInspectionManager.executeViewSql(coreViewMapping.getViewSql());	        
		RdbmsImportSource source = RdbmsImportSource.getInstance(rs, coreViewMapping);
        UploadEvent coreEvent = new UploadEvent();
        coreEvent.setResource(resource);
		// upload records
		Map<String, Long> idMap = uploadManager.uploadCore(source, resource, coreViewMapping.getExtension(), coreEvent);
		// save upload event
        Date now = new Date();
        coreEvent.setExecutionDate(now);
		uploadEventManager.save(coreEvent);
		// update resource properties
		resource.setLastImport(now);
		resource.setRecordCount(coreEvent.getRecordsUploaded());
		occResourceManager.save(resource);
		// upload further extensions one by one
		for (ViewMapping view : resource.getExtensionMappings()){
			rs = datasourceInspectionManager.executeViewSql(view.getViewSql());	        
			source = RdbmsImportSource.getInstance(rs, view);
			uploadManager.uploadExtension(source, idMap, resource, view.getExtension());
		}
	}


	private UploadEvent getFakeUpload(DatasourceBasedResource resource){
		Random rnd = new Random();
        int numExistingRecords = resource.getRecordCount();
		int numAdded = rnd.nextInt(10000);
		int numDeleted = rnd.nextInt(numExistingRecords/100);
		int numChanged = rnd.nextInt((numExistingRecords-numDeleted-numAdded)/10);
		int numUploaded = numExistingRecords+numAdded-numDeleted;
        UploadEvent event = new UploadEvent();
        event.setRecordsAdded(numAdded);
        event.setRecordsDeleted(numDeleted);
        event.setRecordsChanged(numChanged);
        event.setRecordsUploaded(numUploaded);
		return event;
	}
}
