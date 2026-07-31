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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * Equivalent to io.frictionlessdata.datapackage.resource.FilebasedResource
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPackageResource {

  public static final String PROFILE_TABULAR_DATA_RESOURCE = "tabular-data-resource";
  public static final String FORMAT_CSV = "csv";

  @Setter
  @Getter
  private String name;
  @Setter
  @Getter
  private String profile = PROFILE_TABULAR_DATA_RESOURCE;
  @Setter
  @Getter
  private String format = FORMAT_CSV;
  @Setter
  @Getter
  private String encoding = "utf-8";

  // Paths are always relative to the package's base directory (dataPackageFolder),
  // matching FilebasedResource(name, files, baseDir).
  @Setter
  @Getter
  @JsonIgnore
  private List<String> paths = new ArrayList<>();

  /**
   * Attach a (possibly filtered, e.g. via getFields().removeIf(...)) schema to serialize inline.
   */
  @Setter
  @Getter
  @JsonIgnore
  private DataPackageTableSchema tableSchema;

  @JsonIgnore
  private String schemaRef;

  // true  -> emit "schema": { ...full object... }   (replaces setShouldSerializeFullSchema(true))
  // false -> emit "schema": "<schemaRef url>"        (replaces setShouldSerializeSchemaToFile(true), the default)
  @Setter
  @JsonIgnore
  private boolean inlineSchema = false;

  public DataPackageResource() {
  }

  public DataPackageResource(String name) {
    this.name = name;
  }

  /** Equivalent to `new FilebasedResource(name, Collections.singleton(new File(fn)), baseDir)`. */
  public static DataPackageResource fromFile(String name, String relativePath) {
    DataPackageResource r = new DataPackageResource(name);
    r.paths = Collections.singletonList(relativePath);
    return r;
  }

  /** Equivalent to the JSONBase.getOriginalReferences().put(JSON_KEY_SCHEMA, url) trick. */
  public void setSchemaUrl(String url) {
    this.schemaRef = url;
  }

  @JsonGetter("schema")
  private Object schemaForJson() {
    if (!inlineSchema && schemaRef != null) {
      return schemaRef;
    }
    return tableSchema;
  }

  @JsonGetter("path")
  private Object pathForJson() {
    if (paths == null || paths.isEmpty()) {
      return null;
    }
    return paths.size() == 1 ? paths.get(0) : paths;
  }
}
