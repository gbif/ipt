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
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Related identifier.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "relationType",
    "relatedIdentifier",
    "resourceTypeGeneral",
    "relatedIdentifierType"
})
public class RelatedIdentifier implements Serializable {

  private final static long serialVersionUID = 3596514384020747828L;

  /**
   * Description of the relationship between the resource (the package) and the related resource.
   * (Required)
   */
  @JsonProperty("relationType")
  @JsonPropertyDescription("Description of the relationship between the resource (the package) and the related resource.")
  @NotNull
  private RelatedIdentifier.RelationType relationType;

  /**
   * Unique identifier of the related resource (e.g. a DOI or URL).
   * (Required)
   */
  @JsonProperty("relatedIdentifier")
  @JsonPropertyDescription("Unique identifier of the related resource (e.g. a DOI or URL).")
  @NotNull
  private String relatedIdentifier;

  /**
   * General type of the related resource.
   */
  @JsonProperty("resourceTypeGeneral")
  @JsonPropertyDescription("General type of the related resource.")
  private RelatedIdentifier.ResourceTypeGeneral resourceTypeGeneral;

  /**
   * Type of the `RelatedIdentifier`.
   * (Required)
   */
  @JsonProperty("relatedIdentifierType")
  @JsonPropertyDescription("Type of the `RelatedIdentifier`.")
  @NotNull
  private RelatedIdentifier.RelatedIdentifierType relatedIdentifierType;

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Description of the relationship between the resource (the package) and the related resource.
   * (Required)
   */
  @JsonProperty("relationType")
  public RelatedIdentifier.RelationType getRelationType() {
    return relationType;
  }

  /**
   * Description of the relationship between the resource (the package) and the related resource.
   * (Required)
   */
  @JsonProperty("relationType")
  public void setRelationType(RelatedIdentifier.RelationType relationType) {
    this.relationType = relationType;
  }

  /**
   * Unique identifier of the related resource (e.g. a DOI or URL).
   * (Required)
   */
  @JsonProperty("relatedIdentifier")
  public String getRelatedIdentifier() {
    return relatedIdentifier;
  }

  /**
   * Unique identifier of the related resource (e.g. a DOI or URL).
   * (Required)
   */
  @JsonProperty("relatedIdentifier")
  public void setRelatedIdentifier(String relatedIdentifier) {
    this.relatedIdentifier = relatedIdentifier;
  }

  /**
   * General type of the related resource.
   */
  @JsonProperty("resourceTypeGeneral")
  public RelatedIdentifier.ResourceTypeGeneral getResourceTypeGeneral() {
    return resourceTypeGeneral;
  }

  /**
   * General type of the related resource.
   */
  @JsonProperty("resourceTypeGeneral")
  public void setResourceTypeGeneral(RelatedIdentifier.ResourceTypeGeneral resourceTypeGeneral) {
    this.resourceTypeGeneral = resourceTypeGeneral;
  }

  /**
   * Type of the `RelatedIdentifier`.
   * (Required)
   */
  @JsonProperty("relatedIdentifierType")
  public RelatedIdentifier.RelatedIdentifierType getRelatedIdentifierType() {
    return relatedIdentifierType;
  }

