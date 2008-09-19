package org.gbif.provider.model;

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class ProviderCfg {
	private Long id;
	private ResourceMetadata meta = new ResourceMetadata();
	private String descriptionImage = "images/provider-description.jpg";
	private String baseUrl = "http://localhost:8080";
	private String geoserverUrl = "http://localhost:8080/geoserver";
	private String dataDir = "/tmp/gbif/ipt";
	
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public ResourceMetadata getMeta() {
		return meta;
	}
	public void setMeta(ResourceMetadata meta) {
		this.meta = meta;
	}	
	
	public String getDescriptionImage() {
		return descriptionImage;
	}
	public void setDescriptionImage(String descriptionImage) {
		this.descriptionImage = descriptionImage;
	}
	
	@Column(length=128)
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	@Column(length=128)
	public String getGeoserverUrl() {
		return geoserverUrl;
	}
	public void setGeoserverUrl(String geoserverUrl) {
		this.geoserverUrl = geoserverUrl;
	}
	
	
	@Column(length=128)
    public String getDataDir() {
		return dataDir;
	}
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
		
}
