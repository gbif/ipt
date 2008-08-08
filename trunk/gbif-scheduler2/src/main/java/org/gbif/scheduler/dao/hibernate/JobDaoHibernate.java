/**
 * 
 */
package org.gbif.scheduler.dao.hibernate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.model.Job;

/**
 * @author timrobertson
 */
public class JobDaoHibernate extends GenericDaoHibernate<Job, Long> implements JobDao {
	public JobDaoHibernate() {
		super(Job.class);
	}

	/*
	 * This is overly complicated due to limitations with some (all maybe, but definitely mysql) RDBMS 
	 * such that they the running order of a group by and an order by don't work here
	 * 
	 * TODO - this is geared up only for single instance running right now.  We will need to do a 2 phase commit style
	 * in the future where we do an update, then a select to see what we managed to update for our instance, then 
	 * return those ones
	 * 
	 * @see org.gbif.bioindex.dao.JobTriggerDao#findByReadyToExecuteAndNotFired(int)
	 */
	@SuppressWarnings("unchecked")
	public List<Job> findByReadyToExecuteAndNotFired(final int maximumToReturn) {
		if (maximumToReturn<1) {
			return new LinkedList<Job>();
		}
		
		// get the groups running
		List<String> runningGroups = getHibernateTemplate().find("select distinct(runningGroup) from Job where runningGroup is not null and started is not null");
		final Set<String> groupsExecuting = new HashSet<String>();
		groupsExecuting.addAll(runningGroups);
		
		// find and return eligible jobs
		List<Job> eligibleJobs;
		if (groupsExecuting.size() == 0) {
			eligibleJobs =  (List<Job>) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {
						Query query = 
							session.createQuery("from Job where started is null and nextFireTime<=current_timestamp() order by nextFireTime")
								.setMaxResults(maximumToReturn);
						return query.list();
					}
				}
			);
		} else {
			eligibleJobs =  (List<Job>) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session) {
						Query query = 
							session.createQuery("from Job where started is null and nextFireTime<=current_timestamp() and runningGroup not in(:groups) order by nextFireTime")
								.setParameterList("groups", groupsExecuting)
								.setMaxResults(maximumToReturn);
						return query.list();
					}
				}
			);
		}
		
		// ensure that only 1 per group is returned
		Set<String> groupsReturning = new HashSet<String>();
		List<Job> jobs = new LinkedList<Job>();
		for (Job job : eligibleJobs) {
			if (!groupsReturning.contains(job.getRunningGroup())) {
				jobs.add(job);
				groupsReturning.add(job.getRunningGroup());
			}
		}
		return jobs;
	}
	
	/**
	 * @see org.gbif.scheduler.dao.JobDao#clearStartedJobs(java.lang.String)
	 */
	public void clearStartedJobs(String instanceId) {
		log.info("Clearing Jobs for instanceId: " + instanceId);
		getHibernateTemplate().bulkUpdate("update Job set started=null, instanceId=null where instanceId=?", instanceId);
	}

	/**
	 * @see org.gbif.scheduler.dao.JobDao#findByGroup(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Job> findByGroup(String jobGroup) {
		return (List<Job>) getHibernateTemplate().find("from Job where jobGroup = ?", jobGroup);
	}

	/**
	 * @see org.gbif.scheduler.dao.JobDao#findByRunning()
	 */
	@SuppressWarnings("unchecked")
	public List<Job> findByRunning() {
		return (List<Job>) getHibernateTemplate().find("from Job where started is not null");		
	}
}