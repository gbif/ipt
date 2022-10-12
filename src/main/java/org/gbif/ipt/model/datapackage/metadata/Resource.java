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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
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
 * Data Resource
 * <p>
 * Data Resource.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "profile",
    "name",
    "path",
    "data",
    "schema",
    "title",
    "description",
    "homepage",
    "sources",
    "licenses",
    "format",
    "mediatype",
    "encoding",
    "bytes",
    "hash"
})
public class Resource implements Serializable {

  private final static long serialVersionUID = -7790507710447205789L;

  /**
   * Profile
   * <p>
   * The profile of this descriptor.
   */
  @JsonProperty("profile")
  @JsonPropertyDescription("The profile of this descriptor.")
  private String profile = "data-resource";

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
   * Path
   * <p>
   * A reference to the data for this resource, as either a path as a string, or an array of paths as strings. of valid URIs.
   */
  @JsonProperty("path")
  @JsonPropertyDescription("A reference to the data for this resource, as either a path as a string, or an array of paths as strings. of valid URIs.")
  private Object path;

  /**
   * Data
   * <p>
   * Inline data for this resource.
   */
  @JsonProperty("data")
  @JsonPropertyDescription("Inline data for this resource.")
  private Object data;

  /**
   * Schema
   * <p>
   * A schema for this resource.
   */
  @JsonProperty("schema")
  @JsonPropertyDescription("A schema for this resource.")
  private String schema;

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
   * Sources
   * <p>
   * The raw sources for this resource.
   */
  @JsonProperty("sources")
  @JsonPropertyDescription("The raw sources for this resource.")
  @Size()
  @Valid
  private List<Source> sources = null;

  /**
   * Licenses
   * <p>
   * The license(s) under which the resource is published.
   */
  @JsonProperty("licenses")
  @JsonPropertyDescription("The license(s) under which the resource is published.")
  @Size(min = 1)
  @Valid
  private List<License> licenses = null;

  /**
   * Format
   * <p>
   * The file format of this resource.
   */
  @JsonProperty("format")
  @JsonPropertyDescription("The file format of this resource.")
  private String format;

  /**
   * Media Type
   * <p>
   * The media type of this resource. Can be any valid media type listed with <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>.
   */
  @JsonProperty("mediatype")
  @JsonPropertyDescription("The media type of this resource. Can be any valid media type listed with [IANA](https://www.iana.org/assignments/media-types/media-types.xhtml).")
  @Pattern(regexp = "^(.+)/(.+)$")
  private String mediatype;

  /**
   * Encoding
   * <p>
   * The file encoding of this resource.
   */
  @JsonProperty("encoding")
  @JsonPropertyDescription("The file encoding of this resource.")
  private String encoding = "utf-8";

  /**
   * Bytes
   * <p>
   * The size of this resource in bytes.
   */
  @JsonProperty("bytes")
  @JsonPropertyDescription("The size of this resource in bytes.")
  private Integer bytes;

  /**
   * Hash
   * <p>
   * The MD5 hash of this resource. Indicate other hashing algorithms with the {algorithm}:{hash} format.
   */
  @JsonProperty("hash")
  @JsonPropertyDescription("The MD5 hash of this resource. Indicate other hashing algorithms with the {algorithm}:{hash} format.")
  @Pattern(regexp = "^([^:]+:[a-fA-F0-9]+|[a-fA-F0-9]{32}|)$")
  private String hash;

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
   * Path
   * <p>
   * A reference to the data for this resource, as either a path as a string, or an array of paths as strings. of valid URIs.
   */
  @JsonProperty("path")
  public Object getPath() {
    return path;
  }

  /**
   * Path
   * <p>
   * A reference to the data for this resource, as either a path as a string, or an array of paths as strings. of valid URIs.
   */
  @JsonProperty("path")
  public void setPath(Object path) {
    this.path = path;
  }

  /**
   * Data
   * <p>
   * Inline data for this resource.
   */
  @JsonProperty("data")
  public Object getData() {
    return data;
  }

  /**
   * Data
   * <p>
   * Inline data for this resource.
   */
  @JsonProperty("data")
  public void setData(Object data) {
    this.data = data;
  }

  /**
   * Schema
   * <p>
   * A schema for this resource.
   */
  @JsonProperty("schema")
  public String getSchema() {
    return schema;
  }

  /**
   * Schema
   * <p>
   * A schema for this resource.
   */
  @JsonProperty("schema")
  public void setSchema(String schema) {
    this.schema = schema;
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

  /**
   * Licenses
   * <p>
   * The license(s) under which the resource is published.
   */
  @JsonProperty("licenses")
  public List<License> getLicenses() {
    return licenses;
  }

  /**
   * Licenses
   * <p>
   * The license(s) under which the resource is published.
   */
  @JsonProperty("licenses")
  public void setLicenses(List<License> licenses) {
    this.licenses = licenses;
  }

  /**
   * Format
   * <p>
   * The file format of this resource.
   */
  @JsonProperty("format")
  public String getFormat() {
    return format;
  }

  /**
   * Format
   * <p>
   * The file format of this resource.
   */
  @JsonProperty("format")
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Media Type
   * <p>
   * The media type of this resource. Can be any valid media type listed with <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>.
   */
  @JsonProperty("mediatype")
  public String getMediatype() {
    return mediatype;
  }

  /**
   * Media Type
   * <p>
   * The media type of this resource. Can be any valid media type listed with <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>.
   */
  @JsonProperty("mediatype")
  public void setMediatype(String mediatype) {
    this.mediatype = mediatype;
  }

  /**
   * Encoding
   * <p>
   * The file encoding of this resource.
   */
  @JsonProperty("encoding")
  public String getEncoding() {
    return encoding;
  }

  /**
   * Encoding
   * <p>
   * The file encoding of this resource.
   */
  @JsonProperty("encoding")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Bytes
   * <p>
   * The size of this resource in bytes.
   */
  @JsonProperty("bytes")
  public Integer getBytes() {
    return bytes;
  }

  /**
   * Bytes
   * <p>
   * The size of this resource in bytes.
   */
  @JsonProperty("bytes")
  public void setBytes(Integer bytes) {
    this.bytes = bytes;
  }

  /**
   * Hash
   * <p>
   * The MD5 hash of this resource. Indicate other hashing algorithms with the {algorithm}:{hash} format.
   */
  @JsonProperty("hash")
  public String getHash() {
    return hash;
  }

  /**
   * Hash
   * <p>
   * The MD5 hash of this resource. Indicate other hashing algorithms with the {algorithm}:{hash} format.
   */
  @JsonProperty("hash")
  public void setHash(String hash) {
    this.hash = hash;
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
