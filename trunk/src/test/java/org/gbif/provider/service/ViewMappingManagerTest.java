package org.gbif.provider.service;

import java.util.List;

import org.appfuse.service.BaseManagerTestCase;
import org.appfuse.service.impl.BaseManagerMockTestCase;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.impl.ViewMappingManagerImpl;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.AssertThrows;


public class ViewMappingManagerTest extends BaseManagerTestCase {
    private ViewMappingManager viewMappingManager;
    
    public void setViewMappingManager(ViewMappingManager viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}


	@Test
    public void testSaveMapping() {
        ViewMapping testData = new ViewMapping();
        testData.setViewSql("Select * from specimen");
        try{
            testData = viewMappingManager.save(testData);
        }catch(DataIntegrityViolationException e){
        	//ok
        	int a = 0;
        	
        }
    }

	//@Test
    public void testFindByResource() {
        //List<ViewMapping> result = viewMappingManager.findByResource(1L);
        //System.out.println(result);
        //TODO: insert default test unitdb data
        //assertFalse(result.isEmpty());        
    }
}
