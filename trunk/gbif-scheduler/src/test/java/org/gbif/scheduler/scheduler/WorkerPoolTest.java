package org.gbif.scheduler.scheduler;

import static org.junit.Assert.*;

import java.util.Date;

import org.appfuse.webapp.action.BaseActionTestCase;
import org.gbif.scheduler.mock.MockJob;
import org.gbif.scheduler.model.Job;
import org.junit.Test;

public class WorkerPoolTest extends WorkerPoolBaseTest{
	
	@Test
	public void testScheduleNextRepeatingJob() {
		Job job = new Job();
		job.setDescription("test job");
		job.setId(1L);
		job.setDataAsJSON("{}");
		job.setJobClassName(MockJob.class.getName());
		job.setJobGroup("testGroup");
		job.setNextFireTime(new Date());
		
		Job newJob = workerPool.getNextRepeatingJob(job);
		assertNull(newJob);
		// add repeating days to create a new job
		job.setRepeatInDays(42);
		newJob = workerPool.getNextRepeatingJob(job);
		assertNotSame(job, newJob);
	}


}
