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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Data Resource
 * <p>
 * Data Resource.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "profile",
    "schema"
})
public class Resource implements Serializable {

  private final static long serialVersionUID = -5654049327007100865L;

  /**
   * Identifier of the resource.
   */
  @JsonProperty("name")
  @JsonPropertyDescription("Identifier of the resource.")
  private Resource.Name name;

  /**
   * (Required)
   */
  @JsonProperty("profile")
  @NotNull
  private Object profile;

  /**
   * (Required)
   */
  @JsonProperty("schema")
  @NotNull
  private Object schema;

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Identifier of the resource.
   */
  @JsonProperty("name")
  public Resource.Name getName() {
    return name;
  }

  /**
   * Identifier of the resource.
   */
  @JsonProperty("name")
  public void setName(Resource.Name name) {
    this.name = name;
  }

  /**
   * (Required)
   */
  @JsonProperty("profile")
  public Object getProfile() {
    return profile;
  }

  /**
   * (Required)
   */
  @JsonProperty("profile")
  public void setProfile(Object profile) {
    this.profile = profile;
  }

  /**
   * (Required)
   */
  @JsonProperty("schema")
  public Object getSchema() {
    return schema;
  }

  /**
   * (Required)
   */
  @JsonProperty("schema")
  public void setSchema(Object schema) {
    this.schema = schema;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }


  /**
   * Identifier of the resource.
   */
  public enum Name {

    DEPLOYMENTS("deployments"),
    MEDIA("media"),
    OBSERVATIONS("observations");
    private final String value;
    private final static Map<String, Resource.Name> CONSTANTS = new HashMap<>();

    static {
      for (Resource.Name c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Name(String value) {
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
    public static Resource.Name fromValue(String value) {
      Resource.Name constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

}
