package org.gbif.provider.util;

import org.gbif.provider.model.Resource;

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
