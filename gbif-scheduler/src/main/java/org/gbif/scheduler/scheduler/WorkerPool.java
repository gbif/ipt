/**
 * 
 */
package org.gbif.scheduler.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.model.Job;
import org.gbif.util.JSONUtils;
import org.springframework.context.ApplicationContext;

/**
 * A pool that will return a Worker, and on borrowing an object, will set the 
 * started and instance id and similarly remove the Job when returning
 * @author timrobertson
 */
public class WorkerPool extends GenericObjectPool {
	protected static Log logger = LogFactory.getLog(WorkerPool.class);
	protected JobDao jobDao;
	protected String instanceId;
	// TODO, a UI driven watcher
	public static final int poolSize = 10;
	
	public WorkerPool(JobDao jobDao, String instanceId, ApplicationContext applicationContext, ServletContext servletContext) {
		super();
		this.jobDao = jobDao;
		this.instanceId = instanceId;
		this.setFactory(new WorkerFactory(applicationContext, servletContext));
		setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
		setMaxActive(poolSize);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericObjectPool#borrowObject()
	 */
	@Override
	public synchronized Object borrowObject() throws Exception {
		throw new Exception("Invalid invocation - use borrowObject(Job job)");
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericObjectPool#borrowObject()
	 */
	public synchronized Worker borrowObject(Job job) throws Exception {
		Worker worker = (Worker) super.borrowObject();
		logger.info("Borrowing worker, so setting parameters on Job");
		// set that it is being worked upon
		job.setStarted(new Date());
		job.setInstanceId(instanceId);
		jobDao.save(job);
		worker.setJob(job);
		return worker;
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericObjectPool#returnObject(java.lang.Object)
	 */
	@Override
	public synchronized void returnObject(Object obj) throws Exception {
		if (obj instanceof Worker) {
			Worker worker = (Worker) obj;
			Job job = worker.getJob();
			// for repeatable jobs schedule the next run
			Job newJob = getNextRepeatingJob(job);
			if (newJob != null){
				logger.info("Job is repeating every "+job.getRepeatInDays()+" days. Schedule next job at "+newJob.getNextFireTime());
				jobDao.save(newJob);
			}
			logger.info("Returning worker, so removing Job");
			// a failure here means corrupted and pretty fatal so just pass to application
			jobDao.remove(job.getId());
			super.returnObject(obj);
		} else {
			throw new Exception("Error: trying to return a non Worker to the Worker pool: " + obj.getClass());
		}
	}
	
	protected static Job getNextRepeatingJob(Job job){
		Job nextJob = null;
		if (job.getRepeatInDays()>0){
			nextJob = new Job();
			nextJob.setCreated(new Date());
			nextJob.setDataAsJSON(job.getDataAsJSON());
			nextJob.setDescription(job.getDescription());
			nextJob.setInstanceId(job.getInstanceId());
			nextJob.setJobClassName(job.getJobClassName());
			nextJob.setJobGroup(job.getJobGroup());
			nextJob.setName(job.getName());
			nextJob.setRepeatInDays(job.getRepeatInDays());
			nextJob.setRunningGroup(job.getRunningGroup());
			// calculate next fire time based on last schedule + weeks to be repeated in
			Calendar cal = Calendar.getInstance();
			cal.setTime(job.getNextFireTime());
			cal.add(Calendar.DATE, job.getRepeatInDays());			
			nextJob.setNextFireTime(cal.getTime());
		}
		return nextJob; 
	}
	
	/**
	 * Creates the worker objects for the pool
	 * @author timrobertson
	 */
	class WorkerFactory implements PoolableObjectFactory {
		private ApplicationContext applicationContext;
		private ServletContext servletContext;

		private WorkerFactory(ApplicationContext applicationContext, ServletContext servletContext) {
			super();
			this.applicationContext = applicationContext;
			this.servletContext = servletContext;
		}

		public Object makeObject() throws Exception {
			return new Worker(applicationContext, servletContext);
		}
		
		// required methods
		public void destroyObject(Object arg0) throws Exception {
		}
		public boolean validateObject(Object arg0) {
			return true;
		}
		public void activateObject(Object arg0) throws Exception {}
		public void passivateObject(Object arg0) throws Exception {}		
	}	
}
