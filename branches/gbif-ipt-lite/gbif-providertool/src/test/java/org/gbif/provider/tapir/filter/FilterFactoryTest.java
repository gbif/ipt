package org.gbif.provider.tapir.filter;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class FilterFactoryTest {
	Log log = LogFactory.getLog(this.getClass());
	
	@Test
	public void testBuild() {
		
		File testDir = new File(this.getClass().getResource("/tapir/filter").getFile());
		
		File[] input = testDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("xml"))
					return true;
				else 
					return false;
			}});
		File[] output = testDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("txt"))
					return true;
				else 
					return false;
			}});
		
		
		if (input.length != output.length) {
			fail("Invalid configuration.  Input files[" + input.length + "], output files[" + output.length + "]");
		} 
				
		for (int i=0; i<input.length; i++) {
			try {
				log.debug("Starting parsing of input[" + i + "]");
				
				FileInputStream inputFIS = new FileInputStream(input[i]);
				//Map<String, String[]> params = new HashMap<String, String[]>();
				//params.put("lower", new String[]{"Aus"});
				//params.put("upper", new String[]{"Bus"});
				Map<String, String> params = new HashMap<String, String>();
				params.put("lower", "Aus");
				params.put("upper", "Bus");
				Filter filter = FilterFactory.build(inputFIS, params);
				FileReader fr = new FileReader(output[i]);
				BufferedReader br = new BufferedReader(fr);
				String expected = br.readLine();
				
				log.debug("Received: " + filter.toString());
				log.debug("Expected: " + expected);
				
				// can't decide how restrictive to be...
				assertEquals(expected.replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "").toUpperCase(), filter.toString().replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "").toUpperCase());
				//assertEquals(expected.trim(), filter.toString());
				log.debug("Input[" + i + "] parsed successfully");
				
				br.close();
				fr.close();
				inputFIS.close();
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
		}
	}

}
