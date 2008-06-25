package org.gbif.provider;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TryingHard {
	@Test
	public void testBool(){
		Boolean b = true;
		if (b !=null && b){
			System.out.println("yeah");
		}else{
			System.out.println("NO");
		}
	}
	
	@Test
	public void testArray(){
		int i = 1;
		int[] a = new int[10]; 
		Arrays.fill(a, i++);
		System.out.println(a);
		//Collections.list(a);
		String[] names = {"peter","marta","caroline"};
		System.out.println(names);
	}

	@Test
	public void testNull(){
		int i = 1;
		Long l = 1000L;
		Long t = null;
		if (t==l){
			System.out.println("yes");
		}else{
			System.out.println("no");
		}
	}

	@Test
	public void testEqual(){
		Long i = new Long(1);
		Long i2 = new Long(1);
		Long i3 = new Long(2);
		assertFalse(i==i2);
		assertTrue(i.equals(i2));
		assertFalse(i==i3);
	}
	
	@Test
	public void testPrimitives(){
		int i;
		Map<String, Integer> m = new HashMap<String, Integer>();
		m.put("one", Integer.valueOf(1));
		m.put("two", Integer.valueOf(2));
	}
	
}
