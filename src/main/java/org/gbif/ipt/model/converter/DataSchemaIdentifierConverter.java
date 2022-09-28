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

import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.service.admin.DataSchemaManager;

import com.google.inject.Inject;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DataSchemaIdentifierConverter implements Converter {

  private final DataSchemaManager manager;
  private DataSchema lastDataSchemaConverted;

  @Inject
  public DataSchemaIdentifierConverter(DataSchemaManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(DataSchema.class);
  }

  public DataSchema getLastDataSchemaConverted() {
    return lastDataSchemaConverted;
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataSchema d = (DataSchema) value;
    writer.setValue(d.getIdentifier());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    DataSchema d = manager.get(reader.getValue());
    lastDataSchemaConverted = d;
    return d;
  }

}
