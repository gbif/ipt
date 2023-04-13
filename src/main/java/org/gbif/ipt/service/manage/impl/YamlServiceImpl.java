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

import org.gbif.ipt.service.manage.YamlService;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Singleton;

@Singleton
public class YamlServiceImpl implements YamlService {

  private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

  @Override
  public <T> T readValue(File src, Class<T> valueType) throws IOException {
    return objectMapper.readValue(src, valueType);
  }

  @Override
  public void writeValue(File resultFile, Object value) throws IOException {
    objectMapper.writeValue(resultFile, value);
  }

  @Override
  public void writeValue(Writer writer, Object value) throws IOException {
    objectMapper.writeValue(writer, value);
  }
}
