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
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSubschema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@Singleton
public class DataSchemaFieldConverter implements Converter {

  private static final Logger LOG = LogManager.getLogger(DataSchemaFieldConverter.class);
  private final DataSchemaIdentifierConverter schemaConverter;
  private final DataSubschemaNameConverter schemaNameConverter;

  @Inject
  public DataSchemaFieldConverter(DataSchemaIdentifierConverter schemaConverter,
                                  DataSubschemaNameConverter schemaNameConverter) {
    this.schemaConverter = schemaConverter;
    this.schemaNameConverter = schemaNameConverter;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return DataSchemaField.class.isAssignableFrom(clazz);
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataSchemaField field = (DataSchemaField) value;
    writer.setValue(field.getName());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    DataPackageSchema schema = schemaConverter.getLastDataSchemaConverted();
    String subschemaName = schemaNameConverter.getLastDataSubschemaConverted();

    DataSchemaField field = null;

    if (schema != null) {
      DataSubschema subschema = null;
      for (DataSubschema dss : schema.getTableSchemas()) {
        if (dss.getName().equals(subschemaName)) {
          subschema = dss;
          break;
        }
      }

      if (subschema != null) {
        for (DataSchemaField dsf : subschema.getFields()) {
          if (dsf.getName().equals(reader.getValue())) {
            field = dsf;
          }
        }
      }
    }

    if (field == null) {
      LOG.warn("Cant unmarshall field " + reader.getValue());
    }

    return field;
  }
}
