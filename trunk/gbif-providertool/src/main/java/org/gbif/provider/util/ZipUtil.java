package org.gbif.provider.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipUtil {
	protected static final Log log = LogFactory.getLog(ZipUtil.class);
	static final int BUFFER = 2048;

	public static List<File> unzipFile(File directory, File zipFile) throws IOException {
	    ZipFile zf = new ZipFile(zipFile);
	    Enumeration entries = zf.entries();
	    List<File> files = new ArrayList<File>();
	    while(entries.hasMoreElements()) {
	        ZipEntry entry = (ZipEntry)entries.nextElement();
	        if(entry.isDirectory()) {
	        	log.warn("ZIP archive contains directories which are being ignored");
	            continue;
	        }
	        System.out.println("ZIP entry:");
	        System.out.println(entry.getName());
	        String fn = new File(entry.getName()).getName();
	        System.out.println(fn);
	        if(fn.startsWith(".")) {
	        	log.warn("ZIP archive contains a hidden file which is being ignored");
	            continue;
	        }
	        File targetFile = new File(directory, fn);
	        files.add(targetFile);
	        log.debug("Extracting file: " + entry.getName() + " to: "+targetFile.getAbsolutePath());
	        copyInputStream(zf.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFile)));
	    }
	    zf.close();
		return files;
	}
	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException
	  {
	    byte[] buffer = new byte[1024];
	    int len;

	    while((len = in.read(buffer)) >= 0)
	      out.write(buffer, 0, len);

	    in.close();
	    out.close();
	}
	  
	public static void zipFile(File file, File zipFile) throws IOException {
		Set<File> files = new HashSet<File>();
		files.add(file);
		zipFiles(files, zipFile);
	}
	public static void zipFiles(Set<File> files, File zipFile) throws IOException {
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
					log.debug("Adding file "+f+" to archive");
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
