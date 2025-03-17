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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.ipt.service.manage.JsonService;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.YamlService;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class MetadataReaderImpl implements MetadataReader {

  private final JsonService jsonService;
  private final YamlService yamlService;

  public MetadataReaderImpl(JsonService jsonService, YamlService yamlService) {
    this.jsonService = jsonService;
    this.yamlService = yamlService;
  }

  @Override
  public <T> T readValue(File src, Class<T> valueType) throws IOException {
    if (valueType == ColMetadata.class || valueType == FrictionlessColMetadata.class) {
      return yamlService.readValue(src, valueType);
    }
    return jsonService.readValue(src, valueType);
  }

  @Override
  public void writeValue(File resultFile, Object value) throws IOException {
    if (value instanceof ColMetadata || value instanceof FrictionlessColMetadata) {
      yamlService.writeValue(resultFile, value);
    } else {
      jsonService.writeValue(resultFile, value);
    }
  }

  @Override
  public void writeValue(Writer writer, Object value) throws IOException {
    if (value instanceof ColMetadata || value instanceof FrictionlessColMetadata) {
      yamlService.writeValue(writer, value);
    } else {
      jsonService.writeValue(writer, value);
    }
  }
}
