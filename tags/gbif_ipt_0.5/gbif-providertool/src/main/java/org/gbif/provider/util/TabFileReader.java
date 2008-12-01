package org.gbif.provider.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DarwinCore;



/**
 * Simple tab file reader that iterates through a tab file and returns row by row and assures all rows have the same number of columns.
 * Will throw a MalformedTabFileException otherwise.
 * The header is not returned as a regular row through the iterator, but can be retrieved via the getHeader() method at any time. 
 * @author markus
 *
 */
public class TabFileReader implements Iterator<String[]>{
	private final Log log = LogFactory.getLog(TabFileReader.class);
	private LineIterator it;
	private final File file;
	private String[] header;
	
	public TabFileReader(File file) throws IOException, MalformedTabFileException{
		this.it = FileUtils.lineIterator(file, "UTF-8");
		this.file = file;
		// read header
		if (it.hasNext()){
		    String line = it.nextLine();
			header = line.split("\t");
		}else{
			throw new MalformedTabFileException();
		}
		
	}
	
	public File getFile(){
		return file;
	}
	
	public String[] getHeader(){
		return header;
	}

	public void close() throws IOException{
		LineIterator.closeQuietly(it);
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public String[] next() {
	    String line = it.nextLine();
		String[] columns = line.split("\t");
		if (columns.length > header.length){
			List<String> cl = Arrays.asList(columns);
			cl.subList(0, header.length-1);
			log.warn("row contains more columns than header");
			columns = cl.toArray(new String[header.length-1]);
		}else if (columns.length < header.length){
			List<String> cl = Arrays.asList(columns);
			while(cl.size()<header.length){
				cl.add(null);
			}
			columns = cl.toArray(new String[header.length-1]);
		}
		return columns;
	}

	public void remove() {
		it.remove();		
	}

}
