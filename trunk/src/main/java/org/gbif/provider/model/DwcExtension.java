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

@Entity
public class DwcExtension {
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
	
	
}
