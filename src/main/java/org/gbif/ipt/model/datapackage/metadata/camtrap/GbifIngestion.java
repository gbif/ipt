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

import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class GbifIngestion {

  @JsonDeserialize(using = ObservationLevel.ObservationLevelDeserializer.class)
  @JsonProperty("observationLevel")
  private ObservationLevel observationLevel = ObservationLevel.EVENT;

  @JsonProperty("observationLevel")
  public ObservationLevel getObservationLevel() {
    return observationLevel;
  }

  @JsonProperty("observationLevel")
  public void setObservationLevel(ObservationLevel observationLevel) {
    this.observationLevel = observationLevel;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    GbifIngestion that = (GbifIngestion) o;
    return Objects.equals(observationLevel, that.observationLevel);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(observationLevel);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GbifIngestion.class.getSimpleName() + "[", "]")
        .add("observationLevel='" + observationLevel + "'")
        .toString();
  }
}
