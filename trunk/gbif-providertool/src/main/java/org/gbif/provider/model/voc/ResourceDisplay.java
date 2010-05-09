package org.gbif.provider.model.voc;

import org.gbif.provider.model.Resource;

import com.google.common.base.Objects;

import static com.google.common.base.Objects.equal;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

public class ResourceDisplay {
  private Long id;
  String title;
  Date modified;
  String creatorFullName;
  PublicationStatusForDisplay status;
  String type;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public ResourceDisplay(Resource r) {
    if(r.isRegistered()){
      if(r.isDirty()){
        status=PublicationStatusForDisplay.UNPUBLISHED_CHANGES;
      } else {
        status=PublicationStatusForDisplay.PUBLISHED;
      }
    } else {
      status=PublicationStatusForDisplay.PRIVATE;
    }
    title = r.getTitle();
    modified = r.getModified();
    creatorFullName = r.getCreator().getFullName();
    type = r.getType();
  }
  
  @Transient
  public PublicationStatusForDisplay getStatus(){
    return status;
  }
  
  @Transient
  public String getTitle(){
    return title;
  }

  @Transient
  public Date getModified(){
    return modified;
  }
  
  @Transient
  public String getCreatorFullName(){
    return creatorFullName;
  }

  @Transient
  public String getType(){
    return type;
  }
  
  public void setId(Long id) {
    this.id = id;
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
        && equal(title, o.title)
        && equal(modified, o.modified) && equal(creatorFullName, o.creatorFullName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, status, title, modified, creatorFullName);
  }

  @Override
  public String toString() {
    return String.format(
        "Type=%s, Status=%s, Title=%s, Modified=%s, CreatorFullName=%s",
        type, status, title, modified, creatorFullName);
  }
}

