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

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;

import java.util.Set;
import java.util.TreeSet;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ExtensionMappingConverter implements Converter {

  private final ReflectionConverter reflectionConverter;

  public ExtensionMappingConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    this.reflectionConverter = new ReflectionConverter(mapper, reflectionProvider);
  }

  @Override
  public boolean canConvert(Class type) {
    return ExtensionMapping.class.equals(type);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    ExtensionMapping mapping = new ExtensionMapping();

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String nodeName = reader.getNodeName();

      switch (nodeName) {
        case "extension":
          Extension extension = (Extension) context.convertAnother(mapping, Extension.class);
          mapping.setExtension(extension);
          mapping.setExtensionVerbatim(reader.getValue());
          break;

        case "fields":
          Set<PropertyMapping> fields = new TreeSet<>();
          while (reader.hasMoreChildren()) {
            reader.moveDown();
            PropertyMapping pm = (PropertyMapping) context.convertAnother(
                mapping, PropertyMapping.class);
            fields.add(pm);
            reader.moveUp();
          }
          mapping.setFields(fields);
          break;

        default:
          // explicitly ignore unknown nodes or log
          break;
      }

      reader.moveUp();
    }

    return mapping;
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    // Delegate entirely to default reflection-based serialization
    reflectionConverter.marshal(source, writer, context);
  }
}
