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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;
import lombok.Setter;

/**
 * Equivalent to io.frictionlessdata.tableschema.schema.Schema.
 * <p>
 * Field lists are plain mutable ArrayLists on purpose, needed for fields filtering (DwC-DP).
 * <p>
 * Table Schema descriptors may carry additional schema-level metadata beyond fields/
 * primaryKey/foreignKeys, e.g. DwC-DP's schemas include "identifier", and additionalProperties may
 * include "title"/"description". These are preserved (not just tolerated) via the
 * any-getter/setter below, so they round-trip if the schema ends up serialized inline.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPackageTableSchema {

  @Setter
  @Getter
  private List<DataPackageField> fields = new ArrayList<>();
  @Setter
  @Getter
  @JsonDeserialize(using = StringOrStringListDeserializer.class)
  private List<String> primaryKey = new ArrayList<>();
  @Setter
  @Getter
  private List<DataPackageForeignKey> foreignKeys = new ArrayList<>();
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Equivalent to Schema.fromJson(url, strict). */
  public static DataPackageTableSchema fromUrl(URL url) throws IOException {
    return MAPPER.readValue(url, DataPackageTableSchema.class);
  }

  public static DataPackageTableSchema fromFile(File file) throws IOException {
    return MAPPER.readValue(file, DataPackageTableSchema.class);
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

