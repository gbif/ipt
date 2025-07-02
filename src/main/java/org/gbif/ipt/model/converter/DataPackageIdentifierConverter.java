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

import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DataPackageIdentifierConverter implements Converter {

  private final DataPackageSchemaManager manager;
  private DataPackageSchema lastDataPackageConverted;

  public DataPackageIdentifierConverter(DataPackageSchemaManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(DataPackageSchema.class);
  }

  public DataPackageSchema getLastDataPackageConverted() {
    return lastDataPackageConverted;
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataPackageSchema d = (DataPackageSchema) value;
    writer.setValue(d.getIdentifier());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    DataPackageSchema d = manager.get(reader.getValue());
    lastDataPackageConverted = d;
    return d;
  }

}
