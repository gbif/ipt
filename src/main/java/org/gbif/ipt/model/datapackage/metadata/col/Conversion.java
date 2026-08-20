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
package org.gbif.ipt.model.datapackage.metadata.col;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "description",
    "url"
})
public class Conversion {

  @JsonProperty("description")
  private String description;

  @JsonProperty("url")
  private URI url;

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("url")
  public URI getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(URI url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Conversion that = (Conversion) o;
    return Objects.equals(description, that.description) && Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, url);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Conversion.class.getSimpleName() + "[", "]")
        .add("description='" + description + "'")
        .add("url=" + url)
        .toString();
  }
}
