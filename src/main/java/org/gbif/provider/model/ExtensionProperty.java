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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

@Entity
public class ExtensionProperty implements Comparable<ExtensionProperty> {
	private Long id;
	private DwcExtension extension;
	private String uri;
	private String name;
	private String columnName;
	private String link;
	private boolean required;
	private List<String> terms = new ArrayList<String>();
	private int columnLength;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "extension_id", insertable = false, updatable = false, nullable = false)
	public DwcExtension getExtension() {
		return extension;
	}

	public void setExtension(DwcExtension extension) {
		this.extension = extension;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The database column name to be generated/used for this extension property
	 * Should be 32 characters max and only use lowercase alphabetical
	 * characters and underscore
	 * 
	 * @return
	 */
	@Column(length = 32)
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * The length of the database column to be generated when the extension
	 * property is installed
	 * 
	 * @return
	 */
	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@CollectionOfElements
	@IndexColumn(name = "term_order", base = 0, nullable = false)
	@JoinColumn(name = "ExtensionProperty_id", nullable = false)
	public List<String> getTerms() {
		return terms;
	}

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}

	public boolean hasTerms() {
		return !terms.isEmpty();
	}

	/**
	 * Simply compare by ID so we can store any comparison order when designing
	 * new extensions
	 * 
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(ExtensionProperty prop) {
		return this.id.compareTo(prop.id);
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ExtensionProperty)) {
			return false;
		}
		ExtensionProperty rhs = (ExtensionProperty) object;
		return new EqualsBuilder().append(this.extension, rhs.extension)
				.append(this.uri, rhs.uri).append(this.link, rhs.link).append(
						this.name, rhs.name).append(this.id, rhs.id).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1405305075, 321865033)
				.append(this.extension).append(this.uri).append(this.link)
				.append(this.name).append(this.id).toHashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("id",
				this.id).append("extension", this.extension).append("link",
				this.link).append("uri", this.uri).toString();
	}

}
