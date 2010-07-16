package org.gbif.ipt.model;

import java.io.Serializable;
import java.util.Date;

import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;

/** The main class to represent an IPT resource.
 * Its enumerated type property defines the kind of resource (Metadata, Checklist, Occurrence)
 * A resource can be identified by its short name which has to be unique within an IPT instance.
 * @author markus
 *
 */
public class Resource implements Serializable {
    private static final long serialVersionUID = 3832626162173352190L;

    protected String shortname; // unique
    protected String title;
    protected String description;
    protected ResourceType type;
    protected PublicationStatus status;
    // resource meta-metadata
    protected User creator;
    protected Date created = new Date();
    protected User modifier;
    protected Date modified;
    
    
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ResourceType getType() {
		return type;
	}
	public void setType(ResourceType type) {
		this.type = type;
	}
	public PublicationStatus getStatus() {
		return status;
	}
	public void setStatus(PublicationStatus status) {
		this.status = status;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public User getModifier() {
		return modifier;
	}
	public void setModifier(User modifier) {
		this.modifier = modifier;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public String getGuid() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setGuid(String guid) {
		// TODO Auto-generated method stub
		
	}
    
}
