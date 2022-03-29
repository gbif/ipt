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
import java.util.Objects;
import java.util.StringJoiner;

public class DataSubschemaForeignKey implements Serializable {

  private static final long serialVersionUID = 5858994392171274432L;

  private String fields;
  private DataSchemaFieldReference reference;

  public String getFields() {
    return fields;
  }

  public void setFields(String fields) {
    this.fields = fields;
  }

  public DataSchemaFieldReference getReference() {
    return reference;
  }

  public void setReference(DataSchemaFieldReference reference) {
    this.reference = reference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSubschemaForeignKey that = (DataSubschemaForeignKey) o;
    return Objects.equals(fields, that.fields) && Objects.equals(reference, that.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fields, reference);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSubschemaForeignKey.class.getSimpleName() + "[", "]")
        .add("fields='" + fields + "'")
        .add("reference=" + reference)
        .toString();
  }
}
