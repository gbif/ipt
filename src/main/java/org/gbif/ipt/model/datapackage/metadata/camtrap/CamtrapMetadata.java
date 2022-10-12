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
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "resources",
    "profile",
    "name",
    "id",
    "created",
    "title",
    "contributors",
    "description",
    "version",
    "keywords",
    "image",
    "homepage",
    "sources",
    "licenses",
    "bibliographicCitation",
    "project",
    "coordinatePrecision",
    "spatial",
    "temporal",
    "taxonomic",
    "relatedIdentifiers",
    "references"
})
public class CamtrapMetadata implements Serializable {

  private final static long serialVersionUID = 7011607601336714408L;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#resource-information">Data Package specification</a>. Camtrap DP further requires each object to be a <a href="https://specs.frictionlessdata.io/tabular-data-resource/">Tabular Data Resource</a> with a specific `name` and `schema`. See <a href="../data">Data</a> for the requirements for those resources.
   */
  @JsonProperty("resources")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#resource-information). Camtrap DP further requires each object to be a [Tabular Data Resource](https://specs.frictionlessdata.io/tabular-data-resource/) with a specific `name` and `schema`. See [Data](../data) for the requirements for those resources.")
  @Size(min = 3, max = 3)
  @Valid
  private List<Resource> resources = null;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#profile">Data Package specification</a>. Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).
   * (Required)
   */
  @JsonProperty("profile")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#profile). Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).")
  @NotNull
  private URI profile;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#name">Data Package specification</a>.
   */
  @JsonProperty("name")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#name).")
  private Object name;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#id">Data Package specification</a>.
   */
  @JsonProperty("id")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#id).")
  private Object id;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#created">Data Package specification</a>. Camtrap DP makes this a required property.
   * (Required)
   */
  @JsonProperty("created")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#created). Camtrap DP makes this a required property.")
  @NotNull
  private Object created;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#title">Data Package specification</a>. Not to be confused with the title of the project that originated the package (`package.project.title`).
   */
  @JsonProperty("title")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#title). Not to be confused with the title of the project that originated the package (`package.project.title`).")
  private Object title;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#contributors">Data Package specification</a>. Camtrap DP makes this a required property. Can include people and organizations.
   * (Required)
   */
  @JsonProperty("contributors")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#contributors). Camtrap DP makes this a required property. Can include people and organizations.")
  @NotNull
  private Object contributors;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#description">Data Package specification</a>. Not to be confused with the description of the project that originated the package (`package.project.description`).
   */
  @JsonProperty("description")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#description). Not to be confused with the description of the project that originated the package (`package.project.description`).")
  private Object description;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#version">Data Package specification</a>.
   */
  @JsonProperty("version")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#version).")
  private Object version;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#keywords">Data Package specification</a>.
   */
  @JsonProperty("keywords")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#keywords).")
  private Object keywords;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#image">Data Package specification</a>.
   */
  @JsonProperty("image")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#image).")
  private Object image;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#homepage">Data Package specification</a>.
   */
  @JsonProperty("homepage")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#homepage).")
  private Object homepage;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#sources">Data Package specification</a>. Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).
   */
  @JsonProperty("sources")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#sources). Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).")
  @Valid
  private List<Source> sources = null;

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#licenses">Data Package specification</a>. If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.
   */
  @JsonProperty("licenses")
  @JsonPropertyDescription("See [Data Package specification](https://specs.frictionlessdata.io/data-package/#licenses). If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.")
  @Size(min = 2)
  @Valid
  private List<License> licenses = null;

  /**
   * Bibliographic/recommended citation for the package.
   */
  @JsonProperty("bibliographicCitation")
  @JsonPropertyDescription("Bibliographic/recommended citation for the package.")
  private String bibliographicCitation;

  /**
   * Camera trap project or study that originated the package.
   * (Required)
   */
  @JsonProperty("project")
  @JsonPropertyDescription("Camera trap project or study that originated the package.")
  @Valid
  @NotNull
  private Project project;

  /**
   * Least precise coordinate precision of the `deployments.latitude` and `deployments.longitude` (least precise in case of mixed precision, e.g. `0.01` for coordinates of precision of 0.01 and 0.001 degree). Especially relevant when coordinates have been rounded to protect sensitive species.
   */
  @JsonProperty("coordinatePrecision")
  @JsonPropertyDescription("Least precise coordinate precision of the `deployments.latitude` and `deployments.longitude` (least precise in case of mixed precision, e.g. `0.01` for coordinates of precision of 0.01 and 0.001 degree). Especially relevant when coordinates have been rounded to protect sensitive species.")
  private Double coordinatePrecision;

  /**
   * GeoJSON Object
   * <p>
   * This object represents a geometry, feature, or collection of features.
   * (Required)
   */
  @JsonProperty("spatial")
  @JsonPropertyDescription("This object represents a geometry, feature, or collection of features.")
  @Valid
  @NotNull
  private Geojson spatial;

