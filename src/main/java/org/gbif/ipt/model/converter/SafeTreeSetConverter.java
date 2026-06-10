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

import org.gbif.ipt.model.PropertyMapping;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Reads legacy TreeSet XML without triggering XStream's TreeMapConverter.
 * Materializes it as a LinkedHashSet to preserve iteration order from the XML.
 */
public class SafeTreeSetConverter implements Converter {

  @Override
  public boolean canConvert(Class type) {
    return TreeSet.class.equals(type);
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    @SuppressWarnings("unchecked")
    Set<Object> set = (Set<Object>) source;
    for (Object entry : set) {
      String nodeName = (entry instanceof PropertyMapping) ? "field" : "item";
      writer.startNode(nodeName);
      context.convertAnother(entry);
      writer.endNode();
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Set<Object> out = new LinkedHashSet<>();
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      Object item;
      if ("field".equals(reader.getNodeName())) {
        item = context.convertAnother(out, PropertyMapping.class);
      } else {
        item = context.convertAnother(out, Object.class);
      }
      out.add(item);
      reader.moveUp();
    }
    return out;
  }
}
