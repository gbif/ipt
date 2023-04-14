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
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Agent class for COL Data Package (ColDP) metadata.
 * Generated from <a href="https://github.com/CatalogueOfLife/coldp/blob/master/metadata.json">JSON schema</a>.
 * <p>
 * Agent entities are used for many fields below and can be either a person, an organisation or a combination of both.
 * The minimum requirement is either a persons family name, an organisation name or any of the identifiers for them.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "orcid",
  "family",
  "given",
  "rorid",
  "department",
  "organisation",
  "city",
  "state",
  "country",
  "email",
  "url",
  "note"
})
public class Agent {

  /**
   * ORCID person ID, https://orcid.org  example: 0000-0001-9745-636X
   */
  @JsonProperty("orcid")
  private String orcid;

  /**
   * last name
   */
  @JsonProperty("family")
  private String family;

  /**
   * first name
   */
  @JsonProperty("given")
  private String given;

  /**
  ROR organisation ID, https://ror.org  example: https://ror.org/05fjyn938
   */
  @JsonProperty("rorid")
  private String rorid;

  /**
   * subunit within the organisation, sometimes a project
   */
  @JsonProperty("department")
  private String department;

  /**
   * name of the organisation / institution
   */
  @JsonProperty("organisation")
  private String organisation;

  /**
   * city
   */
  @JsonProperty("city")
  private String city;

  /**
   * state or province
   */
  @JsonProperty("state")
  private String state;

  /**
   * ISO 2-letter country code
   */
  @JsonProperty("country")
  @Size(min = 2, max = 2)
  private String country;

  /**
   * email address
   */
  @JsonProperty("email")
  private String email;

  /**
   * webpage
   */
  @JsonProperty("url")
  private URI url;

  /**
   * miscellaneous extra information, used for contributor roles
   */
  @JsonProperty("note")
  private String note;

  @JsonProperty("orcid")
  public String getOrcid() {
    return orcid;
  }

  @JsonProperty("orcid")
  public void setOrcid(String orcid) {
    this.orcid = orcid;
  }

  @JsonProperty("family")
  public String getFamily() {
    return family;
  }

  @JsonProperty("family")
  public void setFamily(String family) {
    this.family = family;
  }

  @JsonProperty("given")
  public String getGiven() {
    return given;
  }

  @JsonProperty("given")
  public void setGiven(String given) {
    this.given = given;
  }

  @JsonProperty("rorid")
  public String getRorid() {
    return rorid;
  }

  @JsonProperty("rorid")
  public void setRorid(String rorid) {
    this.rorid = rorid;
  }

  @JsonProperty("department")
  public String getDepartment() {
    return department;
  }

  @JsonProperty("department")
  public void setDepartment(String department) {
    this.department = department;
  }

  @JsonProperty("organisation")
  public String getOrganisation() {
    return organisation;
  }

  @JsonProperty("organisation")
  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  @JsonProperty("city")
  public void setCity(String city) {
    this.city = city;
  }

  @JsonProperty("state")
  public String getState() {
    return state;
  }

  @JsonProperty("state")
  public void setState(String state) {
    this.state = state;
  }

  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  @JsonProperty("country")
  public void setCountry(String country) {
    this.country = country;
  }

  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  @JsonProperty("url")
  public URI getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(URI url) {
    this.url = url;
  }

  @JsonProperty("note")
  public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

}
