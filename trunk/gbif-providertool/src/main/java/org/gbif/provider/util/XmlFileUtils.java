package org.gbif.provider.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class XmlFileUtils {
	public static Writer startNewUtf8File(File file) throws IOException{
		Writer writer=null;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false),"UTF8"));
		return writer;
	}
	public static Writer startNewUtf8XmlFile(File file) throws IOException{
		Writer writer=startNewUtf8File(file);
		writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
		return writer;
	}
}
