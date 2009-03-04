package org.gbif.provider.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



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
	private static Pattern tabPattern = Pattern.compile("\t");
	
	public TabFileReader(File file) throws IOException, MalformedTabFileException{
		this.it = FileUtils.lineIterator(file, "UTF-8");
		this.file = file;
		// read header
		if (it.hasNext()){
		    String line = it.nextLine();
			header = tabPattern.split(line);
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
	    String[] columns = tabPattern.split(line);
		if (columns.length > header.length){
			List<String> cl = Arrays.asList(columns);
			cl.subList(0, header.length-1);
			log.warn("row contains more columns than header");
			columns = cl.toArray(new String[header.length-1]);
		}else if (columns.length < header.length){
			// pad missing columns with nulls
			columns = (String[]) ArrayUtils.addAll(columns, new String[header.length-columns.length]);
		}
		return columns;
	}

	public void remove() {
		it.remove();		
	}

}
