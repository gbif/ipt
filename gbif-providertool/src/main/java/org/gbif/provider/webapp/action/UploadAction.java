package org.gbif.provider.webapp.action;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.CacheManager;

public class UploadAction extends BaseOccurrenceResourceAction {
	private CacheManager cacheManager;
	private String status;
	private boolean ajax = false;
	private OccurrenceResource occResource;
	
	public String upload(){
        saveMessage(getText("UPLOAD"));
		if (resource_id != null){
			// run task in different thread
			cacheManager.runUpload(resource_id, getCurrentUser().getId());			
	        saveMessage(getText("upload.addedJob"));
		}
		return SUCCESS;
	}
	public String cancel(){
        saveMessage(getText("CANCEL"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
		}
		return SUCCESS;
	}
	public String process(){
        saveMessage(getText("PROCESS"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
		}
		return SUCCESS;
	}
	public String status(){
        saveMessage(getText("STATUS"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
		}
		if (ajax){
			return "ajax";
		}else{
			occResource = occResourceManager.get(resource_id);
			return SUCCESS;
		}
	}
	public String clear(){
        saveMessage(getText("CLEAR"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
		}
		return SUCCESS;
	}

	public String history(){
        saveMessage(getText("HISTORY"));
		if (resource_id != null){
			status = cacheManager.getUploadStatus(resource_id);
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
	
}
