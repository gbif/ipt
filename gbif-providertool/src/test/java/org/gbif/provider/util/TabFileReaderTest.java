package org.gbif.provider.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class TabFileReaderTest extends ContextAwareTestBase{

	@Test
	public void testTabFileReader() {
		Resource r = this.getApplicationContext().getResource("classpath:gbifOccurrenceDownload.txt"); 
		File f;
		try {
			f = r.getFile();
			TabFileReader reader = new TabFileReader(f);
			 System.out.println(Arrays.asList(reader.getHeader()));
			 System.out.println();
		     for (int i=0; i<25; i++){	    	 
				 System.out.println(Arrays.asList(reader.next()));
		     }
		     reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedTabFileException e) {
			e.printStackTrace();
		}
		
	}

}
