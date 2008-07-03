package org.gbif.scheduler.webapp.action;

import org.apache.struts2.ServletActionContext;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;
import com.opensymphony.xwork2.ActionSupport;

public class JobActionTest extends BaseActionTestCase {
    private JobAction action;

    @Override @SuppressWarnings("unchecked")
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        action = new JobAction();
        JobManager jobManager = (JobManager) applicationContext.getBean("jobManager");
        action.setJobManager(jobManager);
    
        // add a test job to the database
        Job job = new Job();

        // enter all required fields

        jobManager.save(job);
    }

    public void testSearch() throws Exception {
        assertEquals(action.list(), ActionSupport.SUCCESS);
        assertTrue(action.getJobs().size() >= 1);
    }

    public void testEdit() throws Exception {
        log.debug("testing edit...");
        action.setId(-1L);
        assertNull(action.getJob());
        assertEquals("success", action.edit());
        assertNotNull(action.getJob());
        assertFalse(action.hasActionErrors());
    }

    public void testSave() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setId(-1L);
        assertEquals("success", action.edit());
        assertNotNull(action.getJob());

        Job job = action.getJob();
        // update required fields

        action.setJob(job);

        assertEquals("input", action.save());
        assertFalse(action.hasActionErrors());
        assertFalse(action.hasFieldErrors());
        assertNotNull(request.getSession().getAttribute("messages"));
    }

    public void testRemove() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setDelete("");
        Job job = new Job();
        job.setId(-2L);
        action.setJob(job);
        assertEquals("success", action.delete());
        assertNotNull(request.getSession().getAttribute("messages"));
    }
}