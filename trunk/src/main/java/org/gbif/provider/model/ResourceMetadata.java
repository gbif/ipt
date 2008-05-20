/**
 * 
 */
package org.gbif.provider.model;


import javax.persistence.Entity;

/**
 * @author markus
 *
 */
@Entity
public class ResourceMetadata extends ResolvableBase{
	private String title;
	private String description;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
