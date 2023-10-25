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
package org.gbif.ipt.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A field in schema file of {@link DataSchema}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSchemaField implements Serializable {

  private static final long serialVersionUID = 2049952968649028260L;

  private String name;
  private String type;
  private String format;
  private String description;
  private Object example;
  private DataSchemaFieldConstraints constraints;

  public String qualifiedName(String namespace) {
    return namespace + '/' + name;
  }
}
