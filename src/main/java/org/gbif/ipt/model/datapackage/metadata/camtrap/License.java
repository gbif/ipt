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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scope"
})
public class License implements Serializable {

  private final static long serialVersionUID = -6253983938937199264L;

  /**
   * Scope of the license. `data` applies to the content of the package and resources, `media` to the (locally or externally hosted) media files referenced in `media.filePath`.
   * (Required)
   */
  @JsonProperty("scope")
  @JsonPropertyDescription("Scope of the license. `data` applies to the content of the package and resources, `media` to the (locally or externally hosted) media files referenced in `media.filePath`.")
  @NotNull
  private License.Scope scope;

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Scope of the license. `data` applies to the content of the package and resources, `media` to the (locally or externally hosted) media files referenced in `media.filePath`.
   * (Required)
   */
  @JsonProperty("scope")
  public License.Scope getScope() {
    return scope;
  }

  /**
   * Scope of the license. `data` applies to the content of the package and resources, `media` to the (locally or externally hosted) media files referenced in `media.filePath`.
   * (Required)
   */
  @JsonProperty("scope")
  public void setScope(License.Scope scope) {
    this.scope = scope;
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
   * Scope of the license. `data` applies to the content of the package and resources, `media` to the (locally or externally hosted) media files referenced in `media.filePath`.
   */
  public enum Scope {

    DATA("data"),
    MEDIA("media");
    private final String value;
    private final static Map<String, License.Scope> CONSTANTS = new HashMap<String, License.Scope>();

    static {
      for (License.Scope c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Scope(String value) {
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
    public static License.Scope fromValue(String value) {
      License.Scope constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

}
