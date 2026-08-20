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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "name",
    "taxon",
    "synonym",
    "reference",
    "author",
    "typeMaterial"
})
public class UrlFormatter {

  @JsonProperty("name")
  private String name;

  @JsonProperty("taxon")
  private String taxon;

  @JsonProperty("synonym")
  private String synonym;

  @JsonProperty("reference")
  private String reference;

  @JsonProperty("author")
  private String author;

  @JsonProperty("typeMaterial")
  private String typeMaterial;

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("taxon")
  public String getTaxon() {
    return taxon;
  }

  @JsonProperty("taxon")
  public void setTaxon(String taxon) {
    this.taxon = taxon;
  }

  @JsonProperty("synonym")
  public String getSynonym() {
    return synonym;
  }

  @JsonProperty("synonym")
  public void setSynonym(String synonym) {
    this.synonym = synonym;
  }

  @JsonProperty("reference")
  public String getReference() {
    return reference;
  }

  @JsonProperty("reference")
  public void setReference(String reference) {
    this.reference = reference;
  }

  @JsonProperty("author")
  public String getAuthor() {
    return author;
  }

  @JsonProperty("author")
  public void setAuthor(String author) {
    this.author = author;
  }

  @JsonProperty("typeMaterial")
  public String getTypeMaterial() {
    return typeMaterial;
  }

  @JsonProperty("typeMaterial")
  public void setTypeMaterial(String typeMaterial) {
    this.typeMaterial = typeMaterial;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    UrlFormatter that = (UrlFormatter) o;
    return Objects.equals(name, that.name) && Objects.equals(taxon, that.taxon) && Objects.equals(synonym, that.synonym) && Objects.equals(reference, that.reference) && Objects.equals(author, that.author) && Objects.equals(typeMaterial, that.typeMaterial);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, taxon, synonym, reference, author, typeMaterial);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UrlFormatter.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("taxon='" + taxon + "'")
        .add("synonym='" + synonym + "'")
        .add("reference='" + reference + "'")
        .add("author='" + author + "'")
        .add("typeMaterial='" + typeMaterial + "'")
        .toString();
  }
}
