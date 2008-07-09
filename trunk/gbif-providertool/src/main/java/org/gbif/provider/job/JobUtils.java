package org.gbif.provider.job;

import java.util.HashMap;
import java.util.Map;

import org.gbif.provider.model.Resource;
import org.gbif.scheduler.model.Job;
import org.gbif.util.JSONUtils;

public class JobUtils {
	public static String getJobGroup(Resource resource){
		return "resource["+resource.getId()+"]";
	}
	public static String getJobGroup(Long resourceId){
		return "resource["+resourceId+"]";
	}
	public static Long getResourceId(String jobGroup){
		return Long.valueOf(jobGroup.substring(9, jobGroup.length()-1));
	}
	public static Job getUploadJob(Resource resource){
		// create job data
		Map<String, Object> seed = new HashMap<String, Object>();
		seed.put("resourceId", resource.getId());
		// create upload job
		Job job = new Job();
		job.setJobClassName(RdbmsUploadJob.class.getCanonicalName());
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
		job.setJobGroup(JobUtils.getJobGroup(resource));
		job.setName("RDBMS data upload");
		job.setDescription("Data upload from RDBMS to resource "+resource.getTitle());
		return job;				
	}
}
