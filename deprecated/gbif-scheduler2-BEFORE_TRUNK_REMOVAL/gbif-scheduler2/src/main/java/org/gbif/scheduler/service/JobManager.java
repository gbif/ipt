/**
 * 
 */
package org.gbif.scheduler.service;

import java.util.List;

import org.gbif.scheduler.model.Job;

/**
 * @author timrobertson
 */
public interface JobManager extends GenericManager<Job, Long> {
	/**
	 * @return Jobs that are started limited to 1000 jobs
	 */
	public List<Job> getRunningJobs();
	
	/**
	 * @return All jobs with running first, and then the next in line to run
	 */
	public List<Job> getAllJobs();
	
	
	/**
	 * @return All jobs within the running group ordered by fire time
	 */
	public List<Job> getJobsInGroup(String runningGroup);
}
