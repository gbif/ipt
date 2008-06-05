package org.gbif.provider.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class StarExtension {
	private Long id;	
	private String name;
	private String namespace;
	private URL documentation;
	private ArrayList<String> properties = new ArrayList<String>();

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
	
	public URL getDocumentation() {
		return documentation;
	}
	public void setDocumentation(URL documentation) {
		this.documentation = documentation;
	}
	
	@Lob
	public ArrayList<String> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<String> properties) {
		this.properties = properties;
	}
	
	
}
