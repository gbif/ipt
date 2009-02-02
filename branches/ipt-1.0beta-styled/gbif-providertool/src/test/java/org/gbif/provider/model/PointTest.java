package org.gbif.provider.model;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PointTest {

	@Test
	public void rangeLimit(){
		Point p1 = new Point(54.0, 123.0);
		Point p2 = new Point(31.24332, -13.9883321);
		p2.toStringShort(3);
		assertFalse(p1.equals(p2));
		p1.setLatitude(31.24332);
		p1.setLongitude(-13.9883321);
		assertTrue(p1.equals(p2));
		
		p1.setLatitude(null);
		boolean success=false;
		try{
			p1.setLatitude(121.243);
		}catch(IllegalArgumentException e){
			success=true;
		}
		assertTrue(success);
		assertFalse(p1.isValid());
		assertTrue(p2.isValid());
		
		// test nulls
		Point p = new Point();
		Point pp = new Point(p);
		Double lon = p.getLongitude();
		Double lat = p.getLatitude();
		pp = new Point(lat, lon);
	}
}
