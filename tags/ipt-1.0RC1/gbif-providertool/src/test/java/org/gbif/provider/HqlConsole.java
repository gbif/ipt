package org.gbif.provider;

import java.util.List;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.HqlTester;
import org.gbif.provider.util.ContextAwareTestBase;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
public class HqlConsole extends ContextAwareTestBase{
	@Autowired
	private HqlTester hqlTester;


	@Test	
	public void testDummy() {
		assertTrue(true);
	}
	
	@Test	
	public void testHql() {
		hqlTester.runHql();
	}
	
}
