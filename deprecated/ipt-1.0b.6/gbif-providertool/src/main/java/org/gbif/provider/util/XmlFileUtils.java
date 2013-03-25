package org.gbif.provider.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;

public class XmlFileUtils {
	public static Writer startNewUtf8File(File file) throws IOException{
		Writer writer=null;
		FileUtils.touch(file);
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false),"UTF8"));
		return writer;
	}
	public static Writer startNewUtf8XmlFile(File file) throws IOException{
		Writer writer=startNewUtf8File(file);
		writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
		return writer;
	}
	public static Reader getUtf8Reader(File file) throws FileNotFoundException{
		Reader reader=null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return reader;
	}
}
