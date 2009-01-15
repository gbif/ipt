package org.gbif.provider.tapir.filter;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.tapir.Filter;
import org.gbif.provider.tapir.FilterFactory;
import org.gbif.provider.tapir.FilterHandler;
import org.junit.Before;
import org.junit.Test;

public class FilterHandlerTest {
	Log log = LogFactory.getLog(this.getClass());

	@Test
	public void testFilters() throws Exception {
		File testDir = new File(this.getClass().getResource("/org/gbif/provider/tapir/filter/test").getFile());
		
		File[] input = testDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("kvp"))
					return true;
				else 
					return false;
			}});		

		FilterHandler fh = new FilterHandler();
		for (int i=0; i<input.length; i++) {
			try {
				log.debug("Starting parsing of input[" + i + "]");
				
				FileReader fr = new FileReader(input[i]);
				BufferedReader br = new BufferedReader(fr);
				String filterString = br.readLine();
				fh.parse(filterString);				
				
				//assertEquals(expected.trim(), filter.toString());
				log.debug("Input[" + i + "] parsed successfully");								
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				fail(e.getMessage());
			}
			
		}
	}	
}
