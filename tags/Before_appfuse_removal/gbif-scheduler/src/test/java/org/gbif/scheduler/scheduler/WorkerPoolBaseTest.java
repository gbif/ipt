package org.gbif.scheduler.scheduler;

import org.appfuse.webapp.action.BaseActionTestCase;
import org.gbif.scheduler.mock.WorkerPoolFactory;
import org.gbif.scheduler.scheduler.WorkerPool;
import org.junit.Before;
import org.junit.Test;

public class WorkerPoolBaseTest extends BaseActionTestCase {

	protected WorkerPoolFactory workerPoolFactory;
	public WorkerPool workerPool;

	public void setWorkerPoolFactory(WorkerPoolFactory workerPoolFactory) {
		this.workerPoolFactory = workerPoolFactory;
	}

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		workerPool = workerPoolFactory.newWorkerPool("TestInstance1");
	}

	@Test
	public void testNothing(){
		assertTrue(true);
	}
}
