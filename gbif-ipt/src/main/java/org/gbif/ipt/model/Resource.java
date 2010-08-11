package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.PublicationStatus;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The main class to represent an IPT resource.
 * Its enumerated type property defines the kind of resource (Metadata, Checklist, Occurrence)
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 * 
 * @author markus
 * 
 */
public class Resource implements Serializable, Comparable<Resource> {
  public enum CoreRowType {
    OCCURRENCE, CHECKLIST
  }

  private static final long serialVersionUID = 3832626162173352190L;;
  private String shortname; // unique
  private String title;
  private String description;
  private CoreRowType type;
  private String subtype;
  private PublicationStatus status = PublicationStatus.PRIVATE;
  // resource meta-metadata
  private User creator;
  private Date created;
  private User modifier;
  private Date modified;
  private Set<User> managers = new HashSet<User>();
  // registry data - only exists when status=REGISTERED
  private UUID key;
  private Organisation organisation;

  public void addManager(User manager) {
    if (manager != null) {
      this.managers.add(manager);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Resource o) {
    return shortname.compareToIgnoreCase(o.shortname);
  }

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

  public UUID getKey() {
    return key;
  }

  public Set<User> getManagers() {
    return managers;
  }

  public Date getModified() {
    return modified;
  }

  public User getModifier() {
    return modifier;
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public String getShortname() {
    return shortname;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public String getSubtype() {
    return subtype;
  }

  public String getTitle() {
    return title;
  }

  public CoreRowType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(shortname);
  }

  public void setCreated(Date created) {
    this.created = created;
    if (modified == null) {
      modified = created;
    }
  }

  public void setCreator(User creator) {
    this.creator = creator;
    if (modifier == null) {
      modifier = creator;
    }
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setKey(UUID key) {
    this.key = key;
  }

  public void setManagers(Set<User> managers) {
    this.managers = managers;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setModifier(User modifier) {
    this.modifier = modifier;
  }

  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }

  public void setStatus(PublicationStatus status) {
    this.status = status;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setType(CoreRowType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Resource " + shortname;
  }
}
