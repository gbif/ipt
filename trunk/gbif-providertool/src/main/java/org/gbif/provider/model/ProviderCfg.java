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
	private ResourceMetadata org = new ResourceMetadata();
	private String orgPassword;
	private String orgNode;
	private ResourceMetadata ipt = new ResourceMetadata();
	private String descriptionImage;
	private String baseUrl;
	private String googleMapsApiKey;
	private String geoserverUrl;
	private String geoserverDataDir;
	private String geoserverUser;
	private String geoserverPass;
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=128)
	public String getOrgPassword() {
		return orgPassword;
	}
	public void setOrgPassword(String orgPassword) {
		this.orgPassword = orgPassword;
	}

	@Column(length=128)
	public String getOrgNode() {
		return orgNode;
	}
	public void setOrgNode(String orgNode) {
		this.orgNode = orgNode;
	}
	public ResourceMetadata getOrgMeta() {
		return org;
	}
	public void setOrgMeta(ResourceMetadata meta) {
		this.org = meta;
	}	

	public ResourceMetadata getIptMeta() {
		return ipt;
	}
	public void setIptMeta(ResourceMetadata meta) {
		this.ipt = meta;
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
	public String getGeoserverDataDir() {
		return geoserverDataDir;
	}
	public void setGeoserverDataDir(String geoserverDataDir) {
		this.geoserverDataDir = geoserverDataDir;
	}

	@Column(length=64)
	public String getGeoserverUser() {
		return geoserverUser;
	}
	public void setGeoserverUser(String geoserverUser) {
		this.geoserverUser = geoserverUser;
	}

	@Column(length=64)
	public String getGeoserverPass() {
		return geoserverPass;
	}
	public void setGeoserverPass(String geoserverPass) {
		this.geoserverPass = geoserverPass;
	}
	
	@Column(length=128)
	public String getGoogleMapsApiKey() {
		return googleMapsApiKey;
	}
	public void setGoogleMapsApiKey(String googleMapsApiKey) {
		googleMapsApiKey = StringUtils.trimToNull(googleMapsApiKey);
		this.googleMapsApiKey = googleMapsApiKey;
	}
	
}
