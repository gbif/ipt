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
package org.gbif.ipt.model.datapackage.metadata.col;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Person.
 * Generated from <a href="https://github.com/CatalogueOfLife/coldp/blob/master/metadata.json">JSON schema</a>.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = Person.PersonDeserializer.class, contentUsing = Person.PersonDeserializer.class)
@JsonSerialize(using = Person.PersonSerializer.class, contentUsing = Person.PersonSerializer.class)
@JsonPropertyOrder({
  "family",
  "given",
  "literal"
})
public class Person {

  @JsonProperty("family")
  private String family;

  @JsonProperty("given")
  private String given;

  @JsonProperty("literal")
  private String literal;

  @JsonProperty("family")
  public String getFamily() {
    return family;
  }

  @JsonProperty("family")
  public void setFamily(String family) {
    this.family = family;
  }

  @JsonProperty("given")
  public String getGiven() {
    return given;
  }

  @JsonProperty("given")
  public void setGiven(String given) {
    this.given = given;
  }

  @JsonProperty("literal")
  public String getLiteral() {
    return literal;
  }

  @JsonProperty("literal")
  public void setLiteral(String literal) {
    this.literal = literal;
  }

  /**
   * Custom deserializer. Person can be a regular string or an object.
   */
  public static class PersonDeserializer extends JsonDeserializer<Person> {
    @Override
    public Person deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode node = jsonParser.getCodec().readTree(jsonParser);

      Person result = new Person();

      if (node instanceof TextNode) {
        result.setLiteral(node.asText());
      } else {
        result.setLiteral(node.get("literal") != null ? node.get("literal").asText() : null);
        result.setFamily(node.get("family") != null ? node.get("family").asText() : null);
        result.setGiven(node.get("given") != null ? node.get("given").asText() : null);
      }

      return result;
    }
  }

  /**
   * Custom serializer. Person can be a regular string or an object.
   */
  public static class PersonSerializer extends JsonSerializer<Person> {

    @Override
    public void serialize(Person value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      if (value.getLiteral() != null) {
        jgen.writeString(value.getLiteral());
      } else {
        jgen.writeStartObject();
        if (value.getFamily() != null) {
          jgen.writeObjectField("family", value.getFamily());
        }
        if (value.getGiven() != null) {
          jgen.writeObjectField("given", value.getGiven());
        }
        jgen.writeEndObject();
      }
    }
  }

}
