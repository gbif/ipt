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

import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single table schema of {@link DataPackageSchema}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageTableSchema implements Serializable {

  @Serial
  private static final long serialVersionUID = 3929428113035839253L;

  private String identifier;
  private URL url;

  private String name;
  private String title;
  private String description;
  // DwC-DP 1.0 calls it "notes"
  @SerializedName(value = "comments", alternate = {"notes"})
  private String comments;
  private String examples;
  private List<DataPackageField> fields;
  private String primaryKey;
  private List<DataPackageTableSchemaForeignKey> foreignKeys;

  // worked out on first use and kept: installed schemas are loaded once and shared by all resources
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private transient DwcDpTableGroup dwcDpGroup;

  /**
   * How the resource overview groups this table, if it's a Darwin Core Data Package table.
   */
  public DwcDpTableGroup getDwcDpGroup() {
    if (dwcDpGroup == null) {
      dwcDpGroup = DwcDpTableGroup.of(this);
    }
    return dwcDpGroup;
  }
}
