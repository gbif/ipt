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

import org.gbif.ipt.model.datapackage.metadata.DataPackageSource;
import org.gbif.ipt.model.datapackage.metadata.Source;

import java.io.IOException;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CamtrapSource extends DataPackageSource {

  private final static long serialVersionUID = -86921756591701358L;

  /**
   * Version of the source.
   */
  @JsonProperty("version")
  private String version;

  /**
   * Version of the source.
   */
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  /**
   * Version of the source.
   */
  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  public static class CamtrapSourceDeserializer extends JsonDeserializer<Source> {
    @Override
    public Source deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode node = jsonParser.readValueAsTree();
      return jsonParser.getCodec().treeToValue(node, CamtrapSource.class);
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CamtrapSource.class.getSimpleName() + "[", "]")
        .add("title='" + super.getTitle() + "'")
        .add("path='" + super.getPath() + "'")
        .add("name='" + super.getEmail() + "'")
        .add("scope=" + version)
        .add("additionalProperties=" + super.getAdditionalProperties())
        .toString();
  }
}