  /**
   * Temporal coverage of the package.
   * (Required)
   */
  @JsonProperty("temporal")
  @JsonPropertyDescription("Temporal coverage of the package.")
  @Valid
  @NotNull
  private Temporal temporal;

  /**
   * Taxonomic coverage of the package, based on the unique `observations.scientificName`.
   * (Required)
   */
  @JsonProperty("taxonomic")
  @JsonPropertyDescription("Taxonomic coverage of the package, based on the unique `observations.scientificName`.")
  @Valid
  @NotNull
  private List<Taxonomic> taxonomic = null;

  /**
   * Identifiers of resources related to the package (e.g. papers, project pages, derived datasets, APIs, etc.).
   */
  @JsonProperty("relatedIdentifiers")
  @JsonPropertyDescription("Identifiers of resources related to the package (e.g. papers, project pages, derived datasets, APIs, etc.).")
  @Valid
  private List<RelatedIdentifier> relatedIdentifiers = null;

  /**
   * List of references related to the package (e.g. references cited in `package.project.description`). References ideally include a DOI.
   */
  @JsonProperty("references")
  @JsonPropertyDescription("List of references related to the package (e.g. references cited in `package.project.description`). References ideally include a DOI.")
  @Valid
  private List<String> references = null;

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#resource-information">Data Package specification</a>. Camtrap DP further requires each object to be a <a href="https://specs.frictionlessdata.io/tabular-data-resource/">Tabular Data Resource</a> with a specific `name` and `schema`. See [Data](../data) for the requirements for those resources.
   */
  @JsonProperty("resources")
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#resource-information">Data Package specification</a>. Camtrap DP further requires each object to be a <a href="https://specs.frictionlessdata.io/tabular-data-resource/">Tabular Data Resource</a> with a specific `name` and `schema`. See [Data](../data) for the requirements for those resources.
   */
  @JsonProperty("resources")
  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#profile">Data Package specification</a>. Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).
   * (Required)
   */
  @JsonProperty("profile")
  public URI getProfile() {
    return profile;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#profile">Data Package specification</a>. Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).
   * (Required)
   */
  @JsonProperty("profile")
  public void setProfile(URI profile) {
    this.profile = profile;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#name">Data Package specification</a>.
   */
  @JsonProperty("name")
  public Object getName() {
    return name;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#name">Data Package specification</a>.
   */
  @JsonProperty("name")
  public void setName(Object name) {
    this.name = name;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#id">Data Package specification</a>.
   */
  @JsonProperty("id")
  public Object getId() {
    return id;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#id">Data Package specification</a>.
   */
  @JsonProperty("id")
  public void setId(Object id) {
    this.id = id;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#created">Data Package specification</a>. Camtrap DP makes this a required property.
   * (Required)
   */
  @JsonProperty("created")
  public Object getCreated() {
    return created;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#created">Data Package specification</a>. Camtrap DP makes this a required property.
   * (Required)
   */
  @JsonProperty("created")
  public void setCreated(Object created) {
    this.created = created;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#title">Data Package specification</a>. Not to be confused with the title of the project that originated the package (`package.project.title`).
   */
  @JsonProperty("title")
  public Object getTitle() {
    return title;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#title">Data Package specification</a>. Not to be confused with the title of the project that originated the package (`package.project.title`).
   */
  @JsonProperty("title")
  public void setTitle(Object title) {
    this.title = title;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#contributors">Data Package specification</a>. Camtrap DP makes this a required property. Can include people and organizations.
   * (Required)
   */
  @JsonProperty("contributors")
  public Object getContributors() {
    return contributors;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#contributors">Data Package specification</a>. Camtrap DP makes this a required property. Can include people and organizations.
   * (Required)
   */
  @JsonProperty("contributors")
  public void setContributors(Object contributors) {
    this.contributors = contributors;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#description">Data Package specification</a>. Not to be confused with the description of the project that originated the package (`package.project.description`).
   */
  @JsonProperty("description")
  public Object getDescription() {
    return description;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#description">Data Package specification</a>. Not to be confused with the description of the project that originated the package (`package.project.description`).
   */
  @JsonProperty("description")
  public void setDescription(Object description) {
    this.description = description;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#version">Data Package specification</a>.
   */
  @JsonProperty("version")
  public Object getVersion() {
    return version;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#version">Data Package specification</a>.
   */
  @JsonProperty("version")
  public void setVersion(Object version) {
    this.version = version;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#keywords">Data Package specification</a>.
   */
  @JsonProperty("keywords")
  public Object getKeywords() {
    return keywords;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#keywords">Data Package specification</a>.
   */
  @JsonProperty("keywords")
  public void setKeywords(Object keywords) {
    this.keywords = keywords;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#image">Data Package specification</a>.
   */
  @JsonProperty("image")
  public Object getImage() {
    return image;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#image">Data Package specification</a>.
   */
  @JsonProperty("image")
  public void setImage(Object image) {
    this.image = image;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#homepage">Data Package specification</a>.
   */
  @JsonProperty("homepage")
  public Object getHomepage() {
    return homepage;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#homepage">Data Package specification</a>.
   */
  @JsonProperty("homepage")
  public void setHomepage(Object homepage) {
    this.homepage = homepage;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#sources">Data Package specification</a>. Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).
   */
  @JsonProperty("sources")
  public List<Source> getSources() {
    return sources;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#sources">Data Package specification</a>. Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).
   */
  @JsonProperty("sources")
  public void setSources(List<Source> sources) {
    this.sources = sources;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#licenses">Data Package specification</a>. If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.
   */
  @JsonProperty("licenses")
  public List<License> getLicenses() {
    return licenses;
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#licenses">Data Package specification</a>. If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.
   */
  @JsonProperty("licenses")
  public void setLicenses(List<License> licenses) {
    this.licenses = licenses;
  }

  /**
   * Bibliographic/recommended citation for the package.
   */
  @JsonProperty("bibliographicCitation")
  public String getBibliographicCitation() {
    return bibliographicCitation;
  }

  /**
   * Bibliographic/recommended citation for the package.
   */
  @JsonProperty("bibliographicCitation")
  public void setBibliographicCitation(String bibliographicCitation) {
    this.bibliographicCitation = bibliographicCitation;
  }

  /**
   * Camera trap project or study that originated the package.
   * (Required)
   */
  @JsonProperty("project")
  public Project getProject() {
    return project;
  }

  /**
   * Camera trap project or study that originated the package.
   * (Required)
   */
  @JsonProperty("project")
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Least precise coordinate precision of the `deployments.latitude` and `deployments.longitude` (least precise in case of mixed precision, e.g. `0.01` for coordinates of precision of 0.01 and 0.001 degree). Especially relevant when coordinates have been rounded to protect sensitive species.
   */
  @JsonProperty("coordinatePrecision")
  public Double getCoordinatePrecision() {
    return coordinatePrecision;
  }

  /**
   * Least precise coordinate precision of the `deployments.latitude` and `deployments.longitude` (least precise in case of mixed precision, e.g. `0.01` for coordinates of precision of 0.01 and 0.001 degree). Especially relevant when coordinates have been rounded to protect sensitive species.
   */
  @JsonProperty("coordinatePrecision")
  public void setCoordinatePrecision(Double coordinatePrecision) {
    this.coordinatePrecision = coordinatePrecision;
  }

  /**
   * GeoJSON Object
   * <p>
   * This object represents a geometry, feature, or collection of features.
   * (Required)
   */
  @JsonProperty("spatial")
  public Geojson getSpatial() {
    return spatial;
  }

  /**
   * GeoJSON Object
   * <p>
   * This object represents a geometry, feature, or collection of features.
   * (Required)
   */
  @JsonProperty("spatial")
  public void setSpatial(Geojson spatial) {
    this.spatial = spatial;
  }

  /**
   * Temporal coverage of the package.
   * (Required)
   */
  @JsonProperty("temporal")
  public Temporal getTemporal() {
    return temporal;
  }

  /**
   * Temporal coverage of the package.
   * (Required)
   */
  @JsonProperty("temporal")
  public void setTemporal(Temporal temporal) {
    this.temporal = temporal;
  }

  /**
   * Taxonomic coverage of the package, based on the unique `observations.scientificName`.
   * (Required)
   */
  @JsonProperty("taxonomic")
  public List<Taxonomic> getTaxonomic() {
    return taxonomic;
  }

  /**
   * Taxonomic coverage of the package, based on the unique `observations.scientificName`.
   * (Required)
   */
  @JsonProperty("taxonomic")
  public void setTaxonomic(List<Taxonomic> taxonomic) {
    this.taxonomic = taxonomic;
  }

  /**
   * Identifiers of resources related to the package (e.g. papers, project pages, derived datasets, APIs, etc.).
   */
  @JsonProperty("relatedIdentifiers")
  public List<RelatedIdentifier> getRelatedIdentifiers() {
    return relatedIdentifiers;
  }

  /**
   * Identifiers of resources related to the package (e.g. papers, project pages, derived datasets, APIs, etc.).
   */
  @JsonProperty("relatedIdentifiers")
  public void setRelatedIdentifiers(List<RelatedIdentifier> relatedIdentifiers) {
    this.relatedIdentifiers = relatedIdentifiers;
  }

  /**
   * List of references related to the package (e.g. references cited in `package.project.description`). References ideally include a DOI.
   */
  @JsonProperty("references")
  public List<String> getReferences() {
    return references;
  }

  /**
   * List of references related to the package (e.g. references cited in `package.project.description`). References ideally include a DOI.
   */
  @JsonProperty("references")
  public void setReferences(List<String> references) {
    this.references = references;
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
