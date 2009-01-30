package org.gbif.provider.model;

public interface Record {

	public Long getCoreId();

	public String getPropertyValue(ExtensionProperty property);

}