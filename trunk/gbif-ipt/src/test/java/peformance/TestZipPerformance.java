/**
 * 
 */
package peformance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This is a test to see how long it takes to modify an existing Zip file  
 * @author timrobertson
 */
public class TestZipPerformance {
	public static void main(String[] args) {
		try {
			long time = System.currentTimeMillis();
			System.out.println("Creating Zip file in the /tmp folder");
			FileOutputStream dest = new FileOutputStream("/tmp/iptziptest.zip");
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			
			time = System.currentTimeMillis();
			System.out.println("Adding data to Zip");
			InputStream is = TestZipPerformance.class.getResourceAsStream("/data/two_fifty_thousand.txt");
			BufferedInputStream bis = new BufferedInputStream(is, 1024);
			ZipEntry entry = new ZipEntry("two_fifty_thousand.txt");
			out.putNextEntry(entry);
			int count;
			byte data[] = new byte[1024];
			while((count = bis.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();
			System.out.println("  Time: " + (System.currentTimeMillis()-time) + " msecs");
			
			time = System.currentTimeMillis();
			System.out.println("Adding EML to Zip");
			is = TestZipPerformance.class.getResourceAsStream("/data/eml.xml");
			bis = new BufferedInputStream(is, 1024);
			entry = new ZipEntry("eml.xml");
			out.putNextEntry(entry);
			count=0;
			while((count = bis.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();
			System.out.println("  Time: " + (System.currentTimeMillis()-time) + " msecs");
			
			System.out.println("Closing Zip");
			out.close();
			
			System.out.println("Creating Zip2");
			dest = new FileOutputStream("/tmp/iptziptest2.zip");
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			ZipFile zip = new ZipFile("/tmp/iptziptest.zip");
			
			time = System.currentTimeMillis();
			System.out.println("Adding new EML to new Zip");
			is = TestZipPerformance.class.getResourceAsStream("/data/eml2.xml");
			bis = new BufferedInputStream(is, 1024);
			entry = new ZipEntry("eml.xml");
			out.putNextEntry(entry);
			count=0;
			while((count = bis.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();
			System.out.println("  Time: " + (System.currentTimeMillis()-time) + " msecs");
			
			time = System.currentTimeMillis();
			System.out.println("Adding previous data to new Zip");
			is = zip.getInputStream(zip.getEntry("two_fifty_thousand.txt"));
			bis = new BufferedInputStream(is, 1024);
			out.putNextEntry(zip.getEntry("two_fifty_thousand.txt"));
			while((count = bis.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();			
			System.out.println("  Time: " + (System.currentTimeMillis()-time) + " msecs");
			
			System.out.println("Closing new Zip");
			out.close();			
			
			
			
			 
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		
	}
}
