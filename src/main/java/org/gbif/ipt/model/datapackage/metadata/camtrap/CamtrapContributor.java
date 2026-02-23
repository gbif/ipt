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

import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;

import java.io.IOException;
import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.gbif.ipt.validation.BasicMetadata;
import org.gbif.ipt.validation.NotNullLastNameForPeopleContributors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NotNullLastNameForPeopleContributors(message = "validation.input.required", groups = BasicMetadata.class)
public class CamtrapContributor extends FrictionlessContributor {

  @Serial
  private static final long serialVersionUID = -8059939413339566278L;

  /**
   * First name
   * <p>
   * First name of the contributor.
   */
  @JsonProperty("firstName")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String firstName;

  /**
   * Last name
   * <p>
   * Last name of the contributor.
   */
  @JsonProperty("lastName")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String lastName;

  @Override
  @JsonProperty("role")
  @JsonDeserialize(using = CamtrapContributorRoleDeserializer.class)
  public String getRole() {
    return super.getRole();
  }

  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  @JsonProperty("firstName")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  @JsonProperty("lastName")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CamtrapContributor.class.getSimpleName() + "[", "]")
        .add("title='" + super.getTitle() + "'")
        .add("path='" + super.getPath() + "'")
        .add("email='" + super.getEmail() + "'")
        .add("organization='" + super.getOrganization() + "'")
        .add("role='" + super.getRole() + "'")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("additionalProperties=" + super.getAdditionalProperties())
        .toString();
  }

  public static class CamtrapContributorDeserializer extends JsonDeserializer<CamtrapContributor> {
    @Override
    public CamtrapContributor deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
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

      // de-serialize unknown values as NULLs
      return null;
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
    public final static List<String> CITATION_ROLES = Arrays.asList(CONTACT.value(), PRINCIPAL_INVESTIGATOR.value(), CONTRIBUTOR.value);
    public final static Map<String, Role> CONSTANTS = new HashMap<>();
    public final static Map<String, String> VOCABULARY = new LinkedHashMap<>();

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

    public String value() {
      return this.value;
    }

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
