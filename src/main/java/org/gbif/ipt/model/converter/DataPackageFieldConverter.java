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

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;

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
public class DataPackageFieldConverter implements Converter {

  private static final Logger LOG = LogManager.getLogger(DataPackageFieldConverter.class);
  private final DataPackageIdentifierConverter schemaConverter;
  private final TableSchemaNameConverter schemaNameConverter;

  @Inject
  public DataPackageFieldConverter(DataPackageIdentifierConverter schemaConverter,
                                   TableSchemaNameConverter schemaNameConverter) {
    this.schemaConverter = schemaConverter;
    this.schemaNameConverter = schemaNameConverter;
  }

  @Override
  public boolean canConvert(Class clazz) {
    return DataPackageField.class.isAssignableFrom(clazz);
  }

  @Override
  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataPackageField field = (DataPackageField) value;
    writer.setValue(field.getName());
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    DataPackageSchema schema = schemaConverter.getLastDataPackageConverted();
    String tableSchemaName = schemaNameConverter.getLastTableSchemaConverted();

    DataPackageField field = null;

    if (schema != null) {
      DataPackageTableSchema tableSchema = null;
      for (DataPackageTableSchema dss : schema.getTableSchemas()) {
        if (dss.getName().equals(tableSchemaName)) {
          tableSchema = dss;
          break;
        }
      }

      if (tableSchema != null) {
        for (DataPackageField dsf : tableSchema.getFields()) {
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
