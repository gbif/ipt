package org.gbif.provider.tapir;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FilterTest {

	@Test
	public void testFilter() throws Exception {
		Filter f = new Filter("Hallo Tim, do you like my parser?");
		System.out.println("-----");
		f = new Filter("Hallo Tim: \"do you like my parser\"? Good's");
		System.out.println("-----");
		f = new Filter("Hallo Tim( \"do you like my parser\"? Good.(me too)) hi");
		System.out.println("-----");
		boolean failed=false;
		try{
			f = new Filter("Hallo, \"do you like my parser\"? Good to know \" quotes dont work");
		}catch (Exception e){
			failed=true;
		}
		assertTrue(failed);
	}

}
