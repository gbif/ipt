/**
 * 
 */
package org.gbif.scheduler.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.appfuse.service.impl.GenericManagerImpl;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;

/**
 * @author timrobertson
 */
public class JobManagerImpl extends GenericManagerImpl<Job, Long> implements
		JobManager {
	private JobDao jobDao;

	public JobManagerImpl(JobDao jobDao) {
		super(jobDao);
		this.jobDao = jobDao;
	}

	@Override
	public List<Job> getAll() {
		return getAllJobs();
	}

	public List<Job> getAllJobs() {
		List<Job> results = jobDao.getAll();
		Collections.sort(results, new JobComparer());
		return results;
	}

	public List<Job> getJobsInGroup(String runningGroup) {
		List<Job> results = jobDao.findByGroup(runningGroup);
		Collections.sort(results, new JobComparer());
		return results;
	}

	public List<Job> getRunningJobs() {
		List<Job> results = jobDao.findByRunning();
		Collections.sort(results, new JobComparer());
		return results;
	}
	
	/**
	 * Puts the running ones first, then in order of timestamp
	 * @author timrobertson
	 */
	class JobComparer implements Comparator<Job> {
		public int compare(Job j1, Job j2) {
			if (j1.getStarted() != null) {
				if (j2.getStarted() == null) {
					return -1;
				} else {
					return j1.getStarted().compareTo(j2.getStarted());
				}
			} else {
				if (j2.getStarted() != null) {
					return 1;
				} else {
					return j1.getNextFireTime().compareTo(j2.getNextFireTime());
				}
			}
		}
	}
}
