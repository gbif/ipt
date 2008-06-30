/**
 * 
 */
package org.gbif.scheduler.scheduler;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.ibiodiversity.harvest.dao.JobDao;
import com.ibiodiversity.harvest.model.Job;

/**
 * A pool that will return a Worker, and on borrowing an object, will set the 
 * started and instance id and similarly remove the Job when returning
 * @author timrobertson
 */
public class WorkerPool extends GenericObjectPool {
	protected Log logger = LogFactory.getLog(this.getClass());
	protected JobDao jobDao;
	protected String instanceId;
	// TODO, a UI driven watcher
	public static final int poolSize = 10;
	
	public WorkerPool(JobDao jobDao,
			String instanceId) {
		super();
		this.jobDao = jobDao;
		this.instanceId = instanceId;
		this.setFactory(new WorkerFactory());
		setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
		setMaxActive(poolSize);
	}

	/**
	 * @see org.apache.commons.pool.impl.GenericObjectPool#borrowObject()
	 */
	@Override
	public synchronized Object borrowObject() throws Exception {
		throw new Exception("Invalid invocation - use borrowObject(jobDao, instanceId)");
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
			logger.info("Returning worker, so removing Job");
			// a failure here means corrupted and pretty fatal so just pass to application
			jobDao.remove(job.getId());
			super.returnObject(obj);
		} else {
			throw new Exception("Error: trying to return a non Worker to the Worker pool: " + obj.getClass());
		}
	}
	
	
	/**
	 * Creates the worker objects for the pool
	 * @author timrobertson
	 */
	class WorkerFactory implements PoolableObjectFactory {
		public Object makeObject() throws Exception {
			return new Worker();
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
