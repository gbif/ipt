package org.gbif.provider.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.DarwinCore;



public class TabFileWriter {
	private BufferedWriter writer;
	private final List<String> header;
	
	public TabFileWriter(File file, List<String> header) throws IOException{
		this.header=header;
		writer = new BufferedWriter(new FileWriter(file));
		// write headers to tabfile
		Map<String,String> headerMap = new HashMap<String, String>();
		for (String col : header){
			headerMap.put(col, col);
		}
		this.write(headerMap);
	}
	
	public List<String> getHeader() {
		return header;
	}

	/**
	 * write a line of tab seperated map values identified by the column header name given as the map key
	 * In order specified by the header list
	 * @param ln
	 * @throws IOException 
	 */
	public void write(Map<String, String> ln) throws IOException{
		Iterator<String> iter = header.iterator();
		while (iter.hasNext()){
			writer.write(escape(ln.get(iter.next())));
			if (iter.hasNext()){
				writer.append("\t");
			}else{
				//writer.append("\n");
				writer.newLine();
			}			
		}
	}
	
	private String escape(Object obj){
		String result = "";
		if (obj != null){
			result = obj.toString().replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r");
		}
		return result;
	}
}
