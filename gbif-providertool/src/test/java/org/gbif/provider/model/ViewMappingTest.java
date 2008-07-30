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
		DatasourceBasedResource r = OccurrenceResource.newInstance(null);
		r.setTitle("Berlin Moss");
		vm1 = new ViewMapping();
		vm1.getCoreIdColumn().setColumnName("specimen_id");
		vm1.setSourceSql("Select * from specimen limit 100");
		vm1.setResource(r);
		
		vm2 = new ViewMapping();
		vm2.getCoreIdColumn().setColumnName("specimen_id");
		vm2.setSourceSql("Select * from specimen limit 100");
		vm2.setResource(r);
		
		vm3 = new ViewMapping();
		vm3.getCoreIdColumn().setColumnName("field_number");
		vm3.setSourceSql("Select * from specimen limit 100");
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
