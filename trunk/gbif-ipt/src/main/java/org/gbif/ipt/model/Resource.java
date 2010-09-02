package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.metadata.eml.Eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
  private Eml eml=new Eml();
  private CoreRowType type;
  private String subtype;
  // publication
  private PublicationStatus status = PublicationStatus.PRIVATE;
  private int lastPublishedEmlHash=0;
  private int emlVersion=0;
  private Date lastPublished;
  private int recordsPublished=0;
  // registry data - only exists when status=REGISTERED
  private UUID key;
  private Organisation organisation;
  // resource meta-metadata
  private User creator;
  private Date created;
  private User modifier;
  private Date modified;
  private Set<User> managers = new HashSet<User>();
  // mapping configs
  private Set<Source> sources = new HashSet<Source>();
  private ExtensionMapping core;
  private Set<ExtensionMapping> extensions = new HashSet<ExtensionMapping>();

  public void addExtension(ExtensionMapping extension) {
    if (extension != null) {
      this.extensions.add(extension);
    }
  }

  public int getRecordsPublished() {
	return recordsPublished;
}
  public boolean isPublished(){
	  return lastPublished!=null;
  }

public void setRecordsPublished(int recordsPublished) {
	this.recordsPublished = recordsPublished;
}

public boolean isPublished() {
  return lastPublished!=null;
}


public Date getLastPublished() {
	return lastPublished;
}

public void setLastPublished(Date lastPublished) {
	this.lastPublished = lastPublished;
}

public int getEmlVersion() {
	return emlVersion;
}

public void setEmlVersion(int emlVersion) {
	this.emlVersion = emlVersion;
	if (eml!=null){
		eml.setEmlVersion(emlVersion);
	}
}

public Eml getEml() {
	return eml;
}

public void setEml(Eml eml) {
	this.eml = eml;
}

public void addSource(Source src, boolean allowOverwrite) throws AlreadyExistingException {
    // make sure we talk about the same resource
    src.setResource(this);
    if (!allowOverwrite && sources.contains(src)) {
      throw new AlreadyExistingException();
    }
    sources.add(src);
  }

  public boolean deleteMapping(ExtensionMapping mapping) {
    if (mapping != null) {
      if (core.equals(mapping)) {
        core = null;
        return true;
      } else {
        return extensions.remove(mapping);
      }
    }
    return false;
  }

  public boolean deleteSource(Source src) {
    if (src != null) {
      return sources.remove(src);
    }
    return false;
  }

  public ExtensionMapping getCore() {
    return core;
  }

  public String getCoreRowType() {
    if (core != null && core.getExtension() != null) {
      return core.getExtension().getRowType();
    }
    return null;
  }

  public Set<ExtensionMapping> getExtensions() {
    return extensions;
  }

  public ExtensionMapping getMapping(String rowType) {
    if (rowType == null) {
      return null;
    }
    if (core != null && core.getExtension() != null && rowType.equals(core.getExtension().getRowType())) {
      return core;
    }
    for (ExtensionMapping em : extensions) {
      if (rowType.equals(em.getExtension().getRowType())) {
        return em;
      }
    }
    return null;
  }

  public Source getSource(String name) {
    if (name == null) {
      return null;
    }
    name = Source.normaliseName(name);
    for (Source s : sources) {
      if (s.getName().equals(name)) {
        return s;
      }
    }
    return null;
  }

  public List<Source> getSources() {
    List<Source> srcs = new ArrayList<Source>(sources);
    Collections.sort(srcs);
    return srcs;
  }

  public void setCore(ExtensionMapping core) {
    this.core = core;
  }

  public void setExtensions(Set<ExtensionMapping> extensions) {
    this.extensions = extensions;
  }

  public void addManager(User manager) {
    if (manager != null) {
      this.managers.add(manager);
    }
  }

  public int getLastPublishedEmlHash() {
	return lastPublishedEmlHash;
}

public void setLastPublishedEmlHash(int lastPublishedEmlHash) {
	this.lastPublishedEmlHash = lastPublishedEmlHash;
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
	  if (eml!=null){
		    return eml.getDescription();		  
	  }
	  return null;
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
	  if (eml!=null){
		    return eml.getTitle();		  
	  }
	  return null;
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
    if (eml!=null && eml.getTitle()==null){
    	eml.setTitle(shortname);
    }
  }

  public void setStatus(PublicationStatus status) {
    this.status = status;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public void setTitle(String title) {
	  if (eml!=null){
		    this.eml.setTitle(title);
	  }
  }

  public void setType(CoreRowType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Resource " + shortname;
  }
}
