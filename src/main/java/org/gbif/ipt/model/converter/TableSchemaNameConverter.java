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

import org.gbif.ipt.model.DataPackageTableSchemaName;

import com.google.inject.Singleton;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@Singleton
public class TableSchemaNameConverter implements Converter {

  private String lastTableSchemaConverted;

  @Override
  public boolean canConvert(Class clazz) {
    return clazz.equals(DataPackageTableSchemaName.class);
  }

  public String getLastTableSchemaConverted() {
    return lastTableSchemaConverted;
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataPackageTableSchemaName u = (DataPackageTableSchemaName) value;
    writer.setValue(u.getName());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String tableSchemaName = reader.getValue();
    lastTableSchemaConverted = tableSchemaName;
    DataPackageTableSchemaName result = new DataPackageTableSchemaName();
    result.setName(tableSchemaName);
    return result;
  }
}
