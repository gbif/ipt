/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.model;


import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.appfuse.model.BaseObject;
import org.appfuse.model.User;
import org.gbif.provider.model.hibernate.Timestampable;
import org.gbif.provider.service.Resolvable;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A generic resource describing any digitial, online and non digital available biological resources
 * Should be replaced by a proper GBRDS model class.
 * Lacking most properties and multilingual abilities
 * @author markus
 *
 */
@Entity
public class Resource extends BaseObject implements Comparable<Resource>, Resolvable, Timestampable{
	private Long id;
	private String guid;
	private String link;
	// resource metadata
	private String title;
	private String description;
	// resource meta-metadata
	private User creator;
	private Date created;
	private User modifier;
	private Date modified;
	
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=128)
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	@Column(length=128)
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

		
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	
	@Column(length=128)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Lob
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
	
	
	public int compareTo(Resource object) {
		if (this.title != null){
			return this.title.compareToIgnoreCase(object.getTitle());
		}else{
			return "".compareToIgnoreCase(object.getTitle());
		}
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Resource)) {
			return false;
		}
		Resource rhs = (Resource) object;
		return new EqualsBuilder().append(this.modified, rhs.modified).append(
				this.created, rhs.created).append(this.creator, rhs.creator)
				.append(this.title, rhs.title).append(this.modifier,
						rhs.modifier).append(this.description, rhs.description)
				.append(this.guid, rhs.guid).append(this.link, rhs.link)
				.append(this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1501230247, -1510855635).append(
				this.modified).append(this.created).append(this.creator)
				.append(this.title).append(this.modifier).append(
						this.description).append(this.guid).append(this.link)
				.append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("created", this.created)
				.append("modified", this.modified).append("creator",
						this.creator).append("description", this.description)
				.append("id", this.id).append("title", this.title).append(
						"link", this.link).append("modifier", this.modifier)
				.append("guid", this.guid).toString();
	}


}
