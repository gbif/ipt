package org.gbif.provider.webapp.action;

import org.gbif.provider.job.RdbmsUploadJob;
import org.gbif.scheduler.service.JobManager;

public class JobStatusAction extends BaseOccurrenceResourceAction {
	private JobManager jobManager;
	private RdbmsUploadJob rdbmsUploadJob;
	private String status;
	
	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public void setRdbmsUploadJob(RdbmsUploadJob rdbmsUploadJob) {
		this.rdbmsUploadJob = rdbmsUploadJob;
	}
	
	public String getStatus() {
		return status;
	}

	//
	// actions
	//
	
	public String uploadStatus(){
		if (resource_id != null){
			status = rdbmsUploadJob.status(resource_id);
		}
		return SUCCESS;
	}
}
