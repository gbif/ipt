package org.gbif.provider.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.SourceColumn;
import org.gbif.provider.model.TermMapping;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.TermMappingManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class TermMappingManagerTest extends ResourceTestBase {
	@Autowired 
	private SourceInspectionManager sourceInspectionManager;
	@Autowired 
	private ExtensionManager extensionManager;
	@Autowired
	private TermMappingManager termMappingManager;

	@Test
	@Transactional(propagation=Propagation.REQUIRED)
	public void testDistinctTerms(){
		setup();
		ExtensionProperty prop = extensionManager.getProperty(Constants.SCIENTIFIC_NAME_QUALNAME);
		try {
			Set<String> terms = sourceInspectionManager.getDistinctValues(resource.getCoreMapping().getSource(), resource.getCoreMapping().getMappedProperty(prop).getColumn());
			assertTrue(terms.size()>500);
//			List<String> t2 = new ArrayList<String>(terms);
//			Collections.sort(t2);
//			System.out.println(t2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTermMappings(){
		SourceColumn sc = new SourceColumn();
		sc.setColumnName("ScientificName");
		List<TermMapping> terms = termMappingManager.getTermMappings(1l, sc);
	}	
	
	@Test
	public void testGetMappingMap(){
		SourceColumn sc = new SourceColumn();
		sc.setColumnName("ScientificName");
		Map<String, String> terms = termMappingManager.getMappingMap(1l, sc);
//		System.out.println(terms);
	}	
	
}
