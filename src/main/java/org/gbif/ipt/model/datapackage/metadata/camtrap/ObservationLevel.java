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
package org.gbif.ipt.model.datapackage.metadata.camtrap;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public enum ObservationLevel {

  MEDIA("media"),
  EVENT("event");
  private final String value;
  public final static Map<String, ObservationLevel> CONSTANTS = new HashMap<>();
  public final static Map<String, String> VOCABULARY = new HashMap<>();

  static {
    for (ObservationLevel c : values()) {
      CONSTANTS.put(c.value, c);
      VOCABULARY.put(c.name(), c.value);
    }
  }

  ObservationLevel(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  @JsonCreator
  public static ObservationLevel fromValue(String value) {
    return CONSTANTS.get(value);
  }

  public static class ObservationLevelDeserializer extends JsonDeserializer<ObservationLevel> {
    @Override
    public ObservationLevel deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
      JsonNode node = jsonParser.readValueAsTree();
      return jsonParser.getCodec().treeToValue(node, ObservationLevel.class);
    }
  }

  public static class ObservationLevelSetDeserializer extends JsonDeserializer<Set<ObservationLevel>> {
    @SuppressWarnings("unchecked")
    @Override
    public Set<ObservationLevel> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      Set<String> values = parser.readValueAs(Set.class);
      Set<ObservationLevel> methods = new LinkedHashSet<>();
      for (String value : values) {
        ObservationLevel method = ObservationLevel.fromValue(value);
        if (method != null) {
          methods.add(method);
        }
      }

      // if some of the values were invalid return empty set
      return values.size() == methods.size() ? methods : new LinkedHashSet<>();
    }
  }
}

