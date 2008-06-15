package org.gbif.provider.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
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
	private Set<ViewMapping> mappings = new HashSet<ViewMapping>();
		
	@OneToMany(mappedBy="resource", cascade=CascadeType.ALL)
	public Set<ViewMapping> getMappings() {
		return mappings;
	}
	public void setMappings(Set<ViewMapping> mappings) {
		this.mappings = mappings;
	}
	public void addMapping(ViewMapping mapping) {
		mapping.setResource(this);
		this.mappings.add(mapping);
	}
	

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof OccurrenceResource)) {
			return false;
		}
		OccurrenceResource rhs = (OccurrenceResource) object;
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
