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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Temporal coverage of the package.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Temporal implements Serializable {

  private final static long serialVersionUID = 1939846711591134405L;

  /**
   * Start date of the first deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("start")
  @NotNull
  private String start;

  /**
   * End date of the last (completed) deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("end")
  @NotNull
  private String end;

  @SuppressWarnings("FieldMayBeFinal")
  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Start date of the first deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("start")
  public String getStart() {
    return start;
  }

  /**
   * Start date of the first deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("start")
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * End date of the last (completed) deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("end")
  public String getEnd() {
    return end;
  }

  /**
   * End date of the last (completed) deployment, as an ISO 8601 formatted string (`YYYY-MM-DD`).
   * (Required)
   */
  @JsonProperty("end")
  public void setEnd(String end) {
    this.end = end;
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
