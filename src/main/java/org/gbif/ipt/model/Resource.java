package org.gbif.ipt.model;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.metadata.eml.Eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import org.apache.log4j.Logger;

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
 */
public class Resource implements Serializable, Comparable<Resource> {
  public enum CoreRowType {
    OCCURRENCE, CHECKLIST
  }

  private static Logger log = Logger.getLogger(Resource.class);

  private static final TermFactory fact = new TermFactory();

  private static final long serialVersionUID = 3832626162173352190L;;
  private String shortname; // unique
  private Eml eml = new Eml();
  private String subtype;
  // publication
  private PublicationStatus status = PublicationStatus.PRIVATE;
  private int emlVersion = 0;
  private Date lastPublished;
  private int recordsPublished = 0;
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
  private List<ExtensionMapping> mappings = new ArrayList<ExtensionMapping>();

  public void addManager(User manager) {
    if (manager != null) {
      this.managers.add(manager);
    }
  }

  /**
   * Adds a new extension mapping to the resource. For non core extensions a core extension must exist already.
   * It returns the list index for this mapping according to getMappings(rowType)
   * 
   * @param mapping
   * @return list index corresponding to getMappings(rowType) or null if the mapping couldnt be added
   * @throws IllegalArgumentException if no core mapping exists when adding a non core mapping
   */
  public Integer addMapping(ExtensionMapping mapping) throws IllegalArgumentException {
    if (mapping != null && mapping.getExtension() != null) {
      if (!mapping.isCore() && !hasCore()) {
        throw new IllegalArgumentException("Cannot add extension mapping before a core mapping exists");
      }
      Integer index = getMappings(mapping.getExtension().getRowType()).size();
      this.mappings.add(mapping);
      return index;
    }
    return null;
  }

  public void addSource(Source src, boolean allowOverwrite) throws AlreadyExistingException {
    // make sure we talk about the same resource
    src.setResource(this);
    if (!allowOverwrite && sources.contains(src)) {
      throw new AlreadyExistingException();
    }
    sources.add(src);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Resource o) {
    return shortname.compareToIgnoreCase(o.shortname);
  }

  public boolean deleteMapping(ExtensionMapping mapping) {
    boolean result = false;
    if (mapping != null) {
      result = mappings.remove(mapping);
      if (result && mapping.isCore()) {
        // if last core gets deleted, delete all other mappings too!
        if (getCoreMappings().isEmpty()) {
          mappings.clear();
        }
      }
    }
    return result;
  }

  public boolean deleteSource(Source src) {
    boolean result = false;
    if (src != null) {
      result = sources.remove(src);
      // also remove existing mappings
      List<ExtensionMapping> ems = new ArrayList<ExtensionMapping>(mappings);
      for (ExtensionMapping em : ems) {
        if (em.getSource().equals(src)) {
          deleteMapping(em);
          log.debug("Cascading source delete to mapping " + em.getExtension().getTitle());
        }
      }
    }
    return result;
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

  public List<ExtensionMapping> getCoreMappings() {
    List<ExtensionMapping> cores = new ArrayList<ExtensionMapping>();
    for (ExtensionMapping m : mappings) {
      if (m.isCore()) {
        cores.add(m);
      }
    }
    return cores;
  }

  public String getCoreRowType() {
    List<ExtensionMapping> cores = getCoreMappings();
    if (cores.size() > 0) {
      return cores.get(0).getExtension().getRowType();
    }
    return null;
  }

  public ConceptTerm getCoreType() {
    List<ExtensionMapping> cores = getCoreMappings();
    if (cores.size() > 0) {
      return fact.findTerm(cores.get(0).getExtension().getRowType());
    }
    return null;
  }

  public Date getCreated() {
    return created;
  }

  public User getCreator() {
    return creator;
  }

  public String getDescription() {
    if (eml != null) {
      return eml.getDescription();
    }
    return null;
  }

  public Eml getEml() {
    return eml;
  }

  public int getEmlVersion() {
    return emlVersion;
  }

  public UUID getKey() {
    return key;
  }

  public Date getLastPublished() {
    return lastPublished;
  }

  public Set<User> getManagers() {
    return managers;
  }

  public List<Extension> getMappedExtensions() {
    Set<Extension> exts = new HashSet<Extension>();
    for (ExtensionMapping em : mappings) {
      exts.add(em.getExtension());
    }
    return new ArrayList<Extension>(exts);
  }

  public ExtensionMapping getMapping(String rowType, Integer index) {
    if (rowType != null && index != null) {
      List<ExtensionMapping> maps = getMappings(rowType);
      if (maps.size() >= index) {
        return maps.get(index);
      }
    }
    return null;
  }

  public List<ExtensionMapping> getMappings() {
    return mappings;
  }

  /**
   * Get the list of mappings for the requested extension rowtype.
   * The order of mappings in the list is guaranteed to be stable and the same as the underlying original mappings list.
   * 
   * @param rowType identifying the extension
   * @return the list of mappings for the requested extension rowtype
   */
  public List<ExtensionMapping> getMappings(String rowType) {
    List<ExtensionMapping> maps = new ArrayList<ExtensionMapping>();
    if (rowType != null) {
      for (ExtensionMapping m : mappings) {
        if (rowType.equals(m.getExtension().getRowType())) {
          maps.add(m);
        }
      }
    }
    return maps;
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

  public int getRecordsPublished() {
    return recordsPublished;
  }

  public String getShortname() {
    return shortname;
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

  public PublicationStatus getStatus() {
    return status;
  }

  public String getSubtype() {
    return subtype;
  }

  public String getTitle() {
    if (eml != null) {
      return eml.getTitle();
    }
    return null;
  }

  public String getTitleOrShortname() {
    if (eml != null) {
      return eml.getTitle();
    }
    return shortname;
  }

  /**
   * @return true if this resource is mapped to at least one core extension
   */
  public boolean hasCore() {
    if (getCoreType() != null) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(shortname);
  }

  public boolean hasMappedData() {
    for (ExtensionMapping cm : getCoreMappings()) {
      // test each core mapping if there is at least one field mapped
      if (cm.getFields().size() > 0) {
        return true;
      }
    }
    return false;
  }

  public boolean hasPublishedData() {
    return recordsPublished > 0;
  }

  public boolean isPublished() {
    return lastPublished != null;
  }

  public boolean isRegistered() {
    return key != null;
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

  public void setEml(Eml eml) {
    this.eml = eml;
  }

  public void setEmlVersion(int emlVersion) {
    this.emlVersion = emlVersion;
    if (eml != null) {
      eml.setEmlVersion(emlVersion);
    }
  }

  public void setKey(UUID key) {
    this.key = key;
  }

  public void setLastPublished(Date lastPublished) {
    this.lastPublished = lastPublished;
  }

  public void setManagers(Set<User> managers) {
    this.managers = managers;
  }

  public void setMappings(List<ExtensionMapping> extensions) {
    this.mappings = extensions;
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

  public void setRecordsPublished(int recordsPublished) {
    this.recordsPublished = recordsPublished;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
    if (eml != null && eml.getTitle() == null) {
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
    if (eml != null) {
      this.eml.setTitle(title);
    }
  }

  @Override
  public String toString() {
    return "Resource " + shortname;
  }

}
