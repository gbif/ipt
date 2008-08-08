/**
 * 
 */
package org.gbif.scheduler.dao;

import java.util.List;


import org.gbif.scheduler.model.Job;

/**
 * @author timrobertson
 */
public interface JobDao extends GenericDao<Job, Long> {
	/**
	 * @return Jobs that are ready to fire and are not fired
	 */
	public List<Job> findByReadyToExecuteAndNotFired(int maximumToReturn);
	
	/**
	 * Typically used at instance startup to ensure that any hanging jobs are restarted
	 * @param instanceId To clear all jobs for
	 */
	public void clearStartedJobs(String instanceId);
	
	/**
	 * @param runningGroup To search on
	 * @return The jobs in the group
	 */
	public List<Job> findByGroup(String runningGroup);
	
	/**
	 * @return The jobs running
	 */
	public List<Job> findByRunning();
}
