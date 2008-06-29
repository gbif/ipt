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

package org.gbif.provider.webapp.action;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.job.Launchable;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.util.Constants;
import org.hibernate.type.SortedMapType;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysql.jdbc.DatabaseMetaData;
import com.opensymphony.xwork2.Preparable;

public class UploadAction extends BaseResourceAction implements Preparable{
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<UploadEvent, Long> uploadEventManager;
    private OccurrenceResource resource;
	private List<Launchable> scheduledJobs;
	private List<UploadEvent> uploadEvents;
	private Launchable uploader;
	
	
	public void setRdbmsUploader(Launchable rdbmsUploader) {
		this.uploader = rdbmsUploader;
	}

	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setUploadEventManager(
			GenericManager<UploadEvent, Long> uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public List<UploadEvent> getUploadEvents() {
		return uploadEvents;
	}

	public OccurrenceResource getResource() {
		return resource;
	}


	
	public void prepare() {
    	resource = occResourceManager.get(getResourceId());
		scheduledJobs = new ArrayList<Launchable>();
    	//TODO: validate resource?
    	
	}


	public String execute(){
        return SUCCESS;
	}
	
	public String addUploadJob() throws Exception{
		// until the job scheduler is integrated run the upload job directly!
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put("resourceId", resource.getId());
		uploader.launch(seed);
        saveMessage(getText("upload.addedJob", Arrays.asList(resource.getRecordCount())));
		return SUCCESS;
	}
	
	public String history(){
		//TODO: select only events belonging to this resource!
		uploadEvents=uploadEventManager.getAll();
		return SUCCESS;
	}

}
