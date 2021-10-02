/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package peformance;

import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;

import java.io.IOException;
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
