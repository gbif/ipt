package org.gbif.iptlite.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Record;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.dto.ExtensionRecord;



public class ArchiveWriter {
	public static final String ID_COLUMN_NAME = "dc:identifier";
	public static final String LINK_COLUMN_NAME = "dc:source";
	public static final String MODIFIED_COLUMN_NAME = "dc:modified";

	private BufferedWriter writer;
	private final List<ExtensionProperty> header;
	private final File file;
	private final String guidPropertyName;
	private boolean useLink=false;
	
	public ArchiveWriter(File file, ExtensionMapping view, boolean useLink) throws IOException{
		this.header=view.getMappedProperties();
		this.file = file;
		if (file.exists()){
			file.delete();
		}else{
			FileUtils.forceMkdir(file.getParentFile());
		}
		file.createNewFile();
		this.writer = new BufferedWriter(new FileWriter(file));
		this.useLink=useLink;
		this.guidPropertyName = view.getResource().getDwcGuidPropertyName();
		// write headers to tabfile
		writePropertyHeader();		
	}
	

	private void writePropertyHeader() throws IOException {
//		writer.write(valToString(ID_COLUMN_NAME));
		writer.write(guidPropertyName);
		if (useLink){
			writer.append("\t");
			writer.write(valToString(LINK_COLUMN_NAME));
		}
		for (ExtensionProperty prop : header){
			if (prop.getName().equals(guidPropertyName)){
				continue;
			}
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
	public void write(ImportRecord rec) throws IOException{
		writer.write(valToString(rec.getSourceId()));
		if (useLink){
			writer.append("\t");
			writer.write(valToString(rec.getLink()));
		}
		for (ExtensionProperty prop : header){
			if (prop.getName().equals(guidPropertyName)){
				continue;
			}
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
