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

public enum CaptureMethod {

  MOTION_DETECTION("motionDetection"),
  TIME_LAPSE("timeLapse");
  private final String value;
  public final static Map<String, CaptureMethod> CONSTANTS = new HashMap<>();
  public final static Map<String, String> VOCABULARY = new HashMap<>();

  static {
    for (CaptureMethod c : values()) {
      CONSTANTS.put(c.value, c);
      VOCABULARY.put(c.name(), c.value);
    }
  }

  CaptureMethod(String value) {
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
  public static CaptureMethod fromValue(String value) {
    return CONSTANTS.get(value);
  }

  public static class CaptureMethodSetDeserializer extends JsonDeserializer<Set<CaptureMethod>> {
    @SuppressWarnings("unchecked")
    @Override
    public Set<CaptureMethod> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      Set<String> values = parser.readValueAs(Set.class);
      Set<CaptureMethod> methods = new LinkedHashSet<>();
      for (String value : values) {
        CaptureMethod method = CaptureMethod.fromValue(value);
        if (method != null) {
          methods.add(method);
        }
      }

      // if some of the values were invalid return empty set
      return values.size() == methods.size() ? methods : new LinkedHashSet<>();
    }
  }
}

