package org.gbif.provider.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipUtil {
	protected static final Log log = LogFactory.getLog(ZipUtil.class);
	static final int BUFFER = 2048;

	public static void zipFiles(List<File> files, File zipFile) throws IOException {
		if (files.isEmpty()){
			log.warn("no files to zip.");
		}else{
		     try {
				BufferedInputStream origin = null;
				 FileOutputStream dest = new FileOutputStream(zipFile);
				 ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
				 //out.setMethod(ZipOutputStream.DEFLATED);
				 byte data[] = new byte[BUFFER];
				 for (File f : files) {
				    System.out.println("Adding: "+f);
				    FileInputStream fi = new FileInputStream(f);
				    origin = new BufferedInputStream(fi, BUFFER);
				    ZipEntry entry = new ZipEntry(f.getName());
				    out.putNextEntry(entry);
				    int count;
				    while((count = origin.read(data, 0, BUFFER)) != -1) {
				       out.write(data, 0, count);
				    }
				    origin.close();
				 }
				 out.finish();
				 out.close();
			} catch (IOException e) {
				log.error("IOException while zipping files: "+files);
				throw e;
			}
		}
	}
}
