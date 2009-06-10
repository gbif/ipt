package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.service.CacheManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ImportAction extends BaseDataResourceAction implements Preparable{
	private static final String BUSY = "resource-busy";
	private static final String READY = "resource-ready";
	@Autowired
	private CacheManager cacheManager;
	private String status;
	private boolean busy = false;
	
	
	public void prepare(){
		if (resource_id != null){
			busy=cacheManager.isBusy(resource_id);
		}		
	}

	public String execute() {
		// load resource
		super.prepare();
		if (resource==null){
			return RESOURCE404;
		}
		// create GoogleChart string
		return SUCCESS;
	}		

	public String upload(){
		// run task in different thread
		cacheManager.runUpload(resource_id);			
		return SUCCESS;
	}
	
	public String status(){
		super.prepare();
		if (resource==null){
			return RESOURCE404;
		}
		status = cacheManager.getUploadStatus(resource_id);
		if (busy){
			return BUSY;
		}else{
			return READY;
		}
	}
	
	public String getStatus() {
		return status;
	}
	public boolean isBusy() {
		return busy;
	}
	
}
