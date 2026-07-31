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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * Minimal Table Schema field descriptor.
 * <p>
 * Does NOT have field-level type/format validation.
 * This is purely a data holder that (de)serializes to/from the spec's JSON shape.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPackageField {

  @Setter
  @Getter
  private String name;
  @Setter
  @Getter
  private String type;
  @Setter
  @Getter
  private String format;
  @Setter
  @Getter
  private String title;
  @Setter
  @Getter
  private String description;

  // Anything else the schema descriptor carries (constraints, rdfType, categories, ...)
  // is preserved verbatim on read and re-emitted on write, without needing a typed model for it.
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  public DataPackageField() {
  }

  public DataPackageField(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String key, Object value) {
    additionalProperties.put(key, value);
  }
}
