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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;

/**
 * Main class for COL Data Package (ColDP) metadata.
 * Generated from <a href="https://github.com/CatalogueOfLife/coldp/blob/master/metadata.json">JSON schema</a>.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "doi",
  "identifier",
  "title",
  "alias",
  "version",
  "issued",
  "creator",
  "editor",
  "publisher",
  "contact",
  "contributor",
  "description",
  "keyword",
  "taxonomicScope",
  "taxonomicScopeInEnglish",
  "temporalScope",
  "geographicScope",
  "completeness",
  "confidence",
  "url",
  "issn",
  "license",
  "logo",
  "source",
  "notes"
})
public class ColMetadata implements DataPackageMetadata {

  /**
   * single, primary DOI for the dataset
   */
  @JsonProperty("doi")
  private Pattern doi;

  /**
   * List of alternative identifiers for the dataset with an ID and an optional type value
   * Supported ID types: DOI, GBIF, COL, PLAZI
   */
  @JsonProperty("identifier")
  @Valid
  private Identifier identifier;

  /**
   * Full dataset title
   */
  @JsonProperty("title")
  @NotNull
  private String title;

  /**
   * Short, hopefully unique name for the dataset
   */
  @JsonProperty("alias")
  private String alias;

  /**
   * Version name of this copy
   */
  @JsonProperty("version")
  private String version;

  /**
   * Official release date of this version in ISO YYYY-MM-DD. Will be part of the default citation!
   */
  @JsonProperty("issued")
  @NotNull
  private Pattern issued;

  /**
   * Creators of the dataset. <b>Will be treated as authors in the default citation!</b>
   * For available fields see Agent type at the top
   * Please provide an ORCID if you can, so it can be included in DOI metadata
   */
  @JsonProperty("creator")
  @Valid
  @NotNull
  private List<Agent> creator = new ArrayList<>();

  /**
   * Editors of the dataset. <b>Will be part of the default citation!</b>
   */
  @JsonProperty("editor")
  @Valid
  private List<Agent> editor = new ArrayList<>();

  /**
   * Single publishing organisation. <b>Will be part of the default citation!</b>
   */
  @JsonProperty("publisher")
  @Valid
  private Agent publisher;

  /**
   * Single point of contact for questions
   */
  @JsonProperty("contact")
  @Valid
  private Agent contact;

  /**
   * Agent contributing to the dataset in any other way, but who is not considered an author of the dataset
   * Use the optional note field to specify the kind of contribution
   */
  @JsonProperty("contributor")
  @Valid
  private List<Agent> contributor = new ArrayList<>();

  /**
   * Multi paragraph description / abstract of the dataset
   */
  @JsonProperty("description")
  private String description;

  /**
   * Keywords list. Added in v1.1
   */
  @JsonProperty("keyword")
  @Valid
  private List<String> keyword;

  /**
   * Taxonomic scope of the dataset
   */
  @JsonProperty("taxonomicScope")
  private String taxonomicScope;

  /**
   * Taxonomic scope of the dataset given as English vernacular name(s)
   */
  @JsonProperty("taxonomicScopeInEnglish")
  private String taxonomicScopeInEnglish;

  /**
   * Temporal scope of the dataset
   */
  @JsonProperty("temporalScope")
  private String temporalScope;

  /**
   * Description of the geographical scope of the dataset
   */
  @JsonProperty("geographicScope")
  private String geographicScope;

  /**
   * 0-100 to express the completeness of the dataset in regard to the defined scope
   */
  @JsonProperty("completeness")
  private Integer completeness;

  /**
   * Integer between 1-5 with 5 expressing the highest confidence in quality of the data
   */
  @JsonProperty("confidence")
  private Integer confidence;

  /**
   * Link to a website for the dataset
   */
  @JsonProperty("url")
  private URI url;

  /**
   * ISSN for a serial publication
   */
  @JsonProperty("issn")
  private String issn;

  /**
   * Any commons license (CC0, CC-BY, CC-BY-NC, CC-BY-SA, CC-BY-ND, CC-BY-NC-SA, CC-BY-NC-ND)
   * Must be CC0 or CC-BY to be used by COL !!!
   */
  @JsonProperty("license")
  @NotNull
  private String license;

  /**
   * URL to large logo image
   */
  @JsonProperty("logo")
  private URI logo;

  /**
   * List of Citations this dataset is derived from.
   * We use CSL-JSON fields to represent a structured citation, see below for the main fields used for bibliographies.
   * <p>
   * NAME fields:
   * are lists of people represented either by a simple string or objects with family & given fields.
   * In case no parsed name can be provided, simple strings will be parsed.
   * For the parsing to given and family name to correctly work it is highly recommended to use one of the following formats:
   * 1) GivenName FamilyName: Only use this in case the last name is a single token. There can be many first names e.g. John Paul Sartre
   * 2) FamilyName, GivenName: Supports any conmplex last name. E.g. Brinch Hansen, Per
   * <p>
   * DATE fields:
   * are ISO dates that can be truncated to represent a year, year & month or exact date: 1998, 1998-05 or 1998-05-21
   */
  @JsonProperty("source")
  @Valid
  private List<Citation> source = new ArrayList<>();

  /**
   * Remarks, comments and usage notes about this dataset
   */
  @JsonProperty("notes")
  private String notes;

  @JsonIgnore
  @Valid
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonProperty("doi")
  public Pattern getDoi() {
    return doi;
  }

  @JsonProperty("doi")
  public void setDoi(Pattern doi) {
    this.doi = doi;
  }

  @JsonProperty("identifier")
  public Identifier getIdentifier() {
    return identifier;
  }

  @JsonProperty("identifier")
  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @Override
  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("alias")
  public String getAlias() {
    return alias;
  }

  @JsonProperty("alias")
  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @Override
  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  @JsonProperty("issued")
  public Pattern getIssued() {
    return issued;
  }

  @JsonProperty("issued")
  public void setIssued(Pattern issued) {
    this.issued = issued;
  }

  @JsonProperty("creator")
  public List<Agent> getCreator() {
    return creator;
  }

  @JsonProperty("creator")
  public void setCreator(List<Agent> creator) {
    this.creator = creator;
  }

  @JsonProperty("editor")
  public List<Agent> getEditor() {
    return editor;
  }

  @JsonProperty("editor")
  public void setEditor(List<Agent> editor) {
    this.editor = editor;
  }

  @JsonProperty("publisher")
  public Agent getPublisher() {
    return publisher;
  }

  @JsonProperty("publisher")
  public void setPublisher(Agent publisher) {
    this.publisher = publisher;
  }

  @JsonProperty("contact")
  public Agent getContact() {
    return contact;
  }

  @JsonProperty("contact")
  public void setContact(Agent contact) {
    this.contact = contact;
  }

  @JsonProperty("contributor")
  public List<Agent> getContributor() {
    return contributor;
  }

  @JsonProperty("contributor")
  public void setContributor(List<Agent> contributor) {
    this.contributor = contributor;
  }

  @Override
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @Override
  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("keyword")
  public List<String> getKeyword() {
    return keyword;
  }

  @JsonProperty("keyword")
  public void setKeyword(List<String> keyword) {
    this.keyword = keyword;
  }

  @JsonProperty("taxonomicScope")
  public String getTaxonomicScope() {
    return taxonomicScope;
  }

  @JsonProperty("taxonomicScope")
  public void setTaxonomicScope(String taxonomicScope) {
    this.taxonomicScope = taxonomicScope;
  }

  @JsonProperty("taxonomicScopeInEnglish")
  public String getTaxonomicScopeInEnglish() {
    return taxonomicScopeInEnglish;
  }

  @JsonProperty("taxonomicScopeInEnglish")
  public void setTaxonomicScopeInEnglish(String taxonomicScopeInEnglish) {
    this.taxonomicScopeInEnglish = taxonomicScopeInEnglish;
  }

  @JsonProperty("temporalScope")
  public String getTemporalScope() {
    return temporalScope;
  }

  @JsonProperty("temporalScope")
  public void setTemporalScope(String temporalScope) {
    this.temporalScope = temporalScope;
  }

  @JsonProperty("geographicScope")
  public String getGeographicScope() {
    return geographicScope;
  }

  @JsonProperty("geographicScope")
  public void setGeographicScope(String geographicScope) {
    this.geographicScope = geographicScope;
  }

  @JsonProperty("completeness")
  public Integer getCompleteness() {
    return completeness;
  }

  @JsonProperty("completeness")
  public void setCompleteness(Integer completeness) {
    this.completeness = completeness;
  }

  @JsonProperty("confidence")
  public Integer getConfidence() {
    return confidence;
  }

  @JsonProperty("confidence")
  public void setConfidence(Integer confidence) {
    this.confidence = confidence;
  }

  @JsonProperty("url")
  public URI getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(URI url) {
    this.url = url;
  }

  @JsonProperty("issn")
  public String getIssn() {
    return issn;
  }

  @JsonProperty("issn")
  public void setIssn(String issn) {
    this.issn = issn;
  }

  @JsonProperty("license")
  public String getLicense() {
    return license;
  }

  @JsonProperty("license")
  public void setLicense(String license) {
    this.license = license;
  }

  @JsonProperty("logo")
  public URI getLogo() {
    return logo;
  }

  @JsonProperty("logo")
  public void setLogo(URI logo) {
    this.logo = logo;
  }

  @Override
  public String getImage() {
    return logo != null ? logo.toString() : null;
  }

  @JsonProperty("source")
  public List<Citation> getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(List<Citation> source) {
    this.source = source;
  }

  @JsonProperty("notes")
  public String getNotes() {
    return notes;
  }

  @JsonProperty("notes")
  public void setNotes(String notes) {
    this.notes = notes;
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
    ColMetadata that = (ColMetadata) o;
    return Objects.equals(doi, that.doi)
      && Objects.equals(identifier, that.identifier)
      && Objects.equals(title, that.title)
      && Objects.equals(alias, that.alias)
      && Objects.equals(version, that.version)
      && Objects.equals(issued, that.issued)
      && Objects.equals(creator, that.creator)
      && Objects.equals(editor, that.editor)
      && Objects.equals(publisher, that.publisher)
      && Objects.equals(contact, that.contact)
      && Objects.equals(contributor, that.contributor)
      && Objects.equals(description, that.description)
      && Objects.equals(keyword, that.keyword)
      && Objects.equals(taxonomicScope, that.taxonomicScope)
      && Objects.equals(taxonomicScopeInEnglish, that.taxonomicScopeInEnglish)
      && Objects.equals(temporalScope, that.temporalScope)
      && Objects.equals(geographicScope, that.geographicScope)
      && Objects.equals(completeness, that.completeness)
      && Objects.equals(confidence, that.confidence)
      && Objects.equals(url, that.url)
      && Objects.equals(issn, that.issn)
      && Objects.equals(license, that.license)
      && Objects.equals(logo, that.logo)
      && Objects.equals(source, that.source)
      && Objects.equals(notes, that.notes)
      && Objects.equals(additionalProperties, that.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, identifier, title, alias, version, issued, creator, editor, publisher, contact,
      contributor, description, keyword, taxonomicScope, taxonomicScopeInEnglish, temporalScope, geographicScope,
      completeness, confidence, url, issn, license, logo, source, notes, additionalProperties);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ColMetadata.class.getSimpleName() + "[", "]")
      .add("doi=" + doi)
      .add("identifier=" + identifier)
      .add("title='" + title + "'")
      .add("alias='" + alias + "'")
      .add("version='" + version + "'")
      .add("issued=" + issued)
      .add("creator=" + creator)
      .add("editor=" + editor)
      .add("publisher=" + publisher)
      .add("contact=" + contact)
      .add("contributor=" + contributor)
      .add("description='" + description + "'")
      .add("keyword=" + keyword)
      .add("taxonomicScope='" + taxonomicScope + "'")
      .add("taxonomicScopeInEnglish='" + taxonomicScopeInEnglish + "'")
      .add("temporalScope='" + temporalScope + "'")
      .add("geographicScope='" + geographicScope + "'")
      .add("completeness=" + completeness)
      .add("confidence=" + confidence)
      .add("url=" + url)
      .add("issn='" + issn + "'")
      .add("license='" + license + "'")
      .add("logo=" + logo)
      .add("source=" + source)
      .add("notes='" + notes + "'")
      .add("additionalProperties=" + additionalProperties)
      .toString();
  }
}
