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
import java.util.HashMap;
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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Source class for COL Data Package (ColDP) metadata.
 * Generated from <a href="https://github.com/CatalogueOfLife/coldp/blob/master/metadata.json">JSON schema</a>.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
  "id",
  "type",
  "title",
  "version",
  "author",
  "editor",
  "publisher",
  "publisherPlace",
  "issued",
  "containerTitle",
  "containerAuthor",
  "volume",
  "issue",
  "edition",
  "page",
  "collectionTitle",
  "collectionEditor",
  "doi",
  "isbn",
  "issn",
  "url",
  "accessed",
  "note"
})
public class Citation {

  /**
   * identifier for the source, can be referenced from individual data records via sourceID to indicate the provenance on a record level
   */
  @JsonProperty("id")
  @NotNull
  private String id;

  /**
   * CSL type, e.g. ARTICLE-JOURNAL, BOOK, CHAPTER, DATASET, WEBPAGE. See https://aurimasv.github.io/z2csl/typeMap.xml for a mapping of CSL types to field sets
   */
  @JsonProperty("type")
  private Citation.Type type;

  /**
   * primary title of the item
   */
  @JsonProperty("title")
  @NotNull
  private String title;

  /**
   * version of the dataset/source
   */
  @JsonProperty("version")
  private String version;

  /**
   * author list. type=NAME[]
   */
  @JsonProperty("author")
  @Valid
  private List<Person> author = new ArrayList<>();

  /**
   * editor list. type=NAME[]
   */
  @JsonProperty("editor")
  @Valid
  private List<Person> editor = new ArrayList<>();

  /**
   * publisher
   */
  @JsonProperty("publisher")
  private String publisher;

  /**
   * geographic location of the publisher
   */
  @JsonProperty("publisherPlace")
  private String publisherPlace;

  /**
   * date the item was issued/published in possibly truncated ISO format, e.g. 1998, 1998-05 or 1998-05-21. type=DATE
   */
  @JsonProperty("issued")
  private Pattern issued;

  /**
   * title of the container holding the item (e.g. the book title for a book chapter, the journal title for a journal article)
   */
  @JsonProperty("containerTitle")
  private String containerTitle;

  /**
   * author(s) of the container holding the item (e.g. the book author for a book chapter). type=NAME[]
   */
  @JsonProperty("containerAuthor")
  @Valid
  private List<Person> containerAuthor = new ArrayList<>();

  /**
   * (container) volume holding the item (e.g. “2” when citing a chapter from book volume 2). type=NUMBER
   */
  @JsonProperty("volume")
  private String volume;

  /**
   * (container) issue holding the item (e.g. “5” when citing a journal article from journal volume 2, issue 5). type=NUMBER
   */
  @JsonProperty("issue")
  private String issue;

  /**
   * (container) edition holding the item (e.g. “3” when citing a chapter in the third edition of a book). type=NUMBER
   */
  @JsonProperty("edition")
  private String edition;

  /**
   * range of pages the item (e.g. a journal article) covers in a container (e.g. a journal issue)
   */
  @JsonProperty("page")
  private String page;

  /**
   * title of the collection holding the item (e.g. the series title for a book)
   */
  @JsonProperty("collectionTitle")
  private String collectionTitle;

  /**
   * editor(s) of the collection holding the item (e.g. the series editor for a book). type=NAME[]
   */
  @JsonProperty("collectionEditor")
  @Valid
  private List<Person> collectionEditor = new ArrayList<>();

  /**
   * a DOI
   */
  @JsonProperty("doi")
  private Pattern doi;

  /**
   * International Standard Book Number
   */
  @JsonProperty("isbn")
  private String isbn;

  /**
   * International Standard Serial Number
   */
  @JsonProperty("issn")
  private String issn;

  /**
   * link to webpage for electronic resources
   */
  @JsonProperty("url")
  private URI url;

  /**
   * date the item has been accessed. type=DATE
   */
  @JsonProperty("accessed")
  private Pattern accessed;

  /**
   * (short) inline note giving additional item details (e.g. a concise summary or commentary)
   */
  @JsonProperty("note")
  private String note;

