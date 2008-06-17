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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.IndexColumn;

@Entity
public class DwcExtension implements Comparable<DwcExtension> {
	private Long id;	
	private String name;
	private String namespace;
	private String link;
	private List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();

	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	@OneToMany(cascade=CascadeType.ALL)
	@IndexColumn(name = "property_order",base=0, nullable=false)
	@JoinColumn(name="extension_id", nullable=false) 
	public List<ExtensionProperty> getProperties() {
		return properties;
	}
	public void setProperties(List<ExtensionProperty> properties) {
		this.properties = properties;
	}	
	
	public void addProperty(ExtensionProperty property) {
		property.setExtension(this);
		properties.add(property);
	}

	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof DwcExtension)) {
			return false;
		}
		DwcExtension rhs = (DwcExtension) object;
		return new EqualsBuilder().append(this.namespace, rhs.namespace)
				.append(this.link, rhs.link).append(this.name, rhs.name).append(
						this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1767311583, -1664356007).append(
				this.namespace).append(this.link).append(this.name).append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("id", this.id).append(
				"namespace", this.namespace).append("link", this.link)
				.toString();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(DwcExtension object) {
		return new CompareToBuilder().append(this.namespace, object.namespace)
				.append(this.id, object.id).toComparison();
	}
	
	
}
