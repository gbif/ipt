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

import org.gbif.ipt.validation.BasicMetadata;
import org.gbif.ipt.validation.InternalField;
import org.gbif.ipt.validation.KeywordsMetadata;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.opensymphony.xwork2.util.Element;

/**
 * Data Package
 * <p>
 * Data Package is a simple specification for data access and delivery.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPackageMetadata implements Serializable {

  private final static long serialVersionUID = 5948080618683312611L;

  /**
   * Profile
   * <p>
   * The profile of this descriptor.
   */
  @JsonProperty("profile")
  private String profile = "data-package";

  /**
   * Name
   * <p>
   * An identifier string. Lower case characters with `.`, `_`, `-` and `/` are allowed.
   */
  @JsonProperty("name")
  @Pattern(regexp = "^([-a-z0-9._/])+$", groups = BasicMetadata.class)
  private String name;

  /**
   * ID
   * <p>
   * A property reserved for globally unique identifiers. Examples of identifiers that are unique include UUIDs and DOIs.
   */
  @JsonProperty("id")
  private String id;

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @JsonProperty("title")
  private String title;

  /**
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @JsonProperty("description")
  private String description;

  /**
   * Home Page
   * <p>
   * The home on the web that is related to this data package.
   */
  @JsonProperty("homepage")
  private URI homepage;

  /**
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @JsonProperty("version")
  private Object version = "1.0";

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private Date created;

  /**
   * Contributors
   * <p>
   * The contributors to this descriptor.
   */
  @JsonProperty("contributors")
  @NotNull(message = "validation.input.required", groups = BasicMetadata.class)
  @Size(min = 1, message = "validation.datapackage.metadata.contributors.size", groups = BasicMetadata.class)
  @Valid
  private List<Contributor> contributors = new ArrayList<>();

  /**
   * Keywords
   * <p>
   * A list of keywords that describe this package.
   */
  @JsonProperty("keywords")
  @NotNull(message = "validation.input.notNull", groups = KeywordsMetadata.class)
  @Valid
  private List<String> keywords = new ArrayList<>();

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  private String image;

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  @NotNull(message = "validation.input.required", groups = BasicMetadata.class)
  @Size(min = 1, message = "validation.datapackage.metadata.licenses.size", groups = BasicMetadata.class)
  @Valid
  private List<License> licenses = new ArrayList<>();

  /**
   * Data Resources
   * <p>
   * An `array` of Data Resource objects, each compliant with the [Data Resource](/data-resource/) specification.
   * (Required)
   */
  @JsonProperty("resources")
  @NotNull(message = "validation.input.required", groups = InternalField.class)
  @Size(min = 1, message = "validation.datapackage.metadata.resources.size", groups = InternalField.class)
  @Valid
  private List<Resource> resources = new ArrayList<>();

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  @NotNull(message = "validation.input.notNull", groups = BasicMetadata.class)
  @Valid
  private List<Source> sources = new ArrayList<>();

  @SuppressWarnings("FieldMayBeFinal")
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
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @JsonProperty("version")
  public Object getVersion() {
    return version;
  }

  /**
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @JsonProperty("version")
  public void setVersion(Object version) {
    this.version = version;
  }

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  public Date getCreated() {
    return created;
  }

  /**
   * Created
   * <p>
   * The datetime on which this descriptor was created.
   */
  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
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
  @JsonDeserialize(contentUsing = DataPackageLicense.DataPackageLicenseDeserializer.class)
  @Element(DataPackageLicense.class)
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
  @JsonDeserialize(contentUsing = DataPackageSource.DataPackageSourceDeserializer.class)
  @Element(DataPackageResource.class)
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
  @JsonDeserialize(contentUsing = DataPackageSource.DataPackageSourceDeserializer.class)
  @Element(DataPackageSource.class)
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

  @Override
  public String toString() {
    return new StringJoiner(", ", DataPackageMetadata.class.getSimpleName() + "[", "]")
        .add("profile='" + profile + "'")
        .add("name='" + name + "'")
        .add("id='" + id + "'")
        .add("title='" + title + "'")
        .add("description='" + description + "'")
        .add("homepage=" + homepage)
        .add("version=" + version)
        .add("created=" + created)
        .add("contributors=" + contributors)
        .add("keywords=" + keywords)
        .add("image='" + image + "'")
        .add("licenses=" + licenses)
        .add("resources=" + resources)
        .add("sources=" + sources)
        .add("additionalProperties=" + additionalProperties)
        .toString();
  }
}
