package org.gbif.provider.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ViewMappingTest {
	private ExtensionMapping vm1;
	private ExtensionMapping vm2;
	private ExtensionMapping vm3;

	@Before
	public void setUp() throws Exception {
		DataResource r = new OccurrenceResource();
		r.setTitle("Berlin Moss");
		SourceSql src = new SourceSql("Specimens", "Select * from specimen limit 100");
		vm1 = new ExtensionMapping();
		vm1.setCoreIdColumn("specimen_id");
		vm1.setSource(src);
		vm1.setResource(r);
		
		vm2 = new ExtensionMapping();
		vm2.setCoreIdColumn("specimen_id");
		vm2.setSource(src);
		vm2.setResource(r);
		
		vm3 = new ExtensionMapping();
		vm3.setCoreIdColumn("field_number");
		vm3.setSource(src);
		vm3.setResource(r);
	}

	@Test
	public void testHashCode() {
		int hash1 = vm1.hashCode();
		int hash2 = vm2.hashCode();
		// consistency check
		assertTrue(hash1==vm1.hashCode());
		// equal objects must have the same hashcode
		if (vm1.equals(vm2)){
			// FIXME hashcode routine seems wrong!
			assertTrue(vm1.hashCode() == vm2.hashCode());
		}
	}

	@Test
	public void testCompareTo() {
		assertTrue(vm1.compareTo(vm2) < 0);
	}

	@Test
	public void testEqualsObject() {
		assertFalse(vm1.equals(null));
		assertTrue(vm1.equals(vm2));
		assertFalse(vm1.equals(vm3));
	}

	@Test
	public void testToString() {
		String out = vm1.toString();
	}

}
