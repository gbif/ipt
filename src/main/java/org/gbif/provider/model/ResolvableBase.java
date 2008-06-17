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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.appfuse.model.BaseObject;
import org.gbif.provider.model.hibernate.Timestampable;
import org.gbif.provider.service.Resolvable;
import org.hibernate.annotations.Type;

/**
 * @author markus
 *
 */
@MappedSuperclass
public abstract class ResolvableBase extends BaseObject implements Comparable<ResolvableBase>, Resolvable{
	private Long id;
	private String uuid;
	private String uri;
	private Date modified;
	
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	//@Type(type="uuid")
	@Column(length=36)
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
		
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	

	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(ResolvableBase object) {
		return this.id.compareTo(object.id); 
	}
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ResolvableBase)) {
			return false;
		}
		ResolvableBase rhs = (ResolvableBase) object;
		return new EqualsBuilder().append(
				this.uri, rhs.uri).append(this.uuid, rhs.uuid).append(this.id,
				rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-881154887, 1796106617).append(this.uri).append(this.uuid).append(
				this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).toString();
	}


}
