package org.gbif.provider.model;

import javax.persistence.Entity;

@Entity
public class CoreViewMapping extends ViewMapping {
	private Integer guidColumnIndex;
	private Integer linkColumnIndex;
	
	
	public Integer getGuidColumnIndex() {
		return guidColumnIndex;
	}
	public void setGuidColumnIndex(Integer guidColumnIndex) {
		this.guidColumnIndex = guidColumnIndex;
	}
	
	public Integer getLinkColumnIndex() {
		return linkColumnIndex;
	}
	public void setLinkColumnIndex(Integer linkColumnIndex) {
		this.linkColumnIndex = linkColumnIndex;
	}
	
	
}
