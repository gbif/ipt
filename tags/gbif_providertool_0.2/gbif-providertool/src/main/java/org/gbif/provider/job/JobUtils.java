package org.gbif.provider.job;

import java.util.HashMap;
import java.util.Map;

import org.appfuse.model.User;
import org.gbif.provider.model.Resource;
import org.gbif.scheduler.model.Job;
import org.gbif.util.JSONUtils;

public class JobUtils {

	// resource id
	public static String getJobGroup(Resource resource){
		return getJobGroup(resource.getId());
	}
	public static String getJobGroup(Long resourceId){
		return "resource["+resourceId+"]";
	}
	public static Long getResourceId(String jobGroup){
		return Long.valueOf(jobGroup.substring(9, jobGroup.length()-1));
	}

}
