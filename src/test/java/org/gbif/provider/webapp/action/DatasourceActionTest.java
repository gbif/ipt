package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.Datasource;
import org.appfuse.webapp.action.BaseActionTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

public class DatasourceActionTest extends BaseActionTestCase {
    private DatasourceAction action;

    @Override @SuppressWarnings("unchecked")
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        action = new DatasourceAction();
        GenericManager datasourceManager = (GenericManager) applicationContext.getBean("datasourceManager");
        action.setDatasourceManager(datasourceManager);
        GenericManager resourceMetadataManager = (GenericManager) applicationContext.getBean("resourceMetadataManager");
        action.setResourceMetadataManager(resourceMetadataManager);
    
        // add a test datasource to the database
        Datasource datasource = new Datasource();

        // enter all required fields
        datasourceManager.save(datasource);
    }

    public void testSearch() throws Exception {
        assertEquals(action.list(), ActionSupport.SUCCESS);
        assertTrue(action.getDatasources().size() >= 1);
    }

    public void testEdit() throws Exception {
        log.debug("testing edit...");
        action.setId(-1L);
        assertNull(action.getDatasource());
        assertEquals("success", action.edit());
        assertNotNull(action.getDatasource());
        assertFalse(action.hasActionErrors());
    }

    public void testSave() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setId(-1L);
        assertEquals("success", action.edit());
        assertNotNull(action.getDatasource());

        Datasource datasource = action.getDatasource();
        // update required fields

        action.setDatasource(datasource);

        assertEquals("input", action.save());
        assertFalse(action.hasActionErrors());
        assertFalse(action.hasFieldErrors());
        assertNotNull(request.getSession().getAttribute("messages"));
    }

    public void testRemove() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        action.setDelete("");
        Datasource datasource = new Datasource();
        datasource.setId(-2L);
        action.setDatasource(datasource);
        assertEquals("success", action.delete());
        assertNotNull(request.getSession().getAttribute("messages"));
    }
}