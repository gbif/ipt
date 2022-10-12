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
package org.gbif.ipt.model.datapackage.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Contributor
 * <p>
 * A contributor to this descriptor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "path",
    "email",
    "organization",
    "role"
})
public class Contributor implements Serializable {

  private final static long serialVersionUID = -288140518286006582L;

  /**
   * Title
   * <p>
   * A human-readable title.
   * (Required)
   */
  @JsonProperty("title")
  @JsonPropertyDescription("A human-readable title.")
  @NotNull
  private String title;

  /**
   * Path
   * <p>
   * A fully qualified URL, or a POSIX file path.
   */
  @JsonProperty("path")
  @JsonPropertyDescription("A fully qualified URL, or a POSIX file path.")
  @Pattern(regexp = "^(?=^[^./~])(^((?!\\.{2}).)*$).*$")
  private String path;

  /**
   * Email
   * <p>
   * An email address.
   */
  @JsonProperty("email")
  @JsonPropertyDescription("An email address.")
  private String email;

  /**
   * Organization
   * <p>
   * An organizational affiliation for this contributor.
   */
  @JsonProperty("organization")
  @JsonPropertyDescription("An organizational affiliation for this contributor.")
  private String organization;

  @JsonProperty("role")
  private String role = "contributor";

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Title
   * <p>
   * A human-readable title.
   * (Required)
   */
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  /**
   * Title
   * <p>
   * A human-readable title.
   * (Required)
   */
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Path
   * <p>
   * A fully qualified URL, or a POSIX file path.
   */
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  /**
   * Path
   * <p>
   * A fully qualified URL, or a POSIX file path.
   */
  @JsonProperty("path")
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Email
   * <p>
   * An email address.
   */
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  /**
   * Email
   * <p>
   * An email address.
   */
  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Organization
   * <p>
   * An organizational affiliation for this contributor.
   */
  @JsonProperty("organization")
  public String getOrganization() {
    return organization;
  }

  /**
   * Organization
   * <p>
   * An organizational affiliation for this contributor.
   */
  @JsonProperty("organization")
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  @JsonProperty("role")
  public String getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(String role) {
    this.role = role;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
