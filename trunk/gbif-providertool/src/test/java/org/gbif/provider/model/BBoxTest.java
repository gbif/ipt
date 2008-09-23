package org.gbif.provider.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BBoxTest {
	private Point p1;
	private Point p2;
	private Point p3;
	private Point p4;
	private BBox bbox;
	
	@Before
	public void setUp() throws Exception {
		p1 = new Point(54f, 123f);
		p2 = new Point(31.243f, -13.9883f);
		p3 = new Point(41.1f, -3.9f);
		p4 = new Point(41.1f, -33.9f);
		bbox = new BBox(p1, p2);
	}

	@Test
	public void testExpandBox() {
		bbox.expandBox(p1);
		bbox.expandBox(p3);
		assertTrue(bbox.contains(p3));
		
		bbox.expandBox(p4);
		assertTrue(bbox.contains(p4));
		
		// test with null values in bbox
		bbox=new BBox();
		assertFalse(bbox.contains(p4));
		bbox.expandBox(p4);
		assertTrue(bbox.contains(p4));
		bbox.expandBox(p3);
		assertTrue(bbox.contains(p3));
		assertTrue(bbox.contains(p4));
		bbox.expandBox(p1);
		assertTrue(bbox.contains(p1));
		assertTrue(bbox.contains(p3));
		assertTrue(bbox.contains(p4));
	}

	@Test
	public void testContains() {
		assertTrue(bbox.contains(p1));
		assertTrue(bbox.contains(p3));
		assertFalse(bbox.contains(p4));
	}

	@Test
	public void testBoxRatio() {
		System.out.println(bbox);
		bbox.fitRatio(1.4);
		System.out.println(bbox);
		bbox.fitRatio(1.2);
		System.out.println(bbox);
	}
}
