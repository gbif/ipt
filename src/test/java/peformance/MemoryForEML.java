package peformance;

import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class MemoryForEML {

  public static void main(String[] args) {

    try {
      URL sample = new URL("http://rs.gbif.org/schema/eml/sample.xml");
      Map<Integer, Eml> cache = new HashMap<>();
      long time = System.currentTimeMillis();
      for (int i = 0; i < 1000; i++) {
        cache.put(i, EmlFactory.build(sample.openStream()));
      }
      System.out.println(System.currentTimeMillis() - time);
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();
      System.gc();

      long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      System.out.println(mem / (1024));
    } catch (IOException | SAXException | ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
}
