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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;

@Entity
public class DwcExtension implements Comparable {
	private Long id;	
	private String name;
	private String namespace;
	private String link;
	private Set<ExtensionProperty> properties = new HashSet<ExtensionProperty>();

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
	
	@OneToMany(mappedBy="extension", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	public Set<ExtensionProperty> getProperties() {
		return properties;
	}
	public void setProperties(Set<ExtensionProperty> properties) {
		this.properties = properties;
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
				.append(this.link, rhs.link).append(this.properties,
						rhs.properties).append(this.name, rhs.name).append(
						this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1767311583, -1664356007).append(
				this.namespace).append(this.link).append(this.properties)
				.append(this.name).append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append(
				"properties", this.properties).append("id", this.id).append(
				"namespace", this.namespace).append("link", this.link)
				.toString();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		DwcExtension myClass = (DwcExtension) object;
		return new CompareToBuilder().append(this.namespace, myClass.namespace)
				.append(this.link, myClass.link).append(this.properties,
						myClass.properties).append(this.name, myClass.name)
				.append(this.id, myClass.id).toComparison();
	}
	
	

	
	
}
