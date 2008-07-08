package org.gbif.logging.webapp.action;

import org.apache.struts2.ServletActionContext;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import org.gbif.logging.model.LogEvent;
import org.gbif.logging.service.LogEventManager;
import org.gbif.logging.webapp.action.LogEventAction;

import com.opensymphony.xwork2.ActionSupport;

public class LogEventActionTest extends BaseActionTestCase {
    private LogEventAction action;

    @Override @SuppressWarnings("unchecked")
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        action = new LogEventAction();
        LogEventManager logEventManager = (LogEventManager) applicationContext.getBean("logEventManager");
        action.setLogEventManager(logEventManager);
    
        // add a test logEvent to the database
        LogEvent logEvent = new LogEvent();

        // enter all required fields
        logEvent.setLevel(681671571);

        logEventManager.save(logEvent);
    }

    public void testSearch() throws Exception {
        assertEquals(action.list(), ActionSupport.SUCCESS);
        assertTrue(action.getLogEvents().size() >= 1);
    }

    public void testEdit() throws Exception {
        log.debug("testing edit...");
        action.setId(-1L);
        assertNull(action.getLogEvent());
        assertEquals("success", action.edit());
        assertNotNull(action.getLogEvent());
        assertFalse(action.hasActionErrors());
    }

    public void testSave() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setId(-1L);
        assertEquals("success", action.edit());
        assertNotNull(action.getLogEvent());

        LogEvent logEvent = action.getLogEvent();
        // update required fields
        logEvent.setLevel(230392599);

        action.setLogEvent(logEvent);

        assertEquals("input", action.save());
        assertFalse(action.hasActionErrors());
        assertFalse(action.hasFieldErrors());
        assertNotNull(request.getSession().getAttribute("messages"));
    }

    public void testRemove() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setDelete("");
        LogEvent logEvent = new LogEvent();
        logEvent.setId(-2L);
        action.setLogEvent(logEvent);
        assertEquals("success", action.delete());
        assertNotNull(request.getSession().getAttribute("messages"));
    }
}