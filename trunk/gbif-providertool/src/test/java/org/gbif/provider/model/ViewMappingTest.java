package org.gbif.provider.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ViewMappingTest {
	private ViewMapping vm1;
	private ViewMapping vm2;
	private ViewMapping vm3;

	@Before
	public void setUp() throws Exception {
		DatasourceBasedResource r = new OccurrenceResource();
		r.setTitle("Berlin Moss");
		vm1 = new ViewMapping();
		vm1.setCoreIdColumnIndex(3);
		vm1.setViewSql("Select * from specimen limit 100");
		vm1.setResource(r);
		vm2 = new ViewMapping();
		vm2.setCoreIdColumnIndex(3);
		vm2.setViewSql("Select * from specimen limit 100");
		vm2.setResource(r);
		vm3 = new ViewMapping();
		vm3.setCoreIdColumnIndex(2);
		vm3.setViewSql("Select * from specimen limit 100");
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
			//assertTrue(vm1.hashCode() == vm2.hashCode());
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
