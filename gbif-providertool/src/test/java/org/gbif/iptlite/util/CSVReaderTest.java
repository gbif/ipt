package org.gbif.iptlite.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.gbif.provider.util.AppConfig;
import org.junit.Before;
import org.junit.Test;

public class CSVReaderTest {

	@Test
	public void testBuildReaderFile() throws IOException {
		CSVReader reader = CSVReader.buildReader(classpathFile("iucn100.csv"), 0);
		reader.close();
		reader = CSVReader.buildReader(classpathFile("iucn100.tab.txt"), 0);
		reader.close();
		reader = CSVReader.buildReader(classpathFile("iucn100.pipe.txt"), 0);
		reader.close();
	}

	@Test
	public void testHeaderRows() throws IOException {
		File source = classpathFile("iucn100.csv");
		// assert the headers are the same, no matter how many rows we skip for the iterator
		CSVReader reader = CSVReader.buildReader(source, true);
		String[] header1 = reader.getHeader(); 
		reader.close();
		
		reader = CSVReader.buildReader(source, false);
		String[] header2 = reader.getHeader(); 
		reader.close();

		reader = CSVReader.buildReader(source, 3);
		String[] header3 = reader.getHeader(); 
		reader.close();
		
		assertTrue(header1.length==header2.length);
		int idx = header1.length;
		while(idx>0){
			idx--;
			assertEquals(header1[idx], header2[idx]);
		}
		
		assertTrue(header1.length==header3.length);
		idx = header1.length;
		while(idx>0){
			idx--;
			assertEquals(header1[idx], header3[idx]);
		}
	}
	

	@Test
	public void testSkipRows() throws IOException {
		File source = classpathFile("iucn100.csv");

		CSVReader reader = CSVReader.buildReader(source, 7);
		for (String[] row : reader){
			assertEquals(row[0], "9");
			assertEquals(row[1], "Aaptosyax grypus Rainboth, 1991");
			assertEquals(row[4], "Actinopterygii");
			break;
		}
	}
	
	
	@Test
	public void testHeaderTrue() throws IOException {
		File source = classpathFile("iucn100.csv");

		CSVReader reader = CSVReader.buildReader(source, true);
		for (String[] row : reader){
			assertEquals(row[1], "Aaadonta angaurana");
			break;
		}
		reader.close();
	}
	
	@Test
	public void testHeaderFalse() throws IOException {
		File source = classpathFile("iucn100.csv");

		CSVReader reader = CSVReader.buildReader(source, false);
		for (String[] row : reader){
			assertEquals(row[1], "Lophopsittacus bensoni Holyoak, 1973");
			break;
		}
		reader.close();
	}

	
	public File classpathFile(String path){
		File f = null;
		//relative path. Use classpath instead
		URL url = getClass().getClassLoader().getResource(path);
		if (url!=null){
			f = new File(url.getFile());
		}
		return f;
	}
}
