package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.ResourceMetadata;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

public class ResourceMetadataActionTest extends BaseActionTestCase {
    private ResourceMetadataAction action;

    @Override @SuppressWarnings("unchecked")
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        action = new ResourceMetadataAction();
        GenericManager resourceMetadataManager = (GenericManager) applicationContext.getBean("resourceMetadataManager");
        action.setResourceMetadataManager(resourceMetadataManager);
    
        // add a test resourceMetadata to the database
        ResourceMetadata resourceMetadata = new ResourceMetadata();

        // enter all required fields

        resourceMetadataManager.save(resourceMetadata);
    }

    public void testSearch() throws Exception {
        assertEquals(action.list(), ActionSupport.SUCCESS);
        assertTrue(action.getResourceMetadatas().size() >= 1);
    }

    public void testEdit() throws Exception {
        log.debug("testing edit...");
        action.setId(-1L);
        assertNull(action.getResourceMetadata());
        assertEquals("success", action.edit());
        assertNotNull(action.getResourceMetadata());
        assertFalse(action.hasActionErrors());
    }

    public void testSave() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setId(-1L);
        assertEquals("success", action.edit());
        assertNotNull(action.getResourceMetadata());

        ResourceMetadata resourceMetadata = action.getResourceMetadata();
        // update required fields

        action.setResourceMetadata(resourceMetadata);

        assertEquals("input", action.save());
        assertFalse(action.hasActionErrors());
        assertFalse(action.hasFieldErrors());
        assertNotNull(request.getSession().getAttribute("messages"));
    }

    public void testRemove() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setDelete("");
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setId(-2L);
        action.setResourceMetadata(resourceMetadata);
        assertEquals("success", action.delete());
        assertNotNull(request.getSession().getAttribute("messages"));
    }
}