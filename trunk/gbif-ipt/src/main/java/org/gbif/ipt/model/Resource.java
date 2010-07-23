package org.gbif.ipt.model;

import org.gbif.ipt.model.eml.Eml;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Date;

/**
 * The main class to represent an IPT resource.
 * Its enumerated type property defines the kind of resource (Metadata, Checklist, Occurrence)
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 * 
 * @author markus
 * 
 */
public class Resource implements Serializable {
  private static final long serialVersionUID = 3832626162173352190L;

  private String shortname; // unique
  private String title;
  private String description;
  private ResourceType type;
  private PublicationStatus status;
  // properties which are only available when fully loaded from the config files
  private Eml eml;
  private ResourceConfiguration config;
  // resource meta-metadata
  private User creator;
  private Date created = new Date();
  private User modifier;
  private Date modified;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Resource)) {
      return false;
    }
    Resource o = (Resource) other;
    return equal(shortname, o.shortname);
  }

  public Date getCreated() {
    return created;
  }

  public User getCreator() {
    return creator;
  }

  public String getDescription() {
    return description;
  }

  public String getGuid() {
    // TODO Auto-generated method stub
    return null;
  }

  public Date getModified() {
    return modified;
  }

  public User getModifier() {
    return modifier;
  }

  public String getShortname() {
    return shortname;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public ResourceType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(shortname);
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setGuid(String guid) {
    // TODO Auto-generated method stub

  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setModifier(User modifier) {
    this.modifier = modifier;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  public void setStatus(PublicationStatus status) {
    this.status = status;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setType(ResourceType type) {
    this.type = type;
  }

}
