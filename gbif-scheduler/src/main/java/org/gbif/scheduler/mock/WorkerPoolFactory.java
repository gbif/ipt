package org.gbif.scheduler.mock;

import javax.servlet.ServletContext;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.scheduler.WorkerPool;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

public class WorkerPoolFactory implements ServletContextAware, ApplicationContextAware{
	private ApplicationContext applicationContext;
	private ServletContext servletContext;
	private JobDao jobDao;

	public WorkerPoolFactory(JobDao jobDao) {
		this.jobDao=jobDao;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public WorkerPool newWorkerPool(String instanceId){
		return new WorkerPool(jobDao, instanceId, applicationContext, servletContext);
	}
}
