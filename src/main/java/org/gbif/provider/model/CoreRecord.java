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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.appfuse.model.BaseObject;
import org.gbif.provider.service.Resolvable;
import org.gbif.provider.service.impl.OccurrenceUploadManagerImpl;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MappedSuperclass
public class CoreRecord extends BaseObject implements Comparable<CoreRecord>, Resolvable {
	protected static final Log log = LogFactory.getLog(CoreRecord.class);

	private Long id;
	private String localId;
	private String guid;
	private String link;
	private DatasourceBasedResource resource;
	private boolean isDeleted;
	private Date modified;
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();
	private Extension extension;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=128)
	public String getLocalId() {
		return localId;
	}
	public void setLocalId(String localId) {
		this.localId = localId;
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
	
	@ManyToOne
	public DatasourceBasedResource getResource() {
		return resource;
	}
	public void setResource(DatasourceBasedResource resource) {
		this.resource = resource;
	}
		
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}


	@Transient
	public Map<ExtensionProperty, String> getProperties() {
		return properties;
	}
	private void setProperties(Map<ExtensionProperty, String> properties) {
		this.properties = properties;
	}
	public void setPropertyValue(ExtensionProperty property, String value) {
		// check if this is the first property ever set. 
		// If so, remember the extension and check all further added properties
		if (extension == null){
			extension = property.getExtension();
		}else{
			if (!extension.equals(property.getExtension())){
				throw new IllegalArgumentException();
			}
		}
		properties.put(property, value);
		
	}
	
	public String getPropertyValue(ExtensionProperty property){
		return properties.get(property);
	}
	
	/**
	 * Get the extension this record belongs to. 
	 * The property is being set by the set properties methods which guarantee that all properties
	 * of this record belong to the same extension.
	 * @return
	 */
	@Transient
	public Extension getExtension() {
		return extension;
	}
	
	
	
	public int compareTo(CoreRecord object) {
		return this.id.compareTo(object.id); 
	}

	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("modified", this.modified)
				.append("resource", this.resource).append("id", this.id)
				.append("link", this.link).append("coreID", this.localId)
				.append("deleted", this.isDeleted()).append("guid", this.guid)
				.toString();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(164509673, -1797513509).append(this.modified).append(this.localId)
				.append(this.guid).append(this.link).append(this.resource)
				.append(this.isDeleted).append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof CoreRecord)) {
			return false;
		}
		CoreRecord rhs = (CoreRecord) object;
		return new EqualsBuilder().append(this.modified, rhs.modified).append(
				this.localId, rhs.localId).append(this.guid, rhs.guid).append(
				this.link, rhs.link).append(this.resource, rhs.resource)
				.append(this.isDeleted, rhs.isDeleted).append(this.id, rhs.id)
				.isEquals();
	}	
}
