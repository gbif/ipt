package org.gbif.provider.model.voc;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import org.gbif.provider.model.Resource;

import java.util.Date;

public class ResourceDisplay {
  private Long id;
  String title;
  Date modified;
  String creatorFullName;
  PublicationStatusForDisplay status;
  String type;

  public ResourceDisplay(Resource r) {
    if (r == null) {
      type = "stub";
      return;
    }
    if (r.isRegistered()) {
      if (r.isDirty()) {
        status = PublicationStatusForDisplay.UNPUBLISHED_CHANGES;
      } else {
        status = PublicationStatusForDisplay.PUBLISHED;
      }
    } else {
      status = PublicationStatusForDisplay.PRIVATE;
    }
    id = r.getId();
    title = r.getTitle();
    modified = r.getModified();
    creatorFullName = r.getCreator().getFullName();
    type = r.getType();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ResourceDisplay)) {
      return false;
    }
    ResourceDisplay o = (ResourceDisplay) obj;
    return equal(type, o.type) && equal(status, o.status)
        && equal(title, o.title) && equal(modified, o.modified)
        && equal(creatorFullName, o.creatorFullName);
  }

  public String getCreatorFullName() {
    return creatorFullName;
  }

  public Long getId() {
    return id;
  }

  public Date getModified() {
    return modified;
  }

  public PublicationStatusForDisplay getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, status, title, modified, creatorFullName);
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format(
        "Type=%s, Status=%s, Title=%s, Modified=%s, CreatorFullName=%s", type,
        status, title, modified, creatorFullName);
  }
}