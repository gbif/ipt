package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Worker;
import org.gbif.scheduler.scheduler.WorkerPool;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;

public class WorkerTest extends BaseDaoTestCase {
	private WorkerPool workerPool;
	
	public void setWorkerPool(WorkerPool workerPool) {
		this.workerPool = workerPool;
	}

	
	@Test
	public void testGetBeanNamesByType() throws ClassNotFoundException {
		String jobClassName = RdbmsUploadJob.class.getName();
		String[] beanNames = applicationContext.getBeanNamesForType(Class.forName(jobClassName));
		String[] expectedBeanNames = new String[1];
		expectedBeanNames[0] = "rdbmsUploadJob";
		assertArrayEquals(expectedBeanNames, beanNames);
	}
	
	/**
	 * Supposed to throw ObjectRetrievalFailureException as we dont use a job from the jobDao but created a new one 
	 * and the pool doesnt find it in the db
	 * @throws ObjectRetrievalFailureException
	 */
	@Test
	public void testRunWorker() throws ObjectRetrievalFailureException {
		Job job = new Job();
		job.setDescription("test job");
		job.setId(1L);
		job.setDataAsJSON("{}");
		job.setJobClassName(RdbmsUploadJob.class.getName());
		job.setJobGroup("testGroup");
		job.setNextFireTime(new Date());
		
		String classToRun = job.getJobClassName();
		String dataAsJSON = job.getDataAsJSON();

		Worker worker;
		try {
			worker = workerPool.borrowObject(job);
			worker.setApplicationContext(applicationContext);
			worker.execute(workerPool, classToRun, dataAsJSON);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
