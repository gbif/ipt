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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

@Entity
public class ExtensionProperty implements BaseObject, Comparable<ExtensionProperty> {
	private Long id;
	private Extension extension;
	private String name;
	private String namespace;
	private String qualName;
	private int columnLength;
	private String link;
	private boolean required;
	private ThesaurusVocabulary vocabulary;

	public ExtensionProperty() {
		super();
	}
	public ExtensionProperty(String qualName) {
		super();
		this.qualName = qualName;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "extension_fk", insertable = false, updatable = false, nullable = false)
	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	@Column(length=255)
	public String getQualName() {
		return qualName;
	}

	public void setQualName(String qualName) {
		this.qualName = qualName;
	}

	@Column(length=64)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public String getHQLName() {
		return StringUtils.uncapitalize(name);
	}

	@Column(length=128)
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * The length of the database column to be generated when the extension
	 * property is installed. Also used to trim incoming data before SQL insert is generated.
	 * For LOB columns use -1 or any negative value
	 * 
	 * @return
	 */
	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	@Column(length=255)
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

	@ManyToOne(optional = true)
	public ThesaurusVocabulary getVocabulary() {
		return vocabulary;
	}
	public void setVocabulary(ThesaurusVocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}
	public boolean hasTerms() {
		return vocabulary != null;
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
	 * Just compare the unique qualified names to see if extension properties are equal
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ExtensionProperty)) {
			return false;
		}
		ExtensionProperty prop = (ExtensionProperty) o;
		return (this.qualName == null ? prop.qualName == null : qualName.equals(prop.qualName));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (qualName != null ? qualName.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + columnLength;
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name != null ? name : id.toString();
	}

}
