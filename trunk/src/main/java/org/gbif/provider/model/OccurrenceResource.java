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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.gbif.provider.webapp.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MapKeyManyToMany;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DatasourceBasedResource {

	@Transient
	public ViewMapping getCoreMapping() {
		return this.getMappings().get(Constants.DARWIN_CORE_EXTENSION_ID);
	}

	@Transient
	public Collection<ViewMapping> getExtensionMappings() {
		Map<Long, ViewMapping> extMappings = new HashMap<Long, ViewMapping>();
		extMappings.putAll(this.getMappings());
		extMappings.remove(Constants.DARWIN_CORE_EXTENSION_ID);
		return extMappings.values();
	}		

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof OccurrenceResource)) {
			return false;
		}
		DatasourceBasedResource rhs = (DatasourceBasedResource) object;
		return new EqualsBuilder().appendSuper(super.equals(object)).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(830738695, -777913529).appendSuper(
				super.hashCode()).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append(
				"jdbcUser", this.getJdbcUser()).append("created",
				this.getCreated()).append("modified", this.getModified())
				.append("id", this.getId())
				.append("jdbcUrl", this.getJdbcUrl()).append("validConnection",
						this.isValidConnection()).append("modifier",
						this.getModifier()).append("uri", this.getUri())
				.append("jdbcDriverClass", this.getJdbcDriverClass()).append(
						"recordCount", this.getRecordCount()).append(
						"description", this.getDescription()).append("creator",
						this.getCreator()).append("title", this.getTitle())
				.append("serviceName", this.getServiceName()).append("datasource",
						this.getDatasource()).append("lastImport",
						this.getLastImport()).append("uuid", this.getUuid())
				.append("jdbcPassword", this.getJdbcPassword()).toString();
	}
	
}
