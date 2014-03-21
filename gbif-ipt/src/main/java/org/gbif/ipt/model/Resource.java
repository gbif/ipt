package org.gbif.ipt.model;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.ipt.model.voc.MaintUpFreqType;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.metadata.eml.Eml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;

import static com.google.common.base.Objects.equal;

/**
 * The main class to represent an IPT resource.
 * Its enumerated type property defines the kind of resource (Metadata, Checklist, Occurrence)
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 */
public class Resource implements Serializable, Comparable<Resource> {

  public enum CoreRowType {
    OCCURRENCE, CHECKLIST, METADATA, OTHER
  }

  private static Logger log = Logger.getLogger(Resource.class);

  private static final TermFactory TERM_FACTORY = TermFactory.instance();

  private static final long serialVersionUID = 3832626162173352190L;
  private String shortname; // unique
  private Eml eml = new Eml();
  private String coreType;
  private String subtype;
  // update frequency
  private MaintUpFreqType updateFrequency;
  // publication status
  private PublicationStatus status = PublicationStatus.PRIVATE;
  // publication mode
  private PublicationMode publicationMode;
  // resource version and eml version are the same
  private int emlVersion = 0;
  // last time resource was successfully published
  private Date lastPublished;
  // next time resource is scheduled to be pubished
  private Date nextPublished;
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
   * @return list index corresponding to getMappings(rowType) or null if the mapping couldnt be added
   *
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
    if (allowOverwrite && sources.contains(src)) {
      // If source file is going to be overwritten, it should be actually re-add it.
      sources.remove(src);
      // Changing the SourceBase in the ExtensionMapping object from the mapping list.
      for (ExtensionMapping ext : this.getMappings()) {
        if (ext.getSource().equals(src)) {
          ext.setSource(src);
        }
      }
    }
    sources.add(src);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Resource o) {
    return shortname.compareToIgnoreCase(o.shortname);
  }

  /**
   * Delete a Resource's mapping. If the mapping gets successfully deleted, and the mapping is a core type mapping,
   * and there are no additional core type mappings, all other mappings are also cleared.
   *
   * @param mapping ExtensionMapping
   *
   * @return if deletion was successful or not
   */
  public boolean deleteMapping(ExtensionMapping mapping) {
    boolean result = false;
    if (mapping != null) {
      result = mappings.remove(mapping);
      // if last core gets deleted, delete all other mappings too!
      if (result && mapping.isCore() && getCoreMappings().isEmpty()) {
        mappings.clear();
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
        if (em.getSource() != null && src.equals(em.getSource())) {
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

  /**
   * Get the rowType of the core mapping. This method first iterates through a list of the core mappings if there
   * are any. Then, since they will all be of the same core rowType, the first mapping is read for its rowType and
   * this String is returned.
   *
   * @return core rowType
   */
  public String getCoreRowType() {
    List<ExtensionMapping> cores = getCoreMappings();
    if (!cores.isEmpty()) {
      return cores.get(0).getExtension().getRowType();
    }
    return null;
  }

  public String getCoreType() {
    return coreType;
  }

  public Term getCoreTypeTerm() {
    List<ExtensionMapping> cores = getCoreMappings();
    if (!cores.isEmpty()) {
      return TERM_FACTORY.findTerm(cores.get(0).getExtension().getRowType());
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

  /**
   * Get resource version. Same as EML version.
   *
   * @return resource version
   */
  public int getEmlVersion() {
    return emlVersion;
  }

  /**
   * Get the next resource version.
   *
   * @return next resource version
   */
  public int getNextVersion() {
    return emlVersion + 1;
  }

  /**
   * Get the last resource version.
   *
   * @return last resource version
   */
  public int getLastVersion() {
    return emlVersion - 1;
  }

  public UUID getKey() {
    return key;
  }

  /**
   * Return the date the resource was last published successfully.
   *
   * @return the date the resource was last published successfully
   */
  public Date getLastPublished() {
    return lastPublished;
  }

  /**
   * Return the date the resource is scheduled to be published next.
   *
   * @return the date the resource is scheduled to be published next.
   */
  public Date getNextPublished() {
    return nextPublished;
  }

  public Set<User> getManagers() {
    return managers;
  }

  public List<Extension> getMappedExtensions() {
    Set<Extension> exts = new HashSet<Extension>();
    for (ExtensionMapping em : mappings) {
      if (em.getExtension() != null && em.getSource() != null) {
        exts.add(em.getExtension());
      } else {
        log.error("ExtensionMapping referencing NULL Extension or Source for resource: " + getShortname());
      }
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
   * The order of mappings in the list is guaranteed to be stable and the same as the underlying original mappings
   * list.
   *
   * @param rowType identifying the extension
   *
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
    name = SourceBase.normaliseName(name);
    for (Source s : sources) {
      if (s.getName().equals(name)) {
        return s;
      }
    }
    return null;
  }

  public List<Source> getSources() {
    return Ordering.natural().nullsLast().onResultOf(new Function<Source, String>() {
      @Nullable
      public String apply(@Nullable Source src) {
        return src.getName();
      }
    }).sortedCopy(sources);
  }

  public PublicationStatus getStatus() {
    return status;
  }

  /**
   * Return the PublicationMode of the resource. Default is PublicationMode.AUTO_PUBLISH_OFF meaning that the
   * resource must be republished manually, and that the resource has not been configured yet for auto-publishing.
   *
   * @return the PublicationMode of the resource, or PublicationMode.AUTO_PUBLISH_OFF if not set yet
   */
  public PublicationMode getPublicationMode() {
    return (publicationMode == null) ? PublicationMode.AUTO_PUBLISH_OFF: publicationMode;
  }

  public String getSubtype() {
    return subtype;
  }

  /**
   * Return the frequency with which changes and additions are made to the dataset after the initial dataset is
   * completed.
   *
   * @return the maintenance update frequency
   */
  @Nullable
  public MaintUpFreqType getUpdateFrequency() {
    return updateFrequency;
  }

  public String getTitle() {
    if (eml != null) {
      return eml.getTitle();
    }
    return null;
  }

  /**
   * Build and return string composed of resource title and shortname in brackets if the title and shortname are
   * different. This string can be called to construct log messages.
   *
   * @return constructed string
   */
  public String getTitleAndShortname() {
    StringBuilder sb = new StringBuilder();
    if (eml != null) {
      sb.append(eml.getTitle());
      if (!shortname.equalsIgnoreCase(eml.getTitle())) {
        sb.append(" (").append(shortname).append(")");
      }
    }
    return sb.toString();
  }

  /**
   * @return true if this resource is mapped to at least one core extension
   */
  public boolean hasCore() {
    return getCoreTypeTerm() != null;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(shortname);
  }

  public boolean hasMappedData() {
    for (ExtensionMapping cm : getCoreMappings()) {
      // test each core mapping if there is at least one field mapped
      if (!cm.getFields().isEmpty()) {
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

  public void setCoreType(String coreType) {
    this.coreType = Strings.isNullOrEmpty(coreType) ? null : coreType;
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

  public void setNextPublished(Date nextPublished) {
    this.nextPublished = nextPublished;
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

  /**
   * Sets the resource PublicationMode. Its value must come from the Enumeration PublicationMode.
   *
   * @param publicationMode PublicationMode
   */
  public void setPublicationMode(PublicationMode publicationMode) {
    this.publicationMode = publicationMode;
  }

  /**
   * Sets the resource subtype. If it is null or an empty string, it is set to null. Otherwise, it is simply set
   * in lowercase.
   *
   * @param subtype subtype String
   */
  public void setSubtype(String subtype) {
    this.subtype = (Strings.isNullOrEmpty(subtype)) ? null : subtype.toLowerCase();
  }

  /**
   * Sets the maintenance update frequency. Its value comes in as a String, and gets matched to the Enumeration
   * MainUpFreqType. If no match occurs, the value is set to null.
   *
   * @param updateFrequency MainUpFreqType Enum
   */
  public void setUpdateFrequency(String updateFrequency) {
    this.updateFrequency = MaintUpFreqType.inferType(updateFrequency);
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

  /**
   * Check if the resource has been configured for auto-publishing. To qualify, the resource must have an update
   * frequency suitable for auto-publishing (annually, biannually, monthly, weekly, daily) or have a next published
   * date that isn't null, and must have auto-publishing mode turned on.
   *
   * @return true if the resource uses auto-publishing
   */
  public boolean usesAutoPublishing() {
    return publicationMode == PublicationMode.AUTO_PUBLISH_ON && updateFrequency != null;
  }
}
