package org.gbif.provider.model;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class DatasourceBasedResourceTest {
	private Resource r1;
	private Resource r2;
	private Resource r3;
	private static final Date now = new Date();
	private static final String guid = "ac567-hjds78-asgzc-26347129";

	@Before
	public void setUp() throws Exception {
		r1 = getNewResource();
		r2 = getNewResource();
		r3 = getNewResource();
		r3.setTitle("A different title");
	}
	private Resource getNewResource(){
		DataResource r = OccurrenceResource.newInstance();
		r.setTitle("Berlin Moss");
		r.setCreated(now);
		r.setGuid(guid);
		r.setJdbcUser("root");
		r.setDescription("bla bla bla");
		// add viewmapping
		Extension ext = new Extension();
		ext.setId(1L);
		ext.setName("Bernde");
		ExtensionMapping vm = new ExtensionMapping();
		vm.setId(23L);
		vm.setExtension(ext);
		r.addExtensionMapping(vm);
		return r;
	}
	
	@Test
	public void testHashCode() {
		int hash1 = r1.hashCode();
		int hash2 = r2.hashCode();
		// consistency check
		assertTrue(hash1==r1.hashCode());
		// equal objects must have the same hashcode
		if (r1.equals(r2)){
			assertTrue(r1.hashCode() == r2.hashCode());
		}
	}

	@Test
	public void testEqualsObject() {
		assertFalse(r1.equals(null));
		assertTrue(r1.equals(r2));
		assertFalse(r1.equals(r3));	
	}

	@Test
	public void testToString() {
		String out = r1.toString();
	}

	@Test
	public void testCompareTo() {
		System.out.println(r1.compareTo(r3));
		assertTrue(r1.compareTo(r1) == 0);
		assertTrue(r1.compareTo(r2) == 0);
		assertTrue(r1.compareTo(r3) > 0);
	}

}
