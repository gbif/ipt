package org.gbif.provider.webapp.action;

import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.UploadEventManager;

import com.opensymphony.xwork2.Preparable;

public class UploadAction extends BaseOccurrenceResourceAction implements Preparable{
	private CacheManager cacheManager;
	private UploadEventManager uploadEventManager; 
	private String status;
	private boolean busy = false;
	private boolean ajax = false;
	private OccurrenceResource occResource;
	private List<UploadEvent> uploadEvents;
	
	public void prepare() throws Exception {
		busy=false;
		if (resource_id != null){
			if (cacheManager.currentUploads().contains(resource_id)){
				busy=true;
			}
		}
	}

	public String upload(){
		if (resource_id != null){
			// run task in different thread
			cacheManager.runUpload(resource_id, getCurrentUser().getId());			
	        saveMessage(getText("upload.added"));
		}
		return SUCCESS;
	}
	public String cancel(){
		if (resource_id != null){
			cacheManager.cancelUpload(resource_id);
	        saveMessage(getText("upload.cancelled"));
		}
		return SUCCESS;
	}
	public String process(){
        saveMessage(getText("upload.processed"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
		}
		return SUCCESS;
	}
	public String status(){
		if (ajax){
			if (resource_id != null){
				status = cacheManager.getUploadStatus(resource_id);
			}
			return "ajax";
		}
		if (!busy){
			return "ready";
		}
		if (resource_id != null){
			occResource = occResourceManager.get(resource_id);
		}
		return SUCCESS;
	}
	public String clear(){
        saveMessage(getText("upload.cleared"));
		if (resource_id != null){
			cacheManager.clearCache(resource_id);
		}
		return SUCCESS;
	}

	public String history(){
		if (resource_id != null){
			uploadEvents = uploadEventManager.getUploadEventsByResource(resource_id);
		}
		return SUCCESS;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public String getStatus() {
		return status;
	}
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}
	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public List<UploadEvent> getUploadEvents() {
		return uploadEvents;
	}

	public void setUploadEventManager(UploadEventManager uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public boolean isBusy() {
		return busy;
	}
	
}