  @JsonIgnore
  @Valid
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("type")
  public Citation.Type getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(Citation.Type type) {
    this.type = type;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  @JsonProperty("author")
  public List<Person> getAuthor() {
    return author;
  }

  @JsonProperty("author")
  public void setAuthor(List<Person> author) {
    this.author = author;
  }

  @JsonProperty("editor")
  public List<Person> getEditor() {
    return editor;
  }

  @JsonProperty("editor")
  public void setEditor(List<Person> editor) {
    this.editor = editor;
  }

  @JsonProperty("publisher")
  public String getPublisher() {
    return publisher;
  }

  @JsonProperty("publisher")
  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  @JsonProperty("publisherPlace")
  public String getPublisherPlace() {
    return publisherPlace;
  }

  @JsonProperty("publisherPlace")
  public void setPublisherPlace(String publisherPlace) {
    this.publisherPlace = publisherPlace;
  }

  @JsonProperty("issued")
  public Pattern getIssued() {
    return issued;
  }

  @JsonProperty("issued")
  public void setIssued(Pattern issued) {
    this.issued = issued;
  }

  @JsonProperty("containerTitle")
  public String getContainerTitle() {
    return containerTitle;
  }

  @JsonProperty("containerTitle")
  public void setContainerTitle(String containerTitle) {
    this.containerTitle = containerTitle;
  }

  @JsonProperty("containerAuthor")
  public List<Person> getContainerAuthor() {
    return containerAuthor;
  }

  @JsonProperty("containerAuthor")
  public void setContainerAuthor(List<Person> containerAuthor) {
    this.containerAuthor = containerAuthor;
  }

  @JsonProperty("volume")
  public String getVolume() {
    return volume;
  }

  @JsonProperty("volume")
  public void setVolume(String volume) {
    this.volume = volume;
  }

  @JsonProperty("issue")
  public String getIssue() {
    return issue;
  }

  @JsonProperty("issue")
  public void setIssue(String issue) {
    this.issue = issue;
  }

  @JsonProperty("edition")
  public String getEdition() {
    return edition;
  }

  @JsonProperty("edition")
  public void setEdition(String edition) {
    this.edition = edition;
  }

  @JsonProperty("page")
  public String getPage() {
    return page;
  }

  @JsonProperty("page")
  public void setPage(String page) {
    this.page = page;
  }

  @JsonProperty("collectionTitle")
  public String getCollectionTitle() {
    return collectionTitle;
  }

  @JsonProperty("collectionTitle")
  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  @JsonProperty("collectionEditor")
  public List<Person> getCollectionEditor() {
    return collectionEditor;
  }

  @JsonProperty("collectionEditor")
  public void setCollectionEditor(List<Person> collectionEditor) {
    this.collectionEditor = collectionEditor;
  }

  @JsonProperty("doi")
  public Pattern getDoi() {
    return doi;
  }

  @JsonProperty("doi")
  public void setDoi(Pattern doi) {
    this.doi = doi;
  }

  @JsonProperty("isbn")
  public String getIsbn() {
    return isbn;
  }

  @JsonProperty("isbn")
  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  @JsonProperty("issn")
  public String getIssn() {
    return issn;
  }

  @JsonProperty("issn")
  public void setIssn(String issn) {
    this.issn = issn;
  }

  @JsonProperty("url")
  public URI getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(URI url) {
    this.url = url;
  }

  @JsonProperty("accessed")
  public Pattern getAccessed() {
    return accessed;
  }

  @JsonProperty("accessed")
  public void setAccessed(Pattern accessed) {
    this.accessed = accessed;
  }

  @JsonProperty("note")
  public String getNote() {
    return note;
  }

  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public enum Type {

    ARTICLE_JOURNAL("article-journal"),
    BOOK("book"),
    CHAPTER("chapter"),
    THESIS("thesis"),
    PAPER_CONFERENCE("paper-conference"),
    MANUSCRIPT("manuscript"),
    DATASET("dataset"),
    WEBPAGE("webpage"),
    PERSONAL_COMMUNICATION("personal_communication");
    private final String value;
    private final static Map<String, Citation.Type> CONSTANTS = new HashMap<>();

    static {
      for (Citation.Type c: values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Type(String value) {
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
    public static Citation.Type fromValue(String value) {
      Citation.Type constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Citation citation = (Citation) o;
    return Objects.equals(id, citation.id)
      && type == citation.type
      && Objects.equals(title, citation.title)
      && Objects.equals(version, citation.version)
      && Objects.equals(author, citation.author)
      && Objects.equals(editor, citation.editor)
      && Objects.equals(publisher, citation.publisher)
      && Objects.equals(publisherPlace, citation.publisherPlace)
      && Objects.equals(issued, citation.issued)
      && Objects.equals(containerTitle, citation.containerTitle)
      && Objects.equals(containerAuthor, citation.containerAuthor)
      && Objects.equals(volume, citation.volume)
      && Objects.equals(issue, citation.issue)
      && Objects.equals(edition, citation.edition)
      && Objects.equals(page, citation.page)
      && Objects.equals(collectionTitle, citation.collectionTitle)
      && Objects.equals(collectionEditor, citation.collectionEditor)
      && Objects.equals(doi, citation.doi)
      && Objects.equals(isbn, citation.isbn)
      && Objects.equals(issn, citation.issn)
      && Objects.equals(url, citation.url)
      && Objects.equals(accessed, citation.accessed)
      && Objects.equals(note, citation.note)
      && Objects.equals(additionalProperties, citation.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, title, version, author, editor, publisher, publisherPlace, issued, containerTitle,
      containerAuthor, volume, issue, edition, page, collectionTitle, collectionEditor, doi, isbn, issn, url, accessed,
      note, additionalProperties);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Citation.class.getSimpleName() + "[", "]")
      .add("id='" + id + "'")
      .add("type=" + type)
      .add("title='" + title + "'")
      .add("version='" + version + "'")
      .add("author=" + author)
      .add("editor=" + editor)
      .add("publisher='" + publisher + "'")
      .add("publisherPlace='" + publisherPlace + "'")
      .add("issued=" + issued)
      .add("containerTitle='" + containerTitle + "'")
      .add("containerAuthor=" + containerAuthor)
      .add("volume='" + volume + "'")
      .add("issue='" + issue + "'")
      .add("edition='" + edition + "'")
      .add("page='" + page + "'")
      .add("collectionTitle='" + collectionTitle + "'")
      .add("collectionEditor=" + collectionEditor)
      .add("doi=" + doi)
      .add("isbn='" + isbn + "'")
      .add("issn='" + issn + "'")
      .add("url=" + url)
      .add("accessed=" + accessed)
      .add("note='" + note + "'")
      .add("additionalProperties=" + additionalProperties)
      .toString();
  }
}
