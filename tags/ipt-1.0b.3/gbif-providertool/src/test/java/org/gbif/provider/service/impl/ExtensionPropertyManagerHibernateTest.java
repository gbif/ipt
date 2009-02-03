package org.gbif.provider.service.impl;


import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.tapir.filter.Filter;
import org.gbif.provider.tapir.filter.Like;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ExtensionPropertyManagerHibernateTest extends ContextAwareTestBase{
	@Autowired
	private ExtensionPropertyManager extensionPropertyManager;

	@Test
	public void testPropertyReplace() throws Exception {
		Filter f = new Filter();
		Like like = new Like();
		like.setProperty("http://rs.tdwg.org/dwc/dwcore/ScientificName");
		like.setValue("Abies*");
		f.setRoot(like);
		extensionPropertyManager.lookupFilterProperties(f, ExtensionType.Occurrence);
		System.out.println(f);
		
	}

	@Test
	public void testFindProperty() throws Exception {
		ExtensionProperty p = extensionPropertyManager.getByQualName("http://rs.tdwg.org/dwc/dwcore/ScientificName", ExtensionType.Occurrence);
		assertTrue(p!=null);
		
		p = extensionPropertyManager.getByName("ScientificName", ExtensionType.Occurrence);
		assertTrue(p!=null);
	}

}
