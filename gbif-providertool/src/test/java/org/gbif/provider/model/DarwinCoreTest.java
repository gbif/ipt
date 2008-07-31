package org.gbif.provider.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.Constants;
import org.junit.Test;

public class DarwinCoreTest {
	
	@Test
	public void testDarwinCore() {
		DarwinCore dwc = DarwinCore.newInstance();
		dwc.setCatalogNumber("befhjsa6788-x");
		dwc.setScientificName("Abies alba");
		dwc.setBasisOfRecord("specimen");
		System.out.println(dwc);
		assertTrue(dwc.hashCode() > 0);
		assertTrue(dwc.toString().length() > 0);
		assertTrue(dwc.equals(dwc));
		assertFalse(dwc.equals(null));

		DarwinCore dwc2 = DarwinCore.newInstance();
		dwc2.setCatalogNumber("befhjsa6788-x");
		dwc2.setScientificName("Abies alba");
		dwc2.setBasisOfRecord("specimen");
		assertTrue(dwc.equals(dwc2));
		assertTrue(dwc.hashCode() == dwc2.hashCode());
		System.out.println(dwc2);
		
		dwc2.setInstitutionCode("RBGK");
		assertFalse(dwc.hashCode() == dwc2.hashCode());
		assertFalse(dwc.equals(dwc2));

	}

}
