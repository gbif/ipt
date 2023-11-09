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

import org.gbif.ipt.validation.TaxonomicScopeMetadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Taxonomic implements Serializable {

  private final static long serialVersionUID = 2951714488879146221L;

  /**
   * Unique identifier of the taxon. Preferably a global unique identifier issued by an authoritative checklist.
   */
  @JsonProperty("taxonID")
  private String taxonID;

  /**
   * Scientific name of the taxon.
   * (Required)
   */
  @JsonProperty("scientificName")
  @NotNull(message = "validation.input.required", groups = TaxonomicScopeMetadata.class)
  private String scientificName;

  /**
   * Taxonomic rank of the scientific name.
   */
  @JsonProperty("taxonRank")
  private Taxonomic.TaxonRank taxonRank;

  /**
   * Kingdom in which the taxon is classified.
   */
  @JsonProperty("kingdom")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String kingdom;

  /**
   * Phylum or division in which the taxon is classified
   */
  @JsonProperty("phylum")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String phylum;

  /**
   * Class in which the taxon is classified.
   */
  @JsonProperty("class")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String _class;

  /**
   * Order in which the taxon is classified.
   */
  @JsonProperty("order")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String order;

  /**
   * Family in which the taxon is classified.
   */
  @JsonProperty("family")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String family;

  /**
   * Genus in which the taxon is classified.
   */
  @JsonProperty("genus")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String genus;

  /**
   * Common or vernacular names of the taxon, as `languageCode: vernacular name` pairs. Language codes should follow ISO 693-3 (e.g. `eng` for English).
   */
  @JsonProperty("vernacularNames")
  @Valid
  private Map<String, String> vernacularNames = new LinkedHashMap<>();

  @SuppressWarnings("FieldMayBeFinal")
  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Unique identifier of the taxon. Preferably a global unique identifier issued by an authoritative checklist.
   */
  @JsonProperty("taxonID")
  public String getTaxonID() {
    return taxonID;
  }

  /**
   * Unique identifier of the taxon. Preferably a global unique identifier issued by an authoritative checklist.
   */
  @JsonProperty("taxonID")
  public void setTaxonID(String taxonID) {
    this.taxonID = taxonID;
  }

  /**
   * Scientific name of the taxon.
   * (Required)
   */
  @JsonProperty("scientificName")
  public String getScientificName() {
    return scientificName;
  }

  /**
   * Scientific name of the taxon.
   * (Required)
   */
  @JsonProperty("scientificName")
  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }

  /**
   * Taxonomic rank of the scientific name.
   */
  @JsonProperty("taxonRank")
  public Taxonomic.TaxonRank getTaxonRank() {
    return taxonRank;
  }

  /**
   * Taxonomic rank of the scientific name.
   */
  @JsonProperty("taxonRank")
  public void setTaxonRank(Taxonomic.TaxonRank taxonRank) {
    this.taxonRank = taxonRank;
  }

  /**
   * Kingdom in which the taxon is classified.
   */
  @JsonProperty("kingdom")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getKingdom() {
    return kingdom;
  }

  /**
   * Kingdom in which the taxon is classified.
   */
  @JsonProperty("kingdom")
  public void setKingdom(String kingdom) {
    this.kingdom = kingdom;
  }

  /**
   * Phylum or division in which the taxon is classified
   */
  @JsonProperty("phylum")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getPhylum() {
    return phylum;
  }

  /**
   * Phylum or division in which the taxon is classified
   */
  @JsonProperty("phylum")
  public void setPhylum(String phylum) {
    this.phylum = phylum;
  }

  /**
   * Class in which the taxon is classified.
   */
  @JsonProperty("class")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getClass_() {
    return _class;
  }

  /**
   * Class in which the taxon is classified.
   */
  @JsonProperty("class")
  public void setClass_(String _class) {
    this._class = _class;
  }

  /**
   * Order in which the taxon is classified.
   */
  @JsonProperty("order")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getOrder() {
    return order;
  }

  /**
   * Order in which the taxon is classified.
   */
  @JsonProperty("order")
  public void setOrder(String order) {
    this.order = order;
  }

  /**
   * Family in which the taxon is classified.
   */
  @JsonProperty("family")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getFamily() {
    return family;
  }

  /**
   * Family in which the taxon is classified.
   */
  @JsonProperty("family")
  public void setFamily(String family) {
    this.family = family;
  }

  /**
   * Genus in which the taxon is classified.
   */
  @JsonProperty("genus")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getGenus() {
    return genus;
  }

  /**
   * Genus in which the taxon is classified.
   */
  @JsonProperty("genus")
  public void setGenus(String genus) {
    this.genus = genus;
  }

  /**
   * Common or vernacular names of the taxon, as `languageCode: vernacular name` pairs. Language codes should follow ISO 693-3 (e.g. `eng` for English).
   */
  @JsonProperty("vernacularNames")
  public Map<String, String> getVernacularNames() {
    return vernacularNames;
  }

  /**
   * Common or vernacular names of the taxon, as `languageCode: vernacular name` pairs. Language codes should follow ISO 693-3 (e.g. `eng` for English).
   */
  @JsonProperty("vernacularNames")
  public void setVernacularNames(Map<String, String> vernacularNames) {
    this.vernacularNames = vernacularNames;
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
   * Taxonomic rank of the scientific name.
   */
  public enum TaxonRank {

    KINGDOM("kingdom"),
    PHYLUM("phylum"),
    CLASS("class"),
    ORDER("order"),
    FAMILY("family"),
    GENUS("genus"),
    SPECIES("species"),
    SUBSPECIES("subspecies");
    private final String value;
    public final static Map<String, Taxonomic.TaxonRank> CONSTANTS = new HashMap<>();
    public final static Map<String, String> VOCABULARY = new LinkedHashMap<>();

    static {
      for (Taxonomic.TaxonRank c : values()) {
        CONSTANTS.put(c.value, c);
        VOCABULARY.put(c.name(), c.value);
      }
    }

    TaxonRank(String value) {
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
    public static Taxonomic.TaxonRank fromValue(String value) {
      Taxonomic.TaxonRank constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }
}
