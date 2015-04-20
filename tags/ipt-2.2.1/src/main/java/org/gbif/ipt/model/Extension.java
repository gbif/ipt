package org.gbif.ipt.model;

import org.gbif.dwc.terms.Term;
import org.gbif.ipt.config.AppConfig;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.google.common.base.Objects.equal;

/**
 * A Darwin Core extension definition.
 */
public class Extension implements Serializable {

  private static final long serialVersionUID = 543543543L;
  private String title; // human title
  private String name; // table, file & xml tag naming. no whitespace allowed
  private URL url;
  @SerializedName("identifier")
  private String rowType; // Custom serialized field for JSON.
  private String subject;
  private String description;
  private String namespace;
  private URL link; // to documentation
  private boolean installed;
  private List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
  private Date modified = new Date();

  public void addProperty(ExtensionProperty property) {
    property.setExtension(this);
    properties.add(property);
  }

  public int compareTo(Extension object) {
    return new CompareToBuilder().append(this.rowType, object.rowType).toComparison();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Extension)) {
      return false;
    }
    Extension o = (Extension) other;
    return equal(rowType, o.rowType);
  }

  public String getDescription() {
    return description;
  }

  public URL getLink() {
    return link;
  }

  public Date getModified() {
    return modified;
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public List<ExtensionProperty> getProperties() {
    return properties;
  }

  public ExtensionProperty getProperty(Term term) {
    return getProperty(term.qualifiedName());
  }

  public ExtensionProperty getProperty(String term) {
    if (term == null) {
      return null;
    }
    for (ExtensionProperty p : properties) {
      if (term.equalsIgnoreCase(p.getQualname())) {
        return p;
      }
    }
    return null;
  }

  public String getRowType() {
    return rowType;
  }

  public String getSubject() {
    return subject;
  }

  public String getTitle() {
    return title;
  }

  public URL getUrl() {
    return url;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rowType);
  }

  public boolean hasProperty(Term term) {
    return getProperty(term) != null;
  }

  public boolean hasProperty(String term) {
    return getProperty(term) != null;
  }

  public boolean isCore() {
    // the IPT can be configured to support different core types
    // Remember that being a core does not exclude the use as a leaf in the star schema
    return AppConfig.isCore(rowType);
  }

  public boolean isInstalled() {
    return installed;
  }

  /**
   * @return set of vocabularies used by this extension
   */
  public Set<Vocabulary> listVocabularies() {
    Set<Vocabulary> vocabs = new HashSet<Vocabulary>();
    for (ExtensionProperty prop : getProperties()) {
      if (prop.getVocabulary() != null) {
        vocabs.add(prop.getVocabulary());
      }
    }
    return vocabs;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  public void setLink(String link) {
    try {
      this.link = new URL(link);
    } catch (MalformedURLException e) {
    }
  }

  public void setLink(URL link) {
    this.link = link;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setName(String name) {
    this.name = name.replaceAll("\\s", "_");
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setProperties(List<ExtensionProperty> properties) {
    this.properties = properties;
  }

  public void setRowType(String rowType) {
    this.rowType = rowType;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", this.name).append("rowType", this.rowType).toString();
  }

}
