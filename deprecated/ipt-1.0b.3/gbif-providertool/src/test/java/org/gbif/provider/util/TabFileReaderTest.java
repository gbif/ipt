package org.gbif.provider.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class TabFileReaderTest extends ContextAwareTestBase{
	@Autowired
	private AppConfig cfg;
	
	@Test
	public void testTabFileReader() throws IOException, MalformedTabFileException {
		File f = cfg.getResourceSourceFile(Constants.TEST_OCC_RESOURCE_ID, "pontaurus.txt");
		TabFileReader reader = new TabFileReader(f);
		 System.out.println(Arrays.asList(reader.getHeader()));
		 System.out.println();
	     for (int i=0; i<25; i++){	    	 
			 System.out.println(Arrays.asList(reader.next()));
	     }
	     reader.close();
	}

}
