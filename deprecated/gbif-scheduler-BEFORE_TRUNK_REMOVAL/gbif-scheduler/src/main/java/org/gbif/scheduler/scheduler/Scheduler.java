/**
 * 
 */
package org.gbif.scheduler.scheduler;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.model.Job;
import org.gbif.util.JSONUtils;

/**
 * @author timrobertson
 *
 */
public class Scheduler{
	// watch interval for state changes
	private long watchIntervalMsec=10000;
	private String instanceId="unknown";
	private static Log logger = LogFactory.getLog(Scheduler.class);
	private JobDao jobDao;
	private ApplicationContext applicationContext;
	private ServletContext servletContext;
	
	// the pool of actual worker
	WorkerPool pool;
	
	private boolean isRunning = true;

	public Scheduler(String instanceId, ApplicationContext applicationContext, ServletContext servletContext, JobDao jobDao) {
		super();
		this.instanceId = instanceId;
		this.jobDao = jobDao;
		this.applicationContext=applicationContext;
		this.servletContext=servletContext;
	}
	
	
	// starts the scheduler, ready to work
	public void start() {
		pool = new WorkerPool(jobDao, instanceId, applicationContext, servletContext);
		
		// start the watcher that will issue the jobs
		Thread workerLauncher = new Thread(new WorkerLauncher());
		jobDao.clearStartedJobs(instanceId);
		workerLauncher.start();
	}
	
	/**
	 * The class that actually launches and runs jobs
	 * @author timrobertson
	 */
	class WorkerLauncher implements Runnable {
		public void run() {
			while (true) {
				if (isRunning) {
					logger.debug("Starting job issuing");
					
					// no point looking for work if we can see the pool is exhausted anyway!
					if (pool.getNumActive()>=pool.getMaxActive()) {
						logger.info("Pool exhausted, so not looking for any jobs - will try later");
					} else {
					
						// let's see if there is anything needing doing now
						List<Job> jobs = jobDao.findByReadyToExecuteAndNotFired(pool.getMaxActive());
						if (jobs.size() == 0) {
							logger.debug("There are no jobs ready to execute");
						} else {
							logger.info("There are " + jobs.size() + " jobs ready to execute");
						}
						
						for (Job job : jobs) {
							// so we have a job, lets try and see if the pool can give us a worker
							Worker worker = null;
							try {
								worker = (Worker) pool.borrowObject(job);
								String classToRun = job.getJobClassName();
								String dataAsJSON = job.getDataAsJSON();
								try {
		
									// this will return to the pool when it is ready to do so...
									// if it didnt we'd be in trouble!
									worker.execute(pool, classToRun, dataAsJSON);
									
									
								} catch (Exception e) { // if any of the above fails, then we NEED to return the object
									try {
										pool.returnObject(worker);
									} catch (Exception e1) {
										logger.error("FATAL: no longer able to return to pool: " + e.getMessage(), e);
									}
								}
								
							} catch (Exception e) {
								// pool is exhausted, so break and try again later!
								logger.info("Pool is exhausted, will try later");
								break;
							}
						}
					}
				} else {
					logger.info("Scheduler state is not running, no jobs will be issued");
				}
				try {
					Thread.sleep(watchIntervalMsec);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
