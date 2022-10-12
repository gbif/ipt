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
package org.gbif.ipt.model.datapackage.metadata;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Data Package
 * <p>
 * Data Package is a simple specification for data access and delivery.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "profile",
    "name",
    "id",
    "title",
    "description",
    "homepage",
    "created",
    "contributors",
    "keywords",
    "image",
    "licenses",
    "resources",
    "sources"
})
public class DataPackageMetadata implements Serializable {

  private final static long serialVersionUID = 5948080618683312611L;

  /**
   * Profile
   * <p>
   * The profile of this descriptor.
   */
  @JsonProperty("profile")
  @JsonPropertyDescription("The profile of this descriptor.")
  private String profile = "data-package";

  /**
   * Name
   * <p>
   * An identifier string. Lower case characters with `.`, `_`, `-` and `/` are allowed.
   */
  @JsonProperty("name")
  @JsonPropertyDescription("An identifier string. Lower case characters with `.`, `_`, `-` and `/` are allowed.")
  @Pattern(regexp = "^([-a-z0-9._/])+$")
  private String name;

  /**
   * ID
   * <p>
   * A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.
   */
  @JsonProperty("id")
  @JsonPropertyDescription("A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.")
  private String id;

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @JsonProperty("title")
  @JsonPropertyDescription("A human-readable title.")
  private String title;

  /**
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @JsonProperty("description")
  @JsonPropertyDescription("A text description. Markdown is encouraged.")
  private String description;

  /**
   * Home Page
   * <p>
   * The home on the web that is related to this data package.
   */
  @JsonProperty("homepage")
  @JsonPropertyDescription("The home on the web that is related to this data package.")
  private URI homepage;

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  @JsonPropertyDescription("The datetime on which this descriptor was created.")
  private Date created;

  /**
   * Contributors
   * <p>
   * The contributors to this descriptor.
   */
  @JsonProperty("contributors")
  @JsonPropertyDescription("The contributors to this descriptor.")
  @Size(min = 1)
  @Valid
  private List<Contributor> contributors = null;

  /**
   * Keywords
   * <p>
   * A list of keywords that describe this package.
   */
  @JsonProperty("keywords")
  @JsonPropertyDescription("A list of keywords that describe this package.")
  @Size(min = 1)
  @Valid
  private List<String> keywords = null;

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  @JsonPropertyDescription("A image to represent this package.")
  private String image;

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  @JsonPropertyDescription("The license(s) under which this package is published.")
  @Size(min = 1)
  @Valid
  private List<License> licenses = null;

  /**
   * Data Resources
   * <p>
   * An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.
   * (Required)
   */
  @JsonProperty("resources")
  @JsonPropertyDescription("An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.")
  @Size(min = 1)
  @Valid
  @NotNull
  private List<Resource> resources = null;

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  @JsonPropertyDescription("The raw sources for this resource.")
  @Size()
  @Valid
  private List<Source> sources = null;

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Profile
   * <p>
   * The profile of this descriptor.
   */
  @JsonProperty("profile")
  public String getProfile() {
    return profile;
  }

  /**
   * Profile
   * <p>
   * The profile of this descriptor.
   */
  @JsonProperty("profile")
  public void setProfile(String profile) {
    this.profile = profile;
  }

  /**
   * Name
   * <p>
   * An identifier string. Lower case characters with `.`, `_`, `-` and `/` are allowed.
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  /**
   * Name
   * <p>
   * An identifier string. Lower case characters with `.`, `_`, `-` and `/` are allowed.
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * ID
   * <p>
   * A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.
   */
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  /**
   * ID
   * <p>
   * A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.
   */
  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  /**
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Home Page
   * <p>
   * The home on the web that is related to this data package.
   */
  @JsonProperty("homepage")
  public URI getHomepage() {
    return homepage;
  }

  /**
   * Home Page
   * <p>
   * The home on the web that is related to this data package.
   */
  @JsonProperty("homepage")
  public void setHomepage(URI homepage) {
    this.homepage = homepage;
  }

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  public Date getCreated() {
    return created;
  }

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * Contributors
   * <p>
   * The contributors to this descriptor.
   */
  @JsonProperty("contributors")
  public List<Contributor> getContributors() {
    return contributors;
  }

  /**
   * Contributors
   * <p>
   * The contributors to this descriptor.
   */
  @JsonProperty("contributors")
  public void setContributors(List<Contributor> contributors) {
    this.contributors = contributors;
  }

  /**
   * Keywords
   * <p>
   * A list of keywords that describe this package.
   */
  @JsonProperty("keywords")
  public List<String> getKeywords() {
    return keywords;
  }

  /**
   * Keywords
   * <p>
   * A list of keywords that describe this package.
   */
  @JsonProperty("keywords")
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  public String getImage() {
    return image;
  }

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  public void setImage(String image) {
    this.image = image;
  }

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  public List<License> getLicenses() {
    return licenses;
  }

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  public void setLicenses(List<License> licenses) {
    this.licenses = licenses;
  }

  /**
   * Data Resources
   * <p>
   * An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.
   * (Required)
   */
  @JsonProperty("resources")
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * Data Resources
   * <p>
   * An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.
   * (Required)
   */
  @JsonProperty("resources")
  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  public List<Source> getSources() {
    return sources;
  }

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  public void setSources(List<Source> sources) {
    this.sources = sources;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
