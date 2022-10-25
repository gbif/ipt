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

import org.gbif.ipt.model.datapackage.metadata.Contributor;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.License;
import org.gbif.ipt.model.datapackage.metadata.Resource;
import org.gbif.ipt.model.datapackage.metadata.Source;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CamtrapMetadata extends DataPackageMetadata {

  private final static long serialVersionUID = 7011607601336714408L;

  /**
   * Bibliographic/recommended citation for the package.
   */
  @JsonProperty("bibliographicCitation")
  private String bibliographicCitation;

  /**
   * Camera trap project or study that originated the package.
   * (Required)
   */
  @JsonProperty("project")
  @Valid
  @NotNull
  private Project project;

  /**
   * Least precise coordinate precision of the `deployments.latitude` and `deployments.longitude` (least precise in case of mixed precision, e.g. `0.01` for coordinates of precision of 0.01 and 0.001 degree). Especially relevant when coordinates have been rounded to protect sensitive species.
   */
  @JsonProperty("coordinatePrecision")
  private Double coordinatePrecision;

  /**
   * GeoJSON Object
   * <p>
   * This object represents a geometry, feature, or collection of features.
   * (Required)
   */
  @JsonProperty("spatial")
  @Valid
  @NotNull
  private Geojson spatial;

  /**
   * Temporal coverage of the package.
   * (Required)
   */
  @JsonProperty("temporal")
  @Valid
  @NotNull
  private Temporal temporal;

  /**
   * Taxonomic coverage of the package, based on the unique `observations.scientificName`.
   * (Required)
   */
  @JsonProperty("taxonomic")
  @Valid
  @NotNull
  private List<Taxonomic> taxonomic = new ArrayList<>();

  /**
   * Identifiers of resources related to the package (e.g. papers, project pages, derived datasets, APIs, etc.).
   */
  @JsonProperty("relatedIdentifiers")
  @Valid
  private List<RelatedIdentifier> relatedIdentifiers = new ArrayList<>();

  /**
   * List of references related to the package (e.g. references cited in `package.project.description`). References ideally include a DOI.
   */
  @JsonProperty("references")
  @Valid
  private List<String> references = new ArrayList<>();

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#resource-information">Data Package specification</a>. Camtrap DP further requires each object to be a <a href="https://specs.frictionlessdata.io/tabular-data-resource/">Tabular Data Resource</a> with a specific `name` and `schema`. See <a href="../data">Data</a> for the requirements for those resources.
   */
  @Override
  @JsonProperty("resources")
  @Size(min = 3, max = 3)
  @Valid
  public List<Resource> getResources() {
    return super.getResources();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#resource-information">Data Package specification</a>. Camtrap DP further requires each object to be a <a href="https://specs.frictionlessdata.io/tabular-data-resource/">Tabular Data Resource</a> with a specific `name` and `schema`. See [Data](../data) for the requirements for those resources.
   */
  @Override
  @JsonProperty("resources")
  public void setResources(List<Resource> resources) {
    super.setResources(resources);
  }

  // TODO: 13/10/2022 parent profile is String, camtrap is URI. Keep it String, but validate it is a valid URL?
  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#profile">Data Package specification</a>. Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).
   * (Required)
   */
  @Override
  @JsonProperty("profile")
  public String getProfile() {
    return super.getProfile();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#profile">Data Package specification</a>. Camtrap DP further requires this to be the URL of the used Camtrap DP Profile version (e.g. `https://raw.githubusercontent.com/tdwg/camtrap-dp/1.0/camtrap-dp-profile.json`).
   * (Required)
   */
  @Override
  @JsonProperty("profile")
  public void setProfile(String profile) {
    super.setProfile(profile);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#name">Data Package specification</a>.
   */
  @Override
  @JsonProperty("name")
  public String getName() {
    return super.getName();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#name">Data Package specification</a>.
   */
  @Override
  @JsonProperty("name")
  public void setName(String name) {
    super.setName(name);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#id">Data Package specification</a>.
   */
  @Override
  @JsonProperty("id")
  public String getId() {
    return super.getId();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#id">Data Package specification</a>.
   */
  @Override
  @JsonProperty("id")
  public void setId(String id) {
    super.setId(id);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#created">Data Package specification</a>. Camtrap DP makes this a required property.
   * (Required)
   */
  @Override
  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  public Date getCreated() {
    return super.getCreated();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#created">Data Package specification</a>. Camtrap DP makes this a required property.
   * (Required)
   */
  @Override
  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  public void setCreated(Date created) {
    super.setCreated(created);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#title">Data Package specification</a>. Not to be confused with the title of the project that originated the package (`package.project.title`).
   */
  @Override
  @JsonProperty("title")
  public String getTitle() {
    return super.getTitle();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#title">Data Package specification</a>. Not to be confused with the title of the project that originated the package (`package.project.title`).
   */
  @Override
  @JsonProperty("title")
  public void setTitle(String title) {
    super.setTitle(title);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#contributors">Data Package specification</a>. Camtrap DP makes this a required property. Can include people and organizations.
   * (Required)
   */
  @Override
  @JsonProperty("contributors")
  @NotNull
  public List<Contributor> getContributors() {
    return super.getContributors();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#contributors">Data Package specification</a>. Camtrap DP makes this a required property. Can include people and organizations.
   * (Required)
   */
  @Override
  @JsonProperty("contributors")
  public void setContributors(List<Contributor> contributors) {
    super.setContributors(contributors);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#description">Data Package specification</a>. Not to be confused with the description of the project that originated the package (`package.project.description`).
   */
  @Override
  @JsonProperty("description")
  public String getDescription() {
    return super.getDescription();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#description">Data Package specification</a>. Not to be confused with the description of the project that originated the package (`package.project.description`).
   */
  @Override
  @JsonProperty("description")
  public void setDescription(String description) {
    super.setDescription(description);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#version">Data Package specification</a>.
   */
  @Override
  @JsonProperty("version")
  public Object getVersion() {
    return super.getVersion();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#version">Data Package specification</a>.
   */
  @Override
  @JsonProperty("version")
  public void setVersion(Object version) {
    super.setVersion(version);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#keywords">Data Package specification</a>.
   */
  @Override
  @JsonProperty("keywords")
  public List<String> getKeywords() {
    return super.getKeywords();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#keywords">Data Package specification</a>.
   */
  @Override
  @JsonProperty("keywords")
  public void setKeywords(List<String> keywords) {
    super.setKeywords(keywords);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#image">Data Package specification</a>.
   */
  @Override
  @JsonProperty("image")
  public String getImage() {
    return super.getImage();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#image">Data Package specification</a>.
   */
  @Override
  @JsonProperty("image")
  public void setImage(String image) {
    super.setImage(image);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#homepage">Data Package specification</a>.
   */
  @Override
  @JsonProperty("homepage")
  public URI getHomepage() {
    return super.getHomepage();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#homepage">Data Package specification</a>.
   */
  @Override
  @JsonProperty("homepage")
  public void setHomepage(URI homepage) {
    super.setHomepage(homepage);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#sources">Data Package specification</a>. Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).
   */
  @Override
  @JsonProperty("sources")
  @Valid
  public List<Source> getSources() {
    return super.getSources();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#sources">Data Package specification</a>. Can include the data management platform from which the package was derived (e.g. Agouti, Trapper, Wildlife Insights).
   */
  @Override
  @JsonProperty("sources")
  public void setSources(List<Source> sources) {
    super.setSources(sources);
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#licenses">Data Package specification</a>. If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.
   */
  @Override
  @JsonProperty("licenses")
  @JsonDeserialize(contentUsing = CamtrapLicense.CamtrapLicenseDeserializer.class)
  @Size(min = 2)
  @Valid
  public List<License> getLicenses() {
    return super.getLicenses();
  }

  /**
   * See <a href="https://specs.frictionlessdata.io/data-package/#licenses">Data Package specification</a>. If provided, Camtrap DP further requires at least a license for the content of the package and one for the media files.
   */
  @Override
  @JsonProperty("licenses")
  public void setLicenses(List<License> licenses) {
    super.setLicenses(licenses);
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

  @Override
  public String toString() {
    return new StringJoiner(", ", CamtrapMetadata.class.getSimpleName() + "[", "]")
        .add("bibliographicCitation='" + bibliographicCitation + "'")
        .add("project=" + project)
        .add("coordinatePrecision=" + coordinatePrecision)
        .add("spatial=" + spatial)
        .add("temporal=" + temporal)
        .add("taxonomic=" + taxonomic)
        .add("relatedIdentifiers=" + relatedIdentifiers)
        .add("references=" + references)
        .toString();
  }
}
