package org.gbif.provider.model;

import org.gbif.provider.model.voc.ImageType;

public class ResourceStats implements BaseObject, ResourceRelatedObject{
	private Long id;
	private Resource resource;
	private String value;
	private Integer count;
	private ImageType statsType;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
	public ImageType getStatsType() {
		return statsType;
	}
	public void setStatsType(ImageType statsType) {
		this.statsType = statsType;
	}
	
	
	
}
