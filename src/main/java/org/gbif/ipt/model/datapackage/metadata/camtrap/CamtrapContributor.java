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

import org.gbif.ipt.model.datapackage.metadata.Contributor;
import org.gbif.ipt.model.datapackage.metadata.DataPackageContributor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CamtrapContributor extends DataPackageContributor {

  private static final long serialVersionUID = -8059939413339566278L;

  @Override
  @JsonProperty("role")
  @JsonDeserialize(using = CamtrapContributorRoleDeserializer.class)
  public String getRole() {
    return super.getRole();
  }

  public static class CamtrapContributorDeserializer extends JsonDeserializer<Contributor> {
    @Override
    public Contributor deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode node = jsonParser.readValueAsTree();
      return jsonParser.getCodec().treeToValue(node, CamtrapContributor.class);
    }
  }

  public static class CamtrapContributorRoleDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      String role = jsonParser.getValueAsString();

      if (role == null) {
        return null;
      }

      if (Role.CONSTANTS.get(role) != null) {
        return role;
      }

      throw new JsonParseException(jsonParser, "Invalid role: " + role);
    }
  }


  /**
   * Role of the contributor.
   */
  public enum Role {

    CONTACT("contact"),
    PRINCIPAL_INVESTIGATOR("principalInvestigator"),
    RIGHTS_HOLDER("rightsHolder"),
    PUBLISHER("publisher"),
    CONTRIBUTOR("contributor");

    private final String value;
    public final static Map<String, Role> CONSTANTS = new HashMap<>();
    public final static Map<String, String> VOCABULARY = new HashMap<>();

    static {
      for (Role r : values()) {
        CONSTANTS.put(r.value, r);
        VOCABULARY.put(r.name(), r.value);
      }
    }

    Role(String value) {
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
    public static Role fromValue(String value) {
      Role constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
