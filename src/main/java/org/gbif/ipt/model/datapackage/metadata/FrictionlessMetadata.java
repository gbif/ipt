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
import org.gbif.ipt.validation.KeywordsMetadata;
import org.gbif.ipt.validation.ValidURI;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.apache.struts2.util.Element;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Frictionless metadata
 * <p>
 * Data Package is a simple specification for data access and delivery.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FrictionlessMetadata<C extends FrictionlessContributor, L extends FrictionlessLicense, S extends FrictionlessSource>
    implements DataPackageMetadata, Serializable {

  @Serial
  private final static long serialVersionUID = 5948080618683312611L;

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @JsonProperty("title")
  @NotNull(message = "validation.input.required", groups = BasicMetadata.class)
  private String title;

  /**
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @JsonProperty("version")
  private String version = "1.0";

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
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @JsonProperty("description")
  @NotNull(message = "validation.input.required", groups = BasicMetadata.class)
  private String description;

  /**
   * Home Page
   * <p>
   * The home on the web that is related to this data package.
   */
  @ValidURI(message = "validation.url.fullyQualified", groups = BasicMetadata.class)
  @JsonProperty("homepage")
  private URI homepage;

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
  @Valid
  private List<C> contributors = new ArrayList<>();

  /**
   * Keywords
   * <p>
   * A list of keywords that describe this package.
   */
  @JsonProperty("keywords")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @NotNull(message = "validation.input.notNull", groups = KeywordsMetadata.class)
  @Valid
  private List<String> keywords = new ArrayList<>();

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String image;

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  @NotNull(message = "validation.input.required", groups = BasicMetadata.class)
  @Valid
  private List<L> licenses = new ArrayList<>();

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  @NotNull(message = "validation.input.notNull", groups = BasicMetadata.class)
  @Valid
  private List<S> sources = new ArrayList<>();

  @SuppressWarnings("FieldMayBeFinal")
  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @Override
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  /**
   * Title
   * <p>
   * A human-readable title.
   */
  @Override
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @Override
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  /**
   * Version
   * <p>
   * A version string identifying the version of the package.
   */
  @Override
  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

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
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @Override
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  /**
   * Description
   * <p>
   * A text description. Markdown is encouraged.
   */
  @Override
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
  @Element(FrictionlessContributor.class)
  public List<C> getContributors() {
    return contributors;
  }

  /**
   * Contributors
   * <p>
   * The contributors to this descriptor.
   */
  @JsonProperty("contributors")
  public void setContributors(List<C> contributors) {
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
  @Override
  @JsonProperty("image")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getImage() {
    return image;
  }

  /**
   * Image
   * <p>
   * An image to represent this package.
   */
  @JsonProperty("image")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setImage(String image) {
    this.image = image;
  }

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  @JsonDeserialize(contentUsing = FrictionlessLicense.DataPackageLicenseDeserializer.class)
  @Element(FrictionlessLicense.class)
  public List<L> getLicenses() {
    return licenses;
  }

  /**
   * Licenses
   * <p>
   * The license(s) under which this package is published.
   */
  @JsonProperty("licenses")
  public void setLicenses(List<L> licenses) {
    this.licenses = licenses;
  }

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  @JsonDeserialize(contentUsing = FrictionlessSource.DataPackageSourceDeserializer.class)
  @Element(FrictionlessSource.class)
  public List<S> getSources() {
    return sources;
  }

  /**
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  public void setSources(List<S> sources) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FrictionlessMetadata that = (FrictionlessMetadata) o;
    return Objects.equals(title, that.title)
        && Objects.equals(version, that.version)
        && Objects.equals(profile, that.profile)
        && Objects.equals(name, that.name)
        && Objects.equals(id, that.id)
        && Objects.equals(description, that.description)
        && Objects.equals(homepage, that.homepage)
        && Objects.equals(created, that.created)
        && Objects.equals(contributors, that.contributors)
        && Objects.equals(keywords, that.keywords)
        && Objects.equals(image, that.image)
        && Objects.equals(licenses, that.licenses)
        && Objects.equals(sources, that.sources)
        && Objects.equals(additionalProperties, that.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, version, profile, name, id, description, homepage, created, contributors, keywords,
        image, licenses, sources, additionalProperties);
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
        .add("sources=" + sources)
        .add("additionalProperties=" + additionalProperties)
        .toString();
  }
}
