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

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Camera trap project or study that originated the package.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title",
    "acronym",
    "description",
    "path",
    "samplingDesign",
    "captureMethod",
    "individualAnimals",
    "classificationLevel",
    "sequenceInterval"
})
public class Project implements Serializable {

  private final static long serialVersionUID = 926360715052617217L;

  /**
   * Unique identifier of the project.
   */
  @JsonProperty("id")
  @JsonPropertyDescription("Unique identifier of the project.")
  private String id;

  /**
   * Title of the project. Not to be confused with the title of the package (`package.title`).
   * (Required)
   */
  @JsonProperty("title")
  @JsonPropertyDescription("Title of the project. Not to be confused with the title of the package (`package.title`).")
  @NotNull
  private String title;

  /**
   * Project acronym.
   */
  @JsonProperty("acronym")
  @JsonPropertyDescription("Project acronym.")
  private String acronym;

  /**
   * Description of the project, ideally <a href="http://commonmark.org/">Markdown</a> formatted. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  @JsonPropertyDescription("Description of the project, ideally [Markdown](http://commonmark.org/) formatted. Not to be confused with the description of the package (`package.description`).")
  private String description;

  /**
   * Project website.
   */
  @JsonProperty("path")
  @JsonPropertyDescription("Project website.")
  private URI path;

  /**
   * Type of sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <a href="https://doi.org/10.13140/RG.2.2.23409.17767">10.13140/RG.2.2.23409.17767</a>), pages 80-82: `simple random`: random distribution of sampling locations; `systematic random`: random distribution of sampling locations, but arranged in a regular pattern; `clustered random`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   * (Required)
   */
  @JsonProperty("samplingDesign")
  @JsonPropertyDescription("Type of a sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <https://doi.org/10.13140/RG.2.2.23409.17767>), pages 80-82: `simple random`: random distribution of sampling locations; `systematic random`: random distribution of sampling locations, but arranged in a regular pattern; `clustered random`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).")
  @NotNull
  private Project.SamplingDesign samplingDesign;

  /**
   * Method(s) used to capture the media files.
   * (Required)
   */
  @JsonProperty("captureMethod")
  @JsonDeserialize(as = java.util.LinkedHashSet.class)
  @JsonPropertyDescription("Method(s) used to capture the media files.")
  @Size(min = 1)
  @Valid
  @NotNull
  private Set<CaptureMethod> captureMethod = null;

  /**
   * `true` if the project includes marked or recognizable individuals. See also `observations.individualID`.
   * (Required)
   */
  @JsonProperty("individualAnimals")
  @JsonPropertyDescription("`true` if the project includes marked or recognizable individuals. See also `observations.individualID`.")
  @NotNull
  private Boolean individualAnimals;

  /**
   * Information about a focal level of the classification process. `sequence`: classifications (i.e. rows in the `observations` resource) are provided at a sequence level (multiple media files can be part of one sequence); `media`: classifications are available for each single media file.
   * (Required)
   */
  @JsonProperty("classificationLevel")
  @JsonPropertyDescription("Information about a focal level of the classification process. `sequence`: classifications (i.e. rows in the `observations` resource) are provided at a sequence level (multiple media files can be part of one sequence); `media`: classifications are available for each single media file.")
  @NotNull
  private Project.ClassificationLevel classificationLevel;

  /**
   * Maximum number of seconds between timestamps of successive media files to be considered part of a single sequence and be assigned the same `media.sequenceID`.
   * (Required)
   */
  @JsonProperty("sequenceInterval")
  @JsonPropertyDescription("Maximum number of seconds between timestamps of successive media files to be considered part of a single sequence and be assigned the same `media.sequenceID`.")
  @NotNull
  private Integer sequenceInterval;

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
   * Description of the project, ideally <a href="http://commonmark.org/">Markdown</a> formatted. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  /**
   * Description of the project, ideally <a href="http://commonmark.org/">Markdown</a> formatted. Not to be confused with the description of the package (`package.description`).
   */
  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Project website.
   */
  @JsonProperty("path")
  public URI getPath() {
    return path;
  }

