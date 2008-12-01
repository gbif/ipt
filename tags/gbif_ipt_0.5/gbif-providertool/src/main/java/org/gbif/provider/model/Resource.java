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


import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.appfuse.model.User;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.hibernate.Timestampable;
import org.gbif.provider.util.AppConfig;
import org.hibernate.annotations.CollectionOfElements;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A generic resource describing any digitial, online and non digital available biological resources
 * Only keeps the basic properties, but links to a far more expressive EML file via the embedded ResourceMetadata type.
 * @author markus
 */
@Entity
public class Resource implements BaseObject, Comparable<Resource>, Timestampable{
	private Long id;
	protected String guid = UUID.randomUUID().toString();
	// resource metadata
	protected ResourceMetadata meta = new ResourceMetadata();
	// persistent EML properties so we can search & aggregate on them easily
	protected BBox geoCoverage;
	protected Set<String> keywords = new HashSet<String>();
	// resource meta-metadata
	protected User creator;
	protected Date created;
	protected User modifier;
	protected Date modified;
	
	
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

	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	@ManyToOne
	public User getModifier() {
		return modifier;
	}
	public void setModifier(User modifier) {
		this.modifier = modifier;
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
	
	@CollectionOfElements(fetch=FetchType.EAGER)
	//@JoinColumn(name = "eml_fk", nullable = false)
	public Set<String> getKeywords() {
		return keywords;
	}
	/** Persistent EML property. To change use eml.setKeywords()
	 * @param geoCoverage
	 */
	private void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
	
	public BBox getGeoCoverage() {
		return geoCoverage;
	}
	/** Persistent EML property. To change use eml.setGeoCoverage()
	 * @param geoCoverage
	 */
	private void setGeoCoverage(BBox geoCoverage) {
		this.geoCoverage = geoCoverage;
	}

	public ResourceMetadata getMeta() {
		if (meta==null){
			meta = new ResourceMetadata();			
		}
		return meta;
	}
	public void setMeta(ResourceMetadata meta) {
		this.meta = meta;
	}
	
	
	// DELEGATE METHODS
	@Transient
	public String getLink() {
		return getMeta().getLink();
	}
	@Transient
	public String getContactEmail() {
		return getMeta().getContactEmail();
	}
	@Transient
	public String getContactName() {
		return getMeta().getContactName();
	}
	@Transient
	public String getDescription() {
		return getMeta().getDescription();
	}
	@Transient
	public String getTitle() {
		return getMeta().getTitle();
	}
	@Transient
	public String getType() {
		return meta.getTitle();
	}
	

	public void setLink(String link) {
		meta.setLink(link);
	}
	public void setContactEmail(String contactEmail) {
		meta.setContactEmail(contactEmail);
	}
	public void setContactName(String contactName) {
		meta.setContactName(contactName);
	}
	public void setDescription(String description) {
		meta.setDescription(description);
	}
	public void setTitle(String title) {
		meta.setTitle(title);
	}
	public void setType(String type) {
		meta.setType(type);
	}
	

	
	/**
	 * updates persistent EML properties on resource based on EML values
	 */
	public void updateWithEml(Eml eml){
		// keywords
		Set<String> keys = new HashSet<String>();
		keys.addAll(eml.getKeywords());
		for (TaxonKeyword k : eml.getTaxonomicClassification()){
			keys.add(k.getCommonName());
			keys.add(k.getScientificName());
		}
		this.keywords = keys;
		// geoCoverage
		this.geoCoverage = eml.geographicCoverage().getBoundingCoordinates();
	}
	
	
	public int compareTo(Resource object) {
		if (this.getTitle() != null){
			return this.getTitle().compareToIgnoreCase(object.getTitle());
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
				.append(this.getTitle(), rhs.getTitle()).append(this.modifier,
						rhs.modifier).append(this.getDescription(), rhs.getDescription())
				.append(this.guid, rhs.guid).append(this.getLink(), rhs.getLink())
				.append(this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1501230247, -1510855635).append(
				this.modified).append(this.created).append(this.creator)
				.append(this.getTitle()).append(this.modifier).append(
						this.getDescription()).append(this.guid).append(this.getLink())
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("created", this.created)
				.append("modified", this.modified).append("creator",
						this.creator).append("description", this.getDescription())
				.append("id", this.id).append("title", this.getTitle()).append(
						"link", this.getLink()).append("modifier", this.modifier)
				.append("guid", this.guid).toString();
	}

}
