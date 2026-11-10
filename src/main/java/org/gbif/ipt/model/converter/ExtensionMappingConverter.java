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

import org.gbif.ipt.model.ExtensionMapping;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Converts the mappings of a resource (resource.xml).
 * <p>
 * Class names are resolved by the mapper of the XStream instance the converter is registered with, so its type
 * allowlist ({@link org.gbif.ipt.utils.XStreamSecurity}) and aliases (e.g. filesource, sqlsource) apply inside
 * mappings as well.
 */
public class ExtensionMappingConverter extends ReflectionConverter {

  /**
   * @param xstream the XStream instance this converter gets registered with
   */
  public ExtensionMappingConverter(XStream xstream) {
    super(new MappingMapper(xstream.getMapper()), new PureJavaReflectionProvider());
  }

  @Override
  public boolean canConvert(Class type) {
    return ExtensionMapping.class.equals(type);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    // don't take the row type of the previous mapping if this one has none
    context.put(ExtensionRowTypeConverter.ROW_TYPE_VERBATIM, null);
    ExtensionMapping mapping = (ExtensionMapping) super.unmarshal(reader, context);

    if (mapping.getExtension() != null) {
      mapping.setExtensionVerbatim(mapping.getExtension().toString());
    } else {
      // not installed: the row type as written in resource.xml
      mapping.setExtensionVerbatim((String) context.get(ExtensionRowTypeConverter.ROW_TYPE_VERBATIM));
    }

    return mapping;
  }

  /**
   * Keeps the format IPT 3.3.7 writes, so it can still read the mappings after a downgrade: fully qualified class
   * names instead of aliases (as written since IPT 3.2.0), and default implementations only for null and primitive
   * types (XStream's DefaultImplementationsMapper defaults), so the class of any other field is written whenever it
   * differs from the declared type. Everything else, including the allowlist check when reading, comes from the
   * wrapped mapper.
   */
  private static class MappingMapper extends MapperWrapper {

    MappingMapper(Mapper wrapped) {
      super(wrapped);
    }

    @Override
    public String serializedClass(Class type) {
      return type.getName();
    }

    @Override
    public Class defaultImplementationOf(Class type) {
      if (type == null || type.isPrimitive()) {
        return super.defaultImplementationOf(type);
      }
      return type;
    }
  }
}
