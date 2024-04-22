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

import org.gbif.ipt.service.manage.JsonService;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Singleton;

@Singleton
public class JsonServiceImpl implements JsonService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CustomPrettyPrinter prettyPrinter = new CustomPrettyPrinter();

  {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public <T> T readValue(File src, Class<T> valueType) throws IOException {
    return objectMapper.readValue(src, valueType);
  }

  @Override
  public void writeValue(File resultFile, Object value) throws IOException {
    objectMapper.writer(prettyPrinter).writeValue(resultFile, value);
  }

  @Override
  public void writeValue(Writer writer, Object value) throws IOException {
    objectMapper.writer(prettyPrinter).writeValue(writer, value);
  }

  // Custom pretty printer for better JSON formatting
  static class CustomPrettyPrinter implements PrettyPrinter {
    private static final String INDENT = "  ";
    private int level = 0;

    @Override
    public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
      gen.writeRaw("\n");
    }

    @Override
    public void writeStartObject(JsonGenerator gen) throws IOException {
      gen.writeRaw("{\n");
      level++;
    }

    @Override
    public void writeEndObject(JsonGenerator gen, int numValues) throws IOException {
      level--;
      gen.writeRaw("\n");
      indent(gen);
      gen.writeRaw("}");
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
      gen.writeRaw(",\n");
      indent(gen);
    }

    @Override
    public void writeStartArray(JsonGenerator gen) throws IOException {
      gen.writeRaw("[\n");
      level++;
    }

    @Override
    public void writeEndArray(JsonGenerator gen, int numValues) throws IOException {
      level--;
      gen.writeRaw("\n");
      indent(gen);
      gen.writeRaw("]");
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
      gen.writeRaw(",\n");
      indent(gen);
    }

    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException {
      indent(gen);
    }

    @Override
    public void beforeObjectEntries(JsonGenerator gen) throws IOException {
      indent(gen);
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
      gen.writeRaw(": ");
    }

    private void indent(JsonGenerator gen) throws IOException {
      for (int i = 0; i < level; i++) {
        gen.writeRaw(INDENT);
      }
    }
  }
}
