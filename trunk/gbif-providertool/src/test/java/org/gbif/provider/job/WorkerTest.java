package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.gbif.scheduler.MockJob;
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
		String jobClassName = OccDbUploadJob.class.getName();
		String[] beanNames = applicationContext.getBeanNamesForType(Class.forName(jobClassName));
		String[] expectedBeanNames = new String[1];
		expectedBeanNames[0] = "occDbUploadJob";
		assertArrayEquals(expectedBeanNames, beanNames);
	}
	
	/**
	 * Supposed to throw ObjectRetrievalFailureException as we dont use a job from the jobDao but created a new one 
	 * and the pool doesnt find it in the db
	 * @throws Exception 
	 */
	@Test
	public void testMockJob() throws Exception {
		// test occDbUploadJob
		Job job = new Job();
		job.setDescription("test mock job");
		job.setId(1L);
		job.setDataAsJSON("{}");
		job.setJobClassName(MockJob.class.getName());
		job.setJobGroup("testGroup");
		job.setNextFireTime(new Date());
		// reset MockJob.result
		MockJob.result="not run yet";
		assertFalse(MockJob.goodResult.equals(MockJob.result));
		runJob(job);
		// assert that job really run after waiting for 5 seconds to give the job a fair chance to run as it is a different thread
		Thread.sleep(5000);
		assertEquals(MockJob.goodResult, MockJob.result);
	}
	
	@Test
	public void testOccDbUploadJob() throws Exception {
		// test occDbUploadJob
		Job job = new Job();
		job.setDescription("test rdbms upload job");
		job.setId(1L);
		job.setDataAsJSON("{}");
		job.setJobClassName(OccDbUploadJob.class.getName());
		job.setJobGroup("testGroup");
		job.setNextFireTime(new Date());
		runJob(job);
	}

	private void runJob(Job job) throws Exception{
		String classToRun = job.getJobClassName();
		String dataAsJSON = job.getDataAsJSON();

		Worker worker = workerPool.borrowObject(job);
		worker.setApplicationContext(applicationContext);
		try {
			worker.execute(workerPool, classToRun, dataAsJSON);
		} catch (ObjectRetrievalFailureException e) {
			// supposed to throw that as we dont use a job from the jobDao but created a new one
		}
	}

}
