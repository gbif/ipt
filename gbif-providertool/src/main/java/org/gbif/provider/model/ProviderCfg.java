package org.gbif.provider.model;

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

@Entity
public class ProviderCfg {
	private Long id;
	private ResourceMetadata meta = new ResourceMetadata();
	private String descriptionImage;
	private String baseUrl;
	private String geoserverUrl;
	private String googleMapsApiKey;
	
	
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
	public String getGoogleMapsApiKey() {
		return googleMapsApiKey;
	}
	public void setGoogleMapsApiKey(String googleMapsApiKey) {
		googleMapsApiKey = StringUtils.trimToNull(googleMapsApiKey);
		this.googleMapsApiKey = googleMapsApiKey;
	}
	
	@Transient
	public String getEmlUrl() {
		return String.format("%s/eml/provider.eml", baseUrl);
	}

}
