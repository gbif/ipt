package org.gbif.provider.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Record;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtensionRecord;



public class TabFileWriter {
	public static final String ID_COLUMN_NAME = "ipt:coreid";
	public static final String GUID_COLUMN_NAME = "ipt:guid";
	public static final String LINK_COLUMN_NAME = "ipt:link";
	public static final String MODIFIED_COLUMN_NAME = "ipt:modified";

	private BufferedWriter writer;
	private final List<ExtensionProperty> header;
	private final File file;
	
	public TabFileWriter(File file, ViewCoreMapping view) throws IOException{
		this.header=view.getMappedProperties();
		this.file = file;
		writer = new BufferedWriter(new FileWriter(file));
		// write headers to tabfile
		writer.write(valToString(ID_COLUMN_NAME));
		writer.append("\t");
		writer.write(valToString(GUID_COLUMN_NAME));
		writer.append("\t");
		writer.write(valToString(LINK_COLUMN_NAME));
		writer.append("\t");
		writer.write(valToString(MODIFIED_COLUMN_NAME));
		writePropertyHeader();		
	}
	
	public TabFileWriter(File file, ViewMappingBase view) throws IOException{
		this.header=view.getMappedProperties();
		this.file = file;
		writer = new BufferedWriter(new FileWriter(file));
		// write headers to tabfile
		writer.write(valToString(ID_COLUMN_NAME));
		writePropertyHeader();		
	}
	
	private void writePropertyHeader() throws IOException {
		for (ExtensionProperty prop : header){
			writer.append("\t");
			writer.write(prop.getName());
		}
		writer.newLine();
	}

	/**
	 * write a line of tab seperated map values identified by the column header name given as the map key
	 * In order specified by the header list
	 * @param ln
	 * @throws IOException 
	 */
	public void write(CoreRecord rec) throws IOException{
		writer.write(valToString(rec.getCoreId()));
		writer.append("\t");
		writer.write(valToString(rec.getGuid()));
		writer.append("\t");
		writer.write(valToString(rec.getLink()));
		writer.append("\t");
		writer.write(valToString(rec.getModified()));
		writeRecordProperties(rec);
	}
	
	public void write(ExtensionRecord rec) throws IOException{
		writer.write(valToString(rec.getCoreId()));
		writeRecordProperties(rec);
	}
	
	private void writeRecordProperties(Record rec) throws IOException{
		for (ExtensionProperty prop : header){
			writer.append("\t");
			writer.write(valToString(rec.getPropertyValue(prop)));
		}
		writer.newLine();
	}

	public File getFile(){
		return file;
	}
	
	public void close() throws IOException{
		writer.flush();
		writer.close();
	}
	
	
	public static String valToString(Object val){
		String str = "";
		if (val!=null){
			str = val.toString();
			str = str.replaceAll("\\t", "\\\\t").replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r");
		}
		return str;
	}

}
