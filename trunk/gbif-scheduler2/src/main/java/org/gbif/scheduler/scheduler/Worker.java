/**
 * 
 */
package org.gbif.scheduler.scheduler;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import org.gbif.scheduler.model.Job;
import org.gbif.util.JSONUtils;

/**
 * The object that comes from the pool, that will execute the contents of the trigger
 * @author timrobertson
 */
public class Worker {
	private static Log logger = LogFactory.getLog(Worker.class);
	private Job job;
	private ApplicationContext applicationContext;
	private ServletContext servletContext;
	
	public Worker(ApplicationContext applicationContext, ServletContext servletContext) {
		super();
		this.applicationContext=applicationContext;
		this.servletContext=servletContext;
	}

	public void execute(WorkerPool pool, String launchableClassname, String dataAsJSON) {
		Thread t = new Thread(new Runner(this, pool, launchableClassname, dataAsJSON));
		t.start();
	}
	
	class Runner implements Runnable {
		String launchableClassname;
		String dataAsJSON;
		WorkerPool pool;
		Worker worker;
		public Runner(Worker worker, WorkerPool pool, String launchableClassname, String dataAsJSON) {
			this.launchableClassname = launchableClassname;
			this.dataAsJSON = dataAsJSON;
			this.pool = pool;
			this.worker = worker;
		}
		public void run() {
			try {
				Map<String, Object> seed = JSONUtils.mapFromJSON(dataAsJSON); 
				try {
					// we can launch in 3 ways
					// 1 an activity that does not know about the spring application
					// 2,3 an activity that is in the spring application (autowired by type, or name)
					String[] beanNames;
					beanNames = worker.applicationContext.getBeanNamesForType(Class.forName(launchableClassname));
					logger.debug("Job beans found for the required class[" + launchableClassname + "] : "+beanNames.toString());
					Object target = null;
					if (beanNames.length==0) {
						logger.info("No beans are wired for the required class[" + launchableClassname + "], creating a new one");
						try {
							target = Class.forName(launchableClassname).newInstance();
						} catch (InstantiationException e) {
							logger.error("Unable to launch job: " + e.getMessage(), e);
							return;
							
						} catch (IllegalAccessException e) {
							logger.error("Unable to launch job: " + e.getMessage(), e);
							return;
							
						}
						
					} else if (beanNames.length==1) { // autowiring by type basically...
						logger.info("There is only 1 bean wired in the context for type [" + launchableClassname + "], so using this one");
						target = worker.applicationContext.getBean(beanNames[0]);
						
					} else {
						// use a disambiguation bean id
						logger.info("There are " + beanNames.length + " beans wired in the context for type [" + launchableClassname + "].  Using the APP:LAUNCHER:BEAN:ID from the seed to disambiguate");
						String beanId = seed.get("APP:LAUNCHER:BEAN:ID").toString();
						if (beanId == null) {
							logger.error("No APP:LAUNCHER:BEAN:ID found in the seed data.  Autowiring by type is ambiguous, stopping.");	
						} else {
							target = worker.applicationContext.getBean(beanId);
						}
					}
					
					if (target == null) {
						logger.error("Target object is null for: " + launchableClassname);
					} else if (target instanceof Launchable) {
						try {
							((Launchable)target).launch(seed);
						} catch (Exception e) {
							logger.error("Error executing: " + launchableClassname, e);
						}
						
					} else if (target instanceof Runnable) {
						((Runnable)target).run();
						
					} else {
						logger.error("The scheduled object [" + launchableClassname + "] does not implement Runnable, or Launchable!");
					}

					
				} catch (ClassNotFoundException e1) {
					logger.error("Worker is not able to launch class [" + launchableClassname + "] as it is not on the classpath");
					//e1.printStackTrace();
				}
				
				
			} finally {
				try {
					pool.returnObject(worker);
				} catch (Exception e) {
					logger.error("FATAL: no longer able to return to pool: " + e.getMessage(), e);
				}
			}
		}

	}

	/**
	 * Note: PACKAGE access
	 * @return the job
	 */
	Job getJob() {
		return job;
	}

	/**
	 * Note: PACKAGE access
	 * @param job the job to set
	 */
	void setJob(Job job) {
		this.job = job;
		addJobEnvironment();
	}
	/**
	 * Add environment parameters to job seed
	 * @param job
	 */
	private void addJobEnvironment() {
		Map<String, Object> seed = JSONUtils.mapFromJSON(job.getDataAsJSON());
		seed.put(Launchable.JOB_ID, job.getId().toString());
		String webappDir = "/tmp";
		if (this.servletContext != null){
			webappDir = this.servletContext.getRealPath("/");
		}
		seed.put(Launchable.WEBAPP_DIR, webappDir);
		job.setDataAsJSON(JSONUtils.jsonFromMap(seed));
	}

}