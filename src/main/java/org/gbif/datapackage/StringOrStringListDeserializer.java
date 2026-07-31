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
package org.gbif.datapackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StringOrStringListDeserializer extends JsonDeserializer<List<String>> {

  @Override
  public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonToken token = p.currentToken();

    if (token == JsonToken.VALUE_STRING) {
      return Collections.singletonList(p.getValueAsString());
    }

    if (token == JsonToken.START_ARRAY) {
      List<String> result = new ArrayList<>();

      while (p.nextToken() != JsonToken.END_ARRAY) {
        if (p.currentToken() != JsonToken.VALUE_STRING) {
          ctxt.reportInputMismatch(
              handledType(),
              "Expected array of strings but found %s",
              p.currentToken()
          );
        }
        result.add(p.getValueAsString());
      }

      return result;
    }

    if (token == JsonToken.VALUE_NULL) {
      return null;
    }

    return ctxt.reportInputMismatch(
        handledType(),
        "Expected a string or an array of strings but found %s",
        token
    );
  }
}
