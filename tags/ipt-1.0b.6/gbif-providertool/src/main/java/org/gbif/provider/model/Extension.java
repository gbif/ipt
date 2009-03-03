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
import javax.persistence.Column;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.gbif.provider.model.voc.ExtensionType;
import org.hibernate.annotations.IndexColumn;

/**
 * An extension with a list of ExtensionProperties defined to extend some CoreRecord entity
 * @author markus
 *
 */
@Entity
public class Extension implements BaseObject, Comparable<Extension> {
	private Long id;	
	private String title;
	private String name; // table & file naming. no whitespace allowed
	private String namespace;
	private String link; // to documentation
	private boolean installed;
	private List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
	private boolean core=false;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=128)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Column(length=128)
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@Column(length=64)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name.replaceAll("\\s", "_");
	}
	@Transient
	public String getRowType() {
		if (namespace.endsWith("/") || namespace.endsWith("#")){
			return (this.namespace + this.name);
		}else{
			return (this.namespace + "/" + this.name);
		}
	}
	
	@Column(length=255)
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	@OneToMany(cascade=CascadeType.ALL)
	@IndexColumn(name = "property_order",base=0, nullable=false)
	@JoinColumn(name="extension_fk", nullable=false) 
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
	
	public boolean isCore() {
		return this.core;
	}
	public void setCore(boolean core) {
		this.core = core;
	}

	public boolean isInstalled() {
		return installed;
	}
	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Extension)) {
			return false;
		}
		Extension rhs = (Extension) object;
		return new EqualsBuilder()
				.append(this.link, rhs.link)
				.append(this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("id", this.id).toString();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Extension object) {
		return new CompareToBuilder().append(this.id, object.id).toComparison();
	}
	
	
}
