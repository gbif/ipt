package org.gbif.provider.model;

public interface Record {

	public Long getResourceId();
 
	public Long getCoreId();

	public String getPropertyValue(ExtensionProperty property);

}