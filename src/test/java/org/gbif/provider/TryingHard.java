package org.gbif.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}
