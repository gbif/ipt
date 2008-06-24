package org.gbif.provider.upload;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceBasedResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;

public class RdbmsUploader implements Launchable{
	protected static final Log log = LogFactory.getLog(RdbmsUploader.class);

    private DatasourceBasedResourceManager<OccurrenceResource> occResourceManager;
    private GenericManager<UploadEvent, Long> uploadEventManager;
    private DatasourceInspectionManager datasourceInspectionManager;

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
		Long id = Long.valueOf(seed.get("resourceId").toString()); 
		log.info("Starting Upload job for resource "+id);
		DatasourceBasedResource resource = occResourceManager.get(id);
		// upload records
		boolean coreUploaded = uploadView(resource.getCoreMapping(), resource);
		if (coreUploaded){
			for (ViewMapping view : resource.getExtensionMappings()){
				uploadView(view, resource);
			}
		}
	}

	/**
	 * upload data for a single "view" i.e. an extension or core mapping
	 * Returns true to indicate successful upload or false for failed uploads
	 * @param view
	 * @return
	 */
	private boolean uploadView(ViewMapping view, DatasourceBasedResource resource) {
		ResultSet rs;
		log.info("Uploading view "+view.getExtension().getName()+" for resource "+resource.getTitle());
		try {
			rs = datasourceInspectionManager.executeViewSql(view.getViewSql());
	        ResultSetMetaData meta = rs.getMetaData();
	        return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("Couldnt upload view "+view.getExtension().getName());
			e.printStackTrace();
		}
        return false;
	}

}
