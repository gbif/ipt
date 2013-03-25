package org.gbif.provider.webapp.action.manage;

import java.util.List;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ImportAction extends BaseDataResourceAction implements Preparable{
	private static final String BUSY = "resource-busy";
	private static final String READY = "resource-ready";
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private UploadEventManager uploadEventManager; 
	private String status;
	private boolean busy = false;
	private List<UploadEvent> uploadEvents;
	private String gChartData;
	
	
	public void prepare(){
		if (resource_id != null){
			busy=cacheManager.isBusy(resource_id);
		}		
	}

	public String execute() {
		// load resource
		super.prepare();
		// create GoogleChart string
		gChartData = uploadEventManager.getGoogleChartData(resource_id, 400, 200);
		return SUCCESS;
	}		

	public String upload(){
		// run task in different thread
		cacheManager.runUpload(resource_id);			
		return SUCCESS;
	}
	
	public String cancel(){
		cacheManager.cancelUpload(resource_id);
        saveMessage(getText("upload.cancelled"));
		return SUCCESS;
	}

	public String status(){
		super.prepare();
		status = cacheManager.getUploadStatus(resource_id);
		if (busy){
			return BUSY;
		}else{
			return READY;
		}
	}

	public String history(){
		uploadEvents = uploadEventManager.getUploadEventsByResource(resource_id);
		return SUCCESS;
	}

	
	
	
	public String getStatus() {
		return status;
	}

	public List<UploadEvent> getUploadEvents() {
		return uploadEvents;
	}

	public boolean isBusy() {
		return busy;
	}

	public String getGChartData() {
		return gChartData;
	}
	
}
