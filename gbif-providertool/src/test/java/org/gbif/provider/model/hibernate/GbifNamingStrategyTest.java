package org.gbif.provider.model.hibernate;

import static org.junit.Assert.*;

import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.util.ContextAwareTestBase;
import org.hibernate.cfg.NamingStrategy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GbifNamingStrategyTest extends ContextAwareTestBase {
	private static String[] props = {"resource", "propertyName","order","resource.bbox.latitude"};
	private static String[] extensions = {"Paleaontology", "Multi Identification"};
	@Autowired
	private IptNamingStrategy namingStrategy;
	

	@Test
	public void testForeignKeyColumnName() {
		System.out.println(namingStrategy.foreignKeyColumnName("resource.bbox.latitude", "DarwinCore", "Darwin_Core", "id"));
	}

	@Test
	public void testPropertyToColumnName() {
		for (String p : props){
			System.out.println(namingStrategy.propertyToColumnName(p));
		}
	}

	@Test
	public void testLogicalColumnName() {
		for (String p : props){
			System.out.println(namingStrategy.logicalColumnName(p, p));
		}
	}
	
}
