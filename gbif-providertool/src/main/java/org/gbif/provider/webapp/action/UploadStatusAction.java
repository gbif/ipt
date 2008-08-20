package org.gbif.provider.webapp.action;

import org.gbif.provider.service.CacheManager;

public class UploadStatusAction extends BaseOccurrenceResourceAction {
	private CacheManager cacheManager;
	private String status;
	
	public String uploadStatus(){
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
}
