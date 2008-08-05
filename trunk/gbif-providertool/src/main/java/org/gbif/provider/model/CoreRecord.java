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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MappedSuperclass
public class CoreRecord implements BaseObject, Comparable<CoreRecord> {
	public static final String ID_COLUMN_NAME = "#id";
	public static final String MODIFIED_COLUMN_NAME = "#modified";

	protected static final Log log = LogFactory.getLog(CoreRecord.class);

	private Long id;
	private String localId;
	private String guid;
	private String link;
	private boolean isDeleted;
	private boolean isProblematic;
	private Date modified;
	private OccurrenceResource resource;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(length = 128)
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	@Column(length = 128)
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Column(length = 128)
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

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isProblematic() {
		return isProblematic;
	}

	public void setProblematic(boolean isProblematic) {
		this.isProblematic = isProblematic;
	}

	@ManyToOne
	public OccurrenceResource getResource() {
		return resource;
	}

	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}

	public int compareTo(CoreRecord object) {
		return this.id.compareTo(object.id);
	}

	@Transient
	public Map<String, String> getDataMap() {
		Map<String, String> m = new HashMap<String, String>();
		m.put(ID_COLUMN_NAME, this.getGuid());
		String modified = null;
		if (this.getModified() != null) {
			modified = this.getModified().toString();
		}
		m.put(MODIFIED_COLUMN_NAME, modified);
		return m;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("modified", this.modified)
				.append("id", this.id).append("link", this.link).append(
						"coreID", this.localId).append("deleted",
						this.isDeleted()).append("guid", this.guid).toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(164509673, -1797513509)
				.append(this.modified).append(this.localId).append(this.guid)
				.append(this.link).append(this.isDeleted).append(this.id)
				.toHashCode();
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
				this.link, rhs.link).append(this.isDeleted, rhs.isDeleted)
				.append(this.id, rhs.id).isEquals();
	}
}
