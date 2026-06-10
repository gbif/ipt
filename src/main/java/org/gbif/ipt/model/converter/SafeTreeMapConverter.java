/*
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
package org.gbif.ipt.model.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * XStream's TreeMapConverter can fail on newer JVMs (Java 17+) due to reflective access.
 * This converter reads legacy TreeMap XML but materializes it as a LinkedHashMap.
 * <p>
 * Works well when the target field type is Map/SortedMap (interface) rather than concrete TreeMap.
 */
public class SafeTreeMapConverter implements Converter {

  @Override
  public boolean canConvert(Class type) {
    return TreeMap.class.equals(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    // Keep the default behavior if we ever marshal a TreeMap explicitly.
    // (In practice we aim to avoid persisting TreeMap/TreeSet entirely.)
    @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) source;
    for (Map.Entry<Object, Object> e : map.entrySet()) {
      writer.startNode("entry");
      writer.startNode("key");
      context.convertAnother(e.getKey());
      writer.endNode();
      writer.startNode("value");
      context.convertAnother(e.getValue());
      writer.endNode();
      writer.endNode();
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Map<Object, Object> out = new LinkedHashMap<>();

    while (reader.hasMoreChildren()) {
      reader.moveDown(); // entry
      Object key = null;
      Object value = null;

      while (reader.hasMoreChildren()) {
        reader.moveDown(); // key/value or direct item
        String node = reader.getNodeName();

        if ("key".equals(node)) {
          if (reader.hasMoreChildren()) {
            reader.moveDown();
            key = context.convertAnother(out, Object.class);
            reader.moveUp();
          }
        } else if ("value".equals(node)) {
          if (reader.hasMoreChildren()) {
            reader.moveDown();
            value = context.convertAnother(out, Object.class);
            reader.moveUp();
          }
        }

        reader.moveUp();
      }

      // TODO: Support older map formats that might not wrap key/value the same way?
//      if (key == null && value == null && reader.hasMoreChildren()) {}

      if (key != null) {
        out.put(key, value);
      }

      reader.moveUp(); // entry
    }

    return out;
  }
}
