package org.gbif.provider.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;

public class DarwinCoreTest extends ResourceTestBase{
	
	@Test
	public void testDarwinCore() {
		OccurrenceResource r = getResourceMock();
		
		DarwinCore dwc = DarwinCore.newInstance(r);
		dwc.setCatalogNumber("befhjsa6788-x");
		dwc.setScientificName("Abies alba");
		dwc.setBasisOfRecord("specimen");
//		System.out.println(dwc);
		assertTrue(dwc.hashCode() > 0);
		assertTrue(dwc.toString().length() > 0);
		assertTrue(dwc.equals(dwc));
		assertFalse(dwc.equals(null));

		DarwinCore dwc2 = DarwinCore.newInstance(r);
		dwc2.setCatalogNumber("befhjsa6788-x");
		dwc2.setScientificName("Abies alba");
		dwc2.setBasisOfRecord("specimen");
		assertTrue(dwc.equals(dwc2));
		assertTrue(dwc.hashCode() == dwc2.hashCode());
//		System.out.println(dwc2);
		
		dwc2.setInstitutionCode("RBGK");
		assertFalse(dwc.hashCode() == dwc2.hashCode());
		assertFalse(dwc.equals(dwc2));

	}

}
