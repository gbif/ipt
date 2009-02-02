package org.gbif.provider.util;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CacheMapTest {

	@Test
	public void testAll(){
		CacheMap lm = new CacheMap<Integer, Integer>(4);
		Integer[] vals = {1,2,3,2,3,54,34,3,4,5,32,3};
		for (Integer v : vals){
			lm.put(v, v*v);
			assertTrue(lm.size()<5);
			assertEquals(lm.get(v), v*v);
//			System.out.println(lm.keySet());
		}
	}
}
