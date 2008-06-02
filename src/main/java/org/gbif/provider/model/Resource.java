/**
 * 
 */
package org.gbif.provider.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.appfuse.model.User;
import org.gbif.provider.model.hibernate.Timestampable;
import org.gbif.provider.service.Resolvable;

/**
 * A generic resource describing any digitial, online and non digital available biological resources
 * Should be replaced by a proper GBRDS model class.
 * Lacking most properties and multilingual abilities
 * @author markus
 *
 */
@Entity
public class Resource extends ResolvableBase implements Timestampable{
	// resource metadata
	private String title;
	private String description;
	// resource meta-metadata
	private User creator;
	private Date created;
	private User modifier;

	
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
	
	
	@ManyToOne
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
	
	@ManyToOne
	public User getModifier() {
		return modifier;
	}
	public void setModifier(User modifier) {
		this.modifier = modifier;
	}


}
