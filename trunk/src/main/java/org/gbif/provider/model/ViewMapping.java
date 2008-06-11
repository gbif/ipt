package org.gbif.provider.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A mapping between a resource and an extension (incl darwincore itself).
 * The ViewMapping defines the sql statement used to upload data for a certain extension,
 * therefore for every extension there exists a separate sql statement which should be uploaded one after the other.
 * @author markus
 *
 */
@Entity
public class ViewMapping implements Comparable {
	private Long id;	
	private OccurrenceResource resource;
	private DwcExtension extension;
	private String viewSql;
	private Set<PropertyMapping> propertyMappings = new HashSet<PropertyMapping>();
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(optional=false)
	public OccurrenceResource getResource() {
		return resource;
	}
	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}
	
	@ManyToOne
	public DwcExtension getExtension() {
		return extension;
	}
	public void setExtension(DwcExtension extension) {
		this.extension = extension;
	}
	
	public String getViewSql() {
		return viewSql;
	}
	public void setViewSql(String sql) {
		this.viewSql = sql;
	}
	
	// fetch=FetchType.EAGER
	@OneToMany(mappedBy="viewMapping", cascade=CascadeType.ALL)
	public Set<PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}
	public void setPropertyMappings(Set<PropertyMapping> propertyMappings) {
		this.propertyMappings = propertyMappings;
	}
	
	
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		ViewMapping myClass = (ViewMapping) object;
		return new CompareToBuilder().append(this.extension, myClass.extension)
				.append(this.viewSql, myClass.viewSql)
				.append(this.resource, myClass.resource).append(this.id,
						myClass.id).toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ViewMapping)) {
			return false;
		}
		ViewMapping rhs = (ViewMapping) object;
		return new EqualsBuilder().append(this.extension, rhs.extension)
				.append(this.viewSql, rhs.viewSql).append(
						this.resource, rhs.resource).append(this.id, rhs.id)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(651663619, 212381131).append(this.extension)
				.append(this.viewSql).append(
						this.resource).append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("viewSql", this.viewSql).append(
				"resource", this.resource).append("id", this.id).append(
				"extension", this.extension).toString();
	}
	
	
}
