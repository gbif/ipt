package org.gbif.provider.model;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PointTest {

	@Test
	public void rangeLimit(){
		Point p1 = new Point(54f, 123f);
		Point p2 = new Point(31.243f, -13.9883f);
		assertFalse(p1.equals(p2));
		p1.setLatitude(31.243f);
		p1.setLongitude(-13.9883f);
		assertTrue(p1.equals(p2));
		
		p1.setLatitude(null);
		boolean success=false;
		try{
			p1.setLatitude(121.243f);
		}catch(IllegalArgumentException e){
			success=true;
		}
		assertTrue(success);
		assertFalse(p1.isValid());
		assertTrue(p2.isValid());
		
		// test nulls
		Point p = new Point();
		Point pp = new Point(p);
		Float lon = p.getLongitude();
		Float lat = p.getLatitude();
		pp = new Point(lat, lon);
	}
}
