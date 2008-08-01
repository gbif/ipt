package org.gbif.scheduler.scheduler;

import static org.junit.Assert.*;

import java.util.Date;

import org.appfuse.webapp.action.BaseActionTestCase;
import org.gbif.scheduler.mock.MockJob;
import org.gbif.scheduler.model.Job;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;

public class WorkerTest extends WorkerPoolBaseTest {


	@Test
	public void testRunWorker() throws Exception {
		Job job = new Job();
		job.setDescription("test job");
		job.setId(1L);
		job.setDataAsJSON("{}");
		job.setJobClassName(MockJob.class.getName());
		job.setJobGroup("testGroup");
		job.setNextFireTime(new Date());
		
		Worker worker = workerPool.borrowObject(job);
		String classToRun = job.getJobClassName();
		String dataAsJSON = job.getDataAsJSON();

		// reset MockJob.result
		MockJob.result="not run yet";
		assertFalse(MockJob.goodResult.equals(MockJob.result));
		try {
			worker.execute(workerPool, classToRun, dataAsJSON);
		} catch (ObjectRetrievalFailureException e) {
			// supposed to throw that as we dont use a job from the jobDao but created a new one
		}
		// assert that job really run after waiting for 5 seconds to give the job a fair chance to run as it is a different thread
		Thread.sleep(5000);
		assertEquals(MockJob.goodResult, MockJob.result);
	}



}