  /**
   * Type of the `RelatedIdentifier`.
   * (Required)
   */
  @JsonProperty("relatedIdentifierType")
  public void setRelatedIdentifierType(RelatedIdentifier.RelatedIdentifierType relatedIdentifierType) {
    this.relatedIdentifierType = relatedIdentifierType;
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
   * Type of the `RelatedIdentifier`.
   */
  public enum RelatedIdentifierType {

    ARK("ARK"),
    AR_XIV("arXiv"),
    BIBCODE("bibcode"),
    DOI("DOI"),
    EAN_13("EAN13"),
    EISSN("EISSN"),
    HANDLE("Handle"),
    IGSN("IGSN"),
    ISBN("ISBN"),
    ISSN("ISSN"),
    ISTC("ISTC"),
    LISSN("LISSN"),
    LSID("LSID"),
    PMID("PMID"),
    PURL("PURL"),
    UPC("UPC"),
    URL("URL"),
    URN("URN"),
    W_3_ID("w3id");
    private final String value;
    private final static Map<String, RelatedIdentifier.RelatedIdentifierType> CONSTANTS = new HashMap<>();

    static {
      for (RelatedIdentifier.RelatedIdentifierType c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    RelatedIdentifierType(String value) {
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
    public static RelatedIdentifier.RelatedIdentifierType fromValue(String value) {
      RelatedIdentifier.RelatedIdentifierType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

  /**
   * Description of the relationship between the resource (the package) and the related resource.
   */
  public enum RelationType {

    IS_CITED_BY("IsCitedBy"),
    CITES("Cites"),
    IS_SUPPLEMENT_TO("IsSupplementTo"),
    IS_SUPPLEMENTED_BY("IsSupplementedBy"),
    IS_CONTINUED_BY("IsContinuedBy"),
    CONTINUES("Continues"),
    IS_NEW_VERSION_OF("IsNewVersionOf"),
    IS_PREVIOUS_VERSION_OF("IsPreviousVersionOf"),
    IS_PART_OF("IsPartOf"),
    HAS_PART("HasPart"),
    IS_PUBLISHED_IN("IsPublishedIn"),
    IS_REFERENCED_BY("IsReferencedBy"),
    REFERENCES("References"),
    IS_DOCUMENTED_BY("IsDocumentedBy"),
    DOCUMENTS("Documents"),
    IS_COMPILED_BY("IsCompiledBy"),
    COMPILES("Compiles"),
    IS_VARIANT_FORM_OF("IsVariantFormOf"),
    IS_ORIGINAL_FORM_OF("IsOriginalFormOf"),
    IS_IDENTICAL_TO("IsIdenticalTo"),
    HAS_METADATA("HasMetadata"),
    IS_METADATA_FOR("IsMetadataFor"),
    REVIEWS("Reviews"),
    IS_REVIEWED_BY("IsReviewedBy"),
    IS_DERIVED_FROM("IsDerivedFrom"),
    IS_SOURCE_OF("IsSourceOf"),
    DESCRIBES("Describes"),
    IS_DESCRIBED_BY("IsDescribedBy"),
    HAS_VERSION("HasVersion"),
    IS_VERSION_OF("IsVersionOf"),
    REQUIRES("Requires"),
    IS_REQUIRED_BY("IsRequiredBy"),
    OBSOLETES("Obsoletes"),
    IS_OBSOLETED_BY("IsObsoletedBy");
    private final String value;
    private final static Map<String, RelatedIdentifier.RelationType> CONSTANTS = new HashMap<>();

    static {
      for (RelatedIdentifier.RelationType c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    RelationType(String value) {
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
    public static RelatedIdentifier.RelationType fromValue(String value) {
      RelatedIdentifier.RelationType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

  /**
   * General type of the related resource.
   */
  public enum ResourceTypeGeneral {

    AUDIOVISUAL("Audiovisual"),
    BOOK("Book"),
    BOOK_CHAPTER("BookChapter"),
    COLLECTION("Collection"),
    COMPUTATIONAL_NOTEBOOK("ComputationalNotebook"),
    CONFERENCE_PAPER("ConferencePaper"),
    CONFERENCE_PROCEEDING("ConferenceProceeding"),
    DATA_PAPER("DataPaper"),
    DATASET("Dataset"),
    DISSERTATION("Dissertation"),
    EVENT("Event"),
    IMAGE("Image"),
    INTERACTIVE_RESOURCE("InteractiveResource"),
    JOURNAL("Journal"),
    JOURNAL_ARTICLE("JournalArticle"),
    MODEL("Model"),
    OUTPUT_MANAGEMENT_PLAN("OutputManagementPlan"),
    PEER_REVIEW("PeerReview"),
    PHYSICAL_OBJECT("PhysicalObject"),
    PREPRINT("Preprint"),
    REPORT("Report"),
    SERVICE("Service"),
    SOFTWARE("Software"),
    SOUND("Sound"),
    STANDARD("Standard"),
    TEXT("Text"),
    WORKFLOW("Workflow"),
    OTHER("Other");
    private final String value;
    private final static Map<String, RelatedIdentifier.ResourceTypeGeneral> CONSTANTS = new HashMap<>();

    static {
      for (RelatedIdentifier.ResourceTypeGeneral c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ResourceTypeGeneral(String value) {
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
    public static RelatedIdentifier.ResourceTypeGeneral fromValue(String value) {
      RelatedIdentifier.ResourceTypeGeneral constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

}