  /**
   * Project website.
   */
  @JsonProperty("path")
  public void setPath(URI path) {
    this.path = path;
  }

  /**
   * Type of a sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <a href="https://doi.org/10.13140/RG.2.2.23409.17767">10.13140/RG.2.2.23409.17767</a>), pages 80-82: `simple random`: random distribution of sampling locations; `systematic random`: random distribution of sampling locations, but arranged in a regular pattern; `clustered random`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   * (Required)
   */
  @JsonProperty("samplingDesign")
  public Project.SamplingDesign getSamplingDesign() {
    return samplingDesign;
  }

  /**
   * Type of a sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <a href="https://doi.org/10.13140/RG.2.2.23409.17767">10.13140/RG.2.2.23409.17767</a>), pages 80-82: `simple random`: random distribution of sampling locations; `systematic random`: random distribution of sampling locations, but arranged in a regular pattern; `clustered random`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
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

  /**
   * Information about a focal level of the classification process. `sequence`: classifications (i.e. rows in the `observations` resource) are provided at a sequence level (multiple media files can be part of one sequence); `media`: classifications are available for each single media file.
   * (Required)
   */
  @JsonProperty("classificationLevel")
  public Project.ClassificationLevel getClassificationLevel() {
    return classificationLevel;
  }

  /**
   * Information about a focal level of the classification process. `sequence`: classifications (i.e. rows in the `observations` resource) are provided at a sequence level (multiple media files can be part of one sequence); `media`: classifications are available for each single media file.
   * (Required)
   */
  @JsonProperty("classificationLevel")
  public void setClassificationLevel(Project.ClassificationLevel classificationLevel) {
    this.classificationLevel = classificationLevel;
  }

  /**
   * Maximum number of seconds between timestamps of successive media files to be considered part of a single sequence and be assigned the same `media.sequenceID`.
   * (Required)
   */
  @JsonProperty("sequenceInterval")
  public Integer getSequenceInterval() {
    return sequenceInterval;
  }

  /**
   * Maximum number of seconds between timestamps of successive media files to be considered part of a single sequence and be assigned the same `media.sequenceID`.
   * (Required)
   */
  @JsonProperty("sequenceInterval")
  public void setSequenceInterval(Integer sequenceInterval) {
    this.sequenceInterval = sequenceInterval;
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
   * Information about a focal level of the classification process. `sequence`: classifications (i.e. rows in the `observations` resource) are provided at a sequence level (multiple media files can be part of one sequence); `media`: classifications are available for each single media file.
   */
  public enum ClassificationLevel {

    SEQUENCE("sequence"),
    MEDIA("media");
    private final String value;
    private final static Map<String, Project.ClassificationLevel> CONSTANTS = new HashMap<>();

    static {
      for (Project.ClassificationLevel c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ClassificationLevel(String value) {
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
    public static Project.ClassificationLevel fromValue(String value) {
      Project.ClassificationLevel constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

  /**
   * Type of sampling design/layout. The values are based on Wearn & Glover-Kapfer (2017, <a href="https://doi.org/10.13140/RG.2.2.23409.17767">10.13140/RG.2.2.23409.17767</a>), pages 80-82: `simple random`: random distribution of sampling locations; `systematic random`: random distribution of sampling locations, but arranged in a regular pattern; `clustered random`: random distribution of sampling locations, but clustered in arrays; `experimental`: non-random distribution aimed to study an effect, including the before-after control-impact (BACI) design; `targeted`: non-random distribution optimized for capturing specific target species (often using various bait types); `opportunistic`: opportunistic camera trapping (usually with a small number of cameras).
   */
  public enum SamplingDesign {

    SIMPLE_RANDOM("simple random"),
    SYSTEMATIC_RANDOM("systematic random"),
    CLUSTERED_RANDOM("clustered random"),
    EXPERIMENTAL("experimental"),
    TARGETED("targeted"),
    OPPORTUNISTIC("opportunistic");
    private final String value;
    private final static Map<String, Project.SamplingDesign> CONSTANTS = new HashMap<>();

    static {
      for (Project.SamplingDesign c : values()) {
        CONSTANTS.put(c.value, c);
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
      Project.SamplingDesign constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

}
