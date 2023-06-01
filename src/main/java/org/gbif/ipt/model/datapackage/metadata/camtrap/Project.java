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

import org.gbif.ipt.validation.ProjectMetadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Camera trap project or study that originated the package.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project implements Serializable {

  private final static long serialVersionUID = 926360715052617217L;

  /**
   * Unique identifier of the project.
   */
  @JsonProperty("id")
  private String id;

  /**
   * Title of the project. Not to be confused with the title of the package (`package.title`).
   * (Required)
   */
  @JsonProperty("title")
  @NotNull(message = "validation.input.required", groups = ProjectMetadata.class)
  private String title;

  /**
   * Project acronym.
   */
  @JsonProperty("acronym")
  private String acronym;

  /**
   * Description of the project. Preferably formatted as <a href="http://commonmark.org/">Markdown</a>. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  private String description;

  /**
   * Project website.
   */
  @JsonProperty("path")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String path;

  /**
   * Type of a sampling design/layout. The values are based on <a href="https://doi.org/10.13140/RG.2.2.23409.17767">Wearn & Glover-Kapfer (2017)</a>, pages 80-82: `simpleRandom`: random distribution of sampling locations; `systematicRandom`: random distribution of sampling locations, but arranged in a regular pattern; `clusteredRandom`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   * (Required)
   */
  @JsonProperty("samplingDesign")
  @NotNull(message = "validation.input.required", groups = ProjectMetadata.class)
  private Project.SamplingDesign samplingDesign;

  /**
   * Method(s) used to capture the media files.
   * (Required)
   */
  @JsonProperty("captureMethod")
  @JsonDeserialize(using = CaptureMethod.CaptureMethodSetDeserializer.class)
  @NotNull(message = "validation.input.required", groups = ProjectMetadata.class)
  @Size(min = 1, message = "validation.camtrap.metadata.project.captureMethod.size", groups = ProjectMetadata.class)
  @Valid
  private Set<CaptureMethod> captureMethod = new LinkedHashSet<>();

  /**
   * Level at which observations are provided. See also `observations.observationLevel`.
   * (Required)
   */
  @JsonProperty("observationLevel")
  @JsonDeserialize(using = ObservationLevel.ObservationLevelSetDeserializer.class)
  @NotNull(message = "validation.input.required", groups = ProjectMetadata.class)
  @Size(min = 1, message = "validation.camtrap.metadata.project.observationLevel.size", groups = ProjectMetadata.class)
  @Valid
  private Set<ObservationLevel> observationLevel = new LinkedHashSet<>();

  /**
   * `true` if the project includes marked or recognizable individuals. See also `observations.individualID`.
   * (Required)
   */
  @JsonProperty("individualAnimals")
  @NotNull(message = "validation.input.required", groups = ProjectMetadata.class)
  private Boolean individualAnimals = false;

  @SuppressWarnings("FieldMayBeFinal")
  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Unique identifier of the project.
   */
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  /**
   * Unique identifier of the project.
   */
  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Title of the project. Not to be confused with the title of the package (`package.title`).
   * (Required)
   */
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  /**
   * Title of the project. Not to be confused with the title of the package (`package.title`).
   * (Required)
   */
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Project acronym.
   */
  @JsonProperty("acronym")
  public String getAcronym() {
    return acronym;
  }

  /**
   * Project acronym.
   */
  @JsonProperty("acronym")
  public void setAcronym(String acronym) {
    this.acronym = acronym;
  }

  /**
   * Description of the project. Preferably formatted as <a href="http://commonmark.org/">Markdown</a>. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  /**
   * Description of the project. Preferably formatted as <a href="http://commonmark.org/">Markdown</a>. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Project website.
   */
  @JsonProperty("path")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getPath() {
    return path;
  }

  /**
   * Project website.
   */
  @JsonProperty("path")
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Type of a sampling design/layout. The values are based on <a href="https://doi.org/10.13140/RG.2.2.23409.17767">Wearn & Glover-Kapfer (2017)</a>, pages 80-82: `simpleRandom`: random distribution of sampling locations; `systematicRandom`: random distribution of sampling locations, but arranged in a regular pattern; `clusteredRandom`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   * (Required)
   */
  @JsonProperty("samplingDesign")
  public Project.SamplingDesign getSamplingDesign() {
    return samplingDesign;
  }

  /**
   * Type of a sampling design/layout. The values are based on <a href="https://doi.org/10.13140/RG.2.2.23409.17767">Wearn & Glover-Kapfer (2017)</a>, pages 80-82: `simpleRandom`: random distribution of sampling locations; `systematicRandom`: random distribution of sampling locations, but arranged in a regular pattern; `clusteredRandom`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   * (Required)
   */
  @JsonProperty("samplingDesign")
  public void setSamplingDesign(Project.SamplingDesign samplingDesign) {
    this.samplingDesign = samplingDesign;
  }

  /**
   * Method(s) used to capture the media files.
   * (Required)
   */
  @JsonProperty("captureMethod")
  public Set<CaptureMethod> getCaptureMethod() {
    return captureMethod;
  }

  /**
   * Method(s) used to capture the media files.
   * (Required)
   */
  @JsonProperty("captureMethod")
  public void setCaptureMethod(Set<CaptureMethod> captureMethod) {
    this.captureMethod = captureMethod;
  }

  /**
   * Level at which observations are provided. See also `observations.observationLevel`.
   * (Required)
   */
  @JsonProperty("observationLevel")
  public Set<ObservationLevel> getObservationLevel() {
    return observationLevel;
  }

  /**
   * Level at which observations are provided. See also `observations.observationLevel`.
   * (Required)
   */
  @JsonProperty("observationLevel")
  public void setObservationLevel(Set<ObservationLevel> observationLevel) {
    this.observationLevel = observationLevel;
  }

  /**
   * `true` if the project includes marked or recognizable individuals. See also `observations.individualID`.
   * (Required)
   */
  @JsonProperty("individualAnimals")
  public Boolean getIndividualAnimals() {
    return individualAnimals;
  }

  /**
   * `true` if the project includes marked or recognizable individuals. See also `observations.individualID`.
   * (Required)
   */
  @JsonProperty("individualAnimals")
  public void setIndividualAnimals(Boolean individualAnimals) {
    this.individualAnimals = individualAnimals;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  /**
   * Type of sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <a href="https://doi.org/10.13140/RG.2.2.23409.17767">10.13140/RG.2.2.23409.17767</a>), pages 80-82: `simpleRandom`: random distribution of sampling locations; `systematicRandom`: random distribution of sampling locations, but arranged in a regular pattern; `clusteredRandom`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   */
  public enum SamplingDesign {

    SIMPLE_RANDOM("simpleRandom"),
    SYSTEMATIC_RANDOM("systematicRandom"),
    CLUSTERED_RANDOM("clusteredRandom"),
    EXPERIMENTAL("experimental"),
    TARGETED("targeted"),
    OPPORTUNISTIC("opportunistic");
    private final String value;
    public final static Map<String, Project.SamplingDesign> CONSTANTS = new HashMap<>();
    public final static Map<String, String> VOCABULARY = new LinkedHashMap<>();

    static {
      for (Project.SamplingDesign c : values()) {
        CONSTANTS.put(c.value, c);
        VOCABULARY.put(c.name(), c.value);
      }
    }

    SamplingDesign(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static Project.SamplingDesign fromValue(String value) {
      return CONSTANTS.get(value);
    }

  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Project.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("title='" + title + "'")
        .add("acronym='" + acronym + "'")
        .add("description='" + description + "'")
        .add("path=" + path)
        .add("samplingDesign=" + samplingDesign)
        .add("captureMethod=" + captureMethod)
        .add("observationLevel=" + observationLevel)
        .add("individualAnimals=" + individualAnimals)
        .add("additionalProperties=" + additionalProperties)
        .toString();
  }
}
