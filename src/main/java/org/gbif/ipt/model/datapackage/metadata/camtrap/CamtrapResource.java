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

import org.gbif.ipt.model.datapackage.metadata.Resource;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Data Resource
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CamtrapResource extends Resource {

  private final static long serialVersionUID = -5654049327007100865L;

  /**
   * Identifier of the resource.
   */
  @Override
  @JsonProperty("name")
  public CamtrapResource.Name getName() {
    return ((Name) super.getName());
  }

  /**
   * Identifier of the resource.
   */
  @JsonProperty("name")
  public void setName(CamtrapResource.Name name) {
    super.setName(name);
  }

  /**
   * (Required)
   */
  @Override
  @JsonProperty("profile")
  @NotNull
  public String getProfile() {
    return super.getProfile();
  }

  /**
   * (Required)
   */
  @Override
  @JsonProperty("profile")
  public void setProfile(String profile) {
    super.setProfile(profile);
  }

  /**
   * (Required)
   */
  @Override
  @JsonProperty("schema")
  @NotNull
  public String getSchema() {
    return super.getSchema();
  }

  /**
   * (Required)
   */
  @Override
  @JsonProperty("schema")
  public void setSchema(String schema) {
    super.setSchema(schema);
  }

  /**
   * Identifier of the resource.
   */
  public enum Name implements CharSequence {

    DEPLOYMENTS("deployments"),
    MEDIA("media"),
    OBSERVATIONS("observations");
    private final String value;
    private final static Map<String, CamtrapResource.Name> CONSTANTS = new HashMap<>();

    static {
      for (CamtrapResource.Name c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Name(String value) {
      this.value = value;
    }

    @Override
    public int length() {
      return this.value.length();
    }

    @Override
    public char charAt(int index) {
      return this.value.charAt(index);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CharSequence subSequence(int start, int end) {
      return this.value.subSequence(start, end);
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
    public static CamtrapResource.Name fromValue(String value) {
      CamtrapResource.Name constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

}
