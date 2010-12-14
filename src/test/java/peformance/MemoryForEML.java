/**
 * 
 */
package peformance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.xml.sax.SAXException;

/**
 * @author timrobertson
 *
 */
public class MemoryForEML {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			URL sample = new URL("http://rs.gbif.org/schema/eml/sample.xml");
			Map<Integer, Eml> cache = new HashMap<Integer, Eml>();
			long time = System.currentTimeMillis();
			for (int i=0; i<1000; i++) {
				cache.put(i, EmlFactory.build(sample.openStream()));
			}
			System.out.println(System.currentTimeMillis() - time);
			System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
			System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
			System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
			System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
			System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
			
			long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			System.out.println(mem/(1024));			
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}
