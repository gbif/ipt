package org.gbif.provider.webapp.action;

import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.scheduler.service.JobManager;

public class JobStatusAction extends BaseOccurrenceResourceAction {
	private JobManager jobManager;
	private OccDbUploadJob occDbUploadJob;
	private String status;
	
	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}
	
	public void setOccDbUploadJob(OccDbUploadJob occDbUploadJob) {
		this.occDbUploadJob = occDbUploadJob;
	}


	public String getStatus() {
		return status;
	}

	//
	// actions
	//
	
	public String uploadStatus(){
		if (resource_id != null){
			status = occDbUploadJob.status(resource_id);
		}
		return SUCCESS;
	}
}
